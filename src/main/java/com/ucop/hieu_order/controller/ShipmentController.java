package com.ucop.hieu_order.controller;

import com.ucop.hieu_order.Hieu_Shipment;
import com.ucop.hieu_order.service.ShipmentService;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.DecimalFormat;
import java.util.List;

public class ShipmentController {

    @FXML private TableView<Hieu_Shipment> tableShipments;
    @FXML private TableColumn<Hieu_Shipment, Long> colId;
    @FXML private TableColumn<Hieu_Shipment, String> colTrackingNumber;
    @FXML private TableColumn<Hieu_Shipment, String> colShippingMethod;
    @FXML private TableColumn<Hieu_Shipment, String> colStatus;
    @FXML private TableColumn<Hieu_Shipment, String> colAddress;
    @FXML private ComboBox<String> cbStatus;

    private ShipmentService shipmentService = new ShipmentService();
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        try {
            // ✅ KIỂM TRA QUYỀN: Chỉ ADMIN mới có thể xem
            Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();
            if (!isAdmin(currentUser)) {
                showAlert("Lỗi quyền truy cập", "Chỉ quản trị viên mới có thể truy cập chức năng này!");
                disableAllControls();
                return;
            }
            
            // Cấu hình các cột
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colTrackingNumber != null) colTrackingNumber.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));
            if (colShippingMethod != null) colShippingMethod.setCellValueFactory(new PropertyValueFactory<>("shippingMethod"));
            
            // Format status với màu sắc
            if (colStatus != null) {
                colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
                colStatus.setCellFactory(column -> new TableCell<Hieu_Shipment, String>() {
                    @Override
                    protected void updateItem(String status, boolean empty) {
                        super.updateItem(status, empty);
                        if (empty || status == null) {
                            setText(null);
                        } else {
                            switch (status.toUpperCase()) {
                                case "PREPARING": setText("📦 Đang chuẩn bị"); setStyle("-fx-text-fill: #f39c12;"); break;
                                case "SHIPPED": setText("🚚 Đã gửi"); setStyle("-fx-text-fill: #3498db;"); break;
                                case "IN_TRANSIT": setText("🚛 Đang vận chuyển"); setStyle("-fx-text-fill: #9b59b6;"); break;
                                case "DELIVERED": setText("✅ Đã giao"); setStyle("-fx-text-fill: #27ae60;"); break;
                                case "FAILED": setText("❌ Giao thất bại"); setStyle("-fx-text-fill: #e74c3c;"); break;
                                case "RETURNED": setText("↩️ Đã hoàn"); setStyle("-fx-text-fill: #95a5a6;"); break;
                                default: setText(status);
                            }
                        }
                    }
                });
            }
            
            if (colAddress != null) colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

            // Cấu hình ComboBox lọc status
            if (cbStatus != null) {
                cbStatus.getItems().addAll("Tất cả", "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "FAILED", "RETURNED");
                cbStatus.getSelectionModel().selectFirst();
            }
            
            // Load dữ liệu
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khởi tạo", "Lỗi khi khởi tạo giao diện: " + e.getMessage());
        }
    }
    
    // ✅ Kiểm tra xem user có phải ADMIN không
    private boolean isAdmin(Dinh_User user) {
        if (user == null) {
            System.out.println("❌ [PERMISSION] User là null");
            return false;
        }
        
        System.out.println("🔍 [PERMISSION] Kiểm tra user: " + user.getUsername());
        
        // Kiểm tra user có role ADMIN không
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            for (com.ucop.dinh_admin.Dinh_Role role : user.getRoles()) {
                System.out.println("  - Role: " + role.getRoleName());
                if (role != null && "ADMIN".equalsIgnoreCase(role.getRoleName())) {
                    System.out.println("✅ [PERMISSION] User là ADMIN - Cho phép truy cập");
                    return true;
                }
            }
            System.out.println("❌ [PERMISSION] User không có role ADMIN");
            return false;
        }
        
        System.out.println("❌ [PERMISSION] User không có role nào");
        return false;
    }
    
    // ✅ Vô hiệu hóa tất cả các control nếu không phải admin
    private void disableAllControls() {
        if (tableShipments != null) tableShipments.setDisable(true);
        if (cbStatus != null) cbStatus.setDisable(true);
    }

    private void loadData() {
        try {
            List<Hieu_Shipment> shipments = shipmentService.getAllShipments();
            if (shipments == null || shipments.isEmpty()) {
                System.out.println("Không có dữ liệu vận chuyển");
                tableShipments.setItems(FXCollections.observableArrayList());
            } else {
                tableShipments.setItems(FXCollections.observableArrayList(shipments));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi tải dữ liệu", "Không thể tải danh sách vận đơn: " + e.getMessage());
            tableShipments.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void handleFilter() {
        try {
            String selectedStatus = cbStatus.getValue();
            List<Hieu_Shipment> shipments;
            
            if (selectedStatus == null || selectedStatus.equals("Tất cả")) {
                shipments = shipmentService.getAllShipments();
            } else {
                shipments = shipmentService.getShipmentsByStatus(selectedStatus);
            }
            
            if (shipments == null) {
                shipments = new java.util.ArrayList<>();
            }
            
            tableShipments.setItems(FXCollections.observableArrayList(shipments));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi lọc dữ liệu", "Không thể lọc vận đơn: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateStatus() {
        try {
            Hieu_Shipment selected = tableShipments.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Lỗi", "Vui lòng chọn vận đơn để cập nhật!");
                return;
            }

            // Tạo dialog chọn trạng thái mới
            ChoiceDialog<String> dialog = new ChoiceDialog<>("SHIPPED", 
                "PREPARING", "SHIPPED", "IN_TRANSIT", "DELIVERED", "FAILED", "RETURNED");
            dialog.setTitle("Cập nhật trạng thái");
            dialog.setHeaderText("Chọn trạng thái mới cho vận đơn: " + selected.getTrackingNumber());
            dialog.setContentText("Trạng thái:");

            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                shipmentService.updateShipmentStatus(selected.getId(), result.get());
                loadData();
                showAlert("Thành công", "Đã cập nhật trạng thái vận đơn thành: " + result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
