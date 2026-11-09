package cl.duoc.viviendas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {
    private static final String VISTA_PRINCIPAL = "/cl/duoc/viviendas/view/main-view.fxml";

    @Override
    public void start(Stage stage) throws Exception {
        URL vista = App.class.getResource(VISTA_PRINCIPAL);
        if (vista == null) {
            throw new IllegalStateException("No se encontró la vista principal: " + VISTA_PRINCIPAL);
        }
        Parent root = FXMLLoader.load(vista);
        stage.setTitle("Administradora de Viviendas");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
