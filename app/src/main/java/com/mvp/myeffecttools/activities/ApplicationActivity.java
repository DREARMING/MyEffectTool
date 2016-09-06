package com.mvp.myeffecttools.activities;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.fragment.ApplicationFragment;


public class ApplicationActivity extends AppCompatActivity {

    private FragmentManager fm;
    private ProgressBar mProgressBar;
    private ApplicationFragment shf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        setTitle("应用管理");
        initUI();
    }

    private void initUI() {
        mProgressBar = (ProgressBar)findViewById(R.id.fragment_applist_progressbar);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
         fm = getSupportFragmentManager();
        shf = new ApplicationFragment();
        fm.beginTransaction().replace(R.id.container_apps_fragment, shf).commit();
        /*List<AppInfo> list = SystemUtils.getAllInstalledApp(this);
        if(list == null){
            Log.i(TAG,"can't get all application list which is null");
        }
        MyRecycleAdapter adapter = new MyRecycleAdapter(this, list);
        decor = new StickyHeaderDecoration(adapter);

        mLRecyclerViewAdapter = new LRecyclerViewAdapter(getActivity(), adapter);
        list.setAdapter(mLRecyclerViewAdapter);

        list.addItemDecoration(decor, 1);
        list.addOnItemTouchListener(this);

        mLRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), "Item " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });*/
    }

    public void disMissProgress(){
        mProgressBar.setVisibility(ProgressBar.GONE);
    }

}
