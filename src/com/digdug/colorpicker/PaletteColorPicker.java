package com.digdug.colorpicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

public class PaletteColorPicker extends ViewBase implements ColorPicker {
    final int[] HUE_LIST = { 0, 30, 60, 120, 180, 210, 270, 330 };
    final private int NUM_ROWS = 5;
	private int hues = HUE_LIST.length;

	private ColorListener mListener;

    private int[][] mUserPalette;
    private int[][] mSwatchColors;

    private enum PathType {
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
    private HashMap<PathType, Path> mPaths = new HashMap<PathType, Path>();
    private int current;
    private Bitmap cache;

    public PaletteColorPicker(Context context) {
		super(context);
	}

	public PaletteColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PaletteColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void init(Context context) {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);		
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			return false;
		}
		
		float x = event.getX();
		float y = event.getY();
		int color;
		try {
			color = getColorFromPoint(x,y);
		} catch(Exception ex) {
			return true;
		}

		if (Color.alpha(color) < 1.0f) {
			return true;
		}
		current = color;
		notifyListeners(current);
		invalidate();
		return true;
	}
	
	private int getColorFromPoint(float x, float y) {
		return getCache().getPixel((int)x, (int)y);
	}

	private Bitmap getCache() {
		if (cache != null) {
			return cache;
		}

		cache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(cache);
		drawSwatches(c);
		return cache;
	}

	public void onDraw(Canvas canvas) {
		drawSwatches(canvas);
	}

	private void drawSwatches(Canvas canvas) {
        int l = mUserPalette != null ? mUserPalette.length : 0;
		int[][] colors = getSwatchColors(l);

		float w = 0;
		float h = (getHeight()- getPaddingTop() - getPaddingBottom()) / (colors.length + l);

		int save = canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop());

        if (mUserPalette != null) {
            mPaths.clear();
            w = (getWidth()- getPaddingLeft() - getPaddingRight()) / mUserPalette[0].length;

            for (int row = 0; row < mUserPalette.length; row++) {
                for (int col = 0; col < mUserPalette[row].length; col++) {
                    drawSwatch(canvas, mUserPalette[row][col], row, mUserPalette.length, col, mUserPalette[0].length, w, h);
                }
            }
            canvas.translate(0, mUserPalette.length*h);
        }

        w = (getWidth()- getPaddingLeft() - getPaddingRight()) / colors[0].length;
        mPaths.clear();
        for (int row = 0; row < colors.length; row++) {
            for (int col = 0; col < colors[row].length; col++) {
                drawSwatch(canvas, colors[row][col], row, colors.length, col, colors[0].length, w, h);
            }
        }
		canvas.restoreToCount(save);
	}

	private int[][] getSwatchColors(int alreadyTakenRows) {
		if (mSwatchColors != null)
			return mSwatchColors;

        int s = NUM_ROWS - alreadyTakenRows;
		mSwatchColors = new int[s][hues];
		for (int i = 0; i < hues; i++) {
			int c = Color.HSVToColor(new float[] {HUE_LIST[i], 1.0f, 1.0f});
			if (i == hues-1) c= Color.WHITE;

			float r = Color.red(c)/255.0f;
			float g = Color.green(c)/255.0f;
			float b = Color.blue(c)/255.0f;

			for (int j = 0; j < s; j++) {
				float k = (s-1.0f-j)/(s-1.0f);
				// Avoid really dark shades if this color isn't white
				if (c != Color.WHITE) 
					k = k*0.6f + 0.4f;
				mSwatchColors[j][i] = Color.rgb((int)(255.0f*k*r), (int)(255.0f*k*g), (int)(255.0f*k*b));
			}
		}
		return mSwatchColors;
	}

	private void drawSwatch(Canvas canvas, int color, int row, int rows, int col, int cols, float w, float h) {
		float x = w * col;
		float y = h * row;
		Paint p = new Paint();

		p.setColor(color);
		int save = canvas.save();
		canvas.translate(x, y);
		int border = 5;
		canvas.translate(border, border);

		Path path = null;
        if (row == 0 && rows == 1) {
            path = getPath(PathType.SQUARE, w - border, h - border);
        } else if (row == 0) {
			if (col %2 == 0) path = getPath(PathType.UPPER_DOWN, w-border, h-border);
			else path = getPath(PathType.UPPER_UP, w-border, h-border);
		} else if (row == rows - 1) {
			if (col %2 == 0) path = getPath(PathType.LOWER_DOWN, w-border,h-border);
			else path = getPath(PathType.LOWER_UP, w-border,h-border);
		} else {
			if (col %2 == 0) path = getPath(PathType.DOWN, w-border, h-border);
			else path = getPath(PathType.UP, w-border, h-border);
		}

		canvas.drawPath(path, p);
		if (color == Color.WHITE) {
			p.setStyle(Paint.Style.STROKE);
			p.setColor(Color.BLACK);
			p.setStrokeWidth(1);
			canvas.drawPath(path, p);			
		}

		if (getColor() == color) {
			p.setStrokeJoin(Join.ROUND);
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			p.setColor(hsv[2] > 0.5 ? Color.DKGRAY : Color.LTGRAY);
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(border*2);
			canvas.drawPath(path, p);
		}
		canvas.restoreToCount(save);
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

	public int getColor() {
		return current;
	}

    public void setColor(int color) {
        current = color;
        invalidate();
    }

	public void onSizeChange() {
		invalidate();
	}

	private void notifyListeners(int color) {
		if (mListener == null)
			return;

		mListener.onChange(color);
	}

	public void setColorChangeListener(ColorListener listener) {
		mListener = listener;
	}

    public void setPalette(int[] colors) {
        double rows = 0;
        double cols = 0;
        if (colors.length >= 8) {
            rows = Math.ceil((float)colors.length / (float) hues);
            cols = Math.min(hues, Math.ceil(colors.length / rows));
        } else if (colors.length > 4) {
            rows = 2;
            cols = Math.ceil(colors.length / 2f);
        } else {
            rows = 1;
            cols = colors.length;
        }

        mUserPalette = new int[(int) rows][(int) cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = (int) (row*cols + col);
                if (index >= colors.length)
                    mUserPalette[row][col] = Color.WHITE;
                else
                    mUserPalette[row][col] = colors[index];
            }
        }
        mSwatchColors = null;
    }
}
