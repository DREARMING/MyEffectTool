package com.mvp.myeffecttools.adapter;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by 爱的LUICKY on 2016/8/23.
 */
public class RecycleViewWrapAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int BASE_VIEW_TYPE_HEAD = 100000;
    private static final int BASE_VIEW_TYPE_NEXT = 200000;

    private SparseArrayCompat<View> mHeadViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mSecondViews = new SparseArrayCompat<>();

    private ApplicationRecycleAdapter mInnerAdapter;

    public RecycleViewWrapAdapter(final ApplicationRecycleAdapter adapter) {
        mInnerAdapter = adapter;
    }

    public void addHeadView(View view){
        mHeadViews.put(BASE_VIEW_TYPE_HEAD + getHeadViewCount(),view);
    }
    public void addNextView(View view){
        mSecondViews.put(BASE_VIEW_TYPE_NEXT + getHeadViewCount()+ getIntervalCount(),view);
    }

    public void changeHeadView(int pos,View view){
        mHeadViews.put(BASE_VIEW_TYPE_HEAD + pos,view);
        notifyItemChanged(pos);
    }


    public void changeSecondView(int pos, View view){
        mHeadViews.put(BASE_VIEW_TYPE_NEXT + pos,view);
        notifyItemChanged(getHeadViewCount() + getIntervalCount() + pos);
    }

    public void removeItem(int position){
        if(position >= getHeadViewCount() + getIntervalCount() + getNextViewCount()){
            mInnerAdapter.removeItem(position - getHeadViewCount() - getNextViewCount());
        }else{
            mInnerAdapter.removeItem(position - getHeadViewCount());
        }
        notifyItemRemoved(position);
    }

    private boolean isHeadView(int position){
        return position < getHeadViewCount();
    }

    private boolean isNextView(int position){
        return (position < getHeadViewCount() + getIntervalCount() + getNextViewCount()) && (position >= getHeadViewCount()+getIntervalCount());
    }

    public int getIntervalCount() {
        return mInnerAdapter.getIntervalCount();
    }

    public int getHeadViewCount(){
        return mHeadViews.size();
    }

    public int getNextViewCount(){
        return mSecondViews.size();
    }


    @Override
    public int getItemViewType(int position) {
        if(isHeadView(position)){
            return mHeadViews.keyAt(position);
        }else if(isNextView(position)){
            return mSecondViews.keyAt(position - getIntervalCount() - getHeadViewCount() );
        }/*else if(position >= getHeadViewCount() && position< getIntervalCount() + getHeadViewCount()){
            return mInnerAdapter.getItemViewType(position - getHeadViewCount());
        }
        return mInnerAdapter.getItemViewType(position - getHeadViewCount() - getNextViewCount());*/
        else{
            return 0;
        }
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeadViews.get(viewType) != null){
            View head = mHeadViews.get(viewType);
            MyViewHolder holder = new MyViewHolder(head);
            return holder;
        }else if(mSecondViews.get(viewType) != null){
            View nextView = mSecondViews.get(viewType);
            MyViewHolder holder = new MyViewHolder(nextView);
            return holder;
        }
        return mInnerAdapter.onCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(isHeadView(position)){
            //((MyViewHolder) holder).Title.setText("手机应用：" + getIntervalCount());
            return;
        }else if(isNextView(position)){
            //((MyViewHolder) holder).Title.setText("系统应用：" + (mInnerAdapter.getItemCount() - getIntervalCount()));
            Log.e("MVP"," -------------------- has the NextView??");
            return;
        }else if(position >= getHeadViewCount() && (position < getIntervalCount() + getHeadViewCount())){
            mInnerAdapter.onBindViewHolder(holder,position - getHeadViewCount());
            return;
        }
        mInnerAdapter.onBindViewHolder(holder,position - getHeadViewCount() - getNextViewCount());
    }



    @Override
    public int getItemCount() {
        return getHeadViewCount() + mInnerAdapter.getItemCount() + getNextViewCount();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
       // public TextView Title;
        public MyViewHolder(View itemView) {
            super(itemView);
            //Title = (TextView) itemView.findViewById(R.id.head_view_title);
        }
    }

}
