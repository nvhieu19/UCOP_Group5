package com.ucop.long_payment.controller;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.text.DecimalFormat;

public class PaymentController {

    @FXML private Label lblBalance;
    @FXML private TextField txtDeposit;
    @FXML private TableView<Long_Payment> tableHistory;
    
    // Phần nâng cao
    @FXML private TextField txtOrderId;
    @FXML private TextField txtVoucher;
    @FXML private Label lblDetail;
    @FXML private Label lblFinalTotal;
    @FXML private ImageView imgQR;

    private PaymentService service = new PaymentService();
    private String currentUsername; 
    private DecimalFormat df = new DecimalFormat("#,###");

    public void setUsername(String username) {
        this.currentUsername = username;
        loadData();
    }

    private void loadData() {
        if (currentUsername == null) return;
        Long_Wallet wallet = service.getWallet(currentUsername);
        if (wallet != null) {
            lblBalance.setText(df.format(wallet.getBalance()) + " VNĐ");
        }
        tableHistory.setItems(FXCollections.observableArrayList(service.getHistory(currentUsername)));
    }

    @FXML
    public void handleDeposit() {
        try {
            double amount = Double.parseDouble(txtDeposit.getText());
            service.deposit(currentUsername, amount);
            txtDeposit.clear();
            loadData();
            showAlert("Thành công", "Nạp tiền thành công!");
        } catch (Exception e) {
            showAlert("Lỗi", "Số tiền không hợp lệ!");
        }
    }

    // --- XỬ LÝ THANH TOÁN ĐƠN HÀNG & QR ---
    @FXML
    public void handleCheckOrder() {
        try {
            Long orderId = Long.parseLong(txtOrderId.getText());
            String voucher = txtVoucher.getText();

            // 1. Tìm đơn hàng
            Hieu_Order order = service.findPendingOrder(currentUsername, orderId);
            if (order == null) {
                showAlert("Lỗi", "Không tìm thấy đơn hàng ID " + orderId + " (hoặc đã trả rồi)!");
                return;
            }

            // 2. Tính toán chi tiết (Đã có logic Smart Ship bên Service)
            double subTotal = order.getTotalAmount().doubleValue();
            double total = service.calculateFinalAmount(order, voucher);
            
            // Tính ngược lại các khoản phụ phí để hiển thị
            double tax = subTotal * 0.1;
            double ship = (subTotal >= 1000000) ? 0 : 30000; // Hiển thị đúng logic Free Ship
            double discount = (subTotal + tax + ship) - total;

            // 3. Hiển thị thông tin
            String detail = String.format("Tiền hàng: %s\nThuế (10%%): %s\nShip: %s\nGiảm giá: -%s", 
                    df.format(subTotal), df.format(tax), df.format(ship), df.format(discount));
            lblDetail.setText(detail);
            lblFinalTotal.setText(df.format(total) + " VNĐ");

            // 4. [MỚI] TẠO QR CODE CHUẨN NGÂN HÀNG (VietQR)
            String bankId = "MB";       // Mã ngân hàng (VD: MB, VCB)
            String accountNo = "0000123456789"; // Số tài khoản Admin
            String template = "compact";
            String addInfo = "Thanh toan don " + orderId; 
            
            // API VietQR xịn
            String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s",
                    bankId, accountNo, template, (long)total, addInfo.replace(" ", "%20"));
            
            imgQR.setImage(new Image(qrUrl, true));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Kiểm tra lại Mã đơn hàng (Phải là số)!");
        }
    }

    @FXML
    public void handlePay() {
        try {
            Long orderId = Long.parseLong(txtOrderId.getText());
            String voucher = txtVoucher.getText();
            
            service.payOrder(currentUsername, orderId, voucher);
            
            showAlert("Thành công", "Thanh toán đơn hàng " + orderId + " hoàn tất!");
            
            loadData(); 
            txtOrderId.clear(); txtVoucher.clear();
            lblDetail.setText("..."); lblFinalTotal.setText("0 VNĐ"); imgQR.setImage(null);
            
        } catch (Exception e) {
            showAlert("Thất bại", e.getMessage());
        }
    }
    
    // Hàm xử lý nút Hoàn tiền
    @FXML
    public void handleRefund() {
        try {
            String orderIdText = txtOrderId.getText();
            if (orderIdText == null || orderIdText.trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập Mã đơn hàng để hoàn tiền!");
                return;
            }
            Long orderId = Long.parseLong(orderIdText);
            
            service.refundOrder(currentUsername, orderId);
            
            showAlert("Thành công", "Đã hoàn tiền 100% cho đơn hàng: " + orderId);
            loadData(); 
            
        } catch (NumberFormatException e) {
             showAlert("Lỗi", "Mã đơn hàng phải là số!");
        } catch (Exception e) {
            showAlert("Thất bại", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}