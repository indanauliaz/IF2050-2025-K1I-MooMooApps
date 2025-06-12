package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.TransactionModel;
import moomoo.apps.model.UserModel;


import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KeuanganPemilikController implements Initializable, UserAwareController {

    // Komponen FXML untuk Tabel-tabel Keuangan
    @FXML private TableView<TransactionModel> tabelPemasukan;
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
    
    // Kolom Tabel Penggajian
    @FXML private TableColumn<TransactionModel, String> kolomDeskripsiGaji;
    @FXML private TableColumn<TransactionModel, String> kolomKategoriGaji;
    @FXML private TableColumn<TransactionModel, LocalDate> kolomTanggalGaji;
    @FXML private TableColumn<TransactionModel, Double> kolomJumlahGaji;
    @FXML private TableColumn<TransactionModel, String> kolomMetodeGaji;
    @FXML private TableColumn<TransactionModel, String> kolomCatatanGaji;

    // Daftar Observable untuk menyimpan data transaksi per jenis
    private final ObservableList<TransactionModel> pemasukanList = FXCollections.observableArrayList();
    private final ObservableList<TransactionModel> pengeluaranList = FXCollections.observableArrayList();
    private final ObservableList<TransactionModel> penggajianList = FXCollections.observableArrayList();

    private UserModel currentUser;
    private FinanceModel financeModel;

    Locale localeDenganBuilder = new Locale.Builder()
                                 .setLanguage("id")
                                 .setRegion("ID")
                                 .build();

    /**
     * Metode inisialisasi yang dipanggil secara otomatis oleh JavaFX setelah file FXML dimuat.
     * Metode ini menyiapkan model keuangan, mengkonfigurasi kolom tabel, mengatur item untuk TableView,
     * dan menambahkan placeholder untuk tabel kosong. Ini juga mendaftarkan listener untuk memantau
     * perubahan pada FinanceModel.
     *
     * @param url Lokasi yang digunakan untuk mengatasi jalur relatif untuk objek root, atau null jika lokasi tidak diketahui.
     * @param resourceBundle Sumber daya yang digunakan untuk melokalisasi objek root, atau null jika objek root tidak dilokalisasi.
     */
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

        financeModel.getAllTransactions().addListener((ListChangeListener<TransactionModel>) c -> {
            System.out.println("KeuanganPemilikController mendeteksi perubahan di FinanceModel. Memperbarui tabel...");
            updateTables();
        });
    }

    /**
     * Metode ini dipanggil untuk menginisialisasi data spesifik pengguna.
     * Ini adalah bagian dari interface UserAwareController.
     * Setelah pengguna ditetapkan, tabel akan diperbarui.
     *
     * @param user Objek UserModel yang mewakili pengguna yang sedang login.
     */
    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
        updateTables(); 
    }
    
    /**
     * Memperbarui data di ketiga tabel (Pemasukan, Pengeluaran, Penggajian)
     * dengan memfilter transaksi dari daftar `allTransactions` di FinanceModel.
     * Metode ini memastikan bahwa data yang ditampilkan selalu sinkron dengan model.
     */
    private void updateTables() {
        if (financeModel == null) return;

        ObservableList<TransactionModel> allTransactions = financeModel.getAllTransactions();

        pemasukanList.setAll(
            allTransactions.stream()
                .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );

        pengeluaranList.setAll(
            allTransactions.stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );
        
        penggajianList.setAll(
            allTransactions.stream()
                .filter(t -> "Penggajian".equalsIgnoreCase(t.getTransactionType().trim()))
                .collect(Collectors.toList())
        );
    }

    /**
     * Mengatur `CellValueFactory` untuk setiap kolom di ketiga tabel.
     * Ini menghubungkan properti dari objek TransactionModel dengan kolom tabel yang sesuai.
     * Juga menerapkan `CellFactory` kustom untuk memformat tanggal dan jumlah.
     */

    private void setupTableColumns() {
        // --- Konfigurasi Kolom Tabel Pemasukan ---
        kolomDeskripsiPemasukan.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriPemasukan.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalPemasukan.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomTanggalPemasukan.setCellFactory(column -> createDateCell()); 
        kolomJumlahPemasukan.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomJumlahPemasukan.setCellFactory(column -> createCurrencyCell());
        kolomMetodePemasukan.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanPemasukan.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // --- Konfigurasi Kolom Tabel Pengeluaran ---
        kolomDeskripsiPengeluaran.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriPengeluaran.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalPengeluaran.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomTanggalPengeluaran.setCellFactory(column -> createDateCell()); 
        kolomJumlahPengeluaran.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomJumlahPengeluaran.setCellFactory(column -> createCurrencyCell());
        kolomMetodePengeluaran.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanPengeluaran.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        // --- Konfigurasi Kolom Tabel Penggajian ---

        kolomDeskripsiGaji.setCellValueFactory(new PropertyValueFactory<>("description"));
        kolomKategoriGaji.setCellValueFactory(new PropertyValueFactory<>("category"));
        kolomTanggalGaji.setCellValueFactory(new PropertyValueFactory<>("date"));
        kolomTanggalGaji.setCellFactory(column -> createDateCell()); 
        kolomJumlahGaji.setCellValueFactory(new PropertyValueFactory<>("amount"));
        kolomJumlahGaji.setCellFactory(column -> createCurrencyCell());
        kolomMetodeGaji.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        kolomCatatanGaji.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    /**
     * Membuat dan mengembalikan `TableCell` kustom untuk memformat objek `LocalDate`
     * menjadi string tanggal dalam format "dd MMMM yyyy" dengan lokal Indonesia.
     *
     * @return Sebuah `TableCell` untuk kolom tanggal.
     */
    private TableCell<TransactionModel, LocalDate> createDateCell() {
        // Formatter tanggal dengan nama bulan lengkap dalam Bahasa Indonesia
        final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", localeDenganBuilder);
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : DATE_FORMATTER.format(item));
            }
        };
    }

    /**
     * Membuat dan mengembalikan `TableCell` kustom untuk memformat nilai `Double`
     * menjadi string mata uang menggunakan format mata uang Indonesia (IDR).
     *
     * @return Sebuah `TableCell` untuk kolom jumlah mata uang.
     */
    private TableCell<TransactionModel, Double> createCurrencyCell() {
        // Formatter mata uang untuk lokal Indonesia (IDR)
        final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(localeDenganBuilder);
        return new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : CURRENCY_FORMATTER.format(item));
            }
        };
    }
}