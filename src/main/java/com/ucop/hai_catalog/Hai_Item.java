package com.ucop.hai_catalog;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Hai_Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku; // Mã vạch

    @Column(nullable = false)
    private String name;

    private BigDecimal price; // Giá tiền

    @Column(name = "stock_quantity")
    private int stockQuantity; // Tồn kho

    private String unit; // Đơn vị tính
    private String status; // ACTIVE, DISCONTINUED
    
    // --- CỘT MỚI: ĐƯỜNG DẪN ẢNH ---
    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Hai_Category category;

    public Hai_Item() {}

    // Constructor cập nhật thêm imagePath
    public Hai_Item(String sku, String name, double price, int stock, String unit, String imagePath) {
        this.sku = sku;
        this.name = name;
        this.price = BigDecimal.valueOf(price);
        this.stockQuantity = stock;
        this.unit = unit;
        this.status = "ACTIVE";
        this.imagePath = imagePath;
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Hai_Category getCategory() { return category; }
    public void setCategory(Hai_Category category) { this.category = category; }
    
    // Hàm hỗ trợ hiển thị tên trong ComboBox (Module Bán hàng dùng)
    @Override
    public String toString() {
        return name + " (" + price + ")";
    }
}