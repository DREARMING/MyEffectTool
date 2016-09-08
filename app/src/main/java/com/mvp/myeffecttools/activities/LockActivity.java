package com.mvp.myeffecttools.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.interfaces.OnLockViewFinish;
import com.mvp.myeffecttools.views.LockView;

import java.util.List;

public class LockActivity extends AppCompatActivity {

    private LockView lockView;
    private SharedPreferences mPref;
    private RelativeLayout picture_layout;
    private TextView tv;
    private boolean isChangePassword = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        setTitle("应用锁");
        lockView = (LockView) findViewById(R.id.lockview);
        tv = (TextView) findViewById(R.id.set_lock_text);
        picture_layout = (RelativeLayout) findViewById(R.id.lock_picture);
        mPref = getSharedPreferences("configed",MODE_PRIVATE);
        initView();

        lockView.setOnFinishListenr(new OnLockViewFinish() {
            @Override
            public boolean onFinish(List<Integer> password) {
                String savePassword = mPref.getString("password",null);
                StringBuffer sb = new StringBuffer();
                for(Integer item:password){
                    sb.append(item);
                }
                String temp = sb.toString();
                if(savePassword == null){
                    mPref.edit().putString("password",temp).commit();
                    clearView(true);
                    Toast.makeText(LockActivity.this,"密码创建成功",Toast.LENGTH_SHORT).show();
                }else{
                    if(savePassword.equals(temp)){
                        Toast.makeText(LockActivity.this,"密码正确",Toast.LENGTH_SHORT).show();
                        if(!isChangePassword && !isChangePassword()){
                            String action = getIntent().getAction();
                            if(action!=null){
                                if("com.mvp.lockapps".equals(action)){
                                    joinToLockAppsActivity();
                                }else{
                                    finish();
                                }
                            }else{
                                finish();
                            }
                        }else{
                            if(!isChangePassword) {
                                clearView(true);
                                isChangePassword = true;
                            }else{
                                joinToLockAppsActivity();
                            }
                        }
                        return true;
                    }else{
                        Toast.makeText(LockActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                        clearView(false);
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private void joinToLockAppsActivity(){
        Intent intent = new Intent(this,LockAppsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initView(){
        if(!hasPassword()){
            tv.setVisibility(View.VISIBLE);
            picture_layout.setVisibility(View.GONE);
        }else{
            tv.setVisibility(View.GONE);
            picture_layout.setVisibility(View.VISIBLE);
        }
    }

    private void clearView(final boolean isInit){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lockView.clearView();
                if(isInit)
                initView();
            }
        },1000);
    }

    private boolean hasPassword(){
        String savePassword = mPref.getString("password",null);
        return savePassword != null;
    }

    private boolean isChangePassword(){
        Intent  intent = getIntent();
        String myAction = intent.getAction();
        if("com.mvp.changepassword".equals(myAction)){
            mPref.edit().putString("password",null).commit();
            return true;
        }
        return  false;

    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent();
        home.setAction(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
        int index = getIntent().getIntExtra("LockAppNum",-1);
        Intent lockIntent = new Intent("com.mvp.monitor.lock");
        lockIntent.putExtra("index",index);
        sendBroadcast(lockIntent);
        super.onBackPressed();
    }
}
