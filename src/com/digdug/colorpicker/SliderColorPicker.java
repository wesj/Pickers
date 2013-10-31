package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wesj on 10/30/13.
 */
public class SliderColorPicker extends ViewBase implements ColorPicker {


    private ColorListener mListener;
    private Bar mBar1;
    private Bar mBar2;
    private Bar mBar3;
    private MODE mMode;
    private Drawable mIndicator;

    private float[] hsl = new float[3];
    private static int[] mHueList = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };
    private boolean mDragging;

    public float getHue() {
        return hsl[0];
    }

    public float getSat() {
        return hsl[1];
    }

    public float getVal() {
        return hsl[2];
    }

    public enum MODE {
        RGB,
        HSL
    }

    public SliderColorPicker(Context context) {
        super(context);
    }

    public SliderColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SliderColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int getColor() {
        return Color.HSVToColor(hsl);
    }

    @Override
    public void setColorChangeListener(ColorListener listener) {
        mListener = listener;
    }

    @Override
    protected void init(Context context) {
        hsl = new float[3];
        hsl[0] = 0f;
        hsl[1] = 1f;
        hsl[2] = 0f;
        mBar1 = new Bar(getHueList(getSat(), getVal()), getHue()/360);
        mBar2 = new Bar(new int[] { Color.HSVToColor(new float[] { getHue(), 0f, getVal() }),
                                    Color.HSVToColor(new float[] { getHue(), 1f, getVal() }) }, getSat());
        mBar3 = new Bar(new int[] { Color.HSVToColor(new float[] { getHue(), getSat(), 0f }),
                                    Color.HSVToColor(new float[] { getHue(), getSat(), 1f }) }, getVal());
    }

    public void setMode(MODE mode) {
        mMode = mode;
    }

    private float getBarHeight() {
        return getHeight() / 3;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int state = canvas.save();

        mBar1.draw(canvas);
        canvas.translate(0f, getBarHeight());
        mBar2.draw(canvas);
        canvas.translate(0f, getBarHeight());
        mBar3.draw(canvas);

        canvas.restoreToCount(state);
    }

    private class Bar {
        private final Paint mPaint;
        private int[] mColors;
        public float mPosition;
        private LinearGradient mGradient;

        // TODO: Read these in dpi units
        private static final float PADDING = 40;
        private static final float STROKE = 5;
        private RectF mRect;

        public Bar(int[] colors, float position) {
            mPaint = new Paint();
            mColors = colors;
            mPosition = position;
        }

        public void draw(Canvas canvas) {
            int state = canvas.save();

            float padding = 40f;
            mPaint.setStyle(Paint.Style.FILL);
            if (mGradient == null) {
                mGradient = new LinearGradient(PADDING, PADDING, getWidth() - PADDING, PADDING, mColors, null, Shader.TileMode.CLAMP);
            }
            if (mRect == null) {
                mRect = new RectF(PADDING, PADDING/2, getWidth()-PADDING, getBarHeight()-PADDING/2);
            }
            mPaint.setShader(mGradient);
            canvas.drawRect(mRect, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(STROKE);
            mPaint.setColor(Color.BLACK);
            canvas.drawRect(mRect, mPaint);

            drawIndicator(canvas);

            canvas.restoreToCount(state);
        }

        private void drawIndicator(Canvas canvas) {
            int state = canvas.save();
            canvas.translate(mPosition*(getWidth()-PADDING*2)+PADDING, getBarHeight()/2);

            if (mIndicator != null) {
                mIndicator.draw(canvas);
            } else {
                float r = (getBarHeight()/2 - PADDING - 10)/2;

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShader(null);
                mPaint.setColor(getColor());
                canvas.drawCircle(0, 0, r, mPaint);

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(STROKE);
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(0, 0, r, mPaint);

                mPaint.setColor(Color.BLACK);
                canvas.drawCircle(0, 0, r+STROKE, mPaint);
            }

            canvas.restoreToCount(state);
        }

        public float dragTo(float x, float y) {
            x = Math.max(x, PADDING);
            x = Math.min(x, getWidth() - PADDING);
            x -= PADDING;
            mPosition = x/(getWidth() - PADDING*2);
            return mPosition;
        }

        public void setColors(int[] colors) {
            mColors = colors;
            mGradient = null;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            mDragging = false;
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN && !mDragging) {
            // TODO: mDragging should maintain which bar is being drug
            mDragging = true;
        }

        if (mDragging) {
            int bar = getBarAt(event.getX(), event.getY());
            switch(bar) {
                case 0:
                    hsl[0] = mBar1.dragTo(event.getX(), event.getY()) * 360;
                    break;
                case 1:
                    hsl[1] = mBar2.dragTo(event.getX(), event.getY());
                    break;
                case 2:
                default:
                    hsl[2] = mBar3.dragTo(event.getX(), event.getY());
                    break;
            }

            mBar1.setColors(getHueList(getSat(), getVal()));
            mBar2.setColors(new int[] { Color.HSVToColor(new float[] { getHue(), 0f, getVal() }),
                                        Color.HSVToColor(new float[] { getHue(), 1f, getVal() }) });
            mBar3.setColors(new int[] { Color.HSVToColor(new float[] { getHue(), getSat(), 0f }),
                                        Color.HSVToColor(new float[] { getHue(), getSat(), 1f }) });

            if (mListener != null) {
                mListener.onChange(getColor());
            }
            invalidate();
        }
        return true;
    }

    private int[] getHueList(float sat, float val) {
        for (int i = 0; i < mHueList.length; i++) {
            float[] f = new float[3];
            f[0] = 360*i/(mHueList.length-1);
            f[1] = sat;
            f[2] = val;
            mHueList[i] = Color.HSVToColor(f);
        }
        return mHueList;
    }

    private int getBarAt(float x, float y) {
        return (int) Math.floor(y / getBarHeight());
    }
}
