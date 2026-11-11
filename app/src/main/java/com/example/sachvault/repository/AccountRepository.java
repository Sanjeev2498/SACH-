package com.example.sachvault.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sachvault.database.AppDatabase;
import com.example.sachvault.database.dao.AccountDao;
import com.example.sachvault.database.entities.Account;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class AccountRepository {
    
    private AccountDao accountDao;
    private ExecutorService executor;
    
    // LiveData for UI observation
    private LiveData<List<Account>> allActiveAccounts;
    private LiveData<Double> totalBalance;
    
    public AccountRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        accountDao = database.accountDao();
        executor = AppDatabase.databaseWriteExecutor;
        
        // Initialize LiveData
        allActiveAccounts = accountDao.getAllActiveAccounts();
        totalBalance = accountDao.getTotalBalance();
    }
    
    // Insert operations
    public void insertAccount(Account account) {
        executor.execute(() -> accountDao.insertAccount(account));
    }
    
    // Update operations
    public void updateAccount(Account account) {
        executor.execute(() -> accountDao.updateAccount(account));
    }
    
    public void updateAccountBalance(int accountId, double newBalance) {
        executor.execute(() -> accountDao.updateAccountBalance(accountId, newBalance));
    }
    
    public void addToAccountBalance(String accountName, double amount) {
        executor.execute(() -> accountDao.addToAccountBalance(accountName, amount));
    }
    
    public void subtractFromAccountBalance(String accountName, double amount) {
        executor.execute(() -> accountDao.subtractFromAccountBalance(accountName, amount));
    }
    
    // Delete operations
    public void deleteAccount(Account account) {
        executor.execute(() -> accountDao.deleteAccount(account));
    }
    
    public void deleteAccountById(int accountId) {
        executor.execute(() -> accountDao.deleteAccountById(accountId));
    }
    
    public void deactivateAccount(int accountId) {
        executor.execute(() -> accountDao.deactivateAccount(accountId));
    }
    
    public void activateAccount(int accountId) {
        executor.execute(() -> accountDao.activateAccount(accountId));
    }
    
    // Query operations - return LiveData for automatic UI updates
    public LiveData<List<Account>> getAllActiveAccounts() {
        return allActiveAccounts;
    }
    
    public LiveData<List<Account>> getAllAccounts() {
        return accountDao.getAllAccounts();
    }
    
    public LiveData<Account> getAccountById(int accountId) {
        return accountDao.getAccountById(accountId);
    }
    
    public LiveData<Account> getAccountByName(String accountName) {
        return accountDao.getAccountByName(accountName);
    }
    
    public LiveData<List<Account>> getAccountsByType(String accountType) {
        return accountDao.getAccountsByType(accountType);
    }
    
    // Balance operations
    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }
    
    public LiveData<Double> getTotalAssets() {
        return accountDao.getTotalAssets();
    }
    
    public LiveData<Double> getTotalLiabilities() {
        return accountDao.getTotalLiabilities();
    }
    
    // Statistics
    public LiveData<Integer> getActiveAccountCount() {
        return accountDao.getActiveAccountCount();
    }
    
    public LiveData<Integer> getPositiveBalanceAccountCount() {
        return accountDao.getPositiveBalanceAccountCount();
    }
    
    public LiveData<Integer> getNegativeBalanceAccountCount() {
        return accountDao.getNegativeBalanceAccountCount();
    }
    
    // Account summaries
    public LiveData<List<Account>> getAccountsByBalanceDesc() {
        return accountDao.getAccountsByBalanceDesc();
    }
    
    public LiveData<List<Account>> getAccountsByBalanceAsc() {
        return accountDao.getAccountsByBalanceAsc();
    }
    
    // Utility methods
    public LiveData<List<String>> getActiveAccountNames() {
        return accountDao.getActiveAccountNames();
    }
    
    public LiveData<List<String>> getActiveAccountTypes() {
        return accountDao.getActiveAccountTypes();
    }
    
    // Validation (synchronous - use carefully)
    public boolean accountExists(String accountName) {
        return accountDao.accountExists(accountName);
    }
    
    public int getAccountCountByNameExcluding(String accountName, int excludeId) {
        return accountDao.getAccountCountByNameExcluding(accountName, excludeId);
    }
    
    // Helper method to check if database has accounts
    public boolean hasAccounts() {
        return accountDao.getAccountCount() > 0;
    }
}