package moomoo.apps.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import moomoo.apps.model.TaskModel;
import moomoo.apps.utils.DatabaseManager;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TugasPemilikController {

    @FXML private Label totalTugasLabel;
    @FXML private Label totalSelesaiLabel;
    @FXML private Label sedangDikerjakanLabel;
    @FXML private Label efisiensiLabel;
    @FXML private ComboBox<String> filterPeriodeComboBox;
    @FXML private ComboBox<String> filterDepartemenComboBox;
    @FXML private BarChart<String, Number> distribusiTugasChart;
    @FXML private PieChart kinerjaKaryawanChart;
    @FXML private TableView<TaskModel> tugasTableView;
    @FXML private TableColumn<TaskModel, String> tugasColumn;
    @FXML private TableColumn<TaskModel, String> penanggungJawabColumn;
    @FXML private TableColumn<TaskModel, String> departemenColumn;
    @FXML private TableColumn<TaskModel, String> prioritasColumn;
    @FXML private TableColumn<TaskModel, String> tenggatColumn;
    @FXML private TableColumn<TaskModel, String> statusColumn;

    private ObservableList<TaskModel> masterTugasList = FXCollections.observableArrayList();

    @FXML 
    public void initialize() {
        setupKolomTabel();
        muatDataAwal();
        setupFilter();
        setupListeners();
        perbaruiTampilan();
    }

    private void muatDataAwal() { 
        masterTugasList.setAll(DatabaseManager.getAllTasks()); 
    }

    private void setupFilter() {
        filterPeriodeComboBox.setItems(FXCollections.observableArrayList("Semua", "Minggu Ini", "Bulan Ini"));
        filterPeriodeComboBox.setValue("Semua");
        List<String> departemenList = masterTugasList.stream()
            .map(TaskModel::getDepartemen)
            .filter(d -> d != null && !d.isBlank())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        departemenList.add(0, "Semua");
        filterDepartemenComboBox.setItems(FXCollections.observableArrayList(departemenList));
        filterDepartemenComboBox.setValue("Semua");
    }
    
    private void setupListeners() {
        filterDepartemenComboBox.valueProperty().addListener((obs, o, n) -> perbaruiTampilan());
        filterPeriodeComboBox.valueProperty().addListener((obs, o, n) -> perbaruiTampilan());
    }
    
    private void setupKolomTabel() {
        tugasColumn.setCellValueFactory(c -> c.getValue().namaTugasProperty());
        penanggungJawabColumn.setCellValueFactory(c -> c.getValue().namaKaryawanPenanggungJawabProperty());
        departemenColumn.setCellValueFactory(c -> c.getValue().departemenProperty());
        prioritasColumn.setCellValueFactory(c -> c.getValue().prioritasProperty());
        statusColumn.setCellValueFactory(c -> c.getValue().statusProperty());
        tenggatColumn.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().tanggalTugasProperty().get().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        ));
        
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override 
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().removeAll("status-selesai", "status-dikerjakan", "status-tertunda", "status-default");
                if (item != null && !empty) {
                    switch (item.toLowerCase()) {
                        case "selesai": getStyleClass().add("status-selesai"); break;
                        case "sedang dikerjakan": getStyleClass().add("status-dikerjakan"); break;
                        case "tertunda": getStyleClass().add("status-tertunda"); break;
                        default: getStyleClass().add("status-default"); break;
                    }
                }
            }
        });
    }

    private void perbaruiTampilan() {
        String deptFilter = filterDepartemenComboBox.getValue();
        ObservableList<TaskModel> filteredList = masterTugasList.stream()
            .filter(t -> (deptFilter == null || "Semua".equals(deptFilter) || deptFilter.equals(t.getDepartemen())))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        updateUI(filteredList);
    }
    
    private void updateUI(ObservableList<TaskModel> taskList) {
        long selesai = taskList.stream().filter(t -> "Selesai".equalsIgnoreCase(t.getStatus())).count();
        long dikerjakan = taskList.stream().filter(t -> "Sedang Dikerjakan".equalsIgnoreCase(t.getStatus())).count();
        long tertunda = taskList.stream().filter(t -> "Tertunda".equalsIgnoreCase(t.getStatus())).count();
        totalTugasLabel.setText(String.valueOf(taskList.size()));
        totalSelesaiLabel.setText(String.valueOf(selesai));
        sedangDikerjakanLabel.setText(String.valueOf(dikerjakan));
        efisiensiLabel.setText(taskList.isEmpty() ? "0%" : String.format("%.0f%%", (double) selesai / taskList.size() * 100));

        distribusiTugasChart.getData().clear();
        Map<String, Long> distByDept = taskList.stream()
            .filter(t -> t.getDepartemen() != null)
            .collect(Collectors.groupingBy(TaskModel::getDepartemen, Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        distByDept.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
        distribusiTugasChart.getData().add(series);

        kinerjaKaryawanChart.getData().clear();
        kinerjaKaryawanChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Selesai (" + selesai + ")", selesai),
                new PieChart.Data("Dikerjakan ("+ dikerjakan +")", dikerjakan),
                new PieChart.Data("Tertunda ("+ tertunda +")", tertunda)
        ));
        tugasTableView.setItems(taskList);
    }
}
