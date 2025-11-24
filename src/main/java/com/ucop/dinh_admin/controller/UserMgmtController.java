package com.ucop.dinh_admin.controller;

import com.ucop.dinh_admin.Dinh_Role;
import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.dao.UserDAO;
import com.ucop.core.dao.AbstractDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class UserMgmtController {

    @FXML private TableView<Dinh_User> tableUsers;
    @FXML private TextField txtUser;
    @FXML private TextField txtPass;
    @FXML private ComboBox<String> cbRole;

    private UserDAO userDAO = new UserDAO();
    // Tạo DAO nhanh cho Role để lấy danh sách quyền
    private AbstractDAO<Dinh_Role, Long> roleDAO = new AbstractDAO<Dinh_Role, Long>() {};

    @FXML
    public void initialize() {
        loadData();
        // Load danh sách Role vào ComboBox
        List<Dinh_Role> roles = roleDAO.findAll();
        for (Dinh_Role r : roles) {
            cbRole.getItems().add(r.getRoleName());
        }
    }

    private void loadData() {
        tableUsers.setItems(FXCollections.observableArrayList(userDAO.findAll()));
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
            
            // Tìm Role trong DB để gán
            List<Dinh_Role> roles = roleDAO.findAll();
            for(Dinh_Role r : roles) {
                if(r.getRoleName().equals(roleName)) {
                    newUser.addRole(r);
                    break;
                }
            }
            
            userDAO.save(newUser);
            loadData();
            txtUser.clear(); txtPass.clear();
            showAlert("Thành công", "Đã thêm user: " + u);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Trùng tên đăng nhập hoặc lỗi hệ thống!");
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
            userDAO.update(selected);
            loadData();
            tableUsers.refresh(); // Làm mới bảng để thấy trạng thái đổi
        } else {
            showAlert("Chú ý", "Chọn user để đổi trạng thái!");
        }
    }
    
    @FXML
    public void handleDelete() {
         Dinh_User selected = tableUsers.getSelectionModel().getSelectedItem();
         if(selected != null) {
             try {
                 userDAO.delete(selected.getId());
                 loadData();
             } catch(Exception e) {
                 showAlert("Lỗi", "Không thể xóa user này (Do ràng buộc dữ liệu cũ). Hãy dùng chức năng Khóa.");
             }
         }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}