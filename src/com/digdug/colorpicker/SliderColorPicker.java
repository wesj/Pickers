package com.digdug.colorpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wesj on 10/30/13.
 */
public class SliderColorPicker extends ViewBase implements ColorPicker {
    private static final float STROKE_BASE = 2;
    private final float STROKE;

    private Typeface mTypeface;
    private ColorStateList mTextColor;
    private boolean mAllCaps;
    private ColorListener mListener;
    private Bar mBar1;
    private Bar mBar2;
    private Bar mBar3;
    private SparseArray<Bar> mDragging = new SparseArray<Bar>();

    private MODE mMode = MODE.RGB;
    private Drawable mIndicator;

    private int mColor;
    private float[] hsl;
    private static int[] mHueList = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };
    private CharSequence[] mLabels;
    private int mFontSize = -1;
    private float mPadding;
    private boolean shouldTint = true;

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
        this(context, null);
    }

    public SliderColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.style.TextAppearance_DeviceDefault);
    }

    public SliderColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources r = getResources();
        STROKE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, STROKE_BASE, r.getDisplayMetrics());

        final Resources.Theme theme = context.getTheme();
        if (theme != null) {
            int[] vals = new int[] {
                    android.R.attr.textColor,
                    android.R.attr.src,
                    android.R.attr.textSize,
                    android.R.attr.spacing,
                    android.R.attr.targetDescriptions,
            };
            TypedArray appearance = theme.obtainStyledAttributes(attrs, vals, defStyle, android.R.style.TextAppearance_DeviceDefault);

            if (appearance != null) {
                int n = appearance.getIndexCount();

                int typefaceIndex = 0;
                String fontFamily = "";
                int styleIndex = 0;

                for (int i = 0; i < n; i++) {
                    int attr = appearance.getIndex(i);

                    TypedValue val = new TypedValue();
                    appearance.getValue(i, val);

                    switch (vals[attr]) {
                        case android.R.attr.textColor:
                            mTextColor = appearance.getColorStateList(attr);
                            break;
                        case android.R.attr.targetDescriptions:
                            setLabels(appearance.getTextArray(attr));
                            break;
                        case android.R.attr.textSize:
                            setTextSize(appearance.getDimensionPixelSize(attr, -1));
                            break;
                        case android.R.attr.typeface:
                            typefaceIndex = appearance.getInt(attr, -1);
                            break;
                        case android.R.attr.fontFamily:
                            fontFamily = appearance.getString(attr);
                            break;
                        case android.R.attr.textAllCaps:
                            mAllCaps = appearance.getBoolean(attr, false);
                            break;
                        case android.R.attr.textStyle:
                            styleIndex = appearance.getInt(attr, -1);
                            break;
                        case android.R.attr.src:
                            mIndicator = appearance.getDrawable(attr);
                            break;
                        case android.R.attr.spacing:
                            setPadding(appearance.getDimensionPixelSize(attr, 0));
                    }
                }
                setTypefaceFromAttrs(fontFamily, typefaceIndex, styleIndex);
                appearance.recycle();
            }
        }

        if (mFontSize < 0) {
            setTextSize(40);
        }
    }

    public void setLabels(int[] labelIds) {
        if(labelIds.length != 3) {
            throw new IllegalArgumentException("Must pass three labels");
        }

        Resources res = getContext().getResources();
        setLabels(new CharSequence[]{
                res.getString(labelIds[0]),
                res.getString(labelIds[1]),
                res.getString(labelIds[2])
        });
    }

    public void setLabels(CharSequence[] labels) {
        if(labels.length != 3) {
            throw new IllegalArgumentException("Must pass three labels");
        }
        mLabels = labels;

        mBar1.setLabel(labels[0]);
        mBar2.setLabel(labels[1]);
        mBar3.setLabel(labels[2]);

        mPadding = Math.max(mPadding, mFontSize);
        invalidate();
    }

    public void setTextSize(int size) {
        mFontSize = size;
        if (mBar1 != null) mBar1.setTextSize(size);
        if (mBar2 != null) mBar2.setTextSize(size);
        if (mBar3 != null) mBar3.setTextSize(size);

        if (mLabels != null && mLabels.length > 0) {
            mPadding = Math.max(mPadding, size);
        }
        invalidate();
    }

    public void setPadding(int padding) {
        mPadding = Math.max(padding, mFontSize);
        invalidate();
    }

    @Override
    public int getColor() {
        return Color.HSVToColor(hsl);
    }

    public void setColor(int color) {
        internalSetColor(color);

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

    private void internalSetColor(int color) {
        mColor = color;
        Color.colorToHSV(color, hsl);
    }

    private void setupBars() {
        if (mBar1 == null || mBar2 == null || mBar3 == null) {
            if (mMode == MODE.HSV) {
                mBar1 = new Bar(getHueList(getSat(), getVal()), getHue()/360);
                mBar2 = new Bar(new int[] { Color.HSVToColor(new float[] { getHue(), 0f, getVal() }),
                        Color.HSVToColor(new float[] { getHue(), 1f, getVal() }) }, getSat());
                mBar3 = new Bar(new int[] { Color.HSVToColor(new float[] { getHue(), getSat(), 0f }),
                        Color.HSVToColor(new float[] { getHue(), getSat(), 1f }) }, getVal());
            } else {
                mBar1 = new Bar(new int[] { Color.rgb(       0, getGreen(), getBlue()), Color.rgb(     255, getGreen(), getBlue())}, getRed()/255);
                mBar2 = new Bar(new int[] { Color.rgb(getRed(),          0, getBlue()), Color.rgb(getRed(),        255, getBlue())}, getGreen()/255);
                mBar3 = new Bar(new int[] { Color.rgb(getRed(), getGreen(),         0), Color.rgb(getRed(), getGreen(),       255)}, getBlue()/255);
            }

            if (mLabels != null) {
                mBar1.setLabel(mLabels[0]);
                mBar2.setLabel(mLabels[1]);
                mBar3.setLabel(mLabels[2]);
            }
        } else {
            if (mMode == MODE.HSV) {
                mBar1.setColors(getHueList(getSat(), getVal()));
                mBar2.setColors(new int[]{Color.HSVToColor(new float[]{getHue(), 0f, getVal()}),
                        Color.HSVToColor(new float[]{getHue(), 1f, getVal()})});
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
        private RectF mRect;
        private CharSequence mLabel;

        public Bar(int[] colors, float position) {
            mPaint = new Paint();
            if (mFontSize > 0) {
                mPaint.setTextSize(mFontSize);
            }
            if (mTypeface != null) {
                mPaint.setTypeface(mTypeface);
            }
            mColors = colors;
            mPosition = position;
        }

        public void setTextSize(int size) {
            mPaint.setTextSize(size);
        }

        public void setLabel(CharSequence label) {
            mLabel = label;
        }

        public void draw(Canvas canvas) {
            int state = canvas.save();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(mDragging == null);

            if (mGradient == null) {
                mGradient = new LinearGradient(mPadding, mPadding, getWidth() - mPadding, mPadding, mColors, null, Shader.TileMode.CLAMP);
            }

            float textSize = 0f;
            if (mLabel != null) {
                if (mTextColor != null) {
                    mPaint.setColor(mTextColor.getColorForState(View.EMPTY_STATE_SET, Color.BLACK));
                }
                textSize = mPaint.getTextSize();
                canvas.drawText(mLabel.toString(), mPadding, mPadding, mPaint);
            }

            if (mRect == null) {
                mRect = new RectF(mPadding, mPadding + textSize*0.25f, getWidth()-mPadding, getBarHeight());
            }
            mPaint.setShader(mGradient);
            canvas.drawRect(mRect, mPaint);

            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(1);
            mPaint.setColor(0xAAAAAAAA);
            canvas.drawRect(mRect, mPaint);

            drawIndicator(canvas, textSize);

            canvas.restoreToCount(state);
        }

        private void drawIndicator(Canvas canvas, float textSize) {
            int state = canvas.save();
            canvas.translate(mPosition * (getWidth() - mPadding * 2) + mPadding, textSize/8 + mPadding/2 + getBarHeight() / 2);

            float r = (getBarHeight() - mPadding - textSize)/3;
            if (mIndicator != null) {
                float aspect = (float) mIndicator.getIntrinsicHeight() / (float) mIndicator.getIntrinsicWidth();
                mIndicator.setBounds((int) -r, (int) (-r*aspect), (int)r, (int) (r*aspect) );//mIndicator.getMinimumWidth(), mIndicator.getMinimumHeight());
                if (mIndicator instanceof BitmapDrawable && shouldTint) {
                    ((BitmapDrawable) mIndicator).setColorFilter(getColor(), PorterDuff.Mode.MULTIPLY);
                }
                mIndicator.draw(canvas);
            } else {
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setShader(null);
                mPaint.setColor(shouldTint ? getColor() : Color.WHITE);
                canvas.drawCircle(0, 0, r, mPaint);

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(STROKE);
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(0, 0, r, mPaint);

                mPaint.setColor(0xAAAAAAAA);
                mPaint.setStrokeWidth(1);
                canvas.drawCircle(0, 0, r+STROKE, mPaint);
            }

            canvas.restoreToCount(state);
        }

        public void setVal(float val) {
            mPosition = val;
        }

        public float dragTo(float x, float y) {
            x = Math.max(x, mPadding);
            x = Math.min(x, getWidth() - mPadding);
            x -= mPadding;
            mPosition = x/(getWidth() - mPadding*2);
            return mPosition;
        }

        public void setColors(int[] colors) {
            mColors = colors;
            mGradient = null;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP ||
            event.getActionMasked() == MotionEvent.ACTION_UP ||
            event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                mDragging.delete(event.getPointerId(i));
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                mDragging.append(event.getPointerId(i), getBarAt(event.getX(i), event.getY(i)));
            }
        }

        boolean changed = false;
        for (int i = 0; i < event.getPointerCount(); i++) {
            Bar dragging = mDragging.get(event.getPointerId(i));
            if (dragging != null) {
                float val = dragging.dragTo(event.getX(i), event.getY(i));
                if (mMode == MODE.HSV) {
                    if (dragging == mBar1)
                        hsl[0] = val * 360;
                    else if (dragging == mBar2)
                        hsl[1] = val;
                    else if (dragging == mBar3)
                        hsl[2] = val;
                    internalSetColor(Color.HSVToColor(hsl));
                } else {
                    if (dragging == mBar1) {
                        internalSetColor(Color.rgb((int) (val * 255), getGreen(), getBlue()));
                    } else if (dragging == mBar2) {
                        internalSetColor(Color.rgb(getRed(), (int) (val * 255), getBlue()));
                    } else if (dragging == mBar3) {
                        internalSetColor(Color.rgb(getRed(), getGreen(), (int) (val * 255)));
                    }
                }
                changed = true;
            }
        }

        if (changed) {
            setupBars();
            invalidate();

            if (mListener != null) {
                mListener.onChange(getColor());
            }
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

    private Bar getBarAt(float x, float y) {
        int bar = (int) Math.floor(y / getBarHeight());
        switch(bar) {
            case 0: return mBar1;
            case 1: return mBar2;
            case 2: return mBar3;
        }
        return mBar1;
    }

    private void setTypefaceFromAttrs(String familyName, int typefaceIndex, int styleIndex) {
        if (familyName != null) {
            mTypeface = Typeface.create(familyName, styleIndex);
            if (mTypeface != null) {
                return;
            }
        }
        switch (typefaceIndex) {
            case 1:
                mTypeface = Typeface.SANS_SERIF;
                break;
            case 2:
                mTypeface = Typeface.SERIF;
                break;
            case 3:
                mTypeface = Typeface.MONOSPACE;
                break;
        }
    }
}
