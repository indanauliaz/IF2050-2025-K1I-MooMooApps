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

public class FinanceModel {

    private static FinanceModel instance;
    private final ObservableList<TransactionModel> allTransactions;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // konstruktor 
    private FinanceModel() {
        allTransactions = FXCollections.observableArrayList();
        loadAllTransactionsFromDB(); 
    }

    public static synchronized FinanceModel getInstance() {
        if (instance == null) {
            instance = new FinanceModel();
        }
        return instance;
    }

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
    public void loadAllTransactionsFromDB() {
        allTransactions.clear();
        String sql = "SELECT * FROM transactions ORDER BY date DESC"; 

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