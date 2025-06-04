package moomoo.apps.controller; // Sesuaikan dengan package Anda

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import moomoo.apps.model.UserModel; // Asumsi Anda memiliki UserModel
// import moomoo.apps.utils.UserSession; // Jika Anda menggunakan UserSession

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleGroup menuToggleGroup;

    @FXML
    private ToggleButton dashboardButton;

    @FXML
    private ToggleButton laporanButton;

    @FXML
    private ToggleButton keuanganButton;

    @FXML
    private ToggleButton tugasButton;
    
    @FXML
    private Button settingsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private TextField searchField;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private ScrollPane mainContentScrollPane;

    @FXML
    private VBox mainContentArea; 

    @FXML
    private Label welcomeMessageLabel;
    
    private UserModel currentUser; 

    public void initialize() {
        menuToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
             
                if (oldValue != null) {
                    oldValue.setSelected(true); 
                }
            } else {
                handleSidebarNavigation((ToggleButton) newValue);
            }
        });


        dashboardButton.setSelected(true);

    }
    

    public void initData(UserModel user) {
        this.currentUser = user;
        updateUserInfo(user);
        if (user != null) {
            welcomeMessageLabel.setText("Selamat Datang, " + user.getUsername() + "!");
        }
    }

    private void updateUserInfo(UserModel user) {
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());
        }
    }

    // INI NANTI DIGANTI YAA
    private void handleSidebarNavigation(ToggleButton selectedButton) {
        mainContentArea.getChildren().clear(); 

        if (selectedButton == dashboardButton) {
            System.out.println("Dashboard selected");
            loadDashboardContent();
        } else if (selectedButton == laporanButton) {
            System.out.println("Laporan selected");
            Label laporanLabel = new Label("Ini Halaman Laporan (Belum Diimplementasikan)");
            laporanLabel.setStyle("-fx-font-size: 18px;");
            mainContentArea.getChildren().add(laporanLabel);
        } else if (selectedButton == keuanganButton) {
            System.out.println("Keuangan selected");
            loadView("/moomoo/apps/view/FinanceView.fxml", currentUser); 
        } else if (selectedButton == tugasButton) {
            System.out.println("Tugas selected");
             Label tugasLabel = new Label("Ini Halaman Tugas (Belum Diimplementasikan)");
            tugasLabel.setStyle("-fx-font-size: 18px;");
            mainContentArea.getChildren().add(tugasLabel);
        }
       
    }
    
    private void loadDashboardContent() {

         mainContentArea.getChildren().clear();
         Label title = new Label("Dashboard");
         title.getStyleClass().add("dashboard-title");
         
         Label welcome = new Label();
         welcome.getStyleClass().add("welcome-message");
         if (currentUser != null) {
             welcome.setText("Selamat Datang, " + currentUser.getUsername() + "!");
         } else {
             welcome.setText("Selamat Datang!");
         }
         mainContentArea.getChildren().addAll(title, welcome);
    }


    @FXML
    private void handleLogoutAction(ActionEvent event) {
        System.out.println("Logout action triggered");

        try {

            Stage currentStage = (Stage) logoutButton.getScene().getWindow();

            URL fxmlLocation = getClass().getResource("/moomoo/apps/view/LoginView.fxml");
            if (fxmlLocation == null) {
                System.err.println("LoginView.fxml not found!");
                return;
            }
            Parent loginRoot = FXMLLoader.load(fxmlLocation);
            Scene loginScene = new Scene(loginRoot);

            currentStage.setScene(loginScene);
            currentStage.setTitle("Login Moo Moo Apps");
            currentStage.centerOnScreen(); 
        

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void loadView(String fxmlPath, UserModel user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

         
            Object controller = loader.getController();
            if (user != null && controller instanceof KeuanganController) { 
                ((KeuanganController) controller).initData(user);
            }

            mainContentScrollPane.setContent(view); 

        } catch (IOException e) {
            e.printStackTrace();
         
            Label errorLabel = new Label("Gagal memuat tampilan: " + fxmlPath);

            mainContentScrollPane.setContent(errorLabel);
        }
    }
}