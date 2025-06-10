package moomoo.apps.utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.ProductionModel;
import moomoo.apps.model.SdmModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Class ini dibuat sebagai Singleton untuk memastikan hanya ada satu service polling yang berjalan.
public final class PollingService {

    private static PollingService instance;
    private Timeline pollingTimeline;
    private long lastCheckedTimestamp = 0;

    private PollingService() {
        // Private constructor untuk mencegah instansiasi dari luar.
    }

    // Metode untuk mendapatkan satu-satunya instance dari service ini.
    public static synchronized PollingService getInstance() {
        if (instance == null) {
            instance = new PollingService();
        }
        return instance;
    }

    public void start() {
        // Hentikan dulu jika sudah ada yang berjalan untuk mencegah duplikasi
        if (pollingTimeline != null && pollingTimeline.getStatus() == Timeline.Status.RUNNING) {
            return;
        }
        
        // Inisialisasi timestamp saat memulai
        this.lastCheckedTimestamp = System.currentTimeMillis();

        pollingTimeline = new Timeline(
            new KeyFrame(Duration.seconds(3), event -> checkForUpdates()) // Cek setiap 3 detik
        );
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
        System.out.println("Real-time polling service telah dimulai.");
    }

    public void stop() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
            System.out.println("Real-time polling service telah dihentikan.");
        }
    }

    private void checkForUpdates() {
        String sql = "SELECT value FROM app_state WHERE key = 'last_update'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                long latestTimestamp = Long.parseLong(rs.getString("value"));
                
                if (latestTimestamp > this.lastCheckedTimestamp) {
                    System.out.println("Perubahan baru terdeteksi di database! Memuat ulang semua model data...");
                    this.lastCheckedTimestamp = latestTimestamp;
                    
                    // Gunakan Platform.runLater untuk memastikan update UI aman
                    Platform.runLater(() -> {
                        // Perintahkan setiap model Singleton untuk me-refresh dirinya sendiri
                        FinanceModel.getInstance().loadAllTransactionsFromDB();
                        ProductionModel.getInstance().loadProductionDataFromDB();
                        SdmModel.getInstance().loadAllEmployeesFromDB();
                        // Jika ada model lain, panggil method refresh-nya di sini.
                        
                        System.out.println("Semua model data telah diperbarui dari database.");
                    });
                }
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error saat memeriksa pembaruan: " + e.getMessage());
        }
    }
}
