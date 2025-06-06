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
    private ObservableList<EmployeeModel> daftarKaryawanUntukPresensi = FXCollections.observableArrayList(); // Untuk modal presensi
     private ObservableList<EmployeeModel> semuaKaryawanList = FXCollections.observableArrayList(); // For task assignment

    private LocalDate tanggalKehadiranSaatIni;
    private DateTimeFormatter tanggalDisplayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    //  


    @FXML
    public void initialize() {

        DatabaseManager.initializeDatabase();

        tanggalFilterPicker.setValue(LocalDate.now());
        muatDaftarKaryawan();
        muatSemuaTugasDariDB();

        tanggalKehadiranSaatIni = LocalDate.now(); 
        updateLabelTanggalKehadiran();
        setupKolomTabelKehadiran();
        // muatDummyKaryawanUntukPresensi();
        muatDataKehadiran(tanggalKehadiranSaatIni);
        
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == kehadiranTab) {
                System.out.println("Tab Kehadiran dipilih. Mereload data kehadiran...");
                tanggalKehadiranSaatIni = LocalDate.now(); // Reset to today or keep current? For now, reset.
                updateLabelTanggalKehadiran();
                muatDataKehadiran(tanggalKehadiranSaatIni);
            } else if (newTab == tugasTab) {
                System.out.println("Tab Tugas dipilih. Mereload data tugas...");
                muatSemuaTugasDariDB(); // Corrected from muatSemuaTugas()
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
        LocalDate filterDate = tanggalFilterPicker.getValue(); // Get current filter date

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

    
    // // =================================================================================
    // // Metode untuk Manajemen Tugas (Kanban Board)
    // // =================================================================================
    // private void muatSemuaTugas() {
    //     semuaTugasList.clear();
    //     semuaTugasList.addAll(
    //         new TaskModel(1, "Pemerahan Soro Pagi", "Selesaikan pemerahan sore untuk semua sapi", "Inas", LocalDate.now(), "07:00", "Tinggi", "Akan Dilakukan"),
    //         new TaskModel(2, "Laporan Harian Produksi", "Buat dan submit laporan harian terkait produksi susu", "Admin", LocalDate.now(), "09:00", "Normal", "Akan Dilakukan"),
    //         new TaskModel(3, "Pemeriksaan Kesehatan Ternak", "Periksa status kesehatan semua ternak terjadwal", "Dr. Hewan", LocalDate.now(), "10:00", "Tinggi", "Sedang Dikerjakan"),
    //         new TaskModel(4, "Distribusi Pakan Tambahan", "Distribusikan pakan tambahan ke semua ternak", "Budi", LocalDate.now(), "11:00", "Normal", "Sedang Dikerjakan"),
    //         new TaskModel(5, "Pembersihan Kandang Utama", "Selesaikan pembersihan pagi untuk semua kandang utama", "Tim Kandang", LocalDate.now().minusDays(1), "08:00", "Normal", "Selesai")
    //     );
    //     tampilkanTugasDiKanban();
    // }

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
                    case "Akan Dilakukan":
                        akanDilakukanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Sedang Dikerjakan":
                        sedangDikerjakanListVBox.getChildren().add(taskCardNode);
                        break;
                    case "Selesai":
                        selesaiListVBox.getChildren().add(taskCardNode);
                        break;
                    default:
                        System.err.println("Status tugas tidak dikenal: " + tugas.getStatus() + " untuk tugas ID: " + tugas.getId());
                        // Optionally put in a default column or log
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Consider showing an error to the user or logging more robustly
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
            
            // Pass the loaded employee list to the modal controller
            tambahTugasController.initKaryawanList(this.semuaKaryawanList);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahTugasBaruButton.getScene().getWindow());
            modalStage.setTitle("Tambah Tugas Baru");
            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.showAndWait();

            TaskModel tugasBaruDariModal = tambahTugasController.getNewTask();
            if (tugasBaruDariModal != null) {
                boolean saved = DatabaseManager.insertTask(tugasBaruDariModal);
                if (saved) {
                    System.out.println("Tugas baru disimpan ke DB: " + tugasBaruDariModal.getNamaTugas() + " dengan ID: " + tugasBaruDariModal.getId());
                    refreshKanbanBoard(); // Reload all tasks from DB
                } else {
                    System.err.println("Gagal menyimpan tugas baru ke DB: " + tugasBaruDariModal.getNamaTugas());
                    showPlaceholderDialog("Error Simpan", "Gagal menyimpan tugas baru ke database.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholderDialog("Error", "Tidak bisa membuka form tambah tugas.");
        }
    }

     private boolean isSemuaTugasMode = false; // Helper for filter logic
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
        // tanggalFilterPicker.setValue(null); // Clear date picker or ignore it
        isSemuaTugasMode = true;
        refreshKanbanBoard(); 
    }

        @FXML
    private void onTanggalFilterChanged(ActionEvent event) {
        System.out.println("Tanggal filter diubah: " + tanggalFilterPicker.getValue());
        isSemuaTugasMode = false; // When date picker is used, disable "all tasks" mode
        refreshKanbanBoard();
    }

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
                setText(empty ? null : item);
                if (!empty && item != null) {
                    getStyleClass().removeAll("status-hadir", "status-terlambat", "status-absen", "status-izin", "status-sakit");
                    switch (item.toLowerCase()) {
                        case "hadir": getStyleClass().add("status-hadir"); break;
                        case "terlambat": getStyleClass().add("status-terlambat"); break;
                        case "absen": getStyleClass().add("status-absen"); break;
                        case "izin": getStyleClass().add("status-izin"); break;
                        case "sakit": getStyleClass().add("status-sakit"); break; // Added for completeness
                        default: setStyle(""); break;
                    }
                } else {
                    setStyle("");
                     getStyleClass().removeAll("status-hadir", "status-terlambat", "status-absen", "status-izin", "status-sakit");
                }
            }
        });

        Callback<TableColumn<AttendanceRecordModel, Void>, TableCell<AttendanceRecordModel, Void>> cellFactoryAksi = param -> {
            final TableCell<AttendanceRecordModel, Void> cell = new TableCell<>() {
                private final Button btnEdit = new Button("Edit"); 
                private final Button btnDelete = new Button("Hapus"); 
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
        List<EmployeeModel> allEmps = DatabaseManager.getAllEmployees(); // Fetch all
        if (allEmps.size() >= 5) { // Take first 5 for consistency with original dummy
            daftarKaryawanUntukPresensi.addAll(allEmps.subList(0, Math.min(allEmps.size(), 5)));
        } else {
            daftarKaryawanUntukPresensi.addAll(allEmps);
        }
    }

    private void muatDataKehadiran(LocalDate tanggal) {
        System.out.println("Memuat data kehadiran dari DB untuk tanggal: " + tanggal.format(tanggalDisplayFormatter));
        daftarKehadiranList.clear();
        List<AttendanceRecordModel> recordsFromDb = DatabaseManager.getAttendanceRecordsByDate(tanggal);
        daftarKehadiranList.addAll(recordsFromDb);
        tabelKehadiran.setItems(daftarKehadiranList);
        updateSummaryKehadiran();
        if (recordsFromDb.isEmpty()) {
            System.out.println("Tidak ada data kehadiran di DB untuk tanggal ini.");
        }
    }

    private void updateSummaryKehadiran() {
        long hadirCount = daftarKehadiranList.stream().filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long terlambatCount = daftarKehadiranList.stream().filter(r -> "Terlambat".equalsIgnoreCase(r.getStatusKehadiran())).count();
        long tidakHadirCount = daftarKehadiranList.stream().filter(r -> "Absen".equalsIgnoreCase(r.getStatusKehadiran()) || "Izin".equalsIgnoreCase(r.getStatusKehadiran())).count();
        summaryKehadiranLabel.setText(String.format("Hadir: %d | Terlambat: %d | Tidak Hadir: %d", hadirCount, terlambatCount, tidakHadirCount));
    }

    @FXML
    private void handleTambahPresensi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahPresensiView.fxml"));
            Parent root = loader.load();
            TambahPresensiController controller = loader.getController();
            
            List<Integer> employeeIdsWithAttendance = daftarKehadiranList.stream()
                                                                .map(ar -> ar.getKaryawan().getId())
                                                                .collect(Collectors.toList());
            ObservableList<EmployeeModel> employeesForDialog = FXCollections.observableArrayList(
                semuaKaryawanList.stream()
                                 .filter(emp -> !employeeIdsWithAttendance.contains(emp.getId()))
                                 .collect(Collectors.toList())
            );

            if(employeesForDialog.isEmpty() && !semuaKaryawanList.isEmpty()){
                 showPlaceholderDialog("Info", "Semua karyawan sudah memiliki catatan presensi untuk tanggal ini.");
                // return; // Option to prevent opening if all recorded
            }
             controller.setDialogData(employeesForDialog.isEmpty() ? semuaKaryawanList : employeesForDialog, tanggalKehadiranSaatIni, null);


            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tambahPresensiButton.getScene().getWindow());
            modalStage.setTitle("Tambah Presensi Baru");
            Scene scene = new Scene(root);
            modalStage.setScene(scene);
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
    
    private void handleEditPresensi(AttendanceRecordModel recordToEdit) {
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/TambahPresensiView.fxml"));
            Parent root = loader.load();
            TambahPresensiController controller = loader.getController();
            
            // Pass all employees (or just the current one if non-editable) and the record to edit
            controller.setDialogData(FXCollections.observableArrayList(recordToEdit.getKaryawan()), recordToEdit.getTanggalAbsen(), recordToEdit);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(tabelKehadiran.getScene().getWindow());
            modalStage.setTitle("Edit Presensi");
            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.showAndWait();

            AttendanceRecordModel updatedRecord = controller.getAttendanceRecord();
            if (updatedRecord != null) { 

                if (DatabaseManager.updateAttendanceRecord(updatedRecord)) {
                    System.out.println("Presensi diperbarui untuk: " + updatedRecord.getKaryawan().getNamaLengkap());
                    muatDataKehadiran(tanggalKehadiranSaatIni);
                } else {
                    showPlaceholderDialog("Error Update", "Gagal memperbarui data presensi di database.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showPlaceholderDialog("Error", "Tidak bisa membuka form edit presensi.");
        }
    }

    private void handleHapusPresensi(AttendanceRecordModel recordToDelete) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Presensi untuk " + recordToDelete.getKaryawan().getNamaLengkap() + "?");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus data presensi ini?");
        confirmAlert.initOwner(tabelKehadiran.getScene().getWindow());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (DatabaseManager.deleteAttendanceRecord(recordToDelete.getId())) {
                System.out.println("Presensi dihapus untuk: " + recordToDelete.getKaryawan().getNamaLengkap());
                muatDataKehadiran(tanggalKehadiranSaatIni); // Refresh table
            } else {
                showPlaceholderDialog("Error Hapus", "Gagal menghapus data presensi dari database.");
            }
        }
    }
    
    public void showPlaceholderDialog(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}