package com.mvp.myeffecttools.presenter;

/**
 * Created by 爱的LUICKY on 2016/9/11.
 */
public class BasePresenter<V> {
    public V views;

    public void onAttach(V view){
        views = view;
    }

    public void deTeach(){
        views = null;
    }

}
