package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label; // Jika perlu menampilkan pesan error
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import moomoo.apps.model.EmployeeModel; // Untuk mengisi ComboBox Karyawan
import moomoo.apps.model.TaskModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList; // Untuk dummy data karyawan
import java.util.List;     // Untuk dummy data karyawan

public class TambahTugasController {

    @FXML private TextField namaTugasField;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<EmployeeModel> karyawanComboBox; // Kita akan tampilkan EmployeeModel, tapi simpan ID & Nama
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField waktuField;
    @FXML private ComboBox<String> prioritasComboBox;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Button simpanButton;
    @FXML private Button cancelButton;

    // Untuk menyimpan tugas yang baru dibuat, agar bisa diambil oleh TaskManagementController
    private TaskModel newTask = null; 
    private ObservableList<EmployeeModel> daftarKaryawan = FXCollections.observableArrayList(); // Untuk ComboBox

    // Formatter untuk parsing dan validasi waktu
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    @FXML
    public void initialize() {
        // Isi ComboBox Prioritas
        prioritasComboBox.setItems(FXCollections.observableArrayList("Rendah", "Normal", "Tinggi"));
        prioritasComboBox.setValue("Normal"); // Default

        // Isi ComboBox Status Awal
        statusComboBox.setItems(FXCollections.observableArrayList("Akan Dilakukan", "Sedang Dikerjakan"));
        statusComboBox.setValue("Akan Dilakukan"); // Default

        // Isi ComboBox Karyawan (dengan dummy data untuk sekarang)
        // Idealnya, ini di-pass dari TaskManagementController atau diambil dari database
        muatDummyKaryawan(); 
        karyawanComboBox.setItems(daftarKaryawan);
        // Jika EmployeeModel punya toString() yang bagus, akan tampil nama di ComboBox.
        // Atau gunakan setCellFactory dan setButtonCell untuk kustomisasi tampilan.

        // Set default tanggal ke hari ini
        tanggalPicker.setValue(LocalDate.now());
    }

    // Metode untuk di-pass list karyawan dari controller utama (opsional)
    public void initKaryawanList(ObservableList<EmployeeModel> karyawanList) {
        this.daftarKaryawan.setAll(karyawanList);
        karyawanComboBox.setItems(this.daftarKaryawan);
    }
    
    private void muatDummyKaryawan() {
        // Ganti ini dengan pengambilan data karyawan asli
        daftarKaryawan.clear();
        daftarKaryawan.add(new EmployeeModel(1, "Inas", "Peternak")); // ID, Nama, Posisi (Posisi bisa disesuaikan)
        daftarKaryawan.add(new EmployeeModel(2, "Budi", "Staff Produksi"));
        daftarKaryawan.add(new EmployeeModel(3, "Admin", "Administrator"));
        daftarKaryawan.add(new EmployeeModel(0, "Belum Ditugaskan", "-")); // Opsi untuk tidak menugaskan
        karyawanComboBox.setItems(daftarKaryawan);
        // Set default ke "Belum Ditugaskan" atau biarkan kosong jika promptText sudah cukup
        // karyawanComboBox.setValue(daftarKaryawan.get(daftarKaryawan.size() - 1)); 
    }


    @FXML
    private void handleSimpan(ActionEvent event) {
        String namaTugas = namaTugasField.getText();
        String deskripsi = deskripsiArea.getText();
        EmployeeModel selectedEmployee = karyawanComboBox.getSelectionModel().getSelectedItem();
        LocalDate tanggal = tanggalPicker.getValue();
        String waktuStr = waktuField.getText();
        String prioritas = prioritasComboBox.getValue();
        String status = statusComboBox.getValue();

        // --- Validasi Input Sederhana ---
        if (namaTugas == null || namaTugas.trim().isEmpty()) {
            System.err.println("Nama tugas tidak boleh kosong!");
            // TODO: Tampilkan alert ke pengguna
            return;
        }
        if (tanggal == null) {
            System.err.println("Tanggal tidak boleh kosong!");
            // TODO: Tampilkan alert ke pengguna
            return;
        }

        LocalTime waktu = null;
        if (waktuStr != null && !waktuStr.trim().isEmpty()) {
            try {
                waktu = LocalTime.parse(waktuStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("Format waktu salah! Gunakan HH:MM (Contoh: 07:30)");
                // TODO: Tampilkan alert ke pengguna
                return;
            }
        }
        // --- Akhir Validasi ---

        Integer employeeId = null;
        String namaKaryawan = null;
        if (selectedEmployee != null && selectedEmployee.getId() != 0) { // Anggap ID 0 adalah "Belum Ditugaskan"
            employeeId = selectedEmployee.getId();
            namaKaryawan = selectedEmployee.getNamaLengkap();
        }


        // Buat objek TaskModel baru
        // ID tugas akan di-generate oleh database, jadi kita bisa pakai konstruktor tanpa ID dulu,
        // atau set ID ke 0 atau nilai sementara.
        this.newTask = new TaskModel(
                0, // ID sementara, akan di-set oleh database
                namaTugas,
                deskripsi,
                employeeId,
                namaKaryawan,
                tanggal,
                waktu,
                prioritas,
                status,
                null // tanggalSelesai null untuk tugas baru
        );

        System.out.println("Tugas baru dibuat (dummy save): " + newTask.getNamaTugas());
        // TODO: Implementasi penyimpanan ke database melalui DatabaseManager atau service layer
        // Misalnya: DatabaseManager.getInstance().simpanTugas(this.newTask);

        closeStage(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        this.newTask = null; // Pastikan tidak ada tugas yang dikembalikan jika di-cancel
        closeStage(event);
    }

    // Metode untuk mendapatkan tugas yang baru dibuat (dipanggil oleh TaskManagementController)
    public TaskModel getNewTask() {
        return newTask;
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}