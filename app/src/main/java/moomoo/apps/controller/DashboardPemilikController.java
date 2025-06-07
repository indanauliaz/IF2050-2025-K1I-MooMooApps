package moomoo.apps.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;

public class DashboardPemilikController {

    // FXML dari struktur utama
    @FXML private VBox sidebar;
    @FXML private BorderPane mainContentPane;
    
    // FXML untuk header dan tombol sidebar
    @FXML private Label headerTitleLabel;
    @FXML private Button dashboardButton;
    @FXML private Button laporanButton;
    @FXML private Button keuanganButton;
    @FXML private Button tugasButton;

    /**
     * Method initialize() akan otomatis dipanggil oleh JavaFX saat FXML dimuat.
     * Kita atur halaman default yang akan tampil pertama kali di sini.
     */
    @FXML 
    public void initialize() {
        // Secara default, muat halaman tugas saat pertama kali dibuka
        handleTugasMenuClick(null);
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
        mainContentPane.setCenter(new VBox(new Label("Konten Laporan Pemilik akan Tampil di Sini")));
        setActiveButton(laporanButton);
    }

    /**
     * Method ini dipanggil saat tombol 'Keuangan' di sidebar diklik.
     */
    @FXML 
    void handleKeuanganMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Keuangan");
        // Menampilkan konten placeholder
        mainContentPane.setCenter(new VBox(new Label("Konten Keuangan Pemilik akan Tampil di Sini")));
        setActiveButton(keuanganButton);
    }

    /**
     * Method helper untuk memuat file FXML ke dalam BorderPane utama.
     * @param fxmlPath Path menuju file FXML yang akan dimuat.
     */
    private void loadPage(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("Error: Tidak dapat menemukan file FXML di -> " + fxmlPath);
                mainContentPane.setCenter(new VBox(new Label("Error: File " + fxmlPath + " tidak ditemukan.")));
                return;
            }
            Node page = FXMLLoader.load(resourceUrl);
            mainContentPane.setCenter(page);
        } catch (IOException e) {
            e.printStackTrace();
            mainContentPane.setCenter(new VBox(new Label("Error memuat halaman. Cek konsol untuk detail.")));
        }
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
