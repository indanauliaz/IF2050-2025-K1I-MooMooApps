package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import moomoo.apps.model.ProductionRecord;
import moomoo.apps.model.UserModel;
import moomoo.apps.utils.DatabaseManager; 

import java.sql.Connection; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
// import java.util.stream.Collectors; // Tidak terpakai saat ini

public class ProductionController {

    @FXML private ComboBox<String> kategoriComboBox;
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField jumlahField;
    @FXML private ComboBox<String> satuanComboBox;
    @FXML private ComboBox<String> lokasiComboBox;
    @FXML private ComboBox<String> kualitasComboBox;
    @FXML private TextField catatanField;
    @FXML private Button tambahCatatanButton;
    @FXML private TableView<ProductionRecord> productionTableView;
    @FXML private TableColumn<ProductionRecord, String> kategoriCol;
    @FXML private TableColumn<ProductionRecord, String> jumlahCol;
    @FXML private TableColumn<ProductionRecord, String> tanggalCol;
    @FXML private TableColumn<ProductionRecord, String> lokasiCol;
    @FXML private TableColumn<ProductionRecord, String> catatanCol;
    @FXML private TableColumn<ProductionRecord, String> kualitasCol;
    @FXML private TableColumn<ProductionRecord, Void> aksiCol;
    @FXML private Pagination pagination;

    private ObservableList<ProductionRecord> productionList = FXCollections.observableArrayList(); // Untuk TableView (data per halaman)
    private ObservableList<ProductionRecord> allDataList = FXCollections.observableArrayList(); // Semua data dari DB

    private final String[] kategoriOptions = {"Susu", "Daging", "Telur", "Pakan Ternak Jadi", "Pupuk Kandang"};
    private final String[] satuanOptions = {"Liter", "Kg", "Butir", "Karung", "Ton"};
    private final String[] lokasiOptions = {"Kandang A1", "Kandang A2", "Kandang B1", "Gudang Pakan", "Area Pengomposan"};
    private final String[] kualitasOptions = {"Super A", "Grade A", "Grade B", "Grade C", "Standar"};

    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // Untuk simpan/ambil dari DB
    private static final int ITEMS_PER_PAGE = 10;

    private UserModel currentUser;
    private ProductionRecord currentEditingRecord = null; 

    public void initialize() {
        kategoriComboBox.setItems(FXCollections.observableArrayList(kategoriOptions));
        satuanComboBox.setItems(FXCollections.observableArrayList(satuanOptions));
        lokasiComboBox.setItems(FXCollections.observableArrayList(lokasiOptions));
        kualitasComboBox.setItems(FXCollections.observableArrayList(kualitasOptions));

        setupTableColumns();
        loadProductionDataFromDB();
        setupPagination();
        tanggalPicker.setValue(LocalDate.now());
    }

    public void initData(UserModel user) {
        this.currentUser = user;
        System.out.println("ProductionController initData called for user: " + (user != null ? user.getUsername() : "null"));
        // loadProductionDataFromDB(); // Jika data bergantung pada user
    }

