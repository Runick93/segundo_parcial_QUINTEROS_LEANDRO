package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import modelo.Producto;
import modelo.ProductoCarrito;

public class TiendaViewController implements Initializable {
    private static final String ARCHIVO = "src/data/productos.dat";
    private static final String SALIDA = "src/salida/ticket.txt";
    
    List<Producto> productos = new ArrayList<>();
    List<ProductoCarrito> productosCarrito = new ArrayList<>();
    
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnConfirmar;

    @FXML
    private TextField tbCantidad;
    
    @FXML
    private TableView<Producto> tvProductos;
    @FXML
    private ListView<ProductoCarrito> lvCarrito;
    
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private void agregar(ActionEvent e){
        Producto productoSeleccionado = tvProductos.getSelectionModel().getSelectedItem();
        
        try {
            if (productoSeleccionado == null) {
                mostrarAlerta("Error, Producto no seleccionado, seleccione un producto para agregar");
                return;
            }
            
            if (tbCantidad.getText().isEmpty()) {
                mostrarAlerta("Error, cantidad no especificada, indique una cantidad");
                return;
            }
            
            int cantidad = Integer.parseInt(tbCantidad.getText().trim());
            
            if (cantidad <= 0) {
                mostrarAlerta("Error, la cantidad debe ser mayor a 0");
                return;
            }
            
            String nombre = productoSeleccionado.getNombre();
            Double precio = productoSeleccionado.getPrecio();
            int stock = productoSeleccionado.getStock();
            
            boolean encontrado = false;
            
            for (ProductoCarrito item : productosCarrito) {
                if (item.getProducto().getNombre().equals(productoSeleccionado.getNombre())) {
                    if (cantidad > stock) {
                        mostrarAlerta("Error, stock insuficiente");
                        return;
                    }
                    
                    item.setCantidad(item.getCantidad() + cantidad);
                    encontrado = true;
                    break;
                }
            }
            
            if (!encontrado) {
                if (cantidad > stock) {
                    mostrarAlerta("Error, stock insuficiente");
                    return;
                }
                ProductoCarrito nuevoItem = new ProductoCarrito(productoSeleccionado, cantidad);
                productosCarrito.add(nuevoItem);
            }
            
            productoSeleccionado.setStock(productoSeleccionado.getStock() - cantidad);
            tvProductos.refresh();
            
            lvCarrito.getItems().setAll(productosCarrito);
            
            tbCantidad.clear();
            
        } catch (NumberFormatException ex) {
            mostrarAlerta("Error, la cantidad debe ser un numero");
        }
    }

    @FXML
    private void confirmarCarrito(ActionEvent e){
        try {
            if (productosCarrito.isEmpty()) {
                mostrarAlerta("Error, carrito vacio, agregue algun producto para continuar");
                return;
            }
            
            double totalCompra = calcularTotal();
            StringBuilder ticket = new StringBuilder();
            
            ticket.append("Producto - Cantidad - Total\n");
            for (ProductoCarrito itemCarrito : productosCarrito) {
                Producto producto = itemCarrito.getProducto();
                int cantidad = itemCarrito.getCantidad();
                double subtotal = producto.getPrecio() * cantidad;
                
                ticket.append(producto.getNombre())
                    .append(" - ")
                    .append(cantidad)
                    .append(" - $")
                    .append(String.format("%.1f", subtotal))
                    .append("\n");
            }
            ticket.append("...\n");
            ticket.append("TOTAL A PAGAR: $").append(String.format("%.1f", totalCompra));
            
            FileWriter writer = null;
            try {
                writer = new java.io.FileWriter(SALIDA);
                writer.write(ticket.toString());
                
            } catch (IOException ex) {
                mostrarAlerta("Error al generar ticket - " + ex.getMessage());
                return;
            } finally {
                try {
                    if (writer != null) writer.close();
                } catch (IOException ex) {
                   mostrarAlerta(ex.getMessage());
                }
            }
            
            
            serializarProductos(productos);
            tvProductos.refresh();
            productosCarrito.clear();
            lvCarrito.getItems().setAll(productosCarrito);
            
            mostrarAlerta("Compra confirmada, se genero el ticket con los datos de su compra en " + SALIDA);
            
        } catch (Exception ex) {
            mostrarAlerta("Error: " + ex.getMessage());
        }
    }
    
    private double calcularTotal(){
        if (productosCarrito == null) {
            return 0;
        }
        
        double total = 0;
        for (ProductoCarrito item : productosCarrito) {
            Producto producto = item.getProducto();
            total += producto.getPrecio() * item.getCantidad();
        }
        return total;
    }
    
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
            colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
            
            List<Producto> productosLeidos = deserializarProducto();
            
            if (productosLeidos != null) {
                productos = new ArrayList<>(productosLeidos);
                tvProductos.getItems().setAll(productos);
                tvProductos.refresh();
            } else {
                throw new FileNotFoundException("Archivo no encontrado");
            }
        } catch (FileNotFoundException e) {
            mostrarAlerta("Error, archivo no encontrado - " + e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Ocurrio un error: " + e.getMessage());
        }
    }
    
    public List<Producto> deserializarProducto(){
        List<Producto> productos = null;
        ObjectInputStream ois = null;
        try {
            FileInputStream fis = new FileInputStream(ARCHIVO);
            ois = new ObjectInputStream(fis);
            productos = (List<Producto>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (ois != null) ois.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return productos;
    }
    
    public void serializarProductos(List<Producto> productos){
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fos = new FileOutputStream(ARCHIVO);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(productos);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (oos != null) oos.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}