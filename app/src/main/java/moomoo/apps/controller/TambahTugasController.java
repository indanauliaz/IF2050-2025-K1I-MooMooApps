package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.model.TaskModel;
import moomoo.apps.utils.DatabaseManager;
import moomoo.apps.utils.ValidationUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class TambahTugasController implements Initializable {

    @FXML private TextField namaTugasField;
    @FXML private TextArea deskripsiArea;
    @FXML private ComboBox<EmployeeModel> karyawanComboBox;
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField waktuField;
    @FXML private ComboBox<String> prioritasComboBox;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button simpanButton;
    @FXML private Button cancelButton;

    private TaskModel newTask = null;
    private boolean isSaved = false;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup ComboBox Prioritas dan Status
        prioritasComboBox.setItems(FXCollections.observableArrayList("Rendah", "Normal", "Tinggi"));
        prioritasComboBox.setValue("Normal");
        statusComboBox.setItems(FXCollections.observableArrayList("Belum Dimulai", "Sedang Dikerjakan", "Tertunda"));
        statusComboBox.setValue("Belum Dimulai");
        
        // Mengatur bagaimana nama karyawan ditampilkan di ComboBox
        karyawanComboBox.setConverter(new StringConverter<EmployeeModel>() {
            @Override
            public String toString(EmployeeModel employee) {
                return employee == null ? "" : employee.getNamaLengkap();
            }
            @Override
            public EmployeeModel fromString(String string) {
                // Tidak perlu implementasi untuk kasus ini
                return null;
            }
        });

        // Set tanggal default
        tanggalPicker.setValue(LocalDate.now());
    }
    
    /**
     * Method untuk menerima daftar karyawan dari controller pemanggil (HRManagementController).
     * @param karyawanList Daftar karyawan yang akan ditampilkan di ComboBox.
     */
    public void setKaryawanList(ObservableList<EmployeeModel> karyawanList) {
        karyawanComboBox.setItems(karyawanList);
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        String namaTugas = namaTugasField.getText();
        LocalDate tanggal = tanggalPicker.getValue();
        EmployeeModel selectedEmployee = karyawanComboBox.getSelectionModel().getSelectedItem();
        String waktuStr = waktuField.getText();

        if (!ValidationUtils.validateNewTaskInput(namaTugas, tanggal, selectedEmployee, waktuStr)) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap isi semua kolom wajib (Nama Tugas, Karyawan, Tanggal) dan pastikan format waktu adalah HH:MM jika diisi.");
            return;
        }
        String deskripsi = deskripsiArea.getText();
        String prioritas = prioritasComboBox.getValue();
        String status = statusComboBox.getValue();
        LocalTime waktu = (waktuStr != null && !waktuStr.isEmpty()) ? LocalTime.parse(waktuStr, TIME_FORMATTER) : null;
        
        Integer employeeId = selectedEmployee.getId();
        String namaKaryawan = selectedEmployee.getNamaLengkap();
        String departemenKaryawan = selectedEmployee.getDepartemen();

        this.newTask = new TaskModel(0, namaTugas, deskripsi, employeeId, namaKaryawan, departemenKaryawan, tanggal, waktu, prioritas, status, null);
        
        if (DatabaseManager.insertTask(this.newTask)) {
            this.isSaved = true;
            closeStage(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan tugas ke database.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        this.newTask = null;
        this.isSaved = false;
        closeStage(event);
    }
    
    // Getter untuk hasil
    public boolean isTaskSaved() { 
        return isSaved; 
    }
    
    public TaskModel getNewTask() { 
        return newTask; 
    }

    private void closeStage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
