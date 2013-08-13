package com.digdug.colorpicker;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

public class DatePicker extends View {

	public interface DateChangeListener {
		public void onChange(Calendar cal);
	}

	private MotionEvent mDragging;
	private float startScroll;
	private Paint selectorPaint;
	private Paint calendarPaint;

	public void setFontSize(int size) {
	}

	public DatePicker(Context context) {
		super(context);
		init(context);
	}

	public DatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		current = new Cal(new GregorianCalendar());
		selectorPaint = new Paint();
		calendarPaint = new Paint();
	}

	private class DateLayoutParams {
		float w;
		float h;
		float w2;
		float header = 0;

		public DateLayoutParams(int w, int h) {
			this.w = w;
			this.h = h;
			w2 = (int) (w / 7);
		}

		public float getRowHeight(Calendar cal) {
			return h / (cal.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH) + 1);
		}
	}

	private DateLayoutParams mParams;
	private DateLayoutParams getParams() {
		if (mParams == null) {
			mParams = new DateLayoutParams(getWidth() - getPaddingLeft() - getPaddingRight(),
					getHeight() - getPaddingTop() - getPaddingBottom());
		}
		return mParams;
	}
	
	VelocityTracker velTrack;
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
			if (mDragging != null)
				return false;

			mDragging = MotionEvent.obtain(e);
			startScroll = scrollY;
			if (velTrack != null) {
				velTrack.recycle();
				velTrack = null;
			}
			velTrack = VelocityTracker.obtain();
		} else if (e.getActionMasked() == MotionEvent.ACTION_UP) {
			if (dist(mDragging, e) < 20.0f) {
				current.selectedDate = findDateFromPoint(e);
				notifyListeners();
				invalidate();
				velTrack.recycle();
				velTrack = null;
			}
			mDragging.recycle();
			mDragging = null;

			if (velTrack == null)
				return false;

			velTrack.computeCurrentVelocity(1);
			post(new Runnable() {
				final long startTime = SystemClock.currentThreadTimeMillis();
				long prevTime = SystemClock.currentThreadTimeMillis();
				float v0 = velTrack.getYVelocity();
				float vy = velTrack.getYVelocity();
				float a = -999;

				@Override
				public void run() {
					if (vy == 0) {
						velTrack.recycle();
						velTrack = null;
						return;
					}

					long now = SystemClock.currentThreadTimeMillis();
					long dt = now - prevTime;
					if (dt == 0) {
						postDelayed(this, 10);
						return;
					}

					// find an acceleration that will take us to the nearest calendar....
					if (a == -999) {
						if (v0 > 0 && v0 < 2) v0 = 2;
						if (v0 < 0 && v0 > -2) v0 = -2;

						float h = (v0 > 0 ? 1 : -1) * (getHeight());
						int m = 1;
						if (Math.abs(v0) > 10) m = 12;
						else if (Math.abs(v0) > 5)  m = 3;
						a = v0 * v0 / (0 - scrollY + h * m) / -2;
					}

					vy = v0 + a * (now-startTime);
					if (v0 > 0 ? vy > 0 : vy < 0) {
						float x1 = v0*(now-startTime)      + a / 2 * (now-startTime) * (now-startTime);
						float x2 = v0*(prevTime-startTime) + a / 2 * (prevTime-startTime) * (prevTime-startTime);
						scrollBy(x1 - x2);
						prevTime = now;
						postDelayed(this, 10);
					} else {
						scrollTo(scrollY > 0 ? getHeight() : -1*getHeight());
						if (velTrack != null) {
							velTrack.recycle();
							velTrack = null;
						}
					}

				}
			});
			return false;
		}

		if (mDragging != null) {
			if (dist(mDragging, e) < 100) {
				return true;
			}
			if (velTrack != null)
				velTrack.addMovement(e);
			scrollTo(startScroll + e.getY() - mDragging.getY());
			return true;
		}

		return false;
	}

	private void scrollBy(float dy) {
		scrollTo(scrollY + dy);
	}

	private void scrollTo(float y) {
		scrollY = y;

		if (Math.abs(scrollY) >= getHeight()) {
			current = current.getNext(scrollY);
			// if we're scrolling, aggressively try to generate the next calendar we'll show...
			current.getNext(scrollY).getCache();

			// y may be based on startScroll units. Adjust startScroll to account for the new offset
			// TODO: scrollTo really shouldn't need to know about startScroll
			startScroll = (float) (startScroll + (scrollY > 0 ? -1 : 1) * getHeight());
			scrollY = scrollY > 0 ? scrollY - getHeight() : scrollY + getHeight();
		} else {
			invalidate();
		}
	}

	private int findDateFromPoint(MotionEvent e) {
		DateLayoutParams params = getParams();

		int x = (int) Math.floor( (e.getX() - getPaddingLeft()) / params.w2) + 1;
		int y = (int) Math.floor((e.getY() - getPaddingTop() - params.header) / params.getRowHeight(current.month)) + 1;

		GregorianCalendar date = (GregorianCalendar) current.month.clone();
		date.set(GregorianCalendar.DAY_OF_WEEK, x);
		date.set(GregorianCalendar.WEEK_OF_MONTH, y);
		return date.get(GregorianCalendar.DAY_OF_MONTH);
	}

	private float dist(MotionEvent e1, MotionEvent e2) {
		if (e1 == null || e2 == null)
			return 0;
		float dx = e1.getX() - e2.getX();
		float dy = e1.getY() - e2.getY();
		return (float) Math.sqrt(dx*dx + dy*dy);
	}

	private void getPositionForDate(Calendar month, float[] pt) {
		pt[0] = month.get(GregorianCalendar.DAY_OF_WEEK) - 1;
		pt[1] = month.get(GregorianCalendar.WEEK_OF_MONTH) - 1;
	}

	private Cal current;
	private GregorianCalendar mToday;
	public void onDraw(Canvas canvas) {
		int save = canvas.save();
		canvas.translate(0,  scrollY);
		current.draw(canvas);
		if (scrollY != 0) {
			canvas.translate(0, (scrollY > 0 ? -1 : 1) * getHeight());
			current.getNext(scrollY).draw(canvas);
		}
		canvas.restoreToCount(save);
	}

	private float scrollY = 0;
	private static final float HEADER_MARGIN = 20;
	
	private class Cal {
		Calendar month;
		Bitmap cache;
		private int selectedDate = -11;

		public Cal(Calendar month) {
			this.month = month;
		}

		int nextDir = 0;
		Cal next;
		private boolean generating;
		public Cal getNext(float dir) {
			int dir2 = (dir > 0 ? -1 : 1);
			if (next == null || nextDir != dir2) {
				Calendar cal = new GregorianCalendar(getYear(), getMonth(), 1);
				cal.add(GregorianCalendar.MONTH, dir2);

				nextDir = dir2;
				next = new Cal(cal);
			}
			return next;
		}

		private int _month = -1;
		protected int getMonth() {
			if (_month == -1) {
				_month = month.get(GregorianCalendar.MONTH);				
			}
			return _month;
		}

		private int _year = -1;
		protected int getYear() {
			if (_year == -1) {
				_year = month.get(GregorianCalendar.YEAR);
			}
			return _year;
		}

		public void draw(Canvas canvas) {
			if (cache == null || generating) {
				if (!generating)
					getCache();
				return;
			}
			int save = canvas.save();

			DateLayoutParams params = getParams();

			selectorPaint.setTextSize(params.w / 14);
			canvas.translate(getPaddingLeft(), getPaddingTop());

			drawSelection(canvas, selectorPaint, params.w, params.h);
			canvas.drawBitmap(cache, 0, 0, calendarPaint);

			canvas.restoreToCount(save);
		}

		private void getCache() {
			generating = true;
			AsyncTask<Void, Void, Void> cacheTask = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					DateLayoutParams params1 = getParams();
					
					cache = Bitmap.createBitmap((int)params1.w, (int)params1.h, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(cache);

					int save = canvas.save();
					DateLayoutParams params11 = getParams();
		
					calendarPaint.setColor(0xFFAAAAAA);
					calendarPaint.setStyle(Paint.Style.STROKE);
					calendarPaint.setTextSize(params11.w / 14);

					DateLayoutParams params111 = getParams();
					params111.header = 0;
					drawHeader(canvas, calendarPaint);
					params111.header += params111.w/14;

					canvas.translate(0, calendarPaint.getTextSize() + HEADER_MARGIN);
					calendarPaint.setTextSize(params111.w / 18);
					drawWeekHeader(canvas, calendarPaint, params111.w);
					params111.header += params111.w/18;
		
					canvas.translate(0, calendarPaint.getTextSize() + HEADER_MARGIN*4);
					calendarPaint.setTextSize(params111.w / 14);
					drawCalendar(canvas, calendarPaint);
					
					canvas.restoreToCount(save);
					generating = false;
					return null;
				}
				@Override
				protected void onPostExecute(Void result) {
					invalidate();
				}
			};
			cacheTask.execute();
		}

		private void drawHeader(Canvas c, Paint p) {
			int save = c.save();
			DateLayoutParams params = getParams();
			String txt = month.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.SHORT, Locale.ENGLISH);
			txt += " " + month.get(GregorianCalendar.YEAR);
			float s = p.measureText(txt);
			c.drawText(txt, params.w/2 - s/2, p.getTextSize(), p);
			c.restoreToCount(save);
		}

		private void drawWeekHeader(Canvas c, Paint p, float w2) {
			int save = c.save();
			float w = w2/7;
			int i2 = month.getFirstDayOfWeek();
			c.translate(w/4, 0);

			for (int i = 0; i < 7; i++) {
				month.set(GregorianCalendar.DAY_OF_WEEK, i2);
				String txt = month.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SHORT, Locale.ENGLISH);
				c.drawText(txt, 0, p.getTextSize(), p);
				c.translate(w, 0);
				i2++;
				i2 = i2%7;
			}
			c.restoreToCount(save);
		}

		private void drawSelection(Canvas c, Paint p, float w2, float h2) {
			if (selectedDate < 0)
				return;

			DateLayoutParams params = getParams();
			month.set(GregorianCalendar.DATE, selectedDate);

			getPositionForDate(month, pt);

			int save2 = c.save();
			c.translate(pt[0]*params.w2, params.header + (pt[1]+1)*params.getRowHeight(month));

			p.setColor(0x33AAAAFF);
			p.setStyle(Paint.Style.FILL);

			c.drawCircle(params.w2/2, 0, p.getTextSize()/1.25f, p);

			p.setColor(0xFFAAAAAA);
			c.restoreToCount(save2);
		}

		float[] pt = new float[2];
		private void drawCalendar(Canvas c, Paint p) {
			String txt = "";
			// draw in the "center" of each box
			DateLayoutParams params = getParams();
			c.translate(params.w2/2, 0);

			int date = 1;
			while (date < month.getActualMaximum(GregorianCalendar.DATE) + 1) {
				month.set(GregorianCalendar.DATE, date);
				if (isToday(month)) p.setColor(0xFFAAAAFF);
				else p.setColor(0xFFAAAAAA);

				getPositionForDate(month, pt);
				int save2 = c.save();

				c.translate(pt[0]*params.w2, pt[1]*params.getRowHeight(month));
				txt = Integer.toString(date);
				float s = p.measureText(txt);
				c.drawText(txt, -s/2, 0, p);

				c.restoreToCount(save2);
				date++;
			}			
		}
	}

	private GregorianCalendar getToday() {
		if (mToday != null)
			return mToday;
		mToday = new GregorianCalendar();
		mToday.set(GregorianCalendar.HOUR, 0);
		mToday.set(GregorianCalendar.MINUTE, 0);
		mToday.set(GregorianCalendar.SECOND, 0);
		mToday.set(GregorianCalendar.MILLISECOND, 0);
		return mToday;
	}

	public boolean isToday(Calendar month) {
		long dt = month.getTimeInMillis() - getToday().getTimeInMillis();
		return (dt > 0 && dt < 24*60*60*1000);
	}

	public Calendar getDate() {
		current.month.set(GregorianCalendar.DATE, current.selectedDate);
		return current.month;
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

	public DateChangeListener mListener;
	public void setListener(DateChangeListener listener) {
		mListener = listener;
	}
	private void notifyListeners() {
		if (mListener == null)
			return;
		mListener.onChange(getDate());
	}
}
