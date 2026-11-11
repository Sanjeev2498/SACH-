package com.example.sachvault.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sachvault.database.AppDatabase;
import com.example.sachvault.database.dao.BudgetDao;
import com.example.sachvault.database.entities.Budget;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final ExecutorService executor;

    public BudgetRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        budgetDao = database.budgetDao();
        executor = Executors.newFixedThreadPool(2);
    }

    // Insert operations
    public void insertBudget(Budget budget) {
        executor.execute(() -> budgetDao.insertBudget(budget));
    }

    public void insertBudgets(Budget... budgets) {
        executor.execute(() -> budgetDao.insertBudgets(budgets));
    }

    // Update operations
    public void updateBudget(Budget budget) {
        executor.execute(() -> budgetDao.updateBudget(budget));
    }

    public void updateSpentAmount(int budgetId, double spent) {
        executor.execute(() -> budgetDao.updateSpentAmount(budgetId, spent));
    }

    public void updateBudgetAmount(int budgetId, double amount) {
        executor.execute(() -> budgetDao.updateBudgetAmount(budgetId, amount));
    }

    // Delete operations
    public void deleteBudget(Budget budget) {
        executor.execute(() -> budgetDao.deleteBudget(budget));
    }

    public void deleteBudgetById(int budgetId) {
        executor.execute(() -> budgetDao.deleteBudgetById(budgetId));
    }

    public void deleteBudgetsByMonth(int month, int year) {
        executor.execute(() -> budgetDao.deleteBudgetsByMonth(month, year));
    }

    // Query operations
    public LiveData<List<Budget>> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    public LiveData<Budget> getBudgetById(int budgetId) {
        return budgetDao.getBudgetById(budgetId);
    }

    public LiveData<Budget> getBudgetByCategoryAndMonth(int categoryId, int month, int year) {
        return budgetDao.getBudgetByCategoryAndMonth(categoryId, month, year);
    }

    // Monthly budget operations
    public LiveData<List<Budget>> getBudgetsByMonth(int month, int year) {
        return budgetDao.getBudgetsByMonth(month, year);
    }

    public LiveData<List<Budget>> getActiveBudgetsByMonth(int month, int year) {
        return budgetDao.getActiveBudgetsByMonth(month, year);
    }

    // Budget summaries
    public LiveData<Double> getTotalBudgetByMonth(int month, int year) {
        return budgetDao.getTotalBudgetByMonth(month, year);
    }

    public LiveData<Double> getTotalSpentByMonth(int month, int year) {
        return budgetDao.getTotalSpentByMonth(month, year);
    }

    public LiveData<Integer> getActiveBudgetCountByMonth(int month, int year) {
        return budgetDao.getActiveBudgetCountByMonth(month, year);
    }

    public LiveData<Integer> getOverBudgetCountByMonth(int month, int year) {
        return budgetDao.getOverBudgetCountByMonth(month, year);
    }

    // Category budget statistics
    public LiveData<List<Budget>> getBudgetHistoryByCategory(int categoryId) {
        return budgetDao.getBudgetHistoryByCategory(categoryId);
    }

    public LiveData<Double> getAverageBudgetByCategory(int categoryId) {
        return budgetDao.getAverageBudgetByCategory(categoryId);
    }

    // Budget progress queries
    public LiveData<List<Budget>> getOverBudgetsByMonth(int month, int year) {
        return budgetDao.getOverBudgetsByMonth(month, year);
    }

    public LiveData<List<Budget>> getOnTrackBudgetsByMonth(int month, int year) {
        return budgetDao.getOnTrackBudgetsByMonth(month, year);
    }

    // Budget analytics
    public LiveData<List<BudgetDao.BudgetWithActualSpent>> getBudgetsWithActualSpent(int month, int year) {
        return budgetDao.getBudgetsWithActualSpent(month, year);
    }

    // Utility methods
    public boolean budgetExistsForCategoryAndMonth(int categoryId, int month, int year) {
        return budgetDao.budgetExistsForCategoryAndMonth(categoryId, month, year);
    }

    // Synchronous methods for background operations
    public Budget getBudgetByCategoryAndMonthSync(int categoryId, int month, int year) {
        return budgetDao.getBudgetByCategoryAndMonthSync(categoryId, month, year);
    }

    public double getTotalBudgetByMonthSync(int month, int year) {
        return budgetDao.getTotalBudgetByMonthSync(month, year);
    }

    public double getTotalSpentByMonthSync(int month, int year) {
        return budgetDao.getTotalSpentByMonthSync(month, year);
    }
}