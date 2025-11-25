package com.ucop.dinh_admin.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class UserDAO extends AbstractDAO<Dinh_User, Long> {
    
    // Hàm riêng cho User: Kiểm tra đăng nhập
    public Dinh_User findByUsernameAndPassword(String username, String password) {
        try (Session session = getSession()) {
            String hql = "FROM Dinh_User WHERE username = :u AND password = :p";
            Query<Dinh_User> query = session.createQuery(hql, Dinh_User.class);
            query.setParameter("u", username);
            query.setParameter("p", password);
            return query.uniqueResult();
        }
    }
 // 2. CẦN THÊM: Dùng để check trùng tên khi Tạo mới (Create)
    public Dinh_User findByUsername(String username) {
        try (Session session = getSession()) {
            String hql = "FROM Dinh_User WHERE username = :u";
            Query<Dinh_User> query = session.createQuery(hql, Dinh_User.class);
            query.setParameter("u", username);
            return query.uniqueResult();
        }
    }

    // 3. CẦN THÊM: Dùng để hiển thị danh sách lên bảng (Dashboard)
    // (Nếu AbstractDAO của bạn chưa có hàm findAll thì thêm cái này)
    public List<Dinh_User> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM Dinh_User", Dinh_User.class).list();
        }
    }
}
