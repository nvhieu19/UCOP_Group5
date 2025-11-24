package com.ucop.long_payment.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.long_payment.Long_Wallet;
import org.hibernate.Session;
import org.hibernate.query.Query;

// Class này chuyên dùng để tìm Ví tiền trong Database
public class WalletDAO extends AbstractDAO<Long_Wallet, Long> {

    // Hàm tìm ví dựa theo ID của User
    public Long_Wallet findByUserId(Long userId) {
        try (Session session = getSession()) {
            String hql = "FROM Long_Wallet w WHERE w.user.id = :uid";
            Query<Long_Wallet> query = session.createQuery(hql, Long_Wallet.class);
            query.setParameter("uid", userId);
            return query.uniqueResult();
        }
    }
}