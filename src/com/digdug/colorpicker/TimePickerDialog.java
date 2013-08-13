package com.digdug.colorpicker;

import java.util.GregorianCalendar;

import com.digdug.colorpicker.TimePicker.Mode;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class TimePickerDialog extends AlertDialog implements TimePicker.TimeChangeListener {
	private TimePicker tp;
	private LinearLayout linear;
	private TextView tv;
	private int textColor;
	
	public TimePickerDialog(Context context) {
		super(context);
		init(context);
	}

	public TimePickerDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public TimePickerDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	private void init(Context context) {
		textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
		linear = new LinearLayout(context);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		linear.setGravity(Gravity.CENTER);
	
		tv = new TextView(context);
		tv.setTextSize(40);
		tv.setLinksClickable(true);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setTextColor(textColor);
		tv.setHighlightColor(Color.TRANSPARENT);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setPadding(10, 10, 10, 10);
		tv.setTypeface(Typeface.DEFAULT);
		linear.addView(tv);
	
		ImageView separator = new ImageView(context);
		separator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		separator.setPadding(0, 5, 0, 0);
		LayoutParams lp = new LayoutParams();
		lp.setMargins(0, 5, 0, 5);
		separator.setLayoutParams(lp);
		separator.setBackgroundColor(textColor);
		linear.addView(separator);

		tp = new TimePicker(context);
		tp.setPadding(30, 0, 30, 0);
		tp.setIndicatorColor(textColor);
		linear.addView(tp);
	
		setView(linear);
	
		tp.setTimeChangeListener(this);
		onTimeChange(12,0);
	}

	public void setTime(int hours, int minutes) {
		tp.setTime(hours, minutes);
	}
	
	public GregorianCalendar getTime() {
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

	public void onClick(DialogInterface dialog, int which) {
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	public Bundle onSaveInstanceState() {
		return null;
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
	}

	public void updateTime(int hourOfDay, int minutOfHour) {
	}
}
