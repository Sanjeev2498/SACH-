package com.example.sachvault.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;
import com.example.sachvault.ui.analysis.AnalysisFragment.CategoryBreakdownItem;
import com.example.sachvault.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.CategoryViewHolder> {
    
    private List<CategoryBreakdownItem> items = new ArrayList<>();
    private boolean isIncome = false; // Flag to determine if showing income (green) or expense (red)
    private boolean isAccount = false; // Flag to determine if showing accounts (color by positive/negative)
    
    // Category colors matching pie chart
    private static final int[] CATEGORY_COLORS = {
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
        Color.parseColor("#EE5A6F")  // Rose
    };
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_breakdown, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryBreakdownItem item = items.get(position);
        holder.bind(item, position, isIncome, isAccount);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateData(List<CategoryBreakdownItem> newItems) {
        this.items = new ArrayList<>(newItems);
        this.isIncome = false; // Default to expense
        this.isAccount = false;
        notifyDataSetChanged();
    }
    
    public void updateData(List<CategoryBreakdownItem> newItems, boolean isIncome) {
        this.items = new ArrayList<>(newItems);
        this.isIncome = isIncome;
        this.isAccount = false;
        notifyDataSetChanged();
    }
    
    public void updateData(List<CategoryBreakdownItem> newItems, boolean isIncome, boolean isAccount) {
        this.items = new ArrayList<>(newItems);
        this.isIncome = isIncome;
        this.isAccount = isAccount;
        notifyDataSetChanged();
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final View colorIndicator;
        private final TextView tvCategory;
        private final TextView tvAmount;
        private final TextView tvPercentage;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
        
        public void bind(CategoryBreakdownItem item, int position, boolean isIncome, boolean isAccount) {
            // Set category name
            tvCategory.setText(item.getCategory());
            
            // Set amount with appropriate color
            tvAmount.setText(CurrencyUtils.formatCurrencyWithCommas(item.getAmount()));
            
            // Set text color based on type
            if (isAccount) {
                // For accounts: show positive in blue/green, negative in red
                if (item.getAmount() >= 0) {
                    tvAmount.setTextColor(Color.parseColor("#2196F3")); // Blue for positive
                } else {
                    tvAmount.setTextColor(itemView.getContext().getColor(R.color.color_expense)); // Red for negative
                }
            } else if (isIncome) {
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.color_income));
            } else {
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.color_expense));
            }
            
            // Set color indicator
            int color = CATEGORY_COLORS[position % CATEGORY_COLORS.length];
            colorIndicator.setBackgroundColor(color);
            
            // Calculate percentage (would need total to be accurate, for now just show amount)
            // For a proper implementation, you'd pass the total expense amount
            tvPercentage.setText(""); // Will implement percentage calculation if needed
        }
    }
}