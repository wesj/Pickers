package com.digdug.colorpicker;

import com.digdug.colorpicker.NumberPicker.NumberChangeListener;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class NumberPickerDialog extends AlertDialog implements NumberChangeListener {

	private NumberPicker np;
	private LinearLayout linear;
	private int textColor;
	private TextView tv;
	private ImageView separator;

	public NumberPickerDialog(Context context) {
		super(context);
		init(context);
	}

	public NumberPickerDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public NumberPickerDialog(Context context, boolean cancelable,
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

		np = new NumberPicker(context);
		np.setListener(this);
		onChange(0);
		linear.addView(np);
		setView(linear);
	}

	@Override
	public void onChange(int val) {
		tv.setText(Integer.toString(val));
	}

	public void setRange(int i, int j, int k) {
		np.setRange(i, j, k);
	}

	public int getValue() {
		return np.getValue();
	}
}
