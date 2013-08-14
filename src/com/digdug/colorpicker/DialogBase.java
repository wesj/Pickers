package com.digdug.colorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DialogBase extends AlertDialog {
	protected LinearLayout linear;
	protected TextView tv;
	protected View separator;
	protected int textColor;

	public DialogBase(Context context) {
		super(context);
		init(context);
	}

	public DialogBase(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public DialogBase(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	enum Orientation {
		HORIZONTAL,
		VERTICAL
	}

	protected void getContainer(Context context, Orientation orient) {
		linear = new LinearLayout(context);
		if (orient == Orientation.VERTICAL) {
			linear.setOrientation(LinearLayout.VERTICAL);
		} else {
			linear.setOrientation(LinearLayout.HORIZONTAL);
		}
		linear.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		linear.setGravity(Gravity.CENTER);
	}

	protected void addTitle(Context context, ViewGroup vg, Orientation orient) {
		tv = new TextView(context);
		if (orient == Orientation.VERTICAL) {
			tv.setTextSize(40);
		} else {
			tv.setTextSize(20);			
			tv.setMinWidth(200);
		}
		tv.setGravity(Gravity.CENTER);
		tv.setPadding(10, 10, 10, 10);
		tv.setTypeface(Typeface.DEFAULT);
		tv.setTextColor(textColor);
		vg.addView(tv);		
	}
	
	protected void addSeparator(Context context, ViewGroup vg, Orientation orient) {
		separator = new View(context);
		LayoutParams lp;
		if (orient == Orientation.VERTICAL) {
			lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.height = 5;
			separator.setLayoutParams(lp);
			separator.setPadding(5, 0, 0, 0);
			lp.setMargins(5, 0, 5, 0);
		} else {
			lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lp.width = 2;
			separator.setLayoutParams(lp);
			separator.setPadding(0, 5, 0, 0);
			lp.setMargins(0, 5, 0, 5);
		}
		separator.setLayoutParams(lp);
		separator.setBackgroundColor(textColor);
		vg.addView(separator);
	}

	protected Orientation getOrientation() {
		Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		return size.x > size.y ? Orientation.HORIZONTAL : Orientation.VERTICAL;		
	}

	protected void init(Context context) {
		Orientation orient = getOrientation();
		textColor = context.getResources().getColor(android.R.color.holo_blue_dark);

		getContainer(context, orient);
		addTitle(context, linear, orient);
		addSeparator(context, linear, orient);
		setView(linear);
	}
}
