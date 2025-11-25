package com.ucop.dinh_admin.controller;

import com.ucop.dinh_admin.Dinh_AuditLog;
import com.ucop.dinh_admin.dao.AuditDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell; // Import thêm cái này để chỉnh sửa ô
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuditController {

    @FXML private TableView<Dinh_AuditLog> tableAudit;
    @FXML private TableColumn<Dinh_AuditLog, Long> colId;
    @FXML private TableColumn<Dinh_AuditLog, String> colUser;
    @FXML private TableColumn<Dinh_AuditLog, String> colAction;
    @FXML private TableColumn<Dinh_AuditLog, String> colTable;
    // Để Object để nhận được cả String lẫn Date/Time
    @FXML private TableColumn<Dinh_AuditLog, Object> colTime; 
    @FXML private TableColumn<Dinh_AuditLog, String> colDesc;

    private AuditDAO auditDAO = new AuditDAO();

    @FXML
    public void initialize() {
        try {
            // --- SỬA LẠI ĐOẠN NÀY ĐỂ CÓ THANH KÉO ---
            
            // 1. Cho phép bảng tràn ra ngoài (sẽ hiện thanh kéo ngang)
            if (tableAudit != null) {
                tableAudit.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            }

            // 2. Set độ rộng cứng cho từng cột (Để nó to ra, ép thanh kéo phải hiện)
            if (colId != null) { 
                colId.setCellValueFactory(new PropertyValueFactory<>("id"));
                colId.setPrefWidth(50); // ID nhỏ thôi
            }
            if (colUser != null) {
                colUser.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
                colUser.setPrefWidth(120);
            }
            if (colAction != null) {
                colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
                colAction.setPrefWidth(100);
            }
            if (colTable != null) {
                colTable.setCellValueFactory(new PropertyValueFactory<>("targetTable"));
                colTable.setPrefWidth(100);
            }
            
            // Cột Chi tiết cho to đùng ra để đọc cho sướng
            if (colDesc != null) {
                colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
                colDesc.setPrefWidth(400); 
            }

            // --- XỬ LÝ CỘT THỜI GIAN (GIỮ NGUYÊN CODE CŨ) ---
            if (colTime != null) {
                colTime.setPrefWidth(160); // Set thêm độ rộng
                colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
                colTime.setCellFactory(column -> new TableCell<Dinh_AuditLog, Object>() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            String timeStr = item.toString();
                            if(timeStr.contains("T")) timeStr = timeStr.replace("T", " ");
                            if(timeStr.contains(".")) timeStr = timeStr.split("\\.")[0];
                            setText(timeStr);
                        }
                    }
                });
            }

            // 3. Load dữ liệu
            loadData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            if (tableAudit != null) {
                var logs = auditDAO.findAll();
                // Nếu lấy được dữ liệu thì đổ vào bảng
                if (logs != null) {
                    tableAudit.setItems(FXCollections.observableArrayList(logs));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc Database bảng Audit:");
            e.printStackTrace();
        }
    }
}