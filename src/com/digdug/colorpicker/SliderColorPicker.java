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
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by wesj on 10/30/13.
 */
public class SliderColorPicker extends ViewBase implements ColorPicker {


    private ColorListener mListener;
    private Bar mBar1;
    private Bar mBar2;
    private Bar mBar3;
    private MODE mMode = MODE.RGB;
    private Drawable mIndicator;

    private int mColor;
    private float[] hsl;
    private static int[] mHueList = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };
    private boolean mDragging;

    private float getHue() { return hsl[0]; }
    private float getSat() { return hsl[1]; }
    private float getVal() { return hsl[2]; }

    public int getRed() { return Color.red(mColor); }
    public int getGreen() { return Color.green(mColor); }
    public int getBlue() { return Color.blue(mColor); }

    public enum MODE {
        RGB,
        HSV
    }

    public MODE getMode() { return mMode; }
    public void setMode(MODE mode) {
        mMode = mode;
        setupBars();
        invalidate();
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

    public void setColor(int color) {
        mColor = color;
        Color.colorToHSV(color, hsl);

        if (mMode == MODE.HSV) {
            mBar1.setVal(hsl[0] / 360f);
            mBar2.setVal(hsl[1]);
            mBar3.setVal(hsl[2]);
        } else {
            mBar1.setVal(Color.red(color) / 255f);
            mBar2.setVal(Color.green(color) / 255f);
            mBar3.setVal(Color.blue(color) / 255f);
        }
        setupBars();

        invalidate();
    }

    private void setupBars() {
        if (mBar1 == null || mBar2 == null || mBar3 == null) {
            if (mMode == MODE.HSV) {
                mBar1 = new Bar("Hue", getHueList(getSat(), getVal()), getHue()/360);
                mBar2 = new Bar("Saturation", new int[] { Color.HSVToColor(new float[] { getHue(), 0f, getVal() }),
                        Color.HSVToColor(new float[] { getHue(), 1f, getVal() }) }, getSat());
                mBar3 = new Bar("Value", new int[] { Color.HSVToColor(new float[] { getHue(), getSat(), 0f }),
                        Color.HSVToColor(new float[] { getHue(), getSat(), 1f }) }, getVal());
            } else {
                mBar1 = new Bar("Red",   new int[] { Color.rgb(       0, getGreen(), getBlue()), Color.rgb(     255, getGreen(), getBlue())}, getRed()/255);
                mBar2 = new Bar("Green", new int[] { Color.rgb(getRed(),          0, getBlue()), Color.rgb(getRed(),        255, getBlue())}, getGreen()/255);
                mBar3 = new Bar("Blue",  new int[] { Color.rgb(getRed(), getGreen(),         0), Color.rgb(getRed(), getGreen(),       255)}, getBlue()/255);
            }
        } else {
            if (mMode == MODE.HSV) {
                mBar1.setColors(getHueList(getSat(), getVal()));
                mBar2.setColors(new int[] { Color.HSVToColor(new float[] { getHue(), 0f, getVal() }),
                                            Color.HSVToColor(new float[] { getHue(), 1f, getVal() }) });
                mBar3.setColors(new int[] { Color.HSVToColor(new float[] { getHue(), getSat(), 0f }),
                                            Color.HSVToColor(new float[] { getHue(), getSat(), 1f }) });
            } else {
                mBar1.setColors(new int[] { Color.rgb(       0, getGreen(), getBlue()), Color.rgb(     255, getGreen(), getBlue())});
                mBar2.setColors(new int[] { Color.rgb(getRed(),          0, getBlue()), Color.rgb(getRed(),        255, getBlue())});
                mBar3.setColors(new int[] { Color.rgb(getRed(), getGreen(),         0), Color.rgb(getRed(), getGreen(),       255)});
            }
        }
    }

    @Override
    public void setColorChangeListener(ColorListener listener) {
        mListener = listener;
    }

    @Override
    protected void init(Context context) {
        mColor = Color.RED;
        hsl = new float[3];
        hsl[0] = 0f;
        hsl[1] = 1f;
        hsl[2] = 1f;
        setupBars();
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
        private String mLabel = "Hue";

        public Bar(String label, int[] colors, float position) {
            mLabel = label;
            mPaint = new Paint();
            mPaint.setTextSize(mPaint.getTextSize() * 2);
            mColors = colors;
            mPosition = position;
            Log.i("ColorPicker", "create " + mPosition);
        }

        public void draw(Canvas canvas) {
            int state = canvas.save();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(!mDragging);

            if (mGradient == null) {
                mGradient = new LinearGradient(PADDING, PADDING, getWidth() - PADDING, PADDING, mColors, null, Shader.TileMode.CLAMP);
            }

            mPaint.setColor(Color.BLACK);
            canvas.drawText(mLabel, PADDING, PADDING, mPaint);

            if (mRect == null) {
                mRect = new RectF(PADDING, PADDING/2 + mPaint.getTextSize()*1.5f, getWidth()-PADDING, getBarHeight()-PADDING/2);
            }
            mPaint.setShader(mGradient);
            canvas.drawRect(mRect, mPaint);

            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(0xAAAAAAAA);
            canvas.drawRect(mRect, mPaint);

            drawIndicator(canvas);

            canvas.restoreToCount(state);
        }

        private void drawIndicator(Canvas canvas) {
            int state = canvas.save();
            canvas.translate(mPosition*(getWidth()-PADDING*2)+PADDING, getBarHeight()/2);
            Log.i("ColorPicker", "Translate " + mPosition);

            if (mIndicator != null) {
                mIndicator.draw(canvas);
            } else {
                float r = (getBarHeight()/2 - PADDING - 10)/2;

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShader(null);
                mPaint.setColor(getColor());
                canvas.drawCircle(0, mPaint.getTextSize()*3/4, r, mPaint);

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(STROKE);
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(0, mPaint.getTextSize()*3/4, r, mPaint);

                mPaint.setColor(0xAAAAAAAA);
                mPaint.setStrokeWidth(1);
                canvas.drawCircle(0, mPaint.getTextSize()*3/4, r+STROKE, mPaint);
            }

            canvas.restoreToCount(state);
        }

        public void setVal(float val) {
            mPosition = val;
            Log.i("ColorPicker", "set " + mPosition);
        }

        public float dragTo(float x, float y) {
            x = Math.max(x, PADDING);
            x = Math.min(x, getWidth() - PADDING);
            x -= PADDING;
            mPosition = x/(getWidth() - PADDING*2);
            Log.i("ColorPicker", "drag " + mPosition);
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
            if (mMode == MODE.HSV) {
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
                setColor(Color.HSVToColor(hsl));
            } else {
                switch(bar) {
                    case 0:
                        int r = (int) (mBar1.dragTo(event.getX(), event.getY()) * 255);
                        setColor(Color.rgb(r, getGreen(), getBlue()));
                        break;
                    case 1:
                        int g = (int) (mBar2.dragTo(event.getX(), event.getY()) * 255);
                        setColor(Color.rgb(getRed(), g, getBlue()));
                        break;
                    case 2:
                    default:
                        int b = (int) (mBar3.dragTo(event.getX(), event.getY()) * 255);
                        setColor(Color.rgb(getRed(), getGreen(), b));
                        break;
                }
            }

            setupBars();
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
