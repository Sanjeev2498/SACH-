package com.example.sachvault.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.sachvault.database.entities.Account;

import java.util.List;

@Dao
public interface AccountDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccount(Account account);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccounts(Account... accounts);

    // Update operations
    @Update
    void updateAccount(Account account);

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :accountId")
    void updateAccountBalance(int accountId, double newBalance);

    @Query("UPDATE accounts SET balance = balance + :amount WHERE name = :accountName")
    void addToAccountBalance(String accountName, double amount);

    @Query("UPDATE accounts SET balance = balance - :amount WHERE name = :accountName")
    void subtractFromAccountBalance(String accountName, double amount);

    // Delete operations
    @Delete
    void deleteAccount(Account account);

    @Query("DELETE FROM accounts WHERE id = :accountId")
    void deleteAccountById(int accountId);

    @Query("UPDATE accounts SET is_active = 0 WHERE id = :accountId")
    void deactivateAccount(int accountId);

    @Query("UPDATE accounts SET is_active = 1 WHERE id = :accountId")
    void activateAccount(int accountId);

    // Basic queries
    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Account>> getAllActiveAccounts();

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    LiveData<List<Account>> getAllAccounts();

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    LiveData<Account> getAccountById(int accountId);

    @Query("SELECT * FROM accounts WHERE name = :accountName LIMIT 1")
    LiveData<Account> getAccountByName(String accountName);

    // Filter by type
    @Query("SELECT * FROM accounts WHERE type = :accountType AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Account>> getAccountsByType(String accountType);

    // Balance queries
    @Query("SELECT SUM(balance) FROM accounts WHERE is_active = 1")
    LiveData<Double> getTotalBalance();

    @Query("SELECT SUM(balance) FROM accounts WHERE balance > 0 AND is_active = 1")
    LiveData<Double> getTotalAssets();

    @Query("SELECT SUM(ABS(balance)) FROM accounts WHERE balance < 0 AND is_active = 1")
    LiveData<Double> getTotalLiabilities();

    // Account statistics
    @Query("SELECT COUNT(*) FROM accounts WHERE is_active = 1")
    LiveData<Integer> getActiveAccountCount();

    @Query("SELECT COUNT(*) FROM accounts WHERE balance > 0 AND is_active = 1")
    LiveData<Integer> getPositiveBalanceAccountCount();

    @Query("SELECT COUNT(*) FROM accounts WHERE balance < 0 AND is_active = 1")
    LiveData<Integer> getNegativeBalanceAccountCount();

    // Account validation
    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE name = :accountName)")
    boolean accountExists(String accountName);

    @Query("SELECT COUNT(*) FROM accounts WHERE name = :accountName AND id != :excludeId")
    int getAccountCountByNameExcluding(String accountName, int excludeId);

    // Account summaries
    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY balance DESC")
    LiveData<List<Account>> getAccountsByBalanceDesc();

    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY balance ASC")
    LiveData<List<Account>> getAccountsByBalanceAsc();

    // Synchronous methods for immediate operations (use with caution - run in background)
    @Query("SELECT balance FROM accounts WHERE name = :accountName LIMIT 1")
    double getAccountBalanceSync(String accountName);

    @Query("SELECT * FROM accounts WHERE name = :accountName LIMIT 1")
    Account getAccountByNameSync(String accountName);

    // Utility methods
    @Query("SELECT name FROM accounts WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<String>> getActiveAccountNames();

    @Query("SELECT DISTINCT type FROM accounts WHERE is_active = 1 ORDER BY type ASC")
    LiveData<List<String>> getActiveAccountTypes();

    // Default data insertion check
    @Query("SELECT COUNT(*) FROM accounts")
    int getAccountCount();
}