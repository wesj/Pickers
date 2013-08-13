package com.digdug.colorpicker;

import java.util.GregorianCalendar;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

public class TimePicker extends View {

	private int fontColor = 0xFF999999;
	private static int MARGIN = 40;
	private int fontSize = 40;
	private MotionEvent mDragging;
	private layoutParams mLayoutParams;
	private class layoutParams {
		float cx;
		float cy;
		float r;
		int w;
		int h;
		int fontSize;
		public layoutParams(int w, int h, int fontSize) {
			this.w = w;
			this.h = h;
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

	Mode currentMode;
	public class Mode {
		protected String name;
		public final int start;
		public final int end;
		public final int step;
		public int val;
		private Paint indicatorPaint;
		public Bitmap cache;
		public float animating = -1.0f;
		private AnimationRunnable mAnimRunnable;
		private float exit;

		Mode(String name, int start, int end, int step, float exit) {
			this.start = start;
			this.end = end;
			this.step = step;
			indicatorPaint = new Paint();
			this.name = name;
			this.exit = exit;

			mAnimRunnable = new AnimationRunnable(exit) {
				@Override
				protected boolean updateTimer(float t) {
					animating = t;
					if (mEndSize > mStartSize ? animating >= mEndSize : animating <= mEndSize) {
						animating = -1.0f;
						return true;
					}
					return false;
				}

				protected float getStart(float end) {
					if (animating != end && animating != -1) {
						return animating;
					}
					return end == 1.0f ? exit : 1.0f;
				}
			};
		}

		public boolean setVal(int val) {
			if (val == this.val)
				return false;

			if (val < start) val += end;
			if (val > end) val -= end;
			this.val = val;
			return true;
		}

		public void draw(Canvas canvas, layoutParams params, Paint paint) {
			if (mAnimRunnable.preventDraws)
				return;

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

			if (animating > -1) {
				canvas.scale(animating, animating);
				float alpha = (exit - animating)/(exit - 1.0f);
				paint.setAlpha((int) Math.max(0, Math.min(255,alpha*255)));
			} else {
				paint.setAlpha(255);
			}
			Log.i("WESJ", "Alpha " + name + ": " + paint.getAlpha() + " for " + animating);
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
	}

	public final Mode HOURS_MODE = new Mode("HOURS", 1,12,1, 1.5f);
	public final Mode MINUTES_MODE = new Mode("MINUTE", 0,60,5, 0.5f);

	private abstract class AnimationRunnable implements Runnable {
		protected long start = 0;
		protected float duration = 500.0f;
		protected float mStartSize = 0.0f;
		protected float mEndSize = 1.0f;
		protected float exit = 0.0f;
		public boolean preventDraws = false;
		protected TimeInterpolator interpolator = new AnticipateInterpolator(2.0f);
		public AnimationRunnable(float exit) { this.exit = exit; }

		abstract protected boolean updateTimer(float t);
		protected float getStart(float end) { return 0.0f; }

		@SuppressLint("NewApi")
		@Override
		public void run() {
			if (start == 0) {
				start = SystemClock.elapsedRealtime();
			}
			float t = interpolator.getInterpolation((SystemClock.elapsedRealtime() - start) / duration);
			boolean done = updateTimer(mStartSize + (mEndSize - mStartSize)*t);
			preventDraws = false;
			invalidate();
	
			if (done) {
				start = 0;
			} else {
				postDelayed(this, 10);
			}
		}

		private void goTo(float aEnd) {
			preventDraws = true;
			mStartSize = getStart(aEnd);
			mEndSize = aEnd;
			start = 0;
		}

		public AnimationRunnable grow() {
			goTo(1.0f);
			return this;
		}

		public AnimationRunnable shrink() {
			goTo(exit);
			return this;
		}
	}
	
	private AnimationRunnable mAMRunnable = new AnimationRunnable(0.0f) {
		@Override
		protected boolean updateTimer(float t) {
			mAnimatingAM = t;
			if (mAnimatingAM >= mEndSize) {
				mAnimatingAM = -1.0f;
				return true;
			}
			return false;
		}
	};

	private TimeChangeListener mTimeChangeListener;
	private float mAnimatingAM = -1.0f;
	private Paint basePaint;
	private static int indicatorColor;

	public TimePicker(Context context) {
		super(context);
		init(context);
	}

	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		currentMode = HOURS_MODE;
		indicatorColor = context.getResources().getColor(android.R.color.holo_blue_bright);
		basePaint = new Paint();
	}

	public boolean onTouchEvent(MotionEvent e) {
		if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
			if (mDragging != null)
				return false;

			mDragging = MotionEvent.obtain(e);
		} else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
			if (dist(mDragging, e) < 20.0f) {
				if (isInCenter(e)) {
					toggleState();
				} else if (currentMode.setVal(findHover(e.getX(), e.getY()))) {
					if (mTimeChangeListener != null) {
						mTimeChangeListener.onTimeChange("AM".equals(partOfDay) ? HOURS_MODE.val : HOURS_MODE.val + 12, MINUTES_MODE.val);
					}
					invalidate();
					if (currentMode == HOURS_MODE)
						flipMode();
				}
			} else {
				if (currentMode == HOURS_MODE)
					flipMode();
				if (mTimeChangeListener != null) {
					mTimeChangeListener.onTimeChange("AM".equals(partOfDay) ? HOURS_MODE.val : HOURS_MODE.val + 12, MINUTES_MODE.val);
				}
			}
			mDragging.recycle();
			mDragging = null;
			return false;
		}

