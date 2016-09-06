package com.mvp.myeffecttools.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.adapter.SlideAdapter;
import com.mvp.myeffecttools.bean.ItemBean;
import com.mvp.myeffecttools.bean.ProcessInfo;
import com.mvp.myeffecttools.interfaces.OnItemSelected;
import com.mvp.myeffecttools.interfaces.OnItemSwipe;
import com.mvp.myeffecttools.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mvp.myeffecttools.R.id.process_progress_bar;


public class ProcessesActivity extends AppCompatActivity{

    private RecyclerView mRecyclerView;
    private SlideAdapter mSlideAdapter;
    private List<ItemBean> mItemBeans = new ArrayList<>();
    private List<ProcessInfo> mList;
    private Context mContext;
   // private TextView mRightTV;
    private View operationLayout;
    private View cancelLayout;
    private View cleanBtn;
    private Button cancelBtn;
    private Button allSelectBtn;
    private ProgressBar progressBar;
    private TextView tv_processNum;
    private TextView tv_freeRam;
    private TextView tv_toalRam;
    private long totalRam;
    private int processNum;
    private long freeRam;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what){
                case 100:

                    break;
                case 101:
                    progressBar.setVisibility(View.GONE);
                    bindAdapter();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        getSupportActionBar().hide();

        initView();
        initListener();
        loadData();
    }

    private void initBean() {
        if(mList == null) {
            throw new NullPointerException("mList is null");
        }
        for (int x = 0; x < mList.size(); x++) {
            mItemBeans.add(new ItemBean());
        }
    }

    private void loadData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mList = SystemUtils.getAllRunningProcess(mContext);
                handler.sendEmptyMessage(101);
            }
        }).start();
    }

