package com.ucop.long_payment.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.long_payment.Long_Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class PaymentDAO extends AbstractDAO<Long_Payment, Long> {
    
    // Hàm tìm tất cả giao dịch của 1 user (username)
    public List<Long_Payment> findByUsername(String username) {
        try (Session session = getSession()) {
            // Truy vấn HQL: Join từ Payment -> Order -> Customer -> Username
            String hql = "SELECT p FROM Long_Payment p WHERE p.order.customer.username = :u ORDER BY p.paymentDate DESC";
            @SuppressWarnings("unchecked")
            Query<Long_Payment> query = session.createQuery(hql);
            query.setParameter("u", username);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Tìm giao dịch theo User ID (Hỗ trợ hàm getMyHistory cũ)
    public List<Long_Payment> findByUserId(Long userId) {
        try (Session session = getSession()) {
            String hql = "SELECT p FROM Long_Payment p WHERE p.order.customer.id = :uid ORDER BY p.paymentDate DESC";
            @SuppressWarnings("unchecked")
            Query<Long_Payment> query = session.createQuery(hql);
            query.setParameter("uid", userId);
            return query.list();
        }
    }
}