package moomoo.apps.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label; 
import javafx.scene.layout.VBox;
import moomoo.apps.interfaces.ILaporanKontenController; 
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LaporanController {

    @FXML private Button produksiTabButton;
    @FXML private Button keuanganTabButton;
    @FXML private Button sdmTabButton;
    @FXML private ComboBox<String> filterPeriodeComboBox;
    @FXML private VBox dynamicReportContentArea;

    private Map<String, Node> loadedContentNodes = new HashMap<>();

    private ILaporanKontenController activeChildController;

    private Map<String, ILaporanKontenController> loadedControllers = new HashMap<>();

    /* ========== INITIALIZE ========== */
    @FXML
    public void initialize() {
        filterPeriodeComboBox.getItems().addAll("Bulan Ini", "Minggu Ini", "Hari Ini", "Tahun Ini");
        filterPeriodeComboBox.setValue("Bulan Ini");

        filterPeriodeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && activeChildController != null) {
                activeChildController.terapkanFilterPeriode(newVal);
            }
        });

        handleSdmTabAction(null); 
    }

    @FXML
    private void handleProduksiTabAction(ActionEvent event) {
        setTabAktif(produksiTabButton);

        switchContent("/moomoo/apps/view/LaporanProduksiKonten.fxml", "produksi"); 
    }

    @FXML
    private void handleKeuanganTabAction(ActionEvent event) {
        setTabAktif(keuanganTabButton);

        switchContent("/moomoo/apps/view/LaporanFinance.fxml", "keuangan");
    }

    @FXML
    private void handleSdmTabAction(ActionEvent event) {
        setTabAktif(sdmTabButton);

        switchContent("/moomoo/apps/view/LaporanSdmKonten.fxml", "sdm");
    }

    private void switchContent(String fxmlPath, String contentKey) {
        try {
            Node contentNode = loadedContentNodes.get(contentKey);
            ILaporanKontenController childController = loadedControllers.get(contentKey);

            if (contentNode == null || childController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                contentNode = loader.load();
                
                Object controller = loader.getController();
                if (controller instanceof ILaporanKontenController) {
                    childController = (ILaporanKontenController) controller;
                    childController.inisialisasiKonten(); 

                    loadedContentNodes.put(contentKey, contentNode);
                    loadedControllers.put(contentKey, childController);
                } else {
                    throw new RuntimeException("Controller untuk " + fxmlPath + " harus mengimplementasikan ILaporanKontenController.");
                }
            }
            
            activeChildController = childController; 
            dynamicReportContentArea.getChildren().setAll(contentNode);
            
            if (activeChildController != null) {
                activeChildController.terapkanFilterPeriode(filterPeriodeComboBox.getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
            dynamicReportContentArea.getChildren().setAll(new Label("Gagal memuat konten: " + fxmlPath + "\nError: " + e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
             dynamicReportContentArea.getChildren().setAll(new Label("Error konfigurasi controller: " + e.getMessage()));
        }
    }

    private void setTabAktif(Button activeTab) {
        if (produksiTabButton == null || keuanganTabButton == null || sdmTabButton == null) {
             System.err.println("Salah satu tombol tab utama (produksi, keuangan, sdm) adalah null.");
            return;
        }
        produksiTabButton.getStyleClass().remove("tab-button-laporan-active");
        keuanganTabButton.getStyleClass().remove("tab-button-laporan-active");
        sdmTabButton.getStyleClass().remove("tab-button-laporan-active");
        
        if (activeTab != null) {
             activeTab.getStyleClass().add("tab-button-laporan-active");
        }
    }
}