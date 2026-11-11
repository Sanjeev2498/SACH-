package com.example.sachvault.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sachvault.R;
import com.example.sachvault.database.entities.Transaction;
import com.example.sachvault.databinding.ItemTransactionBinding;
import com.example.sachvault.utils.Constants;
import com.example.sachvault.utils.CurrencyUtils;
import com.example.sachvault.utils.DateUtils;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {
    
    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(Transaction transaction);
    }
    
    public TransactionAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.getId() == newItem.getId();
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.getAmount() == newItem.getAmount() &&
                       oldItem.getType().equals(newItem.getType()) &&
                       oldItem.getCategory().equals(newItem.getCategory()) &&
                       oldItem.getAccount().equals(newItem.getAccount()) &&
                       oldItem.getDate().equals(newItem.getDate()) &&
                       oldItem.getTime().equals(newItem.getTime()) &&
                       (oldItem.getNote() == null ? newItem.getNote() == null : 
                        oldItem.getNote().equals(newItem.getNote()));
            }
        };
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TransactionViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.bind(transaction);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }
    
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;
        
        public TransactionViewHolder(@NonNull ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(getItem(position));
                }
            });
            
            // Long click for delete (placeholder for now)
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(getItem(position));
                    return true;
                }
                return false;
            });
        }
        
        public void bind(Transaction transaction) {
            // Set category name
            binding.tvCategory.setText(transaction.getCategory());
            
            // Set account name
            binding.tvAccount.setText(transaction.getAccount());
            
            // Set note (show separator only if note exists)
            if (transaction.getNote() != null && !transaction.getNote().trim().isEmpty()) {
                binding.tvNote.setText(transaction.getNote());
                binding.tvNote.setVisibility(View.VISIBLE);
                binding.tvSeparator.setVisibility(View.VISIBLE);
            } else {
                binding.tvNote.setVisibility(View.GONE);
                binding.tvSeparator.setVisibility(View.GONE);
            }
            
            // Set date and time
            String displayDate = DateUtils.getRelativeDateString(transaction.getDate());
            String displayTime = DateUtils.formatTimeForDisplay(transaction.getTime());
            String dateTimeText = displayDate + " â€¢ " + displayTime;
            binding.tvDateTime.setText(dateTimeText);
            
            // Set amount with proper color coding
            String formattedAmount = CurrencyUtils.formatCurrencyWithCommas(transaction.getAmount());
            binding.tvAmount.setText(formattedAmount);
            
            // Set amount color based on transaction type
            int amountColor;
            if (transaction.isIncome()) {
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.color_income);
            } else if (transaction.isExpense()) {
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.color_expense);
            } else {
                amountColor = ContextCompat.getColor(itemView.getContext(), R.color.color_on_surface);
            }
            binding.tvAmount.setTextColor(amountColor);
            
            // Set category icon (placeholder for now - will need icon mapping)
            setCategoryIcon(transaction.getCategory(), transaction.getType());
        }
        
        private void setCategoryIcon(String category, String type) {
            // Default icon based on transaction type
            int iconRes = R.drawable.ic_receipt_24; // Default icon
            
            // Map categories to icons (simplified version)
            switch (category.toLowerCase()) {
                case "food":
                    iconRes = R.drawable.ic_restaurant_24;
                    break;
                case "transport":
                    iconRes = R.drawable.ic_directions_car_24;
                    break;
                case "shopping":
                    iconRes = R.drawable.ic_shopping_bag_24;
                    break;
                case "salary":
                    iconRes = R.drawable.ic_work_24;
                    break;
                case "business":
                    iconRes = R.drawable.ic_business_24;
                    break;
                default:
                    if (Constants.TRANSACTION_TYPE_INCOME.equals(type)) {
                        iconRes = R.drawable.ic_add_24;
                    } else {
                        iconRes = R.drawable.ic_remove_24;
                    }
                    break;
            }
            
            binding.ivCategoryIcon.setImageResource(iconRes);
        }
    }
}