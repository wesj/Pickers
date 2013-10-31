package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class NumberPicker extends ViewBase {

	private int fontColor = 0xFF999999;
	private static int MARGIN = 40;
	private int fontSize = 40;
	private MotionEvent mDragging;
	private layoutParams mLayoutParams;
	protected String name;
	public int start;
	public int end;
	public int step;
	private int val;

	private Paint indicatorPaint;
	public Bitmap cache;

	private class layoutParams {
		float cx;
		float cy;
		float r;
		int fontSize;
		public layoutParams(int w, int h, int fontSize) {
			cx = w/2f;
			cy = h/2f;
			r = Math.min(cx, cy) - MARGIN - Math.max(getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
			this.fontSize = fontSize;
		}
	}

	public void setFontSize(int size) {
		fontSize = size;
		mLayoutParams = null;
	}

	public void draw(Canvas canvas, layoutParams params, Paint paint) {
		if (cache == null) {
			float r = params.r + params.fontSize;
			cache = Bitmap.createBitmap((int)(r*2f), (int)(r*2f), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(cache);

			int save2 = c.save();
			c.translate(r, r);
			drawNumbers(c, params, paint);
			c.restoreToCount(save2);
		}

		int save = canvas.save();

		drawHighlight(canvas, params, paint);
		canvas.drawBitmap(cache, -1*cache.getWidth()/2, -1*cache.getHeight()/2, paint);

		canvas.restoreToCount(save);		
	}

	// TODO: Indicators should support being a drawable?
	private void drawHighlight(Canvas canvas, layoutParams params, Paint paint) {
		int save = canvas.save();
		indicatorPaint.setColor(indicatorColor);

		Path path = new Path();
		path.addCircle(0, 0, params.r+params.fontSize, Path.Direction.CW);
		path.addCircle(0, 0, fontSize, Path.Direction.CCW);
		canvas.clipPath(path);
		float angle = getAngleForPosition();
		float cx = (float)Math.cos(angle)*(params.r - params.fontSize);
		float cy = (float)Math.sin(angle)*(params.r - params.fontSize);

		indicatorPaint.setAlpha(paint.getAlpha()/4);
		indicatorPaint.setStrokeWidth(params.fontSize/4);
		canvas.drawLine(0, 0, cx, cy, indicatorPaint);

		cx = (float)Math.cos(angle)*(params.r);
		cy = (float)Math.sin(angle)*(params.r);
		canvas.drawCircle(cx, cy, params.fontSize, indicatorPaint);
		paint.setStrokeWidth(1);
		canvas.restoreToCount(save);
	}
	
	private float getAngleForPosition() {
		return getAngleForPosition(val);
	}

	private float getAngleForPosition(float i) {
		return (float) (i/this.end*Math.PI*2f - Math.PI/2f);
	}

	private void drawNumbers(Canvas canvas, layoutParams params, Paint p) {
		int save = canvas.save();
		for (int i = start; i < end || i%end < start; i += step) {
			float angle = getAngleForPosition(i);
			String txt = Integer.toString(i);
			float t = p.measureText(txt);
			canvas.drawText(txt, (float)(params.r*Math.cos(angle)) - t/2f, (float)(params.r*Math.sin(angle) + params.fontSize/3f), p);
		}
		canvas.restoreToCount(save);
	}

	public interface NumberChangeListener {
		public void onChange(int val);
	}
	
	private NumberChangeListener mListener;
	private Paint basePaint;
	private static int indicatorColor;

	public NumberPicker(Context context) {
		super(context);
	}

	public NumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


    protected void init(Context context) {
		indicatorPaint = new Paint();
		indicatorColor = context.getResources().getColor(android.R.color.holo_blue_bright);
		basePaint = new Paint();
	}

	public void notifyListeners() {
		if (mListener != null) {	
			Log.i("WESJ", "Notify " + getValue());
			mListener.onChange(getValue());
		}		
	}
	
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
			if (mDragging != null)
				return false;

			mDragging = MotionEvent.obtain(e);
		} else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
			if (dist(mDragging, e) < 20.0f) {
				if (setVal(findHover(e.getX(), e.getY()))) {
					invalidate();
				}
			}
			notifyListeners();
			mDragging.recycle();
			mDragging = null;
			return false;
		}

		if (mDragging != null) {
			if (dist(mDragging, e) < 20) {
				return true;
			}
			if (setVal(findHover(e.getX(), e.getY()))) {
				notifyListeners();
				invalidate();
			}
			return true;
		}

		return false;
	}

	private float dist(MotionEvent e1, MotionEvent e2) {
		if (e1 == null || e2 == null)
			return 0;
		float dx = e1.getX() - e2.getX();
		float dy = e1.getY() - e2.getY();
		return (float) Math.sqrt(dx*dx + dy*dy);
	}

	private int findHover(float x, float y) {
		float cx = getWidth()/2;
		float cy = getHeight()/2;
		double angle = -1*Math.atan((x-cx)/(y-cy)) * 180/Math.PI;
		if (y > cy) angle += 180;
		return (int) Math.round(angle/360f * end);
	}
	
	public void onDraw(Canvas canvas) {
		if (mLayoutParams == null) {
			mLayoutParams = new layoutParams(getWidth(), getHeight(), fontSize);
		}

		int save = canvas.save();
		canvas.translate(mLayoutParams.cx, mLayoutParams.cy);

		basePaint.setTextSize(fontSize);
		basePaint.setColor(fontColor);

		draw(canvas, mLayoutParams, basePaint);

		canvas.restoreToCount(save);
	}

	public void setListener(NumberChangeListener listener) {
		mListener = listener;
	}

	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		mLayoutParams = null;
	}

	public boolean setVal(int val) {
		if (val == this.val)
			return false;

		if (val < start) val += end;
		if (val > end) val -= end;
		this.val = val;
		return true;
	}

	public int getValue() {
		return val;
	}

	public void setIndicatorColor(int textColor) {
		indicatorColor = textColor;
	}

	public void setRange(int i, int j, int k) {
		start = i;
		end = j;
		step = k;
	}
	
}
