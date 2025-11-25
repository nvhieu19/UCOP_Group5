package com.ucop.hieu_order.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.util.HibernateUtil;
import com.ucop.hieu_order.Hieu_Order;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class OrderDAO extends AbstractDAO<Hieu_Order, Long> {

    /**
     * Tìm tất cả đơn hàng của một người dùng cụ thể với một trạng thái nhất định.
     * @param status Trạng thái của đơn hàng (ví dụ: "PENDING").
     * @param userId ID của người dùng (khách hàng).
     * @return Một danh sách các đơn hàng phù hợp.
     */
    public List<Hieu_Order> findOrdersByStatusAndUser(List<String> statuses, Long userId) {
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Hieu_Order WHERE status IN (:statuses) AND customer.id = :userId";
            @SuppressWarnings("unchecked")
            Query<Hieu_Order> query = session.createQuery(hql);
            query.setParameterList("statuses", statuses);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // Trả về danh sách rỗng nếu có lỗi
        }
    }
}