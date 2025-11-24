package com.ucop.long_payment;

import javax.persistence.*;
import java.math.BigDecimal;
import com.ucop.dinh_admin.Dinh_User;

@Entity
@Table(name = "wallets")
public class Long_Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance")
    private BigDecimal balance; // Số dư ví

    // Quan hệ 1-1: Mỗi User có 1 cái ví
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Dinh_User user;

    public Long_Wallet() {}

    public Long_Wallet(Dinh_User user, double initialBalance) {
        this.user = user;
        this.balance = BigDecimal.valueOf(initialBalance);
    }

    // Hàm nạp tiền
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    // Hàm trừ tiền (Trả về true nếu trừ thành công)
    public boolean deduct(BigDecimal amount) {
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            return true;
        }
        return false; // Không đủ tiền
    }

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public Dinh_User getUser() { return user; }
    public void setUser(Dinh_User user) { this.user = user; }
}