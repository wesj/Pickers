package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;

/**
 * Created by wesj on 11/10/13.
 */
public class FancyPalette extends PaletteColorPicker {
    private java.util.HashMap<PathType, Path> mPaths = new java.util.HashMap<PathType, Path>();
    private float mWidth = -1f;
    private float mHeight = -1f;

    public FancyPalette(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FancyPalette(Context context) {
        super(context);
    }

    protected enum PathType {
        SQUARE,
        UPPER_UP,
        UPPER_DOWN,
        UP,
        DOWN,
        LOWER_UP,
        LOWER_DOWN
    }

    private enum DipType {
        DOWN,
        UP,
        NONE
    }

    protected Path getPath(int row, int rows, int col, int cols, float w, float h) {
        if (w != mWidth || h != mHeight) {
            mPaths.clear();
        }

        int border = getBorder();
        if (row == 0 && rows == 1) {
            return getPath(PathType.SQUARE, w - border, h - border);
        } else if (row == 0) {
            if (col %2 == 0) return getPath(PathType.UPPER_DOWN, w-border, h-border);
            else return getPath(PathType.UPPER_UP, w-border, h-border);
        } else if (row == rows - 1) {
            if (col %2 == 0) return getPath(PathType.LOWER_DOWN, w-border,h-border);
            else return getPath(PathType.LOWER_UP, w-border,h-border);
        } else {
            if (col %2 == 0) return getPath(PathType.DOWN, w-border, h-border);
            else return getPath(PathType.UP, w-border, h-border);
        }
    }

    private Path getPath(PathType type, float w, float h) {
        DipType top = DipType.NONE;
        DipType bottom = DipType.NONE;
        switch(type) {
            case UPPER_DOWN:
                bottom = DipType.DOWN;
                break;
            case UPPER_UP:
                bottom = DipType.UP;
                break;
            case LOWER_DOWN:
                top = DipType.DOWN;
                break;
            case LOWER_UP:
                top = DipType.UP;
                break;
            case DOWN:
                top = DipType.DOWN;
                bottom = DipType.DOWN;
                break;
            case UP:
                top = DipType.UP;
                bottom = DipType.UP;
                break;
            case SQUARE:
                break;
        }
        return getPath(type, top, bottom, w, h);
    }

    private Path getPath(PathType type, DipType topDip, DipType bottomDip, float w, float h) {
        if (mPaths.containsKey(type))
            return mPaths.get(type);

        Path p = new Path();
        p.moveTo(0.0f, 0.0f);

        if (topDip == DipType.DOWN)
            p.lineTo(w/2, h*0.25f);
        else if (topDip == DipType.UP)
            p.lineTo(w/2, h*-0.25f);

        p.lineTo(w, 0);
        p.lineTo(w, h);

        if (bottomDip == DipType.DOWN)
            p.lineTo(w/2, h*1.25f);
        else if (bottomDip == DipType.UP)
            p.lineTo(w/2, h*0.75f);

        p.lineTo(0, h);
        p.lineTo(0, 0);
        p.close();
        mPaths.put(type, p);
        return p;
    }
}
