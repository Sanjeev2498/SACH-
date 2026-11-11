package com.example.sachvault.ui.categories;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sachvault.R;
import com.example.sachvault.MainActivity;
import com.example.sachvault.databinding.FragmentCategoriesBinding;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.repository.AccountRepository;
import com.example.sachvault.repository.CategoryRepository;
import com.example.sachvault.ui.adapters.SimpleCategoryAdapter;
import com.example.sachvault.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CategoriesFragment extends Fragment {

    private static final String TAG = "CategoriesFragment";
    private FragmentCategoriesBinding binding;
    private SimpleCategoryAdapter categoryAdapter;
    private CategoryRepository categoryRepository;
    private AccountRepository accountRepository;
    private String currentFilter = "all";
    private List<Category> allCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeRepositories();
        setupRecyclerView();
        setupClickListeners();
        loadData();
    }

    private void initializeRepositories() {
        try {
            categoryRepository = new CategoryRepository(requireActivity().getApplication());
            accountRepository = new AccountRepository(requireActivity().getApplication());
            Log.d(TAG, "Repositories initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing repositories: " + e.getMessage(), e);
        }
    }

    private void setupRecyclerView() {
        categoryAdapter = new SimpleCategoryAdapter();
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCategories.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener(category -> showCategoryOptionsDialog(category));
    }

    private void setupClickListeners() {
        // Menu button
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                androidx.drawerlayout.widget.DrawerLayout drawerLayout = 
                    mainActivity.findViewById(R.id.drawer_layout);
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                }
            }
        });

        // Filter chips
        binding.chipAll.setOnClickListener(v -> { currentFilter = "all"; filterCategories(); });
        binding.chipExpense.setOnClickListener(v -> { currentFilter = "expense"; filterCategories(); });
        binding.chipIncome.setOnClickListener(v -> { currentFilter = "income"; filterCategories(); });
        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadData() {
        loadAccountsTotal();
        loadCategories();
    }

    private void loadAccountsTotal() {
        if (accountRepository == null) return;
        accountRepository.getAllActiveAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                double total = 0.0;
                for (com.example.sachvault.database.entities.Account account : accounts) {
                    total += account.getBalance();
                }
                binding.tvAllAccountsTotal.setText(CurrencyUtils.formatCurrencyWithCommas(total));
            } else {
                binding.tvAllAccountsTotal.setText(CurrencyUtils.formatCurrencyWithCommas(0.00));
            }
        });
    }

    private void loadCategories() {
        if (categoryRepository == null) return;
        categoryRepository.getAllActiveCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategories = categories;
                filterCategories();
                Log.d(TAG, "Loaded " + categories.size() + " categories");
            } else {
                allCategories = new ArrayList<>();
                categoryAdapter.updateCategories(allCategories);
            }
        });
    }

    private void filterCategories() {
        List<Category> filtered = new ArrayList<>();
        for (Category category : allCategories) {
            if (currentFilter.equals("all") || currentFilter.equals(category.getType())) {
                filtered.add(category);
            }
        }
        categoryAdapter.updateCategories(filtered);
        Log.d(TAG, "Filtered " + filtered.size() + " categories for: " + currentFilter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Category");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView nameLabel = new TextView(getContext());
        nameLabel.setText("Category Name");
        nameLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameLabel);

        EditText nameInput = new EditText(getContext());
        nameInput.setHint("Enter category name");
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameInput);

        TextView typeLabel = new TextView(getContext());
        typeLabel.setText("Category Type");
        typeLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        typeLabel.setPadding(0, 32, 0, 8);
        layout.addView(typeLabel);

        RadioGroup typeGroup = new RadioGroup(getContext());
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton expenseRadio = new RadioButton(getContext());
        expenseRadio.setText("Expense");
        expenseRadio.setId(View.generateViewId());
        expenseRadio.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        expenseRadio.setChecked(true);
        typeGroup.addView(expenseRadio);

        RadioButton incomeRadio = new RadioButton(getContext());
        incomeRadio.setText("Income");
        incomeRadio.setId(View.generateViewId());
        incomeRadio.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        typeGroup.addView(incomeRadio);

        layout.addView(typeGroup);

        // Icon selection
        TextView iconLabel = new TextView(getContext());
        iconLabel.setText("Select Icon");
        iconLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        iconLabel.setPadding(0, 32, 0, 8);
        layout.addView(iconLabel);

        final String[] selectedIcon = {"restaurant"}; // Default
        android.widget.Button iconButton = new android.widget.Button(getContext());
        iconButton.setText("Choose Icon");
        iconButton.setOnClickListener(v -> {
            showIconPickerDialog(selectedIcon, iconButton);
        });
        layout.addView(iconButton);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            if (!name.isEmpty()) {
                String type = expenseRadio.isChecked() ? "expense" : "income";
                addCategory(name, type, selectedIcon[0]);
            } else {
                Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showIconPickerDialog(String[] selectedIcon, android.widget.Button iconButton) {
        String[] icons = {
            "restaurant", "directions_car", "home", "shopping_bag", "movie",
            "school", "bills", "smartphone", "beauty", "work",
            "business", "trending_up", "bonus", "freelance"
        };
        
        String[] iconNames = {
            "Food", "Transport", "Home", "Shopping", "Entertainment",
            "Education", "Bills", "Phone", "Beauty", "Work",
            "Business", "Investment", "Bonus", "Freelance"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Icon");
        builder.setItems(iconNames, (dialog, which) -> {
            selectedIcon[0] = icons[which];
            iconButton.setText("Icon: " + iconNames[which]);
        });
        builder.show();
    }

    private void addCategory(String name, String type, String icon) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(type.equals("expense") ? "#ffb4ab" : "#4edea3");
        category.setActive(true);
        categoryRepository.insertCategory(category);
        Toast.makeText(getContext(), "Category added successfully", Toast.LENGTH_SHORT).show();
    }

    private void showCategoryOptionsDialog(Category category) {
        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(category.getName());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) showEditCategoryDialog(category);
            else if (which == 1) showDeleteConfirmDialog(category);
        });
        builder.show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Category");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView nameLabel = new TextView(getContext());
        nameLabel.setText("Category Name");
        nameLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameLabel);

        EditText nameInput = new EditText(getContext());
        nameInput.setText(category.getName());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        layout.addView(nameInput);

        TextView typeLabel = new TextView(getContext());
        typeLabel.setText("Category Type");
        typeLabel.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        typeLabel.setPadding(0, 32, 0, 8);
        layout.addView(typeLabel);

        RadioGroup typeGroup = new RadioGroup(getContext());
        typeGroup.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton expenseRadio = new RadioButton(getContext());
        expenseRadio.setText("Expense");
        expenseRadio.setId(View.generateViewId());
        expenseRadio.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        expenseRadio.setChecked("expense".equals(category.getType()));
        typeGroup.addView(expenseRadio);

        RadioButton incomeRadio = new RadioButton(getContext());
        incomeRadio.setText("Income");
        incomeRadio.setId(View.generateViewId());
        incomeRadio.setTextColor(getResources().getColor(R.color.color_on_surface, null));
        incomeRadio.setChecked("income".equals(category.getType()));
        typeGroup.addView(incomeRadio);

        layout.addView(typeGroup);
        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            if (!newName.isEmpty()) {
                category.setName(newName);
                category.setType(expenseRadio.isChecked() ? "expense" : "income");
                categoryRepository.updateCategory(category);
                Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(Category category) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryRepository.deleteCategory(category);
                    Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
