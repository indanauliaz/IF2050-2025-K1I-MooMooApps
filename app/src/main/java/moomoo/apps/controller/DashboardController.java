package moomoo.apps.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import moomoo.apps.interfaces.UserAwareController;
import moomoo.apps.model.UserModel;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleGroup menuToggleGroup;

    @FXML
    private ToggleButton dashboardButton;

    @FXML
    private ToggleButton laporanButton;

    @FXML
    private ToggleButton keuanganButton;

    @FXML
    private ToggleButton tugasButton;

    @FXML
    private ToggleButton produksiButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private TextField searchField;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private ScrollPane mainContentScrollPane;

    private UserModel currentUser;
    private Node originalDashboardContent;

    public void initialize() {
        menuToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                if (oldValue != null) {
                    oldValue.setSelected(true);
                }
            } else {
                handleSidebarNavigation((ToggleButton) newValue);
            }
        });

        dashboardButton.setSelected(true);

        Platform.runLater(() -> {
            if (mainContentScrollPane != null) {
                originalDashboardContent = mainContentScrollPane.getContent();
                System.out.println("Saved original dashboard content.");
            }
        });
    }

    public void initData(UserModel user) {
        this.currentUser = user;
        updateUserInfo(user);
    }

    private void updateUserInfo(UserModel user) {
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole());
        }
    }

    private void handleSidebarNavigation(ToggleButton selectedButton) {
        String viewPath = null;

        if (selectedButton == dashboardButton) {
            System.out.println("DEBUG: Dashboard selected");
            loadDashboardFXMLContent();
            return;
        } else if (selectedButton == laporanButton) {
            System.out.println("DEBUG: Laporan selected");
            viewPath = "/moomoo/apps/view/LaporanView.fxml";
        } else if (selectedButton == keuanganButton) {
            System.out.println("DEBUG: Keuangan selected");
            viewPath = "/moomoo/apps/view/FinanceView.fxml";
        } else if (selectedButton == tugasButton) {
            System.out.println("DEBUG: Tugas selected");
            viewPath = "/moomoo/apps/view/TaskManagement.fxml";
        } else if (selectedButton == produksiButton) {
            System.out.println("DEBUG: Produksi selected");
            viewPath = "/moomoo/apps/view/ProductionView.fxml";
        }

        if (viewPath != null) {
            loadView(viewPath, this.currentUser);
        } else if (selectedButton != dashboardButton) {
            showPlaceholderView("Konten untuk " + selectedButton.getText());
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        System.out.println("Logout action triggered");
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

    private void loadView(String fxmlPath, UserModel user) {
        try {
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("ERROR: FXML Resource not found: " + fxmlPath);
                showErrorInScrollPane("File FXML tidak ditemukan: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent view = loader.load();

            Object controller = loader.getController();

            if (user != null && controller instanceof UserAwareController) {
                ((UserAwareController) controller).initData(user);
            }

            mainContentScrollPane.setContent(view);
            mainContentScrollPane.setFitToWidth(true);
            mainContentScrollPane.setFitToHeight(true);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorInScrollPane("Gagal memuat tampilan: " + fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorInScrollPane("Terjadi kesalahan saat memuat: " + fxmlPath);
        }
    }

    private void showPlaceholderView(String pageName) {
        VBox placeholderContent = new VBox();
        placeholderContent.setPadding(new javafx.geometry.Insets(20));
        placeholderContent.setAlignment(Pos.CENTER);
        Label placeholderLabel = new Label("Ini Halaman " + pageName + " (Belum Diimplementasikan)");
        placeholderLabel.setStyle("-fx-font-size: 18px;");
        placeholderContent.getChildren().add(placeholderLabel);
        mainContentScrollPane.setContent(placeholderContent);
        mainContentScrollPane.setFitToWidth(true);
        mainContentScrollPane.setFitToHeight(true);
    }

    private void showErrorInScrollPane(String message) {
        VBox errorContainer = new VBox();
        errorContainer.setPadding(new javafx.geometry.Insets(20));
        errorContainer.setAlignment(Pos.CENTER);
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-wrap-text: true;");
        errorContainer.getChildren().add(errorLabel);
        mainContentScrollPane.setContent(errorContainer);
        mainContentScrollPane.setFitToWidth(true);
        mainContentScrollPane.setFitToHeight(true);
    }

    private void loadDashboardFXMLContent() {
        if (originalDashboardContent != null) {
            mainContentScrollPane.setContent(originalDashboardContent);
            mainContentScrollPane.setFitToWidth(true);
            mainContentScrollPane.setFitToHeight(true);
            System.out.println("Dashboard content restored from original node.");
        } else {
            System.err.println("Original dashboard content is null!");
        }
    }
}
