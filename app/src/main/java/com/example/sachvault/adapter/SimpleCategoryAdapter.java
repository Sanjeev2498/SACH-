package com.example.sachvault.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;
import com.example.sachvault.database.entities.Category;

import java.util.ArrayList;
import java.util.List;

public class SimpleCategoryAdapter extends RecyclerView.Adapter<SimpleCategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener clickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.clickListener = listener;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryType.setText(category.getType().toUpperCase());

        // Set icon from database icon name
        int iconRes = getIconResource(category.getIcon());
        holder.ivCategoryIcon.setImageResource(iconRes);

        // Set type color
        if ("expense".equals(category.getType())) {
            holder.tvCategoryType.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.color_expense, null));
        } else {
            holder.tvCategoryType.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.color_income, null));
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private int getIconResource(String iconName) {
        if (iconName == null || iconName.isEmpty()) return R.drawable.ic_category_24;

        // Map icon names from database to actual drawable resources
        switch (iconName.toLowerCase()) {
            // Income icons
            case "work":
            case "salary":
                return R.drawable.ic_work_24;
            case "business":
                return R.drawable.ic_business_24;
            case "trending_up":
            case "investment":
                return R.drawable.ic_trending_up_24;
            case "card_giftcard":
            case "gift":
                return R.drawable.ic_awards_24;
            case "add_circle":
                return R.drawable.ic_add_24;
            case "bonus":
                return R.drawable.ic_bonus_24;
            case "dividends":
                return R.drawable.ic_dividends_24;
            case "freelance":
                return R.drawable.ic_freelance_24;
            case "interest":
                return R.drawable.ic_interest_24;
            case "refunds":
                return R.drawable.ic_refunds_24;
                
            // Expense icons
            case "restaurant":
            case "food":
            case "dining":
                return R.drawable.ic_restaurant_24;
            case "directions_car":
            case "transport":
                return R.drawable.ic_directions_car_24;
            case "home":
            case "rent":
            case "housing":
                return R.drawable.ic_home_24;
            case "shopping_bag":
            case "shopping":
                return R.drawable.ic_shopping_bag_24;
            case "movie":
            case "entertainment":
                return R.drawable.ic_movie_24;
            case "local_hospital":
            case "healthcare":
                return R.drawable.ic_home_24;
            case "school":
            case "education":
                return R.drawable.ic_education_24;
            case "electrical_services":
            case "utilities":
                return R.drawable.ic_bills_24;
            case "security":
            case "insurance":
                return R.drawable.ic_backup_24;
            case "remove_circle":
                return R.drawable.ic_remove_24;
            case "beauty":
                return R.drawable.ic_beauty_24;
            case "donation":
                return R.drawable.ic_donation_24;
            case "smartphone":
            case "phone":
                return R.drawable.ic_smartphone_24;
                
            // Default
            default:
                return R.drawable.ic_category_24;
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvCategoryType;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryType = itemView.findViewById(R.id.tv_category_type);
        }
    }
}
