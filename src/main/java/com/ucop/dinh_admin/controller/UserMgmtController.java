package com.ucop.dinh_admin.controller;

import com.ucop.dinh_admin.Dinh_Role;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.service.UserService; // Dùng Service thay vì DAO
import com.ucop.dinh_admin.service.SessionManager;
import com.ucop.core.dao.AbstractDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory; // Import thêm cái này để map cột

import java.util.List;

public class UserMgmtController {

    @FXML private TableView<Dinh_User> tableUsers;
    // Khai báo các cột cho bảng (Bạn nhớ đặt fx:id trong FXML tương ứng nhé)
    @FXML private TableColumn<Dinh_User, Long> colId;
    @FXML private TableColumn<Dinh_User, String> colUsername;
    @FXML private TableColumn<Dinh_User, String> colStatus;
    @FXML private TableColumn<Dinh_User, String> colRole; // Cột hiển thị Role

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private ComboBox<String> cbRole;

    // SỬ DỤNG SERVICE ĐỂ CÓ AUDIT LOG
    private UserService userService = new UserService();
    private AbstractDAO<Dinh_Role, Long> roleDAO = new AbstractDAO<Dinh_Role, Long>() {};

    @FXML
    public void initialize() {
        setupTableColumns(); // Map dữ liệu vào cột
        loadData();
        
        // Load danh sách Role vào ComboBox
        List<Dinh_Role> roles = roleDAO.findAll();
        for (Dinh_Role r : roles) {
            cbRole.getItems().add(r.getRoleName());
        }
    }

    // Hàm mapping cột (Quan trọng: ko có hàm này bảng sẽ trắng trơn)
    private void setupTableColumns() {
        if(colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if(colUsername != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        if(colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Hiển thị Role (Hơi phức tạp vì nó là List, lấy cái đầu tiên đại diện)
        if(colRole != null) {
            colRole.setCellFactory(column -> new TableCell<Dinh_User, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        Dinh_User user = getTableView().getItems().get(getIndex());
                        if (!user.getRoles().isEmpty()) {
                            setText(user.getRoles().iterator().next().getRoleName());
                        } else {
                            setText("N/A");
                        }
                    }
                }
            });
        }
    }

    private void loadData() {
        // Gọi qua Service
        tableUsers.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
    }

    @FXML
    public void handleAddUser() {
        try {
            String u = txtUser.getText();
            String p = txtPass.getText();
            String roleName = cbRole.getValue();

            if (u.isEmpty() || p.isEmpty() || roleName == null) {
                showAlert("Lỗi", "Vui lòng nhập đủ thông tin!");
                return;
            }

            Dinh_User newUser = new Dinh_User(u, p, "ACTIVE");
            
            // Gán Role
            List<Dinh_Role> roles = roleDAO.findAll();
            for(Dinh_Role r : roles) {
                if(r.getRoleName().equals(roleName)) {
                    newUser.addRole(r);
                    break;
                }
            }
            
            // Lấy tên Admin đang đăng nhập để ghi Log
            String currentAdmin = SessionManager.getInstance().getCurrentUser().getUsername();

            // GỌI SERVICE (Tự động ghi Audit Log)
            boolean success = userService.register(newUser, currentAdmin);
            
            if (success) {
                loadData();
                txtUser.clear(); txtPass.clear();
                showAlert("Thành công", "Đã thêm user: " + u);
            } else {
                showAlert("Lỗi", "Tên đăng nhập đã tồn tại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Lỗi hệ thống: " + e.getMessage());
        }
    }

    @FXML
    public void handleToggleStatus() {
        Dinh_User selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("ACTIVE".equals(selected.getStatus())) {
                selected.setStatus("LOCKED");
            } else {
                selected.setStatus("ACTIVE");
            }

            // Lấy tên Admin đang thao tác
            String currentAdmin = SessionManager.getInstance().getCurrentUser().getUsername();

            // GỌI SERVICE (Tự động ghi Audit Log: UPDATE)
            userService.updateUser(selected, currentAdmin);
            
            loadData();
            tableUsers.refresh();
        } else {
            showAlert("Chú ý", "Chọn user để đổi trạng thái!");
        }
    }
    
    // Hàm xóa giữ nguyên hoặc chuyển sang Service nếu muốn log hành động Delete
    @FXML
    public void handleDelete() {
         // ... (Giữ nguyên hoặc update tương tự)
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}