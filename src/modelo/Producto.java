package modelo;

/**
 *
 * @author Matias
 */

import java.io.Serializable;

public class Producto implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private String nombre;
    private Double precio;
    private int stock;
    
    public Producto(String nombre,Double precio,int stock){
        this.nombre=nombre;
        this.precio=precio;
        this.stock=stock;
    }
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public Double getPrecio() {
        return precio;
    }
    public void setPrecio(Double precio) {
        this.precio = precio;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }

        @Override
    public String toString() {
        return "Producto [nombre=" + nombre + ", precio=" + precio + ", stock=" + stock + "]";
    }    
}