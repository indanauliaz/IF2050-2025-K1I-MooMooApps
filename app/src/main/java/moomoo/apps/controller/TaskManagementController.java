package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell; // Untuk kustomisasi sel aksi
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox; // Untuk tombol aksi di tabel
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback; // Untuk cell factory
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.model.TaskModel;
import moomoo.apps.model.AttendanceRecordModel; // Model baru

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class TaskManagementController {

    //<editor-fold defaultstate="collapsed" desc="FXML Injections untuk Tugas (Kanban)">
    @FXML private DatePicker tanggalFilterPicker;
    @FXML private Button hariIniButton; // Untuk filter tugas
    @FXML private Button semuaTugasButton; // Untuk filter tugas
    @FXML private Button tambahTugasBaruButton;
    @FXML private TabPane mainTabPane;
    @FXML private Tab tugasTab;
    @FXML private VBox akanDilakukanListVBox;
    @FXML private VBox sedangDikerjakanListVBox;
    @FXML private VBox selesaiListVBox;
    @FXML private ScrollPane akanDilakukanScrollPane;
    @FXML private ScrollPane sedangDikerjakanScrollPane;
    @FXML private ScrollPane selesaiScrollPane;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="FXML Injections untuk Kehadiran">
    @FXML private Tab kehadiranTab;
    @FXML private Label tanggalKehadiranLabel;
    @FXML private Button tambahPresensiButton;
    @FXML private TableView<AttendanceRecordModel> tabelKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, String> kolomKaryawanKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, String> kolomStatusKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, String> kolomMasukKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, String> kolomKeluarKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, String> kolomCatatanKehadiran;
    @FXML private TableColumn<AttendanceRecordModel, Void> kolomAksiKehadiran; // Kolom untuk tombol
    @FXML private Label summaryKehadiranLabel;
    @FXML private Button hariSebelumnyaButton;
    @FXML private Button hariBerikutnyaButton;
    //</editor-fold>

    private ObservableList<TaskModel> semuaTugasList = FXCollections.observableArrayList();
    private ObservableList<AttendanceRecordModel> daftarKehadiranList = FXCollections.observableArrayList();
    private ObservableList<EmployeeModel> daftarKaryawanUntukPresensi = FXCollections.observableArrayList(); // Untuk modal presensi

    private LocalDate tanggalKehadiranSaatIni;
    private DateTimeFormatter tanggalDisplayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private Random random = new Random();


    @FXML
    public void initialize() {
        // --- Inisialisasi untuk Tab Tugas ---
        tanggalFilterPicker.setValue(LocalDate.now());
        muatSemuaTugas();

        // --- Inisialisasi untuk Tab Kehadiran ---
        tanggalKehadiranSaatIni = LocalDate.now(); // Default ke hari ini
        updateLabelTanggalKehadiran();
        setupKolomTabelKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
        
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == kehadiranTab) {
                System.out.println("Tab Kehadiran dipilih. Mereload data kehadiran...");
                muatDataKehadiran(tanggalKehadiranSaatIni);
            } else if (newTab == tugasTab) {
                System.out.println("Tab Tugas dipilih. Mereload data tugas...");
                muatSemuaTugas(); 
            }
        });
        
        muatDummyKaryawanUntukPresensi(); 
    }
    
    // =================================================================================
    // Metode untuk Manajemen Tugas (Kanban Board)
    // =================================================================================
    private void muatSemuaTugas() {
        semuaTugasList.clear();
        // Menggunakan konstruktor TaskModel yang menerima String untuk waktu
        // TaskModel(int id, String namaTugas, String deskripsi, String ditugaskanKe, 
        //           LocalDate tanggal, String waktuStr, String prioritas, String status)
        semuaTugasList.addAll(
            new TaskModel(1, "Pemerahan Soro Pagi", "Selesaikan pemerahan sore untuk semua sapi", "Inas", LocalDate.now(), "07:00", "Tinggi", "Akan Dilakukan"),
            new TaskModel(2, "Laporan Harian Produksi", "Buat dan submit laporan harian terkait produksi susu", "Admin", LocalDate.now(), "09:00", "Normal", "Akan Dilakukan"),
            new TaskModel(3, "Pemeriksaan Kesehatan Ternak", "Periksa status kesehatan semua ternak terjadwal", "Dr. Hewan", LocalDate.now(), "10:00", "Tinggi", "Sedang Dikerjakan"),
            new TaskModel(4, "Distribusi Pakan Tambahan", "Distribusikan pakan tambahan ke semua ternak", "Budi", LocalDate.now(), "11:00", "Normal", "Sedang Dikerjakan"),
            new TaskModel(5, "Pembersihan Kandang Utama", "Selesaikan pembersihan pagi untuk semua kandang utama", "Tim Kandang", LocalDate.now().minusDays(1), "08:00", "Normal", "Selesai")
        );
        tampilkanTugasDiKanban();
    }

    private void tampilkanTugasDiKanban() {
        akanDilakukanListVBox.getChildren().clear();
        sedangDikerjakanListVBox.getChildren().clear();
        selesaiListVBox.getChildren().clear();
        for (TaskModel tugas : semuaTugasList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TaskCard.fxml"));
                VBox taskCardNode = loader.load();
                TaskCardController cardController = loader.getController();
                cardController.setData(tugas, this);
                switch (tugas.getStatus()) {
                    case "Akan Dilakukan": akanDilakukanListVBox.getChildren().add(taskCardNode); break;
                    case "Sedang Dikerjakan": sedangDikerjakanListVBox.getChildren().add(taskCardNode); break;
                    case "Selesai": selesaiListVBox.getChildren().add(taskCardNode); break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void refreshKanbanBoard() { muatSemuaTugas(); }

    @FXML private void handleTambahTugasBaru(ActionEvent event) { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahTugasView.fxml")); 
            Parent root = loader.load();
            TambahTugasController tambahTugasController = loader.getController();
            // Jika kamu punya daftar karyawan yang ingin di-pass ke modal:
            // ObservableList<EmployeeModel> semuaKaryawan = ... ; // Ambil dari DatabaseManager
            // tambahTugasController.initKaryawanList(semuaKaryawan);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahTugasBaruButton.getScene().getWindow()); 
            modalStage.setTitle("Tambah Tugas Baru");
            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.showAndWait(); 
            TaskModel tugasBaruDariModal = tambahTugasController.getNewTask();
            if (tugasBaruDariModal != null) {
                // TODO: Simpan tugasBaruDariModal ke database
                System.out.println("Tugas baru disimpan (dummy): " + tugasBaruDariModal.getNamaTugas());
                refreshKanbanBoard();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleFilterHariIniTugas(ActionEvent event) { /* TODO */ System.out.println("Filter Tugas Hari Ini"); }
    @FXML private void handleFilterSemuaTugas(ActionEvent event) { /* TODO */ System.out.println("Filter Semua Tugas"); }


    // =================================================================================
    // Metode untuk Manajemen Kehadiran
    // =================================================================================
    private void updateLabelTanggalKehadiran() {
        tanggalKehadiranLabel.setText(tanggalKehadiranSaatIni.format(tanggalDisplayFormatter));
    }

    private void setupKolomTabelKehadiran() {
        kolomKaryawanKehadiran.setCellValueFactory(cellData -> cellData.getValue().namaKaryawanProperty());
        kolomStatusKehadiran.setCellValueFactory(cellData -> cellData.getValue().statusKehadiranProperty());
        kolomMasukKehadiran.setCellValueFactory(cellData -> cellData.getValue().waktuMasukFormattedProperty());
        kolomKeluarKehadiran.setCellValueFactory(cellData -> cellData.getValue().waktuKeluarFormattedProperty());
        kolomCatatanKehadiran.setCellValueFactory(cellData -> cellData.getValue().catatanProperty());

        kolomStatusKehadiran.setCellFactory(column -> new TableCell<AttendanceRecordModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-hadir", "status-terlambat", "status-absen", "status-izin");
                    switch (item.toLowerCase()) {
                        case "hadir": getStyleClass().add("status-hadir"); break;
                        case "terlambat": getStyleClass().add("status-terlambat"); break;
                        case "absen": getStyleClass().add("status-absen"); break;
                        case "izin": getStyleClass().add("status-izin"); break;
                        default: setStyle(""); break;
                    }
                }
            }
        });

        Callback<TableColumn<AttendanceRecordModel, Void>, TableCell<AttendanceRecordModel, Void>> cellFactoryAksi = param -> {
            final TableCell<AttendanceRecordModel, Void> cell = new TableCell<>() {
                private final Button btnEdit = new Button("Edit"); // Ganti emoji dengan teks
                private final Button btnDelete = new Button("Hapus"); // Ganti emoji dengan teks
                {
                    btnEdit.getStyleClass().add("button-aksi-tabel");
                    btnDelete.getStyleClass().add("button-aksi-tabel-merah");

                    btnEdit.setOnAction((ActionEvent event) -> {
                        AttendanceRecordModel record = getTableView().getItems().get(getIndex());
                        System.out.println("Edit record: " + record.getKaryawan().getNamaLengkap());
                        handleEditPresensi(record);
                    });
                    btnDelete.setOnAction((ActionEvent event) -> {
                        AttendanceRecordModel record = getTableView().getItems().get(getIndex());
                        System.out.println("Hapus record: " + record.getKaryawan().getNamaLengkap());
                        handleHapusPresensi(record);
                    });
                }
                private final HBox pane = new HBox(5, btnEdit, btnDelete);
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
            return cell;
        };
        kolomAksiKehadiran.setCellFactory(cellFactoryAksi);
        kolomAksiKehadiran.setStyle("-fx-alignment: CENTER;");
    }
    
    private void muatDummyKaryawanUntukPresensi() {
        daftarKaryawanUntukPresensi.clear();
        daftarKaryawanUntukPresensi.add(new EmployeeModel(1, "Sam Wiryawan", "Manager Produksi"));
        daftarKaryawanUntukPresensi.add(new EmployeeModel(2, "Safira Anjani", "Supervisor Peternakan"));
        daftarKaryawanUntukPresensi.add(new EmployeeModel(3, "Salim Mahesa", "Staff Produksi"));
        daftarKaryawanUntukPresensi.add(new EmployeeModel(4, "Sandra Dewi", "Pekerja Lapangan"));
        daftarKaryawanUntukPresensi.add(new EmployeeModel(5, "San Kurniawan", "Akuntan Keuangan"));
    }

    private void muatDataKehadiran(LocalDate tanggal) {
        System.out.println("Memuat data kehadiran untuk tanggal: " + tanggal.format(tanggalDisplayFormatter));
        daftarKehadiranList.clear();
        if (daftarKaryawanUntukPresensi.isEmpty()) muatDummyKaryawanUntukPresensi();

        if (daftarKaryawanUntukPresensi.size() >= 5) { 
            daftarKehadiranList.add(new AttendanceRecordModel(daftarKaryawanUntukPresensi.get(0), tanggal, "Hadir", "07:15", "16:30", "-"));
            daftarKehadiranList.add(new AttendanceRecordModel(daftarKaryawanUntukPresensi.get(1), tanggal, "Hadir", "07:10", "16:30", "-"));
            daftarKehadiranList.add(new AttendanceRecordModel(daftarKaryawanUntukPresensi.get(2), tanggal, "Hadir", "07:05", "16:15", "-"));
            daftarKehadiranList.add(new AttendanceRecordModel(daftarKaryawanUntukPresensi.get(3), tanggal, "Terlambat", "08:45", "16:00", "Terlambat 1 jam 45 menit"));
            daftarKehadiranList.add(new AttendanceRecordModel(daftarKaryawanUntukPresensi.get(4), tanggal, "Absen", null, null, "Tidak ada keterangan"));
        } else {
            System.out.println("Tidak cukup dummy karyawan untuk membuat data kehadiran.");
        }
        if (tanggal.getDayOfMonth() % 2 == 0 && !daftarKehadiranList.isEmpty() && daftarKaryawanUntukPresensi.size() > 0) {
             daftarKehadiranList.get(0).setStatusKehadiran("Izin");
             daftarKehadiranList.get(0).setCatatan("Izin sakit");
        }
        tabelKehadiran.setItems(daftarKehadiranList);
        updateSummaryKehadiran();
    }

    private void updateSummaryKehadiran() {
        long hadirCount = daftarKehadiranList.stream().filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long terlambatCount = daftarKehadiranList.stream().filter(r -> "Terlambat".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long tidakHadirCount = daftarKehadiranList.stream().filter(r -> "Absen".equalsIgnoreCase(r.getStatusKehadiran()) || "Izin".equalsIgnoreCase(r.getStatusKehadiran())).count();
        summaryKehadiranLabel.setText(String.format("Hadir: %d | Terlambat: %d | Tidak Hadir: %d", hadirCount, terlambatCount, tidakHadirCount));
    }

    @FXML
    private void handleTambahPresensi(ActionEvent event) {
        System.out.println("Tombol Tambah Presensi diklik untuk tanggal: " + tanggalKehadiranSaatIni);
        showPlaceholderDialog("Fitur Tambah Presensi", "Fungsionalitas untuk menambah presensi manual akan ada di sini.");
    }

    @FXML
    private void handleHariSebelumnyaKehadiran(ActionEvent event) {
        tanggalKehadiranSaatIni = tanggalKehadiranSaatIni.minusDays(1);
        updateLabelTanggalKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
    }

    @FXML
    private void handleHariBerikutnyaKehadiran(ActionEvent event) {
        tanggalKehadiranSaatIni = tanggalKehadiranSaatIni.plusDays(1);
        updateLabelTanggalKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
    }
    
    private void handleEditPresensi(AttendanceRecordModel record) {
        System.out.println("Edit presensi untuk: " + record.getKaryawan().getNamaLengkap() + " pada " + record.getTanggalAbsen());
        showPlaceholderDialog("Edit Presensi: " + record.getKaryawan().getNamaLengkap(), "Fungsionalitas untuk mengedit presensi akan ada di sini.");
    }

    private void handleHapusPresensi(AttendanceRecordModel record) {
        System.out.println("Hapus presensi untuk: " + record.getKaryawan().getNamaLengkap() + " pada " + record.getTanggalAbsen());
        daftarKehadiranList.remove(record); 
        updateSummaryKehadiran();
        showPlaceholderDialog("Hapus Presensi", "Presensi untuk " + record.getKaryawan().getNamaLengkap() + " telah dihapus (dummy).");
    }
    
    private void showPlaceholderDialog(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}