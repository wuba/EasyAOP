package com.wuba.proxy;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by wswenyue on 2022/3/1.
 */
public final class TestProxy {
    private static final String TAG = "TestProxy";

    public List<PackageInfo> getInstalledPackages(PackageManager packageManager, int flags) {
        Log.e(TAG, "getInstalledPackages call");
        // Add your own code logic here
        // return Collections.emptyList();
        return packageManager.getInstalledPackages(flags);
    }

    @SuppressLint("MissingPermission")
    public static CellLocation getCellLocation(TelephonyManager telephonyManager) {
        return null;
    }

    @SuppressLint("MissingPermission")
    public static List<CellInfo> getAllCellInfo(TelephonyManager telephonyManager) {
        return null;
    }

    public static String getLine1Number(TelephonyManager telephonyManager) {
        Log.d(TAG, "getLine1Number: ");
        return "";
    }

    public static String getLine1Number(TelephonyManager telephonyManager, int subId) {
        Log.d(TAG, "getLine1Number1: ");
        return "";
    }

    public static String getImei(TelephonyManager telephonyManager) {
        Log.d(TAG, "getImei() called with: telephonyManager = [" + telephonyManager + "]");
        return "";
    }

    public static String getImei(TelephonyManager telephonyManager, int slotIndex) {
        Log.d(TAG, "getImei() called with: telephonyManager = [" + telephonyManager + "], slotIndex = [" + slotIndex + "]");
        return "";
    }

    public static String getDeviceId(TelephonyManager telephonyManager) {
        Log.d(TAG, "getDeviceId() called with: telephonyManager = [" + telephonyManager + "]");
        return "";
    }

    public static String getDeviceId(TelephonyManager telephonyManager, int slotIndex) {
        Log.d(TAG, "getDeviceId() called with: telephonyManager = [" + telephonyManager + "], slotIndex = [" + slotIndex + "]");
        return "";
    }

    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            Log.e(TAG, "invoke==>" + method.toString());
            if (method.getDeclaringClass().getName().equals("android.telephony.TelephonyManager") && method.getName().equals("getImei")) {
                return "fakeImei";
            }
            return method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
