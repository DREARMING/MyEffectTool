package com.mvp.myeffecttools.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mvp.myeffecttools.presenter.BasePresenter;

public abstract class BaseMvpActivity<V,T extends BasePresenter<V>> extends AppCompatActivity {

    protected T presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = initPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onAttach((V)this);
    }

    public abstract T initPresenter();
}
