package com.mvp.myeffecttools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.bean.ItemBean;
import com.mvp.myeffecttools.bean.ProcessInfo;
import com.mvp.myeffecttools.interfaces.OnItemSelected;
import com.mvp.myeffecttools.interfaces.OnItemSwipe;
import com.mvp.myeffecttools.views.SlideRelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter implements OnItemSwipe{

    private static final String TAG = "MVP";
    public static final int NORMAL = 1000;
    public static final int SLIDE = 2000;
    public static final int ME = 100000;
    private int mState = NORMAL;
    private List<ItemBean> mItemBeans;
    private List<SlideViewHolder> mSlideViewHolders = new ArrayList<>();
    private List<ProcessInfo> mList;
    private BottomLayoutAnimationListener mLayoutLister;
    private Context mContext;

    public SlideAdapter(Context context,List<ProcessInfo> mList) {
        mContext = context;
        this.mList = mList;
    }

    public void openItemsAnimation() {
        mState = SLIDE;
        for (SlideViewHolder holder : mSlideViewHolders) {
            holder.openItemAnimation();
        }
    }

    @Override
    public void onItemSwiped(int position) {
        removeItem(position);
    }

    public interface BottomLayoutAnimationListener{
        void startAnimation();
        void closeAnimation();
        void removeItem(ProcessInfo killItem);
        void removeItems(List<ProcessInfo> killList);
    }

    public void removeItem(int position){
        ProcessInfo inf = mList.get(position);
        if(inf.getPackageName().equals(mContext.getPackageName())) {
            Toast.makeText(mContext,"can't kill MyEffectTool process",Toast.LENGTH_SHORT).show();
            return;
        }
        mItemBeans.remove(position);
        mSlideViewHolders.remove(position);
        notifyItemRemoved(position);
        mLayoutLister.removeItem(mList.remove(position));
    }

    public void selectAll(){
        for (ItemBean item:mItemBeans){
            item.setChecked(true);
        }
        for (SlideViewHolder holder:mSlideViewHolders){
            holder.setCheck(true);
            holder.onItemSelected();
        }
    }

    public void removeSelectedItems(){
        List<ProcessInfo> killList = new ArrayList<>();
        for (int i = 0 ; i < mItemBeans.size(); i++) {
            if(mItemBeans.get(i).isChecked()){
                ProcessInfo inf = mList.get(i);
                if(inf.getPackageName().equals(mContext.getPackageName())) {
                    Toast.makeText(mContext,"can't kill MyEffectTool process",Toast.LENGTH_SHORT).show();
                    continue;
                }
                killList.add(inf);
                mItemBeans.remove(i);
                mSlideViewHolders.remove(i);
                mList.remove(i);
                notifyItemRemoved(i);
                //删除的item会被下一个替换，所以i-1
                i--;
            }
        }
        closeItemAnimation();
        mLayoutLister.removeItems(killList);
    }

    public void setAnimationListener(BottomLayoutAnimationListener listener){
        mLayoutLister = listener;
    }

    public void closeItemAnimation() {
        resetItemBeans();
        for (SlideViewHolder holder : mSlideViewHolders) {
            holder.closeItemAnimation();
        }
        mLayoutLister.closeAnimation();
        mState = NORMAL;
    }

    private void resetItemBeans(){
        for (ItemBean item:mItemBeans) {
            item.setChecked(false);
        }
    }

    public void setItemBeans(List<ItemBean> beans) {
        mItemBeans = beans;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SlideViewHolder slideViewHolder = new SlideViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_process, parent, false));
        mSlideViewHolders.add(slideViewHolder);
        return slideViewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if(mList.get(position).getPackageName().equals(mContext.getPackageName())){
            //设置当前应用的viewtype
            return ME;
        }
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SlideViewHolder mHolder = (SlideViewHolder) holder;
        mHolder.bind(mItemBeans.get(position));
        ProcessInfo info = mList.get(position);
        mHolder.pSize.setText(Formatter.formatFileSize(mContext,info.getRamDirty()));
        mHolder.pName.setText(info.getProcessName());
        mHolder.icon.setImageDrawable(info.getIcon());
    }

    @Override
    public int getItemCount() {
        return mItemBeans == null ? 0 : mItemBeans.size();
    }

    private class SlideViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener
        ,OnItemSelected{

        private SlideRelativeLayout mSlideRelativeLayout;
        private CheckBox mCheckBox;
        private ItemBean mItemBean;
        private TextView pName;
        private TextView pSize;
        private ImageView icon;
        private View itemView;
        private int selectedColor;
        private int unSelectedColor;

        public SlideViewHolder(View itemView) {
            super(itemView);
            selectedColor = mContext.getResources().getColor(R.color.colorAlphaGray);
            unSelectedColor = itemView.getDrawingCacheBackgroundColor();
            mSlideRelativeLayout = (SlideRelativeLayout) itemView.findViewById(R.id.item_root);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
            icon = (ImageView) itemView.findViewById(R.id.item_icon_iv);
            pName = (TextView) itemView.findViewById(R.id.item_title_tv);
            pSize = (TextView) itemView.findViewById(R.id.item_subtitle_tv);
            this.itemView = itemView;
            clearViewChange();
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        public void setCheck(boolean isCheck){
            mCheckBox.setChecked(isCheck);
        }

        public void bind(ItemBean itemBean) {
            mItemBean = itemBean;
            mCheckBox.setChecked(itemBean.isChecked());
            switch (mState) {
                case NORMAL:
                    mSlideRelativeLayout.close();
                    break;

                case SLIDE:
                    mSlideRelativeLayout.open();
                    break;
            }
        }

        public void openItemAnimation() {
            mSlideRelativeLayout.openAnimation();
        }

        public void closeItemAnimation() {
            mCheckBox.setChecked(false);
            clearViewChange();
            mSlideRelativeLayout.closeAnimation();
        }

        public void setCheckBox() {
            mCheckBox.setChecked(!mCheckBox.isChecked());
            mItemBean.setChecked(mCheckBox.isChecked());
        }

        @Override
        public void onClick(View v) {
            if(mState == SLIDE) {
                setCheckBox();
                if(mCheckBox.isChecked())
                     onItemSelected();
                else
                    clearViewChange();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            switch (mState){
                case NORMAL:
                    mState = SLIDE;
                    mLayoutLister.startAnimation();
                    openItemsAnimation();
                    break;
            }
            return false;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(selectedColor);
        }


        @Override
        public void onItemSwiping(float moveX) {
            itemView.setAlpha(1 - moveX/itemView.getWidth());
        }


        @Override
        public void clearViewChange() {
            itemView.setBackgroundColor(unSelectedColor);
        }
    }
}
