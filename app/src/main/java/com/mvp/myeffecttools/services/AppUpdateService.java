package com.mvp.myeffecttools.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.mvp.myeffecttools.R;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

public class AppUpdateService extends IntentService {

    private Notification.Builder mBuilder;
    private static final String TAG = "MVP";
    private Context mContext;
    private NotificationManager notifyManager;

    public AppUpdateService() {
        super("UpdateService");
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if("com.mvp.myeffecttool.updateApp".equals(action)){
            final String downloadUrl = intent.getStringExtra("DownloadUrl");
            RequestParams params = new RequestParams(downloadUrl);
            final String apkName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
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

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), apkName)),
                            "application/vnd.android.package-archive");

                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    notification.contentIntent = pendingIntent;
                    notifyManager.notify(10, notification);
                    stopSelf();
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

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
    }
}
