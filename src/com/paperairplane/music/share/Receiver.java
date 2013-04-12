package com.paperairplane.music.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {
	private Toast toast = null;
	private Handler handler = null;

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			Log.d(Consts.DEBUG_TAG, "Receiver接收到ACTION_MEDIA_SCANNER_STARTED");
			toast = Toast.makeText(context, R.string.refresh_on_process,
					Toast.LENGTH_SHORT);
			toast.show();
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			Log.d(Consts.DEBUG_TAG, "Receiver接收到ACTION_MEDIA_SCANNER_FINISHED");
			handler.sendEmptyMessage(Consts.Status.REFRESH_LIST_FINISHED);
		}
	}

	Receiver(Handler handler) {
		this.handler = handler;
	}
}
