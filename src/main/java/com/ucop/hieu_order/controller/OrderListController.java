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
            // Lấy tên người mua
            String username = selected.getCustomer().getUsername();
            
            // Gọi hàm "payOrder" xịn bên Service của Long
            // Hàm này sẽ tự: Tính thuế/ship, Trừ ví, Lưu lịch sử, Cập nhật đơn hàng...
            paymentService.payOrder(username, selected.getId(), ""); 
            
            // Nếu chạy qua được dòng trên tức là thành công
            loadData(); // Load lại bảng để thấy trạng thái đổi thành PAID
            showAlert("Thành công", "Thanh toán hoàn tất đơn hàng: " + selected.getId());
            
        } catch (Exception e) {
            // Nếu lỗi (ví dụ thiếu tiền), nó sẽ hiện thông báo ra đây
            showAlert("Thất bại", e.getMessage());
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