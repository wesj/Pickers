package com.digdug.colorpicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class DatePickerDialog extends AlertDialog implements DatePicker.DateChangeListener {

	private DatePicker dp;
	private int textColor;
	private LinearLayout linear;
	private TextView tv;
	private View separator;

	protected DatePickerDialog(Context context) {
		super(context);
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
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setTextColor(textColor);
		tv.setPadding(10, 10, 10, 10);
		tv.setTypeface(Typeface.DEFAULT);
		linear.addView(tv);
	
		separator = new ImageView(context);
		separator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		separator.setPadding(0, 5, 0, 0);
		LayoutParams lp = new LayoutParams();
		lp.setMargins(0, 5, 0, 5);
		separator.setLayoutParams(lp);
		separator.setBackgroundColor(textColor);
		linear.addView(separator);

		dp = new DatePicker(context);
		dp.setListener(this);
		dp.setPadding(50, 50, 50, 50);
		onChange(new GregorianCalendar());
		linear.addView(dp);
		setView(linear);
	}

	public Calendar getDate() {
		return dp.getDate();
	}

	public void onChange(Calendar cal) {
		tv.setText(buildString(cal));
	}

	public static CharSequence buildString(Calendar cal) {
		return DateFormat.format("MMM dd, yyyy", cal);
	}
}
