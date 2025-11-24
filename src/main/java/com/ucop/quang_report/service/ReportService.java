package com.ucop.quang_report.service;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.quang_report.Quang_Promotion;
import com.ucop.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ReportService {
    
    // DAO để quản lý khuyến mãi (Thêm/Xóa Voucher)
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    // 1. Lấy danh sách khuyến mãi
    public List<Quang_Promotion> getAllPromotions() {
        return promoDAO.findAll();
    }

    // 2. Thêm khuyến mãi mới
    public void addPromotion(Quang_Promotion p) {
        promoDAO.save(p);
    }

    // 3. Thống kê Top sản phẩm bán chạy (Cho PieChart)
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

    // 4. Thống kê Doanh thu (Cho BarChart) - Ở đây demo lấy tất cả đơn đã PAID
    // Thực tế có thể group by Ngày/Tháng
    public Double getTotalRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(o.totalAmount) FROM Hieu_Order o WHERE o.status = 'PAID'";
            Query<Number> query = session.createQuery(hql, Number.class);
            Number result = query.uniqueResult();
            return (result != null) ? result.doubleValue() : 0.0;
        }
    }
}