package moomoo.apps.utils;

import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
