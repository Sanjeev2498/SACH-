package com.example.sachvault.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type; // "income" or "expense"

    @ColumnInfo(name = "icon")
    public String icon; // Material icon name

    @ColumnInfo(name = "color")
    public String color; // Hex color code

    @ColumnInfo(name = "is_active")
    public boolean isActive;

    // Default constructor required by Room
    public Category() {
        this.isActive = true;
    }

    // Constructor for creating new categories
    public Category(String name, String type, String icon, String color) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Utility methods
    public boolean isIncomeCategory() {
        return "income".equals(type);
    }

    public boolean isExpenseCategory() {
        return "expense".equals(type);
    }

    // Static methods for predefined categories
    public static Category[] getDefaultIncomeCategories() {
        return new Category[]{
                new Category("Salary", "income", "work", "#4edea3"),
                new Category("Business", "income", "business", "#4edea3"),
                new Category("Investment", "income", "trending_up", "#4edea3"),
                new Category("Gift", "income", "card_giftcard", "#4edea3"),
                new Category("Other Income", "income", "add_circle", "#4edea3")
        };
    }

    public static Category[] getDefaultExpenseCategories() {
        return new Category[]{
                new Category("Food", "expense", "restaurant", "#ffb4ab"),
                new Category("Transport", "expense", "directions_car", "#ffb4ab"),
                new Category("Rent", "expense", "home", "#ffb4ab"),
                new Category("Shopping", "expense", "shopping_bag", "#ffb4ab"),
                new Category("Entertainment", "expense", "movie", "#ffb4ab"),
                new Category("Healthcare", "expense", "local_hospital", "#ffb4ab"),
                new Category("Education", "expense", "school", "#ffb4ab"),
                new Category("Utilities", "expense", "electrical_services", "#ffb4ab"),
                new Category("Insurance", "expense", "security", "#ffb4ab"),
                new Category("Other Expense", "expense", "remove_circle", "#ffb4ab")
        };
    }

    public static Category[] getAllDefaultCategories() {
        Category[] incomeCategories = getDefaultIncomeCategories();
        Category[] expenseCategories = getDefaultExpenseCategories();
        
        Category[] allCategories = new Category[incomeCategories.length + expenseCategories.length];
        System.arraycopy(incomeCategories, 0, allCategories, 0, incomeCategories.length);
        System.arraycopy(expenseCategories, 0, allCategories, incomeCategories.length, expenseCategories.length);
        
        return allCategories;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", icon='" + icon + '\'' +
                ", color='" + color + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}