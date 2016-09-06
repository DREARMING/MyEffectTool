package com.mvp.myeffecttools.views;


/**
 * Created by 爱的LUICKY on 2016/8/28.
 */
public class LockViewPoint {
    private final float X;
    private final float Y;
    private int mState;

    public LockViewPoint(float x, float y,int state) {
        X = x;
        Y = y;
        mState = state;
    }

    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }

    public int getmState() {
        return mState;
    }

    public void setmState(int mState) {
        this.mState = mState;
    }

    public float getDistance(float x, float y){
        return (float) Math.sqrt((x - X)*(x - X) + (y-Y)*(y-Y));
    }

}
