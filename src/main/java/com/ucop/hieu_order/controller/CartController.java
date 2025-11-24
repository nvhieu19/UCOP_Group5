package com.ucop.hieu_order.controller;

import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.Hieu_OrderItem;
import com.ucop.hieu_order.service.OrderService;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

public class CartController {

    @FXML private ComboBox<Hai_Item> cbProducts;
    @FXML private TextField txtQuantity;
    @FXML private TableView<Hieu_OrderItem> tableCart;
    
    // Các Label hiển thị tiền
    @FXML private Label lblSubTotal;
    @FXML private Label lblTax;
    @FXML private Label lblShip;
    @FXML private Label lblDiscount;
    @FXML private Label lblGrandTotal;
    
    @FXML private TextField txtPromoCode;

    private OrderService orderService = new OrderService();
    private Hieu_Order currentOrder;
    private ObservableList<Hieu_OrderItem> cartItems;

    @FXML
    public void initialize() {
        currentOrder = new Hieu_Order();
        cartItems = FXCollections.observableArrayList();
        tableCart.setItems(cartItems);

        ObservableList<Hai_Item> products = FXCollections.observableArrayList(orderService.getAvailableProducts());
        cbProducts.setItems(products);
        cbProducts.setConverter(new StringConverter<Hai_Item>() {
            @Override public String toString(Hai_Item item) { return (item != null) ? item.getName() + " - " + item.getPrice() : ""; }
            @Override public Hai_Item fromString(String string) { return null; }
        });
        
        updateOrderDisplay();
    }

    @FXML
    public void handleAddToCart() {
        Hai_Item selectedItem = cbProducts.getValue();
        if (selectedItem == null) return;
        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            Hieu_OrderItem line = new Hieu_OrderItem(selectedItem, qty);
            
            currentOrder.addOrderItem(line);
            cartItems.add(line);
            
            // Tính toán lại tiền nong
            reCalculate();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng sai!");
        }
    }

    @FXML
    public void handleApplyCode() {
        reCalculate(); // Tính lại kèm mã code
        if (currentOrder.getDiscountAmount().doubleValue() > 0) {
            showAlert("Thành công", "Đã áp dụng mã: " + currentOrder.getPromotionCode());
        } else {
            showAlert("Thông báo", "Mã không hợp lệ hoặc không tìm thấy!");
        }
    }

    private void reCalculate() {
        String code = txtPromoCode.getText().trim();
        // Gọi Service tính toán tất cả (Thuế, Ship, Code)
        orderService.calculateOrderDetails(currentOrder, code);
        updateOrderDisplay();
    }

    private void updateOrderDisplay() {
        lblSubTotal.setText(String.format("%,.0f", currentOrder.getSubTotal()));
        lblTax.setText(String.format("%,.0f", currentOrder.getTaxAmount()));
        lblShip.setText(String.format("%,.0f", currentOrder.getShippingFee()));
        lblDiscount.setText(String.format("%,.0f", currentOrder.getDiscountAmount()));
        lblGrandTotal.setText(String.format("%,.0f VNĐ", currentOrder.getTotalAmount()));
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Lỗi", "Giỏ hàng trống!");
            return;
        }
        try {
            // Lấy user demo (Thực tế phải lấy từ Session đăng nhập)
            Dinh_User customer = new UserService().login("vip_member", "pass123");
            if(customer == null) customer = new UserService().login("admin_dinh", "123456"); // Fallback

            orderService.createOrder(currentOrder, customer);
            
            showAlert("Thành công", "Đơn hàng đã tạo (Trạng thái: PLACED)\nTổng tiền: " + lblGrandTotal.getText());
            
            // Reset
            currentOrder = new Hieu_Order();
            cartItems.clear();
            txtPromoCode.clear();
            updateOrderDisplay();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi lưu đơn hàng: " + e.getMessage());
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