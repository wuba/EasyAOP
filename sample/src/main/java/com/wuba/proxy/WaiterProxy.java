package com.wuba.proxy;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.wuba.easyaopdemo.Waiter;

/**
 * Created by wswenyue on 2022/2/25.
 */
public final class WaiterProxy {
    public static final String TAG = "WaiterProxy";

    public static void drinkBeer(Context context) {
        Toast.makeText(context, "服务员竟然给你上了一杯啤🍺", Toast.LENGTH_SHORT).show();
    }

    public static String specialTea(Waiter waiter, String type) {
        return waiter.coffee() + "和" + waiter.tea(type);
    }

    /**
     * @param isStatic   是否是静态方法
     * @param classPath  eg：“com.wuba.easyaopdemo.Waiter.class”
     * @param methodName eg：“makeTea”
     * @param methodDesc eg：“(I)V”
     * @param args       如果是非静态方法，第一个参数是对象
     */
    public static void insertMethodEnter(boolean isStatic, String classPath, String methodName, String methodDesc, Object[] args) {
        if (!isStatic && args != null) {
            Waiter waiterObj = (Waiter) args[0];
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call begin===============", waiterObj.hashCode(), methodName));
            for (int i = 1; i < args.length; i++) {
                Log.d(TAG, String.format("args[%d]==>%s", i, args[i]));
            }
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call begin===============", waiterObj.hashCode(), methodName));
        }
    }

    public static void insertMethodExit(boolean isStatic, String classPath, String methodName, String methodDesc, Object[] args) {
        if (!isStatic && args != null) {
            Waiter waiterObj = (Waiter) args[0];
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call end===============", waiterObj.hashCode(), methodName));
            for (int i = 1; i < args.length; i++) {
                Log.d(TAG, String.format("args[%d]==>%s", i, args[i]));
            }
            Log.d(TAG, String.format("=========Waiter[%d]====(%s)==call end===============", waiterObj.hashCode(), methodName));
        }
    }

}
