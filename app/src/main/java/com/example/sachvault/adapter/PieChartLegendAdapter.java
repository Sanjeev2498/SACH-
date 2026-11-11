package com.example.sachvault.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;

import java.util.ArrayList;
import java.util.List;

public class PieChartLegendAdapter extends RecyclerView.Adapter<PieChartLegendAdapter.LegendViewHolder> {
    
    private List<LegendItem> items = new ArrayList<>();
    
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
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pie_chart_legend, parent, false);
        return new LegendViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        LegendItem item = items.get(position);
        holder.bind(item, position);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateData(List<LegendItem> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }
    
    static class LegendViewHolder extends RecyclerView.ViewHolder {
        private final View colorIndicator;
        private final TextView tvCategory;
        
        public LegendViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
        
        public void bind(LegendItem item, int position) {
            // Set category name
            tvCategory.setText(item.getCategory());
            
            // Set color indicator
            int color = CATEGORY_COLORS[position % CATEGORY_COLORS.length];
            colorIndicator.setBackgroundColor(color);
        }
    }
    
    // Helper class for legend items
    public static class LegendItem {
        private final String category;
        private final double amount;
        
        public LegendItem(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
        
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }
}