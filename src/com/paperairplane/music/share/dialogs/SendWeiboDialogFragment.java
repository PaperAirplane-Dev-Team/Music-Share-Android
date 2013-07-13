package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.AtSuggestionActivity;
import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.utils.MyLogger;
import com.paperairplane.music.share.utils.ShakeDetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

public class SendWeiboDialogFragment extends AbsDialogFragment {

	private OnShareToWeiboListener onShareListener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View sendweibo = LayoutInflater.from(getActivity()).inflate(
				R.layout.sendweibo, null);
		final EditText et = (EditText) sendweibo.getRootView().findViewById(
				R.id.et_content);
		final CheckBox cb = (CheckBox) sendweibo.findViewById(R.id.cb_follow);
		final ImageView iv_clear = (ImageView) sendweibo
				.findViewById(R.id.clear_button);
		iv_clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				et.setText("");
			}
		});
		final Bundle bundle = getArguments();
		String _content = bundle.getString(Intent.EXTRA_TEXT);
		final String artworkUrl = bundle.getString("artworkUrl");
		final String fileName = bundle.getString("fileName");
		final String annotation = bundle.getString("annotation");
		int selection = bundle.getInt("selection", _content.length());
		// MyLogger.v(Consts.DEBUG_TAG, artworkUrl);
		cb.setChecked(bundle.getBoolean("isChecked", true));
		et.setText(_content);
		et.setSelection(selection);
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try {
					if (s.toString().charAt(start) == '@') {
						MyLogger.i(Consts.DEBUG_TAG, "@ CAUGHT!"); // @提醒
						// 我有错，我悔过
						Intent i = new Intent(getActivity(),
								AtSuggestionActivity.class);
						bundle.putString(Intent.EXTRA_TEXT, s.toString());
						bundle.putBoolean("isChecked", cb.isChecked());
						bundle.putInt("start", start);
						i.putExtras(bundle);
						startActivityForResult(i,
								Consts.LOOK_FOR_SUGGESTION_REQUEST_CODE);
					}
				} catch (Exception e) {

				}
			}
		});

		Dialog dialogSendWeibo = new AlertDialog.Builder(getActivity())
				.setView(sendweibo)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						if (ShakeDetector.sCanDetact)
							ShakeDetector.getInstance(getActivity()).start();
					}
				})
				.setPositiveButton(getString(R.string.share),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String content = et.getText().toString();
								onShareListener.onShareToWeibo(content,
										artworkUrl, fileName,annotation, cb.isChecked());
							}

						}).create();
		return dialogSendWeibo;
	}

	public interface OnShareToWeiboListener {
		public void onShareToWeibo(String content, String artworkUrl,
				String fileName, String annotation, boolean willFollow);
	}

	public void setOnShareToWeiboListener(
			SendWeiboDialogFragment.OnShareToWeiboListener listener) {
		this.onShareListener = listener;
	}
}
