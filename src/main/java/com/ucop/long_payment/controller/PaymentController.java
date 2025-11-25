package com.ucop.long_payment.controller;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.SessionManager;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PaymentController {

    // --- FXML Components ---
    @FXML private Label lblBalance;
    @FXML private TextField txtDeposit;
    @FXML private TableView<Long_Payment> tableHistory;

    // Components mới cho thanh toán đơn hàng
    @FXML private ComboBox<Hieu_Order> cbOrdersToPay;
    @FXML private Label lblOrderTotal;
    @FXML private Button btnPayByWallet;
    @FXML private Button btnPayCOD;

    // --- Services & DAOs ---
    private PaymentService paymentService = new PaymentService();
    private OrderDAO orderDAO = new OrderDAO();
    private Dinh_User currentUser;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        // Lấy người dùng hiện tại từ SessionManager
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.");
            // Vô hiệu hóa toàn bộ giao diện nếu không có user
            lblBalance.getScene().getRoot().setDisable(true);
            return;
        }

        setupOrderComboBox();
        loadWalletData();
        loadUnpaidOrders();
        loadPaymentHistory();
    }

    private void setupOrderComboBox() {
        // Hiển thị thông tin tóm tắt của đơn hàng trong ComboBox
        cbOrdersToPay.setConverter(new StringConverter<>() {
            @Override
            public String toString(Hieu_Order order) {
                if (order == null) return null;
                return String.format("Đơn #%d - Ngày: %s - Tổng: %s",
                        order.getId(), order.getOrderDate(), currencyFormatter.format(order.getTotalAmount()));
            }

            @Override
            public Hieu_Order fromString(String string) {
                return null; // Không cần thiết
            }
        });

        // Cập nhật tổng tiền khi chọn một đơn hàng
        cbOrdersToPay.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblOrderTotal.setText("Tổng tiền: " + currencyFormatter.format(newVal.getTotalAmount()));
            } else {
                lblOrderTotal.setText("Tổng tiền: 0 VNĐ");
            }
        });
    }

    private void loadWalletData() {
        Long_Wallet wallet = paymentService.getMyWallet(currentUser);
        lblBalance.setText(currencyFormatter.format(wallet.getBalance()));
    }

    private void loadUnpaidOrders() {
        List<String> statusesToFind = List.of("PENDING_PAYMENT", "PLACED");
        List<Hieu_Order> unpaidOrders = orderDAO.findOrdersByStatusAndUser(statusesToFind, currentUser.getId());
        cbOrdersToPay.setItems(FXCollections.observableArrayList(unpaidOrders));
    }

    private void loadPaymentHistory() {
        List<Long_Payment> history = paymentService.getMyHistory(currentUser);
        tableHistory.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    private void handleDeposit() {
        try {
            double amount = Double.parseDouble(txtDeposit.getText());
            if (amount <= 0) {
                showError("Lỗi", "Số tiền nạp phải lớn hơn 0.");
                return;
            }
            paymentService.deposit(currentUser, amount);
            txtDeposit.clear();
            loadWalletData(); // Cập nhật lại số dư
            showInfo("Thành công", "Nạp tiền thành công!");
        } catch (NumberFormatException e) {
            showError("Lỗi", "Vui lòng nhập một số hợp lệ.");
        }
    }

    @FXML
    private void handlePayByWallet() {
        Hieu_Order selectedOrder = cbOrdersToPay.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showError("Lỗi", "Vui lòng chọn một đơn hàng để thanh toán.");
            return;
        }

        Long_Wallet wallet = paymentService.getMyWallet(currentUser);
        BigDecimal total = selectedOrder.getTotalAmount();

        if (wallet.getBalance().compareTo(total) < 0) {
            showError("Thanh toán thất bại", "Số dư trong ví không đủ. Vui lòng nạp thêm tiền.");
            return;
        }

        // Trừ tiền và cập nhật
        wallet.deduct(total);
        paymentService.updateWallet(wallet);

        // Cập nhật trạng thái đơn hàng và lưu lịch sử
        selectedOrder.setStatus("PAID");
        orderDAO.update(selectedOrder);
        paymentService.savePayment(new Long_Payment(selectedOrder, "WALLET", total));

        showInfo("Thành công", "Thanh toán đơn hàng #" + selectedOrder.getId() + " thành công!");
        refreshAllData();
    }

    @FXML
    private void handlePayCOD() {
        Hieu_Order selectedOrder = cbOrdersToPay.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showError("Lỗi", "Vui lòng chọn một đơn hàng để thanh toán.");
            return;
        }

        selectedOrder.setStatus("COD"); // Chuyển trạng thái sang chờ giao hàng thu tiền
        orderDAO.update(selectedOrder);

        showInfo("Thành công", "Đơn hàng #" + selectedOrder.getId() + " đã được xác nhận thanh toán khi nhận hàng (COD).");
        refreshAllData();
    }

    private void refreshAllData() {
        loadWalletData();
        loadUnpaidOrders();
        loadPaymentHistory();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}