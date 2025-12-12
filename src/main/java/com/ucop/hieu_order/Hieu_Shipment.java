package com.ucop.hieu_order;

import javax.persistence.*;
import java.time.LocalDateTime;
import com.ucop.dinh_admin.Dinh_User;

@Entity
@Table(name = "shipments")
public class Hieu_Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Hieu_Order order;

    @Column(name = "tracking_number", unique = true)
    private String trackingNumber; // Mã vận đơn

    @Column(name = "shipping_method")
    private String shippingMethod; // VD: Standard, Express, Overnight

    private String status; // PREPARING, SHIPPED, IN_TRANSIT, DELIVERED, FAILED, RETURNED

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    private String address; // Địa chỉ giao

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Dinh_User staff; // Staff xử lý vận chuyển

    public Hieu_Shipment() {
        this.createdAt = LocalDateTime.now();
        this.status = "PREPARING";
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Hieu_Order getOrder() { return order; }
    public void setOrder(Hieu_Order order) { this.order = order; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getShippedDate() { return shippedDate; }
    public void setShippedDate(LocalDateTime shippedDate) { this.shippedDate = shippedDate; }
    
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Dinh_User getStaff() { return staff; }
    public void setStaff(Dinh_User staff) { this.staff = staff; }

    @Override
    public String toString() {
        return "Shipment #" + trackingNumber + " (" + status + ")";
    }
}
