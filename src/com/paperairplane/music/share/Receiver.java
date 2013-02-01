package com.paperairplane.music.share;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class Receiver extends BroadcastReceiver {
	private static final String TAG = "MyReceiver";
	private Toast toast = null;

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			// System.out.println("Scanning");
			toast = Toast.makeText(context, R.string.refresh_on_process,
					Toast.LENGTH_SHORT);
			toast.show();
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			toast = Toast.makeText(context, R.string.refresh_success,
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			Bundle bundle = intent.getExtras();
			Log.d(TAG, "onReceive - " + intent.getAction() + ", extras: "
					+ printBundle(bundle));

			if (JPushInterface.ACTION_REGISTRATION_ID
					.equals(intent.getAction())) {

			} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
					.getAction())) {
				Log.d(TAG,
						"接受到推送下来的自定义消息: "
								+ bundle.getString(JPushInterface.EXTRA_MESSAGE));

			} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED
					.equals(intent.getAction())) {
				Log.d(TAG, "接受到推送下来的通知");

			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
					.getAction())) {
				Log.d(TAG, "用户点击打开了通知");
				String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
				String url = null;
				Uri uri;
				try {
					JSONObject jsonObject = new JSONObject(extra);
					url = jsonObject.getString("url");
					Log.d(TAG, "取到值url"+url);
				} catch (JSONException e) {
					url = null;
					e.printStackTrace();
				}
				// 打开自定义的Activity
				if (url == null) {
					uri = Uri.parse(context.getString(R.string.url));
				} else {
					uri = Uri.parse(url);
				}
				Intent update = new Intent(Intent.ACTION_VIEW, uri);
				update.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try{
				context.startActivity(update);
				}catch(Exception e){
					e.printStackTrace();
				}
			} else {
				Log.d(TAG, "Unhandled intent - " + intent.getAction());
			}
		}

	}

	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
		}
		return sb.toString();
	}

}
