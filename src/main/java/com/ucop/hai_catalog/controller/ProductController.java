package com.ucop.hai_catalog.controller;

import com.ucop.hai_catalog.Hai_Category;
import com.ucop.hai_catalog.Hai_Item;
import com.ucop.hai_catalog.service.CatalogService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.math.BigDecimal;

public class ProductController {

    @FXML private TableView<Hai_Item> tableItems;
    @FXML private TableColumn<Hai_Item, String> colCategory;
    
    @FXML private TextField txtSku, txtName, txtPrice, txtStock;
    @FXML private ComboBox<Hai_Category> cbCategory; 
    @FXML private ImageView imgPreview;

    private CatalogService service = new CatalogService();
    private String currentImagePath = ""; 
    private Hai_Item selectedItem = null; 

    @FXML
    public void initialize() {
        // 1. Load dữ liệu
        loadData();
        loadCategoriesToComboBox();

        // 2. Cấu hình cột Danh mục
        if (colCategory != null) {
            colCategory.setCellValueFactory(cellData -> {
                Hai_Category cat = cellData.getValue().getCategory();
                return new SimpleStringProperty(cat != null ? cat.getName() : "Chưa phân loại");
            });
        }

        // --- [MỚI] CẢNH BÁO LOW STOCK (TỒN KHO THẤP) ---
        // Tô màu nền đỏ nhạt cho các dòng có Tồn kho <= 5
        tableItems.setRowFactory(tv -> new TableRow<Hai_Item>() {
            @Override
            protected void updateItem(Hai_Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Kiểm tra số lượng tồn kho
                    if (item.getStockQuantity() <= 5) {
                        setStyle("-fx-background-color: #ffcccc;"); // Màu đỏ nhạt cảnh báo
                    } else {
                        setStyle(""); // Trả về màu mặc định nếu đủ hàng
                    }
                }
            }
        });
        // ------------------------------------------------

        // 3. Sự kiện chọn dòng
        tableItems.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedItem = newVal;
                showProductDetails(newVal);
            } else {
                selectedItem = null;
                clearFields();
            }
        });
    }

    private void loadData() {
        tableItems.setItems(FXCollections.observableArrayList(service.getAllItems()));
    }
    
    private void loadCategoriesToComboBox() {
        cbCategory.setItems(FXCollections.observableArrayList(service.getAllCategories()));
        cbCategory.setConverter(new StringConverter<Hai_Category>() {
            @Override
            public String toString(Hai_Category category) {
                return category == null ? "" : category.getName();
            }
            @Override
            public Hai_Category fromString(String string) { return null; }
        });
    }

    private void showProductDetails(Hai_Item item) {
        txtSku.setText(item.getSku());
        txtName.setText(item.getName());
        txtPrice.setText(String.valueOf(item.getPrice()));
        txtStock.setText(String.valueOf(item.getStockQuantity()));
        cbCategory.setValue(item.getCategory());
        
        currentImagePath = item.getImagePath() != null ? item.getImagePath() : "";
        if (!currentImagePath.isEmpty()) { 
            try {
                File file = new File(currentImagePath);
                if(file.exists()) {
                    imgPreview.setImage(new Image(file.toURI().toString()));
                } else { imgPreview.setImage(null); }
            } catch (Exception e) { imgPreview.setImage(null); }
        } else { imgPreview.setImage(null); }
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            currentImagePath = selectedFile.getAbsolutePath();
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    public void handleAdd() {
        try {
            String sku = txtSku.getText();
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            
            Hai_Category selectedCat = cbCategory.getValue();
            if (selectedCat == null) {
                showAlert("Lỗi", "Vui lòng chọn Danh mục cho sản phẩm!");
                return;
            }

            Hai_Item newItem = new Hai_Item(sku, name, price, "Cái", currentImagePath);
            newItem.setCategory(selectedCat);
            
            service.addItem(newItem, stock);
            
            loadData(); 
            clearFields();
            showAlert("Thành công", "Đã thêm sản phẩm: " + name);
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Giá và Tồn kho phải là số!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể thêm: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdate() {
        if (selectedItem == null) {
            showAlert("Cảnh báo", "Vui lòng chọn sản phẩm cần sửa!");
            return;
        }
        try {
            Hai_Category selectedCat = cbCategory.getValue();
            if (selectedCat == null) {
                showAlert("Lỗi", "Vui lòng chọn Danh mục!");
                return;
            }

            String sku = txtSku.getText();
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            
            selectedItem.setSku(sku);
            selectedItem.setName(name);
            selectedItem.setPrice(BigDecimal.valueOf(price));
            selectedItem.setCategory(selectedCat);
            selectedItem.setImagePath(currentImagePath);
            
            if (selectedItem.getStockItem() != null) {
                selectedItem.getStockItem().setOnHand(stock);
            }
            
            service.updateItem(selectedItem);
            loadData(); 
            clearFields();
            showAlert("Thành công", "Đã cập nhật sản phẩm!");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleDelete() {
        Hai_Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.deleteItem(selected);
                loadData();
                clearFields();
                showAlert("Thành công", "Đã xóa sản phẩm.");
            } catch (Exception e) {
                selected.setStatus("DISCONTINUED"); 
                service.updateItem(selected);
                loadData();
                showAlert("Thông báo", "Đã chuyển trạng thái sang NGỪNG KINH DOANH (do dính đơn hàng cũ).");
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn sản phẩm để xóa!");
        }
    }
    
    // --- Export/Import CSV (Khôi phục đầy đủ logic) ---
    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file danh sách sản phẩm");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("san_pham_export.csv");
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                service.exportProductsToCSV(file.getAbsolutePath());
                showAlert("Thành công", "Đã xuất dữ liệu ra file: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể xuất file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV để nhập liệu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                service.importProductsFromCSV(file.getAbsolutePath());
                loadData();
                showAlert("Thành công", "Đã nhập dữ liệu từ file!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Lỗi nhập file: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClear() {
        clearFields();
    }
    
    private void clearFields() {
        selectedItem = null;
        txtSku.clear(); txtName.clear(); txtPrice.clear(); txtStock.clear();
        imgPreview.setImage(null);
        currentImagePath = "";
        cbCategory.setValue(null);
        cbCategory.setPromptText("Chọn danh mục...");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}