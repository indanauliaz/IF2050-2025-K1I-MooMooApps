package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import moomoo.apps.interfaces.ILaporanKontenController;
import moomoo.apps.model.AttendanceRecordModel;
import moomoo.apps.model.EmployeeModel;
import moomoo.apps.model.SdmModel;
import moomoo.apps.model.TaskModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller untuk mengelola dan menampilkan laporan terkait Sumber Daya Manusia (SDM).
 * Mencakup statistik karyawan, kehadiran, produktivitas, dan kinerja.
 */
public class LaporanSdmController implements ILaporanKontenController {

    // --- Konstanta untuk String Literal dan Konfigurasi ---
    private static final String PERIODE_DEFAULT = "Tahun Ini";
    private static final String PERIODE_MINGGU_INI = "Minggu Ini";
    private static final String PERIODE_HARI_INI = "Hari Ini";
    private static final String PERIODE_TAHUN_INI = "Tahun Ini";
    private static final String PERIODE_BULAN_INI = "Bulan Ini";

    private static final String STATUS_HADIR = "Hadir";
    private static final String STATUS_TERLAMBAT = "Terlambat";
    private static final String STATUS_ABSEN = "Absen";
    private static final String STATUS_TUGAS_SELESAI = "Selesai";

    private static final String PERINGKAT_A_PLUS = "A+";
    private static final String PERINGKAT_A = "A";
    private static final String PERINGKAT_B = "B";
    private static final String PERINGKAT_C = "C";

    // Bobot dan ambang batas untuk kalkulasi kinerja
    private static final double BOBOT_SKOR_KEHADIRAN = 0.6;
    private static final double BOBOT_SKOR_TUGAS = 0.4;
    private static final double AMBANG_BATAS_A_PLUS = 0.9;
    private static final double AMBANG_BATAS_A = 0.8;
    private static final double AMBANG_BATAS_B = 0.7;

    // Konstanta untuk styling
    private static final String STYLE_CLASS_FILTER_AKTIF = "filter-chart-button-laporan-active";
    private static final String STYLE_PERINGKAT_A = "-fx-text-fill: green; -fx-font-weight: bold;";
    private static final String STYLE_PERINGKAT_B = "-fx-text-fill: orange; -fx-font-weight: bold;";
    private static final String STYLE_PERINGKAT_C = "-fx-text-fill: red; -fx-font-weight: bold;";
    private static final String STYLE_PERINGKAT_DEFAULT = "-fx-text-fill: black;";
    
    private static final double TARGET_KARYAWAN_PROGRESS_BAR = 50.0;

    // --- Komponen FXML ---
    @FXML private Label totalKaryawanLabel;
    @FXML private ProgressBar totalKaryawanProgressBar;
    @FXML private Label totalKaryawanDescLabel;
    @FXML private Label tingkatKehadiranLabel;
    @FXML private ProgressBar kehadiranProgressBar;
    @FXML private Label tingkatKehadiranDescLabel;
    @FXML private Label produktivitasLabel;
    @FXML private ProgressBar produktivitasProgressBar;
    @FXML private Label produktivitasDescLabel;
    @FXML private Label biayaSdmLabel;
    @FXML private ProgressBar biayaSdmProgressBar;
    @FXML private Label biayaSdmDescLabel;
    @FXML private Button filterHadirButton;
    @FXML private Button filterTerlambatButton;
    @FXML private Button filterAbsenButton;
    @FXML private BarChart<String, Number> kehadiranChart;
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

    // --- Properti Kelas ---
    private SdmModel sdmModel;
    private String currentPeriodeFilter = PERIODE_DEFAULT;
    private String currentKehadiranChartFilter = STATUS_HADIR;
    private XYChart.Series<String, Number> kehadiranSeries;
    private final ObservableList<EmployeeModel> kinerjaData = FXCollections.observableArrayList();
    private final ObservableList<PieChart.Data> distribusiData = FXCollections.observableArrayList();

    @Override
    public void inisialisasiKonten() {
        this.sdmModel = SdmModel.getInstance();
        setupTampilanAwalSdm();
        sdmModel.getAllEmployees().addListener((ListChangeListener<EmployeeModel>) c -> muatUlangDataLaporan());
        muatUlangDataLaporan();
    }

    @Override
    public void terapkanFilterPeriode(String periode) {
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? PERIODE_DEFAULT : periode;
        muatUlangDataLaporan();
    }

    /**
     * Metode utama untuk memuat ulang dan memproses semua data laporan.
     * Mengambil data mentah, memfilternya, dan memanggil metode-metode update UI.
     */
    private void muatUlangDataLaporan() {
        if (sdmModel == null) return;

        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        ObservableList<EmployeeModel> semuaKaryawan = sdmModel.getAllEmployees();
        List<AttendanceRecordModel> semuaCatatanKehadiranDiPeriode = sdmModel.getAttendanceRecordsByDateRange(dateRange[0], dateRange[1]);
        List<TaskModel> semuaTugasDiPeriode = sdmModel.getTasksByDateRange(dateRange[0], dateRange[1]);

        List<AttendanceRecordModel> catatanKehadiranTersaring = semuaCatatanKehadiranDiPeriode.stream()
                .filter(r -> currentKehadiranChartFilter.equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.toList());

        List<Integer> employeeIdsTersaring = catatanKehadiranTersaring.stream()
                .map(r -> r.getKaryawan().getId())
                .distinct()
                .collect(Collectors.toList());
        List<EmployeeModel> karyawanTersaringUntukPieChart = semuaKaryawan.stream()
                .filter(emp -> employeeIdsTersaring.contains(emp.getId()))
                .collect(Collectors.toList());

        updateTabelDanKinerjaKaryawan(semuaKaryawan, semuaCatatanKehadiranDiPeriode, semuaTugasDiPeriode, dateRange);
        updateKartuStatistik(semuaKaryawan, semuaCatatanKehadiranDiPeriode);
        updateGrafikDistribusi(karyawanTersaringUntukPieChart);
        updateGrafikKehadiran(semuaCatatanKehadiranDiPeriode);
    }

    /**
     * Menghitung skor kinerja untuk setiap karyawan dan memperbarui data untuk tabel.
     * @param karyawanList Daftar semua karyawan.
     * @param kehadiranList Daftar semua catatan kehadiran dalam periode.
     * @param tugasList Daftar semua tugas dalam periode.
     * @param dateRange Rentang tanggal periode laporan.
     */
    private void updateTabelDanKinerjaKaryawan(List<EmployeeModel> karyawanList, List<AttendanceRecordModel> kehadiranList, List<TaskModel> tugasList, LocalDate[] dateRange) {
        if (kinerjaTableView == null) return;

        long totalHariKerja = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]) + 1;

        Map<Integer, Long> kehadiranPerKaryawan = kehadiranList.stream()
                .filter(r -> STATUS_HADIR.equalsIgnoreCase(r.getStatusKehadiran()) || STATUS_TERLAMBAT.equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.groupingBy(r -> r.getKaryawan().getId(), Collectors.counting()));

        Map<Integer, List<TaskModel>> tugasPerKaryawan = tugasList.stream()
                .filter(t -> t.getEmployeeId() != null)
                .collect(Collectors.groupingBy(TaskModel::getEmployeeId));

        for (EmployeeModel emp : karyawanList) {
            long jumlahHadir = kehadiranPerKaryawan.getOrDefault(emp.getId(), 0L);
            double skorKehadiran = (totalHariKerja > 0) ? ((double) jumlahHadir / totalHariKerja) : 0.0;

            List<TaskModel> tugasKaryawanIni = tugasPerKaryawan.getOrDefault(emp.getId(), List.of());
            long totalTugas = tugasKaryawanIni.size();
            long tugasSelesai = tugasKaryawanIni.stream().filter(t -> STATUS_TUGAS_SELESAI.equalsIgnoreCase(t.getStatus())).count();
            
            double skorTugas = (totalTugas > 0) ? ((double) tugasSelesai / totalTugas) : 1.0;

            double produktivitas = (skorKehadiran * BOBOT_SKOR_KEHADIRAN) + (skorTugas * BOBOT_SKOR_TUGAS);

            String peringkat;
            if (produktivitas >= AMBANG_BATAS_A_PLUS) peringkat = PERINGKAT_A_PLUS;
            else if (produktivitas >= AMBANG_BATAS_A) peringkat = PERINGKAT_A;
            else if (produktivitas >= AMBANG_BATAS_B) peringkat = PERINGKAT_B;
            else peringkat = PERINGKAT_C;

            
            emp.setPersentaseKehadiran(skorKehadiran);
            emp.setPersentaseProduktivitas(produktivitas);
            emp.setPeringkatKinerja(peringkat);
        }

        kinerjaData.setAll(karyawanList);
        tableSummaryLabel.setText("Menampilkan " + kinerjaData.size() + " karyawan");
    }

    /**
     * Memperbarui kartu-kartu statistik di bagian atas (total karyawan, kehadiran, produktivitas).
     */
    private void updateKartuStatistik(List<EmployeeModel> karyawanList, List<AttendanceRecordModel> kehadiranList) {
        // Kartu Total Karyawan
        totalKaryawanLabel.setText(String.valueOf(karyawanList.size()));
        totalKaryawanProgressBar.setProgress(karyawanList.size() / TARGET_KARYAWAN_PROGRESS_BAR);
        totalKaryawanDescLabel.setText("Total karyawan aktif");

        // Kartu Tingkat Kehadiran
        double avgKehadiran = kinerjaData.stream().mapToDouble(EmployeeModel::getPersentaseKehadiran).average().orElse(0.0);
        tingkatKehadiranLabel.setText(String.format("%.1f%%", avgKehadiran * 100));
        kehadiranProgressBar.setProgress(avgKehadiran);
        tingkatKehadiranDescLabel.setText("Rata-rata periode " + currentPeriodeFilter);

        // Kartu Produktivitas
        double avgProduktivitas = kinerjaData.stream().mapToDouble(EmployeeModel::getPersentaseProduktivitas).average().orElse(0.0);
        produktivitasLabel.setText(String.format("%.1f%%", avgProduktivitas * 100));
        produktivitasProgressBar.setProgress(avgProduktivitas);
        produktivitasDescLabel.setText("Kinerja gabungan");

        // Kartu Biaya SDM (placeholder)
        biayaSdmLabel.setText("N/A");
        biayaSdmProgressBar.setProgress(0);
        biayaSdmDescLabel.setText("Data tidak tersedia");
    }

    /**
     * Melakukan setup awal untuk komponen UI, seperti tabel dan grafik.
     */
    private void setupTampilanAwalSdm() {
        setFilterChartAktif(filterHadirButton);
        sebelumnyaButton.setDisable(true);
        selanjutnyaButton.setDisable(true);

        kehadiranChart.setAnimated(false);
        kehadiranSeries = new XYChart.Series<>();
        kehadiranChart.getData().add(kehadiranSeries);

        distribusiKaryawanChart.setAnimated(false);
        distribusiKaryawanChart.setData(distribusiData);

        kinerjaTableView.setItems(kinerjaData);


        karyawanColumn.setCellValueFactory(cellData -> cellData.getValue().namaLengkapProperty());
        departemenColumn.setCellValueFactory(cellData -> cellData.getValue().departemenProperty());
        kehadiranKolomTabel.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getPersentaseKehadiran() * 100)));
        produktivitasKolomTabel.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getPersentaseProduktivitas() * 100)));
        peringkatColumn.setCellValueFactory(cellData -> cellData.getValue().peringkatKinerjaProperty());

        peringkatColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case PERINGKAT_A_PLUS: 
                        case PERINGKAT_A:
                            setStyle(STYLE_PERINGKAT_A);
                            break;
                        case PERINGKAT_B:
                            setStyle(STYLE_PERINGKAT_B);
                            break;
                        case PERINGKAT_C:
                            setStyle(STYLE_PERINGKAT_C);
                            break;
                        default:
                            setStyle(STYLE_PERINGKAT_DEFAULT);
                    }
                }
            }
        });
    }

    /**
     * Memperbarui PieChart distribusi karyawan berdasarkan departemen.
     * @param karyawanList Daftar karyawan yang akan dihitung distribusinya.
     */
    private void updateGrafikDistribusi(List<EmployeeModel> karyawanList) {
        if (distribusiKaryawanChart == null) return;

        Map<String, Long> distribusi = karyawanList.stream()
                .filter(e -> e.getDepartemen() != null && !e.getDepartemen().isEmpty())
                .collect(Collectors.groupingBy(EmployeeModel::getDepartemen, Collectors.counting()));

        List<PieChart.Data> newData = distribusi.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()))
                .collect(Collectors.toList());

        distribusiData.setAll(newData);
    }

    /**
     * Memperbarui BarChart yang menunjukkan rekap kehadiran per hari dalam seminggu.
     * @param kehadiranList Daftar semua catatan kehadiran dalam periode.
     */
    private void updateGrafikKehadiran(List<AttendanceRecordModel> kehadiranList) {
        if (kehadiranChart == null || kehadiranSeries == null) return;

        kehadiranSeries.getData().clear();
        kehadiranSeries.setName(currentKehadiranChartFilter);

        Map<DayOfWeek, Long> dataGrafik = kehadiranList.stream()
                .filter(r -> currentKehadiranChartFilter.equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.groupingBy(r -> r.getTanggalAbsen().getDayOfWeek(), Collectors.counting()));

        for (DayOfWeek day : DayOfWeek.values()) {
            String namaHari = day.toString().substring(0, 3); // "MON", "TUE", dst.
            long jumlah = dataGrafik.getOrDefault(day, 0L);
            kehadiranSeries.getData().add(new XYChart.Data<>(namaHari, jumlah));
        }
    }

    /**
     * Menangani event klik pada tombol filter chart (Hadir, Terlambat, Absen).
     */
    @FXML
    private void handleFilterChartButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        currentKehadiranChartFilter = clickedButton.getText();
        setFilterChartAktif(clickedButton);
        muatUlangDataLaporan();
    }

    /**
     * Mengatur style visual untuk tombol filter yang sedang aktif.
     */
    private void setFilterChartAktif(Button activeFilterButton) {
        if (filterHadirButton == null) return; // Guard clause
        filterHadirButton.getStyleClass().remove(STYLE_CLASS_FILTER_AKTIF);
        filterTerlambatButton.getStyleClass().remove(STYLE_CLASS_FILTER_AKTIF);
        filterAbsenButton.getStyleClass().remove(STYLE_CLASS_FILTER_AKTIF);
        activeFilterButton.getStyleClass().add(STYLE_CLASS_FILTER_AKTIF);
    }

    /**
     * Menentukan rentang tanggal berdasarkan filter periode yang dipilih.
     */
    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        LocalDate today = LocalDate.now();
        YearMonth currentYMonth = YearMonth.from(today);

        switch (periodeFilter) {
            case PERIODE_MINGGU_INI:
                startDate = today.with(DayOfWeek.MONDAY);
                endDate = today.with(DayOfWeek.SUNDAY);
                break;
            case PERIODE_HARI_INI:
                startDate = today;
                endDate = today;
                break;
            case PERIODE_TAHUN_INI:
                startDate = today.withDayOfYear(1);
                endDate = today.withDayOfYear(today.lengthOfYear());
                break;
            case PERIODE_BULAN_INI:
            default: 
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }
}