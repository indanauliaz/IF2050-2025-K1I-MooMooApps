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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        // Listener to enable/disable time fields based on status
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
        tanggalDatePicker.setValue(date); // Set date from HRManagementController

        if (recordToEdit != null) {
            isEditMode = true;
            currentRecord = recordToEdit;
            dialogTitleLabel.setText("Edit Presensi");
            karyawanComboBox.setValue(recordToEdit.getKaryawan());
            karyawanComboBox.setDisable(true); // Usually cannot change employee when editing
            tanggalDatePicker.setDisable(true); // Usually cannot change date when editing
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
            currentRecord = null; // Will be created on save
            dialogTitleLabel.setText("Tambah Presensi Baru");
            karyawanComboBox.setDisable(false);
            tanggalDatePicker.setDisable(false); // Or true if date is fixed by main controller
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        EmployeeModel selectedEmployee = karyawanComboBox.getValue();
        LocalDate attendanceDate = tanggalDatePicker.getValue();
        String status = statusComboBox.getValue();
        String notes = catatanArea.getText();

        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Karyawan harus dipilih.");
            return;
        }
        if (attendanceDate == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Tanggal harus diisi.");
            return;
        }
        if (status == null || status.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Status kehadiran harus dipilih.");
            return;
        }

        LocalTime clockInTime = null;
        LocalTime clockOutTime = null;

        boolean timesRequired = "Hadir".equalsIgnoreCase(status) || "Terlambat".equalsIgnoreCase(status);

        if (timesRequired) {
            if (waktuMasukField.getText() == null || waktuMasukField.getText().trim().isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Input Error", "Waktu masuk harus diisi untuk status " + status);
                 return;
            }
            try {
                clockInTime = LocalTime.parse(waktuMasukField.getText(), TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Format waktu masuk salah (HH:MM).");
                return;
            }

            if (waktuKeluarField.getText() != null && !waktuKeluarField.getText().trim().isEmpty()) {
                try {
                    clockOutTime = LocalTime.parse(waktuKeluarField.getText(), TIME_FORMATTER);
                    if (clockInTime != null && clockOutTime.isBefore(clockInTime)) {
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Waktu keluar tidak boleh sebelum waktu masuk.");
                        return;
                    }
                } catch (DateTimeParseException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Format waktu keluar salah (HH:MM).");
                    return;
                }
            }
        }


        if (isEditMode && currentRecord != null) {
            // Update existing record
            currentRecord.setKaryawan(selectedEmployee); // Should not change typically, but for completeness
            currentRecord.setTanggalAbsen(attendanceDate); // Should not change typically
            currentRecord.setStatusKehadiran(status);
            currentRecord.setWaktuMasuk(clockInTime);
            currentRecord.setWaktuKeluar(clockOutTime);
            currentRecord.setCatatan(notes);
        } else {
            // Create new record
            // ID will be set by DatabaseManager upon insertion for a new record
            currentRecord = new AttendanceRecordModel(0, selectedEmployee, attendanceDate, status, clockInTime, clockOutTime, notes);
        }
        closeStage(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        currentRecord = null; // Ensure no record is returned on cancel
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