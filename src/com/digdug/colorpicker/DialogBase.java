package com.digdug.colorpicker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class DialogBase extends AlertDialog {
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

	protected Orientation getOrientation() {
		Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		return size.x > size.y ? Orientation.HORIZONTAL : Orientation.VERTICAL;		
	}

	protected void init(Context context) {
		Orientation orient = getOrientation();
		textColor = context.getResources().getColor(android.R.color.holo_blue_dark);
	}

    @Override
    public void setTitle(CharSequence title) {
        if (tv != null) {
            tv.setText(title);
            return;
        }
        super.setTitle(title);
    }

}
