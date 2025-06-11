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
import moomoo.apps.model.TaskModel; // Import TaskModel

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LaporanSdmController implements ILaporanKontenController {

    //<editor-fold defaultstate="collapsed" desc="FXML Declarations">
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
    //</editor-fold>

    private SdmModel sdmModel;
    private String currentPeriodeFilter = "Bulan Ini";
    private String currentKehadiranChartFilter = "Hadir";

    @Override
    public void inisialisasiKonten() {
        this.sdmModel = SdmModel.getInstance();
        setupTampilanAwalSdm();
        sdmModel.getAllEmployees().addListener((ListChangeListener<EmployeeModel>) c -> muatUlangDataLaporan());
    }

    @Override
    public void terapkanFilterPeriode(String periode) {
        this.currentPeriodeFilter = (periode == null || periode.isEmpty()) ? "Bulan Ini" : periode;
        muatUlangDataLaporan();
    }
    
    private void muatUlangDataLaporan() {
        if (sdmModel == null) return;
        
        LocalDate[] dateRange = determineDateRange(currentPeriodeFilter);
        
        ObservableList<EmployeeModel> semuaKaryawan = sdmModel.getAllEmployees();
        List<AttendanceRecordModel> catatanKehadiran = sdmModel.getAttendanceRecordsByDateRange(dateRange[0], dateRange[1]);
        // Ambil data tugas sesuai rentang periode
        List<TaskModel> catatanTugas = sdmModel.getTasksByDateRange(dateRange[0], dateRange[1]);
        
        updateTabelDanKinerjaKaryawan(semuaKaryawan, catatanKehadiran, catatanTugas, dateRange);
        updateKartuStatistik(semuaKaryawan, catatanKehadiran);
        updateGrafikDistribusi(semuaKaryawan);
        updateGrafikKehadiran(catatanKehadiran);
    }

    private void updateTabelDanKinerjaKaryawan(List<EmployeeModel> karyawanList, List<AttendanceRecordModel> kehadiranList, List<TaskModel> tugasList, LocalDate[] dateRange) {
        if (kinerjaTableView == null) return;
        
        // 1. Hitung total hari kerja dalam periode
        long totalHariKerja = ChronoUnit.DAYS.between(dateRange[0], dateRange[1]) + 1;
        
        // 2. Kelompokkan data kehadiran dan tugas per karyawan untuk efisiensi
        Map<Integer, Long> kehadiranPerKaryawan = kehadiranList.stream()
            .filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran()) || "Terlambat".equalsIgnoreCase(r.getStatusKehadiran()))
            .collect(Collectors.groupingBy(r -> r.getKaryawan().getId(), Collectors.counting()));
            
        Map<Integer, List<TaskModel>> tugasPerKaryawan = tugasList.stream()
            .filter(t -> t.getEmployeeId() != null)
            .collect(Collectors.groupingBy(TaskModel::getEmployeeId));

        // 3. Iterasi setiap karyawan untuk menghitung skor dan peringkat
        for (EmployeeModel emp : karyawanList) {

            long jumlahHadir = kehadiranPerKaryawan.getOrDefault(emp.getId(), 0L);
            double skorKehadiran = (totalHariKerja > 0) ? ((double) jumlahHadir / totalHariKerja) : 0.0;
            
            List<TaskModel> tugasKaryawanIni = tugasPerKaryawan.getOrDefault(emp.getId(), List.of());
            long totalTugas = tugasKaryawanIni.size();
            long tugasSelesai = tugasKaryawanIni.stream().filter(t -> "Selesai".equalsIgnoreCase(t.getStatus())).count();
            double skorTugas = (totalTugas > 0) ? ((double) tugasSelesai / totalTugas) : 1.0; 

            // --- Kalkulasi Produktivitas Gabungan ---
            double produktivitas = (skorKehadiran * 0.6) + (skorTugas * 0.4);

            // --- Penentuan Peringkat ---
            String peringkat;
            if (produktivitas >= 0.9) peringkat = "A+";
            else if (produktivitas >= 0.8) peringkat = "A";
            else if (produktivitas >= 0.7) peringkat = "B";
            else peringkat = "C";

            // 4. Set properti pada EmployeeModel
            emp.setPersentaseKehadiran(skorKehadiran);
            emp.setPersentaseProduktivitas(produktivitas);
            emp.setPeringkatKinerja(peringkat);
        }
        
        // 5. Tampilkan di tabel
        kinerjaTableView.setItems(FXCollections.observableArrayList(karyawanList));
        tableSummaryLabel.setText("Menampilkan " + karyawanList.size() + " dari " + karyawanList.size() + " karyawan");
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
                        case "A":
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            break;
                        case "B":
                            setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case "C":
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

    }
    
    private void updateGrafikDistribusi(List<EmployeeModel> karyawanList) {
        if (distribusiKaryawanChart == null) return;
        distribusiKaryawanChart.getData().clear();
        Map<String, Long> distribusi = karyawanList.stream()
            .filter(e -> e.getDepartemen() != null && !e.getDepartemen().isEmpty())
            .collect(Collectors.groupingBy(EmployeeModel::getDepartemen, Collectors.counting()));
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        distribusi.forEach((dept, count) -> pieChartData.add(new PieChart.Data(dept + " (" + count + ")", count)));
        distribusiKaryawanChart.setData(pieChartData);
    }
    
    private void updateGrafikKehadiran(List<AttendanceRecordModel> kehadiranList) {
        if (kehadiranChart == null) return;
        kehadiranChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(currentKehadiranChartFilter);
        Map<DayOfWeek, Long> dataGrafik = kehadiranList.stream()
            .filter(r -> currentKehadiranChartFilter.equalsIgnoreCase(r.getStatusKehadiran()))
            .collect(Collectors.groupingBy(r -> r.getTanggalAbsen().getDayOfWeek(), Collectors.counting()));
        for (DayOfWeek day : DayOfWeek.values()) {
            series.getData().add(new XYChart.Data<>(day.toString().substring(0, 3), dataGrafik.getOrDefault(day, 0L)));
        }
        kehadiranChart.getData().add(series);
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
        YearMonth currentYMonth = YearMonth.now();
        switch (periodeFilter) {
            case "Minggu Ini": startDate = LocalDate.now().with(DayOfWeek.MONDAY); endDate = LocalDate.now().with(DayOfWeek.SUNDAY); break;
            case "Hari Ini": startDate = LocalDate.now(); endDate = LocalDate.now(); break;
            case "Tahun Ini": startDate = currentYMonth.withMonth(1).atDay(1); endDate = currentYMonth.withMonth(12).atEndOfMonth(); break;
            default: startDate = currentYMonth.atDay(1); endDate = currentYMonth.atEndOfMonth(); break;
        }
        return new LocalDate[]{startDate, endDate};
    }
    //</editor-fold>
}
