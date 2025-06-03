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

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            

            stmt.execute(createUserTableSQL);
            System.out.println("Tabel 'users' siap atau sudah ada.");
            
            stmt.execute(createTransactionsTableSQL);
            System.out.println("Tabel 'transactions' siap atau sudah ada.");

            stmt.execute(createProductionsTableSQL);
            System.out.println("Tabel 'productions' siap atau sudah ada.");

        } catch (SQLException e) {
            System.err.println("Error saat inisialisasi database: " + e.getMessage());
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
