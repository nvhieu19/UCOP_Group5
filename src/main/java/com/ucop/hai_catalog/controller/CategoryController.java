package com.ucop.hai_catalog.controller;

import com.ucop.hai_catalog.Hai_Category;
import com.ucop.hai_catalog.service.CatalogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class CategoryController {

    @FXML private TableView<Hai_Category> tableCategory;
    @FXML private TextField txtName, txtDescription;
    @FXML private ComboBox<Hai_Category> cbParent;
    @FXML private Button btnSave, btnDelete;
    
    private CatalogService service = new CatalogService();
    private Hai_Category selectedCategory = null;

    @FXML
    public void initialize() {
        // 1. Cấu hình các cột (TableView)
        // Cần đảm bảo cột tên, mô tả, và cột cha hiển thị đúng.
        // Cột ID
        ((TableColumn<Hai_Category, Long>) tableCategory.getColumns().get(0)).setCellValueFactory(new PropertyValueFactory<>("id"));
        // Cột Tên
        ((TableColumn<Hai_Category, String>) tableCategory.getColumns().get(1)).setCellValueFactory(new PropertyValueFactory<>("name"));
        // Cột Mô tả
        ((TableColumn<Hai_Category, String>) tableCategory.getColumns().get(2)).setCellValueFactory(new PropertyValueFactory<>("description"));
        // Cột Danh mục cha (Sử dụng Custom Cell để hiển thị Tên Cha thay vì đối tượng)
        TableColumn<Hai_Category, Hai_Category> colParent = (TableColumn<Hai_Category, Hai_Category>) tableCategory.getColumns().get(3);
        colParent.setCellValueFactory(new PropertyValueFactory<>("parent"));
        colParent.setCellFactory(tc -> new TableCell<Hai_Category, Hai_Category>() {
            @Override
            protected void updateItem(Hai_Category parent, boolean empty) {
                super.updateItem(parent, empty);
                setText(empty || parent == null ? "— (Gốc)" : parent.getName());
            }
        });
        
        loadData();
        setupListeners();
    }

    private void loadData() {
        // Tải danh mục và hiển thị lên TableView
        List<Hai_Category> categories = service.getAllCategories();
        tableCategory.setItems(FXCollections.observableArrayList(categories));
        
        // Tải danh mục cha vào ComboBox
        ObservableList<Hai_Category> parentOptions = FXCollections.observableArrayList(categories);
        // Thêm một option null/trống đại diện cho "Danh mục gốc"
        parentOptions.add(0, null); 
        cbParent.setItems(parentOptions);
    }
    
    private void setupListeners() {
        // Lắng nghe sự kiện chọn dòng trong bảng
        tableCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedCategory = newVal;
                txtName.setText(newVal.getName());
                txtDescription.setText(newVal.getDescription());
                cbParent.setValue(newVal.getParent()); // Gán danh mục cha
                btnSave.setText("Cập Nhật"); // Đổi nút thành Cập Nhật
            } else {
                clearFields();
            }
        });
    }

    @FXML
    public void handleSave() {
        if (txtName.getText().isEmpty()) {
            showAlert("Lỗi", "Tên danh mục không được để trống!");
            return;
        }

        try {
            if (selectedCategory == null) {
                // THÊM MỚI (CREATE)
                Hai_Category newCat = new Hai_Category();
                newCat.setName(txtName.getText());
                newCat.setDescription(txtDescription.getText());
                newCat.setParent(cbParent.getValue()); // Gán danh mục cha
                
                service.addCategory(newCat);
                showAlert("Thành công", "Đã thêm danh mục mới: " + newCat.getName());
            } else {
                // CẬP NHẬT (UPDATE)
                selectedCategory.setName(txtName.getText());
                selectedCategory.setDescription(txtDescription.getText());
                selectedCategory.setParent(cbParent.getValue());
                
                service.updateCategory(selectedCategory);
                showAlert("Thành công", "Đã cập nhật danh mục: " + selectedCategory.getName());
            }
            
            loadData();
            clearFields();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi khi lưu/cập nhật danh mục: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedCategory == null) {
            showAlert("Cảnh báo", "Vui lòng chọn danh mục để xóa!");
            return;
        }
        
        try {
            // Cần kiểm tra Danh mục có Category con hoặc Item con không trước khi xóa
            if (selectedCategory.getSubCategories() != null && !selectedCategory.getSubCategories().isEmpty()) {
                showAlert("Cảnh báo", "Không thể xóa: Danh mục này còn danh mục con!");
                return;
            }
            if (selectedCategory.getItems() != null && !selectedCategory.getItems().isEmpty()) {
                 showAlert("Cảnh báo", "Không thể xóa: Danh mục này còn sản phẩm!");
                return;
            }

            service.deleteCategory(selectedCategory.getId());
            showAlert("Thành công", "Đã xóa danh mục: " + selectedCategory.getName());
            loadData();
            clearFields();
            
        } catch (Exception e) {
            showAlert("Lỗi", "Lỗi xóa Danh mục: " + e.getMessage() + " (Thường do ràng buộc dữ liệu)");
        }
    }
    
    @FXML
    public void handleClear() {
        clearFields();
    }
    
    private void clearFields() {
        selectedCategory = null;
        txtName.clear();
        txtDescription.clear();
        cbParent.getSelectionModel().select(0); // Chọn lại Danh mục gốc
        btnSave.setText("Thêm Mới"); // Đổi nút thành Thêm Mới
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}