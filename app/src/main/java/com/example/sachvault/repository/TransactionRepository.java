package com.example.sachvault.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sachvault.database.AppDatabase;
import com.example.sachvault.database.dao.TransactionDao;
import com.example.sachvault.database.entities.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TransactionRepository {
    
    private TransactionDao transactionDao;
    private ExecutorService executor;
    
    // LiveData for UI observation
    private LiveData<List<Transaction>> allTransactions;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpenses;
    private LiveData<Double> totalBalance;
    
    public TransactionRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        transactionDao = database.transactionDao();
        executor = AppDatabase.databaseWriteExecutor;
        
        // Initialize LiveData
        allTransactions = transactionDao.getAllTransactions();
        totalIncome = transactionDao.getTotalIncome();
        totalExpenses = transactionDao.getTotalExpenses();
        totalBalance = transactionDao.getTotalBalance();
    }
    
    // Insert operations
    public void insertTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.insertTransaction(transaction));
    }
    
    // Update operations
    public void updateTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.updateTransaction(transaction));
    }
    
    // Delete operations
    public void deleteTransaction(Transaction transaction) {
        executor.execute(() -> transactionDao.deleteTransaction(transaction));
    }
    
    public void deleteTransactionById(int transactionId) {
        executor.execute(() -> transactionDao.deleteTransactionById(transactionId));
    }
    
    // Query operations - return LiveData for automatic UI updates
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }
    
    public LiveData<List<Transaction>> getRecentTransactions(int limit) {
        return transactionDao.getRecentTransactions(limit);
    }
    
    public LiveData<Transaction> getTransactionById(int transactionId) {
        return transactionDao.getTransactionById(transactionId);
    }
    
    public LiveData<List<Transaction>> getTransactionsByType(String type) {
        return transactionDao.getTransactionsByType(type);
    }
    
    public LiveData<List<Transaction>> getTransactionsByMonth(String monthPattern) {
        return transactionDao.getTransactionsByMonth(monthPattern);
    }
    
    public LiveData<List<Transaction>> getTransactionsByAccount(String accountName) {
        return transactionDao.getTransactionsByAccount(accountName);
    }
    
    public LiveData<List<Transaction>> getTransactionsByCategory(String categoryName) {
        return transactionDao.getTransactionsByCategory(categoryName);
    }
    
    // Summary operations
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }
    
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }
    
    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }
    
    // Monthly summaries
    public LiveData<Double> getMonthlyIncome(String monthPattern) {
        return transactionDao.getMonthlyIncome(monthPattern);
    }
    
    public LiveData<Double> getMonthlyExpenses(String monthPattern) {
        return transactionDao.getMonthlyExpenses(monthPattern);
    }
    
    public LiveData<Double> getMonthlyBalance(String monthPattern) {
        return transactionDao.getMonthlyBalance(monthPattern);
    }
    
    // Category summaries
    public LiveData<List<TransactionDao.CategorySummary>> getCategorySummary(String type) {
        return transactionDao.getCategorySummary(type);
    }
    
    // Account summaries
    public LiveData<List<TransactionDao.AccountSummary>> getAccountBalances() {
        return transactionDao.getAccountBalances();
    }
    
    // Search
    public LiveData<List<Transaction>> searchTransactions(String searchQuery) {
        return transactionDao.searchTransactions(searchQuery);
    }
    
    // Count operations
    public LiveData<Integer> getTransactionCount() {
        return transactionDao.getTransactionCount();
    }
    
    public LiveData<Integer> getTransactionCountByType(String type) {
        return transactionDao.getTransactionCountByType(type);
    }
    
    public LiveData<Integer> getMonthlyTransactionCount(String monthPattern) {
        return transactionDao.getMonthlyTransactionCount(monthPattern);
    }

    // Analytics operations for monthly breakdowns
    public LiveData<Double> getTotalExpenseByMonth(int month, int year) {
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        return transactionDao.getTotalExpenseByMonth(monthStr, yearStr);
    }

    public LiveData<Double> getTotalIncomeByMonth(int month, int year) {
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        return transactionDao.getTotalIncomeByMonth(monthStr, yearStr);
    }

    public LiveData<java.util.Map<String, Double>> getExpenseByCategoryForMonth(int month, int year) {
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        
        // Transform the LiveData from List<CategoryBreakdown> to Map<String, Double>
        LiveData<List<TransactionDao.CategoryBreakdown>> source = transactionDao.getExpenseByCategoryForMonth(monthStr, yearStr);
        return androidx.lifecycle.Transformations.map(source, categoryBreakdowns -> {
            java.util.Map<String, Double> result = new java.util.HashMap<>();
            if (categoryBreakdowns != null) {
                for (TransactionDao.CategoryBreakdown breakdown : categoryBreakdowns) {
                    result.put(breakdown.category, breakdown.total);
                }
            }
            return result;
        });
    }

    public LiveData<java.util.Map<String, Double>> getIncomeByCategoryForMonth(int month, int year) {
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        
        // Transform the LiveData from List<CategoryBreakdown> to Map<String, Double>
        LiveData<List<TransactionDao.CategoryBreakdown>> source = transactionDao.getIncomeByCategoryForMonth(monthStr, yearStr);
        return androidx.lifecycle.Transformations.map(source, categoryBreakdowns -> {
            java.util.Map<String, Double> result = new java.util.HashMap<>();
            if (categoryBreakdowns != null) {
                for (TransactionDao.CategoryBreakdown breakdown : categoryBreakdowns) {
                    result.put(breakdown.category, breakdown.total);
                }
            }
            return result;
        });
    }

    // Category analytics methods
    public LiveData<List<TransactionDao.CategoryStats>> getCategoryStatistics(String categoryName) {
        return transactionDao.getCategoryStatistics(categoryName);
    }

    public LiveData<List<TransactionDao.CategoryStats>> getTopExpenseCategories() {
        return transactionDao.getTopCategoriesByType("expense");
    }

    public LiveData<List<TransactionDao.CategoryStats>> getTopIncomeCategories() {
        return transactionDao.getTopCategoriesByType("income");
    }

    public LiveData<List<TransactionDao.CategoryStats>> getRecentExpenseStats() {
        return transactionDao.getRecentCategoryStats("expense");
    }

    public LiveData<List<TransactionDao.CategoryStats>> getRecentIncomeStats() {
        return transactionDao.getRecentCategoryStats("income");
    }

    public LiveData<List<TransactionDao.CategoryTrend>> getCategoryTrends() {
        return transactionDao.getCategoryTrends();
    }

    public LiveData<TransactionDao.CategoryUsage> getMostUsedCategory() {
        return transactionDao.getMostUsedCategory();
    }

    public LiveData<TransactionDao.CategoryUsage> getLeastUsedCategory() {
        return transactionDao.getLeastUsedCategory();
    }

    public LiveData<List<String>> getUnusedCategories() {
        return transactionDao.getUnusedCategories();
    }

    public LiveData<List<TransactionDao.CategoryMonthlyAverage>> getCategoryMonthlyAverages() {
        return transactionDao.getCategoryMonthlyAverages();
    }
}