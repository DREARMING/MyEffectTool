package com.mvp.myeffecttools.activities;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.adapter.LockAppAdapter;
import com.mvp.myeffecttools.bean.LockApp;
import com.mvp.myeffecttools.interfaces.DaoListener;
import com.mvp.myeffecttools.interfaces.LockAppListener;
import com.mvp.myeffecttools.services.MonitorService;
import com.mvp.myeffecttools.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;

public class LockAppsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mPb;
    private List<LockApp> lockList;
    private List<LockApp> unLockList;
    private Context mContext;
    private static final int SUCCESS_QUERY = 2;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SUCCESS_QUERY:
                    mPb.setVisibility(View.GONE);
                    bindAdapter();
                    break;
            }
        }
    };
    private LockAppAdapter adapter;
    private Intent intent;
    private MonitorServiceConnection connection;
    private DaoListener myBinder;

    private void bindAdapter() {
        intent = new Intent();
        intent.setClass(mContext, MonitorService.class);
        startService(intent);
        connection = new MonitorServiceConnection();
        bindService(intent,connection, Service.BIND_AUTO_CREATE);
        adapter = new LockAppAdapter(mContext,lockList,unLockList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter.setOnLockAppListener(new LockAppListener() {
            @Override
            public void onCatchListener() {
                if(myBinder!=null) {
                    myBinder.doInsert();
                }
            }

            @Override
            public void onInsertListener() {
                if(myBinder!=null){
                    myBinder.doInsert();
                }
            }

            @Override
            public void onDeleteListener() {
                if(myBinder!=null){
                    myBinder.doDelete();
                }
            }
        });
    }

    class MonitorServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (DaoListener) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_apps);
        mContext = this;
        initView();
        requestData();
    }

    private void requestData() {
        lockList = new ArrayList<>();
        unLockList =  new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemUtils.initOrderApps(mContext);
                lockList = SystemUtils.getLockList();
                unLockList = SystemUtils.getUnLockList();
                unLockList = SystemUtils.dividedSystemApp(unLockList);
                mHandler.sendEmptyMessage(SUCCESS_QUERY);
            }
        }).start();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.lock_recyclerview);
        mPb = (ProgressBar) findViewById(R.id.lock_progressbar);
    }

    @Override
    protected void onDestroy() {
        if(connection!=null) {
            unbindService(connection);
        }
        super.onDestroy();
    }
}
