package com.example.sachvault.ui.dashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.databinding.FragmentDashboardSimpleBinding;
import com.example.sachvault.database.entities.Transaction;
import com.example.sachvault.repository.TransactionRepository;
import com.example.sachvault.ui.addtransaction.AddTransactionActivity;
import com.example.sachvault.ui.adapters.TransactionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SimpleDashboardFragment extends Fragment {
    
    private static final String TAG = "SimpleDashboardFragment";
    private FragmentDashboardSimpleBinding binding;
    private Calendar currentCalendar;
    private SimpleDateFormat monthFormat;
    private TransactionRepository transactionRepository;
    private TransactionAdapter transactionAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardSimpleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize calendar to current date
        currentCalendar = Calendar.getInstance();
        monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        
        // Initialize repository with error handling
        try {
            transactionRepository = new TransactionRepository(requireActivity().getApplication());
            Log.d(TAG, "Repository initialized successfully");
            
            // Log database diagnostic info
            com.example.sachvault.database.AppDatabase.logDatabaseInfo(requireContext());
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repository: " + e.getMessage(), e);
            // Continue without repository for now
        }
        
        setupUI();
        setupRecyclerView();
        setupClickListeners();
        
        // Simple initialization - just load existing data
        loadDataForCurrentMonth();
    }
    
    private void setupUI() {
        updateMonthDisplay();
        
        // Initial loading state
        binding.tvTotalExpense.setText("Loading...");
        binding.tvTotalIncome.setText("Loading...");
        binding.tvTotalBalance.setText("Loading...");
        
        // Show empty state initially
        showEmptyState();
    }
    
    private void setupRecyclerView() {
        try {
            transactionAdapter = new TransactionAdapter();
            binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerTransactions.setAdapter(transactionAdapter);
            
            // Set click listener for transactions
            transactionAdapter.setOnItemClickListener(transaction -> {
                if (transaction != null && getContext() != null) {
                    // Simple click handling
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
        }
    }
    
    private void setupClickListeners() {
        // Previous month button
        binding.btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadDataForCurrentMonth();
        });
        
        // Next month button
        binding.btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadDataForCurrentMonth();
        });
        
        // Add Transaction button (header + icon)
        binding.btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
            startActivity(intent);
        });
        
        // Menu button
        binding.btnMenu.setOnClickListener(v -> {
            // Open navigation drawer
            if (getActivity() instanceof com.example.sachvault.MainActivity) {
                com.example.sachvault.MainActivity mainActivity = 
                    (com.example.sachvault.MainActivity) getActivity();
                if (mainActivity != null) {
                    androidx.drawerlayout.widget.DrawerLayout drawerLayout = 
                        mainActivity.findViewById(com.example.sachvault.R.id.drawer_layout);
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                    }
                }
            }
        });
    }
    
    private void updateMonthDisplay() {
        // Format: SEPTEMBER 2026
        String monthYear = monthFormat.format(currentCalendar.getTime()).toUpperCase();
        binding.tvCurrentMonth.setText(monthYear);
    }
    
    private void loadDataForCurrentMonth() {
        try {
            Log.d(TAG, "=== DATABASE INFO ===");
            Log.d(TAG, "Loading transactions for current month");
            
            // Get current month and year for filtering
            String currentMonth = String.format(Locale.getDefault(), "%02d", currentCalendar.get(Calendar.MONTH) + 1);
            String currentYear = String.valueOf(currentCalendar.get(Calendar.YEAR));
            
            Log.d(TAG, "Filtering for month: " + currentMonth + "/" + currentYear);
            
            // Log database information
            if (getContext() != null) {
                String dbPath = getContext().getDatabasePath("sachvault_database").getAbsolutePath();
                Log.d(TAG, "Database path: " + dbPath);
                Log.d(TAG, "Database exists: " + getContext().getDatabasePath("sachvault_database").exists());
            }
            
            // Show loading state
            binding.tvTotalIncome.setText("Loading...");
            binding.tvTotalExpense.setText("Loading...");
            binding.tvTotalBalance.setText("Loading...");
            
            // Check if repository is available
            if (transactionRepository == null) {
                Log.w(TAG, "Repository is null, showing empty state");
                showEmptyState();
                setZeroTotals();
                return;
            }
            
            // Get ALL transactions and filter by current month
            transactionRepository.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
                try {
                    Log.d(TAG, "=== TRANSACTION DATA ===");
                    Log.d(TAG, "Received total transactions: " + (transactions != null ? transactions.size() : 0));
                    
                    List<Transaction> filteredTransactions = new ArrayList<>();
                    
                    if (transactions != null && !transactions.isEmpty()) {
                        // Filter transactions by current month/year
                        for (Transaction transaction : transactions) {
                            if (transaction != null && transaction.getDate() != null) {
                                try {
                                    // Parse transaction date (format: YYYY-MM-DD)
                                    String transactionDate = transaction.getDate();
                                    String[] dateParts = transactionDate.split("-");
                                    
                                    if (dateParts.length >= 2) {
                                        String transactionYear = dateParts[0];
                                        String transactionMonth = dateParts[1];
                                        
                                        // Check if transaction belongs to current month/year
                                        if (transactionYear.equals(currentYear) && transactionMonth.equals(currentMonth)) {
                                            filteredTransactions.add(transaction);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Error parsing transaction date: " + transaction.getDate());
                                }
                            }
                        }
                        
                        Log.d(TAG, "Filtered transactions for " + currentMonth + "/" + currentYear + ": " + filteredTransactions.size());
                        
                        if (filteredTransactions.size() > 0) {
                            Log.d(TAG, "Sample filtered transactions:");
                            for (int i = 0; i < Math.min(3, filteredTransactions.size()); i++) {
                                Transaction t = filteredTransactions.get(i);
                                Log.d(TAG, "  " + (i+1) + ": " + t.getType() + " Rs" + t.getAmount() + " - " + t.getCategory() + " (" + t.getDate() + ")");
                            }
                        }
                    }
                    
                    if (!filteredTransactions.isEmpty()) {
                        // Process filtered transactions
                        updateTransactionsDisplay(filteredTransactions);
                        calculateAndDisplayTotals(filteredTransactions);
                        showTransactions();
                        
                        Log.d(TAG, "UI updated with " + filteredTransactions.size() + " transactions for current month");
                    } else {
                        Log.d(TAG, "No transactions found for current month - showing empty state");
                        showEmptyState();
                        setZeroTotals();
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error processing transactions: " + e.getMessage());
                    showEmptyState();
                    setZeroTotals();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading data: " + e.getMessage());
            showEmptyState();
            setZeroTotals();
        }
    }
    
    private void updateTransactionsDisplay(List<Transaction> transactions) {
        try {
            if (transactionAdapter != null) {
                transactionAdapter.submitList(transactions);
                Log.d(TAG, "Transactions submitted to adapter");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating transactions display: " + e.getMessage());
        }
    }
    
    private void calculateAndDisplayTotals(List<Transaction> transactions) {
        try {
            double totalIncome = 0.0;
            double totalExpense = 0.0;
            
            for (Transaction transaction : transactions) {
                if (transaction != null) {
                    String type = transaction.getType();
                    double amount = transaction.getAmount();
                    
                    if ("income".equals(type)) {
                        totalIncome += amount;
                    } else if ("expense".equals(type)) {
                        totalExpense += amount;
                    }
                }
            }
            
            double totalBalance = totalIncome - totalExpense;
            
            // Update UI with Indian number formatting
            binding.tvTotalIncome.setText(formatIndianCurrency(totalIncome));
            binding.tvTotalExpense.setText(formatIndianCurrency(totalExpense));
            binding.tvTotalBalance.setText(formatIndianCurrency(totalBalance));
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating totals: " + e.getMessage());
            setZeroTotals();
        }
    }
    
    private String formatIndianCurrency(double amount) {
        try {
            // Create Indian number format (##,##,##0.00)
            java.text.DecimalFormat indianFormatter = new java.text.DecimalFormat("##,##,##0.00");
            return indianFormatter.format(amount);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting currency: " + e.getMessage());
            return String.format(Locale.getDefault(), "%.2f", amount);
        }
    }
    
    private void setZeroTotals() {
        binding.tvTotalIncome.setText("0.00");
        binding.tvTotalExpense.setText("0.00");
        binding.tvTotalBalance.setText("0.00");
    }
    
    private void showEmptyState() {
        binding.recyclerTransactions.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void showTransactions() {
        binding.recyclerTransactions.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }
    
    public void refreshData() {
        Log.d(TAG, "Refresh data called");
        if (binding != null) {
            // Simple refresh without heavy operations
            loadDataForCurrentMonth();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Fragment resumed - refreshing data");
        // Refresh data when returning to this fragment
        if (binding != null) {
            loadDataForCurrentMonth();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}