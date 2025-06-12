package moomoo.apps.model; // Pindah ke paket model

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import moomoo.apps.utils.DatabaseManager; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProductionModel { 

    private final ObservableList<ProductionRecord> allProductionData;
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static ProductionModel instance;

    public ProductionModel() { 
        this.allProductionData = FXCollections.observableArrayList();
        loadProductionDataFromDB();
    }


    public static synchronized ProductionModel getInstance() {
        if (instance == null) {
            instance = new ProductionModel();
        }
        return instance;
    }

    public ObservableList<ProductionRecord> getAllProductionData() {
        return allProductionData;
    }

    public void loadProductionDataFromDB() {
        allProductionData.clear(); 
        String sql = "SELECT id, kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan FROM productions ORDER BY tanggal DESC, id DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allProductionData.add(new ProductionRecord(
                        rs.getInt("id"),
                        rs.getString("kategori"),
                        rs.getDouble("jumlah"),
                        rs.getString("satuan"),
                        LocalDate.parse(rs.getString("tanggal"), DB_DATE_FORMATTER),
                        rs.getString("lokasi"),
                        rs.getString("kualitas"),
                        rs.getString("catatan")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading production data in ProductionModel: " + e.getMessage());
            e.printStackTrace();
 
        }
    }

    public boolean saveRecordToDB(ProductionRecord record) {
        String sql = "INSERT INTO productions (kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, record.getKategori());
            pstmt.setDouble(2, record.getJumlah());
            pstmt.setString(3, record.getSatuan());
            if (record.getTanggal() != null) {
                pstmt.setString(4, DB_DATE_FORMATTER.format(record.getTanggal()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }
            pstmt.setString(5, record.getLokasi());
            pstmt.setString(6, record.getKualitas());
            pstmt.setString(7, record.getCatatan());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getInt(1)); 
                    }
                }
                allProductionData.add(0, record); 
                DatabaseManager.updateLastChangeTimestamp();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving production record in ProductionModel: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRecordInDB(ProductionRecord record) {
        String sql = "UPDATE productions SET kategori = ?, jumlah = ?, satuan = ?, tanggal = ?, lokasi = ?, kualitas = ?, catatan = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getKategori());
            pstmt.setDouble(2, record.getJumlah());
            pstmt.setString(3, record.getSatuan());
            if (record.getTanggal() != null) {
                pstmt.setString(4, DB_DATE_FORMATTER.format(record.getTanggal()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }
            pstmt.setString(5, record.getLokasi());
            pstmt.setString(6, record.getKualitas());
            pstmt.setString(7, record.getCatatan());
            pstmt.setInt(8, record.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                int index = -1;
                for(int i=0; i < allProductionData.size(); i++){
                    if(allProductionData.get(i).getId() == record.getId()){
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    allProductionData.set(index, record); 
                } else {

                    loadProductionDataFromDB(); 
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating production record in ProductionModel: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteRecordFromDB(int recordId) {
        String sql = "DELETE FROM productions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Hapus dari ObservableList agar UI terupdate
                allProductionData.removeIf(r -> r.getId() == recordId);
                DatabaseManager.updateLastChangeTimestamp();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting production record in ProductionModel: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
