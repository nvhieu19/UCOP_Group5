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

import java.math.BigDecimal;

public class CartController {

    @FXML private ComboBox<Hai_Item> cbProducts;
    @FXML private TextField txtQuantity;
    @FXML private TableView<Hieu_OrderItem> tableCart;
    @FXML private Label lblTotal;

    private OrderService orderService = new OrderService();
    private Hieu_Order currentOrder; // Đơn hàng đang tạo
    private ObservableList<Hieu_OrderItem> cartItems;

    @FXML
    public void initialize() {
        // 1. Khởi tạo đơn hàng rỗng
        currentOrder = new Hieu_Order();
        cartItems = FXCollections.observableArrayList();
        tableCart.setItems(cartItems);

        // 2. Load danh sách sản phẩm vào ComboBox
        ObservableList<Hai_Item> products = FXCollections.observableArrayList(orderService.getAvailableProducts());
        cbProducts.setItems(products);

        // 3. (Optional) Làm đẹp hiển thị ComboBox (Chỉ hiện tên SP)
        cbProducts.setConverter(new StringConverter<Hai_Item>() {
            @Override
            public String toString(Hai_Item item) {
                return (item != null) ? item.getName() + " (" + item.getPrice() + ")" : "";
            }
            @Override
            public Hai_Item fromString(String string) { return null; }
        });
    }

    @FXML
    public void handleAddToCart() {
        Hai_Item selectedItem = cbProducts.getValue();
        if (selectedItem == null) {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm!");
            return;
        }

        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            if (qty <= 0) throw new NumberFormatException();

            // Tạo OrderItem mới
            Hieu_OrderItem line = new Hieu_OrderItem(selectedItem, qty);
            
            // Thêm vào Object Order (Backend)
            currentOrder.addOrderItem(line);
            
            // Thêm vào bảng hiển thị (Frontend)
            cartItems.add(line);
            
            // Tính lại tổng tiền
            currentOrder.calculateTotal();
            lblTotal.setText(currentOrder.getTotalAmount() + " VNĐ");

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số lượng phải là số nguyên dương!");
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Lỗi", "Giỏ hàng đang trống!");
            return;
        }

        try {
            // Lấy User mặc định để gán cho đơn hàng (Vì ta chưa truyền User từ Login sang đây kỹ)
            // Tạm thời lấy User đầu tiên trong DB làm khách hàng cho nhanh
            Dinh_User defaultCustomer = new UserService().login("vip_member", "pass123"); 
            // Lưu ý: Nếu user này ko tồn tại thì sẽ lỗi, bạn hãy chắc chắn trong DB có user này hoặc sửa lại code hard-code username khác
            
            if(defaultCustomer == null) {
                 // Fallback: Nếu null thì tạo đại 1 cái giả để không lỗi (Demo thôi)
                 defaultCustomer = new Dinh_User("guest", "123", "ACTIVE");
            }

            orderService.createOrder(currentOrder, defaultCustomer);
            
            showAlert("Thành công", "Đã tạo đơn hàng thành công! Mã đơn: " + currentOrder.getId());
            
            // Reset lại form
            currentOrder = new Hieu_Order();
            cartItems.clear();
            lblTotal.setText("0 VNĐ");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể lưu đơn hàng: " + e.getMessage());
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