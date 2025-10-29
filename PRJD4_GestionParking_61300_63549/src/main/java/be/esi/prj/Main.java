package be.esi.prj;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL resource = Main.class.getResource("/fxml/MainView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Parent root = fxmlLoader.load();
        Image appIcon = new Image(
                Main.class.getResourceAsStream("/PappIcon.png")
        );
        stage.getIcons().add(appIcon);

        Scene scene = new Scene(root, 640, 800);
        stage.setTitle("FlowPark");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}