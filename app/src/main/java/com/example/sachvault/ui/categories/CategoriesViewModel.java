package com.example.sachvault.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sachvault.database.dao.CategoryDao;
import com.example.sachvault.database.dao.TransactionDao;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.repository.CategoryRepository;
import com.example.sachvault.repository.TransactionRepository;
import com.example.sachvault.utils.Constants;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    
    private final MutableLiveData<List<Category>> incomeCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> expenseCategories = new MutableLiveData<>();
    private final MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    private final MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    
    // Analytics data
    private final MutableLiveData<List<CategoryDao.CategoryWithUsage>> categoriesWithUsage = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionDao.CategoryStats>> topExpenseCategories = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionDao.CategoryStats>> topIncomeCategories = new MutableLiveData<>();
    private final MutableLiveData<TransactionDao.CategoryUsage> mostUsedCategory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> unusedCategories = new MutableLiveData<>();
    
    // UI state
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> filterType = new MutableLiveData<>(); // "all", "income", "expense"

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
        
        // Initialize filter
        filterType.setValue("all");
    }

    public void loadCategories() {
        // Load income categories
        categoryRepository.getIncomeCategories().observeForever(categories -> {
            incomeCategories.setValue(categories);
        });

        // Load expense categories
        categoryRepository.getExpenseCategories().observeForever(categories -> {
            expenseCategories.setValue(categories);
        });
    }

    public void loadFinancialSummary() {
        // Load total income - using total income method
        transactionRepository.getTotalIncome().observeForever(income -> {
            totalIncome.setValue(income != null ? income : 0.0);
        });

        // Load total expense - using total expenses method  
        transactionRepository.getTotalExpenses().observeForever(expense -> {
            totalExpense.setValue(expense != null ? expense : 0.0);
        });
    }
    
    public void loadCategoryAnalytics() {
        // Load category usage statistics
        categoryRepository.getCategoriesWithUsage().observeForever(usage -> {
            categoriesWithUsage.setValue(usage);
        });
        
        // Load top categories
        transactionRepository.getTopExpenseCategories().observeForever(topExpense -> {
            topExpenseCategories.setValue(topExpense);
        });
        
        transactionRepository.getTopIncomeCategories().observeForever(topIncome -> {
            topIncomeCategories.setValue(topIncome);
        });
        
        // Load most used category
        transactionRepository.getMostUsedCategory().observeForever(mostUsed -> {
            mostUsedCategory.setValue(mostUsed);
        });
        
        // Load unused categories
        transactionRepository.getUnusedCategories().observeForever(unused -> {
            unusedCategories.setValue(unused);
        });
    }

    public void addCategory(String name, String type, String icon, String color) {
        Category category = new Category(name, type, icon, color);
        categoryRepository.insertCategory(category);
        loadCategories(); // Reload categories
    }
    
    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category);
        loadCategories();
        loadCategoryAnalytics(); // Refresh analytics
    }
    
    public void deleteCategory(Category category) {
        categoryRepository.deleteCategory(category);
        loadCategories();
        loadCategoryAnalytics();
    }
    
    public void deactivateCategory(int categoryId) {
        categoryRepository.deactivateCategory(categoryId);
        loadCategories();
        loadCategoryAnalytics();
    }

    // Search and filter functionality
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    public void setFilterType(String type) {
        filterType.setValue(type);
    }

    // Getters for LiveData
    public LiveData<List<Category>> getIncomeCategories() {
        return incomeCategories;
    }

    public LiveData<List<Category>> getExpenseCategories() {
        return expenseCategories;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }
    
    // Analytics getters
    public LiveData<List<CategoryDao.CategoryWithUsage>> getCategoriesWithUsage() {
        return categoriesWithUsage;
    }
    
    public LiveData<List<TransactionDao.CategoryStats>> getTopExpenseCategories() {
        return topExpenseCategories;
    }
    
    public LiveData<List<TransactionDao.CategoryStats>> getTopIncomeCategories() {
        return topIncomeCategories;
    }
    
    public LiveData<TransactionDao.CategoryUsage> getMostUsedCategory() {
        return mostUsedCategory;
    }
    
    public LiveData<List<String>> getUnusedCategories() {
        return unusedCategories;
    }
    
    // Search and filter getters
    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }
    
    public LiveData<String> getFilterType() {
        return filterType;
    }
    
    // Utility methods
    public void refreshData() {
        loadCategories();
        loadFinancialSummary();
        loadCategoryAnalytics();
    }
}