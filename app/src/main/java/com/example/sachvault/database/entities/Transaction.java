package com.example.sachvault.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "type")
    public String type; // "income", "expense", "transfer"

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "account")
    public String account;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "date")
    public String date; // ISO 8601 format: yyyy-MM-dd

    @ColumnInfo(name = "time")
    public String time; // HH:mm format

    @ColumnInfo(name = "created_at")
    public long createdAt; // Unix timestamp

    // Default constructor required by Room
    public Transaction() {
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor for creating new transactions
    public Transaction(double amount, String type, String category, String account, String note, String date, String time) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.account = account;
        this.note = note;
        this.date = date;
        this.time = time;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public boolean isIncome() {
        return "income".equals(type);
    }

    public boolean isExpense() {
        return "expense".equals(type);
    }

    public boolean isTransfer() {
        return "transfer".equals(type);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", account='" + account + '\'' +
                ", note='" + note + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}