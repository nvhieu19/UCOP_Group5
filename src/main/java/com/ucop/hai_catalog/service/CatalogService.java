package com.ucop.hai_catalog.service;

import com.ucop.hai_catalog.Hai_Category;
import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hai_catalog.dao.ItemDAO;
import com.ucop.core.dao.AbstractDAO;
import java.util.List;

public class CatalogService {
    private ItemDAO itemDAO = new ItemDAO();
    // Tạo tạm DAO cho Category để lấy dữ liệu mẫu (nếu cần)
    private AbstractDAO<Hai_Category, Long> categoryDAO = new AbstractDAO<Hai_Category, Long>() {};

    public List<Hai_Item> getAllItems() {
        return itemDAO.findAll();
    }

    public void addItem(Hai_Item item) {
        // Có thể thêm logic kiểm tra trùng SKU ở đây
        itemDAO.save(item);
    }

    public void deleteItem(Hai_Item item) {
        itemDAO.delete(item.getId());
    }
    
    // Hàm hỗ trợ lấy Category đầu tiên để gán cho sản phẩm mới (Làm tắt cho nhanh)
    public Hai_Category getDefaultCategory() {
        List<Hai_Category> list = categoryDAO.findAll();
        return list.isEmpty() ? null : list.get(0);
    }
}