package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class ThankDialogFragment extends AbsDialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
//		.setOnCancelListener(onCancelListener)
		.setTitle(R.string.thank_title)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setMessage(R.string.thank_content)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface dialog,
							int whichButton) {
						//TODO 恢复窗口
					}
				}).create();
		return dialog;
	}
}
