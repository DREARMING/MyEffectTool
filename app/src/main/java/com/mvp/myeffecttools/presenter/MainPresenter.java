package com.mvp.myeffecttools.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mvp.myeffecttools.bean.VersionBean;
import com.mvp.myeffecttools.model.VersionListener;
import com.mvp.myeffecttools.model.VersionModel;
import com.mvp.myeffecttools.view_interface.Main_UI_Interface;

/**
 * Created by 爱的LUICKY on 2016/9/11.
 */
public class MainPresenter extends BasePresenter<Main_UI_Interface> {

    private Context mContext;
    private Handler mHandler;
    private VersionModel versionChecker;
    private Handler mHander = new Handler(Looper.getMainLooper());

    public MainPresenter(Context context){
        mContext = context;
    }


    public void initListener(){
        versionChecker.setVersionListener(new VersionListener() {

            @Override
            public void OnUpadte(final VersionBean version) {
                mHander.post(new Runnable() {
                    @Override
                    public void run() {
                        views.showUpdateDialog(version);
                    }
                });
            }

            @Override
            public void OnNormal() {

            }

            @Override
            public void OnError() {
                views.showMessage("网络没有连接，无法进行版本更新");
            }
        });
    }

    public void CheckVersion() {
        versionChecker = new VersionModel(mContext);
        initListener();
        versionChecker.CheckVersion();
    }

    public void downloadNewVersion(String url){
        versionChecker.downLoad(url);
    }

}
