package com.ucop.dinh_admin.controller;

import com.ucop.dinh_admin.service.SessionManager;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.Dinh_Role;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class DashboardController {

    @FXML private Label lblWelcome;
    @FXML private BorderPane mainPane; 

    // --- KHAI BÁO CÁC NÚT TRÊN MENU ĐỂ ẨN/HIỆN ---
    @FXML private Button btnUser;      // Nút Quản lý User
    @FXML private Button btnAudit;     // [MỚI] Nút Audit Log
    @FXML private Button btnProduct;   // Nút Kho & SP (Catalog)
    @FXML private Button btnCart;      // Nút Giỏ hàng
    @FXML private Button btnOrder;     // Nút Đơn hàng
    @FXML private Button btnReport;    // Nút Báo cáo
    @FXML private Button btnPayment;   // Nút Thanh toán

    // --- HÀM KHỞI TẠO ---
    @FXML
    public void initialize() {
        Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            lblWelcome.setText("Xin chào: " + currentUser.getUsername());
            applyPermissions(currentUser);
        }
    }

    // --- LOGIC PHÂN QUYỀN (RBAC) ---
    private void applyPermissions(Dinh_User user) {
        String role = "CUSTOMER"; 
        
        Set<Dinh_Role> roles = user.getRoles();
        if (roles != null && !roles.isEmpty()) {
            role = roles.iterator().next().getRoleName(); 
        }

        System.out.println("Current Role: " + role); 

        switch (role.toUpperCase()) {
            case "ADMIN":
                // Admin thấy hết -> Không cần ẩn gì cả
                break;

            case "STAFF":
                // Staff chỉ làm Kho & SP, Xử lý đơn hàng
                // -> Ẩn: Quản lý User, Audit, Báo cáo, Giỏ hàng, Thanh toán
                hideButton(btnUser);
                hideButton(btnAudit);   // [MỚI] Staff ko được xem Audit Log
                hideButton(btnReport);
                hideButton(btnCart);
                hideButton(btnPayment);
                break;

            case "CUSTOMER":
                // Khách hàng chỉ thấy Giỏ hàng, Đơn hàng, Thanh toán
                // -> Ẩn: Quản lý User, Audit, Kho, Báo cáo
                hideButton(btnUser);
                hideButton(btnAudit);   // [MỚI] Khách ko được xem Audit Log
                hideButton(btnProduct);
                hideButton(btnReport);
                break;

            default:
                hideButton(btnUser);
                hideButton(btnAudit);
                hideButton(btnReport);
                break;
        }
    }

    // Hàm phụ trợ để ẩn nút
    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); 
        }
    }

    // --- GIỮ NGUYÊN CÁC HÀM CŨ ---
    public void setUsername(String username) {
        if (lblWelcome != null) lblWelcome.setText("Xin chào: " + username);
    }

    private void loadView(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                lblWelcome.setText("Lỗi: Không tìm thấy file " + fxmlFile);
                mainPane.setCenter(null);
            } else {
                Parent view = FXMLLoader.load(fxmlUrl);
                mainPane.setCenter(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- SỰ KIỆN MENU (Đã thêm showAuditLog) ---
    @FXML public void showUserMgmt() { loadView("UserView.fxml"); }
    
    @FXML public void showAuditLog() { loadView("AuditView.fxml"); } // [MỚI] Hàm mở trang Audit
    
    @FXML public void showCatalog() { loadView("ProductView.fxml"); }
    @FXML public void showOrder() { loadView("CartView.fxml"); }
    @FXML public void showPayment() { loadView("PaymentView.fxml"); }
    @FXML public void showReport() { loadView("ReportView.fxml"); }
    @FXML public void showOrderList() { loadView("OrderListView.fxml"); }

    @FXML
    public void handleLogout() {
        try {
            SessionManager.getInstance().logout();
            if (mainPane.getScene() != null) {
                Stage stage = (Stage) mainPane.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}