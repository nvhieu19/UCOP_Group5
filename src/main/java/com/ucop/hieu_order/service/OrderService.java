package com.ucop.hieu_order.service;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.Hieu_OrderItem;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hai_catalog.service.CatalogService;
import com.ucop.quang_report.Quang_Promotion;
import com.ucop.core.dao.AbstractDAO;
import com.ucop.dinh_admin.Dinh_User;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();
    private CatalogService catalogService = new CatalogService();
    // Dùng DAO của Quang để check mã giảm giá
    private AbstractDAO<Quang_Promotion, Long> promoDAO = new AbstractDAO<Quang_Promotion, Long>() {};

    public List<Hai_Item> getAvailableProducts() {
        return catalogService.getAllItems();
    }
    
    // Lấy danh sách đơn hàng của một User (để hiển thị lịch sử)
    public List<Hieu_Order> getOrdersByUser(Long userId) {
        // Cần viết thêm hàm findByUser trong DAO, ở đây dùng tạm logic lọc Java
        // Thực tế nên viết HQL: FROM Hieu_Order WHERE customer.id = :id
        return orderDAO.findAll(); 
    }

    // --- HÀM TÍNH TOÁN TOÀN BỘ CHI PHÍ [Quan trọng] ---
    public void calculateOrderDetails(Hieu_Order order, String promoCodeInput) {
        // 1. Tính SubTotal (Tổng tiền hàng)
        BigDecimal subTotal = BigDecimal.ZERO;
        for (Hieu_OrderItem item : order.getOrderItems()) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(lineTotal);
        }
        order.setSubTotal(subTotal);

        // 2. Tính Thuế (VAT 10%)
        BigDecimal tax = subTotal.multiply(BigDecimal.valueOf(0.10));
        order.setTaxAmount(tax);

        // 3. Tính Ship (Ví dụ: Đơn < 1 triệu thì ship 30k, trên thì Free)
        BigDecimal ship = (subTotal.compareTo(BigDecimal.valueOf(1000000)) < 0) 
                          ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;
        order.setShippingFee(ship);

        // 4. Tính Giảm giá (Discount)
        BigDecimal discount = BigDecimal.ZERO;
        if (promoCodeInput != null && !promoCodeInput.isEmpty()) {
            List<Quang_Promotion> promos = promoDAO.findAll();
            for (Quang_Promotion p : promos) {
                if (p.getCode().equalsIgnoreCase(promoCodeInput)) {
                    // Logic giảm giá đơn giản: Trừ thẳng tiền
                    discount = BigDecimal.valueOf(p.getDiscountValue());
                    order.setPromotionCode(p.getCode());
                    break;
                }
            }
        }
        order.setDiscountAmount(discount);

        // 5. Tính Tổng cuối (Grand Total) = Sub + Tax + Ship - Discount
        BigDecimal finalTotal = subTotal.add(tax).add(ship).subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO; // Không được âm
        order.setTotalAmount(finalTotal);
    }

    public void createOrder(Hieu_Order order, Dinh_User customer) {
        order.setCustomer(customer);
        order.setStatus("PLACED"); // Chuyển trạng thái từ CART sang ĐÃ ĐẶT
        orderDAO.save(order);
    }
}