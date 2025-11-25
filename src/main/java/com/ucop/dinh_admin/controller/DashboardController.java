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

    // --- KHAI BÁO CÁC NÚT ĐỂ PHÂN QUYỀN (Lấy từ Server) ---
    @FXML private Button btnUser;      
    @FXML private Button btnAudit;     
    @FXML private Button btnProduct;   
    @FXML private Button btnCart;      
    @FXML private Button btnOrder;     
    @FXML private Button btnReport;    
    @FXML private Button btnPayment;   

    // --- HÀM KHỞI TẠO ---
    @FXML
    public void initialize() {
        Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            lblWelcome.setText("Xin chào: " + currentUser.getUsername());
            // Áp dụng phân quyền ngay khi mở Dashboard
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

        switch (role.toUpperCase()) {
            case "ADMIN":
                break; // Admin thấy hết

            case "STAFF":
                // Staff ẩn: User, Audit, Báo cáo, Giỏ hàng, Thanh toán
                hideButton(btnUser);
                hideButton(btnAudit);
                hideButton(btnReport);
                hideButton(btnCart);
                hideButton(btnPayment);
                break;

            case "CUSTOMER":
                // Khách ẩn: User, Audit, Kho, Báo cáo
                hideButton(btnUser);
                hideButton(btnAudit);
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

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); 
        }
    }

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

    // --- CÁC SỰ KIỆN MENU (Đã gộp của BẠN và NHÓM) ---

    @FXML public void showUserMgmt() { loadView("UserView.fxml"); }
    
    // [MỚI] Nhật ký hoạt động (Của nhóm)
    @FXML public void showAuditLog() { 
         loadView("AuditView.fxml"); 
        System.out.println("Chức năng Audit đang phát triển");
    } 
    
    // [QUAN TRỌNG] Quản lý Danh mục (Của BẠN - Giữ lại)
    @FXML public void showCategory() { loadView("CategoryView.fxml"); }

    @FXML public void showCatalog() { loadView("ProductView.fxml"); }
    @FXML public void showOrder() { loadView("CartView.fxml"); }
    @FXML public void showPayment() { loadView("PaymentView.fxml"); }
    @FXML public void showReport() { loadView("ReportView.fxml"); }
    @FXML public void showOrderList() { loadView("OrderListView.fxml"); }

    @FXML
    public void handleLogout() {
        try {
            // Đăng xuất
            SessionManager.getInstance().setCurrentUser(null); 
            
            // Chuyển về màn hình Login
            if (mainPane.getScene() != null) {
                Stage stage = (Stage) mainPane.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("UCOP System - Login");
                stage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}