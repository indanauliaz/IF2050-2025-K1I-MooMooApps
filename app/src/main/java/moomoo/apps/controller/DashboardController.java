package moomoo.apps.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;

import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.*;
import moomoo.apps.utils.DatabaseManager;
import moomoo.apps.utils.PollingService;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    /* --------- Sidebar & Header --------- */
    @FXML private ToggleGroup menuToggleGroup;
    @FXML private ToggleButton dashboardButton, laporanButton, keuanganButton, tugasButton, produksiButton;
    @FXML private Button logoutButton;
    @FXML private Label userNameLabel, userRoleLabel;
    @FXML private ScrollPane mainContentScrollPane;

    /* --------- KPI Controls --------- */
    @FXML private Label totalProduksiLabel, deltaProduksiLabel;
    @FXML private ProgressBar produksiProgress;
    @FXML private Label totalPendapatanLabel, deltaPendapatanLabel;
    @FXML private ProgressBar pendapatanProgress;
    @FXML private Label taskDoneLabel, taskPercentLabel;
    @FXML private ProgressBar taskProgress;
    @FXML private Label attendanceLabel, attendancePercentLabel;
    @FXML private ProgressBar attendanceProgress;

    /* --------- Task List Today --------- */
    @FXML private VBox todayTaskList;

    private Node originalDashboardContent;
    private UserModel currentUser;

    @FXML private AreaChart<String, Number> milkChart;

    /* ========== INITIALIZE ========== */
    @FXML
    private void initialize() {
        menuToggleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                if (oldT != null) oldT.setSelected(true);
            } else handleSidebar((ToggleButton) newT);
        });

        dashboardButton.setSelected(true);

        Platform.runLater(() -> {
            originalDashboardContent = mainContentScrollPane.getContent();
            refreshDashboard();
        });
    }

    @FXML
    public void handleLogoutAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/moomoo/apps/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initData(UserModel user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());
        }
        PollingService.getInstance().start();
    }

    public void refreshDashboard() {
        updateProduksiKpi();
        updateKeuanganKpi();
        updateTaskKpi();
        updateAttendanceKpi();
        populateTodayTasks();
        updateMilkProductionChart(); 
    }

    /* --- PRODUKSI KPI --- */
    private void updateProduksiKpi() {
        LocalDate today = LocalDate.now();
        LocalDate startCurr = today.withDayOfMonth(1);
        LocalDate endPrev   = startCurr.minusDays(1);
        LocalDate startPrev = endPrev.withDayOfMonth(1);

        double currTotal = ProductionModel.getInstance().getAllProductionData().stream()
                .filter(r -> !r.getTanggal().isBefore(startCurr) && !r.getTanggal().isAfter(today))
                .mapToDouble(ProductionRecord::getJumlah).sum();

        double prevTotal = ProductionModel.getInstance().getAllProductionData().stream()
                .filter(r -> !r.getTanggal().isBefore(startPrev) && !r.getTanggal().isAfter(endPrev))
                .mapToDouble(ProductionRecord::getJumlah).sum();

        double delta = prevTotal == 0 ? 0 : (currTotal - prevTotal) / prevTotal;

        totalProduksiLabel.setText(String.format(Locale.US, "%,.0f L", currTotal));
        deltaProduksiLabel.setText(String.format(Locale.US, "%+.1f%% dari bulan lalu", delta * 100));
        produksiProgress.setProgress(Math.min(1.0, Math.abs(delta)));
    }

    /* --- KEUANGAN KPI --- */
    private void updateKeuanganKpi() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        List<TransactionModel> trx = FinanceModel.getInstance().getAllTransactions();
        double pemasukan   = trx.stream().filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()))
                                .mapToDouble(TransactionModel::getAmount).sum();
        double pengeluaran = trx.stream().filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()))
                                .mapToDouble(TransactionModel::getAmount).sum();
        double net = pemasukan - pengeluaran;

        totalPendapatanLabel.setText("Rp " + nf.format(net));
        pendapatanProgress.setProgress(pemasukan == 0 ? 0 : pengeluaran / pemasukan);
        deltaPendapatanLabel.setText("Pengeluaran: Rp " + nf.format(pengeluaran));
    }

    /* --- TASK KPI --- */
    private void updateTaskKpi() {
        List<TaskModel> tasks = DatabaseManager.getAllTasks();
        long done   = tasks.stream().filter(t -> "Selesai".equalsIgnoreCase(t.getStatus())).count();
        int  total  = tasks.size();
        double pct  = total == 0 ? 0 : (double) done / total;

        taskDoneLabel.setText(done + "/" + total);
        taskPercentLabel.setText(String.format(Locale.US, "%.0f%% tingkat penyelesaian", pct * 100));
        taskProgress.setProgress(pct);
    }

    /* --- ATTENDANCE KPI --- */
    private void updateAttendanceKpi() {
        LocalDate today = LocalDate.now();
        List<AttendanceRecordModel> todayRecords = DatabaseManager.getAttendanceRecordsByDate(today);
        long hadir = todayRecords.stream().filter(r -> "Hadir".equalsIgnoreCase(r.getStatusKehadiran())).count();
        int totalKaryawan = DatabaseManager.getAllEmployees().size();
        double pct = totalKaryawan == 0 ? 0 : (double) hadir / totalKaryawan;

        attendanceLabel.setText(hadir + "/" + totalKaryawan);
        attendancePercentLabel.setText(String.format(Locale.US, "%.0f%% tingkat kehadiran", pct * 100));
        attendanceProgress.setProgress(pct);
    }

    /* --- TASK LIST HARI INI --- */
    private void populateTodayTasks() {
        todayTaskList.getChildren().clear();
        LocalDate today = LocalDate.now();
        List<TaskModel> todayTasks = DatabaseManager.getAllTasks().stream()
                .filter(t -> today.equals(t.getTanggalTugas()))
                .collect(Collectors.toList());

        for (TaskModel t : todayTasks) {
            Label name = new Label(t.getNamaTugas());
            name.getStyleClass().add("task-label");

            Label status = new Label(t.getStatus());
            status.getStyleClass().add(switch (t.getStatus().toLowerCase()) {
                case "selesai" -> "status-done";
                case "sedang dikerjakan" -> "status-progress";
                default -> "status-pending";
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(10, name, spacer, status);
            row.setAlignment(Pos.CENTER_LEFT);
            todayTaskList.getChildren().add(row);
        }
    }

    /* --- MILK PRODUCTION CHART --- */
    private void updateMilkProductionChart() {
        milkChart.getData().clear();
        milkChart.setAnimated(false); 

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Produksi Susu (Liter)");

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); 

 
        Map<LocalDate, Double> dailyProduction = ProductionModel.getInstance().getAllProductionData().stream()
                .filter(record -> !record.getTanggal().isBefore(sevenDaysAgo) && !record.getTanggal().isAfter(today))
                .collect(Collectors.groupingBy(
                        ProductionRecord::getTanggal,
                        Collectors.summingDouble(ProductionRecord::getJumlah)
                ));

    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        sevenDaysAgo.datesUntil(today.plusDays(1)) 
                .forEach(date -> {
                    double total = dailyProduction.getOrDefault(date, 0.0); 
                    series.getData().add(new XYChart.Data<>(date.format(formatter), total));
                });

        milkChart.getData().add(series);
    }

    /* ========== VIEW HELPERS ========== */
    private void loadView(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) { showError("FXML not found: " + path); return; }
            FXMLLoader fx = new FXMLLoader(url);
            Parent view = fx.load();
            if (currentUser != null && fx.getController() instanceof UserAwareController uac) uac.initData(currentUser);
            mainContentScrollPane.setContent(view);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Gagal memuat: " + path);
        }
    }

    private void handleSidebar(ToggleButton btn) {
        if (btn == dashboardButton) { restoreDashboard(); return; }
        String fxml = switch (btn.getId()) {
            case "laporanButton"  -> "/moomoo/apps/view/LaporanView.fxml";
            case "keuanganButton" -> "/moomoo/apps/view/FinanceView.fxml";
            case "tugasButton"    -> "/moomoo/apps/view/TaskManagement.fxml";
            case "produksiButton" -> "/moomoo/apps/view/ProductionView.fxml";
            default -> null;
        };
        if (fxml != null) loadView(fxml); else showPlaceholderView(btn.getText());
    }

    private void restoreDashboard() {
        if (originalDashboardContent != null) mainContentScrollPane.setContent(originalDashboardContent);
        refreshDashboard();
    }

    private void showPlaceholderView(String name) {
        Label l = new Label("Halaman " + name + " belum tersedia"); l.setStyle("-fx-font-size:18px;");
        VBox box = new VBox(l); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(20));
        mainContentScrollPane.setContent(box);
    }

    private void showError(String msg) {
        Label l = new Label(msg); l.setStyle("-fx-text-fill:red;-fx-font-size:16px;");
        VBox box = new VBox(l); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(20));
        mainContentScrollPane.setContent(box);
    }
}
