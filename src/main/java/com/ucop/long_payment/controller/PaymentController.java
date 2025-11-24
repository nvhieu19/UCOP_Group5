package com.ucop.long_payment.controller;

import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.text.DecimalFormat;

public class PaymentController {

    @FXML private Label lblBalance;
    @FXML private TextField txtAmount;
    @FXML private TableView<Long_Payment> tableHistory;

    private PaymentService service = new PaymentService();
    private String currentUsername; 

    // Hàm nhận username từ Dashboard chuyển sang
    public void setUsername(String username) {
        this.currentUsername = username;
        loadData();
    }

    private void loadData() {
        if (currentUsername == null) return;

        // 1. Hiển thị số dư
        Long_Wallet wallet = service.getWallet(currentUsername);
        if (wallet != null) {
            DecimalFormat df = new DecimalFormat("#,###");
            lblBalance.setText(df.format(wallet.getBalance()) + " VNĐ");
        }

        // 2. Hiển thị lịch sử
        tableHistory.setItems(FXCollections.observableArrayList(service.getHistory(currentUsername)));
    }

    @FXML
    public void handleDeposit() {
        try {
            double amount = Double.parseDouble(txtAmount.getText());
            if (amount <= 0) throw new NumberFormatException();

            service.deposit(currentUsername, amount);
            
            showAlert("Thành công", "Đã nạp tiền vào ví!");
            txtAmount.clear();
            loadData(); // Cập nhật lại số dư mới

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền phải là số hợp lệ!");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}