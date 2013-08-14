package com.digdug.colorpicker;

import java.util.GregorianCalendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final TextView text = (TextView)findViewById(R.id.time);
		final String ok = getResources().getString(android.R.string.ok);

		Button button = (Button)findViewById(R.id.timePickerButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final TimePickerDialog dialog = new TimePickerDialog(MainActivity.this);
				dialog.setButton(Dialog.BUTTON_POSITIVE, ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						CharSequence format = DateFormat.format("hh:mm aa", dialog.getTime());
						text.setText(format);
					}
				});
				dialog.setTime(new GregorianCalendar());
				dialog.show();
			}
		});

		button = (Button)findViewById(R.id.colorButton1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ColorPickerDialog dialog = new ColorPickerDialog(MainActivity.this);
				dialog.setButton(Dialog.BUTTON_POSITIVE, ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						int color = dialog.getColor();
						text.setText("Color: " + Color.red(color) + "," + Color.green(color) + "," + Color.blue(color));
						text.setTextColor(color);
					}
				});
				dialog.show();
			}
		});

		button = (Button)findViewById(R.id.colorButton2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ColorPickerDialog dialog = new ColorPickerDialog(MainActivity.this);
				dialog.setMode(ColorPickerDialog.Mode.PALETTE);
				dialog.setButton(Dialog.BUTTON_POSITIVE, ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						int color = dialog.getColor();
						text.setText(dialog.getColorString(color));
						text.setTextColor(color);
					}
				});
				dialog.show();
			}
		});

		button = (Button)findViewById(R.id.numberButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final NumberPickerDialog dialog = new NumberPickerDialog(MainActivity.this);
				dialog.setRange(0, 100, 10);
				dialog.setButton(Dialog.BUTTON_POSITIVE, ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						text.setText(Integer.toString(dialog.getValue()));
					}
				});
				dialog.show();
			}
		});

		button = (Button)findViewById(R.id.datePicker);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final DatePickerDialog dialog = new DatePickerDialog(MainActivity.this);
				dialog.setButton(Dialog.BUTTON_POSITIVE, ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						text.setText(dialog.buildString(dialog.getDate()));
					}
				});
				dialog.show();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
