package com.ucop.dinh_admin.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

// [MỚI] Import Controller của Long (Payment)
import com.ucop.long_payment.controller.PaymentController;

public class DashboardController {

    @FXML 
    private Label lblWelcome;

    @FXML 
    private BorderPane mainPane; 

    // [MỚI] Biến để lưu username người dùng đang đăng nhập (để truyền sang các màn hình khác)
    private String currentUsername;

    // Hàm nhận dữ liệu từ Login
    public void setUsername(String username) {
        // [MỚI] Lưu lại username vào biến
        this.currentUsername = username;
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

    // [ĐÃ CẬP NHẬT] Hàm mở chức năng Payment của Long
    @FXML
    public void showPayment() {
<<<<<<< HEAD
        try {
            // 1. Load giao diện từ file FXML của Long
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentView.fxml"));
            Parent view = loader.load();

            // 2. Lấy Controller của Long để truyền username sang
            // (Để bên Payment biết là đang thao tác ví của ai)
            PaymentController paymentCtrl = loader.getController();
            paymentCtrl.setUsername(currentUsername);

            // 3. Hiển thị lên màn hình chính
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Nếu lỗi, hiện thông báo lên màn hình cho dễ debug
            lblWelcome.setText("Lỗi: Không tìm thấy file PaymentView.fxml hoặc lỗi code Controller!");
        }
=======
        // Sắp làm: PaymentView.fxml
        loadView("PaymentView.fxml");
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
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