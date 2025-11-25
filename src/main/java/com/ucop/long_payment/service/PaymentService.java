package com.ucop.long_payment.service;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.dao.PaymentDAO;
import com.ucop.long_payment.dao.WalletDAO;

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    private WalletDAO walletDAO = new WalletDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private OrderDAO orderDAO = new OrderDAO();

    // --- 1. CÁC HÀM CƠ BẢN (FIX LỖI CHO OrderListController) ---

    // Lấy Ví của User (Nếu chưa có thì tự tạo mới 0đ)
    public Long_Wallet getMyWallet(Dinh_User user) {
        Long_Wallet wallet = walletDAO.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Long_Wallet(user, 0); 
            walletDAO.save(wallet);
        }
        return wallet;
    }

    // Nạp tiền vào ví
    public void deposit(Dinh_User user, double amount) {
        Long_Wallet wallet = getMyWallet(user);
        wallet.deposit(BigDecimal.valueOf(amount));
        walletDAO.update(wallet);
    }

    // Cập nhật số dư ví (Sau khi trừ tiền) - OrderListController cần hàm này
    public void updateWallet(Long_Wallet wallet) {
        walletDAO.update(wallet);
    }

    // Lưu lịch sử giao dịch
    public void savePayment(Long_Payment payment) {
        paymentDAO.save(payment);
    }

    // Lấy lịch sử giao dịch (Cho PaymentController)
    public List<Long_Payment> getMyHistory(Dinh_User user) {
        return paymentDAO.findByUserId(user.getId());
    }

    // --- 2. TÍNH NĂNG NÂNG CAO: HOÀN TIỀN (REFUND) ---
    // Dành cho Staff/Admin thực hiện khi đơn hàng bị hủy
    public void refundOrder(Long orderId) throws Exception {
        Hieu_Order order = orderDAO.findById(orderId);
        
        if (order == null) throw new Exception("Không tìm thấy đơn hàng ID: " + orderId);
        
        // Chỉ hoàn tiền nếu đơn đã thanh toán
        if (!"PAID".equals(order.getStatus())) {
            throw new Exception("Đơn hàng này chưa thanh toán (hoặc đã hoàn tiền), không thể Refund!");
        }

        // 1. Lấy ví khách hàng
        Dinh_User customer = order.getCustomer();
        Long_Wallet customerWallet = getMyWallet(customer);

        // 2. Cộng lại tiền vào ví
        BigDecimal refundAmount = order.getTotalAmount(); 
        customerWallet.deposit(refundAmount);
        walletDAO.update(customerWallet);

        // 3. Đổi trạng thái đơn hàng
        order.setStatus("REFUNDED");
        orderDAO.update(order);

        // 4. Ghi log giao dịch hoàn tiền
        Long_Payment refundLog = new Long_Payment(order, "REFUND", refundAmount);
        paymentDAO.save(refundLog);
    }
}