package moomoo.apps.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import moomoo.apps.model.TransactionModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionTabManager {

    private final String transactionType;
    private final ObservableList<TransactionModel> transactionList;


    private final TextField deskripsiField;
    private final DatePicker tanggalPicker;
    private final TextField jumlahField;
    private final ComboBox<String> metodePembayaranBox;
    private final ComboBox<String> kategoriBox;
    private final TextField catatanField;
    private final Button tambahButton;
    private final TableView<TransactionModel> tableView;
    private final TableColumn<TransactionModel, String> deskripsiCol;
    private final TableColumn<TransactionModel, String> kategoriCol;
    private final TableColumn<TransactionModel, String> tanggalCol;
    private final TableColumn<TransactionModel, Number> jumlahCol;
    private final TableColumn<TransactionModel, String> metodeCol;
    private final TableColumn<TransactionModel, String> catatanCol;
    private final TableColumn<TransactionModel, Void> aksiCol;
    private final ComboBox<String> filterBulanBox;
    private final Button exportButton;

    private final KeuanganController mainController; 

    public TransactionTabManager(
            String transactionType, ObservableList<TransactionModel> transactionList,
            TextField deskripsiField, DatePicker tanggalPicker, TextField jumlahField,
            ComboBox<String> metodePembayaranBox, ComboBox<String> kategoriBox, TextField catatanField,
            Button tambahButton, TableView<TransactionModel> tableView,
            TableColumn<TransactionModel, String> deskripsiCol, TableColumn<TransactionModel, String> kategoriCol,
            TableColumn<TransactionModel, String> tanggalCol, TableColumn<TransactionModel, Number> jumlahCol,
            TableColumn<TransactionModel, String> metodeCol, TableColumn<TransactionModel, String> catatanCol,
            TableColumn<TransactionModel, Void> aksiCol, ComboBox<String> filterBulanBox, Button exportButton,
            KeuanganController mainController) {

        this.transactionType = transactionType;
        this.transactionList = transactionList;
        this.deskripsiField = deskripsiField;
        this.tanggalPicker = tanggalPicker;
        this.jumlahField = jumlahField;
        this.metodePembayaranBox = metodePembayaranBox;
        this.kategoriBox = kategoriBox;
        this.catatanField = catatanField;
        this.tambahButton = tambahButton;
        this.tableView = tableView;
        this.deskripsiCol = deskripsiCol;
        this.kategoriCol = kategoriCol;
        this.tanggalCol = tanggalCol;
        this.jumlahCol = jumlahCol;
        this.metodeCol = metodeCol;
        this.catatanCol = catatanCol;
        this.aksiCol = aksiCol;
        this.filterBulanBox = filterBulanBox;
        this.exportButton = exportButton;
        this.mainController = mainController;
    }

    public void initializeTab(List<String> kategoriItems, List<String> metodePembayaranItems, List<String> filterBulanItems, String defaultFilterValue) {
        kategoriBox.setItems(javafx.collections.FXCollections.observableArrayList(kategoriItems));
        metodePembayaranBox.setItems(javafx.collections.FXCollections.observableArrayList(metodePembayaranItems));
        filterBulanBox.setItems(javafx.collections.FXCollections.observableArrayList(filterBulanItems));
        filterBulanBox.setValue(defaultFilterValue);

    }

    public void setupTable(DateTimeFormatter tableDateFormatter) {
        deskripsiCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        tanggalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDate() != null ?
                        tableDateFormatter.format(cellData.getValue().getDate()) : ""
                )
        );
        jumlahCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        jumlahCol.setCellFactory(tc -> new TableCell<TransactionModel, Number>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                   
                    setText(String.format("Rp %,.0f", value.doubleValue()));
                }
            }
        });
        metodeCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        catatanCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        mainController.addAksiButtonsToTable(aksiCol, transactionType, tableView); 

        tableView.setItems(transactionList);
    }

    public void loadData() {
        mainController.loadTransactionData(transactionType, transactionList);
    }

    public void handleTambah() {
        String deskripsi = deskripsiField.getText();
        LocalDate tanggal = tanggalPicker.getValue();
        String jumlahStr = jumlahField.getText();
        String metode = metodePembayaranBox.getValue();
        String kategori = kategoriBox.getValue();
        String catatan = catatanField.getText();

        double jumlah = Double.parseDouble(jumlahStr.replace(",", ".")); 
        int userIdToSave = (mainController.getCurrentUser() != null) ? mainController.getCurrentUser().getId() : 0;
        TransactionModel newTransaction = new TransactionModel(
                transactionType, deskripsi, jumlah, kategori, tanggal, metode, catatan, userIdToSave);
        mainController.saveTransactionToDB(newTransaction, transactionList);
        clearForm();
    }

    public void clearForm() {
        deskripsiField.clear();
        tanggalPicker.setValue(null);
        jumlahField.clear();
        metodePembayaranBox.getSelectionModel().clearSelection();
        kategoriBox.getSelectionModel().clearSelection();
        catatanField.clear();
    }
    
    public void populateFormForEdit(TransactionModel transaction) {
        deskripsiField.setText(transaction.getDescription());
        tanggalPicker.setValue(transaction.getDate());
        jumlahField.setText(String.valueOf(transaction.getAmount()).replace(".", ",")); 
        metodePembayaranBox.setValue(transaction.getPaymentMethod());
        kategoriBox.setValue(transaction.getCategory());
        catatanField.setText(transaction.getNotes());
    }
}