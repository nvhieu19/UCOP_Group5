package com.ucop.hieu_order.controller;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.Dinh_Role;
import com.ucop.dinh_admin.service.SessionManager;
import com.ucop.long_payment.service.PaymentService;
import com.ucop.long_payment.Long_Wallet;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

        // 2. Cột Ngày (Format ngày giờ chuẩn: dd/MM/yyyy HH:mm:ss)
        colDate = (TableColumn<Hieu_Order, String>) tableOrders.getColumns().get(1);
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime orderDate = cellData.getValue().getOrderDate();
            if (orderDate != null) {
                String formatted = orderDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                return new SimpleStringProperty(formatted);
            }
            return new SimpleStringProperty("");
        });

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

        // 5. Cột Trạng thái (FIX: Format status đẹp hơn)
        colStatus = (TableColumn<Hieu_Order, String>) tableOrders.getColumns().get(4);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Hieu_Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    // Format status: PLACED → Đã đặt, PAID → Đã thanh toán, v.v.
                    String displayStatus = formatStatus(status);
                    setText(displayStatus);
                    
                    // Tô màu theo trạng thái
                    switch (status.toUpperCase()) {
                        case "PLACED": setStyle("-fx-text-fill: #3498db;"); break; // Xanh - Đang chờ
                        case "PAID": setStyle("-fx-text-fill: #27ae60;"); break;   // Xanh lá - Đã thanh toán
                        case "SHIPPED": setStyle("-fx-text-fill: #f39c12;"); break; // Cam - Đang giao
                        case "CANCELED": setStyle("-fx-text-fill: #e74c3c;"); break; // Đỏ - Đã hủy
                        default: setStyle("");
                    }
                }
            }
        });

        // --- LOAD DỮ LIỆU ---
        loadData();
        
        cbStatus.getItems().addAll("Tất cả", "PLACED", "PAID", "SHIPPED", "CANCELED");
        cbStatus.getSelectionModel().selectFirst();
    }

    private boolean isAdminOrStaff(Dinh_User user) {
        if (user == null) return false;
        Set<Dinh_Role> roles = user.getRoles();
        if (roles == null) return false;
        for (Dinh_Role r : roles) {
            if (r == null || r.getRoleName() == null) continue;
            String rn = r.getRoleName().trim().toUpperCase();
            if (rn.equals("ADMIN") || rn.equals("STAFF")) return true;
        }
        return false;
    }

    private void loadData() {
        Dinh_User current = SessionManager.getInstance().getCurrentUser();
        if (isAdminOrStaff(current)) {
            // Admin/Staff thấy tất cả đơn
            allOrders = orderDAO.findAll();
        } else if (current != null) {
            // Customer chỉ thấy đơn của chính họ
            // Sử dụng DAO: findOrdersByStatusAndUser - truyền tất cả status để lấy mọi đơn
            List<String> statuses = new ArrayList<>();
            statuses.add("PLACED");
            statuses.add("PAID");
            statuses.add("SHIPPED");
            statuses.add("CANCELED");
            allOrders = orderDAO.findOrdersByStatusAndUser(statuses, current.getId());
        } else {
            allOrders = new ArrayList<>();
        }
        tableOrders.setItems(FXCollections.observableArrayList(allOrders));
    }

    @FXML
    public void handleFilter() {
        String status = cbStatus.getValue();
        if (status == null || status.equals("Tất cả")) {
            tableOrders.setItems(FXCollections.observableArrayList(allOrders));
        } else {
            List<Hieu_Order> filtered = allOrders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            tableOrders.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    // FIX: Thêm hàm format status để hiển thị tiếng Việt
    private String formatStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status.toUpperCase()) {
            case "PLACED": return "📋 Đã đặt";
            case "PAID": return "✅ Đã thanh toán";
            case "SHIPPED": return "🚚 Đang giao";
            case "DELIVERED": return "📦 Đã giao";
            case "CANCELED": return "❌ Đã hủy";
            case "REFUNDED": return "↩️ Đã hoàn tiền";
            case "COD_PENDING": return "💵 Chờ COD";
            default: return status;
        }
    }

    @FXML
    public void handlePayOrder() {
        Hieu_Order selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn đơn hàng cần thanh toán!");
            return;
        }

        // Kiểm tra quyền: nếu user không phải admin/staff thì chỉ được thanh toán đơn của chính họ
        Dinh_User current = SessionManager.getInstance().getCurrentUser();
        if (!isAdminOrStaff(current)) {
            if (current == null || selected.getCustomer() == null || !current.getId().equals(selected.getCustomer().getId())) {
                showAlert("Lỗi", "Bạn chỉ được thanh toán các đơn của chính bạn.");
                return;
            }
        }

        if ("PAID".equals(selected.getStatus())) {
            showAlert("Thông báo", "Đơn hàng này đã thanh toán rồi!");
            return;
        }

        // LOGIC THANH TOÁN (Gọi API xịn của Module SV4 - Long)
        try {
            String username = selected.getCustomer().getUsername();
            
            // Gọi hàm payOrder (Hàm này sẽ tự động: Trừ ví, Lưu lịch sử, Đổi trạng thái đơn)
            // Tham số: (Username, OrderID, VoucherCode)
            paymentService.payOrder(username, selected.getId(), ""); 
            
            // Nếu chạy đến đây là thành công
            loadData(); // Refresh bảng
            
            // Lấy số dư mới để hiện thông báo (Optional)
            Long_Wallet wallet = paymentService.getMyWallet(selected.getCustomer());
            showAlert("Thành công", "Thanh toán hoàn tất!\nSố dư còn lại: " + df.format(wallet.getBalance()) + " VNĐ");
            
        } catch (Exception e) {
            // Nếu ví thiếu tiền hoặc lỗi gì đó, Service sẽ ném thông báo ra đây
            e.printStackTrace(); // In lỗi ra console để debug nếu cần
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
}