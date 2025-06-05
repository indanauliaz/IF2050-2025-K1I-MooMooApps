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
    private TaskModel newTask = null; 
    private ObservableList<EmployeeModel> daftarKaryawan = FXCollections.observableArrayList(); // Untuk ComboBox


    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    @FXML
    public void initialize() {
        prioritasComboBox.setItems(FXCollections.observableArrayList("Rendah", "Normal", "Tinggi"));
        prioritasComboBox.setValue("Normal"); // Default

        statusComboBox.setItems(FXCollections.observableArrayList("Akan Dilakukan", "Sedang Dikerjakan"));
        statusComboBox.setValue("Akan Dilakukan"); // Default

        karyawanComboBox.setItems(daftarKaryawan);

        tanggalPicker.setValue(LocalDate.now());
    }

    public void initKaryawanList(ObservableList<EmployeeModel> karyawanList) {
        this.daftarKaryawan.add(null);
        this.daftarKaryawan.setAll(karyawanList);
        karyawanComboBox.setItems(this.daftarKaryawan);
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

            return;
        }
        if (tanggal == null) {
            System.err.println("Tanggal tidak boleh kosong!");

            return;
        }

        LocalTime waktu = null;
        if (waktuStr != null && !waktuStr.trim().isEmpty()) {
            try {
                waktu = LocalTime.parse(waktuStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.err.println("Format waktu salah! Gunakan HH:MM (Contoh: 07:30)");
    
                return;
            }
        }


        Integer employeeId = null;
        String namaKaryawan = null;
        if (selectedEmployee != null && selectedEmployee.getId() != 0) { 
            employeeId = selectedEmployee.getId();
            namaKaryawan = selectedEmployee.getNamaLengkap();
        }

        this.newTask = new TaskModel(
                0, 
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