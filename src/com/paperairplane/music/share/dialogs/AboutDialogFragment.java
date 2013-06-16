package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AboutDialogFragment extends AbsDialogFragment implements
		OnClickListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String versionName = getArguments().getString("versionName");
		int versionCode = getArguments().getInt("versionCode");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.menu_about))
				// .setOnCancelListener(onCancelListener)
				.setMessage(
						getString(R.string.about_content) + "\n\n"
								+ Consts.RELEASE_DATE + "\nVer " + versionName
								+ " / " + versionCode + "\n"
								+ getString(R.string.update_whats_new)
								+ getString(R.string.whats_new))
				.setPositiveButton(R.string.thank_list, this)
				.setNegativeButton(R.string.about_contact, this)
				.setNeutralButton(R.string.send_feedback, this);

		return builder.create();
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		// 显示感谢名单窗口
		case DialogInterface.BUTTON_POSITIVE:
			DialogFragment dialogThank = new ThankDialogFragment();
			dialogThank.show(getFragmentManager(), "thankDialog");
			break;
		// 打开网站
		case DialogInterface.BUTTON_NEGATIVE:
			Uri uri = Uri.parse(getString(R.string.url));
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		//显示反馈对话框
		case DialogInterface.BUTTON_NEUTRAL:
			FeedbackDialogFragment fdf = new FeedbackDialogFragment();
			fdf.setArguments(getArguments());
			fdf.show(getFragmentManager(), "feedbackDialog");
		}

	}

}
