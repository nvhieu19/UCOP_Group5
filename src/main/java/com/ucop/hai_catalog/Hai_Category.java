package com.ucop.hai_catalog;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Hai_Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Quan hệ đệ quy: Danh mục cha (VD: Laptop có cha là Điện tử)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Hai_Category parent;

    // Quan hệ 1-N: Danh sách con
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Hai_Category> subCategories = new ArrayList<>();

    // Quan hệ 1-N: Một danh mục có nhiều sản phẩm
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Hai_Item> items = new ArrayList<>();

    public Hai_Category() {}

    public Hai_Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Hai_Category getParent() { return parent; }
    public void setParent(Hai_Category parent) { this.parent = parent; }
    public List<Hai_Category> getSubCategories() { return subCategories; }
    public void setSubCategories(List<Hai_Category> subCategories) { this.subCategories = subCategories; }
    public List<Hai_Item> getItems() { return items; }
    public void setItems(List<Hai_Item> items) { this.items = items; }
}