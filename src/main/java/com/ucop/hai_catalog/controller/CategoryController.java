package com.ucop.hai_catalog.controller;

import com.ucop.hai_catalog.Hai_Category;
import com.ucop.hai_catalog.service.CatalogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
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
        
        // Cột ID
        TableColumn<Hai_Category, Long> colId = (TableColumn<Hai_Category, Long>) tableCategory.getColumns().get(0);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Cột Tên
        TableColumn<Hai_Category, String> colName = (TableColumn<Hai_Category, String>) tableCategory.getColumns().get(1);
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Cột Mô tả
        TableColumn<Hai_Category, String> colDesc = (TableColumn<Hai_Category, String>) tableCategory.getColumns().get(2);
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Cột Danh mục cha (Đã sửa lỗi hiển thị dòng trống)
        TableColumn<Hai_Category, Hai_Category> colParent = (TableColumn<Hai_Category, Hai_Category>) tableCategory.getColumns().get(3);
        colParent.setCellValueFactory(new PropertyValueFactory<>("parent"));
        
        // --- [FIX] SỬA LỖI HIỂN THỊ "— (Gốc)" Ở DÒNG TRỐNG ---
        colParent.setCellFactory(tc -> new TableCell<Hai_Category, Hai_Category>() {
            @Override
            protected void updateItem(Hai_Category parent, boolean empty) {
                super.updateItem(parent, empty);
                if (empty) {
                    // Nếu dòng trống -> Không hiện gì cả
                    setText(null);
                } else {
                    // Nếu có dữ liệu -> Hiện tên hoặc "Gốc" nếu null
                    setText(parent == null ? "— (Gốc)" : parent.getName());
                }
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
        // Thêm một option null đại diện cho "Danh mục gốc" vào đầu danh sách
        parentOptions.add(0, null); 
        cbParent.setItems(parentOptions);
        
        // Converter để hiển thị tên danh mục trong ComboBox thay vì mã object
        cbParent.setConverter(new StringConverter<Hai_Category>() {
            @Override
            public String toString(Hai_Category category) {
                return (category == null) ? "— (Danh mục gốc)" : category.getName();
            }

            @Override
            public Hai_Category fromString(String string) {
                return null;
            }
        });
        
        // Mặc định chọn Gốc
        cbParent.getSelectionModel().selectFirst();
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
                
                // [Tùy chọn] Tắt nút xóa nếu đang chọn cập nhật (để an toàn), hoặc giữ nguyên
                btnDelete.setDisable(false);
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
                // --- THÊM MỚI (CREATE) ---
                Hai_Category newCat = new Hai_Category();
                newCat.setName(txtName.getText());
                newCat.setDescription(txtDescription.getText());
                newCat.setParent(cbParent.getValue()); // Gán danh mục cha
                
                service.addCategory(newCat);
                showAlert("Thành công", "Đã thêm danh mục mới: " + newCat.getName());
            } else {
                // --- CẬP NHẬT (UPDATE) ---
                Hai_Category selectedParent = cbParent.getValue();
                
                // Kiểm tra logic: Không được chọn chính nó làm cha
                if (selectedParent != null && selectedParent.getId().equals(selectedCategory.getId())) {
                    showAlert("Lỗi", "Danh mục không thể là cha của chính nó!");
                    return;
                }
                
                // Kiểm tra logic: Không được chọn con của chính mình làm cha (tránh vòng lặp)
                // (Logic này phức tạp hơn, tạm thời chỉ check chính nó)

                selectedCategory.setName(txtName.getText());
                selectedCategory.setDescription(txtDescription.getText());
                selectedCategory.setParent(selectedParent);
                
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
            // Kiểm tra ràng buộc trước khi xóa
            if (selectedCategory.getSubCategories() != null && !selectedCategory.getSubCategories().isEmpty()) {
                showAlert("Cảnh báo", "Không thể xóa: Danh mục này đang chứa danh mục con!");
                return;
            }
            if (selectedCategory.getItems() != null && !selectedCategory.getItems().isEmpty()) {
                 showAlert("Cảnh báo", "Không thể xóa: Danh mục này đang chứa sản phẩm!");
                return;
            }

            // Hộp thoại xác nhận xóa
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa danh mục: " + selectedCategory.getName() + " ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                service.deleteCategory(selectedCategory.getId());
                showAlert("Thành công", "Đã xóa danh mục: " + selectedCategory.getName());
                loadData();
                clearFields();
            }
            
        } catch (Exception e) {
            showAlert("Lỗi", "Lỗi xóa Danh mục: " + e.getMessage());
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
        cbParent.getSelectionModel().selectFirst(); // Về mặc định là Gốc
        btnSave.setText("Thêm Mới");
        tableCategory.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}