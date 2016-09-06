package com.mvp.myeffecttools.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.fragment.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getFragmentManager().beginTransaction().add(R.id.layout_setting,new SettingFragment()).commit();
    }
}
