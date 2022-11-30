package com.wuba.easyaopdemo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by wswenyue on 2022/2/25.
 */
public class Waiter {
    private static final String TAG = "Waiter";

    public static void drink(Context context) {
        Toast.makeText(context, "服务员给你上了" + new Waiter().tea("红茶"), Toast.LENGTH_SHORT).show();
    }

    public static void drinkTea(Context context) {
        Toast.makeText(context, "服务员给你上了" + new Waiter().tea("红茶"), Toast.LENGTH_SHORT).show();
    }

    public String tea(String type) {
        return "一杯" + type;
    }

    public String coffee() {
        return "一杯咖啡";
    }

    public void boilWater() {
        Log.e(TAG, "烧开水");
    }

    public void addTea() {
        Log.e(TAG, "加茶叶");
    }

    public void waitingTime(int time) {
        Log.e(TAG, "泡茶几分钟");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(time));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitingErrorTime() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(new Random().nextInt(3)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "泡茶时间不合适，得到了口味不好的茶");
    }

    public void makeTeaFinish() {
        Log.e(TAG, "茶好了，请用茶");
    }

    public void makeTea(int time) {
        boilWater();
        addTea();
        waitingTime(time);
        waitingErrorTime();
        makeTeaFinish();
    }

}
