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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LaporanFinanceController implements ILaporanKontenController {

    //<editor-fold defaultstate="collapsed" desc="FXML Declarations">
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
    private String currentPeriodeFilter = "Bulan Ini";
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
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? "Bulan Ini" : periode;
        muatUlangDataLaporan();
    }
    
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
                getStyleClass().removeAll("text-green", "text-red", "text-orange");
                if (item != null && !empty) {
                    switch (item.toLowerCase()) {
                        case "pemasukan":
                            getStyleClass().add("text-green");
                            break;
                        case "pengeluaran":
                            getStyleClass().add("text-red");
                            break;
                        case "penggajian":
                            getStyleClass().add("text-orange"); 
                            break;
                    }
                }
            }
        });
    }

    private void updateKartuStatistik(List<TransactionModel> transactions) {
        double pemasukan = transactions.stream()
                .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()))
                .mapToDouble(TransactionModel::getAmount).sum();

        double pengeluaran = transactions.stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()) || "Penggajian".equalsIgnoreCase(t.getTransactionType()))
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

    private void updateGrafikKeuangan(List<TransactionModel> transactions) {
        keuanganChart.getData().clear();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        Map<Integer, Double> pemasukanPerMinggu = transactions.stream()
                .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(t -> t.getDate().get(weekFields.weekOfMonth()), Collectors.summingDouble(TransactionModel::getAmount)));

        // --- REVISI DI SINI ---
        // Untuk grafik, "Pengeluaran" dan "Penggajian" digabung menjadi satu bar "Pengeluaran" agar sesuai gambar
        Map<Integer, Double> pengeluaranGabunganPerMinggu = transactions.stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()) || "Penggajian".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(t -> t.getDate().get(weekFields.weekOfMonth()), Collectors.summingDouble(TransactionModel::getAmount)));

        XYChart.Series<String, Number> pemasukanSeries = new XYChart.Series<>();
        pemasukanSeries.setName("Pemasukan");
        XYChart.Series<String, Number> pengeluaranSeries = new XYChart.Series<>();
        pengeluaranSeries.setName("Pengeluaran");

        for (int i = 1; i <= 5; i++) {
            String weekLabel = "Minggu ke-" + i;
            pemasukanSeries.getData().add(new XYChart.Data<>(weekLabel, pemasukanPerMinggu.getOrDefault(i, 0.0)));
            pengeluaranSeries.getData().add(new XYChart.Data<>(weekLabel, pengeluaranGabunganPerMinggu.getOrDefault(i, 0.0)));
        }
        
        keuanganChart.getData().addAll(pemasukanSeries, pengeluaranSeries);
    }

    private void updateGrafikDistribusi(List<TransactionModel> transactions) {
        distribusiPengeluaranChart.getData().clear();

        // --- REVISI DI SINI ---
        // Distribusi juga harus mencakup kategori dari "Pengeluaran" dan "Penggajian"
        Map<String, Double> distribusi = transactions.stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()) || "Penggajian".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.groupingBy(TransactionModel::getCategory, Collectors.summingDouble(TransactionModel::getAmount)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        distribusi.forEach((kategori, jumlah) -> pieChartData.add(new PieChart.Data(kategori, jumlah)));
        
        distribusiPengeluaranChart.setData(pieChartData);
        distribusiPengeluaranChart.setTitle("Distribusi Pengeluaran");
    }
    
    private void updateTabelTransaksi(List<TransactionModel> transactions) {
        transaksiTableView.setItems(FXCollections.observableArrayList(transactions));
        tableSummaryLabel.setText("Menampilkan " + transactions.size() + " dari " + transactions.size() + " catatan");
    }

    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        YearMonth currentYMonth = YearMonth.now();
        switch (periodeFilter) {
            case "Minggu Ini":
                startDate = LocalDate.now().with(DayOfWeek.MONDAY);
                endDate = LocalDate.now().with(DayOfWeek.SUNDAY);
                break;
            case "Hari Ini":
                startDate = LocalDate.now();
                endDate = LocalDate.now();
                break;
            case "Tahun Ini":
                startDate = currentYMonth.withMonth(1).atDay(1);
                endDate = currentYMonth.withMonth(12).atEndOfMonth();
                break;
            default: // Bulan Ini
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }
}