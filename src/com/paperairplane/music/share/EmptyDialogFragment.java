package com.paperairplane.music.share;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class EmptyDialogFragment extends AbsDialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
		.setMessage(getString(R.string.empty))
		.setPositiveButton(getString(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}
				}).create();
	}

}
