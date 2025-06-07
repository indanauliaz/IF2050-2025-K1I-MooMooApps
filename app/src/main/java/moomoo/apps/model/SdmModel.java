package moomoo.apps.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import moomoo.apps.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SdmModel {

    private static SdmModel instance;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ObservableList<EmployeeModel> allEmployees;

    private SdmModel() {
        this.allEmployees = FXCollections.observableArrayList();
        loadAllEmployeesFromDB();
    }

    public static synchronized SdmModel getInstance() {
        if (instance == null) {
            instance = new SdmModel();
        }
        return instance;
    }

    public ObservableList<EmployeeModel> getAllEmployees() {
        return allEmployees;
    }

    public void loadAllEmployeesFromDB() {
        allEmployees.clear();
        String sql = "SELECT id, nama_lengkap, posisi, tim FROM employees ORDER BY nama_lengkap";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                allEmployees.add(new EmployeeModel(
                    rs.getInt("id"),
                    rs.getString("nama_lengkap"),
                    rs.getString("posisi"),
                    rs.getString("tim")
                ));
            }
            System.out.println("SdmModel: " + allEmployees.size() + " karyawan berhasil dimuat.");
        } catch (SQLException e) {
            System.err.println("Error memuat data karyawan di SdmModel: " + e.getMessage());
        }
    }

    public List<AttendanceRecordModel> getAttendanceRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecordModel> records = new ArrayList<>();
        String sql = "SELECT ar.id, ar.employee_id, e.nama_lengkap, e.posisi, ar.attendance_date, ar.status " +
                     "FROM attendance_records ar JOIN employees e ON ar.employee_id = e.id " +
                     "WHERE ar.attendance_date BETWEEN ? AND ? ORDER BY ar.attendance_date, e.nama_lengkap";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate.format(DB_DATE_FORMATTER));
            pstmt.setString(2, endDate.format(DB_DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EmployeeModel employee = new EmployeeModel(rs.getInt("employee_id"), rs.getString("nama_lengkap"), rs.getString("posisi"));
                records.add(new AttendanceRecordModel(
                    rs.getInt("id"), employee,
                    LocalDate.parse(rs.getString("attendance_date"), DB_DATE_FORMATTER),
                    rs.getString("status"), null, null, null
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error mengambil data kehadiran: " + e.getMessage());
        }
        return records;
    }
    
    /**
     * Metode baru untuk mengambil data tugas dalam rentang tanggal tertentu.
     * @param startDate Tanggal mulai.
     * @param endDate Tanggal akhir.
     * @return List dari TaskModel.
     */
    public List<TaskModel> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TaskModel> allTasks = DatabaseManager.getAllTasks();
        return allTasks.stream()
            .filter(task -> {
                LocalDate taskDate = task.getTanggalTugas();
                return taskDate != null && !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }
}
