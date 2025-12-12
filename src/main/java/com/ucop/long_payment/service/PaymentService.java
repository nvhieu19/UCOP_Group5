package com.ucop.long_payment.service;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.hieu_order.service.ShipmentService;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.dao.PaymentDAO;
import com.ucop.long_payment.dao.WalletDAO;
import com.ucop.quang_report.Quang_Promotion; 

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    private WalletDAO walletDAO = new WalletDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private ShipmentService shipmentService = new ShipmentService();
    
    // Dùng AbstractDAO để lấy Promotion của Quang nhanh gọn
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    // 1. Lấy ví
    public Long_Wallet getWallet(String username) {
        Dinh_User user = findUser(username);
        return getMyWallet(user);
    }

    public Long_Wallet getMyWallet(Dinh_User user) {
        if (user == null) return null;
        Long_Wallet wallet = walletDAO.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Long_Wallet(user, 0.0);
            walletDAO.save(wallet);
        }
        return wallet;
    }

    // 2. Nạp tiền
    public void deposit(String username, double amount) {
        Long_Wallet wallet = getWallet(username);
        if (wallet != null && amount > 0) {
            wallet.deposit(BigDecimal.valueOf(amount));
            walletDAO.update(wallet);
        }
    }

    // 3. Lịch sử giao dịch
    public List<Long_Payment> getHistory(String username) {
        return paymentDAO.findByUsername(username);
    }

    // --- LOGIC THANH TOÁN CHÍNH ---

    public Hieu_Order findPendingOrder(String username, Long orderId) {
        Hieu_Order order = orderDAO.findById(orderId);
        // Chỉ lấy đơn chưa trả và chưa ship
        if (order != null && order.getCustomer().getUsername().equals(username) 
                && !"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            return order;
        }
        return null;
    }

    // [CỐ ĐỊNH] Phí ship luôn là 30k
    public double getShippingFee(double orderValue, String shippingMethod) {
        return 30000; 
    }

    // [CÔNG THỨC CHUẨN] Tính tổng tiền (Sửa lỗi 53 tỷ)
    public double calculateFinalAmount(Hieu_Order order, String voucherCode, double shippingFee) {
        // 1. Lấy tiền hàng (SubTotal)
        double subTotal = (order.getSubTotal() != null) ? order.getSubTotal().doubleValue() : 0;
        
        // 2. Lấy thuế (Tax)
        double tax = (order.getTaxAmount() != null) ? order.getTaxAmount().doubleValue() : 0;

        // *Phòng hờ đơn cũ thiếu dữ liệu*: Tự tính lại nếu = 0
        if (subTotal == 0 && order.getTotalAmount() != null && order.getTotalAmount().doubleValue() > 0) {
            double oldTotal = order.getTotalAmount().doubleValue();
            subTotal = oldTotal / 1.1; 
            tax = subTotal * 0.1;
        }

        double discount = 0;
        // Check Voucher
        if (voucherCode != null && !voucherCode.isEmpty()) {
            try {
                List<Quang_Promotion> promos = promoDAO.findAll();
                for (Quang_Promotion p : promos) {
                    if (p.getCode().equalsIgnoreCase(voucherCode)) {
                        discount = p.getDiscountValue();
                        break;
                    }
                }
            } catch (Exception e) {}
        }

        // TỔNG = HÀNG + THUẾ + SHIP (30k) - GIẢM GIÁ
        double finalAmount = subTotal + tax + shippingFee - discount;
        return finalAmount > 0 ? finalAmount : 0;
    }

    public double calculateFinalAmount(Hieu_Order order, String voucherCode) {
        return calculateFinalAmount(order, voucherCode, 30000);
    }

    // --- THANH TOÁN BẰNG VÍ (Gọn nhẹ, TẠUTO SHIPMENT) ---
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee, String address) throws Exception {
        Long_Wallet wallet = getWallet(username);
        Hieu_Order order = findPendingOrder(username, orderId);

        if (order == null) throw new Exception("Không tìm thấy đơn hàng hoặc đơn đã thanh toán!");

        // Tính tiền chuẩn
        double finalAmount = calculateFinalAmount(order, voucherCode, shippingFee);

        // Kiểm tra số dư
        if (wallet.getBalance().doubleValue() < finalAmount) {
            throw new Exception("Số dư ví không đủ! Cần: " + finalAmount);
        }

        // 1. Trừ tiền ví
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(finalAmount)));
        walletDAO.update(wallet);

        // 2. Đổi trạng thái -> PAID (Đã thanh toán)
        order.setStatus("PAID");
        orderDAO.update(order);

        // 3. Lưu lịch sử giao dịch
        Long_Payment payment = new Long_Payment(order, "WALLET_QR", BigDecimal.valueOf(finalAmount));
        paymentDAO.save(payment);

        // ✅ FIX: TỰ ĐỘNG TẠO SHIPMENT SAU THANH TOÁN THÀNH CÔNG
        if (address != null && !address.isEmpty()) {
            shipmentService.createShipment(orderId, "Giao Tiêu Chuẩn", address, null);
        }
    }
    
    // Các hàm overload giữ nguyên để tương thích
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee) throws Exception {
        payOrder(username, orderId, voucherCode, shippingFee, null);
    }
    public void payOrder(String username, Long orderId, String voucherCode) throws Exception {
        payOrder(username, orderId, voucherCode, 30000);
    }

    // --- THANH TOÁN NGÂN HÀNG (QR) - TỰ ĐỘNG TẠO SHIPMENT ---
    public void payByBankTransfer(String username, Long orderId, double amount, String address) throws Exception {
        Hieu_Order order = findPendingOrder(username, orderId);
        if (order == null) throw new Exception("Đơn hàng không hợp lệ!");

        // Đổi trạng thái thành PAID (Đã thanh toán)
        order.setStatus("PAID");
        orderDAO.update(order);

        Long_Payment payment = new Long_Payment(order, "BANK_TRANSFER", BigDecimal.valueOf(amount));
        paymentDAO.save(payment);

        // ✅ FIX: TỰ ĐỘNG TẠO SHIPMENT SAU THANH TOÁN THÀNH CÔNG
        if (address != null && !address.isEmpty()) {
            shipmentService.createShipment(orderId, "Giao Tiêu Chuẩn", address, null);
        }
    }
    
    public void payByBankTransfer(String username, Long orderId, double amount) throws Exception {
        payByBankTransfer(username, orderId, amount, null);
    }

    // --- HOÀN TIỀN (Sửa lỗi hoàn thiếu) ---
    public void refundOrder(String adminUsername, Long orderId) throws Exception {
        Hieu_Order order = orderDAO.findById(orderId);
        
        if (order == null) throw new Exception("Không tìm thấy đơn hàng ID: " + orderId);
        
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new Exception("Đơn hàng này chưa thanh toán, không thể hoàn tiền!");
        }

        String customerName = order.getCustomer().getUsername();
        Long_Wallet customerWallet = getWallet(customerName);

        // Lấy tiền hàng + thuế để hoàn
        BigDecimal subTotal = order.getSubTotal();
        BigDecimal tax = order.getTaxAmount();

        // Tự tính lại nếu DB thiếu dữ liệu (Fix cho đơn cũ)
        if (subTotal == null) {
             subTotal = order.getTotalAmount().divide(BigDecimal.valueOf(1.1), java.math.RoundingMode.HALF_UP);
        }
        if (tax == null || tax.compareTo(BigDecimal.ZERO) == 0) {
             tax = subTotal.multiply(BigDecimal.valueOf(0.1));
        }

        // Hoàn lại: Hàng + Thuế (Ship 30k không hoàn)
        BigDecimal refundAmount = subTotal.add(tax);

        customerWallet.deposit(refundAmount);
        walletDAO.update(customerWallet);

        order.setStatus("REFUNDED");
        orderDAO.update(order);

        Long_Payment refundLog = new Long_Payment(order, "REFUND", refundAmount);
        paymentDAO.save(refundLog);
    }

    // ✅ FIX: Tạo Shipment cho phương thức thanh toán COD
    public void createShipmentForCOD(Long orderId, String address) throws Exception {
        if (orderId == null || address == null || address.isEmpty()) {
            throw new Exception("Thông tin đơn hàng hoặc địa chỉ không hợp lệ!");
        }
        shipmentService.createShipment(orderId, "Giao Tiêu Chuẩn", address, null);
    }

    private Dinh_User findUser(String username) {
        return new AbstractDAO<Dinh_User, Long>(){}.findAll().stream()
                .filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
}