package moomoo.apps.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.*;
import moomoo.apps.utils.PollingService;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardPemilikController {

    @FXML private VBox sidebar;
    @FXML private BorderPane mainContentPane;
    @FXML private Label headerTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardButton;
    @FXML private Button laporanButton;
    @FXML private Button keuanganButton;
    @FXML private Button tugasButton;
    @FXML private Button logoutButton;
    @FXML private Button refreshButton;

    private UserModel currentUser;
    private Timeline autoRefreshTimeline;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final NumberFormat percentFormatter = NumberFormat.getPercentInstance(new Locale("id", "ID"));

    /* ========== INITIALIZE ========== */
    @FXML
    public void initialize() {
        currencyFormatter.setMaximumFractionDigits(2);
        percentFormatter.setMaximumFractionDigits(1);
        Platform.runLater(() -> {
            handleDashboardMenuClick(null);
            setActiveButton(dashboardButton);
        });
    }

    /* method handler ketika dasboard menu di klik */
    @FXML
    void handleDashboardMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Dashboard");
        setActiveButton(dashboardButton);

        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(20));

        // MEMBUAT HEADER "SELAMAT DATANG"
        VBox headerBox = new VBox();
        Label title = new Label("Dashboard");
        title.getStyleClass().add("dashboard-title");
        Label welcome = new Label("Selamat Datang, Tuan Jones!");
        welcome.getStyleClass().add("welcome-subtext");
        headerBox.getChildren().addAll(title, welcome);

        // MEMBUAT SEMUA BARIS KONTEN
        HBox row1_businessSummary = createBusinessSummaryRow();
        HBox row2_productionMetrics = createProductionMetricsRow();
        HBox row3_charts = createChartsRow();

        // MENGGABUNGKAN SEMUA BAGIAN
        dashboardContent.getChildren().addAll(headerBox, row1_businessSummary, row2_productionMetrics, row3_charts);

        mainContentPane.setCenter(dashboardContent);
    }

    /* ========== HELPER UNTUK DATA PRODUKSI DI DASHBOARD ========== */
    private HBox createProductionMetricsRow() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);


        double susuBulanIni = getMonthlyProductionByCategory(currentMonth, "Susu");
        double susuBulanLalu = getMonthlyProductionByCategory(previousMonth, "Susu");
        String susuChangeText = calculatePercentageChange(susuBulanIni, susuBulanLalu);

        double dagingBulanIni = getMonthlyProductionByCategory(currentMonth, "Daging");
        double dagingBulanLalu = getMonthlyProductionByCategory(previousMonth, "Daging");
        String dagingChangeText = calculatePercentageChange(dagingBulanIni, dagingBulanLalu);

     
        double produktivitasBulanIni = SdmModel.getInstance().getKinerjaGabungan(currentMonth);
        double produktivitasBulanLalu = SdmModel.getInstance().getKinerjaGabungan(previousMonth);
        String produktivitasChangeText = calculatePercentageChange(produktivitasBulanIni, produktivitasBulanLalu);
        VBox cardProduksiSusu = createMetricCard("Produksi Susu", String.format("%,.0f L", susuBulanIni), susuChangeText, susuBulanIni / 20000.0);
        VBox cardProduksiDaging = createMetricCard("Produksi Daging", String.format("%,.0f Kg", dagingBulanIni), dagingChangeText, dagingBulanIni / 5000.0); 
        VBox cardProduktivitas = createMetricCard(
        "Produktivitas Tim", 
        String.format("%.1f%%", produktivitasBulanIni * 100), 
        produktivitasChangeText, 
        produktivitasBulanIni
    );


        HBox row = new HBox(20, cardProduksiSusu, cardProduksiDaging, cardProduktivitas);
        HBox.setHgrow(cardProduksiSusu, Priority.ALWAYS);
        HBox.setHgrow(cardProduksiDaging, Priority.ALWAYS);
        HBox.setHgrow(cardProduktivitas, Priority.ALWAYS);
        return row;
    }

    private double getMonthlyProductionByCategory(YearMonth month, String category) {
        return ProductionModel.getInstance().getAllProductionData().stream()
            .filter(p -> p.getTanggal() != null && YearMonth.from(p.getTanggal()).equals(month) && category.equalsIgnoreCase(p.getKategori()))
            .mapToDouble(ProductionRecord::getJumlah).sum();
    }

    public void initData(UserModel user) {
        this.currentUser = user;
        updateUserInfo();
        PollingService.getInstance().start();
        startAutoRefresh();
    }
    
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> performRefresh(true)));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
        System.out.println("Refresh otomatis setiap 5 detik telah dimulai.");
    }

    /* ========== HELPER UNTUK DATA KEUANGAN DI DASHBOARD ========== */
    private HBox createBusinessSummaryRow() {
        YearMonth currentMonth = YearMonth.now();
        List < TransactionModel > transactionsThisMonth = FinanceModel.getInstance().getAllTransactions().stream().filter(t -> t.getDate() != null && YearMonth.from(t.getDate()).equals(currentMonth)).collect(Collectors.toList());
        double pendapatan = transactionsThisMonth.stream().filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType())).mapToDouble(TransactionModel::getAmount).sum();
        double pengeluaran = transactionsThisMonth.stream().filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()) || "Penggajian".equalsIgnoreCase(t.getTransactionType())).mapToDouble(TransactionModel::getAmount).sum();
        double laba = pendapatan - pengeluaran;
        VBox cardPendapatan = createInfoCard("Pendapatan", formatToJuta(pendapatan), "Bulan Ini");
        VBox cardPengeluaran = createInfoCard("Pengeluaran", formatToJuta(pengeluaran), "Bulan Ini");
        VBox cardLaba = createInfoCard("Laba Bersih", formatToJuta(laba), "Bulan Ini");
        HBox row = new HBox(20, cardPendapatan, cardPengeluaran, cardLaba);
        HBox.setHgrow(cardPendapatan, Priority.ALWAYS);
        HBox.setHgrow(cardPengeluaran, Priority.ALWAYS);
        HBox.setHgrow(cardLaba, Priority.ALWAYS);
        return row;
    }
    
    private HBox createChartsRow() {
        BarChart < String, Number > barChart = createFinancialBarChart();
        VBox chart1Container = new VBox(10);
        chart1Container.getChildren().addAll(new Label("Grafik Keuangan"), new Label("Pendapatan dan pengeluaran 6 bulan terakhir"), barChart);
        chart1Container.getStyleClass().add("chart-card");
        VBox.setVgrow(barChart, Priority.ALWAYS);
        PieChart pieChart = createDistributionPieChart();
        VBox chart2Container = new VBox(10);
        chart2Container.getChildren().addAll(new Label("Distribusi Pendapatan"), new Label("Berdasarkan kategori produk bulan ini"), pieChart);
        chart2Container.getStyleClass().add("chart-card");
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        HBox row = new HBox(20, chart1Container, chart2Container);
        HBox.setHgrow(chart1Container, Priority.ALWAYS);
        return row;
    }
    
    private BarChart < String, Number > createFinancialBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Jumlah (dalam Juta Rp)");
        BarChart < String, Number > barChart = new BarChart < > (xAxis, yAxis);
        barChart.setLegendVisible(true);
        barChart.setAnimated(false);
        XYChart.Series < String, Number > seriesPendapatan = new XYChart.Series < > ();
        seriesPendapatan.setName("Pendapatan");
        XYChart.Series < String, Number > seriesPengeluaran = new XYChart.Series < > ();
        seriesPengeluaran.setName("Pengeluaran");
        List < TransactionModel > allTransactions = FinanceModel.getInstance().getAllTransactions();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, new Locale("id", "ID"));
            double pendapatan = allTransactions.stream().filter(t -> t.getDate() != null && YearMonth.from(t.getDate()).equals(month) && "Pemasukan".equalsIgnoreCase(t.getTransactionType())).mapToDouble(TransactionModel::getAmount).sum();
            double pengeluaran = allTransactions.stream().filter(t -> t.getDate() != null && YearMonth.from(t.getDate()).equals(month) && ("Pengeluaran".equalsIgnoreCase(t.getTransactionType()) || "Penggajian".equalsIgnoreCase(t.getTransactionType()))).mapToDouble(TransactionModel::getAmount).sum();
            seriesPendapatan.getData().add(new XYChart.Data < > (monthName, pendapatan / 1_000_000));
            seriesPengeluaran.getData().add(new XYChart.Data < > (monthName, pengeluaran / 1_000_000));
        }
        barChart.getData().addAll(seriesPendapatan, seriesPengeluaran);
        return barChart;
    }
    
    private PieChart createDistributionPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(false);
        pieChart.setAnimated(false);
        Map < String, Double > distribusiData = FinanceModel.getInstance().getAllTransactions().stream().filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()) && t.getDate() != null && YearMonth.from(t.getDate()).equals(YearMonth.now())).collect(Collectors.groupingBy(TransactionModel::getCategory, Collectors.summingDouble(TransactionModel::getAmount)));
        ObservableList < PieChart.Data > pieChartData = FXCollections.observableArrayList();
        distribusiData.forEach((kategori, jumlah) -> pieChartData.add(new PieChart.Data(kategori, jumlah)));
        pieChart.setData(pieChartData);
        return pieChart;
    }
    
    
    private String calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? "Data baru" : "Tidak ada data";
        }
        double change = ((current - previous) / previous);
        String sign = change >= 0 ? "+" : "";
        return String.format("%s%s dari bulan lalu", sign, percentFormatter.format(change));
    }
    
    /* method refresh data untuk meninjau data terbaru */
    private void performRefresh(boolean isAutoRefresh) {
        System.out.println("Memulai proses refresh data... Dipicu oleh: " + (isAutoRefresh ? "Otomatis" : "Manual"));
        FinanceModel.getInstance().loadAllTransactionsFromDB();
        ProductionModel.getInstance().loadProductionDataFromDB();
        SdmModel.getInstance().loadAllEmployeesFromDB();
        SdmModel.getInstance().loadAllTasksFromDB(); 
        System.out.println("Semua model telah diperbarui.");
        if (headerTitleLabel.getText().equals("Dashboard")) {
            Platform.runLater(() -> handleDashboardMenuClick(null));
        }
        if (!isAutoRefresh) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Data telah berhasil diperbarui.", ButtonType.OK);
            alert.setHeaderText("Refresh Berhasil");
            alert.setTitle("Informasi");
            alert.showAndWait();
        }
    }
    
    private VBox createInfoCard(String title, String value, String description) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title-brown");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("card-sub");
        card.getChildren().addAll(titleLabel, valueLabel, descLabel);
        return card;
    }
    
    private VBox createMetricCard(String title, String value, String description, double progress) {
        VBox card = createInfoCard(title, value, description);
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().add(progressBar);
        return card;
    }
    
    private String formatToJuta(double value) {
        return currencyFormatter.format(value / 1_000_000) + " jt";
    }
    
    /* handler tombol refresh data */
    @FXML
    private void handleRefreshAction(ActionEvent event) {
        performRefresh(false);
    }
    
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            System.out.println("Refresh otomatis telah dihentikan.");
        }
    }
    
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getUsername());
            userRoleLabel.setText(currentUser.getRole());
        }
    }
    
    /* ========== HELPER UNTUK CLICKABLE BUTTON ========== */
    @FXML
    void handleTugasMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Pengawasan Tugas");
        loadPage("/moomoo/apps/view/TugasPemilikView.fxml");
        setActiveButton(tugasButton);
    }
    
    @FXML
    void handleLaporanMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Laporan");
        loadPage("/moomoo/apps/view/LaporanPemilikView.fxml");
        setActiveButton(laporanButton);
    }
    
    @FXML
    void handleKeuanganMenuClick(ActionEvent event) {
        headerTitleLabel.setText("Keuangan");
        loadPage("/moomoo/apps/view/KeuanganPemilikView.fxml");
        setActiveButton(keuanganButton);
    }
    
    @FXML
    private void handleLogoutAction(ActionEvent event) {
        stopAutoRefresh();
        PollingService.getInstance().stop();
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            URL fxmlLocation = getClass().getResource("/moomoo/apps/view/LoginView.fxml");
            if (fxmlLocation == null) {
                System.err.println("LoginView.fxml not found!");
                return;
            }
            Parent loginRoot = FXMLLoader.load(fxmlLocation);
            Scene loginScene = new Scene(loginRoot);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login Moo Moo Apps");
            currentStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showErrorPage(String message) {
        VBox errorBox = new VBox(new Label(message));
        errorBox.setStyle("-fx-alignment: center; -fx-font-size: 16px; -fx-text-fill: red;");
        mainContentPane.setCenter(errorBox);
    }
    
    private void setActiveButton(Button activeButton) {
        if (sidebar != null) {
            sidebar.getChildren().filtered(node -> node instanceof Button).forEach(node -> node.getStyleClass().remove("sidebar-button-active"));
            if (activeButton != null) {
                activeButton.getStyleClass().add("sidebar-button-active");
            }
        }
    }
    
    private void loadPage(String fxmlPath) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                showErrorPage("Error: Tidak dapat menemukan file FXML di -> " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node page = loader.load();
            Object controller = loader.getController();
            if (controller instanceof UserAwareController) {
                ((UserAwareController) controller).initData(currentUser);
            }
            mainContentPane.setCenter(page);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorPage("Error memuat halaman: " + fxmlPath + ". Cek konsol untuk detail.");
        }
    }
}