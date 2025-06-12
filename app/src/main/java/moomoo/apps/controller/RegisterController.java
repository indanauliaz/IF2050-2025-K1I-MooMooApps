package moomoo.apps.controller;

import moomoo.apps.utils.*;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; 
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
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

public class RegisterController implements Initializable { 
    
    @FXML private ImageView logoView;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField; 
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton; 
    @FXML private Label hideLabel;      
    @FXML private Button loginLinkButton; 
    @FXML private Label errorMessageLabel; 

    private boolean isPasswordVisible = false;

    /* ========== INITIALIZE ========== */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        roleComboBox.setItems(FXCollections.observableArrayList("Pemilik", "Manajer")); 
        roleComboBox.setValue("Pemilik"); 

        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        passwordTextField.visibleProperty().bind(passwordField.visibleProperty().not()); 


        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        

        passwordField.setVisible(true);

        hideLabel.setText("Show"); 
    }

    @FXML
    void handleRegisterButtonAction (ActionEvent event){
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        resetFieldStyles();

        boolean hasError = false;
        StringBuilder errorMsg = new StringBuilder();

        if (username.isEmpty()) {
            usernameField.setStyle("-fx-border-color: red;");
            hasError = true;
        }
        if (email.isEmpty()) {
            emailField.setStyle("-fx-border-color: red;");
            hasError = true;
        }
        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red;");
            hasError = true;
        }
        if (role == null || role.isEmpty()) {
            roleComboBox.setStyle("-fx-border-color: red;");
            hasError = true;
        }

        if (hasError) {
            showInlineError("Harap isi semua kolom dengan benar.");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            emailField.setStyle("-fx-border-color: red;");
            showInlineError("Format email tidak valid.");
            return;
        }

        if (password.length() < 6) {
            passwordField.setStyle("-fx-border-color: red;");
            showInlineError("Password minimal 6 karakter.");
            return;
        }

        if (isUserExists(username, email)) {
            return;
        }

        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(password, salt);

        String sql = "INSERT INTO users (username, email, password_hash, role) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            
            pstmt.setString(1,username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, role);
            pstmt.executeUpdate();

            System.out.println("Pengguna baru terdaftar: " + username + " (Data tersimpan di database)");
            showAlert(Alert.AlertType.INFORMATION, "Registrasi Berhasil!", "Akun " + username + " telah berhasil dibuat. Silakan login.");
            clearFields();
            handleLoginLinkAction(null); 

        } catch (SQLException e){
            showAlert(Alert.AlertType.ERROR, "Database Error!", "Gagal menyimpan pengguna: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isUserExists(String username, String email) {
        String sqlCheck = "SELECT id FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
            pstmtCheck.setString(1, username);
            pstmtCheck.setString(2, email);
            ResultSet rs = pstmtCheck.executeQuery();
            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Registrasi Gagal", "Username atau Email sudah digunakan.");
                return true;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memeriksa pengguna: " + e.getMessage());
            e.printStackTrace();
            return true; 
        }
        return false;
    }

    @FXML
    void handleLoginLinkAction(ActionEvent event) {

        javafx.scene.Node sourceNode;
        if (event != null && event.getSource() instanceof javafx.scene.Node) {
            sourceNode = (javafx.scene.Node) event.getSource();
        } else {

            sourceNode = registerButton; 
        }

        System.out.println("Navigasi ke halaman Login...");
        try {

            URL fxmlLocation = getClass().getResource("/moomoo/apps/view/LoginView.fxml"); 
            if (fxmlLocation == null) {
                System.err.println("File LoginView.fxml tidak ditemukan dari RegisterController.");
                showAlert(Alert.AlertType.ERROR, "Load Error", "Gagal menemukan file halaman login.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Moo Moo Apps");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Gagal memuat halaman login: " + e.getMessage());
        }
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear(); 

        if (!roleComboBox.getItems().isEmpty()) {
            roleComboBox.setValue(roleComboBox.getItems().get(0)); 
        }

        resetFieldStyles();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #FAF8F0;
            -fx-background-radius: 10px;
            -fx-font-family: 'Poppins Medium';
            -fx-font-size: 14px;
        """);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("""
            -fx-background-color: #4A7C7A;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            -fx-background-radius: 8px;
            -fx-cursor: hand;
        """);

        alert.showAndWait();
    }

    private void showInlineError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
        errorMessageLabel.setManaged(true);
    }

    private void resetFieldStyles() {
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setManaged(false);
        errorMessageLabel.setText("");

        usernameField.setStyle(null);
        emailField.setStyle(null);
        passwordField.setStyle(null);
        roleComboBox.setStyle(null);
    }

    
    @FXML
    void togglePasswordVisibility(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {

            passwordField.setVisible(false); 

            hideLabel.setText("Hide");
        } else {

            passwordField.setVisible(true);

            hideLabel.setText("Show");
        }
    }
}