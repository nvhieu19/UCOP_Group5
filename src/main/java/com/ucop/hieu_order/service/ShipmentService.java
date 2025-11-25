package com.ucop.hieu_order.service;

import com.ucop.hieu_order.Hieu_Shipment;
import com.ucop.hieu_order.Hieu_Order;
import com.ucop.hieu_order.dao.ShipmentDAO;
import com.ucop.hieu_order.dao.OrderDAO;
import com.ucop.dinh_admin.Dinh_User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShipmentService {
    private ShipmentDAO shipmentDAO = new ShipmentDAO();
    private OrderDAO orderDAO = new OrderDAO();

    /**
     * Tạo vận đơn mới khi đơn hàng được xác nhận thanh toán
     */
    public Hieu_Shipment createShipment(Long orderId, String shippingMethod, String address, Dinh_User staff) {
        Hieu_Order order = orderDAO.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        Hieu_Shipment shipment = new Hieu_Shipment();
        shipment.setOrder(order);
        shipment.setShippingMethod(shippingMethod);
        shipment.setAddress(address);
        shipment.setStaff(staff);
        
        // ✅ FIX: Tạo mã vận đơn DÙNG ORDER_ID (không random, luôn duy nhất cho mỗi đơn)
        // Format: SHIP-ORDER{orderId}-{timestamp}
        long timestamp = System.currentTimeMillis() % 100000; // Lấy 5 chữ số cuối của timestamp
        shipment.setTrackingNumber(String.format("SHIP-ORD%d-%05d", orderId, timestamp));
        
        shipmentDAO.save(shipment);
        
        // ✅ FIX: KHÔNG thay đổi trạng thái đơn hàng - giữ PAID
        // order.setStatus("PAID"); // Đã được set trong PaymentService, không cần thay lại
        
        return shipment;
    }

    /**
     * Cập nhật trạng thái vận chuyển
     */
    public void updateShipmentStatus(Long shipmentId, String newStatus) {
        Hieu_Shipment shipment = shipmentDAO.findById(shipmentId);
        if (shipment == null) {
            throw new RuntimeException("Không tìm thấy vận đơn");
        }

        shipment.setStatus(newStatus);

        // Nếu trạng thái là SHIPPED, ghi lại thời gian gửi
        if ("SHIPPED".equals(newStatus)) {
            shipment.setShippedDate(LocalDateTime.now());
        }

        // Nếu trạng thái là DELIVERED, ghi lại thời gian giao
        if ("DELIVERED".equals(newStatus)) {
            shipment.setDeliveryDate(LocalDateTime.now());
            
            // Cập nhật trạng thái đơn hàng thành DELIVERED
            Hieu_Order order = shipment.getOrder();
            if (order != null) {
                order.setStatus("DELIVERED");
                orderDAO.update(order);
            }
        }

        shipmentDAO.update(shipment);
    }

    /**
     * Lấy tất cả vận đơn
     */
    public List<Hieu_Shipment> getAllShipments() {
        return shipmentDAO.findAll();
    }

    /**
     * Lấy vận đơn theo mã tracking
     */
    public Hieu_Shipment getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentDAO.findByTrackingNumber(trackingNumber);
    }

    /**
     * Lấy tất cả vận đơn của một đơn hàng
     */
    public List<Hieu_Shipment> getShipmentsByOrderId(Long orderId) {
        return shipmentDAO.findByOrderId(orderId);
    }

    /**
     * Lấy tất cả vận đơn với trạng thái nhất định
     */
    public List<Hieu_Shipment> getShipmentsByStatus(String status) {
        return shipmentDAO.findByStatus(status);
    }
}
