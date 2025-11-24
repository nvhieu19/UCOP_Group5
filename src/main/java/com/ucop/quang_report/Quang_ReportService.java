package com.ucop.quang_report;

import org.hibernate.Session;
import org.hibernate.query.Query;
import java.math.BigDecimal;
import java.util.List;

public class Quang_ReportService {
    private Session session;

    public Quang_ReportService(Session session) {
        this.session = session;
    }

    // 1. Thống kê tổng doanh thu (Chỉ tính các đơn đã PAID)
    public BigDecimal getTotalRevenue() {
        // HQL: Sum cột totalAmount từ bảng Order của Hiếu, điều kiện status = 'PAID'
        String hql = "SELECT SUM(o.totalAmount) FROM Hieu_Order o WHERE o.status = 'PAID'";
        Query<BigDecimal> query = session.createQuery(hql, BigDecimal.class);
        
        BigDecimal revenue = query.uniqueResult();
        return (revenue != null) ? revenue : BigDecimal.ZERO;
    }

    // 2. Đếm số lượng đơn hàng theo trạng thái
    public long countOrdersByStatus(String status) {
        String hql = "SELECT COUNT(o) FROM Hieu_Order o WHERE o.status = :st";
        Query<Long> query = session.createQuery(hql, Long.class);
        query.setParameter("st", status);
        return query.uniqueResult();
    }
}