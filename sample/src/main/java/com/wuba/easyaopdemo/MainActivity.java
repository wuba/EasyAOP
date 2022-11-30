package com.wuba.easyaopdemo;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickMe(View view) {
        test();
        switch (view.getId()) {
            case R.id.btn_1:
                Waiter.drink(view.getContext());
                break;
            case R.id.btn_2:
                Waiter.drinkTea(view.getContext());
                break;
            case R.id.btn_3:
                new Waiter().makeTea(1);
                break;
        }

    }


    private void test() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Log.e("TelephonyManager", "imei==>" + telephonyManager.getImei());
//        }
        try {
            Method method = telephonyManager.getClass().getMethod("getImei");
            String imei = (String) method.invoke(telephonyManager);
            Log.e("TelephonyManager", "imei=reflect=>" + imei);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}