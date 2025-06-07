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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
    @FXML
    private ImageView logoView;

    @FXML
    private TextField usernameEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Label hideLabel;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private ComboBox<String> roleComboBoxLogin;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    private boolean passwordVisible = false;
    private UserModel loggedInUser; 

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBoxLogin.setItems(FXCollections.observableArrayList(
                "Pemilik", "Manajer"
        ));


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
        boolean rememberMe = rememberMeCheckBox.isSelected();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username/Email dan Password tidak boleh kosong.");
            return;
        }
        
        if (selectedRole == null || selectedRole.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Role harus dipilih.");
            return;
        }

        System.out.println("Attempting login for: " + usernameOrEmail);
        System.out.println("Selected Role: " + selectedRole);
        System.out.println("Remember Me: " + rememberMe);

        String sql = "SELECT id, username, email, password_hash, role FROM users WHERE (username = ? OR email = ?) AND role = ?";
        
        this.loggedInUser = null; 

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            pstmt.setString(3, selectedRole); 
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");
                if (PasswordUtils.verifyPassword(password, storedPasswordHash)) {
                    this.loggedInUser = new UserModel(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("role")
                    );
                    

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
        } catch (Exception e) { 
            showAlert(Alert.AlertType.ERROR, "System Error", "Terjadi kesalahan sistem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(UserModel user) {
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Navigasi Gagal", "Data pengguna tidak valid.");
            return;
        }

        String fxmlFile = "";
        String role = user.getRole();

        switch (role.toLowerCase()) {
            case "manajer":
                fxmlFile = "/moomoo/apps/view/DashboardView.fxml"; 
                break;
            case "pemilik":
                fxmlFile = "/moomoo/apps/view/DashboardView.fxml"; 
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Navigasi Gagal", "Role '" + role + "' tidak dikenal, tidak dapat melanjutkan.");
                return;
        }

        try {
            URL fxmlLocation = getClass().getResource(fxmlFile);
            if (fxmlLocation == null) {
                System.err.println("FXML file not found: " + fxmlFile);
                showAlert(Alert.AlertType.ERROR, "Error Navigasi", "File dashboard untuk role '" + role + "' tidak ditemukan.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent dashboardRoot = loader.load();


            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboardRoot);
            stage.setScene(scene);
            stage.setTitle(role + " Dashboard - Moo Moo Apps");
            stage.centerOnScreen(); 
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Memuat UI", "Gagal memuat halaman dashboard: " + e.getMessage());
        }
    }


    @FXML
    void handleRegisterLinkAction(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/moomoo/apps/view/RegisterView.fxml");
            if (fxmlLocation == null) {
                System.err.println("File RegisterView.fxml tidak ditemukan untuk navigasi.");
                showAlert(Alert.AlertType.ERROR, "Error Navigasi", "File halaman registrasi tidak ditemukan.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) registerLink.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Registrasi Moo Moo Apps");
            stage.centerOnScreen(); 
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat halaman registrasi.");
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