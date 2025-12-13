package com.ucop.quang_report.controller;

import com.ucop.quang_report.Quang_Promotion;
import com.ucop.quang_report.service.ReportService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; // [Mới] Import thêm Button
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class ReportController {

    @FXML private TableView<Quang_Promotion> tablePromo;
    @FXML private TextField txtCode;
    @FXML private TextField txtValue;
    
    // [Mới] Các nút bấm cần khai báo để điều khiển (cần khớp fx:id bên FXML)
    @FXML private Button btnSavePromo;
    @FXML private Button btnDeletePromo;

    @FXML private PieChart pieChartProducts;
    @FXML private BarChart<String, Number> barChartRevenue;

    private ReportService reportService = new ReportService();
    
    // [Mới] Biến để lưu khuyến mãi đang chọn (nếu có)
    private Quang_Promotion selectedPromo = null;

    @FXML
    public void initialize() {
        setupPromoListeners(); // [Mới] Cài đặt sự kiện chọn dòng
        loadPromoData();
        loadChartData();
    }
    
    // [Mới] Hàm xử lý khi người dùng chọn 1 dòng trong bảng
    private void setupPromoListeners() {
        tablePromo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPromo = newVal;
                // Đổ dữ liệu lên ô nhập
                txtCode.setText(newVal.getCode());
                txtValue.setText(String.valueOf(newVal.getDiscountValue()));
                
                // Đổi trạng thái nút bấm
                if (btnSavePromo != null) btnSavePromo.setText("Cập Nhật"); // Đổi nút Thêm thành Sửa
                if (btnDeletePromo != null) btnDeletePromo.setDisable(false); // Cho phép xóa
            } else {
                clearPromoFields();
            }
        });
    }

    // --- PHẦN 1: TAB KHUYẾN MÃI ---
    private void loadPromoData() {
        List<Quang_Promotion> list = reportService.getAllPromotions();
        tablePromo.setItems(FXCollections.observableArrayList(list));
    }

    // [Cập nhật] Hàm này giờ xử lý cả THÊM và SỬA (thay cho handleAddPromo cũ)
    @FXML
    public void handleSavePromo() {
        if (txtCode.getText().isEmpty() || txtValue.getText().isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đủ Mã và Giá trị!");
            return;
        }

        try {
            String code = txtCode.getText();
            double val = Double.parseDouble(txtValue.getText());
            
            if (selectedPromo == null) {
                // --- TRƯỜNG HỢP THÊM MỚI ---
                Quang_Promotion p = new Quang_Promotion(code, val, "FIXED", 30);
                reportService.addPromotion(p);
                showAlert("Thành công", "Đã tạo mã mới: " + code);
            } else {
                // --- TRƯỜNG HỢP CẬP NHẬT (SỬA) ---
                selectedPromo.setCode(code);
                selectedPromo.setDiscountValue(val);
                reportService.updatePromotion(selectedPromo); // Gọi hàm Update trong Service
                showAlert("Thành công", "Đã cập nhật mã: " + code);
            }
            
            loadPromoData(); // Refresh lại bảng
            clearPromoFields(); // Xóa trắng form
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Giá trị phải là số hợp lệ!");
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể lưu: " + e.getMessage());
        }
    }
    
    // [Mới] Hàm xử lý Xóa
    @FXML
    public void handleDeletePromo() {
        if (selectedPromo == null) {
            showAlert("Lỗi", "Vui lòng chọn dòng cần xóa!");
            return;
        }
        try {
            reportService.deletePromotion(selectedPromo.getId()); // Gọi hàm Delete trong Service
            showAlert("Thành công", "Đã xóa mã: " + selectedPromo.getCode());
            loadPromoData();
            clearPromoFields();
        } catch (Exception e) {
            showAlert("Lỗi", "Lỗi khi xóa: " + e.getMessage());
        }
    }

    // [Mới] Nút "Xóa Form" để reset trạng thái về thêm mới
    @FXML
    public void handleClearPromo() {
        clearPromoFields();
    }

    private void clearPromoFields() {
        selectedPromo = null;
        txtCode.clear();
        txtValue.clear();
        tablePromo.getSelectionModel().clearSelection();
        
        if (btnSavePromo != null) btnSavePromo.setText("Tạo Mã Mới");
        if (btnDeletePromo != null) btnDeletePromo.setDisable(true);
    }

    // --- PHẦN 2: TAB BIỂU ĐỒ (Giữ nguyên) ---
    @FXML
    public void loadChartData() {
        // 1. Vẽ PieChart (Top sản phẩm)
        pieChartProducts.getData().clear();
        List<Object[]> topProducts = reportService.getTopSellingProducts();
        for (Object[] row : topProducts) {
            String productName = (String) row[0];
            Long quantity = (Long) row[1];
            pieChartProducts.getData().add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
        }

        // 2. Vẽ BarChart (Tổng doanh thu)
        barChartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu thực tế");
        
        Double total = reportService.getTotalRevenue();
        series.getData().add(new XYChart.Data<>("Tổng Thu (PAID)", total));
        
        barChartRevenue.getData().add(series);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}