package com.example.sachvault.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.database.entities.Category;
import com.example.sachvault.databinding.ItemCategoryNoBudgetBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryNoBudgetAdapter extends RecyclerView.Adapter<CategoryNoBudgetAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories = new ArrayList<>();
    private OnSetBudgetClickListener listener;

    public interface OnSetBudgetClickListener {
        void onSetBudgetClick(Category category);
    }

    public CategoryNoBudgetAdapter(Context context, OnSetBudgetClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryNoBudgetBinding binding = ItemCategoryNoBudgetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<Category> newCategories) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CategoryDiffCallback(categories, newCategories));
        categories.clear();
        categories.addAll(newCategories);
        diffResult.dispatchUpdatesTo(this);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryNoBudgetBinding binding;

        public CategoryViewHolder(@NonNull ItemCategoryNoBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listener for set budget button
            binding.btnSetBudget.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onSetBudgetClick(categories.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Category category) {
            // Set category name
            binding.tvCategoryName.setText(category.getName());

            // Set category icon based on category name
            setCategoryIcon(category.getName());
        }

        private void setCategoryIcon(String categoryName) {
            // Set appropriate icon based on category name
            int iconResource = getCategoryIconResource(categoryName);
            binding.ivCategoryIcon.setImageResource(iconResource);
        }

        private int getCategoryIconResource(String categoryName) {
            // Map category names to icon resources
            switch (categoryName.toLowerCase()) {
                case "beauty":
                    return com.example.sachvault.R.drawable.ic_beauty_24;
                case "bills":
                    return com.example.sachvault.R.drawable.ic_bills_24;
                case "donation":
                    return com.example.sachvault.R.drawable.ic_donation_24;
                case "education":
                    return com.example.sachvault.R.drawable.ic_education_24;
                case "entertainment":
                    return com.example.sachvault.R.drawable.ic_entertainment_24;
                case "food":
                case "restaurant":
                    return com.example.sachvault.R.drawable.ic_restaurant_24;
                case "transport":
                case "car":
                    return com.example.sachvault.R.drawable.ic_directions_car_24;
                case "shopping":
                    return com.example.sachvault.R.drawable.ic_shopping_bag_24;
                case "healthcare":
                    return com.example.sachvault.R.drawable.ic_business_24;
                case "utilities":
                    return com.example.sachvault.R.drawable.ic_bills_24;
                case "insurance":
                    return com.example.sachvault.R.drawable.ic_account_balance_24;
                default:
                    return com.example.sachvault.R.drawable.ic_category_24;
            }
        }
    }

    private static class CategoryDiffCallback extends DiffUtil.Callback {
        private final List<Category> oldList;
        private final List<Category> newList;

        public CategoryDiffCallback(List<Category> oldList, List<Category> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Category oldCategory = oldList.get(oldItemPosition);
            Category newCategory = newList.get(newItemPosition);

            return oldCategory.getName().equals(newCategory.getName()) &&
                   oldCategory.getType().equals(newCategory.getType());
        }
    }
}