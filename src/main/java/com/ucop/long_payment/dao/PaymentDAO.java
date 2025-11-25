package com.ucop.long_payment.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.long_payment.Long_Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class PaymentDAO extends AbstractDAO<Long_Payment, Long> {
    
    // Hàm tìm lịch sử thanh toán dựa trên ID người dùng
    // Logic: Tìm các Payment -> Thuộc về Order -> Của Customer có ID là ...
    public List<Long_Payment> findByUserId(Long userId) {
        try (Session session = getSession()) {
            // Câu lệnh HQL: Join bảng Payment với bảng Order để lọc theo User
            String hql = "SELECT p FROM Long_Payment p JOIN p.order o WHERE o.customer.id = :uid ORDER BY p.paymentDate DESC";
            
            Query<Long_Payment> query = session.createQuery(hql, Long_Payment.class);
            query.setParameter("uid", userId);
            
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}