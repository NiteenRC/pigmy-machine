package com.example.pigmy;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pigmy.databinding.TransactionHistoryItemBinding;

public class DepositViewHolder extends RecyclerView.ViewHolder {

    private final TransactionHistoryItemBinding mBinding;

    public DepositViewHolder(TransactionHistoryItemBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static DepositViewHolder fromParent(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TransactionHistoryItemBinding binding = TransactionHistoryItemBinding.inflate(inflater, parent, false);
        return new DepositViewHolder(binding);
    }

    public void bind(DepositDetails item, HistoryClickListener listener) {
        mBinding.date.setText(item.date + "(" + item.totalAgentCollection + ")");
        mBinding.depositAmount.setText(item.depAmount.toString());
        //mBinding.accountType.setText(item.accNo +" & "+item.accType);
        mBinding.name.setText(item.name);
        mBinding.accNo.setText(item.accNo + "(" + item.accType + ")");
        //mBinding.agentName.setText(item.agentName);
        mBinding.prevAmount.setText(item.prevAmount.toString());
//        mBinding.container.setOnClickListener(v -> listener.onHistoryClick(item));
    }
}