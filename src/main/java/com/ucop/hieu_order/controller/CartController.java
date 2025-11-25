package com.ucop.hieu_order.controller;

import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.Hieu_OrderItem;
import com.ucop.hieu_order.service.OrderService;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.SessionManager;
import com.ucop.dinh_admin.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.text.DecimalFormat;

public class CartController {

    @FXML private ComboBox<Hai_Item> cbProducts;
    @FXML private TextField txtQuantity;
    @FXML private TableView<Hieu_OrderItem> tableCart;
    @FXML private TextField txtPromoCode;
    
    // Các label hiển thị tiền
    @FXML private Label lblSubTotal, lblTax, lblShip, lblDiscount, lblGrandTotal;

    private OrderService orderService = new OrderService();
    private Hieu_Order currentOrder;
    private ObservableList<Hieu_OrderItem> cartItems;
    private DecimalFormat df = new DecimalFormat("#,###"); // Format số tiền cho đẹp

    @FXML
    public void initialize() {
        currentOrder = new Hieu_Order();
        cartItems = FXCollections.observableArrayList();
        tableCart.setItems(cartItems);

        // Load sản phẩm vào ComboBox
        ObservableList<Hai_Item> products = FXCollections.observableArrayList(orderService.getAvailableProducts());
        cbProducts.setItems(products);
        
        // Custom hiển thị ComboBox: Hiện Tên + Giá
        cbProducts.setConverter(new StringConverter<Hai_Item>() {
            @Override public String toString(Hai_Item item) { 
                return (item == null) ? "" : item.getName() + " (" + df.format(item.getPrice()) + " đ)"; 
            }
            @Override public Hai_Item fromString(String string) { return null; }
        });
        
        updateOrderDisplay(); // Hiển thị số 0 ban đầu
    }

    @FXML
    public void handleAddToCart() {
        Hai_Item selected = cbProducts.getValue();
        if (selected == null) return;

        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            if (qty <= 0) throw new NumberFormatException();

            Hieu_OrderItem item = new Hieu_OrderItem(selected, qty);
            currentOrder.addOrderItem(item); // Thêm vào logic
            cartItems.add(item); // Thêm vào giao diện
            
            // FIX: Không clear promotion code - giữ lại để tính lại chính xác
            // Chỉ reset input fields
            
            reCalculate(); // Tính lại tiền ngay lập tức (bao gồm cả phí ship)
            
            // Clear input sau khi thêm thành công
            txtQuantity.clear();
            cbProducts.setValue(null);
            cbProducts.getSelectionModel().clearSelection();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng không hợp lệ!");
        }
    }

    @FXML
    public void handleApplyCode() {
        reCalculate(); // Tính lại tiền với mã code trong ô input
        if (currentOrder.getDiscountAmount().doubleValue() > 0) {
            showAlert("Thành công", "Đã áp dụng mã giảm giá: " + currentOrder.getPromotionCode());
        } else {
            showAlert("Thông báo", "Mã giảm giá không hợp lệ hoặc hết hạn.");
        }
    }

    // Hàm trung tâm để tính toán và hiển thị lại số liệu
    private void reCalculate() {
        // FIX: Đảm bảo tất cả item trong cartItems có giá được set đúng
        for (Hieu_OrderItem cartItem : cartItems) {
            if (cartItem.getPrice() == null || cartItem.getPrice().doubleValue() == 0) {
                // Nếu item chưa có giá, lấy từ sản phẩm gốc
                if (cartItem.getItem() != null && cartItem.getItem().getPrice() != null) {
                    cartItem.setPrice(cartItem.getItem().getPrice());
                }
            }
        }
        
        // Sync cartItems vào currentOrder.orderItems
        currentOrder.setOrderItems(new java.util.ArrayList<>(cartItems));
        
        String code = txtPromoCode.getText();
        orderService.calculateOrderDetails(currentOrder, code);
        updateOrderDisplay();
    }

    private void updateOrderDisplay() {
        lblSubTotal.setText(df.format(currentOrder.getSubTotal()));
        lblTax.setText(df.format(currentOrder.getTaxAmount()));
        lblShip.setText(df.format(currentOrder.getShippingFee()));
        lblDiscount.setText(df.format(currentOrder.getDiscountAmount()));
        lblGrandTotal.setText(df.format(currentOrder.getTotalAmount()) + " VNĐ");
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Lỗi", "Giỏ hàng trống!");
            return;
        }

        // Lấy người dùng hiện tại từ session
        Dinh_User currentUser = SessionManager.getInstance().getCurrentUser();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (currentUser == null) {
            showAlert("Lỗi", "Bạn cần đăng nhập để tạo đơn hàng!");
            return;
        }

        try {
            // Tạo đơn hàng cho người dùng hiện tại
            orderService.createOrder(currentOrder, currentUser);
            
            showAlert("Thành công", "Đã tạo đơn hàng (Trạng thái: PLACED)!\nTổng tiền: " + lblGrandTotal.getText());
            
            // Reset form sau khi thanh toán thành công
            currentOrder = new Hieu_Order();
            cartItems.clear();
            txtPromoCode.clear();
            updateOrderDisplay();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể lưu đơn hàng: " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    @FXML
    public void handleRemoveItem() {
        Hieu_OrderItem selected = tableCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 1. Xóa khỏi danh sách hiển thị
            cartItems.remove(selected);
            
            // 2. Xóa khỏi logic tính toán
            currentOrder.getOrderItems().remove(selected);
            
            // 3. Tính lại tiền
            reCalculate();
        } else {
            showAlert("Chú ý", "Vui lòng chọn món cần xóa trong bảng!");
        }
    }
}
