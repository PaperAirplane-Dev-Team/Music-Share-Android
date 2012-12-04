package com.paperairplane.music.share;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

public class MusicPlayerService extends Service {
	private MediaPlayer mplayer;
	private final int PLAY=0,PAUSE=1,STOP=2;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		mplayer=new MediaPlayer();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mplayer.release();
		mplayer=null;
	}
	@Override
	public void onStart(Intent intent, int startId) {
		Bundle bundle=intent.getExtras();
		int op=bundle.getInt("op");
		switch(op){
		case PLAY:
			if(bundle.getString("path")!=null){
				String path=bundle.getString("path");
				Log.v("Music Share SERVICE DEBUG",path);
				try{
					mplayer.reset();
					mplayer.setDataSource(path);
					mplayer.prepare();
					mplayer.start();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
			else{
				mplayer.start();
			}
			break;
		case STOP:
			mplayer.stop();
			break;
		case PAUSE:
			mplayer.pause();
			break;
		}
	}

}
