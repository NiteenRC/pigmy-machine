package com.example.pigmy;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Account implements Parcelable {

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
    private String name, type, plusMinus, accNo;
    private int id;

    private double prevAmount;
    private boolean act;

    public Account() {

    }

    protected Account(Parcel in) {
        name = in.readString();
        type = in.readString();
        plusMinus = in.readString();
        accNo = in.readString();
        id = in.readInt();
        prevAmount = in.readDouble();
        act = in.readByte() != 0;
    }

    public String getPlusMinus() {
        return plusMinus;
    }

    public void setPlusMinus(String plusMinus) {
        this.plusMinus = plusMinus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrevAmount() {
        return prevAmount;
    }

    public void setPrevAmount(double prevAmount) {
        this.prevAmount = prevAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    @NonNull
    @Override
    public String toString() {
        return name.concat(" - ").concat(String.valueOf(accNo));
    }

    public boolean isAct() {
        return act;
    }

    public void setAct(boolean act) {
        this.act = act;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(plusMinus);
        parcel.writeString(accNo);
        parcel.writeInt(id);
        parcel.writeDouble(prevAmount);
        parcel.writeByte((byte) (act ? 1 : 0));
    }
}
