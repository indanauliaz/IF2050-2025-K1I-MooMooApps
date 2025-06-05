package moomoo.apps.controller; 

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
import javafx.scene.layout.VBox; // Pastikan ini VBox dari javafx.scene.layout
import javafx.stage.Stage;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.UserModel; 

import java.io.IOException;
import java.net.URL;
import javafx.geometry.Pos; // Tambahkan import untuk Pos

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
    private ToggleButton produksiButton;
    
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
    private ScrollPane mainContentScrollPane; // Ini adalah target utama kita

    // @FXML // mainContentArea sepertinya tidak terpakai jika kita langsung set content ke ScrollPane
    // private VBox mainContentArea; 

    // @FXML // welcomeMessageLabel sepertinya bagian dari dashboard content yang dibuat manual
    // private Label welcomeMessageLabel;
    
    private UserModel currentUser; 

    public void initialize() {
        menuToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // Mencegah deseleksi, selalu ada satu tombol yang aktif
                if (oldValue != null) {
                    oldValue.setSelected(true); 
                }
            } else {
                handleSidebarNavigation((ToggleButton) newValue);
            }
        });


        dashboardButton.setSelected(true);
       
        loadDashboardContent(); 
    }
    
    public void initData(UserModel user) {
        this.currentUser = user;
        updateUserInfo(user);
    }

    private void updateUserInfo(UserModel user) {
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());
        }
    }

    private void handleSidebarNavigation(ToggleButton selectedButton) {
        String viewPath = null;

        if (selectedButton == dashboardButton) {
            System.out.println("DEBUG: Dashboard selected");
            loadDashboardContent(); // Memuat konten dashboard dinamis
            return; 
        } else if (selectedButton == laporanButton) {
            System.out.println("DEBUG: Laporan selected");
            viewPath = "/moomoo/apps/view/LaporanSdmKonten.fxml"; 
        } else if (selectedButton == keuanganButton) {
            System.out.println("DEBUG: Keuangan selected");
            viewPath = "/moomoo/apps/view/FinanceView.fxml"; 
        } else if (selectedButton == tugasButton) {
            System.out.println("DEBUG: Tugas selected");
            viewPath = "/moomoo/apps/view/TaskManagement.fxml";
        } else if (selectedButton == produksiButton) { 
            System.out.println("DEBUG: Produksi selected. Path: /moomoo/apps/view/ProductionView.fxml");
            viewPath = "/moomoo/apps/view/ProductionView.fxml"; 
        }

        if (viewPath != null) {
            System.out.println("DEBUG: Attempting to load view: " + viewPath);
            loadView(viewPath, this.currentUser); 
        } else if (selectedButton != dashboardButton) { // Hanya tampilkan placeholder jika bukan dashboard dan viewPath null
            System.out.println("DEBUG: No FXML path defined for button: " + selectedButton.getText());
            showPlaceholderView("Konten untuk " + selectedButton.getText());
        }
    }
    
    private void loadDashboardContent() {
        System.out.println("DEBUG: Loading dashboard content.");
        VBox dashboardContent = new VBox(10); 
        dashboardContent.setPadding(new javafx.geometry.Insets(20));
        dashboardContent.setAlignment(Pos.TOP_CENTER); 
        // dashboardContent.getStyleClass().add("main-content-area"); // Beri style jika perlu

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); 
        // title.getStyleClass().add("dashboard-title"); 

        Label welcome = new Label();
        welcome.setStyle("-fx-font-size: 18px;"); 
        // welcome.getStyleClass().add("welcome-message"); 
        if (currentUser != null) {
            welcome.setText("Selamat Datang kembaliii, " + currentUser.getUsername() + "!");
        } else {
            welcome.setText("Selamat Datang!");
        }
    
        // Label infoLain = new Label("Ini adalah halaman utama aplikasi Moo Moo.");
        
        dashboardContent.getChildren().addAll(title, welcome /*, infoLain */);
        
        mainContentScrollPane.setContent(dashboardContent); 
        mainContentScrollPane.setFitToWidth(true); 
        mainContentScrollPane.setFitToHeight(true); 
        System.out.println("DEBUG: Dashboard content loaded into ScrollPane.");
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

            // String css = getClass().getResource("/moomoo/apps/view/style_precise.css").toExternalForm();
            // loginScene.getStylesheets().add(css);

            currentStage.setScene(loginScene);
            currentStage.setTitle("Login Moo Moo Apps");
            currentStage.centerOnScreen(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, UserModel user) {
        System.out.println("DEBUG: loadView called with FXML path: " + fxmlPath);
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("ERROR: FXML Resource not found: " + fxmlPath);
                showErrorInScrollPane("File FXML tidak ditemukan: " + fxmlPath);
                return;
            }
            System.out.println("DEBUG: FXML Resource URL: " + resourceUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent view = loader.load(); 
            System.out.println("DEBUG: FXML loaded successfully: " + fxmlPath);

            Object controller = loader.getController();
           
            if (user != null && controller instanceof UserAwareController) {
                System.out.println("DEBUG: Initializing controller (" + controller.getClass().getSimpleName() + ") with user data.");
                ((UserAwareController) controller).initData(user);
            // } else if (user == null && controller instanceof UserAwareController) {
            // System.out.println("DEBUG: UserModel is null, not calling initData on UserAwareController: " + controller.getClass().getSimpleName());
            } else if (controller != null){
                 System.out.println("DEBUG: Controller loaded ("+ controller.getClass().getSimpleName() +") but not UserAwareController or user is null.");
            } else {
                System.out.println("DEBUG: No controller found for " + fxmlPath);
            }

            mainContentScrollPane.setContent(view);
           
            mainContentScrollPane.setFitToWidth(true); 
            mainContentScrollPane.setFitToHeight(true);
            System.out.println("DEBUG: Content set to ScrollPane for: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("ERROR: IOException while loading FXML: " + fxmlPath);
            e.printStackTrace(); 
            showErrorInScrollPane("Gagal memuat tampilan: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\nError: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception in loadView for " + fxmlPath);
            e.printStackTrace();
            showErrorInScrollPane("Terjadi kesalahan tidak terduga saat memuat: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1));
        }
    }
    
    private void showPlaceholderView(String pageName) {
        VBox placeholderContent = new VBox();
        placeholderContent.setPadding(new javafx.geometry.Insets(20));
        placeholderContent.setAlignment(Pos.CENTER); // Menggunakan Pos.CENTER
        Label placeholderLabel = new Label("Ini Halaman " + pageName + " (Belum Diimplementasikan)");
        placeholderLabel.setStyle("-fx-font-size: 18px;");
        placeholderContent.getChildren().add(placeholderLabel);
        mainContentScrollPane.setContent(placeholderContent);
        mainContentScrollPane.setFitToWidth(true); // Pastikan placeholder juga fit
        mainContentScrollPane.setFitToHeight(true);
        System.out.println("DEBUG: Placeholder view shown for: " + pageName);
    }

    // Helper method untuk menampilkan pesan error di ScrollPane
    private void showErrorInScrollPane(String message) {
        VBox errorContainer = new VBox();
        errorContainer.setPadding(new javafx.geometry.Insets(20));
        errorContainer.setAlignment(Pos.CENTER);
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-wrap-text: true;");
        errorContainer.getChildren().add(errorLabel);
        mainContentScrollPane.setContent(errorContainer);
        mainContentScrollPane.setFitToWidth(true);
        mainContentScrollPane.setFitToHeight(true);
    }
}