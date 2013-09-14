package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimePickerDialog extends DialogBase implements TimePicker.TimeChangeListener {
	private TimePicker tp;
	
	public TimePickerDialog(Context context) {
		super(context);
	}

    @Override
    public void setCustomTitle(View v) {
        if (v != null) {
            tv = null;
            super.setCustomTitle(v);
            return;
        }

        tv = new TextView(getContext());
        tv.setTextSize(20);
        tv.setPadding(20, 10, 20, 10);
        tv.setTypeface(Typeface.DEFAULT);
        tv.setTextColor(textColor);

        super.setCustomTitle(tv);
    }

	protected void init(Context context) {
		super.init(context);
        setCustomTitle(null);

		tv.setHighlightColor(Color.TRANSPARENT);
		tv.setLinksClickable(true);
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		tp = new TimePicker(context);
		tp.setIndicatorColor(textColor);
		setView(tp);

		tp.setTimeChangeListener(this);
		onTimeChange(12,0);
	}

	public void setTime(Calendar cal) {
		setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
	}

	public void setTime(int hours, int minutes) {
		tp.setTime(hours, minutes);
		onTimeChange(hours, minutes);
	}
	
	public Calendar getTime() {
		return tp.getTime();
	}

	public void onTimeChange(int hour, int minutes) {
		CharSequence format = DateFormat.format("hh:mm aa", new GregorianCalendar(2000, 1, 1, hour, minutes, 0));

		SpannableString b = SpannableString.valueOf(format);
		b.setSpan(new ClickableSpan() {
			@Override public void onClick(View widget) {
				tp.setMode(tp.HOURS_MODE);
				tv.invalidate();
			}
			@Override public void updateDrawState (TextPaint ds) {
				if (tp.currentMode == tp.HOURS_MODE)
					ds.setFakeBoldText(true);
				else
					ds.setFakeBoldText(false);					
				ds.setUnderlineText(false);
			}
		}, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		b.setSpan(new ClickableSpan() {
			@Override public void onClick(View widget) {
				tp.setMode(tp.MINUTES_MODE);
				tv.invalidate();
			}
			@Override public void updateDrawState (TextPaint ds) {
				if (tp.currentMode == tp.MINUTES_MODE)
					ds.setFakeBoldText(true);
				else
					ds.setFakeBoldText(false);					
				ds.setUnderlineText(false);
			}
		}, 3, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		b.setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				tp.toggleState();
				tv.invalidate();
			}
			@Override public void updateDrawState (TextPaint ds) {
				ds.setUnderlineText(false);
			}
		}, 6, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(b);
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
	}

	public void updateTime(int hourOfDay, int minutOfHour) {
	}
}
