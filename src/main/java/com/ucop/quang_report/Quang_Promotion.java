package com.ucop.quang_report;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
public class Quang_Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Ví dụ: SALE50

    private double discountValue; // Giá trị giảm (VD: 50.000 hoặc 10%)
    
    private String discountType; // FIXED (giảm tiền) hoặc PERCENT (giảm %)

    private LocalDate startDate;
    private LocalDate endDate;

    public Quang_Promotion() {}

    public Quang_Promotion(String code, double value, String type, int durationDays) {
        this.code = code;
        this.discountValue = value;
        this.discountType = type;
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(durationDays);
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}