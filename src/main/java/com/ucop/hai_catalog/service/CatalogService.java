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

    // --- CHỨC NĂNG IMPORT / EXPORT CSV (DÙNG DẤU CHẤM PHẨY ';') ---

    public void exportProductsToCSV(String filePath) throws IOException {
        List<Hai_Item> items = getAllItems();
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            // Ghi BOM để Excel hiểu tiếng Việt
            fos.write(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF });

            try (OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 // [SỬA LẦN CUỐI] Dùng dấu chấm phẩy (;) - Chuẩn Excel Việt Nam
                 CSVWriter writer = new CSVWriter(osw, ';', 
                                                  CSVWriter.NO_QUOTE_CHARACTER, 
                                                  CSVWriter.DEFAULT_ESCAPE_CHARACTER, 
                                                  CSVWriter.DEFAULT_LINE_END)) {

                String[] header = { "SKU", "Tên Sản phẩm", "Giá", "Tồn kho", "Danh mục" };
                writer.writeNext(header);

                for (Hai_Item item : items) {
                    String[] data = {
                        item.getSku(),
                        item.getName(),
                        item.getPrice().toBigInteger().toString(), 
                        String.valueOf(item.getStockQuantity()), 
                        item.getCategory() != null ? item.getCategory().getName() : ""
                    };
                    writer.writeNext(data);
                }
            }
        }
    }

    public void importProductsFromCSV(String filePath) throws Exception {
        // [SỬA LẦN CUỐI] Cấu hình Parser để hiểu dấu chấm phẩy (;)
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();

        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             CSVReader reader = new CSVReaderBuilder(isr).withCSVParser(parser).build()) {
             
            String[] line;
            reader.readNext(); // Bỏ qua Header

            while ((line = reader.readNext()) != null) {
                if (line.length < 4) continue; 

                String sku = line[0];
                String name = line[1];
                
                double price = 0;
                try { price = Double.parseDouble(line[2].trim()); } catch (Exception e) {}

                int stock = 0;
                try { stock = Integer.parseInt(line[3].trim()); } catch (Exception e) {}
                
                Hai_Item newItem = new Hai_Item(sku, name, price, "Cái", "");
                newItem.setCategory(getDefaultCategory());

                try {
                    addItem(newItem, stock);
                } catch (Exception e) {
                    System.err.println("Lỗi nhập dòng SKU " + sku + ": " + e.getMessage());
                }
            }
        }
    }
}