package com.ucop.quang_report.service;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.quang_report.Quang_Promotion;
import com.ucop.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ReportService {
    
    // DAO để quản lý khuyến mãi (Sử dụng AbstractDAO có sẵn)
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    // 1. Lấy danh sách khuyến mãi (Read)
    public List<Quang_Promotion> getAllPromotions() {
        return promoDAO.findAll();
    }

    // 2. Thêm khuyến mãi mới (Create)
    public void addPromotion(Quang_Promotion p) {
        promoDAO.save(p);
    }
    
    // --- [SV5 BỔ SUNG] HOÀN THIỆN CRUD ---
    
    // 3. Cập nhật khuyến mãi (Update)
    public void updatePromotion(Quang_Promotion p) {
        promoDAO.update(p);
    }

    // 4. Xóa khuyến mãi (Delete)
    public void deletePromotion(Long promoId) {
        promoDAO.delete(promoId);
    }
    
    // -------------------------------------

    // 5. Thống kê Top sản phẩm bán chạy (Cho PieChart)
    // Trả về List các mảng Object: [Tên sản phẩm, Tổng số lượng bán]
    public List<Object[]> getTopSellingProducts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // HQL: Gom nhóm theo tên sản phẩm và tính tổng số lượng từ bảng OrderItem
            String hql = "SELECT oi.item.name, SUM(oi.quantity) " +
                         "FROM Hieu_OrderItem oi " +
                         "GROUP BY oi.item.name " +
                         "ORDER BY SUM(oi.quantity) DESC";
            
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setMaxResults(5); // Chỉ lấy Top 5
            return query.list();
        }
    }

    // 6. Thống kê Doanh thu (Cho BarChart) - Lấy tổng các đơn đã PAID
    public Double getTotalRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(o.totalAmount) FROM Hieu_Order o WHERE o.status = 'PAID'";
            Query<Number> query = session.createQuery(hql, Number.class);
            Number result = query.uniqueResult();
            return (result != null) ? result.doubleValue() : 0.0;
        }
    }
}