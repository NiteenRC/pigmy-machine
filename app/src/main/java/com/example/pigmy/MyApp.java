package com.example.pigmy;

import android.app.Application;

import com.example.pigmy.pflockscreen.SharedPref;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new SharedPref(this);
    }
}