/*    private void initData() {

        mSlideAdapter = new SlideAdapter(mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSlideAdapter);
        mSlideAdapter.setItemBeans(mItemBeans);
    }*/

    private void setNumText(){
        tv_processNum.setText("进程: " + processNum );
        tv_freeRam.setText(""+Formatter.formatFileSize(mContext,freeRam));
        tv_toalRam.setText("/"+Formatter.formatFileSize(mContext,totalRam));
    }

    private void initNumText(){
        processNum = mList.size();
        freeRam = SystemUtils.getFreeMem(mContext);
        totalRam = SystemUtils.getTotalMemory(mContext);
        setNumText();
    }

    class MyTouchHelper extends ItemTouchHelper.Callback{
        //ItemTouchHelper支持三种状态：空闲 Idle，滑动swipe，拖拽Drag；

        //判断支持哪些移动事件
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            //GridLayoutManager 继承于 LinearLayoutManager ,所以不能用
            /*
            * if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){}作为只支持LinearLayoutManager的滑动事件
            *
            * */

            //表示当前item是本应用进程，不能移动删除
            if(viewHolder.getItemViewType() == 100000){
                return 0;
            }
            if(!(recyclerView.getLayoutManager() instanceof GridLayoutManager)){
                //设置空闲状态下只能左移，并且在滑动状态下能够左右移动；
               final int swipeFlags = makeFlag(ItemTouchHelper.ACTION_STATE_IDLE,ItemTouchHelper.LEFT) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT);
                return makeMovementFlags(0,swipeFlags);
            }

           return 0;
        }

        //拖拽的时候调用此事件
        //第一个参数是绑定的RecyclerView
        //第二个参数值要拖拽的item
        //第三个是拖拽到哪个item上面，假如说第一个item被拖动，
        //要移动到第三个item，途中经过第二个item，那么当移动的item悬浮在第二个item时，也会调用一次此方法
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        //当控件滑动的时候调用此方法；
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mSlideAdapter.onItemSwiped(viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if(!(actionState == ItemTouchHelper.ACTION_STATE_IDLE)){
                if(viewHolder instanceof OnItemSelected){
                    ((OnItemSelected)viewHolder).onItemSelected();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }


        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                if(viewHolder instanceof OnItemSelected){
                    ((OnItemSelected)viewHolder).onItemSwiping(dX);
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

            super.clearView(recyclerView, viewHolder);
        }
    }

    private void bindAdapter(){
        initBean();
        initNumText();
        mSlideAdapter = new SlideAdapter(mContext,mList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mSlideAdapter);
        mSlideAdapter.setItemBeans(mItemBeans);
        mRecyclerView.setHasFixedSize(true);

        ItemTouchHelper mTouchHelper = new ItemTouchHelper(new MyTouchHelper());
        mTouchHelper.attachToRecyclerView(mRecyclerView);

        mSlideAdapter.setAnimationListener(new SlideAdapter.BottomLayoutAnimationListener() {
            @Override
            public void startAnimation() {
                startLayoutAnimation();
                startCancelLayoutAnimation();
            }

            @Override
            public void closeAnimation() {
                closeCancelLayoutAnimation();
                closeLayoutAnimation();
            }

            @Override
            public void removeItem(ProcessInfo killItem) {
                SystemUtils.killProcess(mContext,killItem.getPackageName());
                long dirty = killItem.getRamDirty();
                processNum--;
                freeRam -= dirty;
                setNumText();
                Toast.makeText(mContext, "清理了1个进程,回收了"+Formatter.formatFileSize(mContext,dirty), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void removeItems(List<ProcessInfo> killList) {
                long size = 0;
                int num = 0;
                for (int i = 0; i < killList.size(); i++){
                    num++;
                    size += killList.get(i).getRamDirty();
                    SystemUtils.killProcesses(mContext,killList);
                }
                processNum -= num;
                freeRam -= size;
                setNumText();
                Toast.makeText(mContext, "清理了"+num+"个进程,回收了"+ Formatter.formatFileSize(mContext,size), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initListener() {

       // mRightTV.setOnClickListener(this);
        MyClickListener listener = new MyClickListener();
        cleanBtn.setOnClickListener(listener);
        cancelBtn.setOnClickListener(listener);
        allSelectBtn.setOnClickListener(listener);
    }


    private void initView() {
        mContext = ProcessesActivity.this;
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_root);
        //mRightTV = (TextView) findViewById(R.id.right_tv);
        operationLayout = findViewById(R.id.process_clear_layout);
        operationLayout.setVisibility(View.GONE);

        tv_freeRam = (TextView)findViewById(R.id.process_free_ram);
        tv_toalRam = (TextView) findViewById(R.id.process_total_ram);
        tv_processNum = (TextView) findViewById(R.id.process_num);

        progressBar = (ProgressBar)findViewById(process_progress_bar);

        cleanBtn = findViewById(R.id.process_clear);
        cancelBtn = (Button) findViewById(R.id.process_cancel);
        allSelectBtn = (Button) findViewById(R.id.process_all_select);

        cancelLayout = findViewById(R.id.process_cancel_layout);
        operationLayout.setVisibility(View.GONE);
        cancelLayout.setVisibility(View.GONE);

    }

    class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.process_cancel:
                    mSlideAdapter.closeItemAnimation();
                    closeCancelLayoutAnimation();
                    closeLayoutAnimation();
                break;
                case R.id.process_clear:
                    mSlideAdapter.removeSelectedItems();
                    //Toast.makeText(mContext,"clear",Toast.LENGTH_SHORT).show();;
                case R.id.process_all_select:
                    mSlideAdapter.selectAll();
                break;
            }
        }
    }


    public void startCancelLayoutAnimation(){
        cancelLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(1,0,1,0,1,-2,1,0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1.0f);
        AnimationSet sets = new AnimationSet(true);
        sets.addAnimation(translateAnimation);
        sets.addAnimation(alphaAnimation);
        sets.setDuration(250);
        sets.setFillAfter(true);
        cancelLayout.startAnimation(sets);
    }

    public void closeCancelLayoutAnimation(){
        TranslateAnimation translateAnimation = new TranslateAnimation(1,0,1,0,1,0,1,-2);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0.0f);
        AnimationSet sets = new AnimationSet(true);
        sets.addAnimation(translateAnimation);
        sets.addAnimation(alphaAnimation);
        sets.setDuration(250);
        sets.setFillAfter(true);
        cancelLayout.startAnimation(sets);
    }


    public void startLayoutAnimation(){
        operationLayout.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(1,0,1,0,1,1,1,0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1.0f);
        AnimationSet sets = new AnimationSet(true);
        sets.addAnimation(translateAnimation);
        sets.addAnimation(alphaAnimation);
        sets.setDuration(250);
        sets.setFillAfter(true);
        operationLayout.startAnimation(sets);
    }

    public void closeLayoutAnimation(){
        TranslateAnimation translateAnimation = new TranslateAnimation(1,0,1,0,1,0,1,1);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        AnimationSet sets = new AnimationSet(true);
        sets.addAnimation(translateAnimation);
        sets.addAnimation(alphaAnimation);
        sets.setDuration(250);
        sets.setFillAfter(true);
        operationLayout.startAnimation(sets);
    }
}
