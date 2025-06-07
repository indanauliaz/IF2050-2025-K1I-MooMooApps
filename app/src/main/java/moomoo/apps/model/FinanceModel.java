package moomoo.apps.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import moomoo.apps.utils.DatabaseManager; // Asumsi path
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FinanceModel {

    private static FinanceModel instance;
    private final ObservableList<TransactionModel> allTransactions;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    private FinanceModel() {
        allTransactions = FXCollections.observableArrayList();
        loadAllTransactionsFromDB(); // Muat data saat pertama kali dibuat
    }

    // Metode Singleton untuk mendapatkan satu-satunya instance
    public static synchronized FinanceModel getInstance() {
        if (instance == null) {
            instance = new FinanceModel();
        }
        return instance;
    }

    // Berikan akses ke list (tapi jangan biarkan controller lain mengubahnya langsung)
    public ObservableList<TransactionModel> getAllTransactions() {
        return allTransactions;
    }

    // Metode untuk menambah transaksi
    public void addTransaction(TransactionModel transaction) {
        if (transaction != null) {
            allTransactions.add(transaction);
        }
    }

    // Metode untuk menghapus transaksi
    public void removeTransaction(TransactionModel transaction) {
        if (transaction != null) {
            allTransactions.removeIf(t -> t.getId() == transaction.getId());
        }
    }
    
    // Metode untuk memuat semua data dari database
    private void loadAllTransactionsFromDB() {
        allTransactions.clear();
        String sql = "SELECT * FROM transactions ORDER BY date DESC"; // Ambil semua tipe

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date"), DB_DATE_FORMATTER);
                allTransactions.add(new TransactionModel(
                    rs.getInt("id"),
                    rs.getString("transaction_type"),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getString("category"),
                    date,
                    rs.getString("payment_method"),
                    rs.getString("notes"),
                    rs.getInt("user_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat semua transaksi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}