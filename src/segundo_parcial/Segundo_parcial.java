package segundo_parcial;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Leandro_dev
 */

public class Segundo_parcial extends Application {

 public void start(Stage stage) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/TiendaView.fxml"));
        
        Scene scene = new Scene(loader.load());
        
        stage.setScene(scene);
        
        stage.setTitle("Venta de Productos");
        
        stage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }    
}
