package com.digdug.colorpicker;

import com.digdug.colorpicker.ColorPicker.ColorListener;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;

public class ColorPickerDialog extends AlertDialog implements ColorListener {
	private ColorPicker v;
	private int textColor;
	private LinearLayout linear;
	private TextView tv;
	private ImageView separator;
	public static enum Mode {
		TRIANGLE,
		PALETTE
	}

	protected ColorPickerDialog(Context context) {
		super(context);
		init(context);
	}

	private void init(final Context context) {
		textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
		linear = new LinearLayout(context);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		linear.setGravity(Gravity.CENTER);

		tv = new TextView(context);
		tv.setTextSize(40);
		tv.setGravity(Gravity.LEFT);
		tv.setTextColor(textColor);
		tv.setPadding(10, 10, 10, 10);
		tv.setTypeface(Typeface.DEFAULT);
		tv.setShadowLayer(2, 0, 0, Color.BLACK);
		linear.addView(tv);
	
		separator = new ImageView(context);
		separator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		separator.setPadding(0, 5, 0, 0);
		LayoutParams lp = new LayoutParams();
		lp.setMargins(0, 5, 0, 5);
		separator.setLayoutParams(lp);
		separator.setBackgroundColor(textColor);
		linear.addView(separator);

/*
		ViewPager vp = new ViewPager(context);
		vp.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		vp.setAdapter(new PagerAdapter() {
			ColorPicker[] cps = new ColorPicker[] {
				new TriangleColorPicker(context),
				new PaletteColorPicker(context)
			};

			@Override
			public int getCount() {
				return cps.length;
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView((View) cps[position]);
				v = cps[position];
				return cps[position];
			}			
		});
		linear.addView(vp);
*/
		setMode(Mode.TRIANGLE);
		setView(linear);

		// v.setColorChangeListener(this);
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

	public static CharSequence getColorString(int color) {
		return Color.red(color) + "," + Color.green(color) + "," + Color.blue(color);
	}
}
