package com.digdug.colorpicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.HashMap;

/**
 * Created by wesj on 9/13/13.
 */
public class MultiColorPicker extends TabHost implements ColorPicker,
        TabHost.TabContentFactory,
        ColorPicker.ColorListener  {
    private ColorPicker mCurrent;
    private ColorListener mListener;
    private TabWidget mTabWidget;
    private FrameLayout mContent;
    private LinearLayout mLinear;
    private HashMap<String, View> mViews;

    public MultiColorPicker(Context context) {
        super(context, null);
        init();
    }

    public MultiColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void init() {
        mViews = new HashMap<String, View>();

        mLinear = new LinearLayout(getContext());
        mLinear.setOrientation(LinearLayout.VERTICAL);
        mLinear.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mLinear);

        /*
        Display display = ((WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        if (p.x > p.y) {
            mLinear.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            mLinear.setOrientation(LinearLayout.VERTICAL);
        }
        */

        mTabWidget = new TabWidget(getContext(), null, android.R.style.Widget_Holo_TabWidget);
        mTabWidget.setId(android.R.id.tabs);
        // mTabWidget.setOrientation(LinearLayout.VERTICAL);
        // mTabWidget.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        mContent = new FrameLayout(getContext());
        mContent.setId(android.R.id.tabcontent);
        mContent.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mLinear.addView(mContent);
        mLinear.addView(mTabWidget);

        setup();

        addTab(newTabSpec("triangle").setIndicator("Triangle").setContent(this));
        addTab(newTabSpec("palette").setIndicator("Palette").setContent(this));
        addTab(newTabSpec("hsv").setIndicator("hsv").setContent(this));
    }

    @Override
    public int getColor() {
        if (mCurrent != null)
            return mCurrent.getColor();
        return 0;
    }

    @Override
    public void setColorChangeListener(ColorListener listener) {
        mListener = listener;
    }

    @Override
    public View createTabContent(String s) {
        if (!mViews.containsKey(s)) {
            View v = null;
            if ("triangle".equals(s)) {
                v = new TriangleColorPicker(getContext());
            } else if ("palette".equals(s)) {
                v = new PaletteColorPicker(getContext());
            } else if ("hsv".equals(s)) {
                v = new SliderColorPicker(getContext());
            }

            if (v != null) {
                ((ColorPicker)v).setColorChangeListener(this);
                mContent.addView(v);
                mViews.put(s, v);
            }
        }

        // mViews.get(s).setColor(mCurrent);
        return mViews.get(s);
    }

    @Override
    public void onChange(int color) {
        if (mListener != null)
            mListener.onChange(color);
    }
}
