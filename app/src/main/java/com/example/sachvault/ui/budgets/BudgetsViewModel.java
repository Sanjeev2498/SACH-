package com.example.sachvault.ui.budgets;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sachvault.database.entities.Budget;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.repository.BudgetRepository;
import com.example.sachvault.repository.CategoryRepository;
import com.example.sachvault.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BudgetsViewModel extends AndroidViewModel {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    
    private final MutableLiveData<List<Budget>> budgetedCategories = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> notBudgetedCategories = new MutableLiveData<>();
    private final MutableLiveData<Double> totalBudget = new MutableLiveData<>();
    private final MutableLiveData<Double> totalSpent = new MutableLiveData<>();
    
    private int currentMonth = 1;
    private int currentYear = 2024;

    public BudgetsViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        categoryRepository = new CategoryRepository(application);
    }

    public void loadBudgetsForMonth(int month, int year) {
        this.currentMonth = month;
        this.currentYear = year;
        
        // Load budgeted categories for the month
        budgetRepository.getBudgetsByMonth(month, year).observeForever(budgets -> {
            budgetedCategories.setValue(budgets);
        });
        
        // Load total budget and spent for the month
        budgetRepository.getTotalBudgetByMonth(month, year).observeForever(total -> {
            totalBudget.setValue(total != null ? total : 0.0);
        });
        
        budgetRepository.getTotalSpentByMonth(month, year).observeForever(spent -> {
            totalSpent.setValue(spent != null ? spent : 0.0);
        });
        
        // Load categories that don't have budgets set
        loadNotBudgetedCategories(month, year);
    }

    private void loadNotBudgetedCategories(int month, int year) {
        // Get all expense categories
        categoryRepository.getExpenseCategories().observeForever(allCategories -> {
            if (allCategories != null) {
                // Filter out categories that already have budgets
                budgetRepository.getBudgetsByMonth(month, year).observeForever(budgets -> {
                    if (budgets != null) {
                        List<Category> notBudgeted = new ArrayList<>();
                        for (Category category : allCategories) {
                            boolean hasBudget = false;
                            for (Budget budget : budgets) {
                                if (budget.getCategoryName().equalsIgnoreCase(category.getName())) {
                                    hasBudget = true;
                                    break;
                                }
                            }
                            if (!hasBudget) {
                                notBudgeted.add(category);
                            }
                        }
                        notBudgetedCategories.setValue(notBudgeted);
                    } else {
                        notBudgetedCategories.setValue(allCategories);
                    }
                });
            }
        });
    }

    public void createBudgetForCategory(String categoryName, double amount, int month, int year) {
        // Find category by name first
        categoryRepository.getAllCategories().observeForever(categories -> {
            if (categories != null) {
                Category category = categories.stream()
                        .filter(cat -> cat.getName().equalsIgnoreCase(categoryName))
                        .findFirst()
                        .orElse(null);
                        
                if (category != null) {
                    Budget budget = new Budget(category.getId(), categoryName, amount, month, year);
                    budgetRepository.insertBudget(budget);
                    
                    // Reload data
                    loadBudgetsForMonth(month, year);
                }
            }
        });
    }

    public void updateBudgetAmount(int budgetId, double newAmount) {
        budgetRepository.updateBudgetAmount(budgetId, newAmount);
        loadBudgetsForMonth(currentMonth, currentYear);
    }

    public void deleteBudget(int budgetId) {
        budgetRepository.deleteBudgetById(budgetId);
        loadBudgetsForMonth(currentMonth, currentYear);
    }

    // Getters for LiveData
    public LiveData<List<Budget>> getBudgetedCategories() {
        return budgetedCategories;
    }

    public LiveData<List<Category>> getNotBudgetedCategories() {
        return notBudgetedCategories;
    }

    public LiveData<Double> getTotalBudget() {
        return totalBudget;
    }

    public LiveData<Double> getTotalSpent() {
        return totalSpent;
    }
}