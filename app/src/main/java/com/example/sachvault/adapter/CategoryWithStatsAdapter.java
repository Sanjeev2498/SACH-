package com.example.sachvault.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.database.dao.CategoryDao;
import com.example.sachvault.database.entities.Category;
import com.example.sachvault.databinding.ItemCategoryStatsBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryWithStatsAdapter extends RecyclerView.Adapter<CategoryWithStatsAdapter.CategoryStatsViewHolder> {

    private List<CategoryDao.CategoryWithUsage> categories = new ArrayList<>();
    private OnCategoryActionListener listener;

    // Interface for handling category actions
    public interface OnCategoryActionListener {
        void onEditCategory(Category category);
        void onDeleteCategory(Category category);
    }

    // Constructor
    public CategoryWithStatsAdapter(List<CategoryDao.CategoryWithUsage> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryStatsBinding binding = ItemCategoryStatsBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryStatsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryStatsViewHolder holder, int position) {
        CategoryDao.CategoryWithUsage categoryWithUsage = categories.get(position);
        holder.bind(categoryWithUsage);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<CategoryDao.CategoryWithUsage> newCategories) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CategoryStatsDiffCallback(categories, newCategories));
        categories.clear();
        categories.addAll(newCategories != null ? newCategories : new ArrayList<>());
        diffResult.dispatchUpdatesTo(this);
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    class CategoryStatsViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryStatsBinding binding;

        public CategoryStatsViewHolder(@NonNull ItemCategoryStatsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listeners
            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CategoryDao.CategoryWithUsage categoryWithUsage = categories.get(getAdapterPosition());
                    Category category = new Category();
                    category.setId(categoryWithUsage.id);
                    category.setName(categoryWithUsage.name);
                    category.setType(categoryWithUsage.type);
                    category.setIcon(categoryWithUsage.icon != null ? categoryWithUsage.icon : "ic_category_24");
                    category.setColor(categoryWithUsage.color != null ? categoryWithUsage.color : "#6200EE");
                    listener.onEditCategory(category);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CategoryDao.CategoryWithUsage categoryWithUsage = categories.get(getAdapterPosition());
                    Category category = new Category();
                    category.setId(categoryWithUsage.id);
                    category.setName(categoryWithUsage.name);
                    category.setType(categoryWithUsage.type);
                    category.setIcon(categoryWithUsage.icon != null ? categoryWithUsage.icon : "ic_category_24");
                    category.setColor(categoryWithUsage.color != null ? categoryWithUsage.color : "#6200EE");
                    listener.onDeleteCategory(category);
                }
            });
        }

        public void bind(CategoryDao.CategoryWithUsage categoryWithUsage) {
            // Set category name
            binding.tvCategoryName.setText(categoryWithUsage.name);

            // Set category type with proper styling
            binding.tvCategoryType.setText(categoryWithUsage.type.toUpperCase());
            Context context = itemView.getContext();
            if ("income".equals(categoryWithUsage.type)) {
                binding.tvCategoryType.setTextColor(context.getResources().getColor(com.example.sachvault.R.color.color_income));
            } else {
                binding.tvCategoryType.setTextColor(context.getResources().getColor(com.example.sachvault.R.color.color_expense));
            }

            // Set usage statistics
            binding.tvUsageCount.setText(String.format("%d transactions", categoryWithUsage.usage_count));
            
            // Set usage bar progress
            int maxUsage = 50; // Could be calculated based on the highest usage in the list
            int progress = Math.min((int) ((categoryWithUsage.usage_count / (double) maxUsage) * 100), 100);
            binding.progressUsage.setProgress(progress);

            // Set category icon and color
            setCategoryIcon(categoryWithUsage.name, categoryWithUsage.type);
            
            // Show/hide delete button for unused categories
            if (categoryWithUsage.usage_count == 0) {
                binding.btnDelete.setVisibility(android.view.View.VISIBLE);
                binding.tvUsageCount.setText("Unused category");
                binding.tvUsageCount.setTextColor(context.getResources().getColor(com.example.sachvault.R.color.color_on_surface_variant));
            } else {
                binding.btnDelete.setVisibility(android.view.View.GONE);
                binding.tvUsageCount.setTextColor(context.getResources().getColor(com.example.sachvault.R.color.color_on_surface));
            }
        }

        private void setCategoryIcon(String categoryName, String categoryType) {
            // Set appropriate icon and background color based on category
            int iconResource = getCategoryIconResource(categoryName);
            int backgroundColor = getCategoryBackgroundColor(categoryName, categoryType);
            
            binding.ivCategoryIcon.setImageResource(iconResource);
            
            // Set icon background color
            if (binding.ivCategoryIcon.getParent() instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) binding.ivCategoryIcon.getParent())
                        .setCardBackgroundColor(itemView.getContext().getResources().getColor(backgroundColor));
            }
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

    private static class CategoryStatsDiffCallback extends DiffUtil.Callback {
        private final List<CategoryDao.CategoryWithUsage> oldList;
        private final List<CategoryDao.CategoryWithUsage> newList;

        public CategoryStatsDiffCallback(List<CategoryDao.CategoryWithUsage> oldList, 
                                       List<CategoryDao.CategoryWithUsage> newList) {
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
            return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CategoryDao.CategoryWithUsage oldItem = oldList.get(oldItemPosition);
            CategoryDao.CategoryWithUsage newItem = newList.get(newItemPosition);

            return oldItem.name.equals(newItem.name) &&
                   oldItem.type.equals(newItem.type) &&
                   oldItem.usage_count == newItem.usage_count;
        }
    }
}