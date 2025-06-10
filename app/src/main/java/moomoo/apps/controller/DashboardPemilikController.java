package moomoo.apps.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.UserModel;

import java.io.IOException;
import java.net.URL;

public class DashboardPemilikController {

    // FXML dari struktur utama
    @FXML private VBox sidebar;
    @FXML private BorderPane mainContentPane;
    
    // FXML untuk header dan tombol sidebar
    @FXML private Label headerTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardButton;
    @FXML private Button laporanButton;
    @FXML private Button keuanganButton;
    @FXML private Button tugasButton;
    @FXML private Button logoutButton;

    private UserModel currentUser;

    /**
     * Method initialize() akan otomatis dipanggil oleh JavaFX saat FXML dimuat.
     * Kita atur halaman default yang akan tampil pertama kali di sini.
     */
    @FXML 
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            handleDashboardMenuClick(null);
        });
    }

    public void initData(UserModel user) {
        this.currentUser = user;
        updateUserInfo();
    }
    
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getUsername());
            userRoleLabel.setText(currentUser.getRole());
        }
    }

    /**
     * Method ini dipanggil saat tombol 'Tugas' di sidebar diklik.
     */
    @FXML 
    void handleTugasMenuClick(ActionEvent event) {
        // Mengubah judul header
        headerTitleLabel.setText("Pengawasan Tugas");
        // Memuat file TugasPemilikView.fxml ke area konten utama
        loadPage("/moomoo/apps/view/TugasPemilikView.fxml");
        // Mengatur tombol 'Tugas' agar aktif secara visual
        setActiveButton(tugasButton);
    }

    /**
     * Method ini dipanggil saat tombol 'Dashboard' di sidebar diklik.
     */
    @FXML 
    void handleDashboardMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Dashboard");
        // Menampilkan konten placeholder
        mainContentPane.setCenter(new VBox(new Label("Konten Dashboard Pemilik akan Tampil di Sini")));
        setActiveButton(dashboardButton);
    }
    
    /**
     * Method ini dipanggil saat tombol 'Laporan' di sidebar diklik.
     */
    @FXML 
    void handleLaporanMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Laporan");
        // Menampilkan konten placeholder
        loadPage("/moomoo/apps/view/LaporanPemilikView.fxml");
        setActiveButton(laporanButton);
    }

    /**
     * Method ini dipanggil saat tombol 'Keuangan' di sidebar diklik.
     */
    @FXML 
    void handleKeuanganMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Keuangan");
        // Menampilkan konten placeholder
        loadPage("/moomoo/apps/view/KeuanganPemilikView.fxml"); 
        setActiveButton(keuanganButton);
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
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

    /**
     * Method helper untuk memuat file FXML ke dalam BorderPane utama.
     * @param fxmlPath Path menuju file FXML yang akan dimuat.
     */
    private void loadPage(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                showErrorPage("Error: Tidak dapat menemukan file FXML di -> " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node page = loader.load();

            Object controller = loader.getController();
            if (controller instanceof UserAwareController) {
                ((UserAwareController) controller).initData(currentUser);
            }

            mainContentPane.setCenter(page);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorPage("Error memuat halaman: " + fxmlPath + ". Cek konsol untuk detail.");
        }
    }

    private void showErrorPage(String message) {
        VBox errorBox = new VBox(new Label(message));
        errorBox.setStyle("-fx-alignment: center; -fx-font-size: 16px; -fx-text-fill: red;");
        mainContentPane.setCenter(errorBox);
    }
    
    /**
     * Method helper untuk memberikan style pada tombol sidebar yang sedang aktif.
     * @param activeButton Tombol yang baru saja diklik.
     */
    private void setActiveButton(Button activeButton) {
        if (sidebar != null) {
            // Hapus style 'aktif' dari semua tombol
            sidebar.getChildren().filtered(node -> node instanceof Button).forEach(node -> node.getStyleClass().remove("sidebar-button-active"));
            
            // Tambahkan style 'aktif' ke tombol yang baru diklik
            if (activeButton != null) {
                activeButton.getStyleClass().add("sidebar-button-active");
            }
        }
    }
}
