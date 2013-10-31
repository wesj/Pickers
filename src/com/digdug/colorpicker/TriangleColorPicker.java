package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TriangleColorPicker extends ViewBase implements ColorPicker {
	private static final float WIDTH = 40;
	private Paint mOuterPaint;
	private float mHue = 0;
	private int[] mColors;
	private Paint mPaint;
	private float[] mCenter;
	private float mVal = 1.0f;
	private float mSat = 1.0f;
	HoverObj dragging = HoverObj.NOTHING;
	private int r;

	int[] hueList = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };
	
	private ColorListener mListener;
	private int fontSize = 40;

    TriangleWidget mTriangle = new TriangleWidget() {
        private Bitmap cache;
        private float[] mVerts;

        public void clearCaches() {
            super.clearCaches();
            mVerts = null;
        }

        public int[] getCoords(float cx, float cy, float r) {
            float[] verts = getVerts(cx, cy, r);
            return new int[] {
                    (int) (verts[2] - cx + (verts[4] - verts[2]) * mVal + (verts[0] - verts[4]) * mSat * mVal),
                    (int) (-1*(verts[3] - cy + (verts[5] - verts[3]) * mVal + (verts[1] - verts[5]) * mSat * mVal))
            };
        }

        public float[] getVerts(float cx, float cy, float r) {
            if (mVerts != null)
                return mVerts;

            int sides = 3;
            mVerts = new float[sides*2];
            for (int i = 0; i < sides; i++) {
                mVerts[2*i]   = (float) (cx + Math.cos(Math.PI*2*i/sides)*(r - WIDTH/2));
                mVerts[2*i+1] = (float) (cy + Math.sin(Math.PI*2*i/sides)*(r - WIDTH/2));
            }
            return mVerts;
        }

        @Override
        public void draw(Canvas canvas, float[] c, float r) {
            int save = canvas.save();
            canvas.rotate(mHue, c[0], c[1]);
            canvas.drawVertices(VertexMode.TRIANGLE_FAN, getVerts(c[0], c[1], r).length,
                    getVerts(c[0], c[1], r), 0,
                    null, 0,
                    mColors, 0,
                    null, 0, 0, mPaint);
            canvas.restoreToCount(save);
            mIndicator.draw(canvas, c ,r);
        }
    };

	public void setFontSize(int size) {
		fontSize = size;
	}

	public TriangleColorPicker(Context context) {
		super(context);
	}

	public TriangleColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TriangleColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

    protected void init(Context context) {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);		
		mPaint = new Paint();
		setHue(0);
	}

	private float[] hsv = new float[3];
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			dragging = HoverObj.NOTHING;
            mCircle.endHover();
            mTriangle.endHover();
            mIndicator.endHover();
			return false;
		}
		
		float x = event.getX();
		float y = event.getY();
		HoverObj h = getHover(x, y);

		switch (h) {
			case RING:
				setHue(getHueFromPoint(x,y));
				return true;
			case CENTER:
				int c = getColorFromPoint(x,y);
				if (Color.alpha(c) < 1.0f) {
					break;
				}

				synchronized(hsv) {
					Color.colorToHSV(c, hsv);
					setSat(hsv[1]);
					setVal(hsv[2]);
					invalidate();
				}
				return true;
		default:
			break;
		}
		return false;
	}
	
	private void setVal(float val) {
		notifyListeners(getColor());
		mVal = val;
	}

	private void setSat(float sat) {
		notifyListeners(getColor());
		mSat = sat;
	}

	private int getColorFromPoint(float x, float y) {
		final Bitmap cache = mTriangle.getCache();
		if (x > 0 && x < cache.getWidth() && y > 0 && y < cache.getHeight()) {
			int color = cache.getPixel((int)x, (int)y);
			if (Color.alpha(color) != 0)
				return color;
		}

		float [] c = getCenter();
		float r  = getRadius();

		// rotate the points
		Matrix m = new Matrix();
		m.setRotate(-1*mHue, c[0], c[1]);
		float[] pt = new float[] { x, y } ;
		m.mapPoints(pt);

		float[] verts = mTriangle.getVerts(c[0], c[1], r);
		if (pt[0] < verts[2]) {
			pt[0] = verts[2];
			pt[1] = Math.min(verts[3], Math.max(pt[1], verts[5]));
		} else if (pt[1] > verts[1])  {
			pt[1] = Math.min(verts[3], (verts[3] - verts[1])/(verts[2]-verts[0])*(pt[0] - verts[0]) + verts[1]);
		} else if (y < verts[1]) {
			pt[1] = Math.max(verts[5], (verts[5] - verts[1])/(verts[4]-verts[0])*(pt[0] - verts[0]) + verts[1]);
		}

		m.setRotate(mHue, c[0], c[1]);
		m.mapPoints(pt);
        if (pt[0] > 0 && pt[0] < cache.getWidth() && pt[1] > 0 && pt[1] < cache.getHeight())
		    return cache.getPixel((int)pt[0], (int)pt[1]);
        return 0;
	}

	private enum HoverObj {
		RING, CENTER, NOTHING
	};

	private HoverObj getHover(float x, float y) {
		if (dragging != HoverObj.NOTHING)
			return dragging;

		float[] center = getCenter();
		float dx = x - center[0];
		float dy = y - center[1];
		float r  = getRadius();

		if (Math.sqrt(dx*dx + dy*dy) > r - fontSize) {
			dragging = HoverObj.RING;
            mCircle.startHover();
            mTriangle.unHover();
            mIndicator.unHover();
		} else {
			dragging = HoverObj.CENTER;
            mCircle.unHover();
            mTriangle.startHover();
            mIndicator.startHover();
		}

		return dragging;
	}

	private float getRadius() {
		if (r > 0)
			return r;
		int w = getWidth() - getPaddingLeft() - getPaddingRight();
		int h = getHeight() - getPaddingTop() - getPaddingBottom();
		r = Math.min(w, h)/2;
		r -= (getPaddingLeft() + fontSize);
		return r;
	}

	private float[] getCenter() {
		if (mCenter != null)
			return mCenter;
		mCenter = new float[] { getWidth()/2, getHeight()/2 };
		return mCenter;
	}

	private float getHueFromPoint(float x, float y) {
		float[] center = getCenter();
		float dx = x - center[0];
		float dy = y - center[1];

		float h = (float) (Math.atan(dy/dx)*180/Math.PI);
		if (dx < 0) {
			h = 180 + h;
		} else {
			if (h < 0) h = 360 + h;
		}
		return h;
	}

	public void setHue(float f) {
		mHue = f;
		mColors = new int[] { Color.HSVToColor(new float[] { mHue, 1.0f, 1.0f }),
							  Color.HSVToColor(new float[] { mHue, 0.0f, 1.0f }),
				              Color.HSVToColor(new float[] { mHue, 1.0f, 0.0f }),
	            Color.RED,
	            Color.RED,
 	            Color.RED };
        if (mTriangle != null) {
		    mTriangle.clearCaches();
        }
		notifyListeners(getColor());
		invalidate();
	}
	
	public void onDraw(Canvas canvas) {
		float [] c = getCenter();
		float r  = getRadius();

        mCircle.draw(canvas, c, r);
        mTriangle.draw(canvas, c ,r);
	}

    abstract private class Widget {
        private Bitmap mCache;

        abstract public void draw(Canvas canvas, float[] center, float radius);

        public void startHover() { }
        public void endHover() { }
        public void unHover() { }

        public void clearCaches() {
            mCache = null;
        }

        public Bitmap getCache() {
            if (mCache != null)
                return mCache;
            mCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mCache);
            mTriangle.draw(c, getCenter(), getRadius());
            return mCache;
        }
    }

    Widget mCircle = new Widget() {
        Paint mPaint;

        private Paint getPaint(float cx, float cy) {
            if (mPaint != null)
                return mPaint;

            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(WIDTH);
            mPaint.setShader(new SweepGradient(cx, cy,
                    hueList,
                    null));
            return mPaint;
        }

        @Override
        public void draw(Canvas canvas, float[] c, float r) {
            canvas.drawCircle(c[0], c[1], r, getPaint(c[0], c[1]));
        }
    };

    Widget mIndicator = new Widget() {
        @Override
 	    public void draw(Canvas canvas, float[] c, float r) {
            int save = canvas.save();
            Paint p = new Paint();
            p.setColor(getColor());
            int[] coords = mTriangle.getCoords(c[0], c[1], r);

            canvas.translate(c[0], c[1]);
            canvas.rotate(mHue, 0, 0);
            p.setStyle(Paint.Style.FILL);
            canvas.drawCircle(coords[0], coords[1], WIDTH/2, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(mVal > 0.5 ? Color.BLACK : Color.WHITE);
            canvas.drawCircle(coords[0], coords[1], WIDTH/2, p);

            canvas.restoreToCount(save);
        }
    };

	public int getColor() {
		return Color.HSVToColor(new float[] { mHue, mSat, mVal });
	}

    abstract public class TriangleWidget extends Widget {
        abstract public int[] getCoords(float cx, float cy, float r);
        abstract public float[] getVerts(float cx, float cy, float r);
    }

	public void onSizeChange() {
		mPaint = null;
        mTriangle.clearCaches();
		mCenter = null;
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

}
