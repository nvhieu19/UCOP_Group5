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

    // --- HÀM TIỆN ÍCH LOAD VIEW ---
    private void loadView(String fxmlFile) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            lblWelcome.setText("Lỗi: Không tìm thấy file " + fxmlFile + " trong thư mục /fxml/");
        }
    }

    // --- CÁC SỰ KIỆN MENU ---

    @FXML
    public void showUserMgmt() {
        loadView("UserView.fxml");
    }

    @FXML
    public void showCatalog() {
        loadView("ProductView.fxml");
    }

    @FXML
    public void showOrder() {
        // Đã sửa tên file cho khớp với hướng dẫn trước (CartView.fxml)
        loadView("CartView.fxml");
    }

    @FXML
    public void showPayment() {
        // Sắp làm: PaymentView.fxml
        loadView("PaymentView.fxml");
    }

    @FXML
    public void showReport() {
        loadView("ReportView.fxml");
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
    @FXML
    public void showOrderList() {
        loadView("OrderListView.fxml");
    }
} 