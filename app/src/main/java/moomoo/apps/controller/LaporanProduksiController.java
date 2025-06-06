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
import javafx.scene.control.cell.PropertyValueFactory;
import moomoo.apps.interfaces.ILaporanKontenController;
import moomoo.apps.model.ProductionModel;
import moomoo.apps.model.ProductionRecord;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LaporanProduksiController implements ILaporanKontenController {


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


    private ProductionModel productionModel;
    private ObservableList<ProductionRecord> filteredProductionList = FXCollections.observableArrayList();
    private String currentPeriodeFilter = "Bulan Ini"; 

    private static final DateTimeFormatter CHART_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ITEMS_PER_PAGE_TABLE = 10; 


    @Override
    public void inisialisasiKonten() {
        System.out.println("LaporanProduksiController: inisialisasiKonten dipanggil.");
        this.productionModel = ProductionModel.getInstance(); 

        setupTableColumns();
        setupChartStyling(); 

        productionModel.getAllProductionData().addListener((ListChangeListener<ProductionRecord>) c -> {
            System.out.println("LaporanProduksiController: Perubahan terdeteksi pada data produksi master.");
   
            filterAndDisplayData(determineDateRange(currentPeriodeFilter)[0], determineDateRange(currentPeriodeFilter)[1]);
        });

    }

    @Override
    public void terapkanFilterPeriode(String periode) {
        if (periode == null || periode.isEmpty()) {
            System.err.println("LaporanProduksiController: Filter periode tidak valid (null atau kosong). Menggunakan default.");
            this.currentPeriodeFilter = "Bulan Ini"; // Fallback
        } else {
            this.currentPeriodeFilter = periode;
        }
        System.out.println("LaporanProduksiController: Menerapkan filter periode - " + this.currentPeriodeFilter);

        LocalDate[] dateRange = determineDateRange(this.currentPeriodeFilter);
        filterAndDisplayData(dateRange[0], dateRange[1]);
    }

    private void setupTableColumns() {
        tanggalTabelCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTanggal() != null ? 
                TABLE_DATE_FORMATTER.format(cellData.getValue().getTanggal()) : ""
            )
        );
        kategoriTabelCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        volumeTabelCol.setCellValueFactory(new PropertyValueFactory<>("jumlah")); // Langsung ambil angka
        satuanTabelCol.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        lokasiTabelCol.setCellValueFactory(new PropertyValueFactory<>("lokasi"));
        kualitasTabelCol.setCellValueFactory(new PropertyValueFactory<>("kualitas"));
        catatanTabelCol.setCellValueFactory(new PropertyValueFactory<>("catatan"));
    }

    private void setupChartStyling() {

    }

    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        YearMonth currentYMonth = YearMonth.now();

        switch (periodeFilter) {
            case "Minggu Ini":
                startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                endDate = LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
                break;
            case "Hari Ini":
                startDate = LocalDate.now();
                endDate = LocalDate.now();
                break;
            case "Tahun Ini":
                startDate = currentYMonth.withMonth(1).atDay(1);
                endDate = currentYMonth.withMonth(12).atEndOfMonth();
                break;
            case "Bulan Ini":
            default:
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }

    private void filterAndDisplayData(LocalDate startDate, LocalDate endDate) {
        if (productionModel == null) {
            System.err.println("ProductionModel belum diinisialisasi di LaporanProduksiController.");
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

    private void updateSummaryCards() {
        double totalProduksiSusu = 0; 
        Map<String, Double> totalPerKategoriDanSatuan = new HashMap<>();

        for (ProductionRecord record : filteredProductionList) {
            if ("Susu".equalsIgnoreCase(record.getKategori()) && "Liter".equalsIgnoreCase(record.getSatuan())) {
                totalProduksiSusu += record.getJumlah();
            }

            String key = record.getKategori() + " (" + record.getSatuan() + ")";
            totalPerKategoriDanSatuan.merge(key, record.getJumlah(), Double::sum);
        }

        totalProduksiLabel.setText(String.format("%.1f L", totalProduksiSusu));
        totalProduksiDescLabel.setText(currentPeriodeFilter.toLowerCase());

        long daysInRange = 0;
        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        if (dateRange[0] != null && dateRange[1] != null) {
             daysInRange = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]) + 1;
        }

        double rataRataSusuHarian = (daysInRange > 0 && totalProduksiSusu > 0) ? totalProduksiSusu / daysInRange : 0;
        rataRataHarianLabel.setText(String.format("%.1f L", rataRataSusuHarian));
        rataRataHarianDescLabel.setText(currentPeriodeFilter.toLowerCase());

        String dominanKualitas = filteredProductionList.stream()
            .filter(r -> "Susu".equalsIgnoreCase(r.getKategori()) && r.getKualitas() != null && !r.getKualitas().isEmpty())
            .collect(Collectors.groupingBy(ProductionRecord::getKualitas, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        peringkatKualitasLabel.setText(dominanKualitas);
        peringkatKualitasDescLabel.setText(currentPeriodeFilter.toLowerCase());

        // INI FUNGSIONALITAS YANG BELUM DICEK(LEBIH KE BINGUNG, BISA DIAPUS KALO GAPERLU)
        efisiensiOperasionalLabel.setText("87%"); 
        efisiensiOperasionalDescLabel.setText("Target: 90%"); 
    }

    private void updateProduksiHarianLineChart() {
        produksiHarianLineChart.getData().clear();
        if (filteredProductionList.isEmpty()) return;

        // Agregasi produksi harian per kategori (misal: Susu, Daging)
        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

        // Kategori utama yang ingin ditampilkan di line chart
        List<String> kategoriUtama = List.of("Susu", "Daging"); 

        for (String kategori : kategoriUtama) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(kategori);
            seriesMap.put(kategori, series);
        }
        
        Map<LocalDate, Map<String, Double>> dailyProductionByCategory = filteredProductionList.stream()
            .filter(r -> kategoriUtama.contains(r.getKategori()))
            .collect(Collectors.groupingBy(
                ProductionRecord::getTanggal,
                Collectors.groupingBy(
                    ProductionRecord::getKategori,
                    Collectors.summingDouble(ProductionRecord::getJumlah)
                )
            ));

        List<LocalDate> sortedDates = filteredProductionList.stream()
                                        .map(ProductionRecord::getTanggal)
                                        .distinct()
                                        .sorted()
                                        .collect(Collectors.toList());

        for (LocalDate date : sortedDates) {
            Map<String, Double> categoriesOnDate = dailyProductionByCategory.getOrDefault(date, new HashMap<>());
            for (String kategori : kategoriUtama) {
                XYChart.Series<String, Number> series = seriesMap.get(kategori);
                double jumlah = categoriesOnDate.getOrDefault(kategori, 0.0);
                series.getData().add(new XYChart.Data<>(date.format(CHART_DATE_FORMATTER), jumlah));
            }
        }
        
        produksiHarianLineChart.getData().addAll(seriesMap.values());
    }

    private void updateProduksiKategoriPieChart() {
        produksiKategoriPieChart.getData().clear();
        if (filteredProductionList.isEmpty()) return;

        Map<String, Double> produksiPerKategori = filteredProductionList.stream()
            .collect(Collectors.groupingBy(
                ProductionRecord::getKategori,
                Collectors.summingDouble(ProductionRecord::getJumlah)
            ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        double totalProduksiKeseluruhan = produksiPerKategori.values().stream().mapToDouble(Double::doubleValue).sum();

        produksiPerKategori.forEach((kategori, total) -> {
            double percentage = (totalProduksiKeseluruhan > 0) ? (total / totalProduksiKeseluruhan) * 100 : 0;
            String label = String.format("%s (%.1f%%)", kategori, percentage);
            pieChartData.add(new PieChart.Data(label, total));
        });

        produksiKategoriPieChart.setData(pieChartData);
        // Tooltip sederhana
        pieChartData.forEach(data -> 
            javafx.scene.control.Tooltip.install(data.getNode(), 
            new javafx.scene.control.Tooltip(data.getName() + "\nJumlah: " + String.format("%.1f", data.getPieValue())))
        );
    }

    private void updateDetailProduksiTable() {
        detailProduksiTableView.setItems(filteredProductionList);
        infoPaginasiTabelLabel.setText("Menampilkan " + filteredProductionList.size() + " dari " + filteredProductionList.size() + " catatan");
    }
}