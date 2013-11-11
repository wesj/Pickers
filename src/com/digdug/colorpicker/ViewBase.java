package com.digdug.colorpicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

abstract public class ViewBase extends View {
    public ViewBase(Context context) {
        super(context);
        init(context);
    }

    public ViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

        int[] vals = getVals(attrs);
        TypedArray appearance = getAppearance(context, attrs, vals, defStyle);

        if (appearance != null) {
            int n = appearance.length(); // getIndexCount();
            for (int i = 0; i < n; i++) {
                // int attr = i; //appearance.getIndex(i);
                handleAttribute(vals[i], appearance, i);
            }
            appearance.recycle();
        }
    }

    protected void handleAttribute(int type, TypedArray appearance, int index) {
        switch (type) {
            case android.R.attr.padding:
                int padding = appearance.getDimensionPixelSize(index, 0);
                setPadding(padding, padding, padding, padding);
                break;
            case android.R.attr.paddingLeft:
                setPadding(appearance.getDimensionPixelSize(index, 0), getPaddingTop(), getPaddingRight(), getPaddingBottom());
                break;
            case android.R.attr.paddingRight:
                setPadding(getPaddingLeft(), getPaddingTop(), appearance.getDimensionPixelSize(index, 0), getPaddingBottom());
                break;
            case android.R.attr.paddingTop:
                setPadding(getPaddingLeft(), appearance.getDimensionPixelSize(index, 0), getPaddingRight(), getPaddingBottom());
                break;
            case android.R.attr.paddingBottom:
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), appearance.getDimensionPixelSize(index, 0));
                break;
        }
    }

    abstract protected void init(Context context);

    public int[] getVals(AttributeSet attrs) {
        int[] vals = new int[0];
        if (attrs != null) {
            vals = new int[attrs.getAttributeCount()];
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                vals[i] = attrs.getAttributeNameResource(i);
            }
        }
        return vals;
    }

    public TypedArray getAppearance(Context context, AttributeSet attrs, int[] vals, int defStyle) {
        final Resources.Theme theme = context.getTheme();
        if (theme != null) {
            return theme.obtainStyledAttributes(attrs, vals, defStyle, android.R.style.TextAppearance_DeviceDefault);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int getMinimumWidth() {
        int h = super.getMinimumWidth();
        if (h == 0)
            return 1 * getContext().getResources().getDisplayMetrics().densityDpi;
        return h;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int getMinimumHeight() {
        int h = super.getMinimumHeight();
        if (h == 0)
            return 1 * getContext().getResources().getDisplayMetrics().densityDpi;
        return h;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int spec = View.MeasureSpec.getMode(widthMeasureSpec);
        int h = getMeasuredHeight();
        int w = getMeasuredWidth();
        int w2 = w;
        int h2 = h;

        int min = Math.min(w, h);
        if (spec == View.MeasureSpec.UNSPECIFIED || spec == View.MeasureSpec.AT_MOST) {
            w2 = Math.max(getMinimumWidth(), min);
            // w2 = Math.min(getMinimumWidth(), w2);
        }

        spec = View.MeasureSpec.getMode(heightMeasureSpec);
        if (spec == View.MeasureSpec.UNSPECIFIED || spec == View.MeasureSpec.AT_MOST) {
            h2 = Math.max(getMinimumHeight(), min);
            // h2 = Math.min(getMinimumHeight(), h2);
        }
        setMeasuredDimension(w2, h2);
    }

}