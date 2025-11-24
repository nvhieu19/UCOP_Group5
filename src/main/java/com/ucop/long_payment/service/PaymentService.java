package com.ucop.long_payment.service;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.long_payment.Long_Payment;
import com.ucop.long_payment.Long_Wallet;
import com.ucop.long_payment.dao.PaymentDAO;
import com.ucop.long_payment.dao.WalletDAO;
import com.ucop.core.dao.AbstractDAO;
import java.math.BigDecimal;
import java.util.List;

public class PaymentService {
    private WalletDAO walletDAO = new WalletDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    
    // Helper tìm User (dùng tạm AbstractDAO để tránh import chéo phức tạp)
    private Dinh_User findUser(String username) {
        return new AbstractDAO<Dinh_User, Long>(){}.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }

    // 1. Lấy ví (nếu chưa có thì tự tạo ví mới với 0đ)
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

    // 2. Nạp tiền
    public void deposit(String username, double amount) {
        Long_Wallet wallet = getWallet(username);
        if (wallet != null && amount > 0) {
            wallet.deposit(BigDecimal.valueOf(amount)); // Gọi hàm cộng tiền trong Entity
            walletDAO.update(wallet);
        }
    }

    // 3. Lấy lịch sử giao dịch
    public List<Long_Payment> getHistory(String username) {
        return paymentDAO.findByUsername(username);
    }
}