package com.ucop.long_payment.service;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.Hieu_Shipment;
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

    // 1. Lấy ví (Hỗ trợ cả cách gọi cũ và mới)
    public Long_Wallet getWallet(String username) {
        Dinh_User user = findUser(username);
        return getMyWallet(user);
    }

    // Hàm này giữ lại để tương thích với code bên Hiếu (OrderListController)
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

    // --- PHẦN NÂNG CAO: TÍNH TOÁN SHIP & THANH TOÁN ---

    // Tìm đơn hàng chưa thanh toán
    public Hieu_Order findPendingOrder(String username, Long orderId) {
        Hieu_Order order = orderDAO.findById(orderId);
        if (order != null && order.getCustomer().getUsername().equals(username) 
                && !"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            return order;
        }
        return null;
    }

    // Logic tính phí Ship: CỐ ĐỊNH 30K
    public double getShippingFee(double orderValue, String shippingMethod) {
        return 30000; // Luôn là 30k bất kể đơn to nhỏ
    }

    // Tính tổng tiền cuối cùng (Có Voucher + Phí Ship cụ thể)
    public double calculateFinalAmount(Hieu_Order order, String voucherCode, double shippingFee) {
        // FIX: Lấy đúng các thành phần tiền, không tính lại thuế
        double subTotal = order.getSubTotal().doubleValue();
        double tax = order.getTaxAmount().doubleValue(); // Lấy thuế đã tính, không tính lại
        double discount = 0;

        // Check Voucher của Quang
        if (voucherCode != null && !voucherCode.isEmpty()) {
            try {
                List<Quang_Promotion> promos = promoDAO.findAll();
                for (Quang_Promotion p : promos) {
                    if (p.getCode().equalsIgnoreCase(voucherCode)) {
                        discount = p.getDiscountValue();
                        break;
                    }
                }
            } catch (Exception e) {
                // Bỏ qua lỗi nếu bảng Promotion chưa có dữ liệu
            }
        }

        double finalAmount = subTotal + tax + shippingFee - discount;
        return finalAmount > 0 ? finalAmount : 0;
    }

    // Hàm hỗ trợ tính toán mặc định (để code cũ không lỗi)
    public double calculateFinalAmount(Hieu_Order order, String voucherCode) {
        return calculateFinalAmount(order, voucherCode, 30000);
    }

    // [QUAN TRỌNG] Thực hiện thanh toán BẰNG VÍ (Trừ tiền thật) - CÓ ĐỊA CHỈ
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee, String address) throws Exception {
        Long_Wallet wallet = getWallet(username);
        Hieu_Order order = findPendingOrder(username, orderId);

        if (order == null) throw new Exception("Không tìm thấy đơn hàng hoặc đơn đã thanh toán!");

        // FIX: Lấy tổng tiền đã tính đúng từ Order (không tính lại)
        double finalAmount = order.getTotalAmount().doubleValue();

        // Kiểm tra số dư
        if (wallet.getBalance().doubleValue() < finalAmount) {
            throw new Exception("Số dư ví không đủ! Cần: " + finalAmount);
        }

        // 1. Trừ tiền ví
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(finalAmount)));
        walletDAO.update(wallet);

        // 2. Cập nhật trạng thái đơn hàng -> PAID
        order.setStatus("PAID");
        orderDAO.update(order);

        // 3. Lưu lịch sử giao dịch
        Long_Payment payment = new Long_Payment(order, "WALLET_QR", BigDecimal.valueOf(finalAmount));
        paymentDAO.save(payment);
        
        // 4. [MỚI] TỰ ĐỘNG TẠO VẬN CHUYỂN SAU KHI THANH TOÁN
        try {
            Dinh_User staff = findAdminStaff();
            String deliveryAddress = (address != null && !address.trim().isEmpty()) ? address : "Địa chỉ chưa xác định";
            Hieu_Shipment shipment = shipmentService.createShipment(
                order.getId(), 
                "Standard", 
                deliveryAddress, 
                staff
            );
            System.out.println("✅ Đã tạo vận chuyển tự động: " + shipment.getTrackingNumber());
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi tạo vận chuyển tự động: " + e.getMessage());
            // Không ném exception, vì thanh toán đã thành công
        }
    }
    
    // Hàm overload để tương thích code cũ (không có địa chỉ)
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee) throws Exception {
        payOrder(username, orderId, voucherCode, shippingFee, null);
    }

    // Hàm overload để tương thích code cũ
    public void payOrder(String username, Long orderId, String voucherCode) throws Exception {
        payOrder(username, orderId, voucherCode, 30000);
    }

    // --- [MỚI] Xử lý thanh toán qua NGÂN HÀNG (Quét QR) - CÓ ĐỊA CHỈ ---
    // Không trừ ví, chỉ xác nhận đơn
    public void payByBankTransfer(String username, Long orderId, double amount, String address) throws Exception {
        Hieu_Order order = findPendingOrder(username, orderId);
        if (order == null) throw new Exception("Đơn hàng không hợp lệ hoặc đã thanh toán!");

        // 1. Không trừ ví (Vì khách chuyển khoản từ ngoài vào)
        
        // 2. Cập nhật trạng thái -> PAID (FIX: thay SHIPPED thành PAID)
        order.setStatus("PAID");
        orderDAO.update(order);

        // 3. Lưu lịch sử (Loại: BANK_TRANSFER)
        Long_Payment payment = new Long_Payment(order, "BANK_TRANSFER", BigDecimal.valueOf(amount));
        paymentDAO.save(payment);
        
        // 4. [MỚI] TỰ ĐỘNG TẠO VẬN CHUYỂN SAU KHI THANH TOÁN
        try {
            Dinh_User staff = findAdminStaff();
            String deliveryAddress = (address != null && !address.trim().isEmpty()) ? address : "Địa chỉ chưa xác định";
            Hieu_Shipment shipment = shipmentService.createShipment(
                order.getId(), 
                "Express", 
                deliveryAddress, 
                staff
            );
            System.out.println("✅ Đã tạo vận chuyển tự động: " + shipment.getTrackingNumber());
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi tạo vận chuyển tự động: " + e.getMessage());
            // Không ném exception, vì thanh toán đã thành công
        }
    }
    
    // Hàm overload để tương thích code cũ (không có địa chỉ)
    public void payByBankTransfer(String username, Long orderId, double amount) throws Exception {
        payByBankTransfer(username, orderId, amount, null);
    }

    // --- CHỨC NĂNG HOÀN TIỀN (REFUND) ---
    public void refundOrder(String adminUsername, Long orderId) throws Exception {
        Hieu_Order order = orderDAO.findById(orderId);
        
        if (order == null) throw new Exception("Không tìm thấy đơn hàng ID: " + orderId);
        
        // Cho phép hoàn tiền cả đơn PAID và SHIPPED
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new Exception("Đơn hàng này chưa thanh toán, không thể hoàn tiền!");
        }

        String customerName = order.getCustomer().getUsername();
        Long_Wallet customerWallet = getWallet(customerName);

        // FIX: Hoàn tiền hàng + thuế, không hoàn phí ship
        BigDecimal refundAmount = order.getSubTotal().add(order.getTaxAmount());

        customerWallet.deposit(refundAmount);
        walletDAO.update(customerWallet);

        order.setStatus("REFUNDED");
        orderDAO.update(order);

        Long_Payment refundLog = new Long_Payment(order, "REFUND", refundAmount);
        paymentDAO.save(refundLog);
    }

    private Dinh_User findUser(String username) {
        return new AbstractDAO<Dinh_User, Long>(){}.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }
    
    // [MỚI] Tìm nhân viên admin để gán vào vận chuyển
    private Dinh_User findAdminStaff() {
        try {
            List<Dinh_User> allUsers = new AbstractDAO<Dinh_User, Long>(){}.findAll();
            // Tìm user có id = 1 hoặc tìm user đầu tiên
            for (Dinh_User user : allUsers) {
                if (user.getId() == 1) {
                    return user;
                }
            }
            // Nếu không tìm thấy id=1, trả về user đầu tiên
            return allUsers.isEmpty() ? null : allUsers.get(0);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm admin staff: " + e.getMessage());
            return null;
        }
    }
}