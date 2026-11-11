package com.example.sachvault.model;

public class CategoryItem {
    private String name;
    private int iconResource;
    private String type; // "income" or "expense"

    public CategoryItem(String name, int iconResource, String type) {
        this.name = name;
        this.iconResource = iconResource;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}