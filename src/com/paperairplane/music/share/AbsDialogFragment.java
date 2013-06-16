package com.paperairplane.music.share;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

public class AbsDialogFragment extends DialogFragment {
//	TODO 待审阅
	@Override
	public void onDismiss(DialogInterface dialog) {
		if (getActivity().getClass().getName().equals("Main")) {
			try {
				if (ShakeDetector.sCanDetact)
					ShakeDetector.getInstance(getActivity()).start();
			} catch (Exception e) {
			}
		}
	}
}
