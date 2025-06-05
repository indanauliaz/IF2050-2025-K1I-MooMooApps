package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // Pastikan import ini ada
import javafx.fxml.FXML;
import javafx.geometry.Pos; // Untuk Pos.CENTER pada placeholder

import javafx.scene.Node; // Untuk menyimpan referensi ke node UI
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import javafx.scene.layout.VBox; // Untuk menyimpan node tabel dan dynamicReportContentArea

import java.util.List;   // Untuk menyimpan daftar Node
import java.util.ArrayList; // Untuk implementasi List
import moomoo.apps.model.EmployeeModel;
import java.util.Map;
import java.util.Random; // Untuk variasi data dummy
import java.util.stream.Collectors;

public class LaporanSdmKontenController {

    //<editor-fold defaultstate="collapsed" desc="FXML Declarations">
    @FXML private Label totalKaryawanLabel;
    @FXML private ProgressBar totalKaryawanProgressBar;
    @FXML private Label totalKaryawanDescLabel;
    // @FXML private VBox dynamicReportContentArea;

    @FXML private Label tingkatKehadiranLabel;
    @FXML private ProgressBar kehadiranProgressBar;
    @FXML private Label tingkatKehadiranDescLabel;

    @FXML private Label produktivitasLabel;
    @FXML private ProgressBar produktivitasProgressBar;
    @FXML private Label produktivitasDescLabel;

    @FXML private Label biayaSdmLabel;
    @FXML private ProgressBar biayaSdmProgressBar;
    @FXML private Label biayaSdmDescLabel;

    @FXML private ComboBox<String> filterPeriodeComboBox;

    @FXML private Button produksiTabButton;
    @FXML private Button keuanganTabButton;
    @FXML private Button sdmTabButton;

    @FXML private Button filterHadirButton;
    @FXML private Button filterTerlambatButton;
    @FXML private Button filterAbsenButton;

    @FXML private BarChart<String, Number> kehadiranChart;
    @FXML private CategoryAxis kehadiranXAxis;
    @FXML private NumberAxis kehadiranYAxis;

    @FXML private PieChart distribusiKaryawanChart;

    @FXML private TableView<EmployeeModel> kinerjaTableView;
    @FXML private TableColumn<EmployeeModel, String> karyawanColumn;
    @FXML private TableColumn<EmployeeModel, String> departemenColumn;
    @FXML private TableColumn<EmployeeModel, String> kehadiranKolomTabel;
    @FXML private TableColumn<EmployeeModel, String> produktivitasKolomTabel;
    @FXML private TableColumn<EmployeeModel, String> peringkatColumn;

    @FXML private Label tableSummaryLabel;
    @FXML private Button sebelumnyaButton;
    @FXML private Button selanjutnyaButton;

    // Kontainer untuk konten dinamis (Statistik, Grafik, Tabel SDM)
    @FXML private VBox dynamicReportContentArea;
    //</editor-fold>

    private ObservableList<EmployeeModel> masterKaryawanList = FXCollections.observableArrayList();
    private Random random = new Random(); // Untuk data dummy yang bervariasi

    // Variabel untuk menyimpan Node UI SDM agar tidak perlu dibuat ulang terus-menerus
    private Node sdmStatistikNode;
    private Node sdmGrafikNode;
    private Node sdmTabelNode;
    private List<Node> sdmContentNodes;
    
    private String currentKehadiranFilter = "Absen"; // Default sesuai FXML
    private String currentPeriodeFilter = "Bulan Ini";

