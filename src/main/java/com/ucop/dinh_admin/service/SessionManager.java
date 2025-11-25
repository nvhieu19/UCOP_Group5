package com.ucop.dinh_admin.service;

import com.ucop.dinh_admin.Dinh_User;

/**
 * Lớp Singleton để quản lý thông tin người dùng đăng nhập trong toàn bộ ứng dụng.
 */
public class SessionManager {

    private static SessionManager instance;
    private Dinh_User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Dinh_User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Dinh_User currentUser) {
        this.currentUser = currentUser;
    }
}