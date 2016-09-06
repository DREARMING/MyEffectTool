package com.mvp.myeffecttools.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.services.MonitorService;

/**
 * Created by 爱的LUICKY on 2016/8/18.
 */
public class SettingFragment extends PreferenceFragment {

    private SharePrefsListener listener;
    private SharedPreferences mPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        listener = new SharePrefsListener();
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPref.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    class SharePrefsListener implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if(getResources().getString(R.string.lock).equals(s)){
                if(sharedPreferences.getBoolean(s,false)) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MonitorService.class);
                    getActivity().startService(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MonitorService.class);
                    getActivity().stopService(intent);
                }
            }else if("keys".equals(s)){
                if(sharedPreferences.getBoolean(s,false)){
                    Intent intent = new Intent();
                    intent.setAction("com.mvp.monitor.keys.open");
                    getActivity().sendBroadcast(intent);
                }else{
                    Intent intent = new Intent();
                    intent.setAction("com.mvp.monitor.keys.close");
                    getActivity().sendBroadcast(intent);
                }
            }
        }
    }
}
