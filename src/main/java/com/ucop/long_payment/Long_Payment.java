package com.ucop.long_payment;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.ucop.hieu_order.Hieu_Order;

@Entity
@Table(name = "payments")
public class Long_Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    private BigDecimal amount; // Số tiền thanh toán

    @Column(name = "payment_method")
    private String paymentMethod; // WALLET, COD, BANK_TRANSFER

    private String status; // SUCCESS, FAILED

    // Quan hệ N-1: Thanh toán cho đơn hàng nào (của Hiếu)
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Hieu_Order order;

    public Long_Payment() {
        this.paymentDate = LocalDateTime.now();
    }

    public Long_Payment(Hieu_Order order, String method, BigDecimal amount) {
        this.order = order;
        this.paymentMethod = method;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Hieu_Order getOrder() { return order; }
    public void setOrder(Hieu_Order order) { this.order = order; }
}