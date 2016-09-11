package com.mvp.myeffecttools.model;


import com.mvp.myeffecttools.bean.VersionBean;

/**
 * Created by 爱的LUICKY on 2016/9/11.
 */
public interface VersionListener {
    void OnUpadte(VersionBean version);
    void OnNormal();
    void OnError();
}
