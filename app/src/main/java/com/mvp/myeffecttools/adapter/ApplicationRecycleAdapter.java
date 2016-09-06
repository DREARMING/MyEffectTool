package com.mvp.myeffecttools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.bean.AppInfo;
import com.mvp.myeffecttools.interfaces.ExpanedRecycleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/25.
 */
public class ApplicationRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ExpanedRecycleView {

    private List<AppInfo> mList;
    private LayoutInflater mInflater;
    private Context mContext;
    private List<AppInfo> list_user;
    private List<AppInfo> list_system;
    private onItemClickListener mListener;

    public ApplicationRecycleAdapter(Context context,List<AppInfo> mList) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mList = mList;
        divided(this.mList);
    }

    public void divided(List<AppInfo> allAppList){
        list_user = new ArrayList<AppInfo>();
        list_system = new ArrayList<AppInfo>();

        for (AppInfo item:allAppList) {
            if(item.isSystemApp){
                list_system.add(item);
            }else {
                list_user.add(item);
            }
        }
    }

    public void removeItem(int position){
        AppInfo info = null;
        if(position < list_user.size()){
            info = list_user.remove(position);
        }else{
            info = list_system.remove(position);
        }
        if(info != null) {
            mList.remove(info);
        }
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mInflater.inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    public interface onItemClickListener{
        void onItemClick(AppInfo info, int position);
    }

    public void setOnItemListener(onItemClickListener listener){
        this.mListener = listener;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        AppInfo info = null;
        ViewHolder viewHolder = (ViewHolder) holder;
        if(position < list_user.size()){
            info = list_user.get(position);
            viewHolder.icon.setImageDrawable(info.icon);
            viewHolder.appName.setText(info.appName);
            viewHolder.appSize.setText(Formatter.formatFileSize(mContext,info.appSize));
            viewHolder.appType.setText("SD卡存储");
        }else{
            info = list_system.get(position - list_user.size());
            viewHolder.icon.setImageDrawable(info.icon);
            viewHolder.appName.setText(info.appName);
            viewHolder.appSize.setText(Formatter.formatFileSize(mContext,info.appSize));
            viewHolder.appType.setText("手机内存");
        }
        final AppInfo appInfo = info;
        ((ViewHolder) holder).itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(appInfo,position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getIntervalCount() {
        return list_user.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView appName;
        public TextView appSize;
        public TextView appType;
        public View itemview;

        public ViewHolder(View itemView) {

            super(itemView);
            this.itemview = itemView;
            icon = (ImageView) itemView.findViewById(R.id.item_app_drawable);
            appName = (TextView) itemView.findViewById(R.id.item_app_name);
            appSize = (TextView) itemView.findViewById(R.id.item_app_size);
            appType = (TextView) itemView.findViewById(R.id.item_app_type);
        }
    }
}
