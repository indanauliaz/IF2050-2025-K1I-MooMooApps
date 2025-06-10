package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import moomoo.apps.model.AttendanceRecordModel;
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class TambahPresensiController {

    @FXML private Label dialogTitleLabel;
    @FXML private ComboBox<EmployeeModel> karyawanComboBox;
    @FXML private DatePicker tanggalDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField waktuMasukField;
    @FXML private TextField waktuKeluarField;
    @FXML private TextArea catatanArea;
    @FXML private Button simpanButton;
    @FXML private Button cancelButton;

    private AttendanceRecordModel currentRecord;
    private boolean isEditMode = false;
    private ObservableList<EmployeeModel> employeeList = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final List<String> statusOptions = Arrays.asList("Hadir", "Terlambat", "Izin", "Sakit", "Absen");


    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(statusOptions));
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean enableTimes = "Hadir".equalsIgnoreCase(newVal) || "Terlambat".equalsIgnoreCase(newVal);
                waktuMasukField.setDisable(!enableTimes);
                waktuKeluarField.setDisable(!enableTimes);
                if (!enableTimes) {
                    waktuMasukField.clear();
                    waktuKeluarField.clear();
                }
            }
        });
    }

    public void setDialogData(ObservableList<EmployeeModel> employees, LocalDate date, AttendanceRecordModel recordToEdit) {
        this.employeeList.setAll(employees);
        karyawanComboBox.setItems(this.employeeList);
        tanggalDatePicker.setValue(date); 

        if (recordToEdit != null) {
            isEditMode = true;
            currentRecord = recordToEdit;
            dialogTitleLabel.setText("Edit Presensi");
            karyawanComboBox.setValue(recordToEdit.getKaryawan());
            karyawanComboBox.setDisable(true); 
            tanggalDatePicker.setDisable(true); 
            statusComboBox.setValue(recordToEdit.getStatusKehadiran());
            if (recordToEdit.getWaktuMasuk() != null) {
                waktuMasukField.setText(recordToEdit.getWaktuMasuk().format(TIME_FORMATTER));
            }
            if (recordToEdit.getWaktuKeluar() != null) {
                waktuKeluarField.setText(recordToEdit.getWaktuKeluar().format(TIME_FORMATTER));
            }
            catatanArea.setText(recordToEdit.getCatatan());
        } else {
            isEditMode = false;
            currentRecord = null; 
            dialogTitleLabel.setText("Tambah Presensi Baru");
            karyawanComboBox.setDisable(false);
            tanggalDatePicker.setDisable(false); 
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        EmployeeModel selectedEmployee = karyawanComboBox.getValue();
        LocalDate attendanceDate = tanggalDatePicker.getValue();
        String status = statusComboBox.getValue();
        String waktuMasukStr = waktuMasukField.getText();
        String waktuKeluarStr = waktuKeluarField.getText();
        String notes = catatanArea.getText();

        if (!ValidationUtils.validateNewAttendanceInput(selectedEmployee, attendanceDate, status, waktuMasukStr, waktuKeluarStr)) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Pastikan semua kolom wajib diisi. Untuk status 'Hadir' atau 'Terlambat', waktu masuk wajib diisi dengan format HH:MM. Waktu keluar juga tidak boleh sebelum waktu masuk.");
            return;
        }

        LocalTime clockInTime = (waktuMasukStr != null && !waktuMasukStr.isEmpty()) ? LocalTime.parse(waktuMasukStr, TIME_FORMATTER) : null;
        LocalTime clockOutTime = (waktuKeluarStr != null && !waktuKeluarStr.isEmpty()) ? LocalTime.parse(waktuKeluarStr, TIME_FORMATTER) : null;
        
        if (isEditMode && currentRecord != null) {
            currentRecord.setStatusKehadiran(status);
            currentRecord.setWaktuMasuk(clockInTime);
            currentRecord.setWaktuKeluar(clockOutTime);
            currentRecord.setCatatan(notes);
        } else {
            currentRecord = new AttendanceRecordModel(0, selectedEmployee, attendanceDate, status, clockInTime, clockOutTime, notes);
        }
        closeStage(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        currentRecord = null; 
        closeStage(event);
    }

    public AttendanceRecordModel getAttendanceRecord() {
        return currentRecord;
    }

    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(simpanButton.getScene().getWindow());
        alert.showAndWait();
    }
}