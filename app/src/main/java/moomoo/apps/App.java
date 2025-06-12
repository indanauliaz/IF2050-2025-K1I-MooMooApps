package moomoo.apps;
import moomoo.apps.utils.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        DatabaseManager.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-Medium.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-ExtraBold.ttf"), 12);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Poppins-Light.ttf"), 12);

            URL fxmlLocation = getClass().getResource("/moomoo/apps/view/LoginView.fxml");

            if (fxmlLocation == null) {
                System.err.println("Tidak dapat menemukan file FXML awal: LoginView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root);

            String css = getClass().getResource("/moomoo/apps/view/style_precise.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Login Moo Moo Apps");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
