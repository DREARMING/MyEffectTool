package com.mvp.myeffecttools.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mvp.myeffecttools.bean.VersionBean;
import com.mvp.myeffecttools.services.AppUpdateService;
import com.mvp.myeffecttools.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 爱的LUICKY on 2016/9/11.
 */
public class VersionModel {

    private VersionListener listener;
    private static final String TAG = "MVP";
    private Context mContext;
    private String IP = "http://10.201.34.48:8080/";
    private String versionFilePath = "version.json";

    public VersionModel(Context context){
        mContext = context;
    }

    public void  setVersionListener(VersionListener listener){
        this.listener = listener;
    }

    public void CheckVersion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                                PackageManager pm = mContext.getPackageManager();
                                PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), 0);
                                if (version1.getVersionCode() > packageInfo.versionCode) {
                                    listener.OnUpadte(version1);
                                } else {
                                    listener.OnNormal();
                                }
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    listener.OnError();
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downLoad(String url){
        Intent intent = new Intent();
        intent.setAction("com.mvp.myeffecttool.updateApp");
        intent.setClass(mContext,AppUpdateService.class);
        intent.putExtra("DownloadUrl",url);
        mContext.startService(intent);
    }

}
