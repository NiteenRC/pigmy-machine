package com.example.pigmy;

import androidx.annotation.NonNull;

public class DepositDetails {
    final String date;
    final String name;
    final Double depAmount;
    final String accType;
    final String code;
    final String accNo;
    final String agentName;
    final Double prevAmount;
    final String key;

    public DepositDetails(String date,
                          String name,
                          Double depositAmount,
                          String accountType,
                          String code,
                          String accNo,
                          String agentName,
                          Double prevAmount,
                          String key
    ) {
        this.date = date;
        this.name = name;
        this.accType = accountType;
        this.depAmount = depositAmount;
        this.code = code;
        this.accNo = accNo;
        this.agentName = agentName;
        this.prevAmount = prevAmount;
        this.key = key;
    }

    @NonNull
    @Override
    public String toString() {
        return "DepositDetails: { " +
                "name: " + name +
                "date = " + date +
                "accType = " + accType +
                "depAmount = " + depAmount +
                "code = " + code +
                "accNo = " + accNo +
                "agentName = " + agentName +
                "prevAmount = " + prevAmount+
                "key = " + key+
                "}";
    }

}
