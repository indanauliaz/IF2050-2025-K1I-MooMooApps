package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.ProductionModel; 
import moomoo.apps.model.ProductionRecord;
import moomoo.apps.model.UserModel;
import moomoo.apps.utils.ValidationUtils; // <-- IMPORT KELAS VALIDASI

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProductionController implements UserAwareController {

    // ... (Deklarasi FXML tetap sama) ...
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

    private ProductionModel productionModel;
    
    private ObservableList<ProductionRecord> currentPageDataList = FXCollections.observableArrayList();

    private final String[] kategoriOptions = {"Susu", "Daging", "Telur", "Pakan Ternak Jadi", "Pupuk Kandang"};
    private final String[] satuanOptions = {"Liter", "Kg", "Butir", "Karung", "Ton"};
    private final String[] lokasiOptions = {"Kandang A1", "Kandang A2", "Kandang B1", "Gudang Pakan", "Area Pengomposan"};
    private final String[] kualitasOptions = {"Super A", "Grade A", "Grade B", "Grade C", "Standar"};

    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ITEMS_PER_PAGE = 10;

    private UserModel currentUser;
    private ProductionRecord currentEditingRecord = null;
    
    @FXML
    public void initialize() {
        this.productionModel = ProductionModel.getInstance();

        kategoriComboBox.setItems(FXCollections.observableArrayList(kategoriOptions));
        satuanComboBox.setItems(FXCollections.observableArrayList(satuanOptions));
        lokasiComboBox.setItems(FXCollections.observableArrayList(lokasiOptions));
        kualitasComboBox.setItems(FXCollections.observableArrayList(kualitasOptions));

        setupTableColumns();
        tanggalPicker.setValue(LocalDate.now());
        productionModel.getAllProductionData().addListener((ListChangeListener<ProductionRecord>) c -> {
            System.out.println("ProductionController: Perubahan terdeteksi pada data master, me-refresh tabel.");
            refreshTableAndPagination();
        });
        setupPagination();
    }

    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
    }

    private void handleCommonSaveLogic() {
        String kategori = kategoriComboBox.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        String jumlahStr = jumlahField.getText();
        String satuan = satuanComboBox.getValue();
        String lokasi = lokasiComboBox.getValue();
        String kualitas = kualitasComboBox.getValue();
        String catatan = catatanField.getText();

        // ** PERUBAHAN UTAMA: Memanggil metode validasi terpusat **
        if (!ValidationUtils.validateProductionInput(kategori, tanggal, jumlahStr, satuan, lokasi, kualitas)) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap isi semua kolom wajib dengan format yang benar. Jumlah harus berupa angka positif.");
            return;
        }

        double jumlah = Double.parseDouble(jumlahStr.replace(",", "."));

        if (currentEditingRecord != null) {
            // Logic untuk UPDATE
            currentEditingRecord.setKategori(kategori);
            currentEditingRecord.setTanggal(tanggal);
            currentEditingRecord.setJumlah(jumlah);
            currentEditingRecord.setSatuan(satuan);
            currentEditingRecord.setLokasi(lokasi);
            currentEditingRecord.setKualitas(kualitas);
            currentEditingRecord.setCatatan(catatan);

            if (productionModel.updateRecordInDB(currentEditingRecord)) {
                clearFormAndResetEditMode();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan produksi berhasil diperbarui.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui catatan produksi di database.");
            }
        } else {
            // Logic untuk INSERT
            ProductionRecord newRecord = new ProductionRecord(kategori, jumlah, satuan, tanggal, lokasi, kualitas, catatan);
            if (productionModel.saveRecordToDB(newRecord)) {
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Catatan produksi berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan catatan produksi ke database.");
            }
        }
    }
    
    @FXML
    private void handleTambahCatatan(ActionEvent event) {
        handleCommonSaveLogic();
    }
    
    // Metode handleUpdateCatatan sekarang digabung ke dalam handleCommonSaveLogic
    // private void handleUpdateCatatan() { ... }

    // Metode validateInput dan parseJumlah dihapus dari sini

    // ... Sisa dari file ProductionController.java tetap sama ...
    private void setupTableColumns() {
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        jumlahCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getJumlahDenganSatuan()));
        tanggalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTanggal() != null ? TABLE_DATE_FORMATTER.format(cellData.getValue().getTanggal()) : ""));
        lokasiCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        catatanCol.setCellValueFactory(new PropertyValueFactory<>("catatan"));
        kualitasCol.setCellValueFactory(new PropertyValueFactory<>("kualitas"));
        addAksiButtonsToTable();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) productionModel.getAllProductionData().size() / ITEMS_PER_PAGE);
        if (pageCount == 0) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.currentPageIndexProperty().removeListener(this::paginationChangeListener);
        pagination.currentPageIndexProperty().addListener(this::paginationChangeListener);
        updateTablePage(0);
    }
    
    private void paginationChangeListener(javafx.beans.value.ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        updateTablePage(newValue.intValue());
    }

    private void updateTablePage(int pageIndex) {
        ObservableList<ProductionRecord> allData = productionModel.getAllProductionData();
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allData.size());

        currentPageDataList.clear();
        if (fromIndex < toIndex) {
            currentPageDataList.setAll(allData.subList(fromIndex, toIndex));
        }
        productionTableView.setItems(currentPageDataList);
    }

    private void handleDeleteCatatan(ProductionRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Catatan Produksi?");
        alert.setContentText("Apakah Anda yakin ingin menghapus catatan untuk kategori: " + record.getKategori() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (productionModel.deleteRecordFromDB(record.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Catatan produksi berhasil dihapus.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus catatan produksi dari database.");
            }
        }
    }

    private void refreshTableAndPagination() {
        int currentPage = pagination.getCurrentPageIndex();
        int newPageCount = (int) Math.ceil((double) productionModel.getAllProductionData().size() / ITEMS_PER_PAGE);
        if (newPageCount == 0) newPageCount = 1;
        
        if (currentPage >= newPageCount) {
            currentPage = Math.max(0, newPageCount - 1);
        }
        
        pagination.setPageCount(newPageCount);
        pagination.setCurrentPageIndex(currentPage); 
        updateTablePage(pagination.getCurrentPageIndex()); 
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
                    prepareEditCatatan(record);
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
    
    private void prepareEditCatatan(ProductionRecord record) {
        currentEditingRecord = record;
        kategoriComboBox.setValue(record.getKategori());
        tanggalPicker.setValue(record.getTanggal());
        jumlahField.setText(String.valueOf(record.getJumlah()).replace(".", ","));
        satuanComboBox.setValue(record.getSatuan());
        lokasiComboBox.setValue(record.getLokasi());
        kualitasComboBox.setValue(record.getKualitas());
        catatanField.setText(record.getCatatan());
        tambahCatatanButton.setText("Update Catatan");
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