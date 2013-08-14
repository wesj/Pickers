package com.digdug.colorpicker;

import com.digdug.colorpicker.NumberPicker.NumberChangeListener;

import android.content.Context;

public class NumberPickerDialog extends DialogBase implements NumberChangeListener {

	private NumberPicker np;

	public NumberPickerDialog(Context context) {
		super(context);
	}

	protected void init(Context context) {
		super.init(context);
		np = new NumberPicker(context);
		np.setListener(this);
		onChange(0);
		linear.addView(np);
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
