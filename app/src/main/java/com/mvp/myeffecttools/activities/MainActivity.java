package com.mvp.myeffecttools.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.bean.VersionBean;
import com.mvp.myeffecttools.utils.StreamUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int UPDATE = 2;
    private static final int NORMAL = 1;
    private static final int ERROR = 3;
    private static final String TAG = "MVP";
    private Context mContext;

    private VersionBean version;
    private String IP = "http://10.201.34.48:8080/";
    private String versionFilePath = "version.json";

    private Notification.Builder mBuilder;
    private NotificationManager notifyManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int stat = msg.what;
            switch (stat) {
                case NORMAL:
                    break;
                case UPDATE:
                    version = (VersionBean) msg.obj;
                    showDialog();
                    break;
                case ERROR:
                    Toast.makeText(mContext, "网络没有连接，无法检测版本更新", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        x.Ext.init(getApplication());
        setTitle("进程管理");
        CheckVersion();
    }

    public void clickApplication(View view){
        Intent intent = new Intent();
        intent.setClass(this,ApplicationActivity.class);
        startActivity(intent);
    }

    public void clickProgress(View view){
        Intent intent = new Intent();
        intent.setClass(this,ProcessesActivity.class);
        startActivity(intent);
    }

    public void clickLockActivity(View view){
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!mPrefs.getBoolean("Lock",false)){
            showOpenLockDialog();
        }else{
            Intent intent = new Intent(mContext,LockActivity.class);
            intent.setAction("com.mvp.lockapps");
            startActivity(intent);
        }
    }

    private void showOpenLockDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("是否要打开程序锁？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPrefs.edit().putBoolean("Lock",true).commit();
                Intent intent = new Intent(mContext,LockActivity.class);
                intent.setAction("com.mvp.lockapps");
                startActivity(intent);
            }
        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext,"程序锁需要开启，才能使用此功能",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public void openSettingActivity(View view){
        Intent intent = new Intent();
        intent.setClass(this,SettingActivity.class);
        startActivity(intent);
    }

    private void showDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle("版本更新").setMessage(version.getDescription()).setPositiveButton("马上更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downLoad(version.getDownloadUrl());
            }
        }).setNegativeButton("暂时不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void downLoad(String url) {
        RequestParams params = new RequestParams(url);
        String downloadUrl = version.getDownloadUrl();
        String apkName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        File file = new File(Environment.getExternalStorageDirectory(), apkName);
        params.setSaveFilePath(file.getPath());
        x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e(TAG, "onError");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e(TAG, "onCancel");
            }

            @Override
            public void onFinished() {
                mBuilder.setProgress(0, 0, false);
                mBuilder.setContentText("Download Complete!Touch it to Install");
                Notification notification = mBuilder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                //安装Apk意图
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                String downloadUrl = version.getDownloadUrl();
                String apkName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), apkName)),
                        "application/vnd.android.package-archive");

                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                notification.contentIntent = pendingIntent;
                notifyManager.notify(10, notification);
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                mBuilder = new Notification.Builder(mContext);
                mBuilder.setSmallIcon(R.mipmap.icon);
               /* mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon));
                mBuilder.setPriority(Notification.PRIORITY_MAX);
                mBuilder.setVibrate(new long[]{200,300,200,400});
                mBuilder.setContentTitle("Download Apk");*/

                notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isDownloading) {
                    mBuilder.setProgress((int) total, (int) current, false);
                    notifyManager.notify(10, mBuilder.build());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void CheckVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = mHandler.obtainMessage();
                long enterTime = System.currentTimeMillis();
                try {
                    URL url = new URL(IP + versionFilePath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(2000);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        String versionString = StreamUtils.getStringFromStream(is);
                        if (!TextUtils.isEmpty(versionString)) {
                            Gson gson = new Gson();
                            VersionBean version1 = gson.fromJson(versionString, VersionBean.class);
                            if (version1 != null) {
                                PackageManager pm = getPackageManager();
                                PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
                                if (version1.getVersionCode() > packageInfo.versionCode) {
                                    msg.what = UPDATE;
                                    msg.obj = version1;
                                } else {
                                    msg.what = NORMAL;
                                }
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    msg.what = ERROR;
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    long time = System.currentTimeMillis() - enterTime;
                    if (time < 2000) {
                        try {
                            Thread.sleep(2000 - time);
                            mHandler.sendMessage(msg);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

}
