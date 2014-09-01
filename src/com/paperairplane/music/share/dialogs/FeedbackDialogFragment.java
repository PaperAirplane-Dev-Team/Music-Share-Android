package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.FeedbackMessage;
import com.paperairplane.music.share.Main;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.utils.HttpQuestHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

public class FeedbackDialogFragment extends AbsDialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View feedback = LayoutInflater.from(getActivity()).inflate(
				R.layout.feedback, null);
		final EditText etContent = (EditText) feedback
				.findViewById(R.id.et_feedback);
		final EditText etName = (EditText) feedback.findViewById(R.id.et_name);
		final EditText etEmail = (EditText) feedback
				.findViewById(R.id.et_email);
		TextWatcher twEmail = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String address = s.toString();
				if (!address
						.matches("(?:\\w+)@(?:\\w+)(?:(\\.[a-zA-z]{2,4})+)$")) {
					etEmail.setTextColor(Color.RED);
				} else {
					int color = (Build.VERSION.SDK_INT > 10) ? android.R.color.primary_text_dark
							: android.R.color.primary_text_light;
					etEmail.setTextColor(getResources().getColor(color));
				}
			}
		};
		etEmail.addTextChangedListener(twEmail);
		final ImageView[] ivClearButtons = new ImageView[3];

		ivClearButtons[0] = (ImageView) feedback
				.findViewById(R.id.btn_clear_content);
		ivClearButtons[1] = (ImageView) feedback
				.findViewById(R.id.btn_clear_name);
		ivClearButtons[2] = (ImageView) feedback
				.findViewById(R.id.btn_clear_email);

		OnClickListener listenerClear = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				switch (id) {
				case R.id.btn_clear_content:
					etContent.setText("");
					break;
				case R.id.btn_clear_name:
					etName.setText("");
					break;
				case R.id.btn_clear_email:
					etEmail.setText("");
					break;
				}

			}
		};
		for (ImageView iv : ivClearButtons) {
			iv.setOnClickListener(listenerClear);
		}

		SharedPreferences pref = getActivity().getApplicationContext()
				.getSharedPreferences(Consts.Preferences.FEEDBACK,
						FragmentActivity.MODE_PRIVATE);
		String content = pref.getString("content", "");
		etContent.setText(content);
		String name = pref.getString("name", "");
		etName.setText(name);
		String email = pref.getString("email", "");
		etEmail.setText(email);
		pref.edit().clear().commit();
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				String strContent = etContent.getText().toString().trim();
				String strName = etName.getText().toString().trim();
				String strEmail = etEmail.getText().toString().trim();
				if (strContent.equals("") || strEmail.equals("")) {
					new EmptyDialogFragment().show(getFragmentManager(),
							"emptyDialog");
				} else {
					String[] contents = new String[3];
					contents[0] = strContent;
					contents[1] = strName;
					contents[2] = strEmail;
					FeedbackMessage feedback = new FeedbackMessage(contents,
							Main.sVersionCode, getActivity());
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						feedback.setMeans(Consts.ShareMeans.OTHERS);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						feedback.setMeans(Consts.ShareMeans.WEIBO);
						break;
					}

					Message m = HttpQuestHandler.getInstance().obtainMessage(
							Consts.NetAccessIntent.SEND_FEEDBACK);
					m.obj = feedback;
					m.sendToTarget();
				}
			}
		};

		builder.setView(feedback)
				.setPositiveButton(R.string.send_feedback, listener)
				.setTitle(R.string.thank_for_feedback)
				.setIcon(android.R.drawable.ic_dialog_info);
		if (getArguments() != null && getArguments().getBoolean("tokenValid")) {
			builder.setNegativeButton(R.string.feedback_weibo, listener);
		}
		return builder.create();
	}

}
