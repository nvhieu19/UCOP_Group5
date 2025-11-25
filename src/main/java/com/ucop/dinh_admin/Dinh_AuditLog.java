package com.ucop.dinh_admin;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class Dinh_AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(nullable = false)
    private String action; // Ví dụ: "LOGIN", "CREATE_USER", "UPDATE_PASS"

    @Column(name = "performed_by")
    private String performedBy; // Tên user thực hiện (ví dụ: "admin")

    @Column(name = "target_table")
    private String targetTable; // Tác động lên bảng nào (ví dụ: "users")

    @Column(columnDefinition = "TEXT")
    private String description; // Chi tiết (ví dụ: "Đổi mật khẩu cho user guest")

    private LocalDateTime timestamp;

    public Dinh_AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public Dinh_AuditLog(String action, String performedBy, String targetTable, String description) {
        this.action = action;
        this.performedBy = performedBy;
        this.targetTable = targetTable;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}