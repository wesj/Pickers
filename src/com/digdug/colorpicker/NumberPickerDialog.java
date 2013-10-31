package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.digdug.colorpicker.NumberPicker.NumberChangeListener;

public class NumberPickerDialog extends DialogBase implements NumberChangeListener {

	private NumberPicker np;

	public NumberPickerDialog(Context context) {
		super(context);
	}

    @Override
    public void setCustomTitle(View v) {
        if (v != null) {
            tv = null;
            super.setCustomTitle(v);
            return;
        }

        tv = new TextView(getContext(), null, android.R.style.TextAppearance_DialogWindowTitle);
        int s = (int) tv.getTextSize();
        tv.setTextSize(s);
        tv.setPadding(s, s / 2, s, s / 2);
        tv.setTypeface(Typeface.DEFAULT);
        tv.setTextColor(textColor);

        super.setCustomTitle(tv);
    }

	protected void init(Context context) {
		super.init(context);
		np = new NumberPicker(context);
		np.setListener(this);
        setView(np);

        setCustomTitle(null);
		onChange(0);
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
