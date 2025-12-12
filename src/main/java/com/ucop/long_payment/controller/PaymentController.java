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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleObjectProperty;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentController {

    @FXML private Label lblBalance;
    @FXML private TextField txtDeposit;
    @FXML private TableView<Long_Payment> tableHistory;
    @FXML private ComboBox<Hieu_Order> cbOrdersToPay;
    @FXML private Label lblOrderTotal;
    @FXML private TableColumn<Long_Payment, Long> colHistoryOrderId;

    private PaymentService service = new PaymentService();
    private OrderDAO orderDAO = new OrderDAO();
    private Dinh_User currentUser;
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Lỗi", "Chưa đăng nhập! Vui lòng đăng nhập lại.");
            return;
        }
        setupOrderComboBox();
        setupHistoryTable();
        loadData();
    }

    private void setupHistoryTable() {
        if (colHistoryOrderId != null) {
            colHistoryOrderId.setCellValueFactory(cellData -> {
                if (cellData.getValue().getOrder() != null) {
                    return new SimpleObjectProperty<>(cellData.getValue().getOrder().getId());
                }
                return null;
            });
        }
    }

    private void loadData() {
        if (currentUser == null) return;
        Long_Wallet wallet = service.getWallet(currentUser.getUsername());
        if (wallet != null) lblBalance.setText(df.format(wallet.getBalance()) + " VNĐ");
        tableHistory.setItems(FXCollections.observableArrayList(service.getHistory(currentUser.getUsername())));
        loadUnpaidOrders();
    }

    private void loadUnpaidOrders() {
        List<Hieu_Order> list = orderDAO.findAll().stream()
                .filter(o -> o.getCustomer().getUsername().equals(currentUser.getUsername()))
                .filter(o -> !"PAID".equals(o.getStatus()) && !"SHIPPED".equals(o.getStatus()) && !"COD_PENDING".equals(o.getStatus()))
                .collect(Collectors.toList());
        cbOrdersToPay.setItems(FXCollections.observableArrayList(list));
    }

    private void setupOrderComboBox() {
        cbOrdersToPay.setConverter(new StringConverter<Hieu_Order>() {
            @Override
            public String toString(Hieu_Order o) {
                return (o == null) ? "" : "Đơn #" + o.getId() + " (" + df.format(o.getTotalAmount()) + "đ)";
            }
            @Override
            public Hieu_Order fromString(String s) { return null; }
        });
        cbOrdersToPay.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                lblOrderTotal.setText("Tổng gốc: " + df.format(newVal.getTotalAmount()) + " VNĐ");
            }
        });
    }

    @FXML
    public void handleDeposit() {
        try {
            double amount = Double.parseDouble(txtDeposit.getText());
            service.deposit(currentUser.getUsername(), amount);
            txtDeposit.clear(); loadData();
            showInfo("Thành công", "Nạp tiền thành công!");
        } catch (Exception e) { showError("Lỗi", "Số tiền không hợp lệ!"); }
    }

    // --- 1. THANH TOÁN BẰNG VÍ (ĐÃ SỬA: HIỆN BẢNG XÁC NHẬN CHI TIẾT) ---
    @FXML
    public void handlePayByWallet() {
        Hieu_Order selectedOrder = cbOrdersToPay.getValue();
        if (selectedOrder == null) {
            showError("Lỗi", "Vui lòng chọn đơn hàng!");
            return;
        }

        String address = showAddressDialog();
        if (address == null) return;

        try {
            // Tính toán chi tiết để hiển thị cho người dùng xem trước
            double shipFee = 30000;
            double subTotal = selectedOrder.getSubTotal().doubleValue();
            double tax = selectedOrder.getTaxAmount().doubleValue();
            double finalTotal = service.calculateFinalAmount(selectedOrder, "", shipFee);

            // [MỚI] Hiện bảng xác nhận chi tiết tiền nong
            String confirmMsg = String.format(
                "Xác nhận thanh toán đơn hàng #%d?\n\n" +
                "Tiền hàng:      %15s VNĐ\n" +
                "Thuế VAT (10%%): %15s VNĐ\n" +
                "Phí Ship:       %15s VNĐ\n" +
                "-----------------------------------\n" +
                "TỔNG TRỪ VÍ:    %15s VNĐ",
                selectedOrder.getId(),
                df.format(subTotal),
                df.format(tax),
                df.format(shipFee),
                df.format(finalTotal)
            );

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận thanh toán Ví");
            confirmAlert.setHeaderText("Chi tiết giao dịch");
            confirmAlert.setContentText(confirmMsg);
            // Chỉnh font chữ dạng Monospaced để các số tiền thẳng hàng cho đẹp
            confirmAlert.getDialogPane().lookup(".content").setStyle("-fx-font-family: 'Consolas', 'Monospaced';");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Nếu bấm OK mới thực hiện trừ tiền
                service.payOrder(currentUser.getUsername(), selectedOrder.getId(), "", shipFee);
                showInfo("Thành công", "Đã trừ tiền ví! Đơn hàng đang được giao đến: " + address);
                loadData(); 
            }
        } catch (Exception e) {
            showError("Thất bại", e.getMessage());
        }
    }

    // --- 2. THANH TOÁN NGÂN HÀNG (QR) ---
    @FXML
    public void handlePayByBank() {
        Hieu_Order selectedOrder = cbOrdersToPay.getValue();
        if (selectedOrder == null) {
            showError("Lỗi", "Vui lòng chọn đơn hàng!");
            return;
        }

        String address = showAddressDialog();
        if (address == null) return;

        try {
            double shipFee = 30000;
            double finalTotal = service.calculateFinalAmount(selectedOrder, "", shipFee);
            String shipMethod = "Giao Tiêu Chuẩn";

            boolean confirm = showQRConfirmDialog(selectedOrder.getId(), finalTotal, shipMethod, shipFee, address);
            
            if (confirm) {
                service.payByBankTransfer(currentUser.getUsername(), selectedOrder.getId(), finalTotal);
                showInfo("Thành công", "Đã xác nhận chuyển khoản! Đơn hàng đang được giao.");
                loadData();
            }
        } catch (Exception e) {
            showError("Lỗi", e.getMessage());
        }
    }

    // --- 3. THANH TOÁN COD ---
    @FXML
    public void handlePayCOD() {
        Hieu_Order selectedOrder = cbOrdersToPay.getValue();
        if (selectedOrder == null) {
            showError("Lỗi", "Vui lòng chọn đơn hàng!");
            return;
        }
        
        String address = showAddressDialog();
        if (address == null) return;

        selectedOrder.setStatus("COD_PENDING"); 
        orderDAO.update(selectedOrder);
        
        showInfo("Thành công", "Đơn hàng #" + selectedOrder.getId() + " đã xác nhận COD.");
        loadData();
    }

    // --- 4. HOÀN TIỀN ---
    @FXML
    public void handleRefund() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hoàn tiền");
        dialog.setHeaderText("Nhập ID đơn hàng cần hoàn tiền:");
        dialog.setContentText("Nhập Mã Đơn (Cột 'Mã Đơn' trong bảng dưới):");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Long orderId = Long.parseLong(result.get());
                service.refundOrder(currentUser.getUsername(), orderId);
                showInfo("Thành công", "Đã hoàn tiền 100% cho đơn hàng #" + orderId);
                loadData();
            } catch (Exception e) {
                showError("Lỗi hoàn tiền", e.getMessage());
            }
        }
    }

    // --- DIALOGS ---
    private String showAddressDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Địa chỉ giao hàng");
        dialog.setHeaderText("Bước 1: Nhập thông tin nhận hàng");
        dialog.setContentText("Vui lòng nhập địa chỉ cụ thể:");
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && !result.get().trim().isEmpty() ? result.get().trim() : null;
    }

    private boolean showQRConfirmDialog(Long orderId, double total, String shipMethod, double shipFee, String address) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thanh toán");
        alert.setHeaderText("Bước 2: Quét mã QR để xác nhận");

        String bankId = "MB"; String accNo = "0000123456789"; 
        String addInfo = "TT Don " + orderId;
        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s",
                bankId, accNo, (long)total, addInfo.replace(" ", "%20"));
        
        ImageView qrView = new ImageView(new Image(qrUrl, true));
        qrView.setFitWidth(200); qrView.setFitHeight(200);

        Label details = new Label(String.format("Đơn hàng: #%d\nGiao: %s\nTổng: %s VNĐ", orderId, address, df.format(total)));
        details.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox content = new VBox(10, qrView, details);
        content.setAlignment(Pos.CENTER);
        alert.getDialogPane().setContent(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setContentText(message); alert.showAndWait();
    }
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(message); alert.showAndWait();
    }
}