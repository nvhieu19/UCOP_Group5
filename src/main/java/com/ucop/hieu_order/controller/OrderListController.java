package com.ucop.hieu_order.controller;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.long_payment.service.PaymentService;
import com.ucop.long_payment.Long_Wallet;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class OrderListController {

    @FXML private TableView<Hieu_Order> tableOrders;
    @FXML private TableColumn<Hieu_Order, Long> colId;
    @FXML private TableColumn<Hieu_Order, String> colDate;
    @FXML private TableColumn<Hieu_Order, String> colCustomer; // Sửa thành String để hiện tên
    @FXML private TableColumn<Hieu_Order, BigDecimal> colTotal;
    @FXML private TableColumn<Hieu_Order, String> colStatus;
    
    @FXML private ComboBox<String> cbStatus;

    private OrderDAO orderDAO = new OrderDAO();
    private PaymentService paymentService = new PaymentService();
    private List<Hieu_Order> allOrders;
    
    // Định dạng số tiền cho đẹp (VD: 48,000,000)
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // --- CẤU HÌNH CỘT BẢNG (QUAN TRỌNG ĐỂ HIỂN THỊ ĐẸP) ---
        
        // 1. Cột ID
        colId = (TableColumn<Hieu_Order, Long>) tableOrders.getColumns().get(0);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // 2. Cột Ngày (Giữ nguyên hoặc format lại nếu muốn)
        colDate = (TableColumn<Hieu_Order, String>) tableOrders.getColumns().get(1);
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        // 3. Cột Khách hàng (FIX LỖI HIỆN TÊN CLASS)
        colCustomer = (TableColumn<Hieu_Order, String>) tableOrders.getColumns().get(2);
        colCustomer.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getUsername())
        );

        // 4. Cột Tổng tiền (FIX LỖI HIỆN SỐ E7)
        colTotal = (TableColumn<Hieu_Order, BigDecimal>) tableOrders.getColumns().get(3);
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        // Custom hiển thị cell để thêm chữ VNĐ và dấu phẩy
        colTotal.setCellFactory(tc -> new TableCell<Hieu_Order, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(df.format(price) + " VNĐ");
                }
            }
        });

        // 5. Cột Trạng thái
        colStatus = (TableColumn<Hieu_Order, String>) tableOrders.getColumns().get(4);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // --- LOAD DỮ LIỆU ---
        loadData();
        
        cbStatus.getItems().addAll("Tất cả", "PLACED", "PAID", "SHIPPED", "CANCELED");
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

        try {
            Dinh_User customer = selected.getCustomer();
            Long_Wallet wallet = paymentService.getMyWallet(customer);
            
            BigDecimal totalAmount = selected.getTotalAmount();
            BigDecimal currentBalance = wallet.getBalance();

            // Debug in ra console để kiểm tra
            System.out.println("Ví hiện có: " + currentBalance);
            System.out.println("Cần trả: " + totalAmount);

            // SO SÁNH SỐ DƯ
            if (currentBalance.compareTo(totalAmount) >= 0) {
                // 1. Trừ tiền
                wallet.deduct(totalAmount);
                paymentService.updateWallet(wallet); // Nhớ đảm bảo PaymentService có hàm này
                
                // 2. Cập nhật trạng thái đơn
                selected.setStatus("PAID");
                orderDAO.update(selected);
                
                loadData(); 
                showAlert("Thành công", "Thanh toán thành công!\nSố dư còn lại: " + df.format(wallet.getBalance()) + " VNĐ");
            } else {
                // Thông báo lỗi rõ ràng hơn
                showAlert("Thất bại", 
                    "Số dư ví không đủ!\n" +
                    "Ví hiện có: " + df.format(currentBalance) + " VNĐ\n" +
                    "Cần trả (Gồm VAT): " + df.format(totalAmount) + " VNĐ\n" +
                    "Thiếu: " + df.format(totalAmount.subtract(currentBalance)) + " VNĐ");
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
}