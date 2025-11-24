package com.ucop.hieu_order.service;

import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hai_catalog.service.CatalogService;
import com.ucop.dinh_admin.Dinh_User; // Cần user để biết ai mua

import java.util.List;

public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();
    private CatalogService catalogService = new CatalogService(); // Gọi service của Hải để lấy hàng

    // Lấy danh sách sản phẩm đang có trong kho để bán
    public List<Hai_Item> getAvailableProducts() {
        return catalogService.getAllItems();
    }

    // Lưu đơn hàng
    public void createOrder(Hieu_Order order, Dinh_User customer) {
        order.setCustomer(customer); // Gán khách hàng
        order.calculateTotal();      // Tính lại tổng tiền lần cuối cho chắc
        orderDAO.save(order);        // Lưu xuống DB (Hibernate tự lưu cả OrderItem)
    }
}