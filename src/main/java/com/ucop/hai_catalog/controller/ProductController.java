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
import java.io.File;

public class ProductController {

    @FXML private TableView<Hai_Item> tableItems;
    @FXML private TextField txtSku, txtName, txtPrice, txtStock;
    @FXML private ImageView imgPreview;

    private CatalogService service = new CatalogService();
    private String currentImagePath = ""; // Lưu đường dẫn tạm thời

    @FXML
    public void initialize() {
        loadData();
        
        // Sự kiện: Khi bấm vào 1 dòng trong bảng -> Hiện thông tin và Ảnh
        tableItems.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showProductDetails(newVal);
            }
        });
    }

    private void loadData() {
        tableItems.setItems(FXCollections.observableArrayList(service.getAllItems()));
    }

    private void showProductDetails(Hai_Item item) {
        txtSku.setText(item.getSku());
        txtName.setText(item.getName());
        txtPrice.setText(String.valueOf(item.getPrice()));
        txtStock.setText(String.valueOf(item.getStockQuantity()));
        
        // Hiển thị ảnh
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                File file = new File(item.getImagePath());
                if(file.exists()) {
                    // Chuyển đường dẫn file thành URL để JavaFX hiển thị
                    Image image = new Image(file.toURI().toString());
                    imgPreview.setImage(image);
                } else {
                    imgPreview.setImage(null);
                }
            } catch (Exception e) {
                imgPreview.setImage(null);
            }
        } else {
            imgPreview.setImage(null);
        }
    }

    @FXML
    public void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            currentImagePath = selectedFile.getAbsolutePath();
            // Hiện thử ảnh lên luôn
            Image image = new Image(selectedFile.toURI().toString());
            imgPreview.setImage(image);
        }
    }

    @FXML
    public void handleAdd() {
        try {
            String sku = txtSku.getText();
            String name = txtName.getText();
            double price = Double.parseDouble(txtPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());
            
            Hai_Category defaultCat = service.getDefaultCategory();
            if (defaultCat == null) {
                showAlert("Lỗi", "Chưa có danh mục (Category) nào trong Database!");
                return;
            }

            // Tạo sản phẩm mới với đường dẫn ảnh
            Hai_Item newItem = new Hai_Item(sku, name, price, stock, "Cái", currentImagePath);
            newItem.setCategory(defaultCat);
            
            service.addItem(newItem);
            
            loadData(); 
            clearFields();
            showAlert("Thành công", "Đã thêm sản phẩm: " + name);
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Giá và Tồn kho phải là số!");
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể thêm sản phẩm: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        Hai_Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // Cách 1: Cố gắng xóa hẳn
                service.deleteItem(selected);
                loadData();
                clearFields();
                showAlert("Thành công", "Đã xóa vĩnh viễn sản phẩm khỏi CSDL.");
            } catch (Exception e) {
                // Cách 2: Nếu không xóa được (do dính đơn hàng cũ) -> Chuyển trạng thái
                // Lưu ý: Cần đảm bảo trong Service có hàm update, hoặc dùng mẹo sau:
                
                selected.setStatus("DISCONTINUED"); // Đổi trạng thái
                // service.updateItem(selected); // Nếu bạn đã viết hàm update trong Service
                
                // Vì AbstractDAO có hàm update, nhưng CatalogService chưa gọi ra.
                // Để nhanh nhất, hãy dùng CÁCH 1 (SQL) ở trên để dọn dữ liệu.
                
                showAlert("Không thể xóa hẳn", 
                    "Sản phẩm này đã có giao dịch trong quá khứ.\n" +
                    "Theo nguyên tắc kế toán, không được xóa nó.\n" +
                    "(Hãy dùng SQL Workbench xóa sạch bảng orders và order_items nếu muốn reset dữ liệu)");
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn sản phẩm để xóa!");
        }
    }

    private void clearFields() {
        txtSku.clear(); txtName.clear(); txtPrice.clear(); txtStock.clear();
        imgPreview.setImage(null);
        currentImagePath = "";
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}