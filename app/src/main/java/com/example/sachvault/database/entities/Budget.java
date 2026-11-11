package com.example.sachvault.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity(
    tableName = "budgets",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "category_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "category_id"),
        @Index(value = {"category_id", "month", "year"}, unique = true)
    }
)
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @ColumnInfo(name = "category_name")
    public String categoryName;

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "spent")
    public double spent;

    @ColumnInfo(name = "month")
    public int month; // 1-12

    @ColumnInfo(name = "year")
    public int year;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "updated_at")
    public String updatedAt;

    // Default constructor for Room
    public Budget() {
        this.createdAt = new Date().toString();
        this.updatedAt = new Date().toString();
        this.spent = 0.0;
    }

    // Constructor for creating new budget
    public Budget(int categoryId, String categoryName, double amount, int month, int year) {
        this();
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    // Helper methods
    public double getRemainingAmount() {
        return amount - spent;
    }

    public double getSpentPercentage() {
        if (amount <= 0) return 0.0;
        return (spent / amount) * 100.0;
    }

    public boolean isOverBudget() {
        return spent > amount;
    }

    public String getMonthYearString() {
        return String.format("%02d/%04d", month, year);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { 
        this.amount = amount;
        this.updatedAt = new Date().toString();
    }

    public double getSpent() { return spent; }
    public void setSpent(double spent) { 
        this.spent = spent;
        this.updatedAt = new Date().toString();
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}