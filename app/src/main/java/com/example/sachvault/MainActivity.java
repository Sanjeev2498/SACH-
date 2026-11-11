package com.example.sachvault;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.sachvault.databinding.ActivityMainBinding;
import com.example.sachvault.ui.accounts.AccountsFragment;
import com.example.sachvault.ui.dashboard.SimpleDashboardFragment;
import com.example.sachvault.ui.analysis.AnalysisFragment;
import com.example.sachvault.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private int currentNavPosition = Constants.NAV_DASHBOARD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            android.util.Log.d("MainActivity", "Starting MainActivity initialization...");
            
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Hide the ActionBar completely
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            setupNavigationDrawer();
            setupBottomNavigation();
            
            // Load default fragment
            if (savedInstanceState == null) {
                loadFragment(new SimpleDashboardFragment());
            }
            
            android.util.Log.d("MainActivity", "MainActivity initialized successfully");
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in onCreate: " + e.getMessage(), e);
            // Show error to user but don't crash
            Toast.makeText(this, "Error starting app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // Try to continue with minimal setup
            try {
                binding = ActivityMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
            } catch (Exception fallbackError) {
                android.util.Log.e("MainActivity", "Fallback initialization failed", fallbackError);
                finish(); // Close app gracefully
            }
        }
    }

    private void setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener(this);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return handleBottomNavigation(item);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        int position = Constants.NAV_DASHBOARD;

        int itemId = item.getItemId();
        
        // Main Navigation Items
        if (itemId == R.id.nav_dashboard) {
            fragment = new SimpleDashboardFragment();
            title = getString(R.string.dashboard_title);
            position = Constants.NAV_DASHBOARD;
            // Also update bottom navigation
            binding.bottomNavigation.setSelectedItemId(R.id.nav_records);
        } else if (itemId == R.id.nav_records_menu) {
            fragment = new SimpleDashboardFragment();
            title = getString(R.string.dashboard_title);
            position = Constants.NAV_DASHBOARD;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_records);
        } else if (itemId == R.id.nav_analysis_menu) {
            fragment = new AnalysisFragment(); 
            title = getString(R.string.nav_analysis);
            position = Constants.NAV_ANALYSIS;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_analysis);
        } else if (itemId == R.id.nav_budgets_menu) {
            fragment = new com.example.sachvault.ui.budgets.BudgetsFragment();
            title = getString(R.string.nav_budgets);
            position = Constants.NAV_BUDGETS;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_budgets);
        } else if (itemId == R.id.nav_accounts_menu) {
            fragment = new AccountsFragment();
            title = getString(R.string.accounts_title);
            position = Constants.NAV_ACCOUNTS;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_accounts);
        } else if (itemId == R.id.nav_categories_menu) {
            fragment = new com.example.sachvault.ui.categories.CategoriesFragment();
            title = getString(R.string.nav_categories);
            position = Constants.NAV_CATEGORIES;
            binding.bottomNavigation.setSelectedItemId(R.id.nav_categories);
        }
        
        // Tool Items
        else if (itemId == R.id.nav_export) {
            Toast.makeText(this, "Export Data - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_import) {
            Toast.makeText(this, "Import Data - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_backup) {
            Toast.makeText(this, "Backup - Coming Soon", Toast.LENGTH_SHORT).show();
        }
        
        // Settings Items
        else if (itemId == R.id.nav_settings) {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_help) {
            Toast.makeText(this, "Help and Support - Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_about) {
            Toast.makeText(this, "About SachVault v1.0", Toast.LENGTH_SHORT).show();
        }

        if (fragment != null) {
            currentNavPosition = position;
            updateToolbarTitle(title);
            loadFragment(fragment);
        }

        // Close the drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean handleBottomNavigation(MenuItem item) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        int position = Constants.NAV_DASHBOARD;

        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_records) {
            fragment = new SimpleDashboardFragment();
            title = getString(R.string.dashboard_title);
            position = Constants.NAV_DASHBOARD;
        } else if (itemId == R.id.nav_analysis) {
            fragment = new AnalysisFragment();
            title = getString(R.string.nav_analysis);
            position = Constants.NAV_ANALYSIS;
        } else if (itemId == R.id.nav_budgets) {
            fragment = new com.example.sachvault.ui.budgets.BudgetsFragment();
            title = getString(R.string.nav_budgets);
            position = Constants.NAV_BUDGETS;
        } else if (itemId == R.id.nav_accounts) {
            fragment = new AccountsFragment();
            title = getString(R.string.accounts_title);
            position = Constants.NAV_ACCOUNTS;
        } else if (itemId == R.id.nav_categories) {
            fragment = new com.example.sachvault.ui.categories.CategoriesFragment();
            title = getString(R.string.nav_categories);
            position = Constants.NAV_CATEGORIES;
        }

        if (fragment != null) {
            currentNavPosition = position;
            updateToolbarTitle(title);
            return loadFragment(fragment);
        }

        return false;
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh current fragment when returning from AddTransaction
        refreshCurrentFragment();
    }

    private void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof SimpleDashboardFragment) {
            ((SimpleDashboardFragment) currentFragment).refreshData();
        } else if (currentFragment instanceof AccountsFragment) {
            ((AccountsFragment) currentFragment).refreshData();
        }
        else if (currentFragment instanceof AnalysisFragment) {
            ((AnalysisFragment) currentFragment).refreshData();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}