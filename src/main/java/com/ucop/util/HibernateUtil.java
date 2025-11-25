package com.ucop.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// Import class của các thành viên
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
                
                // 1. Phần của ĐỊNH (Admin)
                config.addAnnotatedClass(Dinh_Role.class);
                config.addAnnotatedClass(Dinh_User.class);
                // Mới thêm: Nhật ký hoạt động (Lấy từ server về)
                try {
                    config.addAnnotatedClass(Dinh_AuditLog.class);
                } catch (Exception e) {
                    System.out.println("Chưa có file Dinh_AuditLog.java, bỏ qua...");
                }

                // 2. Phần của HẢI (Catalog)
                config.addAnnotatedClass(Hai_Category.class);
                config.addAnnotatedClass(Hai_Item.class);
                // [MỚI] Đăng ký Entity Kho & Tồn kho (Của BẠN làm - Giữ lại)
                config.addAnnotatedClass(Hai_Warehouse.class);
                config.addAnnotatedClass(Hai_StockItem.class);
                
                // 3. Phần của HIẾU (Order)
                config.addAnnotatedClass(Hieu_Order.class);
                config.addAnnotatedClass(Hieu_OrderItem.class);
                
                // 4. Phần của LONG (Payment)
                config.addAnnotatedClass(Long_Wallet.class);
                config.addAnnotatedClass(Long_Payment.class);
                
                // 5. Phần của QUANG (Report)
                config.addAnnotatedClass(Quang_Promotion.class);
                
                sessionFactory = config.buildSessionFactory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}