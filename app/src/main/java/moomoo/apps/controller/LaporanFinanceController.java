package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import moomoo.apps.interfaces.ILaporanKontenController;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.TransactionModel;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller untuk mengelola tampilan laporan keuangan.
 * Bertanggung jawab untuk menampilkan statistik, grafik, dan tabel transaksi
 * berdasarkan periode waktu yang dipilih.
 */
public class LaporanFinanceController implements ILaporanKontenController {

    // --- Konstanta untuk menggantikan "Magic Strings" ---
    private static final String TIPE_PEMASUKAN = "Pemasukan";
    private static final String TIPE_PENGELUARAN = "Pengeluaran";
    private static final String TIPE_PENGGAJIAN = "Penggajian";
    private static final String PERIODE_DEFAULT = "Bulan Ini";
    private static final String PERIODE_MINGGU_INI = "Minggu Ini";
    private static final String PERIODE_HARI_INI = "Hari Ini";
    private static final String PERIODE_TAHUN_INI = "Tahun Ini";
    private static final String STYLE_TEXT_GREEN = "text-green";
    private static final String STYLE_TEXT_RED = "text-red";
    private static final String STYLE_TEXT_ORANGE = "text-orange";

    // --- Komponen FXML ---
    @FXML private Label totalPemasukanLabel;
    @FXML private Label pemasukanPercentageLabel;
    @FXML private Label totalPengeluaranLabel;
    @FXML private Label pengeluaranPercentageLabel;
    @FXML private Label labaBersihLabel;
    @FXML private Label labaPercentageLabel;
    @FXML private Label marginKeuntunganLabel;
    @FXML private Label marginDescLabel;
    @FXML private BarChart<String, Number> keuanganChart;
    @FXML private PieChart distribusiPengeluaranChart;
    @FXML private TableView<TransactionModel> transaksiTableView;
    @FXML private TableColumn<TransactionModel, String> tanggalCol;
    @FXML private TableColumn<TransactionModel, String> deskripsiCol;
    @FXML private TableColumn<TransactionModel, String> jumlahCol;
    @FXML private TableColumn<TransactionModel, String> tipeCol;
    @FXML private TableColumn<TransactionModel, String> kategoriCol;
    @FXML private Label tableSummaryLabel;
    @FXML private Button sebelumnyaButton;
    @FXML private Button selanjutnyaButton;

    private FinanceModel financeModel;
    private String currentPeriodeFilter = PERIODE_DEFAULT;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void inisialisasiKonten() {
        this.financeModel = FinanceModel.getInstance();
        setupTampilanAwal();
        financeModel.getAllTransactions().addListener((ListChangeListener<TransactionModel>) c -> muatUlangDataLaporan());
        muatUlangDataLaporan();
    }

    @Override
    public void terapkanFilterPeriode(String periode) {
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? PERIODE_DEFAULT : periode;
        muatUlangDataLaporan();
    }

    /**
     * Memuat ulang semua data visual (kartu, grafik, tabel) berdasarkan filter periode saat ini.
     * Metode ini menjadi pusat orkestrasi pembaruan UI.
     */
    private void muatUlangDataLaporan() {
        if (financeModel == null) return;
        
        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        List<TransactionModel> filteredData = financeModel.getAllTransactions().stream()
                .filter(t -> !t.getDate().isBefore(dateRange[0]) && !t.getDate().isAfter(dateRange[1]))
                .collect(Collectors.toList());

        updateKartuStatistik(filteredData);
        updateGrafikKeuangan(filteredData);
        updateGrafikDistribusi(filteredData);
        updateTabelTransaksi(filteredData);
    }

    /**
     * Mengatur konfigurasi awal untuk komponen UI seperti format mata uang dan kolom tabel.
     */
    private void setupTampilanAwal() {
        currencyFormatter.setMaximumFractionDigits(0);

        tanggalCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate().toString()));
        deskripsiCol.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        jumlahCol.setCellValueFactory(cell -> new SimpleStringProperty(currencyFormatter.format(cell.getValue().getAmount())));
        tipeCol.setCellValueFactory(cell -> cell.getValue().transactionTypeProperty());
        kategoriCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());
        
        tipeCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);

                getStyleClass().removeAll(STYLE_TEXT_GREEN, STYLE_TEXT_RED, STYLE_TEXT_ORANGE);
                if (item != null && !empty) {
                    switch (item) {
                        case TIPE_PEMASUKAN:
                            getStyleClass().add(STYLE_TEXT_GREEN);
                            break;
                        case TIPE_PENGELUARAN:
                            getStyleClass().add(STYLE_TEXT_RED);
                            break;
                        case TIPE_PENGGAJIAN:
                            getStyleClass().add(STYLE_TEXT_ORANGE);
                            break;
                    }
                }
            }
        });
    }

    /**
     * Memperbarui kartu statistik utama: total pemasukan, pengeluaran, laba bersih, dan margin.
     * @param transactions Daftar transaksi yang telah difilter sesuai periode.
     */
    private void updateKartuStatistik(List<TransactionModel> transactions) {
        double pemasukan = transactions.stream()
                .filter(t -> TIPE_PEMASUKAN.equalsIgnoreCase(t.getTransactionType()))
                .mapToDouble(TransactionModel::getAmount).sum();

        double pengeluaran = transactions.stream()
                .filter(t -> TIPE_PENGELUARAN.equalsIgnoreCase(t.getTransactionType()) || TIPE_PENGGAJIAN.equalsIgnoreCase(t.getTransactionType()))
                .mapToDouble(TransactionModel::getAmount).sum();
        
        double laba = pemasukan - pengeluaran;
        double margin = (pemasukan > 0) ? (laba / pemasukan) * 100 : 0;
        
        totalPemasukanLabel.setText(currencyFormatter.format(pemasukan));
        totalPengeluaranLabel.setText(currencyFormatter.format(pengeluaran));
        labaBersihLabel.setText(currencyFormatter.format(laba));
        marginKeuntunganLabel.setText(String.format("%.1f%%", margin));

        pemasukanPercentageLabel.setText("Periode " + currentPeriodeFilter);
        pengeluaranPercentageLabel.setText("Periode " + currentPeriodeFilter);
        labaPercentageLabel.setText("Periode " + currentPeriodeFilter);
    }

    /**
     * Memperbarui BarChart keuangan untuk menunjukkan total pemasukan dan pengeluaran per minggu.
     * @param transactions Daftar transaksi yang telah difilter sesuai periode.
     */
    private void updateGrafikKeuangan(List<TransactionModel> transactions) {
        keuanganChart.getData().clear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        Map<Integer, Double> pemasukanPerMinggu = transactions.stream()
                .filter(t -> TIPE_PEMASUKAN.equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(t -> t.getDate().get(weekFields.weekOfMonth()), Collectors.summingDouble(TransactionModel::getAmount)));

        Map<Integer, Double> pengeluaranGabunganPerMinggu = transactions.stream()
                .filter(t -> TIPE_PENGELUARAN.equalsIgnoreCase(t.getTransactionType()) || TIPE_PENGGAJIAN.equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(t -> t.getDate().get(weekFields.weekOfMonth()), Collectors.summingDouble(TransactionModel::getAmount)));

        XYChart.Series<String, Number> pemasukanSeries = new XYChart.Series<>();
        pemasukanSeries.setName(TIPE_PEMASUKAN);
        XYChart.Series<String, Number> pengeluaranSeries = new XYChart.Series<>();
        pengeluaranSeries.setName(TIPE_PENGELUARAN); 

        int maxWeek = pengeluaranGabunganPerMinggu.keySet().stream().max(Comparator.naturalOrder()).orElse(
                pemasukanPerMinggu.keySet().stream().max(Comparator.naturalOrder()).orElse(0)
        );

        for (int i = 1; i <= maxWeek; i++) {
            String weekLabel = "Minggu ke-" + i;
            pemasukanSeries.getData().add(new XYChart.Data<>(weekLabel, pemasukanPerMinggu.getOrDefault(i, 0.0)));
            pengeluaranSeries.getData().add(new XYChart.Data<>(weekLabel, pengeluaranGabunganPerMinggu.getOrDefault(i, 0.0)));
        }
        
        keuanganChart.getData().addAll(pemasukanSeries, pengeluaranSeries);
    }

    /**
     * Memperbarui PieChart untuk menunjukkan distribusi pengeluaran berdasarkan kategori.
     * @param transactions Daftar transaksi yang telah difilter sesuai periode.
     */
    private void updateGrafikDistribusi(List<TransactionModel> transactions) {
        distribusiPengeluaranChart.getData().clear();

        Map<String, Double> distribusi = transactions.stream()
                .filter(t -> TIPE_PENGELUARAN.equalsIgnoreCase(t.getTransactionType()) || TIPE_PENGGAJIAN.equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(TransactionModel::getCategory, Collectors.summingDouble(TransactionModel::getAmount)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        distribusi.forEach((kategori, jumlah) -> pieChartData.add(new PieChart.Data(kategori, jumlah)));
        
        distribusiPengeluaranChart.setData(pieChartData);
        distribusiPengeluaranChart.setTitle("Distribusi Pengeluaran");
    }
    
    /**
     * Memperbarui TableView dengan daftar transaksi yang relevan dan ringkasannya.
     * @param transactions Daftar transaksi untuk ditampilkan.
     */
    private void updateTabelTransaksi(List<TransactionModel> transactions) {
        transaksiTableView.setItems(FXCollections.observableArrayList(transactions));
        tableSummaryLabel.setText("Menampilkan " + transactions.size() + " dari " + transactions.size() + " catatan");
    }

    /**
     * Menentukan rentang tanggal (mulai dan selesai) berdasarkan string filter periode.
     * @param periodeFilter String filter seperti "Hari Ini", "Minggu Ini", dll.
     * @return Array LocalDate dengan dua elemen: [0] = tanggal mulai, [1] = tanggal selesai.
     */
    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        YearMonth currentYMonth = YearMonth.now();
        
        switch (periodeFilter) {
            case PERIODE_MINGGU_INI:
                startDate = LocalDate.now().with(DayOfWeek.MONDAY);
                endDate = LocalDate.now().with(DayOfWeek.SUNDAY);
                break;
            case PERIODE_HARI_INI:
                startDate = LocalDate.now();
                endDate = LocalDate.now();
                break;
            case PERIODE_TAHUN_INI:
                startDate = currentYMonth.withMonth(1).atDay(1);
                endDate = currentYMonth.withMonth(12).atEndOfMonth();
                break;
            default: 
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }
}