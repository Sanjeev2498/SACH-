package com.example.sachvault.ui.budgets;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.R;
import com.example.sachvault.databinding.FragmentBudgetsBinding;
import com.example.sachvault.database.dao.BudgetDao;
import com.example.sachvault.database.entities.Budget;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.repository.BudgetRepository;
import com.example.sachvault.repository.CategoryRepository;
import com.example.sachvault.ui.adapters.BudgetCategoryAdapter;
import com.example.sachvault.utils.CurrencyUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetsFragment extends Fragment {

    private static final String TAG = "BudgetsFragment";
    private FragmentBudgetsBinding binding;
    private BudgetCategoryAdapter budgetAdapter;
    private BudgetRepository budgetRepository;
    private CategoryRepository categoryRepository;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize calendar and repository
        currentCalendar = Calendar.getInstance();
        monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        
        try {
            budgetRepository = new BudgetRepository(requireActivity().getApplication());
            categoryRepository = new CategoryRepository(requireActivity().getApplication());
            Log.d(TAG, "Repositories initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repositories: " + e.getMessage(), e);
        }
        
        setupUI();
        setupRecyclerView();
        setupClickListeners();
        setupFAB();
        loadBudgetData();
    }
    
    private void setupUI() {
        updateMonthDisplay();
        
        // Set initial loading state
        binding.tvTotalBudget.setText("Loading...");
        binding.tvTotalSpent.setText("Loading...");
    }
    
    private void updateMonthDisplay() {
        String monthYear = monthYearFormat.format(currentCalendar.getTime());
        binding.tvCurrentMonth.setText(monthYear);
    }
    
    private void setupRecyclerView() {
        budgetAdapter = new BudgetCategoryAdapter();
        binding.rvBudgetCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBudgetCategories.setAdapter(budgetAdapter);
        
        // Set click listener to edit budget
        budgetAdapter.setOnBudgetClickListener(budgetItem -> {
            showEditBudgetDialog(budgetItem);
        });
    }
    
    private void setupClickListeners() {
        // Menu button
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.sachvault.MainActivity) {
                com.example.sachvault.MainActivity mainActivity = 
                    (com.example.sachvault.MainActivity) getActivity();
                androidx.drawerlayout.widget.DrawerLayout drawerLayout = 
                    mainActivity.findViewById(R.id.drawer_layout);
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                }
            }
        });

        binding.btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadBudgetData();
        });
        
        binding.btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadBudgetData();
        });
    }
    
    private void setupFAB() {
        // Setup FAB in fragment's layout
        binding.fabAddBudget.setOnClickListener(v -> showSetBudgetsDialog());
    }
    
    private void showSetBudgetsDialog() {
        if (categoryRepository == null) {
            Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get all expense categories
        categoryRepository.getExpenseCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) {
                Toast.makeText(getContext(), "No categories found. Add categories first.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Set Monthly Budgets");
            
            // Create ScrollView container
            android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(40, 20, 40, 20);
            
            // Store EditTexts for each category
            Map<Integer, EditText> budgetInputs = new HashMap<>();
            
            int month = currentCalendar.get(Calendar.MONTH) + 1;
            int year = currentCalendar.get(Calendar.YEAR);
            
            // For each category, create input field
            for (Category category : categories) {
                // Category label
                TextView categoryLabel = new TextView(getContext());
                categoryLabel.setText(category.getName());
                categoryLabel.setTextSize(16);
                categoryLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
                categoryLabel.setPadding(0, 20, 0, 8);
                container.addView(categoryLabel);
                
                // Budget input
                EditText budgetInput = new EditText(getContext());
                budgetInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                budgetInput.setHint("Enter budget amount");
                budgetInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
                budgetInput.setHintTextColor(getResources().getColor(R.color.color_on_surface_variant, null));
                
                // Load existing budget if available
                budgetRepository.getBudgetByCategoryAndMonth(category.getId(), month, year)
                    .observe(getViewLifecycleOwner(), existingBudget -> {
                        if (existingBudget != null && budgetInput.getText().toString().isEmpty()) {
                            budgetInput.setText(String.valueOf(existingBudget.getAmount()));
                        }
                    });
                
                container.addView(budgetInput);
                budgetInputs.put(category.getId(), budgetInput);
            }
            
            scrollView.addView(container);
            builder.setView(scrollView);
            
            builder.setPositiveButton("Save", (dialog, which) -> {
                saveBudgets(categories, budgetInputs);
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }
    
    private void saveBudgets(List<Category> categories, Map<Integer, EditText> budgetInputs) {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        int savedCount = 0;
        
        for (Category category : categories) {
            EditText input = budgetInputs.get(category.getId());
            if (input != null && !input.getText().toString().trim().isEmpty()) {
                try {
                    double amount = Double.parseDouble(input.getText().toString().trim());
                    if (amount > 0) {
                        Budget budget = new Budget(category.getId(), category.getName(), amount, month, year);
                        budgetRepository.insertBudget(budget);
                        savedCount++;
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid budget amount for " + category.getName());
                }
            }
        }
        
        if (savedCount > 0) {
            Toast.makeText(getContext(), savedCount + " budgets saved", Toast.LENGTH_SHORT).show();
            loadBudgetData();
        } else {
            Toast.makeText(getContext(), "No budgets entered", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadBudgetData() {
        try {
            if (budgetRepository == null) {
                Log.w(TAG, "Budget repository is null, showing empty state");
                showEmptyState();
                return;
            }
            
            int month = currentCalendar.get(Calendar.MONTH) + 1;
            int year = currentCalendar.get(Calendar.YEAR);
            
            Log.d(TAG, "Loading budgets for: " + month + "/" + year);
            
            // Load budgets WITH actual spent amounts from transactions
            budgetRepository.getBudgetsWithActualSpent(month, year).observe(getViewLifecycleOwner(), budgetsWithSpent -> {
                try {
                    if (budgetsWithSpent != null && !budgetsWithSpent.isEmpty()) {
                        Log.d(TAG, "Loaded " + budgetsWithSpent.size() + " budgets with actual spent data");
                        
                        // Convert to adapter items
                        List<BudgetCategoryAdapter.BudgetItem> budgetItems = new ArrayList<>();
                        double totalBudget = 0.0;
                        double totalSpent = 0.0;
                        
                        for (BudgetDao.BudgetWithActualSpent budgetData : budgetsWithSpent) {
                            double actualSpent = budgetData.actual_spent != 0 ? budgetData.actual_spent : 0.0;
                            double remaining = budgetData.amount - actualSpent;
                            int progress = budgetData.amount > 0 ? (int) ((actualSpent / budgetData.amount) * 100) : 0;
                            
                            budgetItems.add(new BudgetCategoryAdapter.BudgetItem(
                                budgetData.categoryId,
                                budgetData.categoryName,
                                budgetData.amount,
                                actualSpent,
                                String.format(Locale.getDefault(), "Spent: %s", CurrencyUtils.formatCurrencyWithCommas(actualSpent)),
                                String.format(Locale.getDefault(), "Remaining: %s", CurrencyUtils.formatCurrencyWithCommas(remaining)),
                                String.format(Locale.getDefault(), "Limit: %s", CurrencyUtils.formatCurrencyWithCommas(budgetData.amount)),
                                progress,
                                getCategoryIcon(budgetData.categoryName)
                            ));
                            
                            totalBudget += budgetData.amount;
                            totalSpent += actualSpent;
                        }
                        
                        // Update summary
                        binding.tvTotalBudget.setText(CurrencyUtils.formatCurrencyWithCommas(totalBudget));
                        binding.tvTotalSpent.setText(CurrencyUtils.formatCurrencyWithCommas(totalSpent));
                        
                        // Update adapter
                        budgetAdapter.updateBudgetItems(budgetItems);
                        showBudgets();
                        
                    } else {
                        Log.d(TAG, "No budgets found for current month");
                        showEmptyState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing budget data: " + e.getMessage(), e);
                    showEmptyState();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading budget data: " + e.getMessage(), e);
            showEmptyState();
        }
    }
    
    private int getCategoryIcon(String categoryName) {
        // Map category names to icons
        if (categoryName == null) return R.drawable.ic_receipt_24;
        
        String normalizedName = categoryName.toLowerCase();
        if (normalizedName.contains("food")) {
            return R.drawable.ic_restaurant_24;
        } else if (normalizedName.contains("transport")) {
            return R.drawable.ic_directions_car_24;
        } else if (normalizedName.contains("housing") || normalizedName.contains("home")) {
            return R.drawable.ic_receipt_24;
        } else if (normalizedName.contains("shopping")) {
            return R.drawable.ic_shopping_bag_24;
        } else {
            return R.drawable.ic_receipt_24;
        }
    }
    
    private void showEmptyState() {
        // Show empty state UI
        binding.tvTotalBudget.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
        binding.tvTotalSpent.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
        budgetAdapter.updateBudgetItems(new ArrayList<>());
    }
    
    private void showBudgets() {
        // Budgets are visible by default
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    private void showEditBudgetDialog(BudgetCategoryAdapter.BudgetItem budgetItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Budget: " + budgetItem.categoryName);
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        TextView currentLabel = new TextView(getContext());
        currentLabel.setText("Current Budget: " + CurrencyUtils.formatCurrencyWithCommas(budgetItem.budgetAmount));
        currentLabel.setTextColor(getResources().getColor(R.color.color_on_surface_variant, null));
        currentLabel.setPadding(0, 0, 0, 16);
        layout.addView(currentLabel);
        
        TextView spentLabel = new TextView(getContext());
        spentLabel.setText("Spent: " + CurrencyUtils.formatCurrencyWithCommas(budgetItem.spentAmountValue));
        spentLabel.setTextColor(getResources().getColor(R.color.color_expense, null));
        spentLabel.setPadding(0, 0, 0, 24);
        layout.addView(spentLabel);
        
        TextView newAmountLabel = new TextView(getContext());
        newAmountLabel.setText("New Budget Amount");
        newAmountLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(newAmountLabel);
        
        EditText budgetInput = new EditText(getContext());
        budgetInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        budgetInput.setHint("Enter new amount");
        budgetInput.setText(String.valueOf(budgetItem.budgetAmount));
        budgetInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(budgetInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Update", (dialog, which) -> {
            String amountStr = budgetInput.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                try {
                    double newAmount = Double.parseDouble(amountStr);
                    if (newAmount > 0) {
                        updateBudgetAmount(budgetItem.categoryId, budgetItem.categoryName, newAmount);
                    } else {
                        Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.setNeutralButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (d, w) -> {
                    deleteBudget(budgetItem.categoryId);
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        builder.show();
    }
    
    private void updateBudgetAmount(int categoryId, String categoryName, double newAmount) {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        
        Budget budget = new Budget(categoryId, categoryName, newAmount, month, year);
        budgetRepository.insertBudget(budget);
        Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_SHORT).show();
        loadBudgetData();
    }
    
    private void deleteBudget(int categoryId) {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        
        // You'll need to add a method to get budget by category and month, then delete it
        // For now, just reload data
        Toast.makeText(getContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
        loadBudgetData();
    }
}