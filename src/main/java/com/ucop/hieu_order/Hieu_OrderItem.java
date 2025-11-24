package com.ucop.hieu_order;

import javax.persistence.*;
import java.math.BigDecimal;
import com.ucop.hai_catalog.Hai_Item; // Import Item của Hải

@Entity
@Table(name = "order_items")
public class Hieu_OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity; // Số lượng mua

    private BigDecimal price; // Giá bán tại thời điểm mua (Snapshot)

    // Quan hệ N-1: Thuộc về đơn hàng nào
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Hieu_Order order;

    // Quan hệ N-1: Là sản phẩm nào (của Hải)
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Hai_Item item;

    public Hieu_OrderItem() {}

    public Hieu_OrderItem(Hai_Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.price = item.getPrice(); // Lấy giá hiện tại của sản phẩm gán vào đây
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Hieu_Order getOrder() { return order; }
    public void setOrder(Hieu_Order order) { this.order = order; }
    public Hai_Item getItem() { return item; }
    public void setItem(Hai_Item item) { this.item = item; }
}