    @FXML
    public void initialize() {
        System.out.println("LaporanSdmKontenController initialized.");

        if (dynamicReportContentArea != null) {
            sdmContentNodes = new ArrayList<>(dynamicReportContentArea.getChildren());
        } else {
            sdmContentNodes = new ArrayList<>(); // Safety net
            System.err.println("ERROR: dynamicReportContentArea adalah null di initialize!");
        }

        setupTampilanAwal();
        setTabAktif(sdmTabButton); // Pastikan tab SDM aktif secara visual
        muatUlangSemuaDataSdm();   // Muat data untuk konten SDM yang sudah ada

        filterPeriodeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                currentPeriodeFilter = newVal;
                System.out.println("Periode dipilih: " + currentPeriodeFilter);
                if (sdmTabButton.getStyleClass().contains("tab-button-laporan-active")) {
                    muatUlangSemuaDataSdm();
                }
            }
        });
    }

    private void setupTampilanAwal() {
        setTabAktif(sdmTabButton);

        setFilterChartAktif(filterAbsenButton);

        filterPeriodeComboBox.setItems(FXCollections.observableArrayList("Bulan Ini", "Minggu Ini", "Hari Ini", "Tahun Ini"));
        filterPeriodeComboBox.setValue(currentPeriodeFilter);

        karyawanColumn.setCellValueFactory(cellData -> cellData.getValue().namaLengkapProperty());
        departemenColumn.setCellValueFactory(cellData -> cellData.getValue().departemenProperty());
        kehadiranKolomTabel.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.0f%%", cellData.getValue().getPersentaseKehadiran() * 100))
        );
        produktivitasKolomTabel.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.0f%%", cellData.getValue().getPersentaseProduktivitas() * 100))
        );
        peringkatColumn.setCellValueFactory(cellData -> cellData.getValue().peringkatKinerjaProperty());
        peringkatColumn.setCellFactory(column -> new TableCell<EmployeeModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("peringkat-a-plus", "peringkat-a", "peringkat-b", "peringkat-c", "peringkat-default");
                if (item != null && !empty) {
                    switch (item) {
                        case "A+": getStyleClass().add("peringkat-a-plus"); break;
                        case "A": getStyleClass().add("peringkat-a"); break;
                        case "B": getStyleClass().add("peringkat-b"); break;
                        case "C": getStyleClass().add("peringkat-c"); break;
                        default: getStyleClass().add("peringkat-default"); break;
                    }
                }
            }
        });
    }

    private void buatNodeKontenSdm() {
        // Asumsi dari FXML:
        // Kartu statistik ada di dalam HBox pertama di dynamicReportContentArea (jika struktur FXMLmu begitu)
        // Grafik ada di HBox kedua
        // Tabel ada di VBox ketiga
        // Jika FXML dynamicReportContentArea langsung berisi kartu, grafik, tabel, ambil children-nya
        // Untuk contoh ini, kita akan buat Node-nya secara manual (idealya diambil dari FXML yang sudah di-load)
        // Ini hanya contoh, cara terbaik adalah dynamicReportContentArea sudah berisi elemen-elemen ini dari FXML.
        // Lalu kita bisa show/hide atau clear/add elemen-elemen ini.

        // Karena elemen @FXML sudah di-inject, kita bisa merujuk ke parent mereka jika perlu
        // Namun, untuk contoh ini, kita anggap `dynamicReportContentArea` di FXML sudah berisi
        // HBox Statistik, HBox Grafik, dan VBox Tabel.
        // Saat controller di-load, elemen2 ini sudah ada di `dynamicReportContentArea`.
        // Kita hanya perlu me-refresh data mereka.

        // Untuk sekarang, mari kita fokus pada pemuatan data ke elemen yang sudah ada.
        muatUlangSemuaDataSdm();
    }
    
    // Menampilkan konten SDM di dynamicReportContentArea
    private void tampilkanKontenSdm() {
        if (dynamicReportContentArea != null && sdmContentNodes != null) {
            dynamicReportContentArea.getChildren().setAll(sdmContentNodes); // Pasang kembali elemen SDM
            muatUlangSemuaDataSdm(); // Muat/refresh datanya
        } else {
            System.err.println("ERROR: Gagal menampilkan konten SDM, dynamicReportContentArea atau sdmContentNodes null.");
        }
    }
    
    private void tampilkanPlaceholder(String pesan) {
        if (dynamicReportContentArea == null) return;
        dynamicReportContentArea.getChildren().clear();
        Label placeholder = new Label(pesan);
        placeholder.setStyle("-fx-font-size: 18px; -fx-text-fill: #777;");
        VBox container = new VBox(placeholder);
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(400); // Beri tinggi agar terlihat
        dynamicReportContentArea.getChildren().add(container);
    }

    private void muatUlangSemuaDataSdm() {
        System.out.println("Memuat ulang data SDM untuk periode: " + currentPeriodeFilter);
        // === INI BAGIAN DUMMY DATA, NANTI GANTI DENGAN LOGIKA DATABASE ===
        masterKaryawanList.clear();
        // Buat variasi data berdasarkan periode
        int baseTotalKaryawan = 10;
        if (currentPeriodeFilter.equals("Minggu Ini")) baseTotalKaryawan = 8;
        else if (currentPeriodeFilter.equals("Hari Ini")) baseTotalKaryawan = 5;
        else if (currentPeriodeFilter.equals("Tahun Ini")) baseTotalKaryawan = 15;

        for (int i = 1; i <= baseTotalKaryawan; i++) {
            String nama = "Karyawan " + periodeAbbrev(currentPeriodeFilter) + i;
            String[] departemenArr = {"Produksi", "Peternakan", "Keuangan", "Pemasaran", "Administrasi"};
            String dept = departemenArr[random.nextInt(departemenArr.length)];
            double kehadiran = 0.75 + (random.nextDouble() * 0.25); // Antara 0.75 dan 1.0
            double produktivitas = 0.70 + (random.nextDouble() * 0.30); // Antara 0.70 dan 1.0
            String[] peringkatArr = {"A+", "A", "B", "C"};
            String peringkat = peringkatArr[random.nextInt(peringkatArr.length)];
            masterKaryawanList.add(new EmployeeModel(i, nama, "Staff " + dept, dept, kehadiran, produktivitas, peringkat));
        }

        // 1. Update Statistik Utama
        totalKaryawanLabel.setText(String.valueOf(masterKaryawanList.size()));
        totalKaryawanProgressBar.setProgress( (double) masterKaryawanList.size() / 20.0); // Anggap max 20
        totalKaryawanDescLabel.setText("Total karyawan periode " + currentPeriodeFilter.toLowerCase());

        double avgKehadiran = masterKaryawanList.stream().mapToDouble(EmployeeModel::getPersentaseKehadiran).average().orElse(0.0);
        tingkatKehadiranLabel.setText(String.format("%.0f%%", avgKehadiran * 100));
        kehadiranProgressBar.setProgress(avgKehadiran);
        tingkatKehadiranDescLabel.setText(avgKehadiran > 0.9 ? "Sangat Baik" : (avgKehadiran > 0.8 ? "Baik" : "Perlu Ditingkatkan"));

        double avgProduktivitas = masterKaryawanList.stream().mapToDouble(EmployeeModel::getPersentaseProduktivitas).average().orElse(0.0);
        produktivitasLabel.setText(String.format("%.0f%%", avgProduktivitas * 100));
        produktivitasProgressBar.setProgress(avgProduktivitas);
        produktivitasDescLabel.setText(avgProduktivitas > 0.85 ? "Produktif" : (avgProduktivitas > 0.75 ? "Cukup" : "Tingkatkan"));

        biayaSdmLabel.setText("Rp " + (20 + random.nextInt(50)) + "." + random.nextInt(10) + " jt");
        biayaSdmProgressBar.setProgress(0.6 + random.nextDouble() * 0.3);
        biayaSdmDescLabel.setText("Perkiraan " + currentPeriodeFilter.toLowerCase());

        // 2. Populate BarChart Kehadiran
        populateKehadiranChart(currentKehadiranFilter);

        // 3. Populate PieChart Distribusi Karyawan
        Map<String, Long> distribusi = masterKaryawanList.stream()
                .collect(Collectors.groupingBy(EmployeeModel::getDepartemen, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        distribusi.forEach((dept, count) -> pieChartData.add(new PieChart.Data(dept + " (" + count + ")", count)));
        distribusiKaryawanChart.setData(pieChartData);

        // 4. Populate TableView Kinerja Karyawan
        kinerjaTableView.setItems(masterKaryawanList);
        tableSummaryLabel.setText("Menampilkan " + masterKaryawanList.size() + " dari " + masterKaryawanList.size() + " catatan");
    }
    
    private String periodeAbbrev(String periode) {
        if (periode.equals("Minggu Ini")) return "M";
        if (periode.equals("Hari Ini")) return "H";
        if (periode.equals("Tahun Ini")) return "T";
        return "B"; // Bulan Ini
    }

    private void populateKehadiranChart(String filterJenis) {
        System.out.println("Populate Kehadiran Chart dengan filter: " + filterJenis + " untuk periode: " + currentPeriodeFilter);
        kehadiranChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        
        int maxVal = 20; // Max karyawan
        if (currentPeriodeFilter.equals("Minggu Ini")) maxVal = 15;
        if (currentPeriodeFilter.equals("Hari Ini")) maxVal = 10;


        for (String day : days) {
            int value = 0;
            switch (filterJenis) {
                case "Hadir":
                    value = (int) ( (0.6 + random.nextDouble() * 0.4) * maxVal ); // 60-100% dari maxVal
                    break;
                case "Terlambat":
                    value = (int) ( (random.nextDouble() * 0.2) * maxVal ); // 0-20% dari maxVal
                    break;
                case "Absen":
                default: // Default ke Absen jika filter tidak dikenali
                    value = (int) ( (random.nextDouble() * 0.15) * maxVal ); // 0-15% dari maxVal
                    break;
            }
            // Tambahkan variasi berdasarkan periode juga jika perlu
             if (currentPeriodeFilter.equals("Hari Ini") && !day.equals("Senin")) { // Contoh: Hari ini hanya ada data Senin
                 // value = 0; // Atau jangan tambahkan data jika bukan hari ini
             }

            series.getData().add(new XYChart.Data<>(day, value));
        }
        kehadiranChart.getData().add(series);
    }
    
    // --- Event Handlers untuk Tabs ---
    @FXML
    private void handleSdmTabAction(ActionEvent event) {
        System.out.println("Tab SDM diklik.");
        setTabAktif((Button)event.getSource());
        tampilkanKontenSdm();
    }
    
    @FXML
    private void handleProduksiTabAction(ActionEvent event) {
        System.out.println("Tab Produksi diklik.");
        setTabAktif((Button)event.getSource());
        tampilkanPlaceholder("Konten Laporan Produksi (Belum Diimplementasikan)");
    }

    @FXML
    private void handleKeuanganTabAction(ActionEvent event) {
        System.out.println("Tab Keuangan diklik.");
        setTabAktif((Button)event.getSource());
        tampilkanPlaceholder("Konten Laporan Keuangan (Belum Diimplementasikan)");
    }
    
    private void setTabAktif(Button activeTab) {
        // Hapus style aktif dari semua tombol tab
        produksiTabButton.getStyleClass().remove("tab-button-laporan-active");
        keuanganTabButton.getStyleClass().remove("tab-button-laporan-active");
        sdmTabButton.getStyleClass().remove("tab-button-laporan-active");
        // Tambahkan style aktif ke tombol yang diklik
        activeTab.getStyleClass().add("tab-button-laporan-active");
    }

    // --- Event Handler untuk Filter Chart Kehadiran ---
    @FXML
    private void handleFilterChartButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        currentKehadiranFilter = clickedButton.getText(); // "Hadir", "Terlambat", atau "Absen"
        setFilterChartAktif(clickedButton);
        populateKehadiranChart(currentKehadiranFilter);
    }

    private void setFilterChartAktif(Button activeFilterButton) {
        filterHadirButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterTerlambatButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterAbsenButton.getStyleClass().remove("filter-chart-button-laporan-active");
        activeFilterButton.getStyleClass().add("filter-chart-button-laporan-active");
    }
}