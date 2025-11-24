package com.ucop.dinh_admin.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import org.hibernate.Session;
import org.hibernate.query.Query;

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
}