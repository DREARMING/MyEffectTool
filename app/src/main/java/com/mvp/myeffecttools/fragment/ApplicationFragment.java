package com.mvp.myeffecttools.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.activities.ApplicationActivity;
import com.mvp.myeffecttools.adapter.ApplicationRecycleAdapter;
import com.mvp.myeffecttools.adapter.RecycleViewWrapAdapter;
import com.mvp.myeffecttools.bean.AppInfo;
import com.mvp.myeffecttools.utils.SystemUtils;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/25.
 */
public class ApplicationFragment extends Fragment {

    private RecyclerView mRecycleView;
    private TextView freeROM;
    private TextView freeSd;
    private List<AppInfo> mList;
    private Context mContext;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1000:
                    ((ApplicationActivity)getActivity()).disMissProgress();
                    bindAdapter();
                case 1001:
                    removeItemFromAdapter();
                    break;
            }
        }
    };
    private RecycleViewWrapAdapter adapterWrap;
    private int SUCCESS_APPLICATIONS = 1000 ;
    private ApplicationRecycleAdapter adapter;
    private UnInstallAppReceiver uninstallReceiver;
    private IntentFilter filter;
    private int UNINSTALL_APP = 1001;
    private int unInstallAppPos = -1;
    private int headViewCount = 0;
    private int nextViewCount = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_applist,container,false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.fragment_applist_recycleview);
        freeROM = (TextView) view.findViewById(R.id.freeRom);
        freeSd = (TextView) view.findViewById(R.id.freeSd);
        setFreeSpace();
    }

    private void setFreeSpace(){
        freeROM.setText("手机内存剩余："+Formatter.formatFileSize(getActivity().getApplicationContext(), SystemUtils.getPhoneFreeSpace()));
        freeSd.setText("Sd卡剩余: "+Formatter.formatFileSize(getActivity().getApplicationContext(),SystemUtils.getSdFreeSize()));
    }

    private void bindAdapter() {
        if(mList != null){
            mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new ApplicationRecycleAdapter(getContext(),mList);

            View viewHead = LayoutInflater.from(mContext).inflate(R.layout.item_recycleview_header,mRecycleView,false);
            TextView head = (TextView) viewHead.findViewById(R.id.head_view_title);
            head.setText("手机应用: " + adapter.getIntervalCount());
            View viewNext = LayoutInflater.from(mContext).inflate(R.layout.item_recycleview_header,mRecycleView,false);
            TextView next = (TextView)viewNext.findViewById(R.id.head_view_title);
            next.setText("系统应用: " + (adapter.getItemCount() - adapter.getIntervalCount()));

            adapterWrap = new RecycleViewWrapAdapter(adapter);
            addHeadView(viewHead);
            addSecondView(viewNext);
            mRecycleView.setAdapter(adapterWrap);
            mRecycleView.setItemAnimator(new DefaultItemAnimator());
            setOnItemClickListener();
        }
    }

    private void addHeadView(View view){
        if(adapterWrap!=null){
            headViewCount++;
            adapterWrap.addHeadView(view);
        }
    }
    private void addSecondView(View view){
        if(adapterWrap!=null){
            nextViewCount++;
            adapterWrap.addNextView(view);
        }
    }

    private void changeHeadView(){
        View viewHead = LayoutInflater.from(mContext).inflate(R.layout.item_recycleview_header,null);
        TextView head = (TextView) viewHead.findViewById(R.id.head_view_title);
        head.setText("手机应用: " + adapter.getIntervalCount());
        //指定为0，因为只有一个HeadView被添加，下标为0
        adapterWrap.changeHeadView(0,viewHead);
    }

    private void changeSecondView(){
        View viewNext = LayoutInflater.from(mContext).inflate(R.layout.item_recycleview_header,null);
        TextView next = (TextView) viewNext.findViewById(R.id.head_view_title);
        next.setText("系统应用: " + (adapter.getItemCount() - adapter.getIntervalCount()) );
        adapterWrap.changeSecondView(0,viewNext);
    }

    private void removeItemFromAdapter(){
        if(unInstallAppPos != -1) {
            setFreeSpace();
            adapterWrap.removeItem(unInstallAppPos);
            if (unInstallAppPos < headViewCount + adapter.getIntervalCount()) {
                changeHeadView();
            }else{
                changeSecondView();
            }
            unInstallAppPos = -1;
        }
    }

    private void setOnItemClickListener(){
        if(adapterWrap !=null){
            adapter.setOnItemListener(new ApplicationRecycleAdapter.onItemClickListener() {
                @Override
                public void onItemClick(final AppInfo info,final int position) {
                    showDialog(info,position);
                }
            });
        }
    }

    private void showDialog(final AppInfo info,final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        List<String> mItems = new ArrayList<String>();
        mItems.add("启动应用");
        mItems.add("卸载应用");
        mItems.add("查看详情");
        ListAdapter mItemsAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mItems);
        builder.setIcon(info.icon).setTitle(info.appName).setAdapter(mItemsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getContext(), which + "is click", Toast.LENGTH_SHORT).show();
                switch (which){
                    case 0:
                        startApplication(info);
                        break;
                    case 1:
                        UninstallApp(info,position);
                        break;
                    case 2:
                        detailsForApplication(info);
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void UninstallApp(final AppInfo info,final int position) {
        if(!info.isSystemApp) {
            unInstallAppPos = position + headViewCount;
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + info.packageName));
            startActivity(intent);
        }else{
            if(!RootTools.isRootAvailable()){
                Toast.makeText(getContext(),"请Root后再卸载系统应用",Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!RootTools.isAccessGiven()){
                            Toast.makeText(getContext(),"请给本应用Root权限",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        unInstallAppPos = position + (headViewCount + nextViewCount);
                        RootTools.sendShell("mount -o remount ,rw /system", 3000);
                        RootTools.sendShell("rm -r "+ info.apkPath, 30000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void detailsForApplication(AppInfo info) {
        Intent intent = new Intent();
        //调用setting应用中的activity，来展示自己的应用
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        //启动界面需要 CATEGROY_DEFAULT
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置传递给activity的应用包名；
        intent.setData(Uri.parse("package:" + info.packageName));
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void startApplication(AppInfo info) {
        PackageManager pm = getContext().getPackageManager();
        //通过包名获取到应用的启动Intent
        Intent launchIntent = pm.getLaunchIntentForPackage(info.packageName);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launchIntent);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerUninstallReceiver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isCapture = loadData();
                if(isCapture){
                    handler.sendEmptyMessage(SUCCESS_APPLICATIONS);
                }
            }
        }).start();
    }

    private void registerUninstallReceiver() {
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        uninstallReceiver = new UnInstallAppReceiver();
        getContext().registerReceiver(uninstallReceiver, filter);
    }

    private void unregisterUninstallReceiver() {
        if(filter != null){
            getContext().unregisterReceiver(uninstallReceiver);
        }
    }


    //监听应用卸载的广播
    class UnInstallAppReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取卸载应用的包名
            String info = intent.getData().toString();
            System.out.println(info);
            handler.sendEmptyMessage(UNINSTALL_APP);
        }
    }

    //获取所有安装在手机中的应用
    private boolean loadData() {
        mList = SystemUtils.getAllInstalledApp(getActivity());
        if(mList!=null)
            return true;
        else
            return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterUninstallReceiver();
    }
}
