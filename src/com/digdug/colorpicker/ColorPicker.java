package com.digdug.colorpicker;

public interface ColorPicker {
	public interface ColorListener {
		public void onChange(int color);
	}

	public int getColor();
    public void setColor(int color);

	public void setColorChangeListener(ColorListener listener);
}
