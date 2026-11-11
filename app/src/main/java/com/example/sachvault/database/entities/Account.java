package com.example.sachvault.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type; // "bank", "card", "cash", "savings", "upi"

    @ColumnInfo(name = "icon")
    public String icon; // Material icon name

    @ColumnInfo(name = "balance")
    public double balance;

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    // Default constructor required by Room
    public Account() {
        this.isActive = true;
        this.balance = 0.0;
    }

    // Constructor for creating new accounts
    public Account(String name, String type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.balance = 0.0;
        this.isActive = true;
    }

    // Predefined account constructor (for initial data)
    public Account(String name, String type, String icon, double balance) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.balance = balance;
        this.isActive = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Utility methods
    public void addToBalance(double amount) {
        this.balance += amount;
    }

    public void subtractFromBalance(double amount) {
        this.balance -= amount;
    }

    public String getFormattedBalance() {
        return String.format("Rs %.2f", balance);
    }

    // Static methods for predefined accounts
    public static Account[] getDefaultAccounts() {
        return new Account[]{
                new Account("Bank", "bank", "account_balance", 12000.0),
                new Account("Card", "card", "credit_card", -1500.0),
                new Account("Cash", "cash", "payments", 2000.0),
                new Account("Savings", "savings", "savings", 10000.0),
                new Account("UPI", "upi", "qr_code_scanner", 732.0)
        };
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", icon='" + icon + '\'' +
                ", balance=" + balance +
                ", isActive=" + isActive +
                '}';
    }
}