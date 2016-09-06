package com.mvp.myeffecttools.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.bean.LockApp;
import com.mvp.myeffecttools.dao.DaoMaster;
import com.mvp.myeffecttools.interfaces.LockAppListener;
import com.mvp.myeffecttools.utils.DaoUtils;
import com.mvp.myeffecttools.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/30.
 */
public class LockAppAdapter extends RecyclerView.Adapter {

    private static final int LOCK_NUM = 100000;
    private static final int UNLOCK_NUM = 100001;
    private LayoutInflater mInflater;
    private LockAppListener mListener;
    private List<LockApp> lockList;
    private List<LockApp> unLockList;
    private Context mContext;
    private List<NumberHolder> numberHolders;

    public LockAppAdapter(Context context,List<LockApp> lockList,List<LockApp>unLockList) {
        this.lockList = lockList;
        this.unLockList = unLockList;
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
        numberHolders = new ArrayList<>();
        SQLiteOpenHelper hp = new DaoMaster.DevOpenHelper(mContext,"MyDb.db",null);
    }

    public void setOnLockAppListener(LockAppListener listener){
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == LOCK_NUM ){
            NumberHolder holder = new NumberHolder(mInflater.inflate(R.layout.item_lock_head,parent,false));
            numberHolders.add(holder);
            return holder;
        }else if(viewType == UNLOCK_NUM){
            NumberHolder holder = new NumberHolder(mInflater.inflate(R.layout.item_lock_head,parent,false));
            numberHolders.add(holder);
            return holder;
        }else{
            LockHolder holder = new LockHolder(mInflater.inflate(R.layout.item_lockapps,parent,false));
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if(viewType == 0){
            if(lockList.size() > 0 && position <= lockList.size()){
                LockHolder lockHolder = (LockHolder) holder;
                LockApp item = lockList.get(position - 1);
                lockHolder.iv.setImageDrawable(item.icon);
                lockHolder.appName.setText(item.appName);
                if(item.isSystemApp)
                     lockHolder.type.setText("系统应用");
                else
                    lockHolder.type.setText("第三方应用");
                lockHolder.mSwitch.setChecked(true);
            }else{
                LockHolder lockHolder = (LockHolder) holder;
                LockApp item;
                if(lockList.size() > 0){
                    item= unLockList.get(position - 2 -lockList.size());
                }else{
                    item = unLockList.get(position - 1);
                }
                lockHolder.iv.setImageDrawable(item.icon);
                lockHolder.appName.setText(item.appName);
                if(item.isSystemApp)
                    lockHolder.type.setText("系统应用");
                else
                    lockHolder.type.setText("第三方应用");
                lockHolder.mSwitch.setChecked(false);
            }
        }else if(viewType == LOCK_NUM){
            ((NumberHolder) holder).tv.setText(lockList.size()+"个应用已加锁");
        }else{
            ((NumberHolder) holder).tv.setText(unLockList.size()+"个应用未加锁");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(lockList.size() > 0 && position <= lockList.size()){
            if(position == 0){
                return LOCK_NUM;
            }else{
                return 0;
            }
        }else{
            if(lockList.size()== 0){
                if(position == 0){
                    return UNLOCK_NUM;
                }else{
                    return 0;
                }
            }else{
                if(position >= lockList.size()+2){
                    return 0;
                }else{
                    return UNLOCK_NUM;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(lockList.size() > 0){
            return lockList.size()+unLockList.size()+2;
        }else{
           return unLockList.size()+1;
        }
    }

    class NumberHolder extends RecyclerView.ViewHolder{
        private TextView tv;
        public NumberHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.lock_num_tv);
        }
    }

    class LockHolder extends RecyclerView.ViewHolder{

        private ImageView iv;
        private TextView appName;
        private TextView type;
        private Switch mSwitch;
        private View itemView;

        public LockHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.item_app_drawable);
            appName = (TextView) itemView.findViewById(R.id.item_app_name);
            type = (TextView) itemView.findViewById(R.id.item_app_type);
            mSwitch = (Switch) itemView.findViewById(R.id.item_lockapp);
            this.itemView = itemView;
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (LockHolder.this).getAdapterPosition();
                    Log.e("MVP","position is : " +position);
                    if(lockList.size()>0 && position <= lockList.size()){
                        mSwitch.setChecked(false);
                        LockApp item = lockList.get(position-1);
                        DaoUtils.deleteLockApp(mContext,item.packageName);
                        lockList.remove(position - 1);
                        unLockList.add(item);
                        notifyDataSetChanged();
                        if(mListener != null)
                        mListener.onDeleteListener();
                    }else if(lockList.size() == 0){
                        mSwitch.setChecked(true);
                        LockApp item = unLockList.get(position-1);
                        lockList.add(item);
                        unLockList.remove(position -1 );
                        notifyDataSetChanged();
                        DaoUtils.insertLockApp(mContext,item.packageName);
                        if(mListener!=null){
                            mListener.onCatchListener();
                        }
                    }else{
                        mSwitch.setChecked(true);
                        LockApp item = unLockList.get(position - lockList.size() - 2);
                        unLockList.remove(position - lockList.size() - 2 );
                        lockList.add(item);
                        notifyDataSetChanged();
                        DaoUtils.insertLockApp(mContext,item.packageName);
                        if(mListener != null)
                        mListener.onInsertListener();
                    }
                }
            });
        }
    }
}
