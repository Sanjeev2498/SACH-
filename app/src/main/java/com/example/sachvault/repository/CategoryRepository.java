package com.example.sachvault.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sachvault.database.AppDatabase;
import com.example.sachvault.database.dao.CategoryDao;
import com.example.sachvault.database.entities.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class CategoryRepository {
    
    private CategoryDao categoryDao;
    private ExecutorService executor;
    
    // LiveData for UI observation
    private LiveData<List<Category>> allActiveCategories;
    private LiveData<List<Category>> incomeCategories;
    private LiveData<List<Category>> expenseCategories;
    
    public CategoryRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        executor = AppDatabase.databaseWriteExecutor;
        
        // Initialize LiveData
        allActiveCategories = categoryDao.getAllActiveCategories();
        incomeCategories = categoryDao.getIncomeCategories();
        expenseCategories = categoryDao.getExpenseCategories();
    }
    
    // Insert operations
    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insertCategory(category));
    }
    
    // Update operations
    public void updateCategory(Category category) {
        executor.execute(() -> categoryDao.updateCategory(category));
    }
    
    // Delete operations
    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDao.deleteCategory(category));
    }
    
    public void deleteCategoryById(int categoryId) {
        executor.execute(() -> categoryDao.deleteCategoryById(categoryId));
    }
    
    public void deactivateCategory(int categoryId) {
        executor.execute(() -> categoryDao.deactivateCategory(categoryId));
    }
    
    public void activateCategory(int categoryId) {
        executor.execute(() -> categoryDao.activateCategory(categoryId));
    }
    
    // Query operations - return LiveData for automatic UI updates
    public LiveData<List<Category>> getAllActiveCategories() {
        return allActiveCategories;
    }
    
    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }
    
    public LiveData<Category> getCategoryById(int categoryId) {
        return categoryDao.getCategoryById(categoryId);
    }
    
    public LiveData<Category> getCategoryByName(String categoryName) {
        return categoryDao.getCategoryByName(categoryName);
    }
    
    public LiveData<List<Category>> getCategoriesByType(String categoryType) {
        return categoryDao.getCategoriesByType(categoryType);
    }
    
    public LiveData<List<Category>> getIncomeCategories() {
        return incomeCategories;
    }
    
    public LiveData<List<Category>> getExpenseCategories() {
        return expenseCategories;
    }
    
    // Statistics
    public LiveData<Integer> getActiveCategoryCount() {
        return categoryDao.getActiveCategoryCount();
    }
    
    public LiveData<Integer> getCategoryCountByType(String categoryType) {
        return categoryDao.getCategoryCountByType(categoryType);
    }
    
    // Category usage statistics
    public LiveData<List<CategoryDao.CategoryWithUsage>> getCategoriesWithUsage() {
        return categoryDao.getCategoriesWithUsage();
    }
    
    public LiveData<List<CategoryDao.CategoryWithUsage>> getCategoriesWithUsageByType(String categoryType) {
        return categoryDao.getCategoriesWithUsageByType(categoryType);
    }
    
    // Utility methods
    public LiveData<List<String>> getCategoryNamesByType(String categoryType) {
        return categoryDao.getCategoryNamesByType(categoryType);
    }
    
    public LiveData<List<String>> getIncomeCategoryNames() {
        return categoryDao.getIncomeCategoryNames();
    }
    
    public LiveData<List<String>> getExpenseCategoryNames() {
        return categoryDao.getExpenseCategoryNames();
    }
    
    // Validation (synchronous - use carefully)
    public boolean categoryExists(String categoryName, String categoryType) {
        return categoryDao.categoryExists(categoryName, categoryType);
    }
    
    public int getCategoryCountByNameAndTypeExcluding(String categoryName, String categoryType, int excludeId) {
        return categoryDao.getCategoryCountByNameAndTypeExcluding(categoryName, categoryType, excludeId);
    }
    
    // Helper method to check if database has categories
    public boolean hasCategories() {
        return categoryDao.getCategoryCount() > 0;
    }
}