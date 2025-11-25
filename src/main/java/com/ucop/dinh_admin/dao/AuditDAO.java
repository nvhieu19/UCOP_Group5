package com.ucop.dinh_admin.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_AuditLog;
import org.hibernate.Session;
import java.util.List;

public class AuditDAO extends AbstractDAO<Dinh_AuditLog, Long> {
    // Chỉ cần kế thừa AbstractDAO là có sẵn hàm save()
	
	// 2. Thêm hàm này để sau này đổ dữ liệu ra bảng "Lịch sử hoạt động" cho Admin xem
    public List<Dinh_AuditLog> findAll() {
        try (Session session = getSession()) {
            // Lưu ý: Sắp xếp giảm dần theo thời gian (mới nhất lên đầu) cho dễ nhìn
            return session.createQuery("FROM Dinh_AuditLog ORDER BY timestamp DESC", Dinh_AuditLog.class).list();
        }
    }
}