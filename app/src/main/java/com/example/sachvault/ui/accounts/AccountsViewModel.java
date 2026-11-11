package com.example.sachvault.ui.accounts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sachvault.database.entities.Account;
import com.example.sachvault.repository.AccountRepository;
import com.example.sachvault.repository.TransactionRepository;

import java.util.List;

public class AccountsViewModel extends AndroidViewModel {
    
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    
    // LiveData for UI observation
    private LiveData<List<Account>> allActiveAccounts;
    private LiveData<List<com.example.sachvault.database.entities.Transaction>> allTransactions;
    private LiveData<Double> totalBalance;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpenses;
    
    public AccountsViewModel(@NonNull Application application) {
        super(application);
        
        accountRepository = new AccountRepository(application);
        transactionRepository = new TransactionRepository(application);
        
        // Initialize LiveData
        allActiveAccounts = accountRepository.getAllActiveAccounts();
        allTransactions = transactionRepository.getAllTransactions();
        totalBalance = accountRepository.getTotalBalance();
        totalIncome = transactionRepository.getTotalIncome();
        totalExpenses = transactionRepository.getTotalExpenses();
    }
    
    // Account operations
    public void insertAccount(Account account) {
        accountRepository.insertAccount(account);
    }
    
    public void updateAccount(Account account) {
        accountRepository.updateAccount(account);
    }
    
    public void deleteAccount(Account account) {
        accountRepository.deleteAccount(account);
    }
    
    public void deactivateAccount(int accountId) {
        accountRepository.deactivateAccount(accountId);
    }
    
    public void activateAccount(int accountId) {
        accountRepository.activateAccount(accountId);
    }
    
    // Getters for UI observation
    public LiveData<List<Account>> getAllActiveAccounts() {
        return allActiveAccounts;
    }
    
    public LiveData<List<com.example.sachvault.database.entities.Transaction>> getAllTransactions() {
        return allTransactions;
    }
    
    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }
    
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }
    
    public LiveData<Double> getTotalExpenses() {
        return totalExpenses;
    }
    
    public LiveData<Account> getAccountById(int accountId) {
        return accountRepository.getAccountById(accountId);
    }
    
    public LiveData<List<Account>> getAccountsByType(String accountType) {
        return accountRepository.getAccountsByType(accountType);
    }
    
    // Statistics
    public LiveData<Integer> getActiveAccountCount() {
        return accountRepository.getActiveAccountCount();
    }
    
    public LiveData<Double> getTotalAssets() {
        return accountRepository.getTotalAssets();
    }
    
    public LiveData<Double> getTotalLiabilities() {
        return accountRepository.getTotalLiabilities();
    }
    
    // Utility methods
    public boolean accountExists(String accountName) {
        return accountRepository.accountExists(accountName);
    }
    
    // Refresh data (useful when returning from other screens)
    public void refreshData() {
        // LiveData automatically refreshes, but we can trigger any manual refreshes here if needed
    }
}