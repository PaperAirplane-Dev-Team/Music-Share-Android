package com.paperairplane.music.share;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;

public class MusicPlayerService extends Service {
	private MediaPlayer mplayer;
	private final int PLAY = 0, PAUSE = 1, STOP = 2, PROGRESS_CHANGE = 3;
	private int currentTime;
	private Handler handler;
	private final static String DEBUG_TAG = "Music Share DEBUG";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mplayer = new MediaPlayer();
		mplayer.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				sendMaxTime();
				handler.sendEmptyMessage(1);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (handler != null) {
			handler.removeMessages(1);
			handler = null;
		}
		mplayer.release();
		mplayer = null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Bundle bundle = intent.getExtras();
		int op = bundle.getInt("op");
		init();
		switch (op) {
		case PLAY:
			if (bundle.getString("path") != null) {
				String path = bundle.getString("path");
				Log.v("Music Share SERVICE DEBUG", path);
				try {
					mplayer.reset();
					mplayer.setDataSource(path);
					mplayer.prepare();
					mplayer.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				mplayer.start();
			}
			break;
		case STOP:
			mplayer.stop();
			handler.removeMessages(1);
			break;
		case PAUSE:
			mplayer.pause();
			handler.removeMessages(1);
			break;
		case PROGRESS_CHANGE:
			int progress = intent.getExtras().getInt("progress");
			mplayer.seekTo(progress);
			Log.d(DEBUG_TAG, "progress change" + progress);
			break;
		}
	}

	private void sendMaxTime() {
		final Intent intent = new Intent();
		intent.setAction("com.paperairplane.music.share.MaxTime");
		int duration = mplayer.getDuration();
		intent.putExtra("maxTime", duration);
		sendBroadcast(intent);
		Log.d(DEBUG_TAG, "发送maxTime" + duration);
	}

	private void init() {
		final Intent intent = new Intent();
		intent.setAction("com.paperairplane.music.share.CurrentTime");
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					currentTime = mplayer.getCurrentPosition();
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent);

				}
				handler.sendEmptyMessageDelayed(1, 600);// 发送空消息持续时间
			}
		};
	}
}
