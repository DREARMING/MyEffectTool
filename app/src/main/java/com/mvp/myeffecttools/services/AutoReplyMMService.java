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
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.utils.PhoneController;

import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/9/1.
 */
public class AutoReplyMMService extends AccessibilityService {


    private static final String TAG = "MVP" ;
    private static String MPacketName = "com.tencent.mm";
    private static final String TalkPage = "com.tencent.mm.ui.LauncherUI";
    private boolean touchNotificatin = false;
    private PowerManager pm;
    private KeyguardManager.KeyguardLock keyguardLock;
    private KeyguardManager km;
    private ActivityManager am;
    private Handler mHandler = new Handler();
    //制定等待多长时间，开始indEditText；
    private final long TimeOut = 1000;

    /*
    * logic
    * Notification 能打开的条件是：屏幕点亮并且已经解锁
    * 1.判断屏幕和锁是否开了，如果开了就解锁并唤醒屏幕
    * 2.通过getParcelableData()获取Notification对象(指的是刚刚微信来的通知）；
    * 3.判断是否是前台获取了通知，如果不是，点开通知，设置已经点击的action，进去聊天界面 ： 跳到步骤9
    * 4.如果是前台界面，点开通知，设置已经点击的action
    * 5.延迟1秒钟，确保打开了聊天界面后
    * 6.查找EditText，并填充数据进去
    * 7.查找发送按钮，performAction（click);
    * 8.返回键；
    *
    * 9.检测窗口状态改变（目的检测是否从后台跳到聊天界面，还是从前台直接进入聊天界面）
    * 10.检测是否从通知跳转来的，如果不是：证明是自己在微信内操作，引起的窗口改变，不加以处理
    * 11.判断当前是不是前台跳转的，还是后台进来的；（因为前台已经填充好数据，并且发送了，就不加以处理；）标志类名是 launcher.ui
    * 12.如果是后台的跳转过来的，则重复6.7步骤
    * 13。Home 键
    *
    * 14.结束，清理设置；
    * */

    @SuppressLint("NewApi")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isAutoReply =  mPref.getBoolean(getResources().getString(R.string.auto_reply),false);
        if(!isAutoReply) return;
        Log.e(TAG,"auto reply : " + true);
        int eventType = event.getEventType();
        switch (eventType){
            //当通知栏状态发生改变时
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.e(TAG,"receive a notification");
                if(PhoneController.isLockScreen(this)){
                    Log.e(TAG,"lock sceen");
                    PhoneController.wakeAndUnlockScreen(this);
                }
                Notification notification = getNotification(event);
                if(notification == null || isLuckyMoneyNotification(notification))return;
                touchNotification(notification);
                break;
            default:
                Log.e(TAG,"tpye window state change");
                if(touchNotificatin) {
                    try {
                        Thread.sleep(TimeOut); //1秒后，确保进到聊天界面
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (fill()) {
                        send();
                    }
                    SystemClock.sleep(1000);
                    PressBack();
                    touchNotificatin = false;
                }
                break;
        }
    }


    private boolean isLuckyMoneyNotification(Notification notification){
        String content = notification.tickerText.toString();
        if(content.contains("[微信红包]")){
            Log.e(TAG,content);
            return true;
        }
        return false;
    }

   /* private boolean isForegroundApp(){
        //android 5.0 以后废弃掉了，无法获取前台的应用；
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //获取处于前台的栈，通过它栈顶的activity获取其包名，然后跟微信包名比较，就可以清楚微信是否处于前台
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
            for(ActivityManager.RunningAppProcessInfo info:runningAppProcesses){
                if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    for(String activieProcess:info.pkgList){
                        if(activieProcess.equals(MPacketName)){
                            return true;
                        }
                    }
                }
            }
        }else {
            String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
            if (packageName.equals(MPacketName)) {
                return true;
            }
        }
        return false;
    }*/

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void send(){
        AccessibilityNodeInfo root = getRootInActiveWindow();
        List<AccessibilityNodeInfo> mList;
        mList = root.findAccessibilityNodeInfosByText("发送");
        if(mList != null && mList.size()>0) {
        }else {
            mList = root.findAccessibilityNodeInfosByText("Send");
        }

        for (AccessibilityNodeInfo info : mList) {
            if ("android.widget.Button".equals(info.getClassName())&&info.isEnabled()) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean fill(){
        Log.e(TAG,"fill?");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null){
            Log.e(TAG,"can't fill,nodeInfo is null");
            return false;
        }
        return findEditText(nodeInfo);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean findEditText(AccessibilityNodeInfo rootNode){
        int count = rootNode.getChildCount();
        for(int i = 0 ; i < count ; i++){
            AccessibilityNodeInfo item = rootNode.getChild(i);
            if(item == null){
                continue;
            }
            if("android.widget.EditText".equals(item.getClassName())){
                Log.e(TAG,"find the EditText");
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                item.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                item.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

                String content = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.auto_reply_content),"I am codding,reply you later");
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                item.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }
            //递归查找
            if(findEditText(item)){
                return true;
            }
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
