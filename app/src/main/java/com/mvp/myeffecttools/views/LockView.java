package com.mvp.myeffecttools.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mvp.myeffecttools.R;
import com.mvp.myeffecttools.interfaces.OnLockViewFinish;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 爱的LUICKY on 2016/8/28.
 */
public class LockView extends View{
    private OnLockViewFinish onFinishListenr;
    private final static int NORMAL = 1;
    private final static int PRESS  = 2;
    private final static int ERROR  = 3;
    private List<LockViewPoint> mSelectedPoints = new ArrayList<LockViewPoint>();
    private LockViewPoint[][] mPoints = new LockViewPoint[3][3];
    private int currentState = NORMAL;
    private boolean onMoving = false;

    private Paint mNormalPaint;
    private Paint mPressPaint;
    private Paint mErrorPaint;

    private int PLineColor = 0;
    private int ELineColor = 0;

    private Bitmap normalBitMap;
    private Bitmap pressedBitmap;
    private Bitmap errorBitmap;
    private List<Integer> mPassword;

    private int radius;
    private float mX;
    private float mY;

    public LockView(Context context) {
        super(context);
    }

    public LockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LockView);
        int n = typedArray.getIndexCount();
        for (int i =0; i<n; i++){
            int index = typedArray.getIndex(i);
            switch (index){
                case R.styleable.LockView_pressedLine:
                    PLineColor = typedArray.getColor(index,getResources().getColor(R.color.line_press));
                    break;
                case R.styleable.LockView_errorLine:
                    PLineColor = typedArray.getColor(index,getResources().getColor(R.color.line_error));
                    break;
                case R.styleable.LockView_normalPoint:
                    normalBitMap = BitmapFactory.decodeResource(getResources(),typedArray.getResourceId(index,R.drawable.point_normal));
                    break;
                case R.styleable.LockView_pressedPoint:
                    pressedBitmap = BitmapFactory.decodeResource(getResources(),typedArray.getResourceId(index,R.drawable.point_press));
                    break;
                case R.styleable.LockView_errorPoint:
                    errorBitmap = BitmapFactory.decodeResource(getResources(),typedArray.getResourceId(index,R.drawable.point_error));
                    break;
            }
        }
        typedArray.recycle();
        initConfig();
    }

    public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnFinishListenr(OnLockViewFinish listenr){
        this.onFinishListenr = listenr;
    }

    private void initConfig(){
        mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if(PLineColor == 0){
            PLineColor = getResources().getColor(R.color.line_press);
        }
        if(ELineColor == 0){
            ELineColor = getResources().getColor(R.color.line_error);
        }
        if(normalBitMap == null){
            normalBitMap = BitmapFactory.decodeResource(getResources(),R.drawable.point_normal);
        }
        if(pressedBitmap == null){
            pressedBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.point_press);
        }
        if(errorBitmap == null){
            errorBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.point_error);
        }


        mPressPaint.setColor(PLineColor);float density =  getResources().getDisplayMetrics().density;
        mPressPaint.setStrokeWidth(7);
        // normalBitMap.setHeight((int)density * 64);
        //normalBitMap.setWidth((int)density * 64);
        mErrorPaint.setColor(ELineColor);
        mErrorPaint.setStrokeWidth(7);

        radius = normalBitMap.getWidth()/2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        initView();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void initView(){

        //offset是相对于控件来说，让九宫格始终处于控件的中间位置
        float offsetX,offsetY;
        int width = getWidth();
        int height = getHeight();
        float mPointWidth;

        //屏幕适配
        if(width <= height){
            offsetX = 0;
            //因为九宫格是正方形，宽等于高，如果要让九宫格处于正中间，那么就是如下计算；
            offsetY = (height - width) / 2;
            mPointWidth = width /4;
        }else{
            offsetX = (width - height)/2;
            //因为九宫格是正方形，宽等于高，如果要让九宫格处于正中间，那么就是如下计算；
            offsetY = 0;
            mPointWidth = height /4;
        }

        mPassword = new ArrayList<>();

        for(int i=0;i<mPoints.length;i++){
            for(int j = 0; j<mPoints[i].length;j++){
                mPoints[i][j] = new LockViewPoint(mPointWidth*(j+1)+offsetX, mPointWidth*(i+1)+offsetY, NORMAL);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        DrawPoints(canvas);
        DrawLines(canvas);
        super.onDraw(canvas);
    }

    private void DrawLine(Canvas canvas,LockViewPoint point1,float X,float Y){
        if(point1.getmState() == PRESS) {
            canvas.drawLine(point1.getX(), point1.getY(), X, Y,mPressPaint);
        }
        if(point1.getmState() == ERROR){
            canvas.drawLine(point1.getX(), point1.getY(), X, Y,mErrorPaint);
        }
    }

    private void DrawLines(Canvas canvas){
        if(mSelectedPoints.size()>0){
            LockViewPoint point1 = mSelectedPoints.get(0);
            for(int j = 1; j<mSelectedPoints.size();j++){
                LockViewPoint point2 = mSelectedPoints.get(j);
                DrawLine(canvas,point1,point2.getX(),point2.getY());
                point1 = point2;
            }
            if(onMoving){
                DrawLine(canvas,point1,mX,mY);
            }
        }
    }

    private void resetPoints(){
        for (int i=0;i<mPoints.length;i++){
            for(int j=0;j<mPoints[i].length;j++) {
                mPoints[i][j].setmState(NORMAL);
            }
        }
        mSelectedPoints.clear();
        mPassword.clear();
    }


    private void DrawPoints(Canvas canvas){
        for (int i=0;i<mPoints.length;i++){
            for(int j=0;j<mPoints[i].length;j++){
                LockViewPoint point = mPoints[i][j];
                switch (point.getmState()){
                    case NORMAL:
                        int normal_radius =  normalBitMap.getWidth()/2;
                        canvas.drawBitmap(normalBitMap,(point.getX() - normal_radius), (point.getY()-normal_radius),mNormalPaint);
                        break;
                    case PRESS:
                        int press_radius = pressedBitmap.getWidth()/2;
                        canvas.drawBitmap(pressedBitmap,(point.getX() - press_radius), (point.getY()-press_radius),mPressPaint);
                        break;
                    case ERROR:
                        int error_radius = errorBitmap.getWidth()/2;
                        canvas.drawBitmap(errorBitmap,(point.getX() - error_radius), (point.getY()-error_radius),mErrorPaint);
                        break;
                }
            }
        }
    }

    private int[] getSelectedPointPos(){
        int[] position;
        for (int i=0;i<mPoints.length;i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                LockViewPoint point = mPoints[i][j];
                if (radius >= point.getDistance(mX, mY)) {
                    position = new int[2];
                    position[0] = i;
                    position[1] = j;
                    return  position;
                }
            }
        }
        return null;
    }

    public void clearView(){
        resetPoints();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        mX = event.getX();
        mY = event.getY();
        int[] pos;
        switch (action){
            case MotionEvent.ACTION_DOWN:
                resetPoints();
                pos = getSelectedPointPos();
                if(pos != null){
                    onMoving = true;
                    int i=pos[0];
                    int j = pos[1];
                    mPoints[i][j].setmState(PRESS);
                    //用来存放密码
                    mSelectedPoints.add(mPoints[i][j]);
                    mPassword.add(i*3+j);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(onMoving) {
                    pos = getSelectedPointPos();
                    if (pos != null) {
                        int i = pos[0];
                        int j = pos[1];
                        LockViewPoint point = mPoints[i][j];
                        if (!mSelectedPoints.contains(point)) {
                            point.setmState(PRESS);
                            mSelectedPoints.add(mPoints[i][j]);
                            mPassword.add(i * 3 + j);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                onMoving = false;
                if(mSelectedPoints.size() > 0){
                    if(onFinishListenr != null ) {
                        boolean flag = onFinishListenr.onFinish(mPassword);
                        if(!flag){
                            for(LockViewPoint point:mSelectedPoints){
                                point.setmState(ERROR);
                            }
                        }
                    }
                }
                break;
        }
        //重新绘制；
        invalidate();
        return true;
    }
}
