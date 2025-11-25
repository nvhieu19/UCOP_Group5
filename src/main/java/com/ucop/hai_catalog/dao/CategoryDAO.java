package com.ucop.hai_catalog.dao;

import com.ucop.core.dao.AbstractDAO;
import com.ucop.hai_catalog.Hai_Category;

/**
 * DAO (Data Access Object) chuyên dụng cho việc quản lý các thao tác Database
 * của Danh mục (Category).
 * Kế thừa các hàm CRUD cơ bản (save, update, delete, findAll...) từ AbstractDAO.
 */
public class CategoryDAO extends AbstractDAO<Hai_Category, Long> {
    // Hiện tại, chưa cần thêm hàm truy vấn phức tạp nào khác.
    // Các hàm CRUD cơ bản đã có sẵn.
}