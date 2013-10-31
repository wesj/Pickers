package com.digdug.colorpicker;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by wesj on 9/15/13.
 */
public class AnimationRunnable implements Runnable {
    protected long start = 0;
    protected float duration = 250.0f;
    protected float mStartSize = 0.0f;
    protected float mEndSize = 1.0f;
    protected float exit = 0.0f;
    public boolean preventDraws = false;
    public View mView;
    public boolean animating;
    public float time;

    public AnimationRunnable(View v, float exit) {
        mView = v;
        this.exit = exit;
    }

    private TimeInterpolator interpolator = new AccelerateInterpolator(); //2.0f);
    public void setInterpolator(TimeInterpolator interp) {
        interpolator = interp;
    }

    protected boolean updateTimer(float t) {
        time = Math.max(mEndSize, Math.min(mStartSize, t));
        if (mEndSize > mStartSize ? time >= mEndSize : time <= mEndSize) {
            animating = false;
            return true;
        }
        return false;
    }

    protected float getStart(float end) {
        if (time != end && animating) {
            return time;
        }
        return end == 1.0f ? exit : 1.0f;
    }

    @SuppressLint("NewApi")
    @Override
    public void run() {
        if (start == 0) {
            start = SystemClock.elapsedRealtime();
        }
        animating = true;
        float t = interpolator.getInterpolation((SystemClock.elapsedRealtime() - start) / duration);
        boolean done = updateTimer(mStartSize + (mEndSize - mStartSize) * t);
        preventDraws = false;
        mView.invalidate();

        if (done) {
            start = 0;
        } else {
            mView.postDelayed(this, 0);
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
