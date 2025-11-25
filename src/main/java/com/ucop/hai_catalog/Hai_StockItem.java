package com.ucop.hai_catalog;

import javax.persistence.*;

@Entity
@Table(name = "stock_items")
public class Hai_StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tồn kho thực tế (OnHand)
    @Column(name = "on_hand")
    private int onHand; 
    
    // Tồn kho đã đặt nhưng chưa ship (Reserved) - Theo yêu cầu đề bài
    private int reserved; 

    // Quan hệ N-1: Thuộc về kho nào
    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Hai_Warehouse warehouse;

    // Quan hệ 1-1: Là tồn kho của sản phẩm nào
    // Dùng @OneToOne và JoinColumn trên cột item_id để liên kết
    @OneToOne
    @JoinColumn(name = "item_id", unique = true, nullable = false)
    private Hai_Item item;

    public Hai_StockItem() {}
    
    public Hai_StockItem(Hai_Warehouse warehouse, Hai_Item item, int onHand) {
        this.warehouse = warehouse;
        this.item = item;
        this.onHand = onHand;
        this.reserved = 0;
    }

    // Tính tồn kho khả dụng
    public int getAvailableStock() {
        return onHand - reserved;
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getOnHand() { return onHand; }
    public void setOnHand(int onHand) { this.onHand = onHand; }
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    public Hai_Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Hai_Warehouse warehouse) { this.warehouse = warehouse; }
    public Hai_Item getItem() { return item; }
    public void setItem(Hai_Item item) { this.item = item; }
}