package com.example.sachvault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sachvault.database.entities.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    // Insert operations
    @Insert
    void insertTransaction(Transaction transaction);

    @Insert
    void insertTransactions(Transaction... transactions);

    // Update operations
    @Update
    void updateTransaction(Transaction transaction);

    // Delete operations
    @Delete
    void deleteTransaction(Transaction transaction);

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    void deleteTransactionById(int transactionId);

    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    // Basic queries
    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getAllTransactions();
    
    // Synchronous version for testing
    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(int limit);

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    LiveData<Transaction> getTransactionById(int transactionId);

    // Filter by type
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getTransactionsByType(String type);

    // Filter by date range
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(String startDate, String endDate);

    // Filter by month (for monthly reports)
    @Query("SELECT * FROM transactions WHERE date LIKE :monthPattern ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getTransactionsByMonth(String monthPattern); // Format: "2026-09%"

    // Filter by account
    @Query("SELECT * FROM transactions WHERE account = :accountName ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getTransactionsByAccount(String accountName);

    // Filter by category
    @Query("SELECT * FROM transactions WHERE category = :categoryName ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String categoryName);

    // Summary queries for dashboard
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense'")
    LiveData<Double> getTotalExpenses();

    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income') - (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense')")
    LiveData<Double> getTotalBalance();

    // Monthly summaries
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND date LIKE :monthPattern")
    LiveData<Double> getMonthlyIncome(String monthPattern);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND date LIKE :monthPattern")
    LiveData<Double> getMonthlyExpenses(String monthPattern);

    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND date LIKE :monthPattern) - (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND date LIKE :monthPattern)")
    LiveData<Double> getMonthlyBalance(String monthPattern);

    // Category-wise summaries for reports
    @Query("SELECT category, SUM(amount) as total, COUNT(*) as count FROM transactions WHERE type = :type GROUP BY category ORDER BY total DESC")
    LiveData<List<CategorySummary>> getCategorySummary(String type);

    // Account-wise summaries
    @Query("SELECT account, SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END) as balance FROM transactions GROUP BY account")
    LiveData<List<AccountSummary>> getAccountBalances();

    // Search functionality
    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%' OR account LIKE '%' || :searchQuery || '%' ORDER BY date DESC, created_at DESC")
    LiveData<List<Transaction>> searchTransactions(String searchQuery);

    // Count queries
    @Query("SELECT COUNT(*) FROM transactions")
    LiveData<Integer> getTransactionCount();

    @Query("SELECT COUNT(*) FROM transactions WHERE type = :type")
    LiveData<Integer> getTransactionCountByType(String type);

    @Query("SELECT COUNT(*) FROM transactions WHERE date LIKE :monthPattern")
    LiveData<Integer> getMonthlyTransactionCount(String monthPattern);

    // Analytics queries for month-specific breakdowns
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND strftime('%m', date) = :month AND strftime('%Y', date) = :year")
    LiveData<Double> getTotalExpenseByMonth(String month, String year);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND strftime('%m', date) = :month AND strftime('%Y', date) = :year")
    LiveData<Double> getTotalIncomeByMonth(String month, String year);

    // Category breakdown for expense analysis
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'expense' AND strftime('%m', date) = :month AND strftime('%Y', date) = :year GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryBreakdown>> getExpenseByCategoryForMonth(String month, String year);

    // Category breakdown for income analysis
    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'income' AND strftime('%m', date) = :month AND strftime('%Y', date) = :year GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryBreakdown>> getIncomeByCategoryForMonth(String month, String year);

    // Category usage and statistics queries
    @Query("SELECT category, COUNT(*) as transaction_count, SUM(amount) as total_amount, AVG(amount) as average_amount FROM transactions WHERE category = :categoryName GROUP BY category")
    LiveData<List<CategoryStats>> getCategoryStatistics(String categoryName);

    @Query("SELECT category, COUNT(*) as transaction_count, SUM(amount) as total_amount, AVG(amount) as average_amount FROM transactions WHERE type = :type GROUP BY category ORDER BY total_amount DESC LIMIT 10")
    LiveData<List<CategoryStats>> getTopCategoriesByType(String type);

    @Query("SELECT category, COUNT(*) as transaction_count, SUM(amount) as total_amount, AVG(amount) as average_amount FROM transactions WHERE type = :type AND date >= date('now', '-30 days') GROUP BY category ORDER BY total_amount DESC")
    LiveData<List<CategoryStats>> getRecentCategoryStats(String type);

    @Query("SELECT category, SUM(amount) as total_amount FROM transactions WHERE type = 'expense' AND date >= date('now', '-6 months') GROUP BY category, strftime('%Y-%m', date) ORDER BY category, date DESC")
    LiveData<List<CategoryTrend>> getCategoryTrends();

    // Most and least used categories
    @Query("SELECT category, COUNT(*) as usage_count FROM transactions GROUP BY category ORDER BY usage_count DESC LIMIT 1")
    LiveData<CategoryUsage> getMostUsedCategory();

    @Query("SELECT category, COUNT(*) as usage_count FROM transactions GROUP BY category ORDER BY usage_count ASC LIMIT 1")
    LiveData<CategoryUsage> getLeastUsedCategory();

    // Unused categories (categories with no transactions in last 3 months)
    @Query("SELECT DISTINCT category FROM transactions WHERE date < date('now', '-90 days') AND category NOT IN (SELECT DISTINCT category FROM transactions WHERE date >= date('now', '-90 days'))")
    LiveData<List<String>> getUnusedCategories();

    // Average spending per category per month
    @Query("SELECT category, AVG(monthly_total) as avg_monthly_spending FROM (SELECT category, strftime('%Y-%m', date) as month, SUM(amount) as monthly_total FROM transactions WHERE type = 'expense' GROUP BY category, month) GROUP BY category ORDER BY avg_monthly_spending DESC")
    LiveData<List<CategoryMonthlyAverage>> getCategoryMonthlyAverages();

    // Data classes for summary queries
    class CategorySummary {
        public String category;
        public double total;
        public int count;
    }

    class AccountSummary {
        public String account;
        public double balance;
    }

    class CategoryBreakdown {
        public String category;
        public double total;
    }

    // Data classes for category analytics
    class CategoryStats {
        public String category;
        public int transaction_count;
        public double total_amount;
        public double average_amount;
    }

    class CategoryTrend {
        public String category;
        public double total_amount;
    }

    class CategoryUsage {
        public String category;
        public int usage_count;
    }

    class CategoryMonthlyAverage {
        public String category;
        public double avg_monthly_spending;
    }
}