package com.digdug.colorpicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.GregorianCalendar;

public class MainActivity extends Activity {

    private int mColor;
    private ColorPickerDialog mDialog;
    private TextView mText;

    private final DialogInterface.OnClickListener mColorListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            mColor = mDialog.getColor();
            mText.setText(ColorNames.roundToName(mColor));
            mText.setTextColor(mColor);
            mDialog = null;
        }
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mText = (TextView)findViewById(R.id.time);
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
						mText.setText(format);
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
                mDialog = new ColorPickerDialog(MainActivity.this);
                mDialog.setButton(Dialog.BUTTON_POSITIVE, ok, mColorListener);
                mDialog.setColor(mColor);
                mDialog.show();
			}
		});

        button = (Button)findViewById(R.id.colorButton3);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new ColorPickerDialog(MainActivity.this);
                mDialog.setMode(ColorPickerDialog.Mode.TAB);
                mDialog.setButton(Dialog.BUTTON_POSITIVE, ok, mColorListener);
                mDialog.setColor(mColor);
                mDialog.show();
            }
        });

		button = (Button)findViewById(R.id.colorButton2);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                mDialog = new ColorPickerDialog(MainActivity.this);
                mDialog.setMode(ColorPickerDialog.Mode.PALETTE);
                mDialog.setButton(Dialog.BUTTON_POSITIVE, ok, mColorListener);
                mDialog.setColor(mColor);
                mDialog.show();
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
						mText.setText(Integer.toString(dialog.getValue()));
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
						mText.setText(dialog.buildString(dialog.getDate()));
					}
				});
				dialog.show();
			}
		});

        button = (Button)findViewById(R.id.hsvButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new ColorPickerDialog(MainActivity.this);
                mDialog.setMode(ColorPickerDialog.Mode.HSV);
                mDialog.setButton(Dialog.BUTTON_POSITIVE, ok, mColorListener);
                mDialog.setColor(mColor);
                mDialog.show();
            }
        });

        button = (Button)findViewById(R.id.rgbButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new ColorPickerDialog(MainActivity.this);
                mDialog.setMode(ColorPickerDialog.Mode.RGB);
                mDialog.setButton(Dialog.BUTTON_POSITIVE, ok, mColorListener);
                mDialog.setColor(mColor);
                mDialog.show();
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
