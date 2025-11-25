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
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.Optional;

public class DashboardController {

    @FXML private Label lblWelcome;
    @FXML private BorderPane mainPane; 

    // --- KHAI BÁO CÁC NÚT ĐỂ PHÂN QUYỀN (Lấy từ Server) ---
    @FXML private Button btnUser;      
    @FXML private Button btnAudit;     
    @FXML private Button btnCategory;
    @FXML private Button btnProduct;   
    @FXML private Button btnCart;      
    @FXML private Button btnOrder;     
    @FXML private Button btnReport;    
    @FXML private Button btnPayment;
    @FXML private Button btnShipment;  // Thêm nút Shipment   

    // --- HÀM KHỞI TẠO ---
    @FXML
    public void initialize() {
        Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            lblWelcome.setText("Xin chào: " + currentUser.getUsername());
            // Áp dụng phân quyền ngay khi mở Dashboard
            applyPermissions(currentUser);
        } else {
            // Nếu không có user, ẩn hết để an toàn
            hideAllButtons();
        }
    }

    // --- LOGIC PHÂN QUYỀN (RBAC) ---
    private void applyPermissions(Dinh_User user) {
        // 1) Ẩn tất cả trước để tránh trạng thái thừa
        hideAllButtons();

        // 2) Kiểm tra tất cả roles (user có thể có nhiều role)
        boolean isAdmin = false;
        boolean isStaff = false;
        boolean isCustomer = false;

        Set<Dinh_Role> roles = user.getRoles();
        if (roles != null) {
            for (Dinh_Role r : roles) {
                if (r == null || r.getRoleName() == null) continue;
                String rn = r.getRoleName().trim().toUpperCase();
                if (rn.equals("ADMIN")) isAdmin = true;
                else if (rn.equals("STAFF")) isStaff = true;
                else if (rn.equals("CUSTOMER") || rn.equals("USER") || rn.equals("CLIENT")) isCustomer = true;
            }
        }

        // 3) Ưu tiên: ADMIN > STAFF > CUSTOMER
        if (isAdmin) {
            // Admin thấy hết
            showButton(btnUser);
            showButton(btnAudit);
            showButton(btnCategory);
            showButton(btnProduct);
            showButton(btnCart);
            showButton(btnOrder);
            showButton(btnReport);
            showButton(btnPayment);
            showButton(btnShipment);  // ✅ Chỉ ADMIN thấy Shipment
            return;
        }

        if (isStaff) {
            // Staff chỉ thấy: Quản lý danh mục (Category), Quản lý kho & sản phẩm (Product)
            // ❌ KHÔNG thấy Shipment (chỉ ADMIN)
            showButton(btnCategory);
            showButton(btnProduct);
            return;
        }

        if (isCustomer) {
            // Customer chỉ thấy: Giỏ hàng, Danh sách đơn hàng (chỉ của bản thân), Thanh toán
            showButton(btnCart);
            showButton(btnOrder);
            showButton(btnPayment);
            return;
        }

        // Default: nếu không có role rõ ràng, giữ ẩn hết (an toàn)
    }

    private void hideAllButtons() {
        hideButton(btnUser);
        hideButton(btnAudit);
        hideButton(btnCategory);
        hideButton(btnProduct);
        hideButton(btnCart);
        hideButton(btnOrder);
        hideButton(btnReport);
        hideButton(btnPayment);
        hideButton(btnShipment);
    }

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); 
        }
    }

    private void showButton(Button btn) {
        if (btn != null) {
            btn.setVisible(true);
            btn.setManaged(true);
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
    @FXML public void showShipment() { loadView("ShipmentView.fxml"); }

    @FXML
    public void handleChangePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu cũ và mật khẩu mới");
        
        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Mật khẩu cũ");
        
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Mật khẩu mới");
        
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Xác nhận mật khẩu mới");
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
            new Label("Mật khẩu cũ:"), oldPassField,
            new Label("Mật khẩu mới:"), newPassField,
            new Label("Xác nhận mật khẩu mới:"), confirmPassField
        );
        
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String oldPass = oldPassField.getText();
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                showAlertMsg("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                showAlertMsg("Lỗi", "Mật khẩu xác nhận không khớp!");
                return;
            }
            
            Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();
            com.ucop.dinh_admin.service.UserService userService = new com.ucop.dinh_admin.service.UserService();
            
            if (userService.changePassword(currentUser.getUsername(), oldPass, newPass)) {
                showAlertMsg("Thành công", "Đổi mật khẩu thành công!");
            } else {
                showAlertMsg("Lỗi", "Mật khẩu cũ không chính xác!");
            }
        }
    }

    private void showAlertMsg(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

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