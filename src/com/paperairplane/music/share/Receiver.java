package com.paperairplane.music.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

public class Receiver extends BroadcastReceiver {
	private Toast toast = null;
	private Handler handler = null;

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		boolean started=false,finished=false;
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)&&!started) {
			toast = Toast.makeText(context, R.string.refresh_on_process,
					Toast.LENGTH_SHORT);
			toast.show();
			started = true;
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)&&!finished) {
			toast = Toast.makeText(context, R.string.refresh_success,
					Toast.LENGTH_SHORT);
			toast.show();
			handler.sendEmptyMessage(Consts.Status.REFRESH_LIST_FINISHED);
			finished = true;
		}
	}
	Receiver(Handler handler){
		this.handler = handler;
	}
}
