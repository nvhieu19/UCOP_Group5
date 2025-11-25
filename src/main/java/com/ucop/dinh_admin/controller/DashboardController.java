package com.ucop.dinh_admin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

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
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                // If the file is not found, show an error message instead of crashing.
                lblWelcome.setText("Lỗi: Không tìm thấy file " + fxmlFile + " trong thư mục /fxml/");
                mainPane.setCenter(null); // Clear the center pane
            } else {
                Parent view = FXMLLoader.load(fxmlUrl);
                mainPane.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
    
    // THÊM MỚI: Phương thức mở màn hình Quản lý Danh mục
    @FXML
    public void showCategory() {
        loadView("CategoryView.fxml");
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
            // SỬA Ở ĐÂY: Dùng mainPane thay vì lblWelcome
            // mainPane là khung gốc, lúc nào cũng có mặt trên cửa sổ
            if (mainPane.getScene() == null) {
                System.out.println("Lỗi: MainPane chưa được gắn vào Scene");
                return;
            }

            Stage stage = (Stage) mainPane.getScene().getWindow();
            
            // Load lại màn hình Login
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("UCOP System - Login");
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