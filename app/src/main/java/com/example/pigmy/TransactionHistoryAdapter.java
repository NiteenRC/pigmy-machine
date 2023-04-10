package com.example.pigmy;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class TransactionHistoryAdapter extends ListAdapter<DepositDetails, DepositViewHolder> {

    private final HistoryClickListener listener;

    protected TransactionHistoryAdapter(@NonNull DiffUtil.ItemCallback<DepositDetails> diffCallback, HistoryClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    @NonNull
    @Override
    public DepositViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return DepositViewHolder.fromParent(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull DepositViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }
}





