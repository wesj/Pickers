package com.digdug.colorpicker;

import android.annotation.TargetApi;
import android.content.Context;
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
    }

    abstract protected void init(Context context);

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