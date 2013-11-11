package com.digdug.colorpicker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.SweepGradient;
import android.view.View;

public class ColorRing extends Widget {
    Paint mPaint = null;
    private float mRadius;
    private PointF mCenter;
    private int[] mHueList = new int[] {
        Color.WHITE,
        Color.BLACK
    };
    private float mValue = 0;
    private float mWidth = 40;

    public ColorRing(View v, int[] hues) {
        super(v);
        mHueList = hues;
    }

    public void draw(Canvas canvas) {
        int state = canvas.save();
        canvas.rotate(mValue * 360f, mCenter.x, mCenter.y);
        canvas.drawCircle(mCenter.x, mCenter.y, mRadius, getPaint());
        canvas.restoreToCount(state);
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float val) {
        mValue = val;
    }

    private Paint getPaint() {
        if (mPaint != null)
            return mPaint;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mWidth);
        mPaint.setShader(new SweepGradient(mCenter.x, mCenter.y,
                mHueList,
                null));
        return mPaint;
    }

    public void setRadius(float r) {
        mRadius = r;
    }

    public void setCenter(PointF c) {
        mCenter = c;
    }

    public void setWidth(float width) {
        mWidth = width;
    }

    public void setColors(int[] colors) {
        mHueList = colors;
        if (mPaint != null) {
            mPaint.setShader(new SweepGradient(mCenter.x, mCenter.y,
                    mHueList,
                    null));
        }
    }
}
