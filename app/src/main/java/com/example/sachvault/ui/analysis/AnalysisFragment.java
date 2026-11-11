package com.example.sachvault.ui.analysis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.databinding.FragmentAnalysisBinding;
import com.example.sachvault.database.entities.Transaction;
import com.example.sachvault.repository.AccountRepository;
import com.example.sachvault.repository.TransactionRepository;
import com.example.sachvault.ui.addtransaction.AddTransactionActivity;
import com.example.sachvault.ui.adapters.CategoryBreakdownAdapter;
import com.example.sachvault.ui.adapters.PieChartLegendAdapter;
import com.example.sachvault.utils.CurrencyUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AnalysisFragment extends Fragment {
    
    private static final String TAG = "AnalysisFragment";
    private FragmentAnalysisBinding binding;
    private Calendar currentCalendar;
    private SimpleDateFormat monthFormat;
    private TransactionRepository transactionRepository;
    private CategoryBreakdownAdapter categoryAdapter;
    private PieChartLegendAdapter legendAdapter;
    private List<String> categoryOrder = new ArrayList<>(); // Track category order for click handling
    private List<Transaction> allFilteredTransactions = new ArrayList<>(); // Store all filtered transactions
    private Map<String, Double> currentExpensesByCategory = new HashMap<>(); // Store current month's expenses by category
    private Map<String, Double> currentIncomeBySource = new HashMap<>(); // Store current month's income by source
    private double currentTotalExpense = 0.0;
    private double currentTotalIncome = 0.0;
    private AccountRepository accountRepository;
    
    // Tab management
    private enum AnalysisTab { EXPENSE, INCOME, ACCOUNT }
    private AnalysisTab currentTab = AnalysisTab.EXPENSE;
    
    // Dynamic color generation for unlimited categories
    private static final int[] BASE_COLORS = {
        Color.parseColor("#FF6B6B"), // Red
        Color.parseColor("#4ECDC4"), // Teal  
        Color.parseColor("#45B7D1"), // Blue
        Color.parseColor("#96CEB4"), // Green
        Color.parseColor("#FECA57"), // Yellow
        Color.parseColor("#FF9FF3"), // Pink
        Color.parseColor("#54A0FF"), // Light Blue
        Color.parseColor("#5F27CD"), // Purple
        Color.parseColor("#00D2D3"), // Cyan
        Color.parseColor("#FF9F43"), // Orange
        Color.parseColor("#10AC84"), // Mint
        Color.parseColor("#EE5A6F"), // Rose
        Color.parseColor("#A55EEA"), // Violet
        Color.parseColor("#26D0CE"), // Aqua
        Color.parseColor("#FD79A8"), // Magenta
        Color.parseColor("#FDCB6E"), // Peach
        Color.parseColor("#6C5CE7"), // Indigo
        Color.parseColor("#FD79A8"), // Coral
        Color.parseColor("#A29BFE"), // Periwinkle
        Color.parseColor("#00B894")  // Emerald
    };
    
    // Cache for generated colors to maintain consistency
    private Map<String, Integer> categoryColorCache = new HashMap<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false);
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
            accountRepository = new com.example.sachvault.repository.AccountRepository(requireActivity().getApplication());
            Log.d(TAG, "Repository initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repository: " + e.getMessage(), e);
        }
        
        setupUI();
        setupPieChart();
        setupRecyclerView();
        setupClickListeners();
        setupTabButtons();
        
        // Load data
        loadAnalysisData();
        loadAccountData();
        
        // Test: Show category card after 3 seconds to verify layout
        binding.getRoot().postDelayed(() -> {
            Log.d(TAG, "Testing category card visibility...");
            binding.tvSelectedCategoryName.setText("Test Category");
            binding.tvSelectedCategoryPercentage.setText("50.0% of total expenses");
            binding.tvSelectedCategoryAmount.setText(CurrencyUtils.formatCurrencyWithCommas(25000.00));
            binding.progressSelectedCategory.setProgress(50);
            binding.tvTransactionCount.setText("10 transactions this month");
            binding.selectedCategoryCard.setVisibility(View.VISIBLE);
            Log.d(TAG, "Test category card should now be visible!");
        }, 3000);
    }
    
    private void setupUI() {
        updateMonthDisplay();
        
        // Initial loading state
        binding.tvTotalExpense.setText("Loading...");
        binding.tvTotalIncome.setText("Loading...");
        binding.tvTotalBalance.setText("Loading...");
        
        // Show empty states initially
        showEmptyStates();
    }
    
    private void setupPieChart() {
        PieChart pieChart = binding.pieChart;
        
        // Configure pie chart appearance  
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(10, 10, 10, 10);
        
        // Configure hole in center - LARGER for better text visibility
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#122131"));
        pieChart.setHoleRadius(45f); // Increased from 35f to 45f
        pieChart.setTransparentCircleRadius(50f); // Increased from 40f to 50f
        
        // Configure text in center - BIGGER text
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Expenses\nBreakdown");
        pieChart.setCenterTextSize(16f); // Increased from 14f to 16f
        pieChart.setCenterTextColor(Color.parseColor("#d4e4fa"));
        
        // Configure rotation and animation
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        
        // DISABLE built-in legend - we use custom right-side legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        
        // Click listener - show category spending details
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                try {
                    Log.d(TAG, "=== PIE CHART SEGMENT CLICKED ===");
                    Log.d(TAG, "Entry: " + e.toString());
                    Log.d(TAG, "Highlight index: " + h.getX());
                    
                    if (e instanceof PieEntry) {
                        PieEntry pieEntry = (PieEntry) e;
                        float percentage = pieEntry.getValue();
                        
                        Log.d(TAG, "PieEntry percentage: " + percentage);
                        
                        // Get category name from our legend data (since PieEntry has empty label)
                        String category = getCategoryFromIndex((int)h.getX());
                        
                        Log.d(TAG, "Category from index: " + category);
                        
                        if (category != null) {
                            // Update center text with selected segment info
                            pieChart.setCenterText(String.format(Locale.getDefault(), 
                                "%s\n%.1f%%", category, percentage));
                            
                            Log.d(TAG, "About to show category spending details for: " + category);
                            
                            // Show category spending details
                            showCategorySpendingDetails(category, percentage);
                        } else {
                            Log.e(TAG, "Category is NULL for index: " + (int)h.getX());
                        }
                    } else {
                        Log.e(TAG, "Entry is not PieEntry: " + e.getClass().getSimpleName());
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error in onValueSelected: " + ex.getMessage(), ex);
                }
            }
            
            @Override
            public void onNothingSelected() {
                Log.d(TAG, "Nothing selected - hiding category details");
                binding.pieChart.setCenterText("Expenses\nBreakdown");
                hideCategorySpendingDetails();
            }
        });
        
        // Animate chart
        pieChart.animateY(1000);
    }
    
    private void setupRecyclerView() {
        try {
            Log.d(TAG, "=== SETTING UP RECYCLERVIEW ===");
            
            // Category breakdown adapter
            categoryAdapter = new CategoryBreakdownAdapter();
            binding.recyclerCategoryBreakdown.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerCategoryBreakdown.setAdapter(categoryAdapter);
            Log.d(TAG, "Category breakdown adapter set up");
            
            // Pie chart legend adapter
            legendAdapter = new PieChartLegendAdapter();
            binding.recyclerPieChartLegend.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerPieChartLegend.setAdapter(legendAdapter);
            Log.d(TAG, "Legend adapter set up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
        }
    }
    
    private void setupClickListeners() {
        // Previous month button
        binding.btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadAnalysisData();
        });
        
        // Next month button
        binding.btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadAnalysisData();
        });
        
        // Add Transaction button
        binding.btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
            startActivity(intent);
        });
        
        // Menu button
        binding.btnMenu.setOnClickListener(v -> {
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
    
    private void loadAnalysisData() {
        try {
            Log.d(TAG, "Loading analysis data for current month");
            
            // Get current month and year for filtering
            String currentMonth = String.format(Locale.getDefault(), "%02d", currentCalendar.get(Calendar.MONTH) + 1);
            String currentYear = String.valueOf(currentCalendar.get(Calendar.YEAR));
            
            Log.d(TAG, "Filtering analysis for month: " + currentMonth + "/" + currentYear);
            
            // Show loading state
            binding.tvTotalIncome.setText("Loading...");
            binding.tvTotalExpense.setText("Loading...");
            binding.tvTotalBalance.setText("Loading...");
            
            if (transactionRepository == null) {
                Log.w(TAG, "Repository is null, showing empty state");
                showEmptyStates();
                setZeroTotals();
                return;
            }
            
            // Get ALL transactions and filter by current month
            transactionRepository.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
                try {
                    Log.d(TAG, "Received total transactions for analysis: " + (transactions != null ? transactions.size() : 0));
                    
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
                                    Log.w(TAG, "Error parsing transaction date for analysis: " + transaction.getDate());
                                }
                            }
                        }
                        
                        Log.d(TAG, "Filtered analysis transactions for " + currentMonth + "/" + currentYear + ": " + filteredTransactions.size());
                    }
                    
                    if (!filteredTransactions.isEmpty()) {
                        // Store filtered transactions for category selection
                        allFilteredTransactions = new ArrayList<>(filteredTransactions);
                        
                        // Process filtered transactions for analysis
                        processTransactionsForAnalysis(filteredTransactions);
                    } else {
                        Log.d(TAG, "No transactions found for current month in analysis");
                        allFilteredTransactions.clear();
                        showEmptyStates();
                        setZeroTotals();
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error processing analysis data: " + e.getMessage());
                    showEmptyStates();
                    setZeroTotals();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading analysis data: " + e.getMessage());
            showEmptyStates();
            setZeroTotals();
        }
    }
    
    /**
     * Enhanced data processing with performance optimizations for large datasets
     */
    private void processTransactionsForAnalysis(List<Transaction> transactions) {
        try {
            Log.d(TAG, "Processing " + transactions.size() + " transactions for analysis");
            
            double totalIncome = 0.0;
            double totalExpense = 0.0;
            Map<String, Double> expensesByCategory = new HashMap<>();
            Map<String, Double> incomeBySource = new HashMap<>();
            
            // Use StringBuilder for efficient category processing
            Set<String> uniqueExpenseCategories = new HashSet<>();
            Set<String> uniqueIncomeCategories = new HashSet<>();
            
            // Single pass processing for efficiency
            for (Transaction transaction : transactions) {
                if (transaction == null) continue;
                
                String type = transaction.getType();
                if (type == null) continue;
                
                double amount = transaction.getAmount();
                String category = transaction.getCategory();
                
                // Normalize category name
                if (category == null || category.trim().isEmpty()) {
                    category = "Uncategorized";
                }
                category = category.trim();
                
                if ("income".equals(type)) {
                    totalIncome += amount;
                    uniqueIncomeCategories.add(category);
                    
                    // Group income by source - use merge for efficiency
                    incomeBySource.merge(category, amount, Double::sum);
                } else if ("expense".equals(type)) {
                    totalExpense += amount;
                    uniqueExpenseCategories.add(category);
                    
                    // Group expenses by category - use merge for efficiency
                    expensesByCategory.merge(category, amount, Double::sum);
                }
            }
            
            double totalBalance = totalIncome - totalExpense;
            
            Log.d(TAG, "Found " + uniqueExpenseCategories.size() + " unique expense categories");
            Log.d(TAG, "Found " + uniqueIncomeCategories.size() + " unique income sources");
            Log.d(TAG, "Total income: " + totalIncome + ", Total expense: " + totalExpense);
            
            // Store current data for category selection
            currentExpensesByCategory = new HashMap<>(expensesByCategory);
            currentTotalExpense = totalExpense;
            currentIncomeBySource = new HashMap<>(incomeBySource);
            currentTotalIncome = totalIncome;
            
            // Update UI components
            updateFinancialSummary(totalIncome, totalExpense, totalBalance);
            updatePieChart(expensesByCategory, totalExpense);
            updateCategoryBreakdown(expensesByCategory);
            updateIncomePieChart(incomeBySource, totalIncome);
            updateIncomeBreakdown(incomeBySource);
            
            // Show appropriate views based on data availability
            if (expensesByCategory.isEmpty()) {
                showEmptyStates();
            } else {
                showDataViews();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing transactions for analysis: " + e.getMessage(), e);
            showEmptyStates();
            setZeroTotals();
        }
    }
    
    private void updateFinancialSummary(double totalIncome, double totalExpense, double totalBalance) {
        // Format numbers with Indian numbering system
        binding.tvTotalIncome.setText(formatIndianCurrency(totalIncome));
        binding.tvTotalExpense.setText(formatIndianCurrency(totalExpense));
        binding.tvTotalBalance.setText(formatIndianCurrency(totalBalance));
        
        // Set balance color based on positive/negative
        if (totalBalance >= 0) {
            binding.tvTotalBalance.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_income, null));
        } else {
            binding.tvTotalBalance.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_expense, null));
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
    
    /**
     * Enhanced pie chart with better handling for many categories
     */
    private void updatePieChart(Map<String, Double> expensesByCategory, double totalExpense) {
        PieChart pieChart = binding.pieChart;
        
        if (expensesByCategory.isEmpty() || totalExpense == 0) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data available");
            pieChart.setNoDataTextColor(Color.parseColor("#c6c6cd"));
            return;
        }
        
        Log.d(TAG, "Updating pie chart with " + expensesByCategory.size() + " categories");
        
        // Sort categories by amount (descending) for better visual hierarchy
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(expensesByCategory.entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // Handle many categories by grouping smaller ones
        ArrayList<PieEntry> entries = new ArrayList<>();
        List<PieChartLegendAdapter.LegendItem> legendItems = new ArrayList<>();
        categoryOrder.clear();
        
        double otherAmount = 0.0;
        int maxDirectCategories = Math.min(10, sortedEntries.size()); // Show max 10 categories directly
        
        // Add main categories
        for (int i = 0; i < maxDirectCategories; i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            float percentage = (float) ((entry.getValue() / totalExpense) * 100);
            
            // Only show categories with significant percentage (>1%) in pie chart
            if (percentage >= 1.0f || i < 5) { // Always show top 5, then only >1%
                entries.add(new PieEntry(percentage, ""));
                legendItems.add(new PieChartLegendAdapter.LegendItem(entry.getKey(), entry.getValue()));
                categoryOrder.add(entry.getKey());
            } else {
                otherAmount += entry.getValue();
            }
        }
        
        // Group remaining smaller categories as "Others"
        if (maxDirectCategories < sortedEntries.size() || otherAmount > 0) {
            for (int i = maxDirectCategories; i < sortedEntries.size(); i++) {
                otherAmount += sortedEntries.get(i).getValue();
            }
            
            if (otherAmount > 0) {
                float otherPercentage = (float) ((otherAmount / totalExpense) * 100);
                entries.add(new PieEntry(otherPercentage, ""));
                legendItems.add(new PieChartLegendAdapter.LegendItem("Others", otherAmount));
                categoryOrder.add("Others");
            }
        }
        
        // Create dataset with optimized settings
        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // Generate colors for categories (including "Others")
        List<String> categoryList = new ArrayList<>();
        for (PieChartLegendAdapter.LegendItem item : legendItems) {
            categoryList.add(item.getCategory());
        }
        List<Integer> colors = generateCategoryColors(categoryList);
        dataSet.setColors(colors);
        
        // Optimize appearance for many categories
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(entries.size() > 8 ? 1f : 3f); // Less space for many categories
        dataSet.setSelectionShift(entries.size() > 8 ? 5f : 8f); // Less selection shift
        
        // Completely disable all labels and values on pie slices
        dataSet.setValueTextSize(0f);
        dataSet.setDrawValues(false);
        dataSet.setValueTextColor(Color.TRANSPARENT);
        dataSet.setValueLineColor(Color.TRANSPARENT);
        dataSet.setValueLinePart1OffsetPercentage(0f);
        dataSet.setValueLinePart1Length(0f);
        dataSet.setValueLinePart2Length(0f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        
        // Create pie data
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.TRANSPARENT);
        
        // Apply to chart
        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();
        
        // Update legend with sorted data
        if (legendAdapter != null) {
            legendAdapter.updateData(legendItems);
        }
        
        Log.d(TAG, "Pie chart updated with " + entries.size() + " visible segments (including " + 
              (otherAmount > 0 ? "Others group" : "all categories") + ")");
    }
    
    /**
     * Enhanced category breakdown with pagination support for many categories
     */
    private void updateCategoryBreakdown(Map<String, Double> expensesByCategory) {
        try {
            Log.d(TAG, "Updating category breakdown with " + expensesByCategory.size() + " categories");
            
            // Convert map to list for adapter with null safety
            List<CategoryBreakdownItem> breakdownItems = new ArrayList<>();
            
            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    breakdownItems.add(new CategoryBreakdownItem(
                        entry.getKey().trim(), 
                        entry.getValue()
                    ));
                }
            }
            
            // Sort by amount (highest first) for better UX
            breakdownItems.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
            
            // For very large lists, consider showing top N with "Show More" option
            if (breakdownItems.size() > 50) {
                Log.d(TAG, "Large category list detected (" + breakdownItems.size() + 
                      " items), performance optimizations applied");
                // Note: Could implement pagination here in the future if needed
            }
            
            // Update adapter
            if (categoryAdapter != null) {
                categoryAdapter.updateData(breakdownItems);
                Log.d(TAG, "Category breakdown updated with " + breakdownItems.size() + " items");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating category breakdown: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update income pie chart
     */
    private void updateIncomePieChart(Map<String, Double> incomeBySource, double totalIncome) {
        try {
            PieChart pieChart = binding.incomePieChart;
            
            if (incomeBySource.isEmpty() || totalIncome == 0) {
                // Show empty state
                pieChart.setVisibility(View.GONE);
                binding.incomeEmptyState.setVisibility(View.VISIBLE);
                return;
            }
            
            pieChart.setVisibility(View.VISIBLE);
            binding.incomeEmptyState.setVisibility(View.GONE);
            
            // Prepare data entries
            List<PieEntry> entries = new ArrayList<>();
            List<String> incomeSourceOrder = new ArrayList<>();
            
            for (Map.Entry<String, Double> entry : incomeBySource.entrySet()) {
                float percentage = (float) ((entry.getValue() / totalIncome) * 100);
                entries.add(new PieEntry(percentage, entry.getKey()));
                incomeSourceOrder.add(entry.getKey());
            }
            
            // Generate colors for income sources (using green-ish tones)
            List<Integer> colors = new ArrayList<>();
            int[] incomeColors = {
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4DD0E1"), // Light Cyan
                Color.parseColor("#26A69A"), // Medium Teal
                Color.parseColor("#66BB6A"), // Medium Green
                Color.parseColor("#81C784")  // Soft Green
            };
            
            for (int i = 0; i < entries.size(); i++) {
                colors.add(incomeColors[i % incomeColors.length]);
            }
            
            // Create dataset
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setSliceSpace(2f);
            dataSet.setSelectionShift(8f);
            
            // Create pie data
            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
            pieChart.invalidate();
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating income pie chart: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update income breakdown list
     */
    private void updateIncomeBreakdown(Map<String, Double> incomeBySource) {
        try {
            if (incomeBySource.isEmpty()) {
                binding.incomeBreakdownRecycler.setVisibility(View.GONE);
                binding.incomeEmptyState.setVisibility(View.VISIBLE);
                return;
            }
            
            binding.incomeBreakdownRecycler.setVisibility(View.VISIBLE);
            binding.incomeEmptyState.setVisibility(View.GONE);
            
            // Create breakdown items
            List<CategoryBreakdownItem> items = new ArrayList<>();
            for (Map.Entry<String, Double> entry : incomeBySource.entrySet()) {
                items.add(new CategoryBreakdownItem(entry.getKey(), entry.getValue()));
            }
            
            // Sort by amount descending
            items.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
            
            // Setup adapter if needed
            if (binding.incomeBreakdownRecycler.getAdapter() == null) {
                CategoryBreakdownAdapter adapter = new CategoryBreakdownAdapter();
                binding.incomeBreakdownRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.incomeBreakdownRecycler.setAdapter(adapter);
            }
            
            // Update data
            ((CategoryBreakdownAdapter) binding.incomeBreakdownRecycler.getAdapter()).updateData(items, true); // true = income
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating income breakdown: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load account distribution data
     */
    private void loadAccountData() {
        try {
            if (accountRepository == null) {
                Log.w(TAG, "Account repository not initialized");
                return;
            }
            
            accountRepository.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
                if (accounts != null && !accounts.isEmpty()) {
                    processAccountsForAnalysis(accounts);
                } else {
                    showAccountEmptyState();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading account data: " + e.getMessage(), e);
            showAccountEmptyState();
        }
    }
    
    /**
     * Process accounts for distribution chart
     */
    private void processAccountsForAnalysis(List<com.example.sachvault.database.entities.Account> accounts) {
        try {
            Map<String, Double> accountBalances = new HashMap<>();
            double totalBalance = 0.0;
            
            // Include ALL accounts (positive, negative, or zero balance)
            for (com.example.sachvault.database.entities.Account account : accounts) {
                accountBalances.put(account.getName(), account.getBalance());
                totalBalance += account.getBalance();
            }
            
            updateAccountPieChart(accountBalances, totalBalance);
            updateAccountBreakdown(accountBalances);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing accounts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update account distribution pie chart
     */
    private void updateAccountPieChart(Map<String, Double> accountBalances, double totalBalance) {
        try {
            PieChart pieChart = binding.accountPieChart;
            
            if (accountBalances.isEmpty()) {
                pieChart.setVisibility(View.GONE);
                binding.accountEmptyState.setVisibility(View.VISIBLE);
                return;
            }
            
            pieChart.setVisibility(View.VISIBLE);
            binding.accountEmptyState.setVisibility(View.GONE);
            
            // Use absolute values for pie chart (can't show negatives in pie chart)
            // Calculate total of absolute values for percentage calculation
            double totalAbsolute = 0.0;
            for (Double balance : accountBalances.values()) {
                totalAbsolute += Math.abs(balance);
            }
            
            // Prepare data entries using absolute values
            List<PieEntry> entries = new ArrayList<>();
            
            for (Map.Entry<String, Double> entry : accountBalances.entrySet()) {
                // Use absolute value for pie chart percentage
                double absValue = Math.abs(entry.getValue());
                float percentage = (float) ((absValue / totalAbsolute) * 100);
                entries.add(new PieEntry(percentage, entry.getKey()));
            }
            
            // Generate colors for accounts (using blue-ish tones)
            List<Integer> colors = new ArrayList<>();
            int[] accountColors = {
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#03A9F4"), // Light Blue
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#FF5722")  // Deep Orange
            };
            
            for (int i = 0; i < entries.size(); i++) {
                colors.add(accountColors[i % accountColors.length]);
            }
            
            // Configure pie chart (similar to expense chart)
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(10, 10, 10, 10);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.parseColor("#122131"));
            pieChart.setHoleRadius(45f);
            pieChart.setTransparentCircleRadius(50f);
            pieChart.setDrawCenterText(true);
            pieChart.setCenterText("Account\nDistribution");
            pieChart.setCenterTextSize(16f);
            pieChart.setCenterTextColor(Color.parseColor("#d4e4fa"));
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(false);
            Legend legend = pieChart.getLegend();
            legend.setEnabled(false);
            
            // Create dataset
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setSliceSpace(2f);
            
            // Create pie data
            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
            pieChart.invalidate();
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating account pie chart: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update account breakdown list
     */
    private void updateAccountBreakdown(Map<String, Double> accountBalances) {
        try {
            if (accountBalances.isEmpty()) {
                binding.accountBreakdownRecycler.setVisibility(View.GONE);
                binding.accountEmptyState.setVisibility(View.VISIBLE);
                return;
            }
            
            binding.accountBreakdownRecycler.setVisibility(View.VISIBLE);
            binding.accountEmptyState.setVisibility(View.GONE);
            
            // Create breakdown items
            List<CategoryBreakdownItem> items = new ArrayList<>();
            for (Map.Entry<String, Double> entry : accountBalances.entrySet()) {
                items.add(new CategoryBreakdownItem(entry.getKey(), entry.getValue()));
            }
            
            // Sort by balance descending (but keep negative values)
            items.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
            
            // Setup adapter if needed
            if (binding.accountBreakdownRecycler.getAdapter() == null) {
                CategoryBreakdownAdapter adapter = new CategoryBreakdownAdapter();
                binding.accountBreakdownRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.accountBreakdownRecycler.setAdapter(adapter);
            }
            
            // Update data - pass isAccount=true to show accounts with proper colors
            ((CategoryBreakdownAdapter) binding.accountBreakdownRecycler.getAdapter()).updateData(items, false, true);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating account breakdown: " + e.getMessage(), e);
        }
    }
    
    private void showAccountEmptyState() {
        binding.accountPieChart.setVisibility(View.GONE);
        binding.accountBreakdownRecycler.setVisibility(View.GONE);
        binding.accountEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void setZeroTotals() {
        binding.tvTotalIncome.setText("0.00");
        binding.tvTotalExpense.setText("0.00");
        binding.tvTotalBalance.setText("0.00");
        hideCategorySpendingDetails();
    }
    
    private void showEmptyStates() {
        binding.pieChartEmptyState.setVisibility(View.VISIBLE);
        binding.pieChart.setVisibility(View.GONE);
        binding.recyclerPieChartLegend.setVisibility(View.GONE);
        binding.tvClickInstruction.setVisibility(View.GONE);
        binding.categoryEmptyState.setVisibility(View.VISIBLE);
        binding.recyclerCategoryBreakdown.setVisibility(View.GONE);
        binding.selectedCategoryCard.setVisibility(View.GONE);
    }
    
    private void showDataViews() {
        binding.pieChartEmptyState.setVisibility(View.GONE);
        binding.pieChart.setVisibility(View.VISIBLE);
        binding.recyclerPieChartLegend.setVisibility(View.VISIBLE);
        binding.tvClickInstruction.setVisibility(View.VISIBLE);
        binding.categoryEmptyState.setVisibility(View.GONE);
        binding.recyclerCategoryBreakdown.setVisibility(View.VISIBLE);
    }
    
    /**
     * Enhanced category spending details with support for grouped categories
     */
    private void showCategorySpendingDetails(String category, float percentage) {
        try {
            Log.d(TAG, "=== SHOWING CATEGORY SPENDING DETAILS ===");
            Log.d(TAG, "Category: " + category + ", Percentage: " + percentage);
            
            // Handle "Others" group differently
            if ("Others".equals(category)) {
                showOthersGroupDetails(percentage);
                return;
            }
            
            // Get the amount for this specific category
            double categoryAmount = currentExpensesByCategory.getOrDefault(category, 0.0);
            Log.d(TAG, "Category amount: " + categoryAmount);
            
            // Count transactions for this category efficiently
            int transactionCount = (int) allFilteredTransactions.stream()
                .filter(transaction -> transaction != null && 
                       "expense".equals(transaction.getType()) && 
                       category.equals(transaction.getCategory()))
                .count();
            
            Log.d(TAG, "Transaction count: " + transactionCount);
            
            // Update UI with category details
            updateCategoryDetailsUI(category, categoryAmount, percentage, transactionCount, false);
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing category spending details: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle "Others" group details - show summary of grouped categories
     */
    private void showOthersGroupDetails(float percentage) {
        try {
            // Calculate total amount for "Others" group
            double othersAmount = currentTotalExpense * (percentage / 100.0);
            
            // Count categories in Others group (categories not in main pie chart)
            Set<String> mainCategories = new HashSet<>(categoryOrder);
            mainCategories.remove("Others"); // Remove "Others" itself
            
            int othersCount = (int) currentExpensesByCategory.entrySet().stream()
                .filter(entry -> !mainCategories.contains(entry.getKey()))
                .count();
            
            // Update UI for Others group
            binding.tvSelectedCategoryName.setText("Others");
            binding.tvSelectedCategoryPercentage.setText(
                String.format(Locale.getDefault(), "%.1f%% of total expenses", percentage));
            
            binding.tvSelectedCategoryAmount.setText(CurrencyUtils.formatCurrencyWithCommas(othersAmount));
            
            binding.progressSelectedCategory.setProgress((int) percentage);
            
            String countText = othersCount == 1 ? 
                "1 category grouped" : 
                othersCount + " categories grouped";
            binding.tvTransactionCount.setText(countText);
            
            // Set a generic group icon
            binding.ivSelectedCategoryIcon.setImageResource(com.example.sachvault.R.drawable.ic_receipt_24);
            
            // Show the card
            binding.selectedCategoryCard.setVisibility(View.VISIBLE);
            binding.selectedCategoryCard.requestLayout();
            
            Log.d(TAG, "Others group details shown: " + othersCount + " categories, amount: " + CurrencyUtils.formatCurrencyWithCommas(othersAmount));
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing Others group details: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update category details UI - unified method
     */
    private void updateCategoryDetailsUI(String category, double amount, float percentage, 
                                       int transactionCount, boolean isOthersGroup) {
        try {
            // Update category name
            binding.tvSelectedCategoryName.setText(category);
            
            // Update percentage
            binding.tvSelectedCategoryPercentage.setText(
                String.format(Locale.getDefault(), "%.1f%% of total expenses", percentage));
            
            // Update amount with Indian formatting
            binding.tvSelectedCategoryAmount.setText(CurrencyUtils.formatCurrencyWithCommas(amount));
            
            // Update progress bar
            binding.progressSelectedCategory.setProgress((int) percentage);
            
            // Update transaction count
            String countText = transactionCount == 1 ? 
                "1 transaction this month" : 
                transactionCount + " transactions this month";
            binding.tvTransactionCount.setText(countText);
            
            // Set appropriate icon
            if (isOthersGroup) {
                binding.ivSelectedCategoryIcon.setImageResource(com.example.sachvault.R.drawable.ic_receipt_24);
            } else {
                setCategoryIcon(category);
            }
            
            // Show the card with animation
            binding.selectedCategoryCard.setVisibility(View.VISIBLE);
            binding.selectedCategoryCard.requestLayout();
            binding.getRoot().invalidate();
            
            Log.d(TAG, "Category details updated for: " + category);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating category details UI: " + e.getMessage(), e);
        }
    }
    
    private void hideCategorySpendingDetails() {
        binding.selectedCategoryCard.setVisibility(View.GONE);
    }
    
    private String getCategoryFromIndex(int index) {
        try {
            if (index >= 0 && index < categoryOrder.size()) {
                return categoryOrder.get(index);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category from index: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Enhanced category icon mapping with fallback for new categories
     */
    private void setCategoryIcon(String category) {
        int iconRes = com.example.sachvault.R.drawable.ic_receipt_24; // Default icon
        
        if (category == null || category.trim().isEmpty()) {
            binding.ivSelectedCategoryIcon.setImageResource(iconRes);
            return;
        }
        
        // Normalize category name for better matching
        String normalizedCategory = category.toLowerCase().trim();
        
        // Enhanced category to icon mapping
        if (normalizedCategory.contains("food") || normalizedCategory.contains("dining") || 
            normalizedCategory.contains("restaurant") || normalizedCategory.contains("meal")) {
            iconRes = com.example.sachvault.R.drawable.ic_restaurant_24;
        } else if (normalizedCategory.contains("transport") || normalizedCategory.contains("travel") || 
                   normalizedCategory.contains("car") || normalizedCategory.contains("fuel") ||
                   normalizedCategory.contains("gas") || normalizedCategory.contains("taxi")) {
            iconRes = com.example.sachvault.R.drawable.ic_directions_car_24;
        } else if (normalizedCategory.contains("shop") || normalizedCategory.contains("retail") ||
                   normalizedCategory.contains("clothing") || normalizedCategory.contains("purchase")) {
            iconRes = com.example.sachvault.R.drawable.ic_shopping_bag_24;
        } else if (normalizedCategory.contains("housing") || normalizedCategory.contains("home") ||
                   normalizedCategory.contains("rent") || normalizedCategory.contains("mortgage")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("bill") || normalizedCategory.contains("utility") ||
                   normalizedCategory.contains("electric") || normalizedCategory.contains("water") ||
                   normalizedCategory.contains("internet") || normalizedCategory.contains("phone")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_long_24;
        } else if (normalizedCategory.contains("entertainment") || normalizedCategory.contains("movie") ||
                   normalizedCategory.contains("game") || normalizedCategory.contains("leisure")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("health") || normalizedCategory.contains("medical") ||
                   normalizedCategory.contains("doctor") || normalizedCategory.contains("pharmacy")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("beauty") || normalizedCategory.contains("personal") ||
                   normalizedCategory.contains("care") || normalizedCategory.contains("salon")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("education") || normalizedCategory.contains("school") ||
                   normalizedCategory.contains("book") || normalizedCategory.contains("course")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("gift") || normalizedCategory.contains("donation") ||
                   normalizedCategory.contains("charity")) {
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        } else if (normalizedCategory.contains("investment") || normalizedCategory.contains("saving") ||
                   normalizedCategory.contains("bank") || normalizedCategory.contains("finance")) {
            iconRes = com.example.sachvault.R.drawable.ic_account_balance_wallet_24;
        } else {
            // For completely new categories, use a generic but appropriate icon
            iconRes = com.example.sachvault.R.drawable.ic_receipt_24;
        }
        
        binding.ivSelectedCategoryIcon.setImageResource(iconRes);
        Log.d(TAG, "Set icon for category '" + category + "': " + iconRes);
    }
    
    private void setupTabButtons() {
        // Set initial tab state
        selectTab(AnalysisTab.EXPENSE);
        
        // Expense tab click
        binding.btnExpenseAnalysis.setOnClickListener(v -> {
            selectTab(AnalysisTab.EXPENSE);
        });
        
        // Income tab click
        binding.btnIncomeAnalysis.setOnClickListener(v -> {
            selectTab(AnalysisTab.INCOME);
        });
        
        // Account tab click
        binding.btnAccountAnalysis.setOnClickListener(v -> {
            selectTab(AnalysisTab.ACCOUNT);
        });
    }
    
    private void selectTab(AnalysisTab tab) {
        currentTab = tab;
        
        // Reset all button states
        binding.btnExpenseAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_surface_container, null));
        binding.btnExpenseAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_surface_variant, null));
        
        binding.btnIncomeAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_surface_container, null));
        binding.btnIncomeAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_surface_variant, null));
        
        binding.btnAccountAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_surface_container, null));
        binding.btnAccountAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_surface_variant, null));
        
        // Hide all content sections
        binding.expenseAnalysisContent.setVisibility(View.GONE);
        binding.incomeAnalysisContent.setVisibility(View.GONE);
        binding.accountAnalysisContent.setVisibility(View.GONE);
        
        // Show selected tab and update button state
        switch (tab) {
            case EXPENSE:
                binding.btnExpenseAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_primary, null));
                binding.btnExpenseAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_primary, null));
                binding.expenseAnalysisContent.setVisibility(View.VISIBLE);
                Log.d(TAG, "Switched to Expense Analysis");
                break;
            case INCOME:
                binding.btnIncomeAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_primary, null));
                binding.btnIncomeAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_primary, null));
                binding.incomeAnalysisContent.setVisibility(View.VISIBLE);
                Log.d(TAG, "Switched to Income Analysis");
                break;
            case ACCOUNT:
                binding.btnAccountAnalysis.setBackgroundColor(getResources().getColor(com.example.sachvault.R.color.color_primary, null));
                binding.btnAccountAnalysis.setTextColor(getResources().getColor(com.example.sachvault.R.color.color_on_primary, null));
                binding.accountAnalysisContent.setVisibility(View.VISIBLE);
                Log.d(TAG, "Switched to Account Analysis");
                break;
        }
    }
    
    /**
     * Generate a unique, consistent color for each category
     * Uses HSV color space to ensure good contrast and variety
     */
    private int getColorForCategory(String category) {
        // Check cache first for consistency
        if (categoryColorCache.containsKey(category)) {
            return categoryColorCache.get(category);
        }
        
        // If we have enough base colors, use them first
        int categoryIndex = new ArrayList<>(categoryColorCache.keySet()).size();
        if (categoryIndex < BASE_COLORS.length) {
            int color = BASE_COLORS[categoryIndex];
            categoryColorCache.put(category, color);
            return color;
        }
        
        // For additional categories, generate colors dynamically
        // Use category name hash to ensure consistency
        int hash = Math.abs(category.hashCode());
        
        // Generate HSV values for good color distribution
        float hue = (hash % 360); // Full hue range
        float saturation = 0.7f + (hash % 30) / 100f; // 70-100% saturation
        float brightness = 0.8f + (hash % 20) / 100f; // 80-100% brightness
        
        // Ensure good contrast with dark theme
        if (brightness < 0.6f) brightness = 0.6f;
        if (saturation < 0.5f) saturation = 0.5f;
        
        int color = Color.HSVToColor(new float[]{hue, saturation, brightness});
        categoryColorCache.put(category, color);
        
        Log.d(TAG, "Generated dynamic color for category '" + category + "': #" + Integer.toHexString(color));
        return color;
    }
    
    /**
     * Generate colors for all categories at once to ensure good distribution
     */
    private List<Integer> generateCategoryColors(List<String> categories) {
        List<Integer> colors = new ArrayList<>();
        
        for (String category : categories) {
            colors.add(getColorForCategory(category));
        }
        
        return colors;
    }
    
    public void refreshData() {
        Log.d(TAG, "Refresh data called");
        if (binding != null) {
            loadAnalysisData();
        }
    }
    
    /**
     * Clear color cache when categories change significantly
     */
    private void clearColorCache() {
        categoryColorCache.clear();
        Log.d(TAG, "Category color cache cleared");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Analysis fragment resumed - refreshing data");
        // Refresh data when returning to this fragment
        if (binding != null) {
            loadAnalysisData();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clear caches to free memory
        categoryColorCache.clear();
        categoryOrder.clear();
        allFilteredTransactions.clear();
        currentExpensesByCategory.clear();
        
        binding = null;
        Log.d(TAG, "Analysis fragment destroyed, caches cleared");
    }
    
    // Helper class for category breakdown items
    public static class CategoryBreakdownItem {
        private final String category;
        private final double amount;
        
        public CategoryBreakdownItem(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
        
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }
}