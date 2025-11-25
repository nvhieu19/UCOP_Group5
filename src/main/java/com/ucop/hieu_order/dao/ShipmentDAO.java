package com.ucop.hieu_order.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.util.HibernateUtil;
import com.ucop.hieu_order.Hieu_Shipment;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class ShipmentDAO extends AbstractDAO<Hieu_Shipment, Long> {

    /**
     * Tìm vận đơn theo mã vận đơn
     */
    public Hieu_Shipment findByTrackingNumber(String trackingNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Hieu_Shipment WHERE trackingNumber = :trackingNumber";
            @SuppressWarnings("unchecked")
            Query<Hieu_Shipment> query = session.createQuery(hql);
            query.setParameter("trackingNumber", trackingNumber);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tìm tất cả vận đơn của một đơn hàng
     */
    public List<Hieu_Shipment> findByOrderId(Long orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Hieu_Shipment WHERE order.id = :orderId";
            @SuppressWarnings("unchecked")
            Query<Hieu_Shipment> query = session.createQuery(hql);
            query.setParameter("orderId", orderId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Tìm tất cả vận đơn với trạng thái nhất định
     */
    public List<Hieu_Shipment> findByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Hieu_Shipment WHERE status = :status ORDER BY createdAt DESC";
            @SuppressWarnings("unchecked")
            Query<Hieu_Shipment> query = session.createQuery(hql);
            query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
