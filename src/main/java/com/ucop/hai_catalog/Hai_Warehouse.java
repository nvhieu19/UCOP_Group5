package com.ucop.hai_catalog;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouses")
public class Hai_Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long id;

    @Column(nullable = false)
    private String name; // Ví dụ: Kho hàng chính, Kho chi nhánh Sài Gòn
    
    private String address;

    // Quan hệ 1-N: Một kho có nhiều mục tồn kho (StockItem)
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Hai_StockItem> stockItems = new ArrayList<>();

    public Hai_Warehouse() {}
    
    public Hai_Warehouse(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<Hai_StockItem> getStockItems() { return stockItems; }
    public void setStockItems(List<Hai_StockItem> stockItems) { this.stockItems = stockItems; }
}