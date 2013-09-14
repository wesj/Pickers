package com.digdug.colorpicker;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DatePickerDialog extends DialogBase implements DatePicker.DateChangeListener {

	private DatePicker dp;

	protected DatePickerDialog(Context context) {
		super(context);
	}

	protected void init(Context context) {
		super.init(context);
		dp = new DatePicker(context);
		dp.setListener(this);
		onChange(new GregorianCalendar());
		setView(dp);
	}

	public Calendar getDate() {
		return dp.getDate();
	}

	public void onChange(Calendar cal) {
        if (tv != null)
		    tv.setText(buildString(cal));
	}

	public CharSequence buildString(Calendar cal) {
		if (getOrientation() == Orientation.VERTICAL)
			return DateFormat.format("MMM dd, yyyy", cal);
		return DateFormat.format("MMM dd\nyyyy", cal);
	}
}
