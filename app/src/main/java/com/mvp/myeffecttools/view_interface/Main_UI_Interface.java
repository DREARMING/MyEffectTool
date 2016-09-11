package com.mvp.myeffecttools.view_interface;

import com.mvp.myeffecttools.bean.VersionBean;

/**
 * Created by 爱的LUICKY on 2016/9/11.
 */
public interface Main_UI_Interface {
    void showMessage(String str);
    void showUpdateDialog(final VersionBean version);
}
