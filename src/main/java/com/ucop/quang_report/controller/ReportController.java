package com.ucop.quang_report.controller;

import com.ucop.quang_report.Quang_Promotion;
import com.ucop.quang_report.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class ReportController {

    @FXML private TableView<Quang_Promotion> tablePromo;
    @FXML private TextField txtCode;
    @FXML private TextField txtValue;

    @FXML private PieChart pieChartProducts;
    @FXML private BarChart<String, Number> barChartRevenue;

    private ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadPromoData();
        loadChartData();
    }

    // --- PHẦN 1: TAB KHUYẾN MÃI ---
    private void loadPromoData() {
        List<Quang_Promotion> list = reportService.getAllPromotions();
        tablePromo.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleAddPromo() {
        try {
            String code = txtCode.getText();
            double val = Double.parseDouble(txtValue.getText());
            
            // Tạo mã giảm giá (Mặc định loại FIXED, hạn 30 ngày)
            Quang_Promotion p = new Quang_Promotion(code, val, "FIXED", 30);
            reportService.addPromotion(p);
            
            loadPromoData(); // Refresh bảng
            txtCode.clear(); txtValue.clear();
            
        } catch (Exception e) {
            showAlert("Lỗi", "Nhập liệu sai: " + e.getMessage());
        }
    }

    // --- PHẦN 2: TAB BIỂU ĐỒ ---
    @FXML
    public void loadChartData() {
        // 1. Vẽ PieChart (Top sản phẩm)
        pieChartProducts.getData().clear();
        List<Object[]> topProducts = reportService.getTopSellingProducts();
        for (Object[] row : topProducts) {
            String productName = (String) row[0];
            Long quantity = (Long) row[1];
            // Thêm miếng bánh vào biểu đồ
            pieChartProducts.getData().add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
        }

        // 2. Vẽ BarChart (Tổng doanh thu)
        barChartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu thực tế");
        
        Double total = reportService.getTotalRevenue();
        series.getData().add(new XYChart.Data<>("Tổng Thu (PAID)", total));
        
        // FIX: Thêm thống kê tỷ lệ hoàn/hủy
        java.util.Map<String, Long> stats = reportService.getOrderCancelRefundStats();
        long totalOrders = stats.getOrDefault("TOTAL", 0L);
        long canceledOrders = stats.getOrDefault("CANCELED", 0L);
        long refundedOrders = stats.getOrDefault("REFUNDED", 0L);
        long successfulOrders = stats.getOrDefault("SUCCESSFUL", 0L);
        
        // Tính tỷ lệ %
        double cancelRate = totalOrders > 0 ? (canceledOrders * 100.0 / totalOrders) : 0;
        double refundRate = totalOrders > 0 ? (refundedOrders * 100.0 / totalOrders) : 0;
        double successRate = totalOrders > 0 ? (successfulOrders * 100.0 / totalOrders) : 0;
        
        series.getData().add(new XYChart.Data<>("Đơn thành công (%)", successRate));
        series.getData().add(new XYChart.Data<>("Đơn hủy (%)", cancelRate));
        series.getData().add(new XYChart.Data<>("Đơn hoàn tiền (%)", refundRate));
        
        barChartRevenue.getData().add(series);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}