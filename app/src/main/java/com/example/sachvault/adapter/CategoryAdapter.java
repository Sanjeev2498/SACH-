package com.example.sachvault.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.database.entities.Category;
import com.example.sachvault.databinding.ItemCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
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
        private final ItemCategoryBinding binding;

        public CategoryViewHolder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categories.get(getAdapterPosition()));
                }
            });

            binding.btnMore.setOnClickListener(v -> {
                // Handle menu click - could show edit/delete options
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    // For now, just trigger the same click event
                    listener.onCategoryClick(categories.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Category category) {
            // Set category name
            binding.tvCategoryName.setText(category.getName());

            // Set category icon based on category name and type
            setCategoryIcon(category.getName(), category.getType());
        }

        private void setCategoryIcon(String categoryName, String categoryType) {
            // Set appropriate icon and background color based on category
            int iconResource = getCategoryIconResource(categoryName);
            int backgroundColor = getCategoryBackgroundColor(categoryName, categoryType);
            
            binding.ivCategoryIcon.setImageResource(iconResource);
            binding.ivCategoryIcon.getParent(); // Get the MaterialCardView parent
            // Set background color if needed
        }

        private int getCategoryIconResource(String categoryName) {
            // Map category names to icon resources
            switch (categoryName.toLowerCase()) {
                // Income categories
                case "salary":
                    return com.example.sachvault.R.drawable.ic_salary_24;
                case "awards":
                    return com.example.sachvault.R.drawable.ic_awards_24;
                case "bonus":
                    return com.example.sachvault.R.drawable.ic_bonus_24;
                case "dividends":
                    return com.example.sachvault.R.drawable.ic_dividends_24;
                case "freelance":
                    return com.example.sachvault.R.drawable.ic_freelance_24;
                case "interest":
                    return com.example.sachvault.R.drawable.ic_interest_24;
                case "refunds":
                case "returns":
                    return com.example.sachvault.R.drawable.ic_refunds_24;

                // Expense categories
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
                case "work":
                case "business":
                    return com.example.sachvault.R.drawable.ic_work_24;
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

        private int getCategoryBackgroundColor(String categoryName, String categoryType) {
            // Return appropriate background color for category
            switch (categoryName.toLowerCase()) {
                case "beauty":
                    return com.example.sachvault.R.color.category_beauty;
                case "bills":
                    return com.example.sachvault.R.color.category_bills;
                case "donation":
                    return com.example.sachvault.R.color.category_donation;
                case "education":
                    return com.example.sachvault.R.color.category_education;
                case "entertainment":
                    return com.example.sachvault.R.color.category_entertainment;
                case "awards":
                    return com.example.sachvault.R.color.category_awards;
                case "bonus":
                    return com.example.sachvault.R.color.category_bonus;
                case "dividends":
                    return com.example.sachvault.R.color.category_dividends;
                case "freelance":
                    return com.example.sachvault.R.color.category_freelance;
                case "interest":
                    return com.example.sachvault.R.color.category_interest;
                case "salary":
                    return com.example.sachvault.R.color.category_salary;
                default:
                    return com.example.sachvault.R.color.color_surface_variant;
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
                   oldCategory.getType().equals(newCategory.getType()) &&
                   oldCategory.getIcon().equals(newCategory.getIcon());
        }
    }
}