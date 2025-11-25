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

    private String unit; // Đơn vị tính
    private String status; // ACTIVE, DISCONTINUED
    
    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Hai_Category category;
    
    // [MỚI] Liên kết 2 chiều đến StockItem để biết tồn kho
    // mappedBy = "item" nghĩa là phía StockItem đang nắm giữ khóa ngoại
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Hai_StockItem stockItem;

    public Hai_Item() {}

    public Hai_Item(String sku, String name, double price, String unit, String imagePath) {
        this.sku = sku;
        this.name = name;
        this.price = BigDecimal.valueOf(price);
        this.unit = unit;
        this.status = "ACTIVE";
        this.imagePath = imagePath;
    }

    // --- LOGIC LẤY TỒN KHO ẢO ĐỂ HIỂN THỊ LÊN BẢNG ---
    // (Giúp code cũ của ProductController không bị lỗi đỏ lòm khi gọi getStockQuantity)
    public int getStockQuantity() {
        if (stockItem != null) {
            return stockItem.getOnHand(); // Trả về số lượng thực tế trong kho
        }
        return 0;
    }
    
    // Hàm này chỉ để tương thích, thực tế nên setStockItem
    public void setStockQuantity(int qty) {
        // Không làm gì hoặc cập nhật vào stockItem nếu cần thiết
    }

    // Getters Setters cơ bản
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Hai_Category getCategory() { return category; }
    public void setCategory(Hai_Category category) { this.category = category; }
    
    // Getter Setter cho StockItem
    public Hai_StockItem getStockItem() { return stockItem; }
    public void setStockItem(Hai_StockItem stockItem) { this.stockItem = stockItem; }

    @Override
    public String toString() {
        return name + " (" + price + ")";
    }
}