package com.ucop.hieu_order.controller;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.dinh_admin.Dinh_User; // Import User
import com.ucop.long_payment.service.PaymentService;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.Long_Payment;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.stream.Collectors;

public class OrderListController {

    @FXML private TableView<Hieu_Order> tableOrders;
    @FXML private ComboBox<String> cbStatus;

    private OrderDAO orderDAO = new OrderDAO();
    private PaymentService paymentService = new PaymentService(); // Gọi dịch vụ của Long
    private List<Hieu_Order> allOrders;

    @FXML
    public void initialize() {
        loadData();
        
        // Setup ComboBox
        cbStatus.getItems().addAll("Tất cả", "PLACED", "PAID", "CANCELED");
        cbStatus.getSelectionModel().selectFirst();
    }

    private void loadData() {
        allOrders = orderDAO.findAll();
        tableOrders.setItems(FXCollections.observableArrayList(allOrders));
    }

    @FXML
    public void handleFilter() {
        String status = cbStatus.getValue();
        if (status == null || status.equals("Tất cả")) {
            tableOrders.setItems(FXCollections.observableArrayList(allOrders));
        } else {
            List<Hieu_Order> filtered = allOrders.stream()
                    .filter(o -> o.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            tableOrders.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    public void handlePayOrder() {
        Hieu_Order selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn đơn hàng cần thanh toán!");
            return;
        }

        if ("PAID".equals(selected.getStatus())) {
            showAlert("Thông báo", "Đơn hàng này đã thanh toán rồi!");
            return;
        }

        // LOGIC THANH TOÁN (Gọi Module SV4 - Long)
        try {
            Dinh_User customer = selected.getCustomer();
            Long_Wallet wallet = paymentService.getMyWallet(customer);
            
            // Check tiền trong ví
            if (wallet.getBalance().compareTo(selected.getTotalAmount()) >= 0) {
                // 1. Trừ tiền ví
                wallet.deduct(selected.getTotalAmount());
                // Cập nhật ví vào DB (Cần thêm hàm update trong WalletDAO hoặc dùng session thủ công, ở đây giả định service làm giúp)
                // paymentService.updateWallet(wallet); // (Bạn cần bổ sung hàm này bên PaymentService nếu chưa có)
                
                // 2. Lưu lịch sử thanh toán
                // Long_Payment pay = new Long_Payment(selected, "WALLET", selected.getTotalAmount());
                // paymentService.savePayment(pay); // (Bổ sung bên PaymentService)

                // 3. Cập nhật trạng thái đơn hàng
                selected.setStatus("PAID");
                orderDAO.update(selected);
                
                loadData(); // Refresh bảng
                showAlert("Thành công", "Thanh toán thành công! Số dư mới: " + wallet.getBalance());
            } else {
                showAlert("Thất bại", "Ví không đủ tiền! Số dư: " + wallet.getBalance() + "\nCần: " + selected.getTotalAmount());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}x