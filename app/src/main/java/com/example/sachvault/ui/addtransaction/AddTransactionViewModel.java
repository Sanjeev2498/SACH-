package com.example.sachvault.ui.addtransaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sachvault.database.entities.Account;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.database.entities.Transaction;
import com.example.sachvault.repository.AccountRepository;
import com.example.sachvault.repository.CategoryRepository;
import com.example.sachvault.repository.TransactionRepository;
import com.example.sachvault.utils.Constants;
import com.example.sachvault.utils.DateUtils;

import java.util.List;

public class AddTransactionViewModel extends AndroidViewModel {
    
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    
    // Calculator logic
    private CalculatorLogic calculatorLogic = new CalculatorLogic();
    
    // Form data
    private MutableLiveData<String> transactionType = new MutableLiveData<>(Constants.TRANSACTION_TYPE_EXPENSE);
    private MutableLiveData<String> selectedAccount = new MutableLiveData<>();
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private MutableLiveData<String> note = new MutableLiveData<>("");
    private MutableLiveData<String> selectedDate = new MutableLiveData<>(DateUtils.getCurrentDateISO());
    private MutableLiveData<String> selectedTime = new MutableLiveData<>(DateUtils.getCurrentTime24());
    
    // UI state
    private MutableLiveData<String> calculatorDisplay = new MutableLiveData<>("0");
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isTransactionSaved = new MutableLiveData<>();
    
    // Data sources
    private LiveData<List<Account>> allAccounts;
    
    public AddTransactionViewModel(@NonNull Application application) {
        super(application);
        
        transactionRepository = new TransactionRepository(application);
        accountRepository = new AccountRepository(application);
        categoryRepository = new CategoryRepository(application);
        
        // Initialize data sources
        allAccounts = accountRepository.getAllActiveAccounts();
        
        // Initialize calculator display
        calculatorDisplay.setValue(calculatorLogic.getCurrentDisplay());
    }
    
    // Calculator operations
    public void onNumberPressed(String number) {
        calculatorLogic.inputNumber(number);
        updateCalculatorDisplay();
    }
    
    public void onDecimalPressed() {
        calculatorLogic.inputDecimal();
        updateCalculatorDisplay();
    }
    
    public void onOperationPressed(String operation) {
        calculatorLogic.inputOperation(operation);
        updateCalculatorDisplay();
    }
    
    public void onEqualsPressed() {
        calculatorLogic.calculateResult();
        updateCalculatorDisplay();
    }
    
    public void onClearPressed() {
        calculatorLogic.clear();
        updateCalculatorDisplay();
    }
    
    public void onBackspacePressed() {
        calculatorLogic.backspace();
        updateCalculatorDisplay();
    }
    
    private void updateCalculatorDisplay() {
        calculatorDisplay.setValue(calculatorLogic.getCurrentDisplay());
    }
    
    // Form operations
    public void setTransactionType(String type) {
        transactionType.setValue(type);
        // Clear selected category when type changes
        selectedCategory.setValue(null);
    }
    
    public void setSelectedAccount(String account) {
        selectedAccount.setValue(account);
    }
    
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }
    
    public void setNote(String noteText) {
        note.setValue(noteText);
    }
    
    public void setSelectedDate(String date) {
        selectedDate.setValue(date);
    }
    
    public void setSelectedTime(String time) {
        selectedTime.setValue(time);
    }
    
    // Save transaction
    public void saveTransaction() {
        if (!validateTransaction()) {
            return;
        }
        
        isLoading.setValue(true);
        
        Transaction transaction = new Transaction(
            calculatorLogic.getCurrentValue(),
            transactionType.getValue(),
            selectedCategory.getValue(),
            selectedAccount.getValue(),
            note.getValue(),
            selectedDate.getValue(),
            selectedTime.getValue()
        );
        
        transactionRepository.insertTransaction(transaction);
        
        isLoading.setValue(false);
        isTransactionSaved.setValue(true);
    }
    
    private boolean validateTransaction() {
        // Validate amount
        if (!calculatorLogic.isValidAmount()) {
            errorMessage.setValue(Constants.ERROR_AMOUNT_INVALID);
            return false;
        }
        
        // Validate category
        if (selectedCategory.getValue() == null || selectedCategory.getValue().isEmpty()) {
            errorMessage.setValue(Constants.ERROR_CATEGORY_REQUIRED);
            return false;
        }
        
        // Validate account
        if (selectedAccount.getValue() == null || selectedAccount.getValue().isEmpty()) {
            errorMessage.setValue(Constants.ERROR_ACCOUNT_REQUIRED);
            return false;
        }
        
        return true;
    }
    
    // Reset form
    public void resetForm() {
        calculatorLogic.clear();
        updateCalculatorDisplay();
        transactionType.setValue(Constants.TRANSACTION_TYPE_EXPENSE);
        selectedAccount.setValue(null);
        selectedCategory.setValue(null);
        note.setValue("");
        selectedDate.setValue(DateUtils.getCurrentDateISO());
        selectedTime.setValue(DateUtils.getCurrentTime24());
        errorMessage.setValue(null);
        isTransactionSaved.setValue(false);
    }
    
    // Getters for UI observation
    public LiveData<String> getTransactionType() {
        return transactionType;
    }
    
    public LiveData<String> getSelectedAccount() {
        return selectedAccount;
    }
    
    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }
    
    public LiveData<String> getNote() {
        return note;
    }
    
    public LiveData<String> getSelectedDate() {
        return selectedDate;
    }
    
    public LiveData<String> getSelectedTime() {
        return selectedTime;
    }
    
    public LiveData<String> getCalculatorDisplay() {
        return calculatorDisplay;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsTransactionSaved() {
        return isTransactionSaved;
    }
    
    public LiveData<List<Account>> getAllAccounts() {
        return allAccounts;
    }
    
    public LiveData<List<Category>> getCategoriesByType(String type) {
        if ("expense".equalsIgnoreCase(type)) {
            return categoryRepository.getExpenseCategories();
        } else if ("income".equalsIgnoreCase(type)) {
            return categoryRepository.getIncomeCategories();
        } else {
            return categoryRepository.getAllActiveCategories();
        }
    }
    
    // Utility methods
    public boolean hasUnsavedChanges() {
        return calculatorLogic.getCurrentValue() > 0 ||
               (selectedAccount.getValue() != null && !selectedAccount.getValue().isEmpty()) ||
               (selectedCategory.getValue() != null && !selectedCategory.getValue().isEmpty()) ||
               (note.getValue() != null && !note.getValue().isEmpty());
    }
    
    public void clearError() {
        errorMessage.setValue(null);
    }
}