package com.example.pigmy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.example.pigmy.databinding.FragmentTransactionBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class TransactionHistoryFragment extends Fragment {
    private final ArrayList<DepositDetails> depositList = new ArrayList<>();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FragmentTransactionBinding mBinding;
    private SharedPreferences preferences = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTransactionBinding.inflate(inflater, container, false);
        preferences = requireActivity().getSharedPreferences(AppConstants.USER_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getTransactions();
    }

    private void getTransactions() {
        CollectionReference collectionReference = firebaseFirestore.collection(AppConstants.TRANSACTION_COLLECTION);
        Query q = collectionReference.whereEqualTo("agentName", preferences.getString(AppConstants.USER_NAME, ""));
        q.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                depositList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("Testing", "getTransactions: " + document);
                    String date = document.getString("date");
                    String code = document.getString("code");
                    String accNo = document.getString("accNo");
                    String name = document.getString("name");
                    String agentName = document.getString("agentName");
                    Double prevAmount = document.getDouble("prevAmount");
                    String accType = document.getString("accType");
                    Double depAmount = document.getDouble("depAmount");
                    String key = document.getId();
                    depositList.add(new DepositDetails(date, name, depAmount, accType, code, accNo, agentName, prevAmount, key, 0));
                }
                Log.d("Testing", "getCustomers: " + depositList);
                Log.d("Testing", "getCustomers: " + depositList.size());

                ArrayList<DepositDetails> depositList1 = new ArrayList<>();
                double totalAgentCollection;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    totalAgentCollection = depositList.stream().mapToDouble(x -> x.depAmount).sum();
                    for(DepositDetails depositDetails : depositList){
                        depositDetails.totalAgentCollection = totalAgentCollection;
                        depositList1.add(depositDetails);
                    }
                }

                Collections.sort(depositList1);
                mBinding.transactionHistoryRv.setAdapter(transactionHistoryAdapter);
                transactionHistoryAdapter.submitList(depositList1);
            }
        });
    }

    private final TransactionHistoryAdapter transactionHistoryAdapter = new TransactionHistoryAdapter(
            new DiffUtil.ItemCallback<DepositDetails>() {
                @Override
                public boolean areItemsTheSame(@NonNull DepositDetails oldItem, @NonNull DepositDetails newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DepositDetails oldItem, @NonNull DepositDetails newItem) {
                    return oldItem.key.equals(newItem.key);
                }
            }, depositDetails -> firebaseFirestore.collection(AppConstants.TRANSACTION_COLLECTION)
            .document(String.valueOf(depositDetails.key))
            .delete()
            .addOnSuccessListener(unused -> getTransactions())
            .addOnFailureListener(e -> Toast.makeText(requireContext(), "something went wrong.", Toast.LENGTH_LONG).show()));
}
