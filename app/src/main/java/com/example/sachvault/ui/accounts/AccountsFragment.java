package com.example.sachvault.ui.accounts;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.R;
import com.example.sachvault.databinding.FragmentAccountsBinding;
import com.example.sachvault.database.entities.Account;
import com.example.sachvault.ui.adapters.AccountAdapter;
import com.example.sachvault.utils.CurrencyUtils;

import java.util.Locale;

public class AccountsFragment extends Fragment {
    
    private static final String TAG = "AccountsFragment";
    private FragmentAccountsBinding binding;
    private AccountsViewModel viewModel;
    private AccountAdapter accountAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        try {
            // Initialize ViewModel
            viewModel = new ViewModelProvider(this).get(AccountsViewModel.class);
            Log.d(TAG, "ViewModel created");
            
            // Setup UI
            setupClickListeners();
            setupRecyclerView();
            observeViewModel();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
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

        // Add account button
        binding.btnAddAccount.setOnClickListener(v -> showAddAccountDialog());
    }
    
    private void setupRecyclerView() {
        try {
            accountAdapter = new AccountAdapter();
            binding.recyclerAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAccounts.setAdapter(accountAdapter);
            
            // Set item click listener
            accountAdapter.setOnItemClickListener(account -> {
                if (account != null && getContext() != null) {
                    Toast.makeText(getContext(), "Clicked: " + account.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            
            Log.d(TAG, "RecyclerView setup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage(), e);
        }
    }
    
    private void observeViewModel() {
        try {
            // Observe all active accounts
            viewModel.getAllActiveAccounts().observe(getViewLifecycleOwner(), accounts -> {
                if (accounts != null) {
                    Log.d(TAG, "Received " + accounts.size() + " accounts from ViewModel");
                    accountAdapter.submitList(accounts);
                    updateTotalBalance(accounts);
                    updateAccountSummary(accounts);
                } else {
                    Log.w(TAG, "Received null accounts list");
                    accountAdapter.submitList(java.util.Collections.emptyList());
                    binding.tvTotalBalance.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
                    resetAccountSummary();
                }
            });
            
            // Observe transactions for expense/income calculation
            viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
                updateTransactionSummary(transactions);
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error observing ViewModel: " + e.getMessage(), e);
        }
    }
    
    private void updateTotalBalance(java.util.List<Account> accounts) {
        try {
            double total = 0.0;
            if (accounts != null) {
                for (Account account : accounts) {
                    total += account.getBalance();
                }
            }
            binding.tvTotalBalance.setText(CurrencyUtils.formatCurrencyWithCommas(total));
        } catch (Exception e) {
            Log.e(TAG, "Error calculating total balance: " + e.getMessage(), e);
            binding.tvTotalBalance.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
        }
    }
    
    private void updateAccountSummary(java.util.List<Account> accounts) {
        try {
            int totalCount = 0;
            double totalAssets = 0.0;
            double totalLiabilities = 0.0;
            
            if (accounts != null) {
                totalCount = accounts.size();
                for (Account account : accounts) {
                    double balance = account.getBalance();
                    if (balance >= 0) {
                        totalAssets += balance;
                    } else {
                        totalLiabilities += Math.abs(balance);
                    }
                }
            }
            
            binding.tvAccountCount.setText(String.valueOf(totalCount));
            binding.tvTotalAssets.setText(CurrencyUtils.formatCurrencyWithCommas(totalAssets));
            binding.tvTotalLiabilities.setText(CurrencyUtils.formatCurrencyWithCommas(totalLiabilities));
        } catch (Exception e) {
            Log.e(TAG, "Error updating account summary: " + e.getMessage(), e);
            resetAccountSummary();
        }
    }
    
    private void resetAccountSummary() {
        binding.tvAccountCount.setText("0");
        binding.tvTotalAssets.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
        binding.tvTotalLiabilities.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
    }
    
    private void updateTransactionSummary(java.util.List<com.example.sachvault.database.entities.Transaction> transactions) {
        try {
            double totalExpense = 0.0;
            double totalIncome = 0.0;
            
            if (transactions != null) {
                for (com.example.sachvault.database.entities.Transaction transaction : transactions) {
                    if ("expense".equalsIgnoreCase(transaction.getType())) {
                        totalExpense += transaction.getAmount();
                    } else if ("income".equalsIgnoreCase(transaction.getType())) {
                        totalIncome += transaction.getAmount();
                    }
                }
            }
            
            binding.tvTotalExpenses.setText(CurrencyUtils.formatCurrencyWithCommas(totalExpense));
            binding.tvTotalIncome.setText(CurrencyUtils.formatCurrencyWithCommas(totalIncome));
        } catch (Exception e) {
            Log.e(TAG, "Error updating transaction summary: " + e.getMessage(), e);
            binding.tvTotalExpenses.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
            binding.tvTotalIncome.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
        }
    }
    
    private void showAddAccountDialog() {
        if (getActivity() == null) return;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Account");
        
        // Create dialog layout
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        
        // Account name input
        TextView nameLabel = new TextView(getContext());
        nameLabel.setText("Account Name");
        nameLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameLabel);
        
        EditText nameInput = new EditText(getContext());
        nameInput.setHint("e.g., My Bank");
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameInput);
        
        // Initial balance input
        TextView balanceLabel = new TextView(getContext());
        balanceLabel.setText("Initial Balance");
        balanceLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        balanceLabel.setPadding(0, 32, 0, 8);
        layout.addView(balanceLabel);
        
        EditText balanceInput = new EditText(getContext());
        balanceInput.setHint("0.00");
        balanceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        balanceInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(balanceInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String balanceStr = balanceInput.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter account name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double balance = 0.0;
            if (!balanceStr.isEmpty()) {
                try {
                    balance = Double.parseDouble(balanceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid balance amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Create and insert account
            Account account = new Account();
            account.setName(name);
            account.setType("general");
            account.setBalance(balance);
            account.setActive(true);
            
            viewModel.insertAccount(account);
            Toast.makeText(getContext(), "Account added successfully", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    // Add refreshData method for MainActivity
    public void refreshData() {
        if (viewModel != null) {
            // Data will refresh automatically through LiveData observers
            Log.d(TAG, "refreshData called");
        }
    }
}
