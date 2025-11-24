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

         // Tính toán số tiền cuối cùng (Kèm Thuế, Ship logic xịn, Voucher)
            public double calculateFinalAmount(Hieu_Order order, String voucherCode) throws Exception {
                double subTotal = order.getTotalAmount().doubleValue();
                double tax = subTotal * 0.1; // Thuế 10% VAT
                
                // --- LOGIC SHIP NÂNG CAO ---
                double ship = 30000; // Mặc định 30k
                if (subTotal >= 1000000) { 
                    ship = 0; // Đơn to thì Free Ship
                }
                // ---------------------------

                double discount = 0;

                // Check Voucher của Quang
                if (voucherCode != null && !voucherCode.isEmpty()) {
                    List<Quang_Promotion> promos = promoDAO.findAll();
                    for (Quang_Promotion p : promos) {
                        if (p.getCode().equalsIgnoreCase(voucherCode)) {
                            // Logic Voucher: Giảm theo số tiền (FIXED)
                            // Nếu muốn giảm theo % thì check p.getDiscountType() == "PERCENT"
                            if ("PERCENT".equalsIgnoreCase(p.getDiscountType())) {
                                 discount = subTotal * (p.getDiscountValue() / 100);
                            } else {
                                 discount = p.getDiscountValue();
                            }
                            break;
                        }
                    }
                }

                double finalAmount = subTotal + tax + ship - discount;
                return finalAmount > 0 ? finalAmount : 0;
            }
            // 3. Hiển thị thông tin
            String detail = String.format("Tiền hàng: %s\nThuế (10%%): %s\nShip: %s\nGiảm giá: -%s", 
                    df.format(subTotal), df.format(tax), df.format(ship), df.format(discount));
            lblDetail.setText(detail);
            lblFinalTotal.setText(df.format(total) + " VNĐ");

            // 4. TẠO QR CODE (Dùng API Online)
            // Nội dung QR: Chuyển khoản cho đơn hàng X số tiền Y
            String qrContent = "PAY_ORDER_" + orderId + "_AMT_" + (long)total;
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + qrContent;
            
            // Load ảnh từ Internet
            imgQR.setImage(new Image(qrUrl, true)); // true = load background (không treo app)

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
            
            // Gọi Service xử lý thanh toán thật
            service.payOrder(currentUsername, orderId, voucher);
            
            showAlert("Thành công", "Thanh toán đơn hàng " + orderId + " hoàn tất!");
            
            // Reset giao diện
            loadData(); // Cập nhật số dư
            txtOrderId.clear(); txtVoucher.clear();
            lblDetail.setText("..."); lblFinalTotal.setText("0 VNĐ"); imgQR.setImage(null);
            
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