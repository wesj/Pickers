package com.digdug.colorpicker;

import com.digdug.colorpicker.ColorPicker.ColorListener;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

public class ColorPickerDialog extends DialogBase implements ColorListener {
	private ColorPicker v;

	public static enum Mode {
		TRIANGLE,
		PALETTE
	}

	protected ColorPickerDialog(Context context) {
		super(context);
	}

	protected void init(final Context context) {
		super.init(context);

		tv.setShadowLayer(2, 0, 0, Color.BLACK);

		setMode(Mode.TRIANGLE);
		setView(linear);
		onChange(Color.RED);
	}
	
	public void setMode(Mode mode) {
		linear.removeView((View) v);
		switch (mode) {
			case TRIANGLE:
				v = new TriangleColorPicker(getContext()); 
				break;
			case PALETTE:
				v = new PaletteColorPicker(getContext());
				break;
		}
		v.setColorChangeListener(this);
		linear.addView((View) v);
	}

	public int getColor() {
		return v.getColor();
	}

	@Override
	public void onChange(int color) {
		tv.setText(getColorString(color));
		tv.setTextColor(color);
		separator.setBackgroundColor(color);
	}

	public CharSequence getColorString(int color) {
		if (getOrientation() == Orientation.HORIZONTAL)
			return "R: " + Color.red(color) + "\nG: " + Color.green(color) + "\nB: " + Color.blue(color);
		return Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color);
	}
}