    private void setupTableColumns() {
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        jumlahCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJumlahDenganSatuan())
        );
        tanggalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getTanggal() != null ?
                                TABLE_DATE_FORMATTER.format(cellData.getValue().getTanggal()) : ""
                ));
        lokasiCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        catatanCol.setCellValueFactory(new PropertyValueFactory<>("catatan"));
        kualitasCol.setCellValueFactory(new PropertyValueFactory<>("kualitas"));
        addAksiButtonsToTable();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allDataList.size() / ITEMS_PER_PAGE);
        if (pageCount == 0) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0); // Selalu mulai dari halaman pertama
        pagination.currentPageIndexProperty().removeListener(this::paginationChangeListener); // Hapus listener lama jika ada
        pagination.currentPageIndexProperty().addListener(this::paginationChangeListener);
        updateTablePage(0);
    }
    
    private void paginationChangeListener(javafx.beans.value.ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        updateTablePage(newValue.intValue());
    }


    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allDataList.size());

        productionList.clear();
        if (fromIndex < toIndex) { // Pastikan fromIndex valid dan ada data
            productionList.setAll(allDataList.subList(fromIndex, toIndex));
        }
        productionTableView.setItems(productionList);
    }

    private void addAksiButtonsToTable() {
        aksiCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.getStyleClass().add("table-action-icon");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().addAll("action-button", "action-button-edit");
                editButton.setOnAction(event -> {
                    ProductionRecord record = getTableView().getItems().get(getIndex());
                    prepareEditCatatan(record); // Ubah ke prepareEdit
                });

                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.getStyleClass().add("table-action-icon");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("action-button", "action-button-delete");
                deleteButton.setOnAction(event -> {
                    ProductionRecord record = getTableView().getItems().get(getIndex());
                    handleDeleteCatatan(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    @FXML
    private void handleTambahCatatan(ActionEvent event) {
        if (currentEditingRecord != null) {
            handleUpdateCatatan(); 
            return;
        }
        

        String kategori = kategoriComboBox.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        String jumlahStr = jumlahField.getText();
        String satuan = satuanComboBox.getValue();
        String lokasi = lokasiComboBox.getValue();
        String kualitas = kualitasComboBox.getValue();
        String catatan = catatanField.getText();

        if (!validateInput(kategori, tanggal, jumlahStr, satuan, lokasi, kualitas)) return;

        double jumlah = parseJumlah(jumlahStr);
        if (jumlah < 0) return; 

        ProductionRecord newRecord = new ProductionRecord(kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan);

        if (saveRecordToDB(newRecord)) {
            allDataList.add(0, newRecord); 
            refreshTableAndPagination();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan produksi berhasil ditambahkan.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan catatan produksi ke database.");
        }
    }
    
    private void handleUpdateCatatan() {
        if (currentEditingRecord == null) return;

        String kategori = kategoriComboBox.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        String jumlahStr = jumlahField.getText();
        String satuan = satuanComboBox.getValue();
        String lokasi = lokasiComboBox.getValue();
        String kualitas = kualitasComboBox.getValue();
        String catatan = catatanField.getText();

        if (!validateInput(kategori, tanggal, jumlahStr, satuan, lokasi, kualitas)) return;
        
        double jumlah = parseJumlah(jumlahStr);
        if (jumlah < 0) return; 


        currentEditingRecord.setKategori(kategori);
        currentEditingRecord.setTanggal(tanggal);
        currentEditingRecord.setJumlah(jumlah);
        currentEditingRecord.setSatuan(satuan);
        currentEditingRecord.setLokasi(lokasi);
        currentEditingRecord.setKualitas(kualitas);
        currentEditingRecord.setCatatan(catatan);

        if (updateRecordInDB(currentEditingRecord)) {

            int index = allDataList.indexOf(currentEditingRecord); 
            if (index != -1) {
                 allDataList.set(index, currentEditingRecord); 
            } else {
                 loadProductionDataFromDB(); 
            }
            
            refreshTableAndPagination();
            clearFormAndResetEditMode();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan produksi berhasil diperbarui.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui catatan produksi di database.");
        }
    }


    private void prepareEditCatatan(ProductionRecord record) {
        currentEditingRecord = record; // Simpan record yang akan diedit
        kategoriComboBox.setValue(record.getKategori());
        tanggalPicker.setValue(record.getTanggal());
        jumlahField.setText(String.valueOf(record.getJumlah()).replace(".", ","));
        satuanComboBox.setValue(record.getSatuan());
        lokasiComboBox.setValue(record.getLokasi());
        kualitasComboBox.setValue(record.getKualitas());
        catatanField.setText(record.getCatatan());

        tambahCatatanButton.setText("Update Catatan"); // Ubah teks tombol
        // Anda bisa menambahkan tombol "Batal Edit" juga jika perlu
    }

    private void handleDeleteCatatan(ProductionRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Catatan Produksi?");
        alert.setContentText("Apakah Anda yakin ingin menghapus catatan untuk kategori: " + record.getKategori() + " pada tanggal " + TABLE_DATE_FORMATTER.format(record.getTanggal()) + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteRecordFromDB(record.getId())) {
                allDataList.remove(record);
                refreshTableAndPagination();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Catatan produksi berhasil dihapus.");
            } else {
                 showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus catatan produksi dari database.");
            }
        }
    }
    
    private boolean validateInput(String kategori, LocalDate tanggal, String jumlahStr, String satuan, String lokasi, String kualitas) {
        if (kategori == null || kategori.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Kategori produksi harus dipilih."); return false;
        }
        if (tanggal == null) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Tanggal harus diisi."); return false;
        }
        if (jumlahStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Jumlah harus diisi."); return false;
        }
        if (satuan == null || satuan.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Satuan untuk jumlah harus dipilih."); return false;
        }
        if (lokasi == null || lokasi.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Lokasi harus dipilih."); return false;
        }
        if (kualitas == null || kualitas.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Kualitas harus dipilih."); return false;
        }
        return true;
    }

    private double parseJumlah(String jumlahStr) {
        try {
            double jumlah = Double.parseDouble(jumlahStr.replace(",","."));
            if (jumlah <= 0) throw new NumberFormatException("Jumlah harus positif.");
            return jumlah;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah harus berupa angka positif yang valid.");
            return -1; // Indikasi error
        }
    }
    
    private void refreshTableAndPagination() {
        int currentPage = pagination.getCurrentPageIndex();
        int newPageCount = (int) Math.ceil((double) allDataList.size() / ITEMS_PER_PAGE);
        if (newPageCount == 0) newPageCount = 1;
        pagination.setPageCount(newPageCount);

        if (currentPage >= newPageCount && newPageCount > 0) {
            pagination.setCurrentPageIndex(newPageCount - 1);
            updateTablePage(newPageCount - 1);
        } else if (allDataList.isEmpty()){ // Jika list kosong setelah operasi
            updateTablePage(0); // Tampilkan halaman kosong
        }
        else {
            updateTablePage(currentPage);
        }
    }

    // --- Database Interaction Methods ---

    private boolean saveRecordToDB(ProductionRecord record) {
        String sql = "INSERT INTO productions (kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, record.getKategori());
            pstmt.setDouble(2, record.getJumlah());
            pstmt.setString(3, record.getSatuan());
            pstmt.setString(4, DB_DATE_FORMATTER.format(record.getTanggal()));
            pstmt.setString(5, record.getLokasi());
            pstmt.setString(6, record.getKualitas());
            pstmt.setString(7, record.getCatatan());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getInt(1)); // Set ID yang digenerate DB ke objek
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving production record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateRecordInDB(ProductionRecord record) {
        String sql = "UPDATE productions SET kategori = ?, jumlah = ?, satuan = ?, tanggal = ?, lokasi = ?, kualitas = ?, catatan = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getKategori());
            pstmt.setDouble(2, record.getJumlah());
            pstmt.setString(3, record.getSatuan());
            pstmt.setString(4, DB_DATE_FORMATTER.format(record.getTanggal()));
            pstmt.setString(5, record.getLokasi());
            pstmt.setString(6, record.getKualitas());
            pstmt.setString(7, record.getCatatan());
            pstmt.setInt(8, record.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating production record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean deleteRecordFromDB(int recordId) {
        String sql = "DELETE FROM productions WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting production record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void loadProductionDataFromDB() {
        allDataList.clear();
        String sql = "SELECT id, kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan FROM productions ORDER BY tanggal DESC, id DESC";
        // Jika ingin filter by user, dan tabel productions punya user_id:
        // if (currentUser != null) { sql = "SELECT ... WHERE user_id = " + currentUser.getId() + " ORDER BY ..."; }
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allDataList.add(new ProductionRecord(
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
            System.err.println("Error loading production data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data produksi.");
        }
    
        if (pagination != null) { 
             refreshTableAndPagination(); 
        }
    }

    private void clearForm() {
        kategoriComboBox.getSelectionModel().clearSelection();
        tanggalPicker.setValue(LocalDate.now());
        jumlahField.clear();
        satuanComboBox.getSelectionModel().clearSelection();
        lokasiComboBox.getSelectionModel().clearSelection();
        kualitasComboBox.getSelectionModel().clearSelection();
        catatanField.clear();
        kategoriComboBox.requestFocus();
    }
    
    private void clearFormAndResetEditMode() {
        clearForm();
        currentEditingRecord = null;
        tambahCatatanButton.setText("+ Tambah Catatan");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}