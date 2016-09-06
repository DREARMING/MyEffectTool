package com.mvp.myeffecttools.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.bean.AppInfo;
import com.mvp.myeffecttools.bean.LockApp;
import com.mvp.myeffecttools.bean.ProcessInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by 爱的LUICKY on 2016/8/23.
 */
public class SystemUtils {
    private static final String TAG = "MVP";
    private static List<LockApp> lockList;
    private static List<LockApp> unLockList;
    /*
    * root@generic_x86:/proc # cat meminfo
        cat meminfo
        MemTotal:         900228 kB   第一行就是总内存

    * */
    public static long getTotalMemory(Context context){
        File file = new File("/proc/meminfo");
        if(!file.exists()){
           return 0;
        }
        BufferedReader reader = null;
        try {
             reader = new BufferedReader(new FileReader(file));
            String totalMem = reader.readLine();
            totalMem = totalMem.substring(totalMem.indexOf(":")+1,totalMem.indexOf("kB")).trim();
             return Long.parseLong(totalMem)*1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long  getFreeMem(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //封装内存信息到memoryInfo中
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public static long getSdFreeSize(){
        return Environment.getExternalStorageDirectory().getFreeSpace();
    }

    public static long getPhoneFreeSpace(){
        return  Environment.getDataDirectory().getFreeSpace();
    }

    /*
    * 获取进程的数量
    *
    * */
    public static int getRunningProcessNum(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        return list.size();
    }

    public static List<ProcessInfo> getAllRunningProcess(Context context) {
        List<ProcessInfo> mList = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        PackageManager pm = context.getPackageManager();

        for (ActivityManager.RunningAppProcessInfo info : list) {
            ProcessInfo mProcessInfo = new ProcessInfo();
            //进程的名字就是其所在应用的包名
            mProcessInfo.setPackageName(info.processName);
            //根据进程id来获取他占用的内存信息；
            Debug.MemoryInfo[] memInfos = am.getProcessMemoryInfo(new int[]{info.pid});
            mProcessInfo.setRamDirty(memInfos[0].getTotalPrivateDirty());
            try {
                //通过包名从PackageManager里面获取到包的信息！包里面封装这应用的信息
                PackageInfo packageInfo = pm.getPackageInfo(info.processName, 0);
                mProcessInfo.setProcessName(packageInfo.applicationInfo.loadLabel(pm).toString());
                //根据标志判断是否是系统级别，还是用户级别；
                int flag = packageInfo.applicationInfo.flags;

                if((flag & ApplicationInfo.FLAG_SYSTEM) == 0){
                    mProcessInfo.setUserProcess(true);
                }else{
                    mProcessInfo.setUserProcess(false);
                }


                Drawable temp = packageInfo.applicationInfo.loadIcon(pm);
                if (temp == null) {
                    //因为有一些系统应用是不存在icon的；这时候要给一张默认的icon
                    temp = context.getResources().getDrawable(R.drawable.ic_launcher);
                }
                mProcessInfo.setIcon(temp);
                mList.add(mProcessInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mList;
    }


    public static  void killProcess(Context context,String packageName){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
    }



    public static  void killProcesses(Context context,List<ProcessInfo> list){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if(list!=null) {
            for (ProcessInfo info : list) {
                am.killBackgroundProcesses(info.getPackageName());
            }
        }
    }

    public static boolean isSystemApp(Context context,int flag){
        //判断应用的flag是否是系统应用
        if((ApplicationInfo.FLAG_SYSTEM & flag) == 0){
            return false;
        }
        return true;
    }

    public static boolean isSdcardApp(Context context,int flag){
        //判断应用的flag是否是sd卡应用
        if((ApplicationInfo.FLAG_EXTERNAL_STORAGE & flag) == 0){
            return false;
        }
        return true;
    }

    public static List<AppInfo> getAllInstalledApp(Context context){
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appList = pm.getInstalledApplications(0);
        List<AppInfo> MyAppList = new ArrayList<AppInfo>();
        for (ApplicationInfo info:appList) {
            AppInfo app = new AppInfo();
            app.packageName = info.packageName;
            Drawable icon = info.loadIcon(pm);
            if(icon == null){
                icon = context.getResources().getDrawable(R.drawable.ic_launcher);
            }
            app.icon = icon;
            app.appName = info.loadLabel(pm).toString();
            app.isSystemApp = isSystemApp(context,info.flags);
            app.isSdCardApp = isSdcardApp(context,info.flags);
            app.apkPath = info.sourceDir;

            File file = new File(info.sourceDir);
            long length = file.length();
            if(length>0){
                app.appSize = length;
            }
            MyAppList.add(app);
        }
        return MyAppList;
    }

    public static List<LockApp> getLockApps(Context context){
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appList = pm.getInstalledApplications(0);
        List<LockApp> MyAppList = new ArrayList<LockApp>();
        for (ApplicationInfo info:appList) {
            LockApp app = new LockApp();
            app.packageName = info.packageName;
            Drawable icon = info.loadIcon(pm);
            if(icon == null){
                icon = context.getResources().getDrawable(R.drawable.ic_launcher);
            }
            app.icon = icon;
            app.appName = info.loadLabel(pm).toString();
            app.isSystemApp = isSystemApp(context,info.flags);
            app.isLock = false;
            MyAppList.add(app);
        }
        return MyAppList;
    }

    public static void initOrderApps(Context context/*List<LockApp> lockList, List<LockApp> unLockList*/){
        lockList = new ArrayList<>();
        unLockList = new ArrayList<>();
        List<LockApp> mList = getLockApps(context);
        Log.e("MVP","Mlist size is :" + mList.size());
        List<com.mvp.myeffecttools.dao.LockApp> mLockList = DaoUtils.getLockApps(context);
        if(mList!=null && mLockList.size()>0){
            for (LockApp item : mList) {
                boolean flag = false;
                for (com.mvp.myeffecttools.dao.LockApp lockItem:mLockList) {
                    if(item.packageName.equals(lockItem.getPackageName())){
                        flag = true;
                        item.isLock = true;
                        lockList.add(item);
                    }
                }
                if(!flag){
                    unLockList.add(item);
                }
            }

        }else {
            unLockList = mList;
            Log.e("MVP","unlockLIst size is :" + unLockList.size());
        }
        //unLockList = dividedSystemApp(unLockList);
    }

    public static List<LockApp> getLockList(){
        return lockList;
    }

    public static List<LockApp> getUnLockList(){
        return unLockList;
    }

    public  static List<LockApp> dividedSystemApp(List<LockApp> lockList){
        List<LockApp> appList = new ArrayList<>();
        List<LockApp> userApp = new ArrayList<>();
        List<LockApp> systemApp = new ArrayList<>();
        for (LockApp item:lockList) {
            if(item.isSystemApp){
                systemApp.add(item);
            }else {
                userApp.add(item);
            }
            //if(item)
        }
        appList.addAll(userApp);
        appList.addAll(systemApp);
        Log.e("MVP",appList.size()+"");
        return appList;
    }

}
