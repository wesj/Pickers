package com.digdug.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;
import android.widget.TextView;

import com.digdug.colorpicker.ColorPicker.ColorListener;

public class ColorPickerDialog extends DialogBase implements ColorListener {
	private ColorPicker v;
    private ShapeDrawable sd;

	public static enum Mode {
		TRIANGLE,
		PALETTE,
        TAB
	}

	protected ColorPickerDialog(Context context) {
		super(context);
	}

	protected void init(final Context context) {
		super.init(context);

        if (tv != null)
		    tv.setShadowLayer(2, 0, 0, Color.BLACK);

		setMode(Mode.TRIANGLE);
        setCustomTitle(null);
		onChange(Color.RED);
	}
	
	public void setMode(Mode mode) {
		switch (mode) {
			case TRIANGLE:
				v = new TriangleColorPicker(getContext()); 
				break;
			case PALETTE:
				v = new PaletteColorPicker(getContext());
				break;
            case TAB:
                v = new MultiColorPicker(getContext());
                break;
		}
		v.setColorChangeListener(this);
		setView((View) v);
	}

    @Override
    public void setCustomTitle(View v) {
        if (v != null) {
            super.setCustomTitle(v);
            tv = null;
            return;
        }

        tv = new TextView(getContext());
        tv.setTextSize(20);
        tv.setPadding(20, 10, 20, 10);
        tv.setTypeface(Typeface.DEFAULT);
        tv.setTextColor(textColor);

        sd = new ShapeDrawable(new RectShape());
        sd.setBounds(0, 0, 40, 40);
        sd.setIntrinsicHeight(40);
        sd.setIntrinsicWidth(40);
        tv.setCompoundDrawables(null, null, sd, null);

        super.setCustomTitle(tv);
    }

	public int getColor() {
		return v.getColor();
	}

    @Override
	public void onChange(int color) {
        setTitle(getColorString(color));
        if (tv != null) {
		    //tv.setTextColor(color);
            if (sd != null) {
                Paint p = sd.getPaint();
                p.setColor(color);
            }
        }
	}

	public CharSequence getColorString(int color) {
		//if (getOrientation() == Orientation.HORIZONTAL)
		//	return "R: " + Color.red(color) + "\nG: " + Color.green(color) + "\nB: " + Color.blue(color);
		return Color.red(color) + ", " + Color.green(color) + ", " + Color.blue(color);
	}
}
