package moomoo.apps.utils;

import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import moomoo.apps.model.AttendanceRecordModel;
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.model.TaskModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    
    private static final String DATABASE_URL = "jdbc:sqlite:moomoo_apps.db";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {
        // SQL untuk membuat tabel Users
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                                  + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                  + "username TEXT NOT NULL UNIQUE,"
                                  + "email TEXT NOT NULL UNIQUE,"
                                  + "password_hash TEXT NOT NULL," 
                                  + "role TEXT NOT NULL"
                                  + ");";

     
        String createTransactionsTableSQL = "CREATE TABLE IF NOT EXISTS transactions ("
                                          + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                          + "transaction_type TEXT NOT NULL," 
                                          + "description TEXT NOT NULL,"
                                          + "amount REAL NOT NULL,"
                                          + "category TEXT NOT NULL,"
                                          + "date TEXT NOT NULL," 
                                          + "payment_method TEXT NOT NULL,"
                                          + "notes TEXT,"
                                          + "user_id INTEGER, " 
                                          + "FOREIGN KEY (user_id) REFERENCES users(id)"
                                          + ");";

        
        String createProductionsTableSQL = "CREATE TABLE IF NOT EXISTS productions ("
                                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                        + "kategori TEXT NOT NULL,"
                                        + "jumlah REAL NOT NULL,"
                                        + "satuan TEXT NOT NULL,"
                                        + "tanggal TEXT NOT NULL," 
                                        + "lokasi TEXT,"
                                        + "kualitas TEXT,"
                                        + "catatan TEXT"
                                        + ");";

        String createEmployeesTableSQL = "CREATE TABLE IF NOT EXISTS employees ("
                                       + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                       + "nama_lengkap TEXT NOT NULL,"
                                       + "posisi TEXT,"
                                       + "tim TEXT"
                                       + ");";

        String createTasksTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "nama_tugas TEXT NOT NULL,"         
                                + "deskripsi_tugas TEXT,"              
                                + "employee_id INTEGER,"               
                                + "tanggal_tugas TEXT NOT NULL,"     
                                + "waktu_tugas TEXT,"                  
                                + "prioritas TEXT,"                  
                                + "status TEXT NOT NULL,"
                                + "tanggal_selesai TEXT,"           
                                + "catatan_manajer TEXT,"            
                                + "FOREIGN KEY (employee_id) REFERENCES employees(id)"
                                + ");";

        String createAttendanceRecordsTableSQL = "CREATE TABLE IF NOT EXISTS attendance_records ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "employee_id INTEGER NOT NULL,"
                + "attendance_date TEXT NOT NULL," 
                + "status TEXT NOT NULL," 
                + "clock_in_time TEXT,"     
                + "clock_out_time TEXT,"    
                + "notes TEXT,"
                + "FOREIGN KEY (employee_id) REFERENCES employees(id)"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            

            stmt.execute(createUserTableSQL);
            System.out.println("Tabel 'users' siap atau sudah ada.");
            
            stmt.execute(createTransactionsTableSQL);
            System.out.println("Tabel 'transactions' siap atau sudah ada.");

            stmt.execute(createProductionsTableSQL);
            System.out.println("Tabel 'productions' siap atau sudah ada.");

            stmt.execute(createEmployeesTableSQL);
            System.out.println("Tabel 'employees' siap atau sudah ada.");
            
            stmt.execute(createTasksTableSQL);
            System.out.println("Tabel 'tasks' siap atau sudah ada.");

            stmt.execute(createAttendanceRecordsTableSQL); 
            System.out.println("Tabel 'attendance_records' siap atau sudah ada.");


            addInitialEmployeeData(conn);

        } catch (SQLException e) {
            System.err.println("Error saat inisialisasi database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addInitialEmployeeData(Connection conn) {

        String checkEmptySQL = "SELECT COUNT(*) AS count FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkEmptySQL)) {
            if (rs.next() && rs.getInt("count") == 0) {
                System.out.println("Menambahkan data karyawan awal...");
                String insertSQL = "INSERT INTO employees (nama_lengkap, posisi, tim) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    
                    // Karyawan 1
                    pstmt.setString(1, "Budi Santoso");
                    pstmt.setString(2, "Staf Kandang Senior");
                    pstmt.setString(3, "Tim A");
                    pstmt.addBatch();

                    // Karyawan 2
                    pstmt.setString(1, "Siti Aminah");
                    pstmt.setString(2, "Staf Pemerahan");
                    pstmt.setString(3, "Tim B");
                    pstmt.addBatch();

                    // Karyawan 3
                    pstmt.setString(1, "Agus Wijaya");
                    pstmt.setString(2, "Staf Kebersihan");
                    pstmt.setString(3, "Umum");
                    pstmt.addBatch();
                    
                    // Karyawan 4
                    pstmt.setString(1, "Dewi Lestari");
                    pstmt.setString(2, "Operator Pakan");
                    pstmt.setString(3, "Logistik");
                    pstmt.addBatch();

                    pstmt.executeBatch();
                    System.out.println("Data karyawan awal berhasil ditambahkan.");
                }
            } else {
                System.out.println("Data karyawan sudah ada atau gagal memeriksa tabel.");
            }
        } catch (SQLException e) {
            System.err.println("Error saat menambahkan data karyawan awal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final String DB_FILE_NAME = "moomoo_apps.db"; 


    public static List<TaskModel> getAllTasks() {
        List<TaskModel> tasks = new ArrayList<>();
        String sql = "SELECT t.id, t.nama_tugas, t.deskripsi_tugas, t.employee_id, e.nama_lengkap AS nama_karyawan, " +
                     "t.tanggal_tugas, t.waktu_tugas, t.prioritas, t.status, t.tanggal_selesai " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.id ORDER BY t.tanggal_tugas, t.waktu_tugas";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String namaTugas = rs.getString("nama_tugas");
                String deskripsiTugas = rs.getString("deskripsi_tugas");
                Integer employeeId = (Integer) rs.getObject("employee_id"); // Handle NULL employee_id
                String namaKaryawan = rs.getString("nama_karyawan");
                
                LocalDate tanggalTugas = null;
                String tanggalTugasStr = rs.getString("tanggal_tugas");
                if (tanggalTugasStr != null) tanggalTugas = LocalDate.parse(tanggalTugasStr, DATE_FORMATTER);

                LocalTime waktuTugas = null;
                String waktuTugasStr = rs.getString("waktu_tugas");
                if (waktuTugasStr != null && !waktuTugasStr.isEmpty()) waktuTugas = LocalTime.parse(waktuTugasStr, TIME_FORMATTER);
                
                String prioritas = rs.getString("prioritas");
                String status = rs.getString("status");

                LocalDate tanggalSelesai = null;
                String tanggalSelesaiStr = rs.getString("tanggal_selesai");
                if (tanggalSelesaiStr != null) tanggalSelesai = LocalDate.parse(tanggalSelesaiStr, DATE_FORMATTER);

                tasks.add(new TaskModel(id, namaTugas, deskripsiTugas, employeeId, namaKaryawan, tanggalTugas, waktuTugas, prioritas, status, tanggalSelesai));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public static boolean insertTask(TaskModel task) {
        String sql = "INSERT INTO tasks(nama_tugas, deskripsi_tugas, employee_id, tanggal_tugas, waktu_tugas, prioritas, status, tanggal_selesai) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getNamaTugas());
            pstmt.setString(2, task.getDeskripsiTugas());
            if (task.getEmployeeId() != null) {
                pstmt.setInt(3, task.getEmployeeId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setString(4, task.getTanggalTugas() != null ? task.getTanggalTugas().format(DATE_FORMATTER) : null);
            pstmt.setString(5, task.getWaktuTugas() != null ? task.getWaktuTugas().format(TIME_FORMATTER) : null);
            pstmt.setString(6, task.getPrioritas());
            pstmt.setString(7, task.getStatus());
            pstmt.setString(8, task.getTanggalSelesai() != null ? task.getTanggalSelesai().format(DATE_FORMATTER) : null);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1)); // Set the auto-generated ID back to the model
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error inserting task: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean updateTaskStatus(int taskId, String newStatus, LocalDate tanggalSelesai) {
        String sql = "UPDATE tasks SET status = ?, tanggal_selesai = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            if (tanggalSelesai != null) {
                pstmt.setString(2, tanggalSelesai.format(DATE_FORMATTER));
            } else {
                pstmt.setNull(2, java.sql.Types.VARCHAR);
            }
            pstmt.setInt(3, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Employee Management Methods ---
    public static List<EmployeeModel> getAllEmployees() {
        List<EmployeeModel> employees = new ArrayList<>();
        String sql = "SELECT id, nama_lengkap, posisi FROM employees ORDER BY nama_lengkap";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                employees.add(new EmployeeModel(rs.getInt("id"), rs.getString("nama_lengkap"), rs.getString("posisi")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all employees: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

        public static List<AttendanceRecordModel> getAttendanceRecordsByDate(LocalDate date) {
        List<AttendanceRecordModel> records = new ArrayList<>();
        String sql = "SELECT ar.id, ar.employee_id, e.nama_lengkap, e.posisi, " +
                     "ar.attendance_date, ar.status, ar.clock_in_time, ar.clock_out_time, ar.notes " +
                     "FROM attendance_records ar " +
                     "JOIN employees e ON ar.employee_id = e.id " +
                     "WHERE ar.attendance_date = ? " +
                     "ORDER BY e.nama_lengkap";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date.format(DATE_FORMATTER));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EmployeeModel employee = new EmployeeModel(rs.getInt("employee_id"), rs.getString("nama_lengkap"), rs.getString("posisi"));
                LocalTime clockIn = rs.getString("clock_in_time") != null ? LocalTime.parse(rs.getString("clock_in_time"), TIME_FORMATTER) : null;
                LocalTime clockOut = rs.getString("clock_out_time") != null ? LocalTime.parse(rs.getString("clock_out_time"), TIME_FORMATTER) : null;
                records.add(new AttendanceRecordModel(
                        rs.getInt("id"),
                        employee,
                        LocalDate.parse(rs.getString("attendance_date"), DATE_FORMATTER),
                        rs.getString("status"),
                        clockIn,
                        clockOut,
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance records by date: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    public static boolean addAttendanceRecord(AttendanceRecordModel record) {
        String sql = "INSERT INTO attendance_records (employee_id, attendance_date, status, clock_in_time, clock_out_time, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, record.getKaryawan().getId());
            pstmt.setString(2, record.getTanggalAbsen().format(DATE_FORMATTER));
            pstmt.setString(3, record.getStatusKehadiran());
            pstmt.setString(4, record.getWaktuMasuk() != null ? record.getWaktuMasuk().format(TIME_FORMATTER) : null);
            pstmt.setString(5, record.getWaktuKeluar() != null ? record.getWaktuKeluar().format(TIME_FORMATTER) : null);
            pstmt.setString(6, record.getCatatan());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getInt(1)); // Set auto-generated ID
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateAttendanceRecord(AttendanceRecordModel record) {
        String sql = "UPDATE attendance_records SET employee_id = ?, attendance_date = ?, status = ?, " +
                     "clock_in_time = ?, clock_out_time = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, record.getKaryawan().getId());
            pstmt.setString(2, record.getTanggalAbsen().format(DATE_FORMATTER));
            pstmt.setString(3, record.getStatusKehadiran());
            pstmt.setString(4, record.getWaktuMasuk() != null ? record.getWaktuMasuk().format(TIME_FORMATTER) : null);
            pstmt.setString(5, record.getWaktuKeluar() != null ? record.getWaktuKeluar().format(TIME_FORMATTER) : null);
            pstmt.setString(6, record.getCatatan());
            pstmt.setInt(7, record.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAttendanceRecord(int recordId) {
        String sql = "DELETE FROM attendance_records WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static EmployeeModel getEmployeeById(int id) {
        String sql = "SELECT id, nama_lengkap, posisi FROM employees WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new EmployeeModel(rs.getInt("id"), rs.getString("nama_lengkap"), rs.getString("posisi"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
        }
        return null; // Or throw an exception
    }

    public static void deleteDatabaseFile() {
        try {

            File dbFile = new File(DB_FILE_NAME);
            if (dbFile.exists()) {

                boolean deleted = Files.deleteIfExists(Paths.get(DB_FILE_NAME));
                if (deleted) {
                    System.out.println("File database '" + DB_FILE_NAME + "' telah berhasil dihapus.");
                } else {
                    System.err.println("Gagal menghapus file database '" + DB_FILE_NAME + "'. Mungkin masih terkunci atau path salah.");
                }
            } else {
                System.out.println("File database '" + DB_FILE_NAME + "' tidak ditemukan (sudah bersih).");
            }
        } catch (IOException e) {
            System.err.println("Error saat menghapus file database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { 
            System.err.println("Error tidak terduga saat menghapus file database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
