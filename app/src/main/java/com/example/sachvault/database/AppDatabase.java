package com.example.sachvault.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.sachvault.database.dao.AccountDao;
import com.example.sachvault.database.dao.BudgetDao;
import com.example.sachvault.database.dao.CategoryDao;
import com.example.sachvault.database.dao.TransactionDao;
import com.example.sachvault.database.entities.Account;
import com.example.sachvault.database.entities.Budget;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.database.entities.Transaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {Transaction.class, Account.class, Category.class, Budget.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // Database name
    private static final String DATABASE_NAME = "sachvault_database";

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Background executor for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Abstract methods to get DAOs
    public abstract TransactionDao transactionDao();
    public abstract AccountDao accountDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();

    // Singleton pattern to get database instance
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        android.util.Log.d("AppDatabase", "Creating persistent database...");
                        
                        // Lightweight initialization for mobile devices
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDatabase.class,
                                DATABASE_NAME
                        )
                        .fallbackToDestructiveMigration() // Wipe DB on version conflicts
                        .build();
                        
                        android.util.Log.d("AppDatabase", "Persistent database created successfully at: " + 
                            context.getApplicationContext().getDatabasePath(DATABASE_NAME).getAbsolutePath());
                        
                    } catch (Exception e) {
                        android.util.Log.e("AppDatabase", "Error creating persistent database: " + e.getMessage(), e);
                        // Create in-memory database as fallback
                        try {
                            android.util.Log.w("AppDatabase", "FALLBACK: Creating in-memory database (data will not persist!)");
                            INSTANCE = Room.inMemoryDatabaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class
                            ).build();
                            android.util.Log.w("AppDatabase", "WARNING: Using in-memory database - data will be lost on app restart!");
                        } catch (Exception fallbackError) {
                            android.util.Log.e("AppDatabase", "Failed to create fallback database: " + fallbackError.getMessage(), fallbackError);
                            throw new RuntimeException("Cannot initialize database", fallbackError);
                        }
                    }
                }
            }
        }
        return INSTANCE;
    }

    // Callback to populate database with default data on creation
    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Only populate essential default data (accounts and categories)
            // DO NOT insert sample budgets or transactions
            databaseWriteExecutor.execute(() -> {
                populateEssentialDefaults(INSTANCE);
            });
        }
    };

    // Database migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create budgets table
            database.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`category_id` INTEGER NOT NULL, " +
                    "`category_name` TEXT NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`spent` REAL NOT NULL, " +
                    "`month` INTEGER NOT NULL, " +
                    "`year` INTEGER NOT NULL, " +
                    "`created_at` INTEGER, " +
                    "`updated_at` INTEGER, " +
                    "FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE)");
            
            // Create indexes
            database.execSQL("CREATE INDEX `index_budgets_category_id` ON `budgets` (`category_id`)");
            database.execSQL("CREATE UNIQUE INDEX `index_budgets_category_id_month_year` ON `budgets` (`category_id`, `month`, `year`)");
        }
    };

    // Method to populate ONLY essential defaults (categories ONLY - NO accounts)
    private static void populateEssentialDefaults(AppDatabase database) {
        try {
            android.util.Log.d("AppDatabase", "Populating essential default data only...");
            
            // Get DAOs
            CategoryDao categoryDao = database.categoryDao();

            // DO NOT insert default accounts - user creates their own
            android.util.Log.d("AppDatabase", "Skipping default accounts - user will create them");

            // Insert default categories ONLY
            Category[] defaultCategories = Category.getAllDefaultCategories();
            categoryDao.insertCategories(defaultCategories);
            android.util.Log.d("AppDatabase", "Default categories inserted");
            
            // DO NOT insert sample budgets or transactions
            android.util.Log.d("AppDatabase", "Essential defaults populated successfully");
            
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error populating essential defaults: " + e.getMessage(), e);
        }
    }
    
    // Removed: populateDefaultData() - replaced with populateEssentialDefaults()

    // Method to clear all data (for testing or reset functionality)
    public void clearAllData() {
        databaseWriteExecutor.execute(() -> {
            transactionDao().deleteAllTransactions();
            // Note: We don't delete accounts and categories as they are default data
            // If needed, you can add methods to reset them to defaults
        });
    }

    // Method to reset to default data
    public void resetToDefaults() {
        databaseWriteExecutor.execute(() -> {
            // Clear all data
            transactionDao().deleteAllTransactions();
            
            // Reset accounts to default values
            Account[] defaultAccounts = Account.getDefaultAccounts();
            for (Account account : defaultAccounts) {
                // Update existing accounts or insert if not exists
                Account existingAccount = accountDao().getAccountByNameSync(account.getName());
                if (existingAccount != null) {
                    existingAccount.setBalance(account.getBalance());
                    existingAccount.setActive(true);
                    accountDao().updateAccount(existingAccount);
                } else {
                    accountDao().insertAccount(account);
                }
            }
        });
    }

    // Database migration example (for future versions)
    // static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    //     @Override
    //     public void migrate(SupportSQLiteDatabase database) {
    //         // Example: Add new column
    //         database.execSQL("ALTER TABLE transactions ADD COLUMN description TEXT");
    //     }
    // };

    // Method to check database type and sync data
    public static void logDatabaseInfo(Context context) {
        try {
            android.util.Log.d("AppDatabase", "=== DATABASE DIAGNOSTIC ===");
            
            if (INSTANCE != null) {
                // Check if it's in-memory or persistent
                android.util.Log.d("AppDatabase", "Database instance exists");
                
                // Try to get database path
                String dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
                boolean dbExists = context.getDatabasePath(DATABASE_NAME).exists();
                
                android.util.Log.d("AppDatabase", "Expected DB path: " + dbPath);
                android.util.Log.d("AppDatabase", "DB file exists: " + dbExists);
                
                if (dbExists) {
                    long dbSize = context.getDatabasePath(DATABASE_NAME).length();
                    android.util.Log.d("AppDatabase", "DB file size: " + dbSize + " bytes");
                } else {
                    android.util.Log.w("AppDatabase", "WARNING: Database file does not exist - likely using in-memory!");
                }
            } else {
                android.util.Log.w("AppDatabase", "Database instance is null");
            }
            
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error checking database info: " + e.getMessage(), e);
        }
    }

    // Method to close database (call this in Application's onTerminate if needed)
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }

    // Utility method to check if database has transactions
    public boolean hasTransactions() {
        try {
            Integer count = transactionDao().getTransactionCount().getValue();
            return count != null && count > 0;
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error checking transaction count: " + e.getMessage());
            return false;
        }
    }
}