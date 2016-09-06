package com.mvp.myeffecttools.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by 爱的LUICKY on 2016/8/26.
 */
public class ProcessInfo {

    private  Drawable icon;
    private String processName;
    private long ramDirty;
    private String packageName;
    private boolean isUserProcess;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getRamDirty() {
        return ramDirty;
    }

    public void setRamDirty(long ramDirty) {
        this.ramDirty = ramDirty;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isUserProcess() {
        return isUserProcess;
    }

    public void setUserProcess(boolean userProcess) {
        isUserProcess = userProcess;
    }
}
