package com.ucop.hieu_order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ucop.dinh_admin.Dinh_User;

@Entity
@Table(name = "orders")
public class Hieu_Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    // --- CÁC TRƯỜNG TÍNH TOÁN TIỀN (Requirement 3.6) ---
    // Lưu chi tiết để phục vụ báo cáo và hiển thị hóa đơn
    private BigDecimal subTotal;      // Tổng tiền hàng (chưa thuế/phí)
    private BigDecimal taxAmount;     // Thuế VAT
    private BigDecimal shippingFee;   // Phí vận chuyển
    private BigDecimal discountAmount;// Số tiền được giảm giá
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;   // Tổng thanh toán cuối cùng (Grand Total)

    private String promotionCode;     // Mã giảm giá đã áp dụng

    // --- TRẠNG THÁI ĐƠN HÀNG (Requirement 3.5) ---
    // Quy trình: CART -> PLACED -> PENDING_PAYMENT -> PAID -> PACKED -> SHIPPED -> DELIVERED -> CLOSED
    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Dinh_User customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Hieu_OrderItem> orderItems = new ArrayList<>();

    public Hieu_Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "CART"; // Mặc định khi mới tạo là Giỏ hàng
        // Khởi tạo giá trị 0 để tránh lỗi NullPointerException khi tính toán
        this.subTotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingFee = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addOrderItem(Hieu_OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // Getters & Setters (Bạn hãy generate đầy đủ trong Eclipse nhé)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Dinh_User getCustomer() { return customer; }
    public void setCustomer(Dinh_User customer) { this.customer = customer; }
    public List<Hieu_OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<Hieu_OrderItem> orderItems) { this.orderItems = orderItems; }
}