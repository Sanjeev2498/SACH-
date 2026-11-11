package com.example.sachvault.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;
import com.example.sachvault.database.dao.TransactionDao;
import com.example.sachvault.databinding.ItemCategoryTrendBinding;
import com.example.sachvault.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoryTrendAdapter extends RecyclerView.Adapter<CategoryTrendAdapter.TrendViewHolder> {

    private List<TransactionDao.CategoryStats> trends = new ArrayList<>();

    @NonNull
    @Override
    public TrendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryTrendBinding binding = ItemCategoryTrendBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TrendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendViewHolder holder, int position) {
        TransactionDao.CategoryStats trend = trends.get(position);
        holder.bind(trend, position + 1);
    }

    @Override
    public int getItemCount() {
        return Math.min(trends.size(), 5); // Show top 5 trends
    }

    public void updateTrends(List<TransactionDao.CategoryStats> newTrends) {
        trends.clear();
        if (newTrends != null) {
            trends.addAll(newTrends);
        }
        notifyDataSetChanged();
    }

    static class TrendViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryTrendBinding binding;

        public TrendViewHolder(@NonNull ItemCategoryTrendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TransactionDao.CategoryStats trend, int rank) {
            // Set rank
            binding.tvRank.setText(String.valueOf(rank));
            
            // Set category name
            binding.tvCategoryName.setText(trend.category);
            
            // Set amount with proper color based on type
            binding.tvAmount.setText(CurrencyUtils.formatCurrencyWithCommas(Math.abs(trend.total_amount)));
            
            // Set transaction count
            binding.tvTransactionCount.setText(trend.transaction_count + " transactions");
            
            // Color coding
            if (trend.total_amount < 0) { // Expense
                binding.tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.color_expense));
                binding.tvCategoryName.setTextColor(itemView.getContext().getResources().getColor(R.color.color_on_surface));
            } else { // Income
                binding.tvAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.color_income));
                binding.tvCategoryName.setTextColor(itemView.getContext().getResources().getColor(R.color.color_on_surface));
            }
            
            // Set rank background color
            int rankColor;
            switch (rank) {
                case 1:
                    rankColor = R.color.color_primary;
                    break;
                case 2:
                    rankColor = R.color.category_awards;
                    break;
                case 3:
                    rankColor = R.color.category_bonus;
                    break;
                default:
                    rankColor = R.color.color_surface_variant;
                    break;
            }
            binding.tvRank.setBackgroundTintList(
                    itemView.getContext().getResources().getColorStateList(rankColor));
        }
    }
}