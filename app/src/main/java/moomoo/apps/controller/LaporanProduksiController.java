package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import moomoo.apps.interfaces.ILaporanKontenController;
import moomoo.apps.model.ProductionModel;
import moomoo.apps.model.ProductionRecord;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller untuk mengelola dan menampilkan laporan data produksi.
 * Mencakup statistik ringkas, grafik tren harian, distribusi kategori, dan detail dalam tabel.
 */

public class LaporanProduksiController implements ILaporanKontenController {

    // --- Konstanta untuk String Literal ---
    private static final String PERIODE_BULAN_INI = "Bulan Ini";
    private static final String PERIODE_MINGGU_INI = "Minggu Ini";
    private static final String PERIODE_HARI_INI = "Hari Ini";
    private static final String PERIODE_TAHUN_INI = "Tahun Ini";

    private static final String KATEGORI_SUSU = "Susu";
    private static final String KATEGORI_DAGING = "Daging";
    private static final String SATUAN_LITER = "Liter";
    private static final String KUALITAS_NA = "N/A";

    // Kategori utama yang akan ditampilkan pada Line Chart
    private static final List<String> KATEGORI_UTAMA_CHART = List.of(KATEGORI_SUSU, KATEGORI_DAGING);

    // Formatter untuk tanggal
    private static final DateTimeFormatter CHART_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Komponen FXML ---
    @FXML private Label totalProduksiLabel;
    @FXML private Label totalProduksiDescLabel;
    @FXML private Label rataRataHarianLabel;
    @FXML private Label rataRataHarianDescLabel;
    @FXML private Label peringkatKualitasLabel;
    @FXML private Label peringkatKualitasDescLabel;
    @FXML private Label efisiensiOperasionalLabel;
    @FXML private Label efisiensiOperasionalDescLabel;
    @FXML private LineChart<String, Number> produksiHarianLineChart;
    @FXML private CategoryAxis produksiHarianXAxis;
    @FXML private NumberAxis produksiHarianYAxis;
    @FXML private PieChart produksiKategoriPieChart;
    @FXML private TableView<ProductionRecord> detailProduksiTableView;
    @FXML private TableColumn<ProductionRecord, String> tanggalTabelCol;
    @FXML private TableColumn<ProductionRecord, String> kategoriTabelCol;
    @FXML private TableColumn<ProductionRecord, String> volumeTabelCol;
    @FXML private TableColumn<ProductionRecord, String> satuanTabelCol;
    @FXML private TableColumn<ProductionRecord, String> lokasiTabelCol;
    @FXML private TableColumn<ProductionRecord, String> kualitasTabelCol;
    @FXML private TableColumn<ProductionRecord, String> catatanTabelCol;
    @FXML private Label infoPaginasiTabelLabel;

    // --- Properti Kelas ---
    private ProductionModel productionModel;
    private final ObservableList<ProductionRecord> filteredProductionList = FXCollections.observableArrayList();
    private String currentPeriodeFilter = PERIODE_BULAN_INI;

    /**
     * {@inheritDoc}
     */
    @Override
    public void inisialisasiKonten() {
        this.productionModel = ProductionModel.getInstance();
        setupTableColumns();
        setupChartStyling();

        productionModel.getAllProductionData().addListener((ListChangeListener<ProductionRecord>) c -> {
            LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
            filterAndDisplayData(dateRange[0], dateRange[1]);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terapkanFilterPeriode(String periode) {
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? PERIODE_BULAN_INI : periode;
        LocalDate[] dateRange = determineDateRange(this.currentPeriodeFilter);
        filterAndDisplayData(dateRange[0], dateRange[1]);
    }

    /**
     * Mengatur koneksi antara kolom-kolom tabel dengan properti dari model ProductionRecord.
     */
    private void setupTableColumns() {
        tanggalTabelCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTanggal();
            return new SimpleStringProperty(date != null ? TABLE_DATE_FORMATTER.format(date) : "");
        });

        kategoriTabelCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        volumeTabelCol.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        satuanTabelCol.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        lokasiTabelCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        kualitasTabelCol.setCellValueFactory(new PropertyValueFactory<>("kualitas"));
        catatanTabelCol.setCellValueFactory(new PropertyValueFactory<>("catatan"));
    }

    private void setupChartStyling() {

    }

    /**
     * Menentukan rentang tanggal (mulai dan selesai) berdasarkan string filter periode.
     * @param periodeFilter String filter seperti "Hari Ini", "Minggu Ini", dll.
     * @return Array LocalDate dengan dua elemen: [0] = tanggal mulai, [1] = tanggal selesai.
     */
    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        LocalDate today = LocalDate.now();
        YearMonth currentYMonth = YearMonth.from(today);

        switch (periodeFilter) {
            case PERIODE_MINGGU_INI:
                startDate = today.with(java.time.DayOfWeek.MONDAY);
                endDate = today.with(java.time.DayOfWeek.SUNDAY);
                break;
            case PERIODE_HARI_INI:
                startDate = today;
                endDate = today;
                break;
            case PERIODE_TAHUN_INI:
                startDate = currentYMonth.withMonth(1).atDay(1);
                endDate = currentYMonth.withMonth(12).atEndOfMonth();
                break;
            case PERIODE_BULAN_INI:
            default:
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }

    /**
     * Pusat orkestrasi: memfilter data master berdasarkan rentang tanggal, lalu memicu pembaruan semua komponen UI.
     * @param startDate Tanggal awal periode filter.
     * @param endDate   Tanggal akhir periode filter.
     */
    private void filterAndDisplayData(LocalDate startDate, LocalDate endDate) {
        if (productionModel == null) {
            return;
        }

        List<ProductionRecord> dataDalamRentang = productionModel.getAllProductionData().stream()
                .filter(record -> record.getTanggal() != null &&
                        !record.getTanggal().isBefore(startDate) &&
                        !record.getTanggal().isAfter(endDate))
                .sorted(Comparator.comparing(ProductionRecord::getTanggal))
                .collect(Collectors.toList());

        filteredProductionList.setAll(dataDalamRentang);

    
        updateSummaryCards();
        updateProduksiHarianLineChart();
        updateProduksiKategoriPieChart();
        updateDetailProduksiTable();
    }

    /**
     * Memperbarui kartu-kartu statistik di bagian atas layar (total produksi, rata-rata, dll).
     */
    private void updateSummaryCards() {
       
        double totalProduksiSusu = filteredProductionList.stream()
                .filter(r -> KATEGORI_SUSU.equalsIgnoreCase(r.getKategori()) && SATUAN_LITER.equalsIgnoreCase(r.getSatuan()))
                .mapToDouble(ProductionRecord::getJumlah)
                .sum();
        totalProduksiLabel.setText(String.format("%.1f L", totalProduksiSusu));
        totalProduksiDescLabel.setText(currentPeriodeFilter.toLowerCase());


        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        long daysInRange = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]) + 1;
        double rataRataSusuHarian = (daysInRange > 0 && totalProduksiSusu > 0) ? totalProduksiSusu / daysInRange : 0;
        rataRataHarianLabel.setText(String.format("%.1f L", rataRataSusuHarian));
        rataRataHarianDescLabel.setText(currentPeriodeFilter.toLowerCase());

