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

public class LaporanSdmController implements ILaporanKontenController {

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

    private SdmModel sdmModel;
    private String currentPeriodeFilter = "Tahun Ini";
    private String currentKehadiranChartFilter = "Hadir";
    
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
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? "Tahun Ini" : periode;
        muatUlangDataLaporan();
    }

    private void muatUlangDataLaporan() {
        if (sdmModel == null) return;
        
        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        
        ObservableList<EmployeeModel> semuaKaryawan = sdmModel.getAllEmployees();
        List<AttendanceRecordModel> semuaCatatanKehadiranDiPeriode = sdmModel.getAttendanceRecordsByDateRange(dateRange[0], dateRange[1]);
        List<TaskModel> catatanTugas = sdmModel.getTasksByDateRange(dateRange[0], dateRange[1]);

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
        
        updateTabelDanKinerjaKaryawan(semuaKaryawan, semuaCatatanKehadiranDiPeriode, catatanTugas, dateRange);
        updateKartuStatistik(semuaKaryawan, semuaCatatanKehadiranDiPeriode);
        updateGrafikDistribusi(karyawanTersaringUntukPieChart); 
        updateGrafikKehadiran(semuaCatatanKehadiranDiPeriode);
    }

    private void updateTabelDanKinerjaKaryawan(List<EmployeeModel> karyawanList, List<AttendanceRecordModel> kehadiranList, List<TaskModel> tugasList, LocalDate[] dateRange) {
        if (kinerjaTableView == null) return;
        
        long totalHariKerja = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]) + 1;
        
        Map<Integer, Long> kehadiranPerKaryawan = kehadiranList.stream()
                .filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran()) || "Terlambat".equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.groupingBy(r -> r.getKaryawan().getId(), Collectors.counting()));
                
        Map<Integer, List<TaskModel>> tugasPerKaryawan = tugasList.stream()
                .filter(t -> t.getEmployeeId() != null)
                .collect(Collectors.groupingBy(TaskModel::getEmployeeId));

        for (EmployeeModel emp : karyawanList) {
            long jumlahHadir = kehadiranPerKaryawan.getOrDefault(emp.getId(), 0L);
            double skorKehadiran = (totalHariKerja > 0) ? ((double) jumlahHadir / totalHariKerja) : 0.0;
            
            List<TaskModel> tugasKaryawanIni = tugasPerKaryawan.getOrDefault(emp.getId(), List.of());
            long totalTugas = tugasKaryawanIni.size();
            long tugasSelesai = tugasKaryawanIni.stream().filter(t -> "Selesai".equalsIgnoreCase(t.getStatus())).count();
            double skorTugas = (totalTugas > 0) ? ((double) tugasSelesai / totalTugas) : 1.0; 

            double produktivitas = (skorKehadiran * 0.6) + (skorTugas * 0.4);

            String peringkat;
            if (produktivitas >= 0.9) peringkat = "A+";
            else if (produktivitas >= 0.8) peringkat = "A";
            else if (produktivitas >= 0.7) peringkat = "B";
            else peringkat = "C";

            emp.setPersentaseKehadiran(skorKehadiran);
            emp.setPersentaseProduktivitas(produktivitas);
            emp.setPeringkatKinerja(peringkat);
        }
        
        kinerjaData.setAll(karyawanList);
        tableSummaryLabel.setText("Menampilkan " + kinerjaData.size() + " dari " + kinerjaData.size() + " karyawan");
    }

    private void updateKartuStatistik(List<EmployeeModel> karyawanList, List<AttendanceRecordModel> kehadiranList) {
        totalKaryawanLabel.setText(String.valueOf(karyawanList.size()));
        totalKaryawanProgressBar.setProgress(karyawanList.size() / 50.0);
        totalKaryawanDescLabel.setText("Total karyawan aktif");

        double avgKehadiran = karyawanList.stream().mapToDouble(EmployeeModel::getPersentaseKehadiran).average().orElse(0.0);
        tingkatKehadiranLabel.setText(String.format("%.1f%%", avgKehadiran * 100));
        kehadiranProgressBar.setProgress(avgKehadiran);
        tingkatKehadiranDescLabel.setText("Rata-rata periode " + currentPeriodeFilter);

        double avgProduktivitas = sdmModel.getKinerjaGabunganPeriodeIni(); 
        produktivitasLabel.setText(String.format("%.1f%%", avgProduktivitas * 100));
        produktivitasProgressBar.setProgress(avgProduktivitas);
        produktivitasDescLabel.setText("Kinerja gabungan");
 
        biayaSdmLabel.setText("N/A");
        biayaSdmProgressBar.setProgress(0);
        biayaSdmDescLabel.setText("Data tidak tersedia");
    }
    
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
        
        peringkatColumn.setCellFactory(column -> new TableCell<EmployeeModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "A": setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); break;
                        case "B": setStyle("-fx-text-fill: orange; -fx-font-weight: bold;"); break;
                        case "C": setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); break;
                        default: setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });
    }
    
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
    
    private void updateGrafikKehadiran(List<AttendanceRecordModel> kehadiranList) {
        if (kehadiranChart == null || kehadiranSeries == null) return;

        kehadiranSeries.getData().clear();
        kehadiranSeries.setName(currentKehadiranChartFilter);
        
        Map<DayOfWeek, Long> dataGrafik = kehadiranList.stream()
                .filter(r -> currentKehadiranChartFilter.equalsIgnoreCase(r.getStatusKehadiran()))
                .collect(Collectors.groupingBy(r -> r.getTanggalAbsen().getDayOfWeek(), Collectors.counting()));
                
        for (DayOfWeek day : DayOfWeek.values()) {
            kehadiranSeries.getData().add(new XYChart.Data<>(day.toString().substring(0, 3), dataGrafik.getOrDefault(day, 0L)));
        }
    }

    @FXML
    private void handleFilterChartButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        currentKehadiranChartFilter = clickedButton.getText();
        setFilterChartAktif(clickedButton);
        muatUlangDataLaporan(); 
    }

    private void setFilterChartAktif(Button activeFilterButton) {
        if (filterHadirButton == null) return;
        filterHadirButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterTerlambatButton.getStyleClass().remove("filter-chart-button-laporan-active");
        filterAbsenButton.getStyleClass().remove("filter-chart-button-laporan-active");
        activeFilterButton.getStyleClass().add("filter-chart-button-laporan-active");
    }
    
    private LocalDate[] determineDateRange(String periodeFilter) {
        LocalDate startDate, endDate;
        LocalDate today = LocalDate.now();
        YearMonth currentYMonth = YearMonth.from(today);
        
        switch (periodeFilter) {
            case "Minggu Ini":
                startDate = today.with(DayOfWeek.MONDAY);
                endDate = today.with(DayOfWeek.SUNDAY);
                break;
            case "Hari Ini":
                startDate = today;
                endDate = today;
                break;
            case "Tahun Ini":
                startDate = today.withDayOfYear(1);
                endDate = today.withDayOfYear(today.lengthOfYear());
                break;
            default: // "Bulan Ini"
                startDate = currentYMonth.atDay(1);
                endDate = currentYMonth.atEndOfMonth();
                break;
        }
        return new LocalDate[]{startDate, endDate};
    }
}