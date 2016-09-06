package com.mvp.myeffecttools.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

public class PhoneController {

    private final static String TAG = "MVP";


    public static boolean isLockScreen(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }


    public static void wakeAndUnlockScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        wakeLock.acquire(1000); // 点亮屏幕
        wakeLock.release();

        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock lock = km.newKeyguardLock("unlock");
        lock.disableKeyguard(); // 解锁
        //10秒后释放lock；
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lock.reenableKeyguard();
            }
        }).start();
    }
}
