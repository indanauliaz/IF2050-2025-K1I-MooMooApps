package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import moomoo.apps.model.EmployeeModel; // Asumsi kamu punya ini
import moomoo.apps.model.TaskModel;   // Kamu perlu buat model ini

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskManagementController {

    //<editor-fold defaultstate="collapsed" desc="FXML Injections dari TaskManagement.fxml">
    @FXML private DatePicker tanggalFilterPicker;
    @FXML private Button hariIniButton;
    @FXML private Button semuaTugasButton;
    @FXML private Button tambahTugasBaruButton;

    @FXML private TabPane mainTabPane;
    @FXML private Tab tugasTab;
    @FXML private Tab kehadiranTab;

    // Kolom Kanban
    @FXML private VBox akanDilakukanListVBox;
    @FXML private VBox sedangDikerjakanListVBox;
    @FXML private VBox selesaiListVBox;
    
    @FXML private ScrollPane akanDilakukanScrollPane;
    @FXML private ScrollPane sedangDikerjakanScrollPane;
    @FXML private ScrollPane selesaiScrollPane;
    //</editor-fold>

    // Untuk menyimpan daftar tugas dari model
    private ObservableList<TaskModel> semuaTugasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup awal, misalnya listener untuk tab atau filter
        tanggalFilterPicker.setValue(LocalDate.now()); // Set default tanggal filter
        
        // Muat data tugas (awal)
        muatSemuaTugas(); // Ini akan memanggil metode yang memuat data tugas dummy/asli
    }

    private void muatSemuaTugas() {
        // --- INI BAGIAN DUMMY DATA TUGAS ---
        // Nanti, ini akan mengambil data dari DatabaseManager atau service
        semuaTugasList.clear();
        // Status: "Akan Dilakukan", "Sedang Dikerjakan", "Selesai"
        // Prioritas: "Tinggi", "Normal", "Rendah"
        semuaTugasList.addAll(
            new TaskModel(1, "Pemerahan Soro Pagi", "Selesaikan pemerahan sore untuk semua sapi", "Inas", LocalDate.now(), "07:00", "Tinggi", "Akan Dilakukan"),
            new TaskModel(2, "Laporan Harian Produksi", "Buat dan submit laporan harian terkait produksi susu", "Admin", LocalDate.now(), "09:00", "Normal", "Akan Dilakukan"),
            new TaskModel(3, "Pemeriksaan Kesehatan Ternak", "Periksa status kesehatan semua ternak terjadwal", "Dr. Hewan", LocalDate.now(), "10:00", "Tinggi", "Sedang Dikerjakan"),
            new TaskModel(4, "Distribusi Pakan Tambahan", "Distribusikan pakan tambahan ke semua ternak", "Budi", LocalDate.now(), "11:00", "Normal", "Sedang Dikerjakan"),
            new TaskModel(5, "Pembersihan Kandang Utama", "Selesaikan pembersihan pagi untuk semua kandang utama", "Tim Kandang", LocalDate.now().minusDays(1), "08:00", "Normal", "Selesai")
        );
        // --- AKHIR DUMMY DATA ---

        tampilkanTugasDiKanban();
    }

    private void tampilkanTugasDiKanban() {
        // Bersihkan kolom sebelum mengisi ulang
        akanDilakukanListVBox.getChildren().clear();
        sedangDikerjakanListVBox.getChildren().clear();
        selesaiListVBox.getChildren().clear();

        for (TaskModel tugas : semuaTugasList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TaskCard.fxml")); // Pastikan path benar
                VBox taskCardNode = loader.load();
                
                TaskCardController cardController = loader.getController();
                cardController.setData(tugas, this); // Kirim data tugas dan referensi TaskManagementController

                switch (tugas.getStatus()) {
                    case "Akan Dilakukan":
                        akanDilakukanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Sedang Dikerjakan":
                        sedangDikerjakanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Selesai":
                        selesaiListVBox.getChildren().add(taskCardNode);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error memuat TaskCard.fxml: " + e.getMessage());
            }
        }
    }
    
    // Dipanggil dari TaskCardController saat status tugas berubah
    public void refreshKanbanBoard() {
        muatSemuaTugas(); // Untuk sementara, muat ulang semua. Nanti bisa lebih optimal.
    }

    @FXML
    private void handleTambahTugasBaru(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahTugasView.fxml")); // Pastikan path benar
            Parent root = loader.load();

            TambahTugasController tambahTugasController = loader.getController();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahTugasBaruButton.getScene().getWindow()); // Set owner
            modalStage.setTitle("Tambah Tugas Baru");

            Scene scene = new Scene(root);
            modalStage.setScene(scene);

            modalStage.showAndWait(); // Tampilkan dan tunggu

            TaskModel tugasBaruDariModal = tambahTugasController.getNewTask();
            if (tugasBaruDariModal != null) {
                System.out.println("Tugas baru diterima dari modal: " + tugasBaruDariModal.getNamaTugas());
                semuaTugasList.add(tugasBaruDariModal); // Jika semuaTugasList adalah list utama
                tampilkanTugasDiKanban(); // Atau refreshKanbanBoard() yang lebih komprehensif
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Implementasi handler untuk filter tanggal, hariIniButton, semuaTugasButton
    @FXML private void handleFilterTanggal(ActionEvent event) { System.out.println("Filter Tanggal"); muatSemuaTugas(); }
    @FXML private void handleHariIni(ActionEvent event) { System.out.println("Filter Hari Ini"); muatSemuaTugas(); }
    @FXML private void handleSemuaTugas(ActionEvent event) { System.out.println("Filter Semua Tugas"); muatSemuaTugas(); }

    // TODO: Listener untuk Tab Kehadiran
}