package com.digdug.colorpicker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by wesj on 11/10/13.
 */
abstract class Widget {
    private Bitmap mCache;
    protected View mView;

    public Widget(View view) {
        mView = view;
    }

    abstract public void draw(Canvas canvas);

    public void startHover() { }
    public void endHover() { }
    public void unHover() { }

    public void clearCaches() {
        mCache = null;
    }

    public Bitmap getCache() {
        if (mCache != null)
            return mCache;
        mCache = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mCache);
        draw(c);
        return mCache;
    }
}
