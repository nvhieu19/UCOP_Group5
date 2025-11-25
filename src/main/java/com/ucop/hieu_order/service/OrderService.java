package com.ucop.hieu_order.service;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.Hieu_OrderItem;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hai_catalog.service.CatalogService;
import com.ucop.quang_report.Quang_Promotion;
import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;




public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();
    private CatalogService catalogService = new CatalogService();
    
    // Dùng Generic DAO để truy vấn bảng Khuyến mãi
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    // Lấy danh sách sản phẩm từ kho
    public List<Hai_Item> getAvailableProducts() {
        return catalogService.getAllItems();
    }

    /**
     * HÀM TÍNH TOÁN CHI PHÍ ĐƠN HÀNG (Logic nghiệp vụ)
     */
    public void calculateOrderDetails(Hieu_Order order, String promoCodeInput) {
        // 1. Tính SubTotal
        BigDecimal subTotal = BigDecimal.ZERO;
        int totalItems = 0;
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (Hieu_OrderItem item : order.getOrderItems()) {
                if (item != null && item.getPrice() != null) {
                    BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    subTotal = subTotal.add(lineTotal);
                    totalItems += item.getQuantity();
                }
            }
        }
        order.setSubTotal(subTotal);

        // 2. Tính Thuế (VAT 10%)
        BigDecimal tax = subTotal.multiply(BigDecimal.valueOf(0.10));
        order.setTaxAmount(tax);

        // 3. Tính Ship (Luôn tính 30,000 đ nếu có sản phẩm, trừ khi giỏ trống)
        BigDecimal ship = BigDecimal.ZERO;
        if (totalItems > 0) {
            // Có sản phẩm trong giỏ -> luôn tính phí ship 30,000 đ
            ship = BigDecimal.valueOf(30000);
        }
        order.setShippingFee(ship);
        
        System.out.println("[DEBUG] SubTotal: " + subTotal + ", Items: " + totalItems + ", Ship: " + ship);

        // 4. Tính Giảm giá
        BigDecimal discount = BigDecimal.ZERO;
        order.setPromotionCode(null); // Reset mã promo trước
        if (promoCodeInput != null && !promoCodeInput.trim().isEmpty()) {
            List<Quang_Promotion> promos = promoDAO.findAll();
            for (Quang_Promotion p : promos) {
                if (p.getCode().equalsIgnoreCase(promoCodeInput.trim())) {
                    discount = BigDecimal.valueOf(p.getDiscountValue());
                    order.setPromotionCode(p.getCode());
                    break; 
                }
            }
        }
        order.setDiscountAmount(discount);

        // 5. Tính Tổng cuối
        BigDecimal grandTotal = subTotal.add(tax).add(ship).subtract(discount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }
        order.setTotalAmount(grandTotal);
    }

    /**
     * LƯU ĐƠN HÀNG (ĐÃ FIX LỖI TRANSIENT OBJECT)
     * Sử dụng Session thủ công để quản lý User state
     */
    public void createOrder(Hieu_Order order, Dinh_User customer) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();

            Dinh_User managedCustomer = null;

            // 1. KIỂM TRA ID CỦA KHÁCH HÀNG
            if (customer.getId() != null) {
                // Nếu khách hàng ĐÃ CÓ ID -> Lấy từ DB lên để attach vào session
                managedCustomer = session.get(Dinh_User.class, customer.getId());
            } 
            
            // Nếu khách hàng CHƯA CÓ ID (id == null) hoặc tìm không thấy trong DB
            if (managedCustomer == null) {
                // -> Đây là khách mới, cần lưu vào DB trước để sinh ID
                // Kiểm tra trùng username trước khi lưu để tránh lỗi
                // (Trong demo này ta lưu luôn, nếu trùng username thì Hibernate sẽ báo lỗi khác)
                session.save(customer); 
                managedCustomer = customer; // Giờ nó đã có ID sau khi save
            }

            // 2. Gán User đã được quản lý (managed) vào Order
            order.setCustomer(managedCustomer);
            
            // ... (Phần còn lại giữ nguyên) ...
            order.setStatus("PLACED");
            session.save(order);
            
            transaction.commit();
            System.out.println("✅ Đã tạo đơn hàng thành công: ID " + order.getId());

        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu đơn hàng: " + e.getMessage());
        } finally {
            session.close();
        }
    }
}