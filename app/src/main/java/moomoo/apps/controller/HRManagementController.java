package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import moomoo.apps.utils.DatabaseManager;
import moomoo.apps.model.AttendanceRecordModel; // Model baru

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


public class HRManagementController {

    //<editor-fold defaultstate="collapsed" desc="FXML Injections untuk Tugas">
    @FXML private DatePicker tanggalFilterPicker;
    @FXML private Button hariIniButton; 
    @FXML private Button semuaTugasButton; 
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
    private ObservableList<EmployeeModel> semuaKaryawanList = FXCollections.observableArrayList();

    private LocalDate tanggalKehadiranSaatIni;
    private DateTimeFormatter tanggalDisplayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private boolean isSemuaTugasMode = false;


    @FXML
    public void initialize() {
        // Inisialisasi Database
        DatabaseManager.initializeDatabase();

        // Setup untuk Tab Tugas
        tanggalFilterPicker.setValue(LocalDate.now());
        muatDaftarKaryawan();
        muatSemuaTugasDariDB();

        // Setup untuk Tab Kehadiran
        tanggalKehadiranSaatIni = LocalDate.now(); 
        updateLabelTanggalKehadiran();
        setupKolomTabelKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
        
        // Listener untuk perpindahan Tab
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == kehadiranTab) {
                System.out.println("Tab Kehadiran dipilih. Mereload data kehadiran...");
                muatDataKehadiran(tanggalKehadiranSaatIni);
            } else if (newTab == tugasTab) {
                System.out.println("Tab Tugas dipilih. Mereload data tugas...");
                refreshKanbanBoard();
            }
        });
    }

    private void muatDaftarKaryawan() {
        semuaKaryawanList.clear();
        List<EmployeeModel> employeesFromDb = DatabaseManager.getAllEmployees();
        if (employeesFromDb != null) {
            semuaKaryawanList.addAll(employeesFromDb);
        }
         System.out.println("Loaded " + semuaKaryawanList.size() + " employees for tasks.");
    }

    private void muatSemuaTugasDariDB() {
        semuaTugasList.clear();
        LocalDate filterDate = tanggalFilterPicker.getValue();

        List<TaskModel> tasksFromDb = DatabaseManager.getAllTasks();
    
        List<TaskModel> filteredTasks;
        if (filterDate != null && !isSemuaTugasMode) { 
             filteredTasks = tasksFromDb.stream()
                    .filter(t -> t.getTanggalTugas() != null && t.getTanggalTugas().equals(filterDate))
                    .collect(Collectors.toList());
             System.out.println("Memuat tugas dari DB untuk tanggal: " + filterDate + ", ditemukan: " + filteredTasks.size());
        } else {
            filteredTasks = tasksFromDb;
            System.out.println("Memuat semua tugas dari DB, ditemukan: " + filteredTasks.size());
        }
        semuaTugasList.addAll(filteredTasks);
        tampilkanTugasDiKanban();
    }
    
    private void tampilkanTugasDiKanban() {
        akanDilakukanListVBox.getChildren().clear();
        sedangDikerjakanListVBox.getChildren().clear();
        selesaiListVBox.getChildren().clear();

        if (semuaTugasList.isEmpty()) {
            System.out.println("Tidak ada tugas untuk ditampilkan di Kanban.");
        }

        for (TaskModel tugas : semuaTugasList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TaskCard.fxml"));
                VBox taskCardNode = loader.load();
                TaskCardController cardController = loader.getController();
                cardController.setData(tugas, this);

                switch (tugas.getStatus()) {
                    case "Belum Dimulai": // Disesuaikan dengan status baru
                    case "Akan Dilakukan": // Menjaga kompatibilitas
                        akanDilakukanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Sedang Dikerjakan":
                        sedangDikerjakanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Selesai":
                        selesaiListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Tertunda": // Status baru
                        // Mungkin bisa dimasukkan ke 'Akan Dilakukan' atau kolom baru
                        akanDilakukanListVBox.getChildren().add(taskCardNode);
                        break;
                    default:
                        System.err.println("Status tugas tidak dikenal: " + tugas.getStatus());
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshKanbanBoard() {
        System.out.println("Refreshing Kanban board...");
        muatSemuaTugasDariDB();
    }

    @FXML
    private void handleTambahTugasBaru(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahTugasView.fxml"));
            Parent root = loader.load();
            TambahTugasController tambahTugasController = loader.getController();
            
            // Mengirim daftar karyawan ke modal
            tambahTugasController.setKaryawanList(this.semuaKaryawanList);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahTugasBaruButton.getScene().getWindow());
            modalStage.setTitle("Tambah Tugas Baru");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            // Cek apakah tugas berhasil disimpan dari dalam modal
            if (tambahTugasController.isTaskSaved()) {
                System.out.println("Modal ditutup, tugas berhasil disimpan. Me-refresh Kanban...");
                refreshKanbanBoard();
            } else {
                System.out.println("Modal ditutup tanpa menyimpan tugas baru.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholderDialog("Error", "Tidak bisa membuka form tambah tugas.");
        }
    }

    @FXML 
    private void handleFilterHariIniTugas(ActionEvent event) { 
        System.out.println("Filter Tugas Hari Ini");
        tanggalFilterPicker.setValue(LocalDate.now());
        isSemuaTugasMode = false;
        refreshKanbanBoard();
    }

    @FXML 
    private void handleFilterSemuaTugas(ActionEvent event) { 
        System.out.println("Filter Semua Tugas");
        isSemuaTugasMode = true;
        refreshKanbanBoard(); 
    }

    @FXML
    private void onTanggalFilterChanged(ActionEvent event) {
        System.out.println("Tanggal filter diubah: " + tanggalFilterPicker.getValue());
        isSemuaTugasMode = false;
        refreshKanbanBoard();
    }

    // =================================================================================
    // Metode untuk Manajemen Kehadiran (tidak ada perubahan)
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

        kolomStatusKehadiran.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (!empty && item != null) {
                    getStyleClass().removeAll("status-hadir", "status-terlambat", "status-absen", "status-izin", "status-sakit");
                    switch (item.toLowerCase()) {
                        case "hadir": getStyleClass().add("status-hadir"); break;
                        case "terlambat": getStyleClass().add("status-terlambat"); break;
                        case "absen": getStyleClass().add("status-absen"); break;
                        case "izin": getStyleClass().add("status-izin"); break;
                        case "sakit": getStyleClass().add("status-sakit"); break;
                    }
                } else {
                    getStyleClass().removeAll("status-hadir", "status-terlambat", "status-absen", "status-izin", "status-sakit");
                }
            }
        });

        Callback<TableColumn<AttendanceRecordModel, Void>, TableCell<AttendanceRecordModel, Void>> cellFactoryAksi = param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Hapus");
            {
                btnEdit.getStyleClass().add("button-aksi-tabel");
                btnDelete.getStyleClass().add("button-aksi-tabel-merah");
                btnEdit.setOnAction(event -> handleEditPresensi(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> handleHapusPresensi(getTableView().getItems().get(getIndex())));
            }
            private final HBox pane = new HBox(5, btnEdit, btnDelete);
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        };
        kolomAksiKehadiran.setCellFactory(cellFactoryAksi);
        kolomAksiKehadiran.setStyle("-fx-alignment: CENTER;");
    }
    
    private void muatDataKehadiran(LocalDate tanggal) {
        daftarKehadiranList.setAll(DatabaseManager.getAttendanceRecordsByDate(tanggal));
        tabelKehadiran.setItems(daftarKehadiranList);
        updateSummaryKehadiran();
    }

    private void updateSummaryKehadiran() {
        long hadir = daftarKehadiranList.stream().filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long terlambat = daftarKehadiranList.stream().filter(r -> "Terlambat".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long tidakHadir = daftarKehadiranList.stream().filter(r -> "Absen".equalsIgnoreCase(r.getStatusKehadiran()) || "Izin".equalsIgnoreCase(r.getStatusKehadiran()) || "Sakit".equalsIgnoreCase(r.getStatusKehadiran())).count();
        summaryKehadiranLabel.setText(String.format("Hadir: %d | Terlambat: %d | Tidak Hadir: %d", hadir, terlambat, tidakHadir));
    }

    @FXML
    private void handleTambahPresensi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahPresensiView.fxml"));
            Parent root = loader.load();
            TambahPresensiController controller = loader.getController();
            
            List<Integer> employeeIdsWithAttendance = daftarKehadiranList.stream().map(ar -> ar.getKaryawan().getId()).collect(Collectors.toList());
            ObservableList<EmployeeModel> employeesForDialog = semuaKaryawanList.stream().filter(emp -> !employeeIdsWithAttendance.contains(emp.getId())).collect(Collectors.toCollection(FXCollections::observableArrayList));

            if(employeesForDialog.isEmpty() && !semuaKaryawanList.isEmpty()){
                showPlaceholderDialog("Info", "Semua karyawan sudah memiliki catatan presensi untuk tanggal ini.");
            }
            controller.setDialogData(employeesForDialog, tanggalKehadiranSaatIni, null);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahPresensiButton.getScene().getWindow());
            modalStage.setTitle("Tambah Presensi Baru");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            AttendanceRecordModel newRecord = controller.getAttendanceRecord();
            if (newRecord != null) {
                if (DatabaseManager.addAttendanceRecord(newRecord)) {
                    System.out.println("Presensi baru disimpan untuk: " + newRecord.getKaryawan().getNamaLengkap());
                    muatDataKehadiran(tanggalKehadiranSaatIni); 
                } else {
                    showPlaceholderDialog("Error Simpan", "Gagal menyimpan data presensi ke database.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholderDialog("Error", "Tidak bisa membuka form tambah presensi.");
        }
    }
    
    @FXML private void handleHariSebelumnyaKehadiran(ActionEvent event) {
        tanggalKehadiranSaatIni = tanggalKehadiranSaatIni.minusDays(1);
        updateLabelTanggalKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
    }

    @FXML private void handleHariBerikutnyaKehadiran(ActionEvent event) {
        tanggalKehadiranSaatIni = tanggalKehadiranSaatIni.plusDays(1);
        updateLabelTanggalKehadiran();
        muatDataKehadiran(tanggalKehadiranSaatIni);
    }
    
    private void handleEditPresensi(AttendanceRecordModel recordToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahPresensiView.fxml"));
            Parent root = loader.load();
            TambahPresensiController controller = loader.getController();
            controller.setDialogData(FXCollections.observableArrayList(recordToEdit.getKaryawan()), recordToEdit.getTanggalAbsen(), recordToEdit);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabelKehadiran.getScene().getWindow());
            modalStage.setTitle("Edit Presensi");
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            AttendanceRecordModel updatedRecord = controller.getAttendanceRecord();
            if (updatedRecord != null && DatabaseManager.updateAttendanceRecord(updatedRecord)) {
                muatDataKehadiran(tanggalKehadiranSaatIni);
            } else if (updatedRecord != null) {
                showPlaceholderDialog("Error Update", "Gagal memperbarui data presensi di database.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleHapusPresensi(AttendanceRecordModel recordToDelete) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Apakah Anda yakin ingin menghapus data presensi untuk " + recordToDelete.getKaryawan().getNamaLengkap() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText(null);
        confirmAlert.initOwner(tabelKehadiran.getScene().getWindow());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (DatabaseManager.deleteAttendanceRecord(recordToDelete.getId())) {
                    muatDataKehadiran(tanggalKehadiranSaatIni);
                } else {
                    showPlaceholderDialog("Error Hapus", "Gagal menghapus data presensi dari database.");
                }
            }
        });
    }
    
    public void showPlaceholderDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
