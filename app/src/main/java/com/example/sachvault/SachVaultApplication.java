package com.example.sachvault;

import android.app.Application;
import android.util.Log;

import com.example.sachvault.database.AppDatabase;

public class SachVaultApplication extends Application {
    
    private static final String TAG = "SachVaultApplication";
    private AppDatabase database;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            Log.d(TAG, "Initializing SachVault Application...");
            
            // Initialize Room Database with error handling
            database = AppDatabase.getDatabase(this);
            
            Log.d(TAG, "Database initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing application: " + e.getMessage(), e);
            // Don't crash the app, continue without database for now
        }
    }
    
    public AppDatabase getDatabase() {
        try {
            if (database == null) {
                Log.w(TAG, "Database is null, reinitializing...");
                database = AppDatabase.getDatabase(this);
            }
            return database;
        } catch (Exception e) {
            Log.e(TAG, "Error getting database: " + e.getMessage(), e);
            return null;
        }
    }
}