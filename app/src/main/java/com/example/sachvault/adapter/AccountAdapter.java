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
import com.example.sachvault.database.entities.Account;
import com.example.sachvault.databinding.ItemAccountBinding;
import com.example.sachvault.utils.CurrencyUtils;

public class AccountAdapter extends ListAdapter<Account, AccountAdapter.AccountViewHolder> {
    
    private OnItemClickListener onItemClickListener;
    private OnMenuClickListener onMenuClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(Account account);
    }
    
    public interface OnMenuClickListener {
        void onMenuClick(Account account, View view);
    }
    
    public AccountAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<Account> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<Account>() {
            @Override
            public boolean areItemsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
                return oldItem.getId() == newItem.getId();
            }
            
            @Override
            public boolean areContentsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getType().equals(newItem.getType()) &&
                       oldItem.getBalance() == newItem.getBalance() &&
                       oldItem.isActive() == newItem.isActive();
            }
        };
    
    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccountBinding binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AccountViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = getItem(position);
        holder.bind(account);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.onMenuClickListener = listener;
    }
    
    class AccountViewHolder extends RecyclerView.ViewHolder {
        private final ItemAccountBinding binding;
        
        public AccountViewHolder(@NonNull ItemAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(getItem(position));
                }
            });
            
            // Set menu button click listener
            binding.btnAccountMenu.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onMenuClickListener != null) {
                    onMenuClickListener.onMenuClick(getItem(position), v);
                }
            });
        }
        
        public void bind(Account account) {
            if (account == null) {
                return;
            }
            
            // Set account name with null safety
            String accountName = account.getName() != null ? account.getName() : "Unknown Account";
            binding.tvAccountName.setText(accountName);
            
            // Set account balance with null safety
            try {
                String formattedBalance = CurrencyUtils.formatCurrencyWithCommas(account.getBalance());
                binding.tvAccountBalance.setText(formattedBalance);
            } catch (Exception e) {
                binding.tvAccountBalance.setText("Rs 0.00");
            }
            
            // Set balance color based on positive/negative
            int balanceColor;
            try {
                if (account.getBalance() >= 0) {
                    balanceColor = ContextCompat.getColor(itemView.getContext(), R.color.color_on_surface);
                } else {
                    balanceColor = ContextCompat.getColor(itemView.getContext(), R.color.color_expense);
                }
                binding.tvAccountBalance.setTextColor(balanceColor);
            } catch (Exception e) {
                // Use default color if there's an issue
                balanceColor = ContextCompat.getColor(itemView.getContext(), R.color.color_on_surface);
                binding.tvAccountBalance.setTextColor(balanceColor);
            }
            
            // Set account icon with null safety
            String accountType = account.getType() != null ? account.getType() : "bank";
            setAccountIcon(accountType);
        }
        
        private void setAccountIcon(String accountType) {
            int iconRes;
            
            try {
                // Map account types to icons with null safety
                if (accountType == null) {
                    iconRes = R.drawable.ic_account_balance_24;
                } else {
                    switch (accountType.toLowerCase()) {
                        case "bank":
                            iconRes = R.drawable.ic_account_balance_24;
                            break;
                        case "card":
                            iconRes = R.drawable.ic_credit_card_24;
                            break;
                        case "cash":
                            iconRes = R.drawable.ic_payments_24;
                            break;
                        case "savings":
                            iconRes = R.drawable.ic_savings_24;
                            break;
                        case "upi":
                            iconRes = R.drawable.ic_qr_code_scanner_24;
                            break;
                        default:
                            iconRes = R.drawable.ic_account_balance_24;
                            break;
                    }
                }
                
                binding.ivAccountIcon.setImageResource(iconRes);
            } catch (Exception e) {
                // Use default icon if there's any issue
                binding.ivAccountIcon.setImageResource(R.drawable.ic_account_balance_24);
            }
        }
    }
}