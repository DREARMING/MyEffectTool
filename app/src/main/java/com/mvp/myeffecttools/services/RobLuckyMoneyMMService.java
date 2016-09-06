package com.mvp.myeffecttools.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.utils.PhoneController;

import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/9/2.
 */
public class RobLuckyMoneyMMService extends AccessibilityService {
    private static final String TAG = "MVP" ;
    private static final String LUCKY_MONEY_OPEN_PAGE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String LUCKY_MONEY_DETIALS_PAGE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private static final String LUCKY_OPEN_BUTTON = "android.widget.Button";
    private boolean touchNotificatin = false;
    //制定等待多长时间，开始indEditText；
    private final long TimeOut = 500;
    private boolean isFind = false;

    /*
    * logic
    * Notification 能打开的条件是：屏幕点亮并且已经解锁
    * 1.判断屏幕和锁是否开了，如果开了就解锁并唤醒屏幕
    * 2.通过getParcelableData()获取Notification对象(指的是刚刚微信来的通知）；
    * 3.判断通知是否是红包通知？如果是，跳转到聊天界面，如果不是，不处理；
    * 4.监测窗口状态事件：
    *   是否是点通知跳过来的？如果不是，不加以处理，如果是，则进行如下判断：
    *       1. 聊天界面
    * */

    @SuppressLint("NewApi")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoReply =  mPref.getBoolean(getResources().getString(R.string.auto_red_pakage),false);
        if(!isAutoReply) return;
        int eventType = event.getEventType();
        switch (eventType){
            //当通知栏状态发生改变时
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Notification notification = getNotification(event);
                if((notification == null) || (!isLuckyMoneyNotification(notification))){
                    break;
                }

                if(PhoneController.isLockScreen(this)){
                    Log.e(TAG,"lock sceen");
                    PhoneController.wakeAndUnlockScreen(this);
                }
                touchNotification(notification);
                break;
            default:
                Log.e(TAG,"tpye window state change");
                if(touchNotificatin) {
                    String currentPage = event.getClassName().toString();
                    if(currentPage.equals(LUCKY_MONEY_OPEN_PAGE)){
                        Log.e(TAG,"红包中有钱");
                        openLuckyMoney();
                    }else if(currentPage.equals(LUCKY_MONEY_DETIALS_PAGE)){
                        Log.e(TAG,"Luck money,详细界面");
                        touchNotificatin = false;
                        isFind = false;
                        PressBack();
                        SystemClock.sleep(500);
                        PressBack();
                    }else {
                        if(isFind){
                            break;
                        }
                        try {
                            Thread.sleep(TimeOut); //1秒后，确保进到聊天界面
                            if(robLuckyMoney()){
                                isFind = false;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean robLuckyMoney(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> nodeList = rootNode.findAccessibilityNodeInfosByText("领取红包");
        int count = nodeList.size();
        //倒序查找最新的红包
        for(int i = count -1 ; i>=0; i--){
            AccessibilityNodeInfo parent = nodeList.get(i).getParent();
            while(parent != null){
                if(parent.isClickable() ){
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.e(TAG, "聊天页面，发现了一个红包哦！");
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean openLuckyMoney(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if(rootNode != null){
            return findOpenMoneyButton(rootNode);
        }
        return false;
    }

    private boolean findOpenMoneyButton(AccessibilityNodeInfo rootNode){
        int count = rootNode.getChildCount();
        for(int i = 0 ; i <count ; i++){
            AccessibilityNodeInfo item = rootNode.getChild(i);
            if(item == null){
                continue;
            }
            if(LUCKY_OPEN_BUTTON.equals(item.getClassName().toString())){
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return true;
            }

            if(findOpenMoneyButton(item)){
                return true;
            }
        }
        return false;
    }

    private boolean isLuckyMoneyNotification(Notification notification){
        String content = notification.tickerText.toString();
        if(content.contains("[微信红包]")){
            Log.e(TAG,content);
            return true;
        }
        return false;
    }


    private void touchNotification(Notification notification){
        if(notification == null){
            return;
        }
        PendingIntent contentIntent = notification.contentIntent;
        try {
            touchNotificatin = true;
            contentIntent.send();
            Log.e(TAG,"touch notification");
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

    }

    private Notification getNotification(AccessibilityEvent event){
        Parcelable data = event.getParcelableData();
        if(data != null && data instanceof Notification){
            Log.e(TAG,"notification is not null");
            Notification mNotification = (Notification) data;
            return mNotification;
        }

        return null;
    }



    @Override
    protected void onServiceConnected() {
        Log.e(TAG,"Service connected");
        super.onServiceConnected();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG,"service disConnect");
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void PressBack(){
        Log.e(TAG,"返回键");
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

}