        String dominanKualitas = filteredProductionList.stream()
                .filter(r -> KATEGORI_SUSU.equalsIgnoreCase(r.getKategori()) && r.getKualitas() != null && !r.getKualitas().isEmpty())
                .collect(Collectors.groupingBy(ProductionRecord::getKualitas, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(KUALITAS_NA);
        peringkatKualitasLabel.setText(dominanKualitas);
        peringkatKualitasDescLabel.setText(currentPeriodeFilter.toLowerCase());

        efisiensiOperasionalLabel.setText("-");
    }

    /**
     * Memperbarui LineChart untuk menampilkan tren produksi harian untuk kategori utama.
     */
    private void updateProduksiHarianLineChart() {
        produksiHarianLineChart.getData().clear();
        if (filteredProductionList.isEmpty()) return;

        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        KATEGORI_UTAMA_CHART.forEach(kategori -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(kategori);
            seriesMap.put(kategori, series);
        });
        
        Map<LocalDate, Map<String, Double>> dailyProductionByCategory = filteredProductionList.stream()
                .filter(r -> KATEGORI_UTAMA_CHART.contains(r.getKategori()))
                .collect(Collectors.groupingBy(
                        ProductionRecord::getTanggal,
                        Collectors.groupingBy(
                                ProductionRecord::getKategori,
                                Collectors.summingDouble(ProductionRecord::getJumlah)
                        )
                ));

        dailyProductionByCategory.keySet().stream().sorted().forEach(date -> {
            String formattedDate = date.format(CHART_DATE_FORMATTER);
            Map<String, Double> categoriesOnDate = dailyProductionByCategory.get(date);
        
            KATEGORI_UTAMA_CHART.forEach(kategori -> {
                double jumlah = categoriesOnDate.getOrDefault(kategori, 0.0);
                seriesMap.get(kategori).getData().add(new XYChart.Data<>(formattedDate, jumlah));
            });
        });

        produksiHarianLineChart.getData().addAll(seriesMap.values());
    }

    /**
     * Memperbarui PieChart untuk menampilkan distribusi total produksi berdasarkan kategori.
     */
    private void updateProduksiKategoriPieChart() {
        produksiKategoriPieChart.getData().clear();
        if (filteredProductionList.isEmpty()) return;

        // Agregasi total produksi per kategori
        Map<String, Double> produksiPerKategori = filteredProductionList.stream()
                .collect(Collectors.groupingBy(
                        ProductionRecord::getKategori,
                        Collectors.summingDouble(ProductionRecord::getJumlah)
                ));

        // Buat data untuk PieChart
        ObservableList<PieChart.Data> pieChartData = produksiPerKategori.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        produksiKategoriPieChart.setData(pieChartData);

        pieChartData.forEach(data -> {
            String tooltipText = String.format("Jumlah: %.1f", data.getPieValue());
            Tooltip.install(data.getNode(), new Tooltip(tooltipText));
        });
    }

    /**
     * Memperbarui TableView dengan data produksi yang telah difilter.
     */
    private void updateDetailProduksiTable() {
        detailProduksiTableView.setItems(filteredProductionList);
        infoPaginasiTabelLabel.setText("Menampilkan " + filteredProductionList.size() + " catatan");
    }
}