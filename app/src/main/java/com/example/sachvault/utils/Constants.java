package com.example.sachvault.utils;

public class Constants {
    
    // Transaction Types
    public static final String TRANSACTION_TYPE_INCOME = "income";
    public static final String TRANSACTION_TYPE_EXPENSE = "expense";
    public static final String TRANSACTION_TYPE_TRANSFER = "transfer";
    
    // Intent Keys
    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_TRANSACTION_TYPE = "transaction_type";
    public static final String EXTRA_SELECTED_ACCOUNT = "selected_account";
    public static final String EXTRA_SELECTED_CATEGORY = "selected_category";
    
    // Date Formats
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    public static final String TIME_FORMAT_24 = "HH:mm";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String TIME_FORMAT_DISPLAY = "h:mm a";
    public static final String MONTH_PATTERN_FORMAT = "yyyy-MM";
    
    // Currency
    public static final String CURRENCY_SYMBOL = "Rs";
    public static final String CURRENCY_FORMAT = "%s %.2f";
    
    // Preferences
    public static final String PREF_NAME = "sachvault_prefs";
    public static final String PREF_CURRENT_MONTH = "current_month";
    public static final String PREF_SELECTED_ACCOUNT = "selected_account";
    public static final String PREF_TRANSACTION_TYPE = "transaction_type";
    
    // Request Codes
    public static final int REQUEST_CODE_ADD_TRANSACTION = 1001;
    public static final int REQUEST_CODE_EDIT_TRANSACTION = 1002;
    public static final int REQUEST_CODE_SELECT_ACCOUNT = 1003;
    public static final int REQUEST_CODE_SELECT_CATEGORY = 1004;
    
    // Animation Duration
    public static final int ANIMATION_DURATION_SHORT = 150;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;
    
    // Default Values
    public static final double DEFAULT_AMOUNT = 0.0;
    public static final String DEFAULT_NOTE = "";
    public static final int DEFAULT_TRANSACTION_LIMIT = 10;
    
    // Calculator
    public static final String CALCULATOR_DECIMAL_SEPARATOR = ".";
    public static final int CALCULATOR_MAX_DIGITS = 12;
    public static final int CALCULATOR_MAX_DECIMAL_PLACES = 2;
    
    // Error Messages
    public static final String ERROR_AMOUNT_REQUIRED = "Amount is required";
    public static final String ERROR_AMOUNT_INVALID = "Please enter a valid amount";
    public static final String ERROR_CATEGORY_REQUIRED = "Please select a category";
    public static final String ERROR_ACCOUNT_REQUIRED = "Please select an account";
    public static final String ERROR_DATE_INVALID = "Invalid date";
    
    // Success Messages
    public static final String SUCCESS_TRANSACTION_SAVED = "Transaction saved successfully";
    public static final String SUCCESS_TRANSACTION_UPDATED = "Transaction updated successfully";
    public static final String SUCCESS_TRANSACTION_DELETED = "Transaction deleted";
    
    // Navigation
    public static final int NAV_DASHBOARD = 0;
    public static final int NAV_ANALYSIS = 1;
    public static final int NAV_BUDGETS = 2;
    public static final int NAV_ACCOUNTS = 3;
    public static final int NAV_CATEGORIES = 4;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}