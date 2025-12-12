package com.ucop.hai_catalog.service;

import com.ucop.hai_catalog.*;
import com.ucop.hai_catalog.dao.*;
import com.ucop.core.dao.AbstractDAO;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.*; 
import java.nio.charset.StandardCharsets; 
import java.util.List;
import java.math.BigDecimal; // [MỚI] Thêm thư viện này để xử lý giá tiền

public class CatalogService {
    private ItemDAO itemDAO = new ItemDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private WarehouseDAO warehouseDAO = new WarehouseDAO();
    private StockItemDAO stockItemDAO = new StockItemDAO();

    // --- CRUD ITEM ---
    public List<Hai_Item> getAllItems() { return itemDAO.findAll(); }

    public void addItem(Hai_Item item, int initialStock) {
        itemDAO.save(item);
        Hai_Warehouse defaultWarehouse = getDefaultWarehouse();
        Hai_StockItem stock = new Hai_StockItem(defaultWarehouse, item, initialStock);
        stockItemDAO.save(stock);
    }
    
    public void updateItem(Hai_Item item) {
        itemDAO.update(item); 
        if (item.getStockItem() != null) {
            stockItemDAO.update(item.getStockItem());
        }
    }

    public void deleteItem(Hai_Item item) { itemDAO.delete(item.getId()); }
    
    // --- HỖ TRỢ KHO ---
    private Hai_Warehouse getDefaultWarehouse() {
        List<Hai_Warehouse> warehouses = warehouseDAO.findAll();
        if (warehouses.isEmpty()) {
            Hai_Warehouse main = new Hai_Warehouse("Kho Chính (Main)", "Hệ thống");
            warehouseDAO.save(main);
            return main;
        }
        return warehouses.get(0);
    }

    // --- CRUD CATEGORY ---
    public Hai_Category getDefaultCategory() {
        List<Hai_Category> list = categoryDAO.findAll();
        return list.isEmpty() ? null : list.get(0);
    }
    
    public List<Hai_Category> getAllCategories() { return categoryDAO.findAll(); }
    public void addCategory(Hai_Category category) { categoryDAO.save(category); }
    public void updateCategory(Hai_Category category) { categoryDAO.update(category); }
    public void deleteCategory(Long categoryId) { categoryDAO.delete(categoryId); }

    // --- CHỨC NĂNG IMPORT / EXPORT CSV (DÙNG DẤU PHẨY ',' CHUẨN QUỐC TẾ) ---

    public void exportProductsToCSV(String filePath) throws IOException {
        List<Hai_Item> items = getAllItems();
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Ghi BOM để Excel hiểu tiếng Việt
            fos.write(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF });

            try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 // Dùng constructor mặc định: Tự động dùng dấu phẩy (,) và bao ngoặc kép (")
                 CSVWriter writer = new CSVWriter(osw)) { 

                String[] header = { "SKU", "Tên Sản phẩm", "Giá", "Tồn kho", "Danh mục" };
                writer.writeNext(header);

                for (Hai_Item item : items) {
                    String[] data = {
                        item.getSku(),
                        item.getName(),
                        // Format giá tiền: bỏ phần thập phân .00 nếu tròn
                        item.getPrice() != null ? String.format("%.0f", item.getPrice()) : "0", 
                        String.valueOf(item.getStockQuantity()), 
                        item.getCategory() != null ? item.getCategory().getName() : ""
                    };
                    writer.writeNext(data);
                }
            }
        }
    }

    public void importProductsFromCSV(String filePath) throws Exception {
        // Lấy danh sách sản phẩm hiện có để kiểm tra trùng SKU (Tránh query DB nhiều lần)
        List<Hai_Item> existingItems = getAllItems();

        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             // Bỏ qua dòng Header
             CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {
             
            String[] line;
            while ((line = reader.readNext()) != null) {
                // Kiểm tra dòng lỗi (không đủ 4 cột bắt buộc)
                if (line.length < 4) continue; 

                // Đọc và làm sạch dữ liệu
                String sku = line[0].trim();
                String name = line[1].trim();
                
                double price = 0;
                try { 
                    // Xử lý giá: thay dấu phẩy thành chấm (nếu người dùng nhập kiểu Việt Nam 10,5)
                    price = Double.parseDouble(line[2].trim().replace(",", ".")); 
                } catch (Exception e) {}

                int stock = 0;
                try { stock = Integer.parseInt(line[3].trim()); } catch (Exception e) {}
                
                // --- LOGIC: KIỂM TRA TỒN TẠI (UPSERT) ---
                
                Hai_Item foundItem = null;
                // Tìm trong danh sách hiện có
                for (Hai_Item item : existingItems) {
                    if (item.getSku().equalsIgnoreCase(sku)) {
                        foundItem = item;
                        break;
                    }
                }

                if (foundItem != null) {
                    // === TRƯỜNG HỢP 1: ĐÃ CÓ -> CẬP NHẬT (UPDATE) ===
                    foundItem.setName(name);
                    foundItem.setPrice(BigDecimal.valueOf(price));
                    
                    // Cập nhật tồn kho (nếu có thông tin kho)
                    if (foundItem.getStockItem() != null) {
                        foundItem.getStockItem().setOnHand(stock);
                    }
                    
                    try {
                        updateItem(foundItem);
                        System.out.println("Đã cập nhật: " + sku);
                    } catch (Exception e) {
                        System.err.println("Lỗi cập nhật " + sku + ": " + e.getMessage());
                    }

                } else {
                    // === TRƯỜNG HỢP 2: CHƯA CÓ -> THÊM MỚI (INSERT) ===
                    Hai_Item newItem = new Hai_Item(sku, name, price, "Cái", "");
                    newItem.setCategory(getDefaultCategory());

                    try {
                        addItem(newItem, stock);
                        // Thêm vào list tạm để các dòng sau trong cùng file check được trùng
                        existingItems.add(newItem); 
                        System.out.println("Đã thêm mới: " + sku);
                    } catch (Exception e) {
                        System.err.println("Lỗi thêm mới " + sku + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}