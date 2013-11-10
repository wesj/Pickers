package com.digdug.colorpicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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
        ColorPicker.ColorListener, TabHost.OnTabChangeListener {
    private ColorPicker mCurrent;
    private ColorListener mListener;
    private TabWidget mTabWidget;
    private FrameLayout mContent;
    private LinearLayout mLinear;
    private HashMap<String, View> mViews;
    private int mColor;

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
        setOnTabChangedListener(this);
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

        Resources res = getContext().getResources();
        addTab(newTabSpec("triangle").setIndicator("", res.getDrawable(R.drawable.triangle)).setContent(this));
        addTab(newTabSpec("palette").setIndicator("", res.getDrawable(R.drawable.palette)).setContent(this));
        addTab(newTabSpec("hsv").setIndicator("", res.getDrawable(R.drawable.hsv)).setContent(this));
        addTab(newTabSpec("rgb").setIndicator("", res.getDrawable(R.drawable.rgb)).setContent(this));
    }

    @Override
    public int getColor() {
        return mColor;
    }

    @Override
    public void setColor(int color) {
        mColor = color;

        if (mCurrent != null)
            mCurrent.setColor(color);
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
                v = new FancyPalette(getContext());
                v.setPadding(20, 20, 20, 20);

                int size = 18;
                int[] colors = new int[size];
                for (int i = 0; i < size; i++) {
                    colors[i] = Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                }
                ((FancyPalette)v).setPalette(colors);
                ((FancyPalette)v).showDefaultPalette(false);
            } else if ("hsv".equals(s)) {
                v = new SliderColorPicker(getContext());
                ((SliderColorPicker)v).setMode(SliderColorPicker.MODE.HSV);
                ((SliderColorPicker)v).setLabels(new int[]{R.string.hue, R.string.sat, R.string.value});
                ((SliderColorPicker)v).setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.large_font_size));
            } else if ("rgb".equals(s)) {
                v = new SliderColorPicker(getContext());
                ((SliderColorPicker)v).setMode(SliderColorPicker.MODE.RGB);
                ((SliderColorPicker)v).setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.medium_font_size));
                ((SliderColorPicker)v).setSpacer(40);
                v.setPadding(40,50,40,50);
            }

            if (v != null) {
                ((ColorPicker)v).setColorChangeListener(this);
                mContent.addView(v);
                mViews.put(s, v);
            }
        }

        return mViews.get(s);
    }

    @Override
    public void onChange(int color) {
        mColor = color;
        if (mListener != null)
            mListener.onChange(color);
    }

    @Override
    public void onTabChanged(String s) {
        mCurrent = (ColorPicker) mViews.get(s);
        mCurrent.setColor(getColor());
    }
}
