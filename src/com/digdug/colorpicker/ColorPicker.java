package com.digdug.colorpicker;

public interface ColorPicker {
	public interface ColorListener {
		public void onChange(int color);
	}

	public int getColor();

	public void setColorChangeListener(ColorListener listener);
}
