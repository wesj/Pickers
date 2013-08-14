package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaletteColorPicker extends View implements ColorPicker {
	int[] hueList = { 0, 30, 60, 120, 180, 210, 270, 330 };
	private int size = 5;
	private int hues = hueList.length;
	private int[][] mSwatchColors;
	private Path mPath;
	private ColorListener mListener;
	private Path mStartPath;
	private Path mEndPath;


	public PaletteColorPicker(Context context) {
		super(context);
		init();
	}

	public PaletteColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PaletteColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);		
	}

	private int current;
	private Bitmap cache;
	private Path mPath2;
	private Path mEndPath2;
	private Path mStartPath2;
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
		int[][] colors = getSwatchColors();

		float w = (getWidth()- getPaddingLeft() - getPaddingRight())/colors[0].length ;
		float h = (getHeight()- getPaddingTop() - getPaddingBottom())/colors.length;

		int save = canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop());
		for (int row = 0; row < colors.length; row++) {
			for (int col = 0; col < colors[row].length; col++) {
				drawSwatch(canvas, colors[row][col], row, col, w, h);
			}
		}
		canvas.restoreToCount(save);
	}

	private int[][] getSwatchColors() {
		if (mSwatchColors != null)
			return mSwatchColors;


		mSwatchColors = new int[size][hues];

		for (int i = 0; i < hues; i++) {
			int c = Color.HSVToColor(new float[] {hueList[i], 1.0f, 1.0f});
			if (i == hues-1) c= Color.WHITE;

			float r = Color.red(c)/255.0f;
			float g = Color.green(c)/255.0f;
			float b = Color.blue(c)/255.0f;

			for (int j = 0; j < size; j++) {
				float k = (size-1.0f-j)/(size-1.0f);
				// Avoid really dark shades if this color isn't white
				if (c != Color.WHITE) 
					k = k*0.6f + 0.4f;
				mSwatchColors[j][i] = Color.rgb((int)(255.0f*k*r), (int)(255.0f*k*g), (int)(255.0f*k*b));
			}
		}
		return mSwatchColors;
	}

	private void drawSwatch(Canvas canvas, int color, int row, int col, float w, float h) {
		float x = w * col;
		float y = h * row;
		Paint p = new Paint();

		p.setColor(color);
		int save = canvas.save();
		canvas.translate(x, y);
		int border = 5;
		canvas.translate(border, border);

		int[][] colors = getSwatchColors();
		Path path = null;
		if (row == 0) {
			if (col %2 == 0) path = getStartPath(w-border, h-border);
			else path = getStartPath2(w-border, h-border);
		} else if (row == colors.length -1) {
			if (col %2 == 0) path = getEndPath(w-border,h-border);
			else path = getEndPath2(w-border,h-border);
		} else {
			if (col %2 == 0) path = getPath(w-border, h-border);
			else path = getPath2(w-border, h-border);
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

	private Path getStartPath(float w, float h) {
		if (mStartPath != null)
			return mStartPath;
		mStartPath = new Path();
		mStartPath.moveTo(0.0f, 0.0f);
		mStartPath.lineTo(w, 0);
		mStartPath.lineTo(w, h);
		mStartPath.lineTo(w/2, h*1.25f);
		mStartPath.lineTo(0, h);
		mStartPath.lineTo(0, 0);
		mStartPath.close();
		return mStartPath;
	}

	private Path getStartPath2(float w, float h) {
		if (mStartPath2 != null)
			return mStartPath2;
		mStartPath2 = new Path();
		mStartPath2.moveTo(0.0f, 0.0f);
		mStartPath2.lineTo(w, 0);
		mStartPath2.lineTo(w, h);
		mStartPath2.lineTo(w/2, h*0.75f);
		mStartPath2.lineTo(0, h);
		mStartPath2.lineTo(0, 0);
		mStartPath2.close();
		return mStartPath2;
	}

	private Path getEndPath(float w, float h) {
		if (mEndPath != null)
			return mEndPath;
		mEndPath = new Path();
		mEndPath.moveTo(0.0f, 0.0f);
		mEndPath.lineTo(w/2, h*0.25f);
		mEndPath.lineTo(w, 0);
		mEndPath.lineTo(w, h);
		mEndPath.lineTo(0, h);
		mEndPath.lineTo(0, 0);
		mEndPath.close();
		return mEndPath;
	}
	
	private Path getEndPath2(float w, float h) {
		if (mEndPath2 != null)
			return mEndPath2;
		mEndPath2 = new Path();
		mEndPath2.moveTo(0.0f, 0.0f);
		mEndPath2.lineTo(w/2, h*-0.25f);
		mEndPath2.lineTo(w, 0);
		mEndPath2.lineTo(w, h);
		mEndPath2.lineTo(0, h);
		mEndPath2.lineTo(0, 0);
		mEndPath2.close();
		return mEndPath2;
	}

	private Path getPath(float w, float h) {
		if (mPath != null)
			return mPath;
		mPath = new Path();
		mPath.moveTo(0.0f, 0.0f);
		mPath.lineTo(w/2, h*0.25f);
		mPath.lineTo(w, 0);
		mPath.lineTo(w, h);
		mPath.lineTo(w/2, h*1.25f);
		mPath.lineTo(0, h);
		mPath.lineTo(0, 0);
		mPath.close();
		return mPath;
	}

	private Path getPath2(float w, float h) {
		if (mPath2 != null)
			return mPath2;
		mPath2 = new Path();
		mPath2.moveTo(0.0f, 0.0f);
		mPath2.lineTo(w/2, h*-0.25f);
		mPath2.lineTo(w, 0);
		mPath2.lineTo(w, h);
		mPath2.lineTo(w/2, h*0.75f);
		mPath2.lineTo(0, h);
		mPath2.lineTo(0, 0);
		mPath2.close();
		return mPath2;
	}
	
	public int getColor() {
		return current;
	}

	public void onSizeChange() {
		invalidate();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int spec = MeasureSpec.getMode(widthMeasureSpec);
		int h = getMeasuredHeight();
		int w = getMeasuredWidth();
		int w2 = w;
		int h2 = h;

		if (spec == MeasureSpec.UNSPECIFIED || spec == MeasureSpec.AT_MOST) {
			w2 = Math.max(500, Math.min(w, h));
		}

		spec = MeasureSpec.getMode(heightMeasureSpec);
		if (spec == MeasureSpec.UNSPECIFIED || spec == MeasureSpec.AT_MOST) {
			h2 = Math.max(500, Math.min(w, h));
		}
		setMeasuredDimension(w2, h2);
	}
	
	private void notifyListeners(int color) {
		if (mListener == null)
			return;

		mListener.onChange(color);
	}

	public void setColorChangeListener(ColorListener listener) {
		mListener = listener;
	}
}
