package com.ucop.dinh_admin.service;

import com.ucop.dinh_admin.Dinh_User;
import com.ucop.dinh_admin.dao.UserDAO;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public Dinh_User login(String username, String password) {
        // Có thể thêm logic mã hóa mật khẩu ở đây trước khi gọi DAO
        return userDAO.findByUsernameAndPassword(username, password);
    }
    
    public void register(Dinh_User user) {
        // Logic kiểm tra password mạnh/yếu, trùng username...
        userDAO.save(user);
    }
}