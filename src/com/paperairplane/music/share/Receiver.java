package com.paperairplane.music.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {
	private Toast mToast = null;
	private Handler mHandler = null;

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			mToast = Toast.makeText(context, R.string.refresh_on_process,
					Toast.LENGTH_SHORT);
			mToast.show();
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			mHandler.sendEmptyMessage(Consts.Status.REFRESH_LIST_FINISHED);
		}
	}

	Receiver(Handler handler) {
		this.mHandler = handler;
	}
}
