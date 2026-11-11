package com.example.sachvault.ui.addtransaction;

import com.example.sachvault.utils.Constants;

import java.text.DecimalFormat;

public class CalculatorLogic {
    
    private StringBuilder currentInput = new StringBuilder("0");
    private double previousValue = 0;
    private String pendingOperation = "";
    private boolean isNewInput = true;
    
    private static final DecimalFormat DISPLAY_FORMAT = new DecimalFormat("0.##");
    
    public String getCurrentDisplay() {
        if (currentInput.length() == 0 || currentInput.toString().equals("")) {
            return "0";
        }
        
        try {
            double value = Double.parseDouble(currentInput.toString());
            if (value == (long) value) {
                return String.valueOf((long) value);
            } else {
                return DISPLAY_FORMAT.format(value);
            }
        } catch (NumberFormatException e) {
            return currentInput.toString();
        }
    }
    
    public double getCurrentValue() {
        try {
            return Double.parseDouble(currentInput.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public void inputNumber(String number) {
        if (isNewInput || currentInput.toString().equals("0")) {
            currentInput = new StringBuilder(number);
            isNewInput = false;
        } else {
            // Check maximum digits
            if (currentInput.length() < Constants.CALCULATOR_MAX_DIGITS) {
                currentInput.append(number);
            }
        }
    }
    
    public void inputDecimal() {
        if (isNewInput) {
            currentInput = new StringBuilder("0.");
            isNewInput = false;
        } else if (!currentInput.toString().contains(Constants.CALCULATOR_DECIMAL_SEPARATOR)) {
            currentInput.append(Constants.CALCULATOR_DECIMAL_SEPARATOR);
        }
    }
    
    public void inputOperation(String operation) {
        if (!pendingOperation.isEmpty()) {
            calculateResult();
        }
        
        previousValue = getCurrentValue();
        pendingOperation = operation;
        isNewInput = true;
    }
    
    public void calculateResult() {
        if (pendingOperation.isEmpty()) {
            return;
        }
        
        double currentValue = getCurrentValue();
        double result = previousValue;
        
        switch (pendingOperation) {
            case "+":
                result = previousValue + currentValue;
                break;
            case "-":
                result = previousValue - currentValue;
                break;
            case "Ã—":
                result = previousValue * currentValue;
                break;
            case "Ã·":
                if (currentValue != 0) {
                    result = previousValue / currentValue;
                } else {
                    // Division by zero - keep previous value
                    result = previousValue;
                }
                break;
        }
        
        // Round to avoid floating point precision issues
        result = Math.round(result * 100.0) / 100.0;
        
        // Ensure result is not negative (amounts should be positive)
        result = Math.max(0, result);
        
        currentInput = new StringBuilder(String.valueOf(result));
        pendingOperation = "";
        isNewInput = true;
    }
    
    public void clear() {
        currentInput = new StringBuilder("0");
        previousValue = 0;
        pendingOperation = "";
        isNewInput = true;
    }
    
    public void backspace() {
        if (!isNewInput && currentInput.length() > 0) {
            if (currentInput.length() == 1) {
                currentInput = new StringBuilder("0");
                isNewInput = true;
            } else {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        }
    }
    
    public void setValue(double value) {
        currentInput = new StringBuilder(String.valueOf(value));
        previousValue = 0;
        pendingOperation = "";
        isNewInput = true;
    }
    
    public boolean isValidAmount() {
        double value = getCurrentValue();
        return value > 0 && value <= 999999999.99;
    }
    
    public String getFormattedAmount() {
        return DISPLAY_FORMAT.format(getCurrentValue());
    }
}