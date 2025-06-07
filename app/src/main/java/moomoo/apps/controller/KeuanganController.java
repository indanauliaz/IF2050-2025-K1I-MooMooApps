package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.FinanceModel;
import moomoo.apps.model.TransactionModel;
import moomoo.apps.model.UserModel;
import moomoo.apps.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
// import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeuanganController implements UserAwareController {

    @FXML private VBox keuanganRootPane; 

    @FXML private TabPane keuanganTabPane;
    @FXML private Tab pemasukanTab;
    @FXML private Tab pengeluaranTab;
    @FXML private Tab penggajianTab;

    // --- PEMASUKAN FXML Elements ---
    @FXML private TextField deskripsiPemasukanField;
    @FXML private DatePicker tanggalPemasukanPicker;
    @FXML private TextField jumlahPemasukanField;
    @FXML private ComboBox<String> metodePembayaranPemasukanBox;
    @FXML private ComboBox<String> kategoriPemasukanBox;
    @FXML private TextField catatanPemasukanField;
    @FXML private Button tambahPemasukanButton;
    @FXML private TableView<TransactionModel> pemasukanTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPemasukanCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPemasukanCol;
    @FXML private TableColumn<TransactionModel, String> metodePemasukanCol;
    @FXML private TableColumn<TransactionModel, String> catatanPemasukanCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPemasukanCol;
    @FXML private ComboBox<String> filterBulanPemasukanBox;
    @FXML private Button exportPemasukanButton;

    // --- PENGELUARAN FXML Elements ---
    @FXML private TextField deskripsiPengeluaranField;
    @FXML private DatePicker tanggalPengeluaranPicker;
    @FXML private TextField jumlahPengeluaranField;
    @FXML private ComboBox<String> metodePembayaranPengeluaranBox;
    @FXML private ComboBox<String> kategoriPengeluaranBox;
    @FXML private TextField catatanPengeluaranField;
    @FXML private Button tambahPengeluaranButton;
    @FXML private TableView<TransactionModel> pengeluaranTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPengeluaranCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> metodePengeluaranCol;
    @FXML private TableColumn<TransactionModel, String> catatanPengeluaranCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPengeluaranCol;
    @FXML private ComboBox<String> filterBulanPengeluaranBox;
    @FXML private Button exportPengeluaranButton;

    // --- PENGGAJIAN FXML Elements ---
    @FXML private TextField deskripsiPenggajianField;
    @FXML private DatePicker tanggalPenggajianPicker;
    @FXML private TextField jumlahPenggajianField;
    @FXML private ComboBox<String> metodePembayaranPenggajianBox;
    @FXML private ComboBox<String> kategoriPenggajianBox;
    @FXML private TextField catatanPenggajianField;
    @FXML private Button tambahPenggajianButton;
    @FXML private TableView<TransactionModel> penggajianTableView;
    @FXML private TableColumn<TransactionModel, String> deskripsiPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> kategoriPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> tanggalPenggajianCol;
    @FXML private TableColumn<TransactionModel, Number> jumlahPenggajianCol;
    @FXML private TableColumn<TransactionModel, String> metodePenggajianCol;
    @FXML private TableColumn<TransactionModel, String> catatanPenggajianCol;
    @FXML private TableColumn<TransactionModel, Void> aksiPenggajianCol;
    @FXML private ComboBox<String> filterBulanPenggajianBox;
    @FXML private Button exportPenggajianButton;


    private ObservableList<TransactionModel> pemasukanList = FXCollections.observableArrayList();
    private ObservableList<TransactionModel> pengeluaranList = FXCollections.observableArrayList();
    private ObservableList<TransactionModel> penggajianList = FXCollections.observableArrayList();

    private static final DateTimeFormatter TABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("id", "ID"));
    private static final DateTimeFormatter DAY_MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));

    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private UserModel currentUser;

    private TransactionTabManager pemasukanManager;
    private TransactionTabManager pengeluaranManager;
    private TransactionTabManager penggajianManager;

    private FinanceModel financeModel;

    public void initialize() {
        this.financeModel = FinanceModel.getInstance();
        pemasukanManager = new TransactionTabManager("Pemasukan", pemasukanList,
                deskripsiPemasukanField, tanggalPemasukanPicker, jumlahPemasukanField,
                metodePembayaranPemasukanBox, kategoriPemasukanBox, catatanPemasukanField,
                tambahPemasukanButton, pemasukanTableView,
                deskripsiPemasukanCol, kategoriPemasukanCol, tanggalPemasukanCol, jumlahPemasukanCol,
                metodePemasukanCol, catatanPemasukanCol, aksiPemasukanCol, filterBulanPemasukanBox, exportPemasukanButton,
                this);

        pengeluaranManager = new TransactionTabManager("Pengeluaran", pengeluaranList,
                deskripsiPengeluaranField, tanggalPengeluaranPicker, jumlahPengeluaranField,
                metodePembayaranPengeluaranBox, kategoriPengeluaranBox, catatanPengeluaranField,
                tambahPengeluaranButton, pengeluaranTableView,
                deskripsiPengeluaranCol, kategoriPengeluaranCol, tanggalPengeluaranCol, jumlahPengeluaranCol,
                metodePengeluaranCol, catatanPengeluaranCol, aksiPengeluaranCol, filterBulanPengeluaranBox, exportPengeluaranButton,
                this);

        penggajianManager = new TransactionTabManager("Penggajian", penggajianList,
                deskripsiPenggajianField, tanggalPenggajianPicker, jumlahPenggajianField,
                metodePembayaranPenggajianBox, kategoriPenggajianBox, catatanPenggajianField,
                tambahPenggajianButton, penggajianTableView,
                deskripsiPenggajianCol, kategoriPenggajianCol, tanggalPenggajianCol, jumlahPenggajianCol,
                metodePenggajianCol, catatanPenggajianCol, aksiPenggajianCol, filterBulanPenggajianBox, exportPenggajianButton,
                this);

        pemasukanManager.initializeTab(
                Arrays.asList("Penjualan Produk", "Pendapatan Jasa", "Investasi", "Hibah", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai", "Dompet Digital", "Cek"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        pemasukanManager.setupTable(TABLE_DATE_FORMATTER);
        pemasukanManager.loadData();
        tambahPemasukanButton.setOnAction(event -> pemasukanManager.handleTambah());
        pengeluaranManager.initializeTab(
                Arrays.asList("Pakan Ternak", "Obat & Vaksin", "Operasional Kandang", "Perawatan Alat", "Transportasi", "Biaya Listrik/Air", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai", "Dompet Digital", "Kas Peternakan"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        pengeluaranManager.setupTable(TABLE_DATE_FORMATTER);
        pengeluaranManager.loadData();
        tambahPengeluaranButton.setOnAction(event -> pengeluaranManager.handleTambah());
        penggajianManager.initializeTab(
                Arrays.asList("Gaji Karyawan Tetap", "Gaji Karyawan Harian", "Bonus", "Tunjangan", "Lain-lain"),
                Arrays.asList("Transfer Bank", "Tunai"),
                Arrays.asList("Bulan Ini", "Bulan Lalu", "Semua"), "Bulan Ini"
        );
        penggajianManager.setupTable(TABLE_DATE_FORMATTER);
        tambahPenggajianButton.setOnAction(event -> penggajianManager.handleTambah());
        financeModel.getAllTransactions().addListener((ListChangeListener.Change<? extends TransactionModel> c) -> {
        System.out.println("DEBUG: Perubahan terdeteksi di FinanceModel, UI akan di-update.");
        updateLocalListsAndUI();
        });
        // setupOverviewTab();

        // keuanganTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
        //     if (newTab != null && newTab.getText().equals("Overview")) {
        //         refreshOverviewData();
        //     }
        // });

         keuanganTabPane.getSelectionModel().select(0);
        //  updateLocalListsAndUI(); 
    }

    private void updateLocalListsAndUI() {

        pemasukanList.setAll(
            financeModel.getAllTransactions().stream()
                .filter(t -> "Pemasukan".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList())
        );

        pengeluaranList.setAll(
            financeModel.getAllTransactions().stream()
                .filter(t -> "Pengeluaran".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList())
        );

        penggajianList.setAll(
            financeModel.getAllTransactions().stream()
                .filter(t -> "Penggajian".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList())
        );


        // refreshOverviewData();
    }

    // private void setupOverviewTab() {
    //     overviewDeskripsiCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    //     overviewKategoriCol.setCellValueFactory(new PropertyValueFactory<>("category"));
    //     overviewTanggalCol.setCellValueFactory(cellData ->
    //         new javafx.beans.property.SimpleStringProperty(
    //             cellData.getValue().getDate() != null ?
    //             TABLE_DATE_FORMATTER.format(cellData.getValue().getDate()) : ""
    //         )
    //     );
    //     overviewJumlahCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
    //     overviewJumlahCol.setCellFactory(tc -> new TableCell<TransactionModel, Number>() {
    //         @Override
    //         protected void updateItem(Number value, boolean empty) {
    //             super.updateItem(value, empty);
    //             if (empty || value == null) {
    //                 setText(null);
    //             } else {
    //                 setText(CURRENCY_FORMATTER.format(value.doubleValue()));
    //             }
    //         }
    //     });
    //     overviewTipeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
    //     overviewRecentTransactionsTable.setItems(allTransactionsList); 

    //     overviewFilterBulanBox.setItems(FXCollections.observableArrayList("Bulan Ini", "Bulan Lalu", "3 Bulan Terakhir", "Semua"));
    //     overviewFilterBulanBox.setValue("Bulan Ini");
    //     overviewFilterBulanBox.setOnAction(event -> refreshOverviewData());

        
    // }

    @Override
    public void initData(UserModel user) {
        this.currentUser = user;
        // pemasukanManager.loadData();
        // pengeluaranManager.loadData();
        // penggajianManager.loadData();
        // refreshOverviewData();
        updateLocalListsAndUI();
    }
    
    // public void refreshOverviewData() {
    // if (currentUser == null) return;

    // LocalDate now = LocalDate.now();
    // YearMonth currentMonth = YearMonth.from(now);
    // YearMonth previousMonth = currentMonth.minusMonths(1);
 

    // double currentMonthPemasukan = pemasukanList.stream()
    //         .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
    //         .mapToDouble(TransactionModel::getAmount).sum();

    // ObservableList<TransactionModel> combinedExpenses = FXCollections.observableArrayList(pengeluaranList);
    // combinedExpenses.addAll(penggajianList);

    // double currentMonthPengeluaranTotal = combinedExpenses.stream()
    //         .filter(t -> YearMonth.from(t.getDate()).equals(currentMonth))
    //         .mapToDouble(TransactionModel::getAmount).sum();
            
    // double previousMonthPemasukan = pemasukanList.stream()
    //         .filter(t -> YearMonth.from(t.getDate()).equals(previousMonth))
    //         .mapToDouble(TransactionModel::getAmount).sum();

    // double previousMonthPengeluaranTotal = combinedExpenses.stream()
    //         .filter(t -> YearMonth.from(t.getDate()).equals(previousMonth))
    //         .mapToDouble(TransactionModel::getAmount).sum();
    
    // double totalPemasukan = pemasukanList.stream().mapToDouble(TransactionModel::getAmount).sum();
    // double grandTotalPengeluaran = combinedExpenses.stream().mapToDouble(TransactionModel::getAmount).sum();
    // double labaBersih = totalPemasukan - grandTotalPengeluaran;

    // overviewTotalPemasukanLabel.setText(CURRENCY_FORMATTER.format(currentMonthPemasukan));
    // overviewTotalPengeluaranLabel.setText(CURRENCY_FORMATTER.format(currentMonthPengeluaranTotal));
    // overviewLabaBersihLabel.setText(CURRENCY_FORMATTER.format(currentMonthPemasukan - currentMonthPengeluaranTotal));
    // overviewKeuanganSaatIniLabel.setText(CURRENCY_FORMATTER.format(labaBersih)); 
    // overviewKeuanganDateLabel.setText("Per " + DAY_MONTH_YEAR_FORMATTER.format(now));

    // setPercentageLabel(overviewPemasukanPercentageLabel, currentMonthPemasukan, previousMonthPemasukan);
    // setPercentageLabel(overviewPengeluaranPercentageLabel, currentMonthPengeluaranTotal, previousMonthPengeluaranTotal);
    // double currentMonthLaba = currentMonthPemasukan - currentMonthPengeluaranTotal;
    // double previousMonthLaba = previousMonthPemasukan - previousMonthPengeluaranTotal;
    // setPercentageLabel(overviewLabaPercentageLabel, currentMonthLaba, previousMonthLaba);

    // // --- BAR CHART LOGIC (REVISED) ---
    // overviewGrafikKeuanganChart.getData().clear();

    // Map<YearMonth, Double> allMonthlyPemasukan = pemasukanList.stream()
    //     .collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate()), Collectors.summingDouble(TransactionModel::getAmount)));
    // Map<YearMonth, Double> allMonthlyPengeluaran = combinedExpenses.stream()
    //     .collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate()), Collectors.summingDouble(TransactionModel::getAmount)));

    // List<YearMonth> sortedMonths = allMonthlyPemasukan.keySet().stream()
    //     .sorted(Comparator.reverseOrder()).limit(6).sorted().collect(Collectors.toList());}

    // if (sortedMonths.isEmpty()) {
    //     YearMonth startLoop = YearMonth.now().minusMonths(5);
    //     for(int i=0; i<6; i++) { sortedMonths.add(startLoop.plusMonths(i)); }
    // }

//     XYChart.Series<String, Number> pemasukanSeries = new XYChart.Series<>();
//     pemasukanSeries.setName("Pemasukan");
//     XYChart.Series<String, Number> pengeluaranSeries = new XYChart.Series<>();
//     pengeluaranSeries.setName("Pengeluaran");

//     List<String> monthLabels = sortedMonths.stream()
//         .map(ym -> ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("id","ID")))
//         .collect(Collectors.toList());
//     overviewGrafikKeuanganXAxis.setCategories(FXCollections.observableArrayList(monthLabels));

//     for (YearMonth ym : sortedMonths) {
//         String monthStr = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("id","ID"));
//         pemasukanSeries.getData().add(new XYChart.Data<>(monthStr, allMonthlyPemasukan.getOrDefault(ym, 0.0)));
//         pengeluaranSeries.getData().add(new XYChart.Data<>(monthStr, allMonthlyPengeluaran.getOrDefault(ym, 0.0)));
//     }
//     overviewGrafikKeuanganChart.getData().addAll(pemasukanSeries, pengeluaranSeries);

//     // --- Menentukan Filter Tanggal ---
//     String filterSelection = overviewFilterBulanBox.getValue();
//     LocalDate filterEndDate = LocalDate.now();
//     LocalDate filterStartDate;
//     switch (filterSelection) {
//         case "Bulan Lalu":
//             filterStartDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
//             filterEndDate = YearMonth.from(filterStartDate).atEndOfMonth();
//             break;
//         case "3 Bulan Terakhir":
//             filterStartDate = LocalDate.now().minusMonths(2).withDayOfMonth(1);
//             break;
//         case "Semua":
//             filterStartDate = LocalDate.MIN;
//             break;
//         case "Bulan Ini":
//         default:
//             filterStartDate = LocalDate.now().withDayOfMonth(1);
//             break;
//     }
//     final LocalDate finalFilterStartDate = filterStartDate;
//     final LocalDate finalFilterEndDate = filterEndDate;

//     // --- PIE CHART LOGIC (REVISED) ---
//     overviewDistribusiPengeluaranChart.getData().clear();
//     overviewDistribusiPengeluaranChart.setTitle("Distribusi Pengeluaran (" + filterSelection + ")");

//     Map<String, Double> pengeluaranByCategory = combinedExpenses.stream()
//         .filter(t -> !t.getDate().isBefore(finalFilterStartDate) && !t.getDate().isAfter(finalFilterEndDate))
//         .collect(Collectors.groupingBy(TransactionModel::getCategory, Collectors.summingDouble(TransactionModel::getAmount)));

//     ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
//     double totalPieChartExpenses = pengeluaranByCategory.values().stream().mapToDouble(Double::doubleValue).sum();

//     for (Map.Entry<String, Double> entry : pengeluaranByCategory.entrySet()) {
//         double percentage = (totalPieChartExpenses > 0) ? (entry.getValue() / totalPieChartExpenses) * 100 : 0;
//         pieChartData.add(new PieChart.Data(String.format("%s (%.1f%%)", entry.getKey(), percentage), entry.getValue()));
//     }
//     overviewDistribusiPengeluaranChart.setData(pieChartData);

//     // --- TABLE VIEW LOGIC (REVISED) ---
//     List<TransactionModel> filteredForTable = financeModel.getAllTransactions().stream()
//         .filter(t -> !t.getDate().isBefore(finalFilterStartDate) && !t.getDate().isAfter(finalFilterEndDate))
//         .sorted(Comparator.comparing(TransactionModel::getDate).reversed())
//         .collect(Collectors.toList());
    
//     // Menggunakan setAll lebih aman untuk TableView daripada membuat list baru setiap saat
//     overviewRecentTransactionsTable.getItems().setAll(filteredForTable);
// }

//     private void setPercentageLabel(Label label, double currentValue, double previousValue) {
//         double percentageChange = 0;
//         if (previousValue != 0) {
//             percentageChange = ((currentValue - previousValue) / previousValue) * 100;
//         } else if (currentValue > 0) {
//             percentageChange = 100.0; 
//         }

//         String prefix = percentageChange >= 0 ? "+" : "";
//         label.setText(String.format("%s%.1f%%", prefix, percentageChange));
//         if (percentageChange >= 0) {
//             label.setTextFill(Color.GREEN);
//         } else {
//             label.setTextFill(Color.RED);
//         }
//         if (label == overviewLabaPercentageLabel) label.setText(label.getText() + " periode sebelumnya");
//         else label.setText(label.getText() + " dari bulan lalu");

//     }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public boolean validateInput(String deskripsi, LocalDate tanggal, String jumlahStr, String metode, String kategori) {
        if (deskripsi.isEmpty() || tanggal == null || jumlahStr.isEmpty() || metode == null || kategori == null) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Lengkap", "Harap isi semua kolom yang wajib diisi (Deskripsi, Tanggal, Jumlah, Metode, Kategori).");
            return false;
        }
        try {
          
            String cleanJumlahStr = jumlahStr.replace(".", "").replace(",", ".");
            double jumlah = Double.parseDouble(cleanJumlahStr);
            if (jumlah <= 0) throw new NumberFormatException("Jumlah harus positif");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah harus berupa angka positif yang valid.");
            return false;
        }
        return true;
    }

    public void saveTransactionToDB(TransactionModel transaction, ObservableList<TransactionModel> listToUpdate) {
        String sql = "INSERT INTO transactions (transaction_type, description, amount, category, date, payment_method, notes, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, transaction.getTransactionType());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getCategory());
            pstmt.setString(5, DB_DATE_FORMATTER.format(transaction.getDate()));
            pstmt.setString(6, transaction.getPaymentMethod());
            pstmt.setString(7, transaction.getNotes());
            pstmt.setInt(8, transaction.getUserId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setId(generatedKeys.getInt(1));
                    }
                }
                // listToUpdate.add(transaction); 
                
                // refreshOverviewData();
                financeModel.addTransaction(transaction);

                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data " + transaction.getTransactionType().toLowerCase() + " berhasil ditambahkan.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadTransactionData(String transactionType, ObservableList<TransactionModel> targetList) {
        targetList.clear();
        String sql = "SELECT id, transaction_type, description, amount, category, date, payment_method, notes, user_id " +
                     "FROM transactions WHERE transaction_type = ? ";
        
        if (currentUser != null) {
            sql += "AND user_id = ? ";
        }
        sql += "ORDER BY date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transactionType);
            if (currentUser != null) {
                 pstmt.setInt(2, currentUser.getId());
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date"), DB_DATE_FORMATTER);
                targetList.add(new TransactionModel(
                        rs.getInt("id"),
                        rs.getString("transaction_type"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        date,
                        rs.getString("payment_method"),
                        rs.getString("notes"),
                        rs.getInt("user_id")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data " + transactionType.toLowerCase() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addAksiButtonsToTable(TableColumn<TransactionModel, Void> column, String type, TableView<TransactionModel> tableView) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                pane.setAlignment(Pos.CENTER);
                SVGPath editIcon = new SVGPath();
                editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                editIcon.getStyleClass().add("table-action-icon");
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().addAll("action-button", "action-button-edit");

                SVGPath deleteIcon = new SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.getStyleClass().add("table-action-icon");
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("action-button", "action-button-delete");

                editButton.setOnAction(event -> {
                    TransactionModel transaction = tableView.getItems().get(getIndex()); // Use passed tableView
                    handleEditTransaction(transaction, type);
                });

                deleteButton.setOnAction(event -> {
                    TransactionModel transaction = tableView.getItems().get(getIndex()); // Use passed tableView
                    handleDeleteTransaction(transaction, type);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void handleEditTransaction(TransactionModel transaction, String type) {
        TransactionTabManager managerToUse = null;
        switch (type) {
            case "Pemasukan":
                managerToUse = pemasukanManager;
                break;
            case "Pengeluaran":
                managerToUse = pengeluaranManager;
                break;
            case "Penggajian":
                managerToUse = penggajianManager;
                break;
        }
        if (managerToUse != null) {
            managerToUse.populateFormForEdit(transaction);
        }
        showAlert(Alert.AlertType.INFORMATION, "Mode Edit (" + type + ")", "Silakan ubah data. Menyimpan akan membuat entri baru atau Anda perlu mengimplementasikan logic UPDATE jika ingin mengubah data yang ada.");
    }

    public void handleDeleteTransaction(TransactionModel transaction, String type) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus Transaksi " + type + "?");
        alert.setContentText("Apakah Anda yakin ingin menghapus data: " + transaction.getDescription() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM transactions WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, transaction.getId());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data " + type.toLowerCase() + " berhasil dihapus.");
                    financeModel.removeTransaction(transaction);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Data tidak ditemukan untuk dihapus.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}