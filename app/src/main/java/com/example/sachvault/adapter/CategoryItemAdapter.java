package com.example.sachvault.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;
import com.example.sachvault.model.CategoryItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ViewHolder> {

    private List<CategoryItem> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem category, int position);
        void onCategoryMenuClick(CategoryItem category, int position);
    }

    public CategoryItemAdapter() {
        this.categories = new ArrayList<>();
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void updateCategories(List<CategoryItem> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem category = categories.get(position);
        holder.bind(category, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCategoryIcon;
        private TextView tvCategoryName;
        private ImageButton btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            btnMore = itemView.findViewById(R.id.btn_more);
        }

        public void bind(CategoryItem category, int position) {
            tvCategoryName.setText(category.getName());
            ivCategoryIcon.setImageResource(category.getIconResource());

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });

            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryMenuClick(category, position);
                }
            });

            // Show more button on hover/focus effect
            itemView.setOnHoverListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_HOVER_ENTER:
                        btnMore.setVisibility(View.VISIBLE);
                        break;
                    case android.view.MotionEvent.ACTION_HOVER_EXIT:
                        btnMore.setVisibility(View.GONE);
                        break;
                }
                return false;
            });
        }
    }
}