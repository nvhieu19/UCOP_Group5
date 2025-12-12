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
<<<<<<< HEAD
        // 1. Lấy tiền hàng (SubTotal)
        double subTotal = (order.getSubTotal() != null) ? order.getSubTotal().doubleValue() : 0;
        
        // 2. Lấy thuế (Tax)
        double tax = (order.getTaxAmount() != null) ? order.getTaxAmount().doubleValue() : 0;
=======
        // FIX: Lấy đúng các thành phần tiền, không tính lại thuế
        double subTotal = order.getSubTotal().doubleValue();
        double tax = order.getTaxAmount().doubleValue(); // Lấy thuế đã tính, không tính lại
        double discount = 0;
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git

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

<<<<<<< HEAD
    // --- THANH TOÁN BẰNG VÍ (Gọn nhẹ, không tạo Shipment) ---
=======
    // [QUAN TRỌNG] Thực hiện thanh toán BẰNG VÍ (Trừ tiền thật) - CÓ ĐỊA CHỈ
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee, String address) throws Exception {
        Long_Wallet wallet = getWallet(username);
        Hieu_Order order = findPendingOrder(username, orderId);

        if (order == null) throw new Exception("Không tìm thấy đơn hàng hoặc đơn đã thanh toán!");

<<<<<<< HEAD
        // Tính tiền chuẩn
        double finalAmount = calculateFinalAmount(order, voucherCode, shippingFee);
=======
        // FIX: Lấy tổng tiền đã tính đúng từ Order (không tính lại)
        double finalAmount = order.getTotalAmount().doubleValue();
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git

        // Kiểm tra số dư
        if (wallet.getBalance().doubleValue() < finalAmount) {
            throw new Exception("Số dư ví không đủ! Cần: " + finalAmount);
        }

        // 1. Trừ tiền ví
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(finalAmount)));
        walletDAO.update(wallet);

<<<<<<< HEAD
        // 2. Đổi trạng thái -> SHIPPED (Đang vận chuyển)
        order.setStatus("SHIPPED");
=======
        // 2. Cập nhật trạng thái đơn hàng -> PAID
        order.setStatus("PAID");
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
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
    
    // Các hàm overload giữ nguyên để tương thích
    public void payOrder(String username, Long orderId, String voucherCode, double shippingFee) throws Exception {
        payOrder(username, orderId, voucherCode, shippingFee, null);
    }
    public void payOrder(String username, Long orderId, String voucherCode) throws Exception {
        payOrder(username, orderId, voucherCode, 30000);
    }

<<<<<<< HEAD
    // --- THANH TOÁN NGÂN HÀNG (QR) ---
=======
    // --- [MỚI] Xử lý thanh toán qua NGÂN HÀNG (Quét QR) - CÓ ĐỊA CHỈ ---
    // Không trừ ví, chỉ xác nhận đơn
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
    public void payByBankTransfer(String username, Long orderId, double amount, String address) throws Exception {
        Hieu_Order order = findPendingOrder(username, orderId);
        if (order == null) throw new Exception("Đơn hàng không hợp lệ!");

<<<<<<< HEAD
        // Chỉ đổi trạng thái, không trừ ví
        order.setStatus("SHIPPED");
=======
        // 1. Không trừ ví (Vì khách chuyển khoản từ ngoài vào)
        
        // 2. Cập nhật trạng thái -> PAID (FIX: thay SHIPPED thành PAID)
        order.setStatus("PAID");
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
        orderDAO.update(order);

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
    
    public void payByBankTransfer(String username, Long orderId, double amount) throws Exception {
        payByBankTransfer(username, orderId, amount, null);
    }

    // --- HOÀN TIỀN (Sửa lỗi hoàn thiếu) ---
    public void refundOrder(String adminUsername, Long orderId) throws Exception {
        Hieu_Order order = orderDAO.findById(orderId);
        
        if (order == null) throw new Exception("Không tìm thấy đơn hàng ID: " + orderId);
        
<<<<<<< HEAD
=======
        // Cho phép hoàn tiền cả đơn PAID và SHIPPED
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus())) {
            throw new Exception("Đơn hàng này chưa thanh toán, không thể hoàn tiền!");
        }

        String customerName = order.getCustomer().getUsername();
        Long_Wallet customerWallet = getWallet(customerName);

<<<<<<< HEAD
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
=======
        // FIX: Hoàn tiền hàng + thuế, không hoàn phí ship
        BigDecimal refundAmount = order.getSubTotal().add(order.getTaxAmount());
>>>>>>> branch 'master' of https://github.com/nvhieu19/UCOP_Group5.git

        customerWallet.deposit(refundAmount);
        walletDAO.update(customerWallet);

        order.setStatus("REFUNDED");
        orderDAO.update(order);

        Long_Payment refundLog = new Long_Payment(order, "REFUND", refundAmount);
        paymentDAO.save(refundLog);
    }

    private Dinh_User findUser(String username) {
        return new AbstractDAO<Dinh_User, Long>(){}.findAll().stream()
                .filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
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