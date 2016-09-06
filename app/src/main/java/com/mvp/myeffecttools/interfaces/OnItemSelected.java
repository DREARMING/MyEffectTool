package com.mvp.myeffecttools.interfaces;

import android.support.v7.widget.RecyclerView;

/**
 * Created by 爱的LUICKY on 2016/8/27.
 */
public interface OnItemSelected {
    public void onItemSelected();
    public void onItemSwiping(float moveX);
    public void clearViewChange();
}
