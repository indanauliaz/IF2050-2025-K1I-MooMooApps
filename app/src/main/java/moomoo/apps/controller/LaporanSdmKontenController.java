package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import javafx.beans.property.SimpleStringProperty; // Untuk formatting di TableView
import moomoo.apps.model.EmployeeModel; // Pastikan path ini benar
import javafx.event.ActionEvent; // Untuk ActionEvent
import javafx.geometry.Side;      // Untuk LegendSide pada PieChart

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class LaporanSdmKontenController {

    //<editor-fold defaultstate="collapsed" desc="FXML Declarations">
    @FXML private Label totalKaryawanLabel;
    @FXML private ProgressBar totalKaryawanProgressBar; // Tambahkan fx:id ini di FXML jika belum
    @FXML private Label totalKaryawanDescLabel;

    @FXML private Label tingkatKehadiranLabel;
    @FXML private ProgressBar kehadiranProgressBar;
    @FXML private Label tingkatKehadiranDescLabel;

    @FXML private Label produktivitasLabel;
    @FXML private ProgressBar produktivitasProgressBar;
    @FXML private Label produktivitasDescLabel;

    @FXML private Label biayaSdmLabel;
    @FXML private ProgressBar biayaSdmProgressBar; // Tambahkan fx:id ini di FXML jika belum
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
    //</editor-fold>

    private ObservableList<EmployeeModel> masterKaryawanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("LaporanSdmKontenController initialized.");
        setupTampilanAwal();
        muatDataLaporanDummy(); // Nanti ganti dengan data asli
    }

    private void setupTampilanAwal() {
        // Atur style tombol SDM agar terlihat 'aktif'
        sdmTabButton.getStyleClass().add("tab-button-laporan-active");
        produksiTabButton.getStyleClass().remove("tab-button-laporan-active");
        keuanganTabButton.getStyleClass().remove("tab-button-laporan-active");

        // Atur filter chart (misalnya, "Absen" aktif by default sesuai FXML)
        filterAbsenButton.getStyleClass().add("filter-chart-button-laporan-active");

        // Setup filter periode
        filterPeriodeComboBox.setItems(FXCollections.observableArrayList("Bulan Ini", "Minggu Ini", "Hari Ini", "Tahun Ini"));
        filterPeriodeComboBox.setValue("Bulan Ini"); // Default

        // Setup kolom tabel untuk menggunakan EmployeeModel
        karyawanColumn.setCellValueFactory(cellData -> cellData.getValue().namaLengkapProperty());
        departemenColumn.setCellValueFactory(cellData -> cellData.getValue().departemenProperty());

        kehadiranKolomTabel.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.0f%%", cellData.getValue().getPersentaseKehadiran() * 100))
        );
        produktivitasKolomTabel.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.0f%%", cellData.getValue().getPersentaseProduktivitas() * 100))
        );
        peringkatColumn.setCellValueFactory(cellData -> cellData.getValue().peringkatKinerjaProperty());

        // Kustomisasi tampilan peringkat (warna teks)
        peringkatColumn.setCellFactory(column -> new TableCell<EmployeeModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : getString());
                setGraphic(null);
                // Hapus style class sebelumnya
                getStyleClass().removeAll("peringkat-a-plus", "peringkat-a", "peringkat-b", "peringkat-c", "peringkat-default");
                if (item != null && !empty) {
                    if ("A+".equals(item)) {
                        getStyleClass().add("peringkat-a-plus");
                    } else if ("A".equals(item)) {
                        getStyleClass().add("peringkat-a");
                    } else if ("B".equals(item)) {
                        getStyleClass().add("peringkat-b");
                    } else if ("C".equals(item)) {
                        getStyleClass().add("peringkat-c");
                    } else {
                        getStyleClass().add("peringkat-default");
                    }
                }
            }
            private String getString() {
                return getItem() == null ? "" : getItem().toString();
            }
        });

        // Atur agar grafik tidak menampilkan legend jika tidak perlu
        kehadiranChart.setLegendVisible(false);
        distribusiKaryawanChart.setLegendSide(Side.RIGHT);
        distribusiKaryawanChart.setLabelsVisible(false); // Sembunyikan label di slice
    }

    private void muatDataLaporanDummy() {
        // === INI BAGIAN DUMMY DATA, NANTI GANTI DENGAN LOGIKA DATABASE ===
        masterKaryawanList.clear();
        masterKaryawanList.addAll(
            new EmployeeModel(1, "Sam Wiryawan", "Manager Produksi", "Produksi", 1.0, 0.95, "A+"),
            new EmployeeModel(2, "Safira Anjani", "Supervisor Peternakan", "Peternakan", 0.95, 0.95, "A"),
            new EmployeeModel(3, "Salim Mahesa", "Staff Produksi", "Produksi", 0.98, 0.95, "A"),
            new EmployeeModel(4, "Sandra Dewi", "Pekerja Lapangan", "Peternakan", 0.90, 0.85, "B"),
            new EmployeeModel(5, "Sarah Larasati", "Akuntan Keuangan", "Keuangan", 1.0, 0.95, "A"),
            new EmployeeModel(6, "Budi Santoso", "Staff Pemasaran", "Pemasaran", 0.92, 0.90, "A"),
            new EmployeeModel(7, "Citra Lestari", "Dokter Hewan", "Peternakan", 0.97, 0.98, "A+"),
            new EmployeeModel(8, "Dewi Anggraini", "Admin Gudang", "Produksi", 0.88, 0.80, "C"),
            new EmployeeModel(9, "Eko Prasetyo", "Quality Control", "Produksi", 0.96, 0.92, "B"),
            new EmployeeModel(10, "Fani Wijaya", "Peternak Senior", "Peternakan", 0.93, 0.88, "B")
            // Tambahkan lebih banyak karyawan jika perlu untuk paginasi
        );

        // 1. Update Statistik Utama
        totalKaryawanLabel.setText(String.valueOf(masterKaryawanList.size()));
        totalKaryawanProgressBar.setProgress(1.0); // Anggap semua ditampilkan
        totalKaryawanDescLabel.setText("Total karyawan aktif"); // Sesuaikan deskripsi

        double avgKehadiran = masterKaryawanList.stream().mapToDouble(EmployeeModel::getPersentaseKehadiran).average().orElse(0.0);
        tingkatKehadiranLabel.setText(String.format("%.0f%%", avgKehadiran * 100));
        kehadiranProgressBar.setProgress(avgKehadiran);
        // Deskripsi perubahan bisa dibuat dinamis
        tingkatKehadiranDescLabel.setText(avgKehadiran > 0.9 ? "Sangat Baik" : "Perlu Ditingkatkan");


        double avgProduktivitas = masterKaryawanList.stream().mapToDouble(EmployeeModel::getPersentaseProduktivitas).average().orElse(0.0);
        produktivitasLabel.setText(String.format("%.0f%%", avgProduktivitas * 100));
        produktivitasProgressBar.setProgress(avgProduktivitas);
        produktivitasDescLabel.setText(avgProduktivitas > 0.85 ? "Produktif" : "Tingkatkan Lagi");

        // Biaya SDM (dummy)
        biayaSdmLabel.setText("Rp 45.5 jt");
        biayaSdmProgressBar.setProgress(0.75); // Contoh
        biayaSdmDescLabel.setText("Perkiraan bulan ini");

        // 2. Populate BarChart Kehadiran (Dummy)
        XYChart.Series<String, Number> seriesKehadiran = new XYChart.Series<>();
        seriesKehadiran.getData().add(new XYChart.Data<>("Senin", 18));
        seriesKehadiran.getData().add(new XYChart.Data<>("Selasa", 20));
        seriesKehadiran.getData().add(new XYChart.Data<>("Rabu", 15));
        seriesKehadiran.getData().add(new XYChart.Data<>("Kamis", 17));
        seriesKehadiran.getData().add(new XYChart.Data<>("Jumat", 14));
        seriesKehadiran.getData().add(new XYChart.Data<>("Sabtu", 10));
        kehadiranChart.getData().clear();
        kehadiranChart.getData().add(seriesKehadiran);

        // 3. Populate PieChart Distribusi Karyawan
        Map<String, Long> distribusi = masterKaryawanList.stream()
                .collect(Collectors.groupingBy(EmployeeModel::getDepartemen, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        distribusi.forEach((dept, count) -> pieChartData.add(new PieChart.Data(dept + " (" + count + ")", count)));
        distribusiKaryawanChart.setData(pieChartData);

        // 4. Populate TableView Kinerja Karyawan
        kinerjaTableView.setItems(masterKaryawanList); // Langsung gunakan master list jika tidak ada paginasi kompleks
        tableSummaryLabel.setText("Menampilkan " + masterKaryawanList.size() + " dari " + masterKaryawanList.size() + " catatan");

        // === AKHIR BAGIAN DUMMY DATA ===
    }

    // Event handler untuk tombol tab
    @FXML
    private void handleProduksiTabAction() {
        System.out.println("Tab Produksi diklik.");
        // Ganti style aktif
        produksiTabButton.getStyleClass().add("tab-button-laporan-active");
        keuanganTabButton.getStyleClass().remove("tab-button-laporan-active");
        sdmTabButton.getStyleClass().remove("tab-button-laporan-active");
        // TODO: Muat konten laporan produksi
    }

    @FXML
    private void handleKeuanganTabAction() {
        System.out.println("Tab Keuangan diklik.");
        // Ganti style aktif
        keuanganTabButton.getStyleClass().add("tab-button-laporan-active");
        produksiTabButton.getStyleClass().remove("tab-button-laporan-active");
        sdmTabButton.getStyleClass().remove("tab-button-laporan-active");
        // TODO: Muat konten laporan keuangan
    }

    // Tombol SDM tidak perlu action jika hanya untuk menandai view saat ini
    // Jika ada sub-laporan SDM lain, baru perlu action.

    // Event handler untuk filter chart (contoh)
    @FXML
    private void handleFilterChartButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        // Hapus style aktif dari semua tombol filter chart
        filterHadirButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterTerlambatButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterAbsenButton.getStyleClass().remove("filter-chart-button-laporan-active");
        // Tambahkan style aktif ke tombol yang diklik
        clickedButton.getStyleClass().add("filter-chart-button-laporan-active");

        // TODO: Implementasikan logika filter data chart berdasarkan tombol yang aktif
        if (clickedButton == filterHadirButton) {
            System.out.println("Filter Hadir dipilih");
        } else if (clickedButton == filterTerlambatButton) {
            System.out.println("Filter Terlambat dipilih");
        } else if (clickedButton == filterAbsenButton) {
            System.out.println("Filter Absen dipilih");
        }
    }
    
    // TODO: Tambahkan logika untuk ComboBox filterPeriodeComboBox jika diperlukan
    // TODO: Tambahkan logika untuk tombol paginasi sebelumnyaButton dan selanjutnyaButton
}