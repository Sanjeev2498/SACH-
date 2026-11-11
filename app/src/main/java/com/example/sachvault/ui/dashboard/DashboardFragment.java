package com.example.sachvault.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.databinding.FragmentDashboardBinding;
import com.example.sachvault.ui.adapters.TransactionAdapter;
import com.example.sachvault.utils.CurrencyUtils;

public class DashboardFragment extends Fragment {
    
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private TransactionAdapter transactionAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        setupRecyclerView();
        setupMonthNavigation();
        observeViewModel();
    }
    
    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTransactions.setAdapter(transactionAdapter);
        
        // Set item click listener for editing transactions
        transactionAdapter.setOnItemClickListener(transaction -> {
            // TODO: Navigate to edit transaction
            // For now, we'll just show a placeholder
        });
        
        // Set delete listener
        transactionAdapter.setOnDeleteClickListener(transaction -> {
            viewModel.deleteTransaction(transaction);
        });
    }
    
    private void setupMonthNavigation() {
        binding.btnPreviousMonth.setOnClickListener(v -> viewModel.navigateToPreviousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.navigateToNextMonth());
        
        // Long click on month text to go to current month
        binding.tvCurrentMonth.setOnLongClickListener(v -> {
            viewModel.navigateToCurrentMonth();
            return true;
        });
    }
    
    private void observeViewModel() {
        // Observe month display
        viewModel.getCurrentMonthDisplay().observe(getViewLifecycleOwner(), monthDisplay -> {
            binding.tvCurrentMonth.setText(monthDisplay);
        });
        
        // Observe monthly transactions
        viewModel.getCurrentMonthTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.recyclerTransactions.setVisibility(View.VISIBLE);
                transactionAdapter.submitList(transactions);
            } else {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerTransactions.setVisibility(View.GONE);
            }
        });
        
        // Observe monthly income
        viewModel.getCurrentMonthIncome().observe(getViewLifecycleOwner(), income -> {
            String formattedIncome = CurrencyUtils.formatAmount(income != null ? income : 0.0);
            binding.tvTotalIncome.setText(formattedIncome);
        });
        
        // Observe monthly expenses
        viewModel.getCurrentMonthExpenses().observe(getViewLifecycleOwner(), expenses -> {
            String formattedExpenses = CurrencyUtils.formatAmount(expenses != null ? expenses : 0.0);
            binding.tvTotalExpense.setText(formattedExpenses);
        });
        
        // Observe monthly balance
        viewModel.getCurrentMonthBalance().observe(getViewLifecycleOwner(), balance -> {
            String formattedBalance = CurrencyUtils.formatAmount(balance != null ? balance : 0.0);
            binding.tvTotalBalance.setText(formattedBalance);
        });
    }
    
    // Method called from MainActivity when returning from other screens
    public void refreshData() {
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}