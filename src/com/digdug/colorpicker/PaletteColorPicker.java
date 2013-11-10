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

public class PaletteColorPicker extends ViewBase implements ColorPicker {
    final int[] HUE_LIST = { 0, 30, 60, 120, 180, 210, 270, 330 };
    final private int NUM_ROWS = 5;
	private int hues = HUE_LIST.length;

	private ColorListener mListener;

    private int[][] mUserPalette;
    private int[][] mSwatchColors;
    private Path mPath;

    private int current;
    private Bitmap cache;

    private int mBorder = 5;
    private boolean mShowDefault = true;

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
		float h;
        if (l == 0 && colors.length == 0) {
            h = getHeight()- getPaddingTop() - getPaddingBottom();
        } else {
            h = (getHeight()- getPaddingTop() - getPaddingBottom()) / (colors.length + l);
        }

		int save = canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop());

        if (mUserPalette != null) {
            mPath = null;
            w = (getWidth()- getPaddingLeft() - getPaddingRight()) / mUserPalette[0].length;

            for (int row = 0; row < mUserPalette.length; row++) {
                for (int col = 0; col < mUserPalette[row].length; col++) {
                    drawSwatch(canvas, mUserPalette[row][col], row, mUserPalette.length, col, mUserPalette[0].length, w, h);
                }
            }
            canvas.translate(0, mUserPalette.length*h);
        }

        if (colors.length == 0 || colors[0].length == 0) {
            w = (getWidth()- getPaddingLeft() - getPaddingRight());
        } else {
            w = (getWidth()- getPaddingLeft() - getPaddingRight()) / colors[0].length;
        }
        mPath = null;
        for (int row = 0; row < colors.length; row++) {
            for (int col = 0; col < colors[row].length; col++) {
                drawSwatch(canvas, colors[row][col], row, colors.length, col, colors[0].length, w, h);
            }
        }
		canvas.restoreToCount(save);
	}

    public int getBorder() {
        return mBorder;
    }

    public void setBorder(int border) {
        mBorder = border;
    }

    public void showDefaultPalette(boolean showPalette) {
        mShowDefault = showPalette;
        mSwatchColors = null;
    }

	private int[][] getSwatchColors(int alreadyTakenRows) {
		if (mSwatchColors != null)
			return mSwatchColors;

        if (!mShowDefault) {
            mSwatchColors = new int[0][0];
            return mSwatchColors;
        }

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
		canvas.translate(mBorder, mBorder);

		Path path = getPath(row, rows, col, cols, w, h);
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
			p.setStrokeWidth(mBorder*2);
			canvas.drawPath(path, p);
		}
		canvas.restoreToCount(save);
	}

    protected Path getPath(int row, int rows, int col, int cols, float w, float h) {
        if (mPath == null) {
            int border = getBorder();
            mPath = new Path();
            mPath.moveTo(border, border);
            mPath.lineTo(w - 2*border, border);
            mPath.lineTo(w - 2*border, h - 2*border);
            mPath.lineTo(border, h - 2*border);
            mPath.lineTo(border, border);
            mPath.close();
        }
        return mPath;
    }

	public int getColor() {
		return current;
	}

    public void setColor(int color) {
        current = color;
        invalidate();
    }

	public void onSizeChange() {
        mPath = null;
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
