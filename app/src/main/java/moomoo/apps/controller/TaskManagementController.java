package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.model.TaskModel;
import moomoo.apps.model.UserModel; // Jika diperlukan untuk hak akses Manajer
import moomoo.apps.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaskManagementController implements moomoo.apps.interfaces.UserAwareController {

    @FXML private VBox akanDilakukanVBox;
    @FXML private VBox sedangDikerjakanVBox;
    @FXML private VBox selesaiVBox;
    @FXML private Label countAkanDilakukanLabel;
    @FXML private Label countSedangDikerjakanLabel;
    @FXML private Label countSelesaiLabel;
    @FXML private DatePicker taskFilterDatePicker;


    private ObservableList<TaskModel> allTasks = FXCollections.observableArrayList();
    private ObservableList<EmployeeModel> allEmployees = FXCollections.observableArrayList();

    private UserModel currentUser; // Jika diperlukan untuk hak akses Manajer

    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER_INPUT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
        // Logika tambahan jika diperlukan berdasarkan user
    }

    public void initialize() {
        taskFilterDatePicker.setValue(LocalDate.now()); // Filter default hari ini
        loadEmployees(); // Muat karyawan untuk ComboBox di dialog
        loadTasksFromDB(); // Muat semua tugas dan distribusikan
    }

    private void loadEmployees() {
        allEmployees.clear();
        String sql = "SELECT id, nama_lengkap, posisi FROM employees ORDER BY nama_lengkap";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                allEmployees.add(new EmployeeModel(
                        rs.getInt("id"),
                        rs.getString("nama_lengkap"),
                        rs.getString("posisi")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data karyawan.");
        }
    }


    private void loadTasksFromDB() {
        allTasks.clear();
        LocalDate filterDate = taskFilterDatePicker.getValue();
        // Pastikan semua kolom yang dibutuhkan TaskModel ada di SELECT
        String sql = "SELECT id, nama_tugas, deskripsi_tugas, employee_id, tanggal_tugas, " +
                    "waktu_tugas, prioritas, status, tanggal_selesai FROM tasks"; // Tambahkan tanggal_selesai

        List<Object> params = new ArrayList<>(); // Gunakan List<Object> untuk parameter
        if (filterDate != null) {
            sql += " WHERE tanggal_tugas = ?";
            params.add(DB_DATE_FORMATTER.format(filterDate));
        }
        // Sesuaikan urutan ORDER BY jika diperlukan
        sql += " ORDER BY CASE prioritas WHEN 'Tinggi' THEN 1 WHEN 'Normal' THEN 2 WHEN 'Rendah' THEN 3 ELSE 4 END, waktu_tugas ASC NULLS LAST, id DESC";


        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i)); // Gunakan setObject
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int employeeIdValue = rs.getInt("employee_id");
                Integer empId = rs.wasNull() ? null : employeeIdValue;
                String namaKaryawan = null;
                if (empId != null) {
                    namaKaryawan = getEmployeeNameById(empId);
                }

                LocalTime waktu = null;
                String waktuStr = rs.getString("waktu_tugas");
                if (waktuStr != null && !waktuStr.isEmpty()) {
                    try {
                        waktu = LocalTime.parse(waktuStr, TIME_FORMATTER_INPUT);
                    } catch (DateTimeParseException e) {
                        System.err.println("Format waktu salah untuk tugas ID " + rs.getInt("id") + ": " + waktuStr);
                    }
                }

                LocalDate tanggalSelesai = null;
                String tanggalSelesaiStr = rs.getString("tanggal_selesai");
                if (tanggalSelesaiStr != null && !tanggalSelesaiStr.isEmpty()){
                    try {
                        tanggalSelesai = LocalDate.parse(tanggalSelesaiStr, DB_DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        System.err.println("Format tanggal selesai salah untuk tugas ID " + rs.getInt("id") + ": " + tanggalSelesaiStr);
                    }
                }

                // Gunakan konstruktor TaskModel yang lengkap
                TaskModel task = new TaskModel(
                        rs.getInt("id"), // Ambil ID
                        rs.getString("nama_tugas"),
                        rs.getString("deskripsi_tugas"),
                        empId,
                        namaKaryawan,
                        LocalDate.parse(rs.getString("tanggal_tugas"), DB_DATE_FORMATTER),
                        waktu,
                        rs.getString("prioritas"),
                        rs.getString("status"),
                        tanggalSelesai // Tambahkan tanggal_selesai
                        // Jika TaskModel Anda tidak memiliki catatanManajer di konstruktor ini, tidak apa-apa
                );
                allTasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat tugas: " + e.getMessage());
        }
        distributeTasksToColumns();
    }

    private void distributeTasksToColumns() {
        akanDilakukanVBox.getChildren().clear();
        sedangDikerjakanVBox.getChildren().clear();
        selesaiVBox.getChildren().clear();

        int countAkan = 0, countSedang = 0, countSelesai = 0;

        for (TaskModel task : allTasks) {
            Node taskCard = createTaskCard(task);
            switch (task.getStatus()) {
                case "Akan Dilakukan":
                    akanDilakukanVBox.getChildren().add(taskCard);
                    countAkan++;
                    break;
                case "Sedang Dikerjakan":
                    sedangDikerjakanVBox.getChildren().add(taskCard);
                    countSedang++;
                    break;
                case "Selesai":
                case "Selesai Tepat Waktu":
                case "Selesai Terlambat":
                    selesaiVBox.getChildren().add(taskCard);
                    countSelesai++;
                    break;
                // Kasus "Ditunda" bisa punya kolom sendiri atau masuk "Akan Dilakukan"
                default: // Termasuk "Baru", "Ditunda"
                     akanDilakukanVBox.getChildren().add(taskCard);
                     countAkan++;
                     break;
            }
        }
        countAkanDilakukanLabel.setText(String.valueOf(countAkan));
        countSedangDikerjakanLabel.setText(String.valueOf(countSedang));
        countSelesaiLabel.setText(String.valueOf(countSelesai));
    }

    private Node createTaskCard(TaskModel task) {
        VBox card = new VBox(5);
        card.getStyleClass().add("task-card");
        if (task.getStatus().startsWith("Selesai")) {
            card.getStyleClass().add("task-card-done");
        }

        HBox header = new HBox();
        header.getStyleClass().add("task-card-header");
        Label taskNameLabel = new Label(task.getNamaTugas());
        taskNameLabel.getStyleClass().add("task-name");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priorityLabel = new Label(task.getPrioritas() != null ? task.getPrioritas() : "Normal");
        priorityLabel.getStyleClass().add("task-priority-badge");
        if (task.getPrioritas() != null) {
            switch (task.getPrioritas()) {
                case "Tinggi": priorityLabel.getStyleClass().add("task-priority-tinggi"); break;
                case "Normal": priorityLabel.getStyleClass().add("task-priority-normal"); break;
                case "Rendah": priorityLabel.getStyleClass().add("task-priority-rendah"); break;
            }
        } else {
            priorityLabel.getStyleClass().add("task-priority-normal");
        }
        header.getChildren().addAll(taskNameLabel, spacer, priorityLabel);

        Label descriptionLabel = new Label(task.getDeskripsiTugas() != null ? task.getDeskripsiTugas() : "Tidak ada deskripsi.");
        descriptionLabel.getStyleClass().add("task-description");

        HBox footer = new HBox();
        footer.getStyleClass().add("task-footer");
        SVGPath timeIcon = new SVGPath();
        timeIcon.setContent("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z");
        timeIcon.getStyleClass().add("task-footer-icon");
        Label timeLabel = new Label(task.getWaktuTugasFormatted());
        timeLabel.getStyleClass().add("task-time");
        
        Label assignedToLabel = new Label(task.getNamaKaryawanPenanggungJawab() != null ? " -> " + task.getNamaKaryawanPenanggungJawab() : " -> Umum");
        assignedToLabel.getStyleClass().add("task-assigned-to");

        footer.getChildren().addAll(timeIcon, timeLabel, assignedToLabel);
        
        HBox actions = new HBox(5);
        actions.getStyleClass().add("task-actions");
        
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> handleOpenEditTaskDialog(task));

        if (!task.getStatus().startsWith("Selesai")) {
            Button startButton = new Button("Kerjakan");
            if (task.getStatus().equals("Sedang Dikerjakan")) {
                startButton.setText("Selesaikan");
                startButton.getStyleClass().add("button-selesai");
                startButton.setOnAction(e -> updateTaskStatus(task, "Selesai"));
            } else { // Akan Dilakukan, Ditunda, Baru
                 startButton.setOnAction(e -> updateTaskStatus(task, "Sedang Dikerjakan"));
            }
            actions.getChildren().add(startButton);
        } else {
            SVGPath doneIcon = new SVGPath();
            doneIcon.setContent("M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"); // Centang
            doneIcon.getStyleClass().add("task-status-icon");
            actions.getChildren().add(doneIcon);
        }

        Button tundaButton = new Button("Tunda");
        if (!task.getStatus().equals("Ditunda") && !task.getStatus().startsWith("Selesai")) {
            tundaButton.setOnAction(e -> updateTaskStatus(task, "Ditunda"));
             actions.getChildren().add(tundaButton);
        }
        
        Button deleteButton = new Button("Hapus");
        deleteButton.setOnAction(e -> deleteTask(task));

        actions.getChildren().addAll(editButton, deleteButton);
        
        HBox footerActions = new HBox(footer, new Region(), actions);
        HBox.setHgrow(footerActions.getChildren().get(1), Priority.ALWAYS);


        card.getChildren().addAll(header, descriptionLabel, footerActions);
        return card;
    }
    
    private void updateTaskStatus(TaskModel task, String newStatus) {
        task.setStatus(newStatus);
        if (newStatus.startsWith("Selesai")) {
            task.setTanggalSelesai(LocalDate.now());
        } else {
            task.setTanggalSelesai(null); 
        }
        
        String sql = "UPDATE tasks SET status = ?, tanggal_selesai = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getStatus());
            pstmt.setString(2, task.getTanggalSelesai() != null ? DB_DATE_FORMATTER.format(task.getTanggalSelesai()) : null);
            pstmt.setInt(3, task.getId());
            pstmt.executeUpdate();
            loadTasksFromDB(); 
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Update Gagal", "Gagal mengupdate status tugas.");
        }
    }

    private void deleteTask(TaskModel task) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus tugas: " + task.getNamaTugas() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM tasks WHERE id = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, task.getId());
                    pstmt.executeUpdate();
                    loadTasksFromDB();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Hapus Gagal", "Gagal menghapus tugas.");
                }
            }
        });
    }


    @FXML
    private void handleOpenNewTaskDialog() {
        showTaskDialog(null); // null untuk tugas baru
    }
    
    private void handleOpenEditTaskDialog(TaskModel taskToEdit) {
        showTaskDialog(taskToEdit); // taskToEdit untuk mode edit
    }

    private void showTaskDialog(TaskModel task) {
        Dialog<TaskModel> dialog = new Dialog<>();
        dialog.setTitle(task == null ? "Tambah Tugas Baru" : "Edit Tugas");
        dialog.setHeaderText(task == null ? "Isi detail tugas baru." : "Ubah detail tugas.");

        ButtonType saveButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField namaTugasField = new TextField();
        namaTugasField.setPromptText("Nama Tugas (cth: Pemerahan Pagi)");
        TextArea deskripsiArea = new TextArea();
        deskripsiArea.setPromptText("Deskripsi detail tugas");
        ComboBox<EmployeeModel> employeeComboBox = new ComboBox<>(allEmployees);
        employeeComboBox.setPromptText("Pilih Karyawan (Opsional)");
        DatePicker tanggalTugasPicker = new DatePicker(LocalDate.now());
        
        // Input Waktu (HH:mm)
        TextField waktuField = new TextField();
        waktuField.setPromptText("HH:MM (cth: 07:30)");

        ComboBox<String> prioritasComboBox = new ComboBox<>(FXCollections.observableArrayList("Rendah", "Normal", "Tinggi"));
        prioritasComboBox.setValue("Normal");
        ComboBox<String> statusComboBox = new ComboBox<>(FXCollections.observableArrayList("Akan Dilakukan", "Sedang Dikerjakan", "Selesai", "Ditunda"));
        statusComboBox.setValue("Akan Dilakukan");


        if (task != null) { // Mode Edit
            namaTugasField.setText(task.getNamaTugas());
            deskripsiArea.setText(task.getDeskripsiTugas());
            if (task.getEmployeeId() != null) {
                allEmployees.stream()
                    .filter(emp -> emp.getId() == task.getEmployeeId())
                    .findFirst()
                    .ifPresent(employeeComboBox::setValue);
            }
            tanggalTugasPicker.setValue(task.getTanggalTugas());
            if (task.getWaktuTugas() != null) {
                waktuField.setText(task.getWaktuTugas().format(TIME_FORMATTER_INPUT));
            }
            prioritasComboBox.setValue(task.getPrioritas());
            statusComboBox.setValue(task.getStatus());
        }

        grid.add(new Label("Nama Tugas:"), 0, 0); grid.add(namaTugasField, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1);  grid.add(deskripsiArea, 1, 1);
        grid.add(new Label("Ditugaskan ke:"), 0, 2); grid.add(employeeComboBox, 1, 2);
        grid.add(new Label("Tanggal:"), 0, 3); grid.add(tanggalTugasPicker, 1, 3);
        grid.add(new Label("Waktu (HH:MM):"), 0, 4); grid.add(waktuField, 1, 4);
        grid.add(new Label("Prioritas:"), 0, 5); grid.add(prioritasComboBox, 1, 5);
        grid.add(new Label("Status Awal:"), 0, 6); grid.add(statusComboBox, 1, 6);
        
        // Validasi agar tombol Simpan hanya aktif jika nama tugas diisi
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        namaTugasField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });


        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalTime waktuTugas = null;
                if (!waktuField.getText().trim().isEmpty()) {
                    try {
                        waktuTugas = LocalTime.parse(waktuField.getText().trim(), TIME_FORMATTER_INPUT);
                    } catch (DateTimeParseException e) {
                        showAlert(Alert.AlertType.ERROR, "Format Waktu Salah", "Gunakan format HH:MM untuk waktu (misal 08:30). Waktu akan diabaikan.");
                    }
                }
                
                EmployeeModel selectedEmp = employeeComboBox.getValue();
                Integer empId = selectedEmp != null ? selectedEmp.getId() : null;
                String empName = selectedEmp != null ? selectedEmp.getNamaLengkap() : null;

                if (task == null) { // Buat baru
                    return new TaskModel(namaTugasField.getText(), deskripsiArea.getText(), empId, empName,
                                         tanggalTugasPicker.getValue(), waktuTugas, prioritasComboBox.getValue(), statusComboBox.getValue());
                } else { // Edit
                    task.setNamaTugas(namaTugasField.getText());
                    task.setDeskripsiTugas(deskripsiArea.getText());
                    task.setEmployeeId(empId);
                    task.setNamaKaryawanPenanggungJawab(empName);
                    task.setTanggalTugas(tanggalTugasPicker.getValue());
                    task.setWaktuTugas(waktuTugas);
                    task.setPrioritas(prioritasComboBox.getValue());
                    task.setStatus(statusComboBox.getValue()); 
                    return task;
                }
            }
            return null;
        });

        Optional<TaskModel> result = dialog.showAndWait();
        result.ifPresent(editedTask -> {
            String sql;
            boolean isNewTask = editedTask.getId() == 0; 
            if (task != null && task.getId() != 0) isNewTask = false; 

            if (isNewTask) {
                sql = "INSERT INTO tasks (nama_tugas, deskripsi_tugas, employee_id, tanggal_tugas, waktu_tugas, prioritas, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE tasks SET nama_tugas = ?, deskripsi_tugas = ?, employee_id = ?, tanggal_tugas = ?, waktu_tugas = ?, prioritas = ?, status = ? WHERE id = ?";
            }

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, editedTask.getNamaTugas());
                pstmt.setString(2, editedTask.getDeskripsiTugas());
                if (editedTask.getEmployeeId() != null) {
                    pstmt.setInt(3, editedTask.getEmployeeId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, DB_DATE_FORMATTER.format(editedTask.getTanggalTugas()));
                pstmt.setString(5, editedTask.getWaktuTugas() != null ? editedTask.getWaktuTugas().format(TIME_FORMATTER_INPUT) : null);
                pstmt.setString(6, editedTask.getPrioritas());
                pstmt.setString(7, editedTask.getStatus());
                if (!isNewTask) {
                    pstmt.setInt(8, editedTask.getId());
                }
                
                pstmt.executeUpdate();
                if (isNewTask) {
                     try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            editedTask.setId(generatedKeys.getInt(1));
                        }
                    }
                }
                loadTasksFromDB(); 
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan tugas.");
            }
        });
    }
    
    @FXML
    private void handleFilterByDate(ActionEvent event) {
        loadTasksFromDB();
    }

    @FXML
    private void handleFilterToday(ActionEvent event) {
        taskFilterDatePicker.setValue(LocalDate.now());
        loadTasksFromDB();
    }
    
    @FXML
    private void handleFilterAllTasks(ActionEvent event) {
        taskFilterDatePicker.setValue(null); 
        loadTasksFromDB();
    }

    private String getEmployeeNameById(int employeeId) {
        if (allEmployees == null) { 
            return "ID: " + employeeId; 
        }
        for (EmployeeModel emp : allEmployees) {
            if (emp.getId() == employeeId) {
                return emp.getNamaLengkap();
            }
        }
        return "Karyawan Tidak Ditemukan (ID: " + employeeId + ")"; 
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}