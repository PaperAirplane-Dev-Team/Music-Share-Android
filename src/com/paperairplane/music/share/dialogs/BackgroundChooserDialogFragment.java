package com.paperairplane.music.share.dialogs;


import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class BackgroundChooserDialogFragment extends AbsDialogFragment {
	private SharedPreferences mPreferencesTheme;
	private OnBackgroundChangedListener listener;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mPreferencesTheme = getActivity().getApplicationContext().getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		final String backgroundPath = getArguments()
				.getString("backgroundPath");
		View v = View.inflate(getActivity(), R.layout.background_chooser, null);
		final ImageView iv_background = (ImageView) v
				.findViewById(R.id.imageView_background);

		if (backgroundPath != null) {
			Drawable background = Drawable.createFromPath(backgroundPath);
			BitmapDrawable bd = (BitmapDrawable) background;
			Bitmap bm = bd.getBitmap();
			iv_background.setImageBitmap(bm);
		}

		DialogInterface.OnClickListener listenerBackground = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				switch (whichButton) {
				case DialogInterface.BUTTON_POSITIVE:
					if (backgroundPath != null) {
						mPreferencesTheme
								.edit()
								.putString(Consts.Preferences.BG_PATH,
										backgroundPath).commit();
					}
					listener.onBackgroundChanged(backgroundPath);
					// TODO 未实现
					/*
					 * DialogInterface.OnClickListener listenerNotice = new
					 * DialogInterface.OnClickListener() {
					 * 
					 * @Override public void onClick(DialogInterface dialog, int
					 * whichButton) { if (whichButton ==
					 * DialogInterface.BUTTON_POSITIVE) showCustomDialog(null,
					 * Consts.Dialogs.CHANGE_COLOR); } }; new
					 * AlertDialog.Builder(getActivity())
					 * .setIcon(android.R.drawable.ic_dialog_info)
					 * .setTitle(android.R.string.dialog_alert_title)
					 * .setMessage(R.string.if_change_text_color)
					 * .setPositiveButton(android.R.string.yes, listenerNotice)
					 * .setNegativeButton(android.R.string.no,
					 * listenerNotice).show();
					 */
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(i,
							Consts.PICK_BACKGROUND_REQUEST_CODE);
					break;
				case DialogInterface.BUTTON_NEUTRAL:
					iv_background
							.setImageResource(R.drawable.background_holo_dark);
					mPreferencesTheme.edit().remove(Consts.Preferences.BG_PATH)
							.commit();
					listener.onBackgroundChanged(null);
					break;
				}

			}
		};
		Dialog dialogBackgroundChooser = new AlertDialog.Builder(getActivity())
				.setView(v).setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.menu_change_background)
				.setPositiveButton(android.R.string.ok, listenerBackground)
				.setNegativeButton(R.string.choose_picture, listenerBackground)
				.setNeutralButton(R.string.choose_default, listenerBackground)
				.create();
		return dialogBackgroundChooser;
	}
	public void setOnBackgroundChangedListener(OnBackgroundChangedListener onBackGroundChangedListener){
		this.listener = onBackGroundChangedListener;
	}
	public interface OnBackgroundChangedListener{
		public void onBackgroundChanged(String path);
	}
}
