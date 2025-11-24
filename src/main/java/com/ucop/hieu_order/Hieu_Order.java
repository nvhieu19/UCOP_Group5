package com.ucop.hieu_order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ucop.dinh_admin.Dinh_User; // Import User của Định

@Entity
@Table(name = "orders")
public class Hieu_Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // Trạng thái: PLACED, PENDING, PAID, CANCELED
    private String status;

    // Quan hệ N-1: Đơn hàng thuộc về 1 khách hàng (User của Định)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Dinh_User customer;

    // Quan hệ 1-N: Đơn hàng có nhiều dòng sản phẩm (Item của Hiếu)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Hieu_OrderItem> orderItems = new ArrayList<>();

    public Hieu_Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "PLACED"; // Mặc định là Mới đặt
        this.totalAmount = BigDecimal.ZERO;
    }

    // Hàm thêm sản phẩm vào đơn
    public void addOrderItem(Hieu_OrderItem item) {
        orderItems.add(item);
        item.setOrder(this); // Gán ngược lại để Hibernate hiểu quan hệ 2 chiều
    }

    // Hàm tính tổng tiền tự động
    public void calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Hieu_OrderItem item : orderItems) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
        }
        this.totalAmount = total;
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Dinh_User getCustomer() { return customer; }
    public void setCustomer(Dinh_User customer) { this.customer = customer; }
    public List<Hieu_OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<Hieu_OrderItem> orderItems) { this.orderItems = orderItems; }
}