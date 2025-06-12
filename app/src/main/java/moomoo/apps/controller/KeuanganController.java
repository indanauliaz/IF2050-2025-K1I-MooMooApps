package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.TransactionModel;
import moomoo.apps.model.UserModel;
import moomoo.apps.utils.DatabaseManager;
import moomoo.apps.utils.ValidationUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeuanganController implements UserAwareController {

    @FXML private VBox keuanganRootPane;

    @FXML private TabPane keuanganTabPane;
    @FXML private Tab pemasukanTab;
    @FXML private Tab pengeluaranTab;
    @FXML private Tab penggajianTab;

    // --- PEMASUKAN FXML Elements ---
    @FXML private TextField deskripsiPemasukanField;
    @FXML private DatePicker tanggalPemasukanPicker;
    @FXML private TextField jumlahPemasukanField;
    @FXML private ComboBox<String> metodePembayaranPemasukanBox;
    @FXML private ComboBox<String> kategoriPemasukanBox;
    @FXML private TextField catatanPemasukanField;
    @FXML private Button tambahPemasukanButton;
    @FXML private TableView<TransactionModel> pemasukanTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPemasukanCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> metodePemasukanCol;
    @FXML private TableColumn<TransactionModel, String> catatanPemasukanCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPemasukanCol;
    @FXML private ComboBox<String> filterBulanPemasukanBox;
    @FXML private Button exportPemasukanButton;

    // --- PENGELUARAN FXML Elements ---
    @FXML private TextField deskripsiPengeluaranField;
    @FXML private DatePicker tanggalPengeluaranPicker;
    @FXML private TextField jumlahPengeluaranField;
    @FXML private ComboBox<String> metodePembayaranPengeluaranBox;
    @FXML private ComboBox<String> kategoriPengeluaranBox;
    @FXML private TextField catatanPengeluaranField;
    @FXML private Button tambahPengeluaranButton;
    @FXML private TableView<TransactionModel> pengeluaranTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPengeluaranCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> metodePengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> catatanPengeluaranCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPengeluaranCol;
    @FXML private ComboBox<String> filterBulanPengeluaranBox;
    @FXML private Button exportPengeluaranButton;

    // --- PENGGAJIAN FXML Elements ---
    @FXML private TextField deskripsiPenggajianField;
    @FXML private DatePicker tanggalPenggajianPicker;
    @FXML private TextField jumlahPenggajianField;
    @FXML private ComboBox<String> metodePembayaranPenggajianBox;
    @FXML private ComboBox<String> kategoriPenggajianBox;
    @FXML private TextField catatanPenggajianField;
    @FXML private Button tambahPenggajianButton;
    @FXML private TableView<TransactionModel> penggajianTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPenggajianCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> metodePenggajianCol;
    @FXML private TableColumn<TransactionModel, String> catatanPenggajianCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPenggajianCol;
    @FXML private ComboBox<String> filterBulanPenggajianBox;
    @FXML private Button exportPenggajianButton;


    private ObservableList<TransactionModel> pemasukanList = FXCollections.observableArrayList();
    private ObservableList<TransactionModel> pengeluaranList = FXCollections.observableArrayList();
    private ObservableList<TransactionModel> penggajianList = FXCollections.observableArrayList();

    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private UserModel currentUser;
    private TransactionModel currentEditingTransaction = null; 

    private TransactionTabManager pemasukanManager;
    private TransactionTabManager pengeluaranManager;
    private TransactionTabManager penggajianManager;

    private FinanceModel financeModel;

    public void initialize() {
        this.financeModel = FinanceModel.getInstance();
        pemasukanManager = new TransactionTabManager("Pemasukan", pemasukanList,
                deskripsiPemasukanField, tanggalPemasukanPicker, jumlahPemasukanField,
                metodePembayaranPemasukanBox, kategoriPemasukanBox, catatanPemasukanField,
                tambahPemasukanButton, pemasukanTableView,
                deskripsiPemasukanCol, kategoriPemasukanCol, tanggalPemasukanCol, jumlahPemasukanCol,
                metodePemasukanCol, catatanPemasukanCol, aksiPemasukanCol, filterBulanPemasukanBox, exportPemasukanButton,
                this);

        pengeluaranManager = new TransactionTabManager("Pengeluaran", pengeluaranList,
                deskripsiPengeluaranField, tanggalPengeluaranPicker, jumlahPengeluaranField,
                metodePembayaranPengeluaranBox, kategoriPengeluaranBox, catatanPengeluaranField,
                tambahPengeluaranButton, pengeluaranTableView,
                deskripsiPengeluaranCol, kategoriPengeluaranCol, tanggalPengeluaranCol, jumlahPengeluaranCol,
                metodePengeluaranCol, catatanPengeluaranCol, aksiPengeluaranCol, filterBulanPengeluaranBox, exportPengeluaranButton,
                this);

        penggajianManager = new TransactionTabManager("Penggajian", penggajianList,
                deskripsiPenggajianField, tanggalPenggajianPicker, jumlahPenggajianField,
                metodePembayaranPenggajianBox, kategoriPenggajianBox, catatanPenggajianField,
                tambahPenggajianButton, penggajianTableView,
                deskripsiPenggajianCol, kategoriPenggajianCol, tanggalPenggajianCol, jumlahPenggajianCol,
                metodePenggajianCol, catatanPenggajianCol, aksiPenggajianCol, filterBulanPenggajianBox, exportPenggajianButton,
                this);

        tambahPemasukanButton.setOnAction(event -> {
            if (validateFormAndShowAlert(deskripsiPemasukanField.getText(), tanggalPemasukanPicker.getValue(), jumlahPemasukanField.getText(), metodePembayaranPemasukanBox.getValue(), kategoriPemasukanBox.getValue())) {
                if (currentEditingTransaction != null && "Pemasukan".equals(currentEditingTransaction.getTransactionType())) {
                    updateTransactionInDB(pemasukanManager);
                } else {
                    pemasukanManager.handleTambah();
                }
            }
        });

        tambahPengeluaranButton.setOnAction(event -> {
            if (validateFormAndShowAlert(deskripsiPengeluaranField.getText(), tanggalPengeluaranPicker.getValue(), jumlahPengeluaranField.getText(), metodePembayaranPengeluaranBox.getValue(), kategoriPengeluaranBox.getValue())) {
                if (currentEditingTransaction != null && "Pengeluaran".equals(currentEditingTransaction.getTransactionType())) {
                    updateTransactionInDB(pengeluaranManager);
                } else {
                    pengeluaranManager.handleTambah();
                }
            }
        });

        tambahPenggajianButton.setOnAction(event -> {
            if (validateFormAndShowAlert(deskripsiPenggajianField.getText(), tanggalPenggajianPicker.getValue(), jumlahPenggajianField.getText(), metodePembayaranPenggajianBox.getValue(), kategoriPenggajianBox.getValue())) {
                if (currentEditingTransaction != null && "Penggajian".equals(currentEditingTransaction.getTransactionType())) {
                    updateTransactionInDB(penggajianManager);
                } else {
                    penggajianManager.handleTambah();
                }
            }
        });

        pemasukanManager.initializeTab(
                Arrays.asList("Penjualan Produk", "Pendapatan Jasa", "Investasi", "Hibah", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai", "Dompet Digital", "Cek"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        pemasukanManager.setupTable(TABLE_DATE_FORMATTER);

        pengeluaranManager.initializeTab(
                Arrays.asList("Pakan Ternak", "Obat & Vaksin", "Operasional Kandang", "Perawatan Alat", "Transportasi", "Biaya Listrik/Air", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai", "Dompet Digital", "Kas Peternakan"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        pengeluaranManager.setupTable(TABLE_DATE_FORMATTER);

        penggajianManager.initializeTab(
                Arrays.asList("Gaji Karyawan Tetap", "Gaji Karyawan Harian", "Bonus", "Tunjangan", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        penggajianManager.setupTable(TABLE_DATE_FORMATTER);

        financeModel.getAllTransactions().addListener((ListChangeListener.Change<? extends TransactionModel> c) -> {
            System.out.println("DEBUG: Perubahan terdeteksi di FinanceModel, UI akan di-update.");
            updateLocalListsAndUI();
        });


        financeModel.loadAllTransactionsFromDB();
        keuanganTabPane.getSelectionModel().select(0);
    }

    private boolean validateFormAndShowAlert(String deskripsi, LocalDate tanggal, String jumlahStr, String metode, String kategori) {
        if (ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahStr, metode, kategori)) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Harap isi semua kolom wajib dengan format yang benar. Jumlah harus angka positif.");
            return false;
        }
    }

    private void updateLocalListsAndUI() {
        pemasukanList.setAll(
                financeModel.getAllTransactions().stream()
                        .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()))
                        .collect(Collectors.toList())
        );

        pengeluaranList.setAll(
                financeModel.getAllTransactions().stream()
                        .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()))
                        .collect(Collectors.toList())
        );

        penggajianList.setAll(
                financeModel.getAllTransactions().stream()
                        .filter(t -> "Penggajian".equalsIgnoreCase(t.getTransactionType()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
        updateLocalListsAndUI(); 
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public void saveTransactionToDB(TransactionModel transaction, ObservableList<TransactionModel> listToUpdate) {
        String sql = "INSERT INTO transactions (transaction_type, description, amount, category, date, payment_method, notes, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getCategory());
            pstmt.setString(5, DB_DATE_FORMATTER.format(transaction.getDate()));
            pstmt.setString(6, transaction.getPaymentMethod());
            pstmt.setString(7, transaction.getNotes());
            pstmt.setInt(8, transaction.getUserId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                    }
                }
                financeModel.addTransaction(transaction);
                DatabaseManager.updateLastChangeTimestamp();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data " + transaction.getTransactionType().toLowerCase() + " berhasil ditambahkan.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Deprecated
    public void loadTransactionData(String transactionType, ObservableList<TransactionModel> targetList) {
    }

    public void addAksiButtonsToTable(TableColumn<TransactionModel, Void> column, String type, TableView<TransactionModel> tableView) {
        column.setCellFactory(param -> new TableCell<>() {
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

                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.getStyleClass().add("table-action-icon");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("action-button", "action-button-delete");

                editButton.setOnAction(event -> {
                    TransactionModel transaction = tableView.getItems().get(getIndex());
                    handleEditTransaction(transaction, type);
                });

                deleteButton.setOnAction(event -> {
                    TransactionModel transaction = tableView.getItems().get(getIndex());
                    handleDeleteTransaction(transaction, type);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void handleEditTransaction(TransactionModel transaction, String type) {
        this.currentEditingTransaction = transaction;
        TransactionTabManager managerToUse = null;

        switch (type) {
            case "Pemasukan":
                managerToUse = pemasukanManager;
                tambahPemasukanButton.setText("Update Transaksi");
                keuanganTabPane.getSelectionModel().select(pemasukanTab);
                break;
            case "Pengeluaran":
                managerToUse = pengeluaranManager;
                tambahPengeluaranButton.setText("Update Transaksi");
                keuanganTabPane.getSelectionModel().select(pengeluaranTab);
                break;
            case "Penggajian":
                managerToUse = penggajianManager;
                tambahPenggajianButton.setText("Update Transaksi");
                keuanganTabPane.getSelectionModel().select(penggajianTab);
                break;
        }

        if (managerToUse != null) {
            managerToUse.populateFormForEdit(transaction);
        }
    }

    private void updateTransactionInDB(TransactionTabManager manager) {
        String deskripsi = manager.getDeskripsiField().getText();
        LocalDate tanggal = manager.getTanggalPicker().getValue();
        double jumlah = Double.parseDouble(manager.getJumlahField().getText().replace(",", "."));
        String kategori = manager.getKategoriBox().getValue();
        String metode = manager.getMetodePembayaranBox().getValue();
        String catatan = manager.getCatatanField().getText();


        String sql = "UPDATE transactions SET description = ?, amount = ?, category = ?, date = ?, " +
                     "payment_method = ?, notes = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, deskripsi);
            pstmt.setDouble(2, jumlah);
            pstmt.setString(3, kategori);
            pstmt.setString(4, DB_DATE_FORMATTER.format(tanggal));
            pstmt.setString(5, metode);
            pstmt.setString(6, catatan);
            pstmt.setInt(7, currentEditingTransaction.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {

                currentEditingTransaction.setDescription(deskripsi);
                currentEditingTransaction.setAmount(jumlah);
                currentEditingTransaction.setCategory(kategori);
                currentEditingTransaction.setDate(tanggal);
                currentEditingTransaction.setPaymentMethod(metode);
                currentEditingTransaction.setNotes(catatan);

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data transaksi berhasil diperbarui.");
                DatabaseManager.updateLastChangeTimestamp();
                
                pemasukanTableView.refresh();
                pengeluaranTableView.refresh();
                penggajianTableView.refresh();

                clearFormAndResetEditMode(manager);

            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui data transaksi di database.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFormAndResetEditMode(TransactionTabManager manager) {
        if (manager != null) {
            manager.clearForm();
            manager.getTambahButton().setText("+ Tambah " + manager.getTransactionType());
        }
        this.currentEditingTransaction = null;
    }

    public void handleDeleteTransaction(TransactionModel transaction, String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Transaksi " + type + "?");
        alert.setContentText("Apakah Anda yakin ingin menghapus data: " + transaction.getDescription() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM transactions WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, transaction.getId());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data " + type.toLowerCase() + " berhasil dihapus.");
                    financeModel.removeTransaction(transaction);
                    DatabaseManager.updateLastChangeTimestamp();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Data tidak ditemukan untuk dihapus.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}