package com.mvp.myeffecttools.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mvp.myeffecttools.R;
public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        setTitle("进程管理");
    }

    public void clickApplication(View view){
        Intent intent = new Intent();
        intent.setClass(this,ApplicationActivity.class);
        startActivity(intent);
    }

    public void clickProgress(View view){
        Intent intent = new Intent();
        intent.setClass(this,ProcessesActivity.class);
        startActivity(intent);
    }

    public void clickLockActivity(View view){
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!mPrefs.getBoolean("Lock",false)){
            showOpenLockDialog();
        }else{
            Intent intent = new Intent(mContext,LockActivity.class);
            intent.setAction("com.mvp.lockapps");
            startActivity(intent);
        }
    }

    private void showOpenLockDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("是否要打开程序锁？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPrefs.edit().putBoolean("Lock",true).commit();
                Intent intent = new Intent(mContext,LockActivity.class);
                intent.setAction("com.mvp.lockapps");
                startActivity(intent);
            }
        }).setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext,"程序锁需要开启，才能使用此功能",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public void openSettingActivity(View view){
        Intent intent = new Intent();
        intent.setClass(this,SettingActivity.class);
        startActivity(intent);
    }
}
