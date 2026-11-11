package com.example.sachvault.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sachvault.database.entities.Transaction;
import com.example.sachvault.repository.TransactionRepository;
import com.example.sachvault.utils.DateUtils;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    
    private TransactionRepository repository;
    
    // Current month tracking
    private MutableLiveData<String> currentMonthPattern = new MutableLiveData<>();
    private MutableLiveData<String> currentMonthDisplay = new MutableLiveData<>();
    private int currentYear;
    private int currentMonth;
    
    // LiveData for UI
    private LiveData<List<Transaction>> monthlyTransactions;
    private LiveData<List<Transaction>> recentTransactions;
    private LiveData<Double> monthlyIncome;
    private LiveData<Double> monthlyExpenses;
    private LiveData<Double> monthlyBalance;
    private LiveData<Integer> monthlyTransactionCount;
    
    // Mediator for switching monthly data
    private MediatorLiveData<List<Transaction>> currentMonthTransactions = new MediatorLiveData<>();
    private MediatorLiveData<Double> currentMonthIncome = new MediatorLiveData<>();
    private MediatorLiveData<Double> currentMonthExpenses = new MediatorLiveData<>();
    private MediatorLiveData<Double> currentMonthBalance = new MediatorLiveData<>();
    
    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        
        // Initialize with current month
        int[] currentDate = DateUtils.getCurrentMonthYear();
        currentYear = currentDate[0];
        currentMonth = currentDate[1];
        
        setCurrentMonth(currentYear, currentMonth);
        
        // Get recent transactions (for all time)
        recentTransactions = repository.getRecentTransactions(10);
    }
    
    public void setCurrentMonth(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;
        
        String monthPattern = DateUtils.getMonthPattern(year, month);
        String monthDisplay = DateUtils.getMonthYearDisplay(year, month);
        
        currentMonthPattern.setValue(monthPattern);
        currentMonthDisplay.setValue(monthDisplay);
        
        // Update monthly data sources
        updateMonthlyData(monthPattern);
    }
    
    private void updateMonthlyData(String monthPattern) {
        // Remove previous sources
        if (monthlyTransactions != null) {
            currentMonthTransactions.removeSource(monthlyTransactions);
            currentMonthIncome.removeSource(monthlyIncome);
            currentMonthExpenses.removeSource(monthlyExpenses);
            currentMonthBalance.removeSource(monthlyBalance);
        }
        
        // Get new data sources
        monthlyTransactions = repository.getTransactionsByMonth(monthPattern);
        monthlyIncome = repository.getMonthlyIncome(monthPattern);
        monthlyExpenses = repository.getMonthlyExpenses(monthPattern);
        monthlyBalance = repository.getMonthlyBalance(monthPattern);
        monthlyTransactionCount = repository.getMonthlyTransactionCount(monthPattern);
        
        // Add new sources
        currentMonthTransactions.addSource(monthlyTransactions, transactions -> {
            currentMonthTransactions.setValue(transactions);
        });
        
        currentMonthIncome.addSource(monthlyIncome, income -> {
            currentMonthIncome.setValue(income != null ? income : 0.0);
        });
        
        currentMonthExpenses.addSource(monthlyExpenses, expenses -> {
            currentMonthExpenses.setValue(expenses != null ? expenses : 0.0);
        });
        
        currentMonthBalance.addSource(monthlyBalance, balance -> {
            currentMonthBalance.setValue(balance != null ? balance : 0.0);
        });
    }
    
    public void navigateToPreviousMonth() {
        int[] newDate = DateUtils.addMonths(currentYear, currentMonth, -1);
        setCurrentMonth(newDate[0], newDate[1]);
    }
    
    public void navigateToNextMonth() {
        int[] newDate = DateUtils.addMonths(currentYear, currentMonth, 1);
        setCurrentMonth(newDate[0], newDate[1]);
    }
    
    public void navigateToCurrentMonth() {
        int[] currentDate = DateUtils.getCurrentMonthYear();
        setCurrentMonth(currentDate[0], currentDate[1]);
    }
    
    // Getters for UI observation
    public LiveData<String> getCurrentMonthDisplay() {
        return currentMonthDisplay;
    }
    
    public LiveData<List<Transaction>> getCurrentMonthTransactions() {
        return currentMonthTransactions;
    }
    
    public LiveData<List<Transaction>> getRecentTransactions() {
        return recentTransactions;
    }
    
    public LiveData<Double> getCurrentMonthIncome() {
        return currentMonthIncome;
    }
    
    public LiveData<Double> getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }
    
    public LiveData<Double> getCurrentMonthBalance() {
        return currentMonthBalance;
    }
    
    public LiveData<Integer> getMonthlyTransactionCount() {
        return monthlyTransactionCount;
    }
    
    // Transaction operations
    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }
    
    // Utility methods
    public boolean isCurrentMonth() {
        int[] todayDate = DateUtils.getCurrentMonthYear();
        return currentYear == todayDate[0] && currentMonth == todayDate[1];
    }
    
    public String getCurrentMonthPattern() {
        return currentMonthPattern.getValue();
    }
    
    // Refresh data (useful when returning from other screens)
    public void refreshData() {
        // Trigger refresh by resetting the current month
        setCurrentMonth(currentYear, currentMonth);
    }
}