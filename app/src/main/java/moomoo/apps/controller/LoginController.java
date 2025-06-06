package moomoo.apps.controller;

import moomoo.apps.model.UserModel;
import moomoo.apps.utils.DatabaseManager;
import moomoo.apps.utils.PasswordUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private ImageView logoView;
    @FXML private TextField usernameEmailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label hideLabel;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private ComboBox<String> roleComboBoxLogin;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private boolean passwordVisible = false;
    private UserModel loggedInUser; 

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBoxLogin.setItems(FXCollections.observableArrayList("Pemilik", "Manajer"));
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        hideLabel.setText("Show");
    }

    @FXML
    void togglePasswordVisibility(MouseEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            hideLabel.setText("Hide");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            hideLabel.setText("Show");
        }
    }

    @FXML
    void handleLoginButtonAction(ActionEvent event) {
        String usernameOrEmail = usernameEmailField.getText();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();
        String selectedRole = roleComboBoxLogin.getValue();
        
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username/Email dan Password tidak boleh kosong.");
            return;
        }
        if (selectedRole == null || selectedRole.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Role harus dipilih.");
            return;
        }

        String sql = "SELECT id, username, email, password_hash, role FROM users WHERE (username = ? OR email = ?) AND role = ?";
        
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, selectedRole); 
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (PasswordUtils.verifyPassword(password, rs.getString("password_hash"))) {
                    this.loggedInUser = new UserModel(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getString("role"));
                    showAlert(Alert.AlertType.INFORMATION, "Login Berhasil", "Selamat datang " + this.loggedInUser.getUsername() + "!");
                    navigateToDashboard(this.loggedInUser);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Gagal", "Password salah.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username/Email atau Role tidak cocok/ditemukan.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengambil data pengguna: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(UserModel user) {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Navigasi Gagal", "Data pengguna tidak valid.");
            return;
        }

        String fxmlFile;
        String role = user.getRole();

        // --- INI PERUBAHAN PENTING ---
        switch (role.toLowerCase()) {
            case "manajer":
                fxmlFile = "/moomoo/apps/view/DashboardView.fxml"; // FXML untuk manajer
                break;
            case "pemilik":
                fxmlFile = "/moomoo/apps/view/DashboardPemilikView.fxml"; // FXML baru untuk pemilik
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Navigasi Gagal", "Role '" + role + "' tidak dikenal.");
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent dashboardRoot = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboardRoot);
            // Hubungkan stylesheet
            scene.getStylesheets().add(getClass().getResource("/moomoo/apps/view/dashboard_style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(role + " Dashboard - Moo Moo Apps");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat UI", "Gagal memuat halaman dashboard: " + fxmlFile + "\nPastikan file ada dan path sudah benar.");
        }
    }

    @FXML
    void handleRegisterLinkAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/moomoo/apps/view/RegisterView.fxml"));
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
