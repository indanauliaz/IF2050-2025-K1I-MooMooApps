package moomoo.apps.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import moomoo.apps.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SdmModel {

    private static SdmModel instance;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ObservableList<EmployeeModel> allEmployees;
    private List<TaskModel> allTasks;

    private SdmModel() {
        this.allEmployees = FXCollections.observableArrayList();
        this.allTasks = new ArrayList<>();
        loadAllEmployeesFromDB();
        loadAllTasksFromDB();
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

    public void loadAllTasksFromDB() {
        this.allTasks = DatabaseManager.getAllTasks(); 
        if (this.allTasks != null) {
            System.out.println("SdmModel: " + this.allTasks.size() + " tugas berhasil dimuat.");
        } else {
            System.err.println("SdmModel: Gagal memuat tugas (null).");
            this.allTasks = new ArrayList<>();
        }
    }


    public List<TaskModel> getTasksByDateRange(LocalDate startDate, LocalDate endDate) {
        if (allTasks == null) {
            return Collections.emptyList();
        }
        
        return allTasks.stream()
            .filter(task -> {
                LocalDate taskDate = task.getTanggalTugas();
                return taskDate != null && !taskDate.isBefore(startDate) && !taskDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
    }

    public double getKinerjaGabunganPeriodeIni() {
        return getKinerjaGabungan(YearMonth.now());
    }

    public double getKinerjaGabungan(YearMonth month) {

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();


        List<EmployeeModel> semuaKaryawan = this.getAllEmployees();
        if (semuaKaryawan == null || semuaKaryawan.isEmpty()) {
            return 0.0;
        }
        
        List<AttendanceRecordModel> catatanKehadiran = this.getAttendanceRecordsByDateRange(startDate, endDate);
        List<TaskModel> catatanTugas = this.getTasksByDateRange(startDate, endDate);
        

        long totalHariKerja = ChronoUnit.DAYS.between(startDate, endDate) + 1;


        Map<Integer, Long> kehadiranPerKaryawan = catatanKehadiran.stream()
                .filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran()) || "Terlambat".equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.groupingBy(r -> r.getKaryawan().getId(), Collectors.counting()));
                
        Map<Integer, List<TaskModel>> tugasPerKaryawan = catatanTugas.stream()
                .filter(t -> t.getEmployeeId() != null)
                .collect(Collectors.groupingBy(TaskModel::getEmployeeId));

        List<Double> skorProduktivitasIndividu = new ArrayList<>();
        for (EmployeeModel emp : semuaKaryawan) {
            long jumlahHadir = kehadiranPerKaryawan.getOrDefault(emp.getId(), 0L);
            double skorKehadiran = (totalHariKerja > 0) ? ((double) jumlahHadir / totalHariKerja) : 0.0;
            
            List<TaskModel> tugasKaryawanIni = tugasPerKaryawan.getOrDefault(emp.getId(), List.of());
            long totalTugas = tugasKaryawanIni.size();
            long tugasSelesai = tugasKaryawanIni.stream().filter(t -> "Selesai".equalsIgnoreCase(t.getStatus())).count();
            double skorTugas = (totalTugas > 0) ? ((double) tugasSelesai / totalTugas) : 1.0; 

            double produktivitas = (skorKehadiran * 0.6) + (skorTugas * 0.4);
            skorProduktivitasIndividu.add(produktivitas);
        }


        return skorProduktivitasIndividu.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
