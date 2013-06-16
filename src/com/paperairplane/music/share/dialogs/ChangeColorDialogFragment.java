package com.paperairplane.music.share.dialogs;

import java.util.Locale;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.utils.MyLogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ChangeColorDialogFragment extends AbsDialogFragment{
	
	private OnColorChangedListener listener;
	private SharedPreferences mPreferencesTheme ;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		mPreferencesTheme = getActivity().getApplicationContext().getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		View changeColor = View.inflate(getActivity(), R.layout.color_chooser,
				null);
		final SeekBar seekColor[] = new SeekBar[3];
		final TextView textColor[] = new TextView[3];
		final TextView textColorCode = (TextView) changeColor
				.findViewById(R.id.text_color);
		final TextView textShowColor = (TextView) changeColor
				.findViewById(R.id.text_show_color);
		seekColor[Consts.Color.RED] = (SeekBar) changeColor
				.findViewById(R.id.seek_red);
		seekColor[Consts.Color.GREEN] = (SeekBar) changeColor
				.findViewById(R.id.seek_green);
		seekColor[Consts.Color.BLUE] = (SeekBar) changeColor
				.findViewById(R.id.seek_blue);
		textColor[Consts.Color.RED] = (TextView) changeColor
				.findViewById(R.id.text_red);
		textColor[Consts.Color.GREEN] = (TextView) changeColor
				.findViewById(R.id.text_green);
		textColor[Consts.Color.BLUE] = (TextView) changeColor
				.findViewById(R.id.text_blue);
		// 实际测试,不透明度是必须的
		// 我昨天也发现了……

		OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				switch (seekBar.getId()) {
				case R.id.seek_red:
					textColor[Consts.Color.RED]
							.setText(getString(R.string.red) + ":"
									+ progress);
					break;
				case R.id.seek_green:
					textColor[Consts.Color.GREEN]
							.setText(getString(R.string.green) + ":"
									+ progress);
					break;
				case R.id.seek_blue:
					textColor[Consts.Color.BLUE]
							.setText(getString(R.string.blue) + ":"
									+ progress);
					break;
				}
				changeColor();

			}

			private void changeColor() {
				String color[] = new String[4];
				color[Consts.Color.RED] = Integer
						.toHexString(seekColor[Consts.Color.RED]
								.getProgress());
				color[Consts.Color.GREEN] = Integer
						.toHexString(seekColor[Consts.Color.GREEN]
								.getProgress());
				color[Consts.Color.BLUE] = Integer
						.toHexString(seekColor[Consts.Color.BLUE]
								.getProgress());
				color[Consts.Color.OPACITY] = "FF";
				for (int i = 0; i < 4; i++) {
					if (color[i].length() == 1)
						color[i] = "0" + color[i];
				}
				String hexColor = ("#" + color[Consts.Color.OPACITY]
						+ color[Consts.Color.RED]
						+ color[Consts.Color.GREEN] + color[Consts.Color.BLUE])
						.toUpperCase(Locale.getDefault());
				// MyLogger.d(Consts.DEBUG_TAG, "Color: "+hexColor);
				textColorCode.setText(hexColor);
				textShowColor.setBackgroundColor(android.graphics.Color
						.parseColor(hexColor));
			}
		};
		for (int i = 0; i < 3; i++) {
			seekColor[i].setOnSeekBarChangeListener(seekListener);
		}
		String nowColor;
		if (mPreferencesTheme.contains(Consts.Preferences.TEXT_COLOR)) {
			nowColor = mPreferencesTheme.getString(
					Consts.Preferences.TEXT_COLOR, "");
		} else {
			nowColor = Consts.ORIGIN_COLOR;
		}
		int colorInt[] = new int[3];
		colorInt[Consts.Color.RED] = Integer.valueOf(
				nowColor.substring(3, 5), 16);
		colorInt[Consts.Color.GREEN] = Integer.valueOf(
				nowColor.substring(5, 7), 16);
		colorInt[Consts.Color.BLUE] = Integer.valueOf(
				nowColor.substring(7, 9), 16);
		MyLogger.i(Consts.DEBUG_TAG, "Integers are: " + colorInt[0] + " "
				+ colorInt[1] + " " + colorInt[2]);
		for (int i = 0; i < 3; i++) {
			seekColor[i].setProgress(colorInt[i]);
		}

		DialogInterface.OnClickListener listenerColor = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				switch (whichButton) {
				case DialogInterface.BUTTON_POSITIVE:
				
					String color = textColorCode.getText().toString();
					if (color.contains("#")) {
						mPreferencesTheme
								.edit()
								.putString(Consts.Preferences.TEXT_COLOR,
										color).commit();
						listener.onColorChanged();
						MyLogger.d(Consts.DEBUG_TAG, "自定义颜色:" + color);
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					getDialog().cancel();
					break;
				case DialogInterface.BUTTON_NEUTRAL:
					if (mPreferencesTheme
							.contains(Consts.Preferences.TEXT_COLOR))
						mPreferencesTheme.edit()
								.remove(Consts.Preferences.TEXT_COLOR)
								.commit();
					listener.onColorChanged();
					break;
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setView(changeColor)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.change_text_color)
				.setPositiveButton(android.R.string.ok, listenerColor)
				.setNegativeButton(android.R.string.cancel, listenerColor)
				.setNeutralButton(R.string.reset, listenerColor);
		return builder.create();
	}
	public void setOnColorChangedListener(OnColorChangedListener listener){
		this.listener = listener;
	}
	public interface OnColorChangedListener{
		public void onColorChanged();
	}
}
