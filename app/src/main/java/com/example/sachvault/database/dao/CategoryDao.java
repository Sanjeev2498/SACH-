package com.example.sachvault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sachvault.database.entities.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(Category... categories);

    // Update operations
    @Update
    void updateCategory(Category category);

    // Delete operations
    @Delete
    void deleteCategory(Category category);

    @Query("DELETE FROM categories WHERE id = :categoryId")
    void deleteCategoryById(int categoryId);

    @Query("UPDATE categories SET is_active = 0 WHERE id = :categoryId")
    void deactivateCategory(int categoryId);

    @Query("UPDATE categories SET is_active = 1 WHERE id = :categoryId")
    void activateCategory(int categoryId);

    // Basic queries
    @Query("SELECT * FROM categories WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getAllActiveCategories();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    LiveData<Category> getCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE name = :categoryName LIMIT 1")
    LiveData<Category> getCategoryByName(String categoryName);

    // Filter by type (income/expense)
    @Query("SELECT * FROM categories WHERE type = :categoryType AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(String categoryType);

    @Query("SELECT * FROM categories WHERE type = 'income' AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getIncomeCategories();

    @Query("SELECT * FROM categories WHERE type = 'expense' AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getExpenseCategories();

    // Category statistics
    @Query("SELECT COUNT(*) FROM categories WHERE is_active = 1")
    LiveData<Integer> getActiveCategoryCount();

    @Query("SELECT COUNT(*) FROM categories WHERE type = :categoryType AND is_active = 1")
    LiveData<Integer> getCategoryCountByType(String categoryType);

    // Category validation
    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE name = :categoryName AND type = :categoryType)")
    boolean categoryExists(String categoryName, String categoryType);

    @Query("SELECT COUNT(*) FROM categories WHERE name = :categoryName AND type = :categoryType AND id != :excludeId")
    int getCategoryCountByNameAndTypeExcluding(String categoryName, String categoryType, int excludeId);

    // Utility methods
    @Query("SELECT name FROM categories WHERE type = :categoryType AND is_active = 1 ORDER BY name ASC")
    LiveData<List<String>> getCategoryNamesByType(String categoryType);

    @Query("SELECT name FROM categories WHERE type = 'income' AND is_active = 1 ORDER BY name ASC")
    LiveData<List<String>> getIncomeCategoryNames();

    @Query("SELECT name FROM categories WHERE type = 'expense' AND is_active = 1 ORDER BY name ASC")
    LiveData<List<String>> getExpenseCategoryNames();

    // Synchronous methods for immediate operations (use with caution - run in background)
    @Query("SELECT * FROM categories WHERE name = :categoryName LIMIT 1")
    Category getCategoryByNameSync(String categoryName);
    
    @Query("SELECT * FROM categories WHERE name = :categoryName AND type = :categoryType LIMIT 1")
    Category getCategoryByNameAndTypeSync(String categoryName, String categoryType);

    @Query("SELECT icon FROM categories WHERE name = :categoryName LIMIT 1")
    String getCategoryIconSync(String categoryName);

    @Query("SELECT color FROM categories WHERE name = :categoryName LIMIT 1")
    String getCategoryColorSync(String categoryName);

    // Default data insertion check
    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

    // Category usage statistics (for reports)
    @Query("SELECT c.*, COUNT(t.id) as usage_count FROM categories c LEFT JOIN transactions t ON c.name = t.category WHERE c.is_active = 1 GROUP BY c.id ORDER BY usage_count DESC, c.name ASC")
    LiveData<List<CategoryWithUsage>> getCategoriesWithUsage();

    @Query("SELECT c.*, COUNT(t.id) as usage_count FROM categories c LEFT JOIN transactions t ON c.name = t.category WHERE c.type = :categoryType AND c.is_active = 1 GROUP BY c.id ORDER BY usage_count DESC, c.name ASC")
    LiveData<List<CategoryWithUsage>> getCategoriesWithUsageByType(String categoryType);

    // Data class for category usage statistics
    class CategoryWithUsage {
        public int id;
        public String name;
        public String type;
        public String icon;
        public String color;
        public boolean is_active;  // Match database column name
        public int usage_count;
    }
}