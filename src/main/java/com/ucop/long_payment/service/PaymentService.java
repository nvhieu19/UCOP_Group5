package com.ucop.long_payment.service;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.hai_catalog.dao.ItemDAO;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.dao.PaymentDAO;
import com.ucop.long_payment.dao.WalletDAO;
import com.ucop.quang_report.Quang_Promotion; // Import code của Quang

import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    private WalletDAO walletDAO = new WalletDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private OrderDAO orderDAO = new OrderDAO();
    
    // Dùng AbstractDAO để lấy Promotion của Quang nhanh gọn
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    // 1. Lấy ví
    public Long_Wallet getWallet(String username) {
        Dinh_User user = findUser(username);
        if (user == null) return null;
        Long_Wallet wallet = walletDAO.findByUserId(user.getId());
        if (wallet == null) {
            wallet = new Long_Wallet(user, 0.0);
            walletDAO.save(wallet);
        }
        return wallet;
    }

    // 2. Nạp tiền (Cơ bản)
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

    // --- PHẦN NÂNG CAO: THANH TOÁN ĐƠN HÀNG ---

    // Tìm đơn hàng chưa thanh toán của User
    public Hieu_Order findPendingOrder(String username, Long orderId) {
        Hieu_Order order = orderDAO.findById(orderId);
        if (order != null && order.getCustomer().getUsername().equals(username) 
                && !"PAID".equals(order.getStatus())) {
            return order;
        }
        return null;
    }

    // Tính toán số tiền cuối cùng (Kèm Thuế, Ship, Voucher)
    public double calculateFinalAmount(Hieu_Order order, String voucherCode) throws Exception {
        double subTotal = order.getTotalAmount().doubleValue();
        double tax = subTotal * 0.1; // Thuế 10%
        double ship = 30000;         // Phí ship cứng 30k
        double discount = 0;

        // Check Voucher của Quang
        if (voucherCode != null && !voucherCode.isEmpty()) {
            List<Quang_Promotion> promos = promoDAO.findAll();
            for (Quang_Promotion p : promos) {
                if (p.getCode().equalsIgnoreCase(voucherCode)) {
                    discount = p.getDiscountValue();
                    break;
                }
            }
        }

        double finalAmount = subTotal + tax + ship - discount;
        return finalAmount > 0 ? finalAmount : 0;
    }

    // Thực hiện thanh toán
    public void payOrder(String username, Long orderId, String voucherCode) throws Exception {
        Long_Wallet wallet = getWallet(username);
        Hieu_Order order = findPendingOrder(username, orderId);

        if (order == null) throw new Exception("Không tìm thấy đơn hàng hoặc đơn đã thanh toán!");

        // Tính tiền
        double finalAmount = calculateFinalAmount(order, voucherCode);

        // Kiểm tra số dư
        if (wallet.getBalance().doubleValue() < finalAmount) {
            throw new Exception("Số dư ví không đủ! Cần: " + finalAmount);
        }

        // 1. Trừ tiền ví
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(finalAmount)));
        walletDAO.update(wallet);

        // 2. Cập nhật trạng thái đơn hàng thành PAID (Của Hiếu)
        order.setStatus("PAID");
        orderDAO.update(order);

        // 3. Lưu lịch sử giao dịch
        Long_Payment payment = new Long_Payment(order, "WALLET_QR", BigDecimal.valueOf(finalAmount));
        paymentDAO.save(payment);
    }

    private Dinh_User findUser(String username) {
        return new AbstractDAO<Dinh_User, Long>(){}.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }
}