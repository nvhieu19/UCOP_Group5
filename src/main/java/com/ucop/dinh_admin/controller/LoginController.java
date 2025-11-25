package com.ucop.dinh_admin.controller;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.UserService;
import com.ucop.dinh_admin.service.SessionManager; // Thêm import
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Đã thêm thư viện nút bấm
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblMessage;
    
    // --- BỔ SUNG KHAI BÁO NÚT LOGIN ---
    @FXML private Button btnLogin; 

    private UserService userService = new UserService();

    @FXML
    public void handleLogin() {
        String u = txtUser.getText();
        String p = txtPass.getText();

        Dinh_User user = userService.login(u, p);

        if (user != null) {
            // LƯU USER VÀO SESSION
            SessionManager.getInstance().setCurrentUser(user);

            try {
                // 1. Load file giao diện Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
                Parent root = loader.load();

                // 2. Lấy Controller của Dashboard để truyền dữ liệu (Ví dụ tên User)
                DashboardController dashboardCtrl = loader.getController();
                dashboardCtrl.setUsername(user.getUsername());

                // 3. Lấy cửa sổ hiện tại (Stage) thông qua nút Login
                Stage stage = (Stage) btnLogin.getScene().getWindow();
                
                // 4. Chuyển cảnh
                stage.setScene(new Scene(root));
                stage.setTitle("UCOP System - Dashboard Main");
                stage.centerOnScreen(); // Căn giữa màn hình

            } catch (Exception e) {
                e.printStackTrace();
                lblMessage.setText("Lỗi mở Dashboard: " + e.getMessage());
            }
        } else {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Sai tài khoản hoặc mật khẩu!");
        }
    }
}