		if (mDragging != null) {
			if (dist(mDragging, e) < 20) {
				return true;
			}
			if (currentMode.setVal(findHover(e.getX(), e.getY()))) {
				if (mTimeChangeListener != null) {
					mTimeChangeListener.onTimeChange("AM".equals(partOfDay) ? HOURS_MODE.val : HOURS_MODE.val + 12, MINUTES_MODE.val);
				}
				invalidate();
			}
			return true;
		}

		return false;
	}

	private boolean isInCenter(MotionEvent e) {
		float dx = e.getX() - mLayoutParams.cx;
		float dy = e.getY() - mLayoutParams.cy;
		return Math.sqrt(dx*dx + dy*dy) < 40;
	}

	private float dist(MotionEvent e1, MotionEvent e2) {
		if (e1 == null || e2 == null)
			return 0;
		float dx = e1.getX() - e2.getX();
		float dy = e1.getY() - e2.getY();
		return (float) Math.sqrt(dx*dx + dy*dy);
	}

	public void setMode(Mode mode) {
		if (currentMode == mode)
			return;

		post(currentMode.mAnimRunnable.shrink());
		currentMode = mode;
		post(currentMode.mAnimRunnable.grow());
	}

	private void flipMode() {
		if (currentMode == HOURS_MODE) setMode(MINUTES_MODE);
		else setMode(HOURS_MODE);
	}

	private int findHover(float x, float y) {
		float cx = getWidth()/2;
		float cy = getHeight()/2;
		double angle = -1*Math.atan((x-cx)/(y-cy)) * 180/Math.PI;
		if (y > cy) angle += 180;
		return (int) Math.round(angle/360f * currentMode.end);
	}
	
	public void onDraw(Canvas canvas) {
		if (mLayoutParams == null) {
			mLayoutParams = new layoutParams(getWidth(), getHeight(), fontSize);
		}

		int save = canvas.save();
		canvas.translate(mLayoutParams.cx, mLayoutParams.cy);

		basePaint.setTextSize(fontSize);
		basePaint.setColor(fontColor);

		if (HOURS_MODE.animating > -1f || MINUTES_MODE.animating > -1f) {
			if (HOURS_MODE.animating > -1f)
				HOURS_MODE.draw(canvas, mLayoutParams, basePaint);
			if (MINUTES_MODE.animating > -1f)
				MINUTES_MODE.draw(canvas, mLayoutParams, basePaint);
		} else {
			currentMode.draw(canvas, mLayoutParams, basePaint);
		}

		//Paint p2 = new Paint();
		//p2.setColor(Color.WHITE);
		//canvas.drawCircle(0, 0, 40, p2);

		if (mAnimatingAM > -1f) {
			if ("AM".equals(partOfDay)) {
				drawAM(canvas,  "PM",     interp(1f, 0f, mAnimatingAM), (int)((1f - mAnimatingAM)*255f));
				drawAM(canvas, partOfDay, interp(2f, 1f, mAnimatingAM), (int) (mAnimatingAM*255f));
			} else {
				drawAM(canvas, "AM",      interp(1f, 2f, mAnimatingAM), (int)((1f - mAnimatingAM)*255f));
				drawAM(canvas, partOfDay, interp(0f, 1f, mAnimatingAM), (int) (mAnimatingAM*255f));				
			}
		} else {
			drawAM(canvas, partOfDay, 1.0f, 255);
		}

		canvas.restoreToCount(save);
	}

	private float interp(float start, float end, float t) {
		return start + (end-start)*t;
	}

	private String partOfDay = "AM";
	private void drawAM(Canvas canvas, String txt, float scale, int alpha) {
		int save = canvas.save();
		//Log.i("WESJ", "Scale " + scale + ", " + alpha);
		canvas.scale(scale, scale);

		Paint p = new Paint();
		p.setTextSize(fontSize);
		p.setColor(fontColor);
		p.setAlpha(Math.min(255, Math.max(0, alpha)));

		float t = p.measureText(txt);
		canvas.drawText(txt, -t/2, t/4, p);

		canvas.restoreToCount(save);
	}
	
	public interface TimeChangeListener {
		public void onTimeChange(int hour, int minutes);
	}

	public void setTimeChangeListener(TimeChangeListener timeChangeListener) {
		mTimeChangeListener = timeChangeListener;
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int spec = MeasureSpec.getMode(widthMeasureSpec);
		int h = getMeasuredHeight();
		int w = getMeasuredWidth();
		int w2 = w;
		int h2 = h;

		if (spec == MeasureSpec.UNSPECIFIED || spec == MeasureSpec.AT_MOST) {
			w2 = Math.min(w, h);
		}

		spec = MeasureSpec.getMode(heightMeasureSpec);
		if (spec == MeasureSpec.UNSPECIFIED || spec == MeasureSpec.AT_MOST) {
			h2 = Math.min(w, h);
		}
		setMeasuredDimension(w2, h2);
	}

	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		mLayoutParams = null;
		HOURS_MODE.cache = null;
		MINUTES_MODE.cache = null;
	}

	public void setTime(int hours, int minutes) {
		HOURS_MODE.setVal(hours);
		MINUTES_MODE.setVal(minutes);
	}

	public GregorianCalendar getTime() {
		return new GregorianCalendar(2000, 1, 1, HOURS_MODE.val, MINUTES_MODE.val, 0);
	}
	
	public void toggleState() {
		partOfDay = "AM".equals(partOfDay) ? "PM" : "AM";
		if (mTimeChangeListener != null) {
			mTimeChangeListener.onTimeChange("AM".equals(partOfDay) ? HOURS_MODE.val : HOURS_MODE.val + 12, MINUTES_MODE.val);
		}
		post(mAMRunnable);
	}

	public void setIndicatorColor(int textColor) {
		indicatorColor = textColor;
	}
	
}
