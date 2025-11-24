package com.ucop.dinh_admin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class DashboardController {

    @FXML 
    private Label lblWelcome;

    @FXML 
    private BorderPane mainPane; 

    // Hàm nhận dữ liệu từ Login
    public void setUsername(String username) {
        lblWelcome.setText("Xin chào, " + username + "!");
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN MENU ---

    @FXML
    public void showUserMgmt() {
        try {
            // Load giao diện Quản lý User
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/UserView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblWelcome.setText("Lỗi: Không tìm thấy file UserView.fxml!");
        }
    }

    @FXML
    public void showCatalog() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/ProductView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblWelcome.setText("Lỗi: Không tìm thấy file ProductView.fxml!");
        }
    }

    @FXML
    public void showOrder() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/CartView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblWelcome.setText("Lỗi: Không tìm thấy file CartView.fxml!");
        }
    }

    @FXML
    public void showPayment() {
        lblWelcome.setText("Đang mở chức năng Thanh toán (Của Long)...");
    }

    @FXML
    public void showReport() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/ReportView.fxml"));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblWelcome.setText("Lỗi: Không tìm thấy file ReportView.fxml!");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}