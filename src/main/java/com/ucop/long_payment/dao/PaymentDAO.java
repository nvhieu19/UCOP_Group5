package com.ucop.long_payment.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.long_payment.Long_Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

// Class này chuyên dùng để lấy lịch sử giao dịch từ Database
public class PaymentDAO extends AbstractDAO<Long_Payment, Long> {
    
    // Hàm tìm tất cả giao dịch của 1 user (username)
    public List<Long_Payment> findByUsername(String username) {
        try (Session session = getSession()) {
            // Lệnh này tìm trong bảng Payment, dựa vào username của người đặt đơn hàng
            String hql = "SELECT p FROM Long_Payment p WHERE p.order.customer.username = :u ORDER BY p.paymentDate DESC";
            Query<Long_Payment> query = session.createQuery(hql, Long_Payment.class);
            query.setParameter("u", username);
            return query.list();
        }
    }
}