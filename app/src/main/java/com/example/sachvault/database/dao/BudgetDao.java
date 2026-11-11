package com.example.sachvault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sachvault.database.entities.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBudget(Budget budget);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBudgets(Budget... budgets);

    // Update operations
    @Update
    void updateBudget(Budget budget);

    @Query("UPDATE budgets SET spent = :spent, updated_at = datetime('now') WHERE id = :budgetId")
    void updateSpentAmount(int budgetId, double spent);

    @Query("UPDATE budgets SET amount = :amount, updated_at = datetime('now') WHERE id = :budgetId")
    void updateBudgetAmount(int budgetId, double amount);

    // Delete operations
    @Delete
    void deleteBudget(Budget budget);

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    void deleteBudgetById(int budgetId);

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    void deleteBudgetsByMonth(int month, int year);

    // Basic queries
    @Query("SELECT * FROM budgets ORDER BY category_name ASC")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    LiveData<Budget> getBudgetById(int budgetId);

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND month = :month AND year = :year LIMIT 1")
    LiveData<Budget> getBudgetByCategoryAndMonth(int categoryId, int month, int year);

    // Monthly budget queries
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category_name ASC")
    LiveData<List<Budget>> getBudgetsByMonth(int month, int year);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND amount > 0 ORDER BY category_name ASC")
    LiveData<List<Budget>> getActiveBudgetsByMonth(int month, int year);

    // Budget summaries
    @Query("SELECT SUM(amount) FROM budgets WHERE month = :month AND year = :year")
    LiveData<Double> getTotalBudgetByMonth(int month, int year);

    @Query("SELECT SUM(spent) FROM budgets WHERE month = :month AND year = :year")
    LiveData<Double> getTotalSpentByMonth(int month, int year);

    @Query("SELECT COUNT(*) FROM budgets WHERE month = :month AND year = :year AND amount > 0")
    LiveData<Integer> getActiveBudgetCountByMonth(int month, int year);

    @Query("SELECT COUNT(*) FROM budgets WHERE month = :month AND year = :year AND spent > amount")
    LiveData<Integer> getOverBudgetCountByMonth(int month, int year);

    // Category budget statistics
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId ORDER BY year DESC, month DESC")
    LiveData<List<Budget>> getBudgetHistoryByCategory(int categoryId);

    @Query("SELECT AVG(amount) FROM budgets WHERE category_id = :categoryId")
    LiveData<Double> getAverageBudgetByCategory(int categoryId);

    // Budget progress queries
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND spent > amount ORDER BY (spent - amount) DESC")
    LiveData<List<Budget>> getOverBudgetsByMonth(int month, int year);

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND spent <= amount ORDER BY (spent / amount) DESC")
    LiveData<List<Budget>> getOnTrackBudgetsByMonth(int month, int year);

    // Utility queries
    @Query("SELECT EXISTS(SELECT 1 FROM budgets WHERE category_id = :categoryId AND month = :month AND year = :year)")
    boolean budgetExistsForCategoryAndMonth(int categoryId, int month, int year);

    @Query("SELECT COUNT(*) FROM budgets")
    int getBudgetCount();

    // Synchronous methods for background operations
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND month = :month AND year = :year LIMIT 1")
    Budget getBudgetByCategoryAndMonthSync(int categoryId, int month, int year);

    @Query("SELECT SUM(amount) FROM budgets WHERE month = :month AND year = :year")
    double getTotalBudgetByMonthSync(int month, int year);

    @Query("SELECT SUM(spent) FROM budgets WHERE month = :month AND year = :year")
    double getTotalSpentByMonthSync(int month, int year);

    // Budget analytics
    @Query("SELECT b.*, " +
           "(SELECT SUM(t.amount) FROM transactions t WHERE t.category = b.category_name AND " +
           "strftime('%m', t.date) = printf('%02d', b.month) AND strftime('%Y', t.date) = b.year AND t.type = 'expense') as actual_spent " +
           "FROM budgets b WHERE b.month = :month AND b.year = :year ORDER BY b.category_name ASC")
    LiveData<List<BudgetWithActualSpent>> getBudgetsWithActualSpent(int month, int year);

    // Data class for budget with actual spending
    class BudgetWithActualSpent {
        public int id;
        @androidx.room.ColumnInfo(name = "category_id")
        public int categoryId;
        @androidx.room.ColumnInfo(name = "category_name")
        public String categoryName;
        public double amount;
        public double spent;
        public int month;
        public int year;
        public double actual_spent;
        
        public double getSpentPercentage() {
            if (amount <= 0) return 0.0;
            return (actual_spent / amount) * 100.0;
        }
        
        public boolean isOverBudget() {
            return actual_spent > amount;
        }
        
        public double getRemainingAmount() {
            return amount - actual_spent;
        }
    }
}