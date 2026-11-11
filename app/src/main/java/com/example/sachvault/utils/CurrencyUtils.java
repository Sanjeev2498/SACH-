package com.example.sachvault.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    
    // Use English locale to ensure proper formatting without locale-specific issues
    private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat AMOUNT_FORMAT;
    
    static {
        // Initialize with English locale symbols to avoid encoding issues
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        
        CURRENCY_FORMAT = new DecimalFormat("#,##0.00", symbols);
        AMOUNT_FORMAT = new DecimalFormat("0.00", symbols);
    }
    
    /**
     * Format amount with currency symbol (Rs 1,234.56)
     */
    public static String formatCurrency(double amount) {
        return Constants.CURRENCY_SYMBOL + " " + String.format(Locale.ENGLISH, "%.2f", amount);
    }
    
    /**
     * Format amount with currency symbol and proper formatting
     */
    public static String formatCurrencyWithCommas(double amount) {
        return Constants.CURRENCY_SYMBOL + " " + CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Format amount without currency symbol (1234.56)
     */
    public static String formatAmount(double amount) {
        return AMOUNT_FORMAT.format(amount);
    }
    
    /**
     * Format amount with commas but no currency symbol (1,234.56)
     */
    public static String formatAmountWithCommas(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }
    
    /**
     * Parse string to double amount
     */
    public static double parseAmount(String amountString) {
        try {
            // Remove currency symbols and commas
            String cleanAmount = amountString.replaceAll("[^0-9.-]", "");
            return Double.parseDouble(cleanAmount);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Validate if string is a valid amount
     */
    public static boolean isValidAmount(String amountString) {
        try {
            double amount = parseAmount(amountString);
            return amount > 0 && amount <= 999999999.99; // Reasonable upper limit
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Format calculator display amount (no currency symbol, proper decimal places)
     */
    public static String formatCalculatorDisplay(double amount) {
        if (amount == (long) amount) {
            // If it's a whole number, don't show decimal places
            return String.format(Locale.ENGLISH, "%.0f", amount);
        } else {
            // Show up to 2 decimal places, removing trailing zeros
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            DecimalFormat df = new DecimalFormat("0.##", symbols);
            return df.format(amount);
        }
    }
    
    /**
     * Round amount to 2 decimal places
     */
    public static double roundAmount(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
    
    /**
     * Get formatted percentage string
     */
    public static String formatPercentage(double percentage) {
        return String.format(Locale.ENGLISH, "%.1f%%", percentage);
    }
    
    /**
     * Calculate percentage of total
     */
    public static double calculatePercentage(double amount, double total) {
        if (total == 0) return 0;
        return (amount / total) * 100;
    }
    
    /**
     * Get color-coded amount string for transaction type
     */
    public static String getColorCodedAmount(double amount, String transactionType) {
        String formattedAmount = formatCurrencyWithCommas(Math.abs(amount));
        
        switch (transactionType) {
            case Constants.TRANSACTION_TYPE_INCOME:
                return "+" + formattedAmount;
            case Constants.TRANSACTION_TYPE_EXPENSE:
                return "-" + formattedAmount;
            default:
                return formattedAmount;
        }
    }
    
    /**
     * Get amount with proper sign for display
     */
    public static String getSignedAmount(double amount, String transactionType) {
        String formattedAmount = formatAmount(Math.abs(amount));
        
        switch (transactionType) {
            case Constants.TRANSACTION_TYPE_INCOME:
                return "+" + formattedAmount;
            case Constants.TRANSACTION_TYPE_EXPENSE:
                return "-" + formattedAmount;
            default:
                return formattedAmount;
        }
    }
    
    /**
     * Format large amounts with K, M abbreviations
     */
    public static String formatLargeAmount(double amount) {
        if (Math.abs(amount) >= 1000000) {
            return String.format(Locale.ENGLISH, "%.1fM", amount / 1000000);
        } else if (Math.abs(amount) >= 1000) {
            return String.format(Locale.ENGLISH, "%.1fK", amount / 1000);
        } else {
            return formatAmount(amount);
        }
    }
    
    /**
     * Safely add two amounts (handles floating point precision)
     */
    public static double safeAdd(double a, double b) {
        return roundAmount(a + b);
    }
    
    /**
     * Safely subtract two amounts (handles floating point precision)
     */
    public static double safeSubtract(double a, double b) {
        return roundAmount(a - b);
    }
    
    /**
     * Check if amount is zero (within small tolerance)
     */
    public static boolean isZero(double amount) {
        return Math.abs(amount) < 0.001;
    }
}