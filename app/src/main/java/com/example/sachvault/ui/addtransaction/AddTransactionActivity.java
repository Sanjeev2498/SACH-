package com.example.sachvault.ui.addtransaction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sachvault.R;
import com.example.sachvault.databinding.ActivityAddTransactionBinding;
import com.example.sachvault.utils.DateUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {
    
    private ActivityAddTransactionBinding binding;
    private AddTransactionViewModel viewModel;
    private String selectedTransactionType = "expense"; // default
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);
        
        setupHeader();
        setupTransactionTypeButtons();
        setupCalculator();
        setupSelectors();
        setupNoteInput();
        setupDateTimeSelectors();
        observeViewModel();
    }
    
    private void setupHeader() {
        // Close button
        binding.btnClose.setOnClickListener(v -> finish());
        
        // Save button with validation
        binding.btnSave.setOnClickListener(v -> {
            if (validateTransactionData()) {
                saveTransaction();
            }
        });
    }
    
    private boolean validateTransactionData() {
        // Validate amount
        if (currentExpression.isEmpty() || currentExpression.equals("0")) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Check if expression contains operators (incomplete calculation)
        if (currentExpression.contains("+") || currentExpression.contains("-") || 
            currentExpression.contains("Ã—") || currentExpression.contains("Ã·")) {
            Toast.makeText(this, "Please complete the calculation by pressing equals (=)", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate amount is a valid number
        try {
            double amount = Double.parseDouble(currentExpression);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate account selection
        if (binding.tvSelectedAccount.getText().toString().equals("Select Account")) {
            Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validate category selection
        if (binding.tvSelectedCategory.getText().toString().equals("Select Category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void saveTransaction() {
        // Use ViewModel to save the transaction to database
        try {
            // Parse the current amount from calculator display
            double amount = 0.0;
            try {
                amount = Double.parseDouble(currentExpression);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get database-compatible date and time formats
            String dbDate = (String) binding.tvSelectedDate.getTag();
            String dbTime = (String) binding.tvSelectedTime.getTag();
            
            // Fallback to current date/time if tags are null
            if (dbDate == null || dbTime == null) {
                Calendar now = Calendar.getInstance();
                dbDate = android.text.format.DateFormat.format("yyyy-MM-dd", now).toString();
                dbTime = android.text.format.DateFormat.format("HH:mm", now).toString();
            }
            
            String selectedAccount = binding.tvSelectedAccount.getText().toString();
            String selectedCategory = binding.tvSelectedCategory.getText().toString();
            
            // Create transaction directly with current form data
            com.example.sachvault.database.entities.Transaction transaction = 
                new com.example.sachvault.database.entities.Transaction(
                    amount,
                    selectedTransactionType,
                    selectedCategory,
                    selectedAccount,
                    binding.etNotes.getText().toString().trim(),
                    dbDate,  // Use database format
                    dbTime   // Use database format
                );
            
            // Save using TransactionRepository directly
            com.example.sachvault.repository.TransactionRepository repository = 
                new com.example.sachvault.repository.TransactionRepository(getApplication());
            
            // Insert transaction
            repository.insertTransaction(transaction);
            
            // SYNC: Update account balance based on transaction type
            syncAccountBalance(selectedAccount, amount, selectedTransactionType);
            
            // SYNC: Update budget spent amount if expense
            if ("expense".equals(selectedTransactionType)) {
                syncBudgetSpent(selectedCategory, amount, dbDate);
            }
            
            // Show success message
            Toast.makeText(this, "Transaction saved successfully!", Toast.LENGTH_SHORT).show();
            
            // Force UI refresh by sending result
            setResult(RESULT_OK);
            
            // Finish activity - this will return to Records page which should refresh
            finish();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error saving transaction: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    /**
     * SYNC: Update account balance when transaction is saved
     */
    private void syncAccountBalance(String accountName, double amount, String transactionType) {
        try {
            com.example.sachvault.repository.AccountRepository accountRepository = 
                new com.example.sachvault.repository.AccountRepository(getApplication());
            
            // Get account by name and update balance
            com.example.sachvault.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    com.example.sachvault.database.AppDatabase database = 
                        com.example.sachvault.database.AppDatabase.getDatabase(getApplicationContext());
                    
                    com.example.sachvault.database.entities.Account account = 
                        database.accountDao().getAccountByNameSync(accountName);
                    
                    if (account != null) {
                        double newBalance = account.getBalance();
                        
                        // Update balance based on transaction type
                        if ("income".equals(transactionType)) {
                            newBalance += amount;
                        } else if ("expense".equals(transactionType)) {
                            newBalance -= amount;
                        }
                        
                        // Update account balance
                        database.accountDao().updateAccountBalance(account.getId(), newBalance);
                        
                        android.util.Log.d("AddTransaction", "âœ… SYNCED: Account " + accountName + 
                            " balance updated from " + account.getBalance() + " to " + newBalance);
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddTransaction", "Error syncing account balance: " + e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            android.util.Log.e("AddTransaction", "Error setting up account sync: " + e.getMessage(), e);
        }
    }
    
    /**
     * SYNC: Update budget spent amount when expense is saved
     */
    private void syncBudgetSpent(String categoryName, double amount, String transactionDate) {
        try {
            // Parse month and year from transaction date
            String[] dateParts = transactionDate.split("-");
            if (dateParts.length >= 2) {
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                
                com.example.sachvault.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        com.example.sachvault.database.AppDatabase database = 
                            com.example.sachvault.database.AppDatabase.getDatabase(getApplicationContext());
                        
                        // Get category ID first
                        com.example.sachvault.database.entities.Category category = 
                            database.categoryDao().getCategoryByNameSync(categoryName);
                        
                        if (category != null) {
                            // Check if budget exists for this category and month
                            com.example.sachvault.database.entities.Budget budget = 
                                database.budgetDao().getBudgetByCategoryAndMonthSync(
                                    category.getId(), month, year);
                            
                            if (budget != null) {
                                // Update spent amount
                                double newSpent = budget.getSpent() + amount;
                                database.budgetDao().updateSpentAmount(budget.getId(), newSpent);
                                
                                android.util.Log.d("AddTransaction", "SYNCED: Budget for " + categoryName + 
                                    " updated, spent: " + newSpent);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AddTransaction", "Error syncing budget: " + e.getMessage(), e);
                    }
                });
            }
            
        } catch (Exception e) {
            android.util.Log.e("AddTransaction", "Error setting up budget sync: " + e.getMessage(), e);
        }
    }
    
    private void setupTransactionTypeButtons() {
        // Set default selection
        selectTransactionType("expense");
        
        binding.btnIncome.setOnClickListener(v -> selectTransactionType("income"));
        binding.btnExpense.setOnClickListener(v -> selectTransactionType("expense"));  
        binding.btnTransfer.setOnClickListener(v -> selectTransactionType("transfer"));
    }
    
    private void selectTransactionType(String type) {
        selectedTransactionType = type;
        
        // Reset all buttons
        binding.btnIncome.setSelected(false);
        binding.btnExpense.setSelected(false);
        binding.btnTransfer.setSelected(false);
        
        // Select the appropriate button
        switch (type) {
            case "income":
                binding.btnIncome.setSelected(true);
                break;
            case "expense":
                binding.btnExpense.setSelected(true);
                break;
            case "transfer":
                binding.btnTransfer.setSelected(true);
                break;
        }
        
        // Update categories based on selection
        updateCategoriesForType(type);
    }
    
    private void updateCategoriesForType(String type) {
        // Reset category selection
        binding.tvSelectedCategory.setText("Select Category");
        
        // TODO: Load appropriate categories based on type
        Toast.makeText(this, "Selected: " + type, Toast.LENGTH_SHORT).show();
    }
    
    private void setupCalculator() {
        // Number buttons
        binding.btn0.setOnClickListener(v -> appendToDisplay("0"));
        binding.btn1.setOnClickListener(v -> appendToDisplay("1"));
        binding.btn2.setOnClickListener(v -> appendToDisplay("2"));
        binding.btn3.setOnClickListener(v -> appendToDisplay("3"));
        binding.btn4.setOnClickListener(v -> appendToDisplay("4"));
        binding.btn5.setOnClickListener(v -> appendToDisplay("5"));
        binding.btn6.setOnClickListener(v -> appendToDisplay("6"));
        binding.btn7.setOnClickListener(v -> appendToDisplay("7"));
        binding.btn8.setOnClickListener(v -> appendToDisplay("8"));
        binding.btn9.setOnClickListener(v -> appendToDisplay("9"));
        
        // Decimal point
        binding.btnDecimal.setOnClickListener(v -> appendToDisplay("."));
        
        // Operations
        binding.btnAdd.setOnClickListener(v -> appendToDisplay("+"));
        binding.btnSubtract.setOnClickListener(v -> appendToDisplay("-"));
        binding.btnMultiply.setOnClickListener(v -> appendToDisplay("Ã—"));
        binding.btnDivide.setOnClickListener(v -> appendToDisplay("Ã·"));
        
        // Special buttons
        binding.btnClear.setOnClickListener(v -> clearDisplay());
        binding.btnBackspace.setOnClickListener(v -> backspace());
        binding.btnEquals.setOnClickListener(v -> calculate());
    }
    
    private String currentExpression = "";
    
    private void appendToDisplay(String value) {
        // Prevent multiple decimal points
        if (value.equals(".")) {
            String[] parts = currentExpression.split("[+\\-Ã—Ã·]");
            String lastPart = parts.length > 0 ? parts[parts.length - 1] : "";
            if (lastPart.contains(".")) {
                return; // Already has decimal point in current number
            }
        }
        
        // Prevent multiple operators in a row
        if (isOperator(value) && !currentExpression.isEmpty()) {
            char lastChar = currentExpression.charAt(currentExpression.length() - 1);
            if (isOperator(String.valueOf(lastChar))) {
                // Replace the last operator with the new one
                currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
            }
        }
        
        currentExpression += value;
        updateAmountDisplay();
    }
    
    private boolean isOperator(String value) {
        return value.equals("+") || value.equals("-") || value.equals("Ã—") || value.equals("Ã·");
    }
    
    private void clearDisplay() {
        currentExpression = "";
        updateAmountDisplay();
    }
    
    private void backspace() {
        if (!currentExpression.isEmpty()) {
            currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
            updateAmountDisplay();
        }
    }
    
    private void calculate() {
        try {
            if (currentExpression.isEmpty()) {
                return;
            }
            
            // Replace display symbols with actual operators
            String expression = currentExpression
                .replace("Ã—", "*")
                .replace("Ã·", "/");
            
            // Simple expression evaluator for basic calculations
            double result = evaluateExpression(expression);
            
            // Update display with result
            currentExpression = String.valueOf(result);
            
            // Remove trailing .0 for whole numbers
            if (currentExpression.endsWith(".0")) {
                currentExpression = currentExpression.substring(0, currentExpression.length() - 2);
            }
            
            updateAmountDisplay();
            
        } catch (Exception e) {
            Toast.makeText(this, "Invalid calculation", Toast.LENGTH_SHORT).show();
            clearDisplay();
        }
    }
    
    // Simple expression evaluator for basic arithmetic
    private double evaluateExpression(String expression) throws Exception {
        // Remove spaces
        expression = expression.replaceAll("\\s+", "");
        
        // Handle simple cases first
        if (!expression.contains("+") && !expression.contains("-") && 
            !expression.contains("*") && !expression.contains("/")) {
            return Double.parseDouble(expression);
        }
        
        // Basic evaluation (handles +, -, *, / with proper precedence)
        return evaluateBasicExpression(expression);
    }
    
    // Simple recursive descent parser for basic arithmetic
    private double evaluateBasicExpression(String expr) throws Exception {
        // Split by + and - (lowest precedence)
        java.util.List<String> parts = new java.util.ArrayList<>();
        java.util.List<Character> operators = new java.util.ArrayList<>();
        
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '+' || c == '-') {
                if (current.length() == 0 && c == '-') {
                    current.append(c); // Handle negative numbers
                } else {
                    parts.add(current.toString());
                    operators.add(c);
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        
        // Evaluate multiplication and division first
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, String.valueOf(evaluateMultDiv(parts.get(i))));
        }
        
        // Then evaluate addition and subtraction
        double result = Double.parseDouble(parts.get(0));
        for (int i = 0; i < operators.size(); i++) {
            double operand = Double.parseDouble(parts.get(i + 1));
            if (operators.get(i) == '+') {
                result += operand;
            } else {
                result -= operand;
            }
        }
        
        return result;
    }
    
    private double evaluateMultDiv(String expr) throws Exception {
        java.util.List<String> parts = new java.util.ArrayList<>();
        java.util.List<Character> operators = new java.util.ArrayList<>();
        
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '*' || c == '/') {
                parts.add(current.toString());
                operators.add(c);
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        
        double result = Double.parseDouble(parts.get(0));
        for (int i = 0; i < operators.size(); i++) {
            double operand = Double.parseDouble(parts.get(i + 1));
            if (operators.get(i) == '*') {
                result *= operand;
            } else {
                if (operand == 0) throw new Exception("Division by zero");
                result /= operand;
            }
        }
        
        return result;
    }
    
    private void updateAmountDisplay() {
        String displayText = currentExpression.isEmpty() ? "0" : currentExpression;
        binding.tvAmountDisplay.setText(displayText);
        
        // Update save button state based on whether calculation is complete
        boolean hasIncompleteExpression = displayText.contains("+") || displayText.contains("-") || 
                                        displayText.contains("Ã—") || displayText.contains("Ã·");
        
        // Optionally change save button appearance for incomplete expressions
        if (hasIncompleteExpression) {
            binding.btnSave.setAlpha(0.6f);
        } else {
            binding.btnSave.setAlpha(1.0f);
        }
    }
    
    private void setupSelectors() {
        // Account selector
        binding.layoutAccountSelector.setOnClickListener(v -> showAccountBottomSheet());
        
        // Category selector  
        binding.layoutCategorySelector.setOnClickListener(v -> showCategoryBottomSheet());
    }
    
    private void showAccountBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        
        // Create dynamic layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(0xFF1E1E1E);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Select an Account");
        title.setTextSize(20);
        title.setTextColor(0xFFFFFFFF);
        title.setPadding(16, 16, 16, 32);
        title.setGravity(android.view.Gravity.CENTER);
        layout.addView(title);
        
        // Show loading message initially
        TextView loading = new TextView(this);
        loading.setText("Loading accounts...");
        loading.setTextColor(0xFFAAAAAA);
        loading.setGravity(android.view.Gravity.CENTER);
        loading.setPadding(16, 32, 16, 32);
        layout.addView(loading);
        
        bottomSheetDialog.setContentView(layout);
        bottomSheetDialog.show();
        
        // Load accounts from database dynamically
        com.example.sachvault.repository.AccountRepository accountRepository = 
            new com.example.sachvault.repository.AccountRepository(getApplication());
        
        accountRepository.getAllActiveAccounts().observe(this, accounts -> {
            if (!bottomSheetDialog.isShowing()) return;
            
            // Clear previous items
            layout.removeAllViews();
            layout.addView(title); // Re-add title
            
            if (accounts != null && !accounts.isEmpty()) {
                for (com.example.sachvault.database.entities.Account account : accounts) {
                    // Create account item
                    LinearLayout item = new LinearLayout(this);
                    item.setOrientation(LinearLayout.HORIZONTAL);
                    item.setPadding(24, 24, 24, 24);
                    item.setClickable(true);
                    item.setFocusable(true);
                    item.setBackground(getDrawable(android.R.drawable.list_selector_background));
                    
                    // Account name
                    TextView nameText = new TextView(this);
                    nameText.setText(account.getName());
                    nameText.setTextSize(16);
                    nameText.setTextColor(0xFFFFFFFF);
                    nameText.setLayoutParams(new LinearLayout.LayoutParams(0, 
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    
                    // Account balance
                    TextView balanceText = new TextView(this);
                    balanceText.setText(String.format("Rs %.2f", account.getBalance()));
                    balanceText.setTextSize(14);
                    balanceText.setTextColor(0xFF4EDEA3);
                    
                    item.addView(nameText);
                    item.addView(balanceText);
                    
                    item.setOnClickListener(v -> {
                        binding.tvSelectedAccount.setText(account.getName());
                        bottomSheetDialog.dismiss();
                    });
                    
                    layout.addView(item);
                }
            } else {
                TextView empty = new TextView(this);
                empty.setText("No accounts found\nAdd accounts first");
                empty.setTextColor(0xFFAAAAAA);
                empty.setGravity(android.view.Gravity.CENTER);
                empty.setPadding(16, 32, 16, 32);
                layout.addView(empty);
            }
        });
    }
    
    private void showCategoryBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_category_selector, null);
        
        // Get the container layout
        LinearLayout layout = bottomSheetView.findViewById(R.id.layout_category_list);
        layout.removeAllViews(); // Clear any existing views
        
        // Load categories from database dynamically
        viewModel.getCategoriesByType(selectedTransactionType).observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                for (com.example.sachvault.database.entities.Category category : categories) {
                    // Create category item
                    View item = getLayoutInflater().inflate(R.layout.item_category, null);
                    
                    // Set icon
                    android.widget.ImageView icon = item.findViewById(R.id.iv_category_icon);
                    String iconName = category.getIcon() != null ? category.getIcon() : "restaurant";
                    int iconRes = getIconResource(iconName);
                    icon.setImageResource(iconRes);
                    
                    // Set category name
                    TextView name = item.findViewById(R.id.tv_category_name);
                    name.setText(category.getName());
                    
                    // Set type
                    TextView type = item.findViewById(R.id.tv_category_type);
                    type.setText(category.getType().toUpperCase());
                    
                    // Set click listener
                    item.setOnClickListener(v -> {
                        binding.tvSelectedCategory.setText(category.getName());
                        bottomSheetDialog.dismiss();
                    });
                    
                    layout.addView(item);
                }
            } else {
                TextView empty = new TextView(this);
                empty.setText("No categories found\nAdd categories first");
                empty.setTextColor(0xFFAAAAAA);
                empty.setGravity(android.view.Gravity.CENTER);
                empty.setPadding(16, 32, 16, 32);
                layout.addView(empty);
            }
        });
        
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    
    private int getIconResource(String iconName) {
        // Map icon names to drawable resources
        switch (iconName.toLowerCase()) {
            case "restaurant":
            case "food":
                return R.drawable.ic_restaurant_24;
            case "directions_car":
            case "transport":
                return R.drawable.ic_directions_car_24;
            case "home":
            case "housing":
                return R.drawable.ic_home_24;
            case "shopping_bag":
            case "shopping":
                return R.drawable.ic_shopping_bag_24;
            case "movie":
            case "entertainment":
                return R.drawable.ic_movie_24;
            case "school":
            case "education":
                return R.drawable.ic_receipt_24; // Use receipt as fallback
            case "bills":
            case "receipt":
                return R.drawable.ic_receipt_24;
            case "smartphone":
            case "phone":
                return R.drawable.ic_smartphone_24;
            case "beauty":
                return R.drawable.ic_receipt_24; // Use receipt as fallback
            case "work":
                return R.drawable.ic_work_24;
            case "business":
                return R.drawable.ic_business_24;
            case "trending_up":
            case "investment":
                return R.drawable.ic_trending_up_24;
            case "bonus":
            case "payments":
                return R.drawable.ic_payments_24;
            case "freelance":
            case "paid":
                return R.drawable.ic_payments_24; // Use payments as fallback
            default:
                return R.drawable.ic_receipt_24;
        }
    }
    
    private void setupNoteInput() {
        binding.etNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // TODO: Handle note text changes
            }
        });
    }
    
    private void setupDateTimeSelectors() {
        // Set current date and time as default
        Calendar calendar = Calendar.getInstance();
        updateDateDisplay(calendar);
        updateTimeDisplay(calendar);
        
        // Date selector
        binding.layoutDateSelector.setOnClickListener(v -> showDatePicker());
        
        // Time selector
        binding.layoutTimeSelector.setOnClickListener(v -> showTimePicker());
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                updateDateDisplay(selectedDate);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay(selectedTime);
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        );
        timePickerDialog.show();
    }
    
    private void updateDateDisplay(Calendar calendar) {
        // Format date for display
        String displayDate = android.text.format.DateFormat.format("MMM dd, yyyy", calendar).toString();
        binding.tvSelectedDate.setText(displayDate);
        
        // Store the actual date in YYYY-MM-DD format for database compatibility
        String dbDate = android.text.format.DateFormat.format("yyyy-MM-dd", calendar).toString();
        binding.tvSelectedDate.setTag(dbDate); // Store database format as tag
    }
    
    private void updateTimeDisplay(Calendar calendar) {
        // Format time for display
        String displayTime = android.text.format.DateFormat.format("h:mm a", calendar).toString();
        binding.tvSelectedTime.setText(displayTime);
        
        // Store the actual time in HH:mm format for database compatibility
        String dbTime = android.text.format.DateFormat.format("HH:mm", calendar).toString();
        binding.tvSelectedTime.setTag(dbTime); // Store database format as tag
    }
    
    private void observeViewModel() {
        // TODO: Implement ViewModel observations
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}