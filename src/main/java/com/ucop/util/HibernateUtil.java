package com.ucop.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// Import class của các thành viên (Hiện tại mới có Định)
import com.ucop.dinh_admin.*; 
import com.ucop.hai_catalog.*;
import com.ucop.hieu_order.*;
import com.ucop.long_payment.*;
import com.ucop.quang_report.*;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration config = new Configuration();
                config.configure(); 
                
                // --- KHU VỰC ĐĂNG KÝ CLASS CỦA 5 THÀNH VIÊN ---
                
                // 1. Phần của ĐỊNH (admin dz khoai lang)
                config.addAnnotatedClass(Dinh_Role.class);
                config.addAnnotatedClass(Dinh_User.class);
                // Mới thêm phần xem nhật kí hoạt động
                config.addAnnotatedClass(Dinh_AuditLog.class);

                // 2. Phần của HẢI (Catalog) - THÊM MỚI
                config.addAnnotatedClass(Hai_Category.class);
                config.addAnnotatedClass(Hai_Item.class);
                
                // 3. Phần của HIẾU (Order) - THÊM MỚI
                config.addAnnotatedClass(Hieu_Order.class);
                config.addAnnotatedClass(Hieu_OrderItem.class);
                
                // 4. Phần của LONG (Payment) - THÊM MỚI
                config.addAnnotatedClass(Long_Wallet.class);
                config.addAnnotatedClass(Long_Payment.class);
                // Sau này Hải, Hiếu, Long, Quang làm xong sẽ thêm dòng vào đây
                
                // 5. Phần của QUANG (Report) - THÊM MỚI
                config.addAnnotatedClass(Quang_Promotion.class);
                
                sessionFactory = config.buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}