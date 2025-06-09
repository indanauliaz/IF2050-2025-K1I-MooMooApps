package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.TransactionModel;
import moomoo.apps.model.UserModel;


import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KeuanganPemilikController implements Initializable, UserAwareController {

    // FXML Components
    @FXML private TableView<TransactionModel> tabelPemasukan;
    // DIUBAH: Tabel penggajian sekarang menggunakan TransactionModel
    @FXML private TableView<TransactionModel> tabelPengeluaran;
    @FXML private TableView<TransactionModel> tabelPenggajian; 
    
    // Kolom Tabel Pemasukan
    @FXML private TableColumn<TransactionModel, String> kolomDeskripsiPemasukan;
    @FXML private TableColumn<TransactionModel, String> kolomKategoriPemasukan;
    @FXML private TableColumn<TransactionModel, LocalDate> kolomTanggalPemasukan;
    @FXML private TableColumn<TransactionModel, Double> kolomJumlahPemasukan;
    @FXML private TableColumn<TransactionModel, String> kolomMetodePemasukan;
    @FXML private TableColumn<TransactionModel, String> kolomCatatanPemasukan;

    // Kolom Tabel Pengeluaran
    @FXML private TableColumn<TransactionModel, String> kolomDeskripsiPengeluaran;
    @FXML private TableColumn<TransactionModel, String> kolomKategoriPengeluaran;
    @FXML private TableColumn<TransactionModel, LocalDate> kolomTanggalPengeluaran;
    @FXML private TableColumn<TransactionModel, Double> kolomJumlahPengeluaran;
    @FXML private TableColumn<TransactionModel, String> kolomMetodePengeluaran;
    @FXML private TableColumn<TransactionModel, String> kolomCatatanPengeluaran;
    
    // DIUBAH: Semua kolom penggajian sekarang merujuk ke TransactionModel
    @FXML private TableColumn<TransactionModel, String> kolomDeskripsiGaji;
    @FXML private TableColumn<TransactionModel, String> kolomKategoriGaji;
    @FXML private TableColumn<TransactionModel, LocalDate> kolomTanggalGaji;
    @FXML private TableColumn<TransactionModel, Double> kolomJumlahGaji;
    @FXML private TableColumn<TransactionModel, String> kolomMetodeGaji;
    @FXML private TableColumn<TransactionModel, String> kolomCatatanGaji;

    private final ObservableList<TransactionModel> pemasukanList = FXCollections.observableArrayList();
    private final ObservableList<TransactionModel> pengeluaranList = FXCollections.observableArrayList();
    private final ObservableList<TransactionModel> penggajianList = FXCollections.observableArrayList();

    private UserModel currentUser;
    private FinanceModel financeModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.financeModel = FinanceModel.getInstance();
        setupTableColumns();

        tabelPemasukan.setItems(pemasukanList);
        tabelPengeluaran.setItems(pengeluaranList);
        tabelPenggajian.setItems(penggajianList);
        
        tabelPemasukan.setPlaceholder(new Label("Tidak ada data pemasukan untuk ditampilkan."));
        tabelPengeluaran.setPlaceholder(new Label("Tidak ada data pengeluaran untuk ditampilkan."));
        tabelPenggajian.setPlaceholder(new Label("Tidak ada data penggajian untuk ditampilkan."));
    }

    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
        updateTables();
    }
    
    private void updateTables() {
        if (financeModel == null) return;

        ObservableList<TransactionModel> allTransactions = financeModel.getAllTransactions();

        // Salin data Pemasukan
        pemasukanList.setAll(
            allTransactions.stream()
                .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );

        // Salin data Pengeluaran
        pengeluaranList.setAll(
            allTransactions.stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );
        
        // Salin data Penggajian
        penggajianList.setAll(
            allTransactions.stream()
                .filter(t -> "Penggajian".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );
    }

    private void setupTableColumns() {
        // --- Konfigurasi Tabel Pemasukan ---
        kolomDeskripsiPemasukan.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriPemasukan.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalPemasukan.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomJumlahPemasukan.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomMetodePemasukan.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanPemasukan.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // --- Konfigurasi Tabel Pengeluaran ---
        kolomDeskripsiPengeluaran.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriPengeluaran.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalPengeluaran.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomJumlahPengeluaran.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomMetodePengeluaran.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanPengeluaran.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        // DIUBAH: PropertyValueFactory menunjuk ke properti TransactionModel
        kolomDeskripsiGaji.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriGaji.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalGaji.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomJumlahGaji.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomMetodeGaji.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanGaji.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }
}