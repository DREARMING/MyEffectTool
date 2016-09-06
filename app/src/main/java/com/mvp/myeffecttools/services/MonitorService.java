package com.mvp.myeffecttools.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mvp.myeffecttools.activities.LockActivity;
import com.mvp.myeffecttools.dao.LockApp;
import com.mvp.myeffecttools.interfaces.DaoListener;
import com.mvp.myeffecttools.utils.DaoUtils;

import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/30.
 */
public class MonitorService extends Service {
    private static final String TAG="MVP";
    //存放着设置保护锁的应用
    private volatile List<LockApp> mList;
    //监测服务开启的标志；
    private volatile boolean isOpen = false;
    //用来判断是否用户设置了解锁一个应用，其他应用不需要密码
    private volatile boolean keys = false;
    private volatile boolean keyOne = false;
    private Context mContext;
    private ActivityManager am;
    private MyThread mThread;
    private ScreenState receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mList =  DaoUtils.getLockApps(mContext);
        isOpen = true;
        keys =  PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("keys",false);
        startMonitor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction("com.mvp.monitor.keys.open");
        filter.addAction("com.mvp.monitor.keys.close");
        filter.addAction("com.mvp.monitor.lock");
        receiver = new ScreenState();
        registerReceiver(receiver,filter);
    }

    private void startMonitor(){
        mThread = new MyThread();
        mThread.start();
    }

    class ScreenState extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_SCREEN_OFF)){
                Log.e(TAG,"screen off");
                isOpen = false;
                keyOne = false;
                mList = DaoUtils.getLockApps(mContext);
            }else if(action.equals(Intent.ACTION_SCREEN_ON)){
                Log.e(TAG,"screen on");
                startMonitor();
                isOpen = true;
                keyOne = false;
            }else if(action.equals("com.mvp.monitor.keys.open")){
                keys = true;
                mList = DaoUtils.getLockApps(mContext);
            }else if(action.equals("com.mvp.monitor.keys.close")){
                keys = false;
                mList = DaoUtils.getLockApps(mContext);
            }else if(action.equals("com.mvp.monitor.lock")){
                int index = intent.getIntExtra("index",-1);
                mList.get(index).setIsProtece(false);
            }
        }
    }


    class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            Log.e(TAG,"OnstartCommand run?");
            isOpen = true;
            while (isOpen) {
                ActivityManager.RunningTaskInfo taskInfo = am.getRunningTasks(1).get(0);
                String packageName = taskInfo.topActivity.getPackageName();
                if(mList.size() == 0){
                    isOpen = false;
                }
                for (int i = 0 ; i< mList.size();i++) {
                    LockApp item = mList.get(i);
                    if (item.getPackageName().equals(packageName)) {
                        //是否是解一次锁，保护的应用就不需要再填密码；
                        if (!keys) {
                            //倘若不是，检查所有保护的应用；
                            if (!item.getIsProtece()) {
                                item.setIsProtece(true);
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("LockAppNum",i);
                                intent.setClass(mContext, LockActivity.class);
                                startActivity(intent);
                            }
                        }else{
                            //如果是解锁一次就不再保护的情况
                            if(!keyOne){
                                if (!item.getIsProtece()) {
                                    keyOne = true;
                                    item.setIsProtece(true);
                                    Intent intent = new Intent();
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setClass(mContext, LockActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //if(mThread.isAlive())
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LockListService();
    }

    class LockListService extends Binder implements DaoListener{

        @Override
        public void doInsert() {
            keyOne = false;
            mList = DaoUtils.getLockApps(mContext);
            if(!isOpen){
                startMonitor();
            }
        }

        @Override
        public void doDelete() {
            mList = DaoUtils.getLockApps(mContext);

        }
    }

    @Override
    public void onDestroy() {
        isOpen = false;
        mList = null;
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
