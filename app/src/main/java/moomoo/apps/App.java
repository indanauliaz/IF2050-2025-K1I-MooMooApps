
package moomoo.apps;

import moomoo.apps.model.*;
import moomoo.apps.utils.*;
// import moomoo.apps.view.*;
import moomoo.apps.controller.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL; 

public class App extends Application {

    @Override
    public void init() throws Exception{
        super.init();
        // DatabaseManager.deleteDatabaseFile();
        DatabaseManager.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) {
    try {

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
        scene.getStylesheets().add(css);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login Moo Moo Apps"); 
        primaryStage.setScene(scene);
        primaryStage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}