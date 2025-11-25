package com.ucop.dinh_admin.service;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.Dinh_AuditLog;
import com.ucop.dinh_admin.dao.UserDAO;
import com.ucop.dinh_admin.dao.AuditDAO;
import java.util.List;

public class UserService {
    private UserDAO userDAO = new UserDAO();
    private AuditDAO auditDAO = new AuditDAO(); // DAO mới để ghi log

    // 1. Chức năng Đăng nhập
    public Dinh_User login(String username, String password) {
        // Lưu ý: Nếu DB đã mã hóa pass thì phải mã hóa 'password' nhập vào trước khi so sánh
        Dinh_User user = userDAO.findByUsernameAndPassword(username, password);
        
        if (user != null && "ACTIVE".equals(user.getStatus())) {
            recordAudit("LOGIN", username, "users", "User logged in successfully");
            return user;
        }
        return null;
    }

    // 2. Chức năng Đăng ký / Tạo mới (Có Validation + Audit)
    public boolean register(Dinh_User user, String performedBy) {
        // Validation: Kiểm tra trùng tên đăng nhập
        if (userDAO.findByUsername(user.getUsername()) != null) {
            System.out.println("Lỗi: Username '" + user.getUsername() + "' đã tồn tại!");
            return false;
        }

        // Set mặc định nếu thiếu
        if (user.getStatus() == null) user.setStatus("ACTIVE");
        
        // Lưu User
        userDAO.save(user);

        // Ghi Audit Log (Yêu cầu bắt buộc của SV1)
        recordAudit("CREATE", performedBy, "users", "Created new user: " + user.getUsername());
        
        return true;
    }

    // 3. Chức năng Lấy danh sách (Cho Admin Dashboard)
    public List<Dinh_User> getAllUsers() {
        return userDAO.findAll();
    }

    // 4. Chức năng Update User
    public void updateUser(Dinh_User user, String performedBy) {
        userDAO.update(user);
        recordAudit("UPDATE", performedBy, "users", "Updated user info: " + user.getUsername());
    }

    // 5. Chức năng Đổi mật khẩu (Change Password)
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Dinh_User user = userDAO.findByUsernameAndPassword(username, oldPassword);
        
        if (user == null) {
            return false; // Mật khẩu cũ sai
        }
        
        // Cập nhật mật khẩu mới
        user.setPassword(newPassword);
        userDAO.update(user);
        
        // Ghi audit log
        recordAudit("CHANGE_PASSWORD", username, "users", "User changed password");
        
        return true;
    }

    // --- Hàm phụ trợ: Ghi Audit Log ---
    public void recordAudit(String action, String who, String table, String desc) {
        try {
            Dinh_AuditLog log = new Dinh_AuditLog(action, who, table, desc);
            auditDAO.save(log);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi nhưng không làm crash app
        }
    }
}