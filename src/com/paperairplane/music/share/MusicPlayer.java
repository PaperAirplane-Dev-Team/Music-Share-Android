package com.paperairplane.music.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MusicPlayer extends Activity {
	private int nowPlaying,nowDuration;
	private final static int PLAY = 0, PAUSE = 1, STOP = 2;
	private boolean isPlaying = false;
	private final static String DEBUG_TAG = "Music Share DEBUG";
	private Intent musicIntent;
	private RefreshProgressBar refresh;
	private ProgressBar progressMusic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		Bundle extras = getIntent().getExtras();
		int id = extras.getInt("id");
		String duration = extras.getString("duration");
		final int orgDuration = extras.getInt("orgDuration")/1000;
		String path = extras.getString("path");
		String title = extras.getString("title");
		String artist = extras.getString("artist");

		if (!isPlaying || nowPlaying != id) {
			try {
				musicIntent = new Intent();
				musicIntent
						.setAction("com.paperairplane.music.share.PLAYMUSIC");
				Bundle bundle = new Bundle();
				bundle.putString("path", path);
				bundle.putInt("op", PLAY);
				musicIntent.putExtras(bundle);
				startService(musicIntent);
				isPlaying = true;
				nowPlaying = id;
			} catch (Throwable e) {
				//Log.d(DEBUG_TAG,e.getMessage());
				//e.printStackTrace();
			}
			refresh=new RefreshProgressBar(orgDuration);
			refresh.setMean(PLAY);
			refresh.run();
			//FIXME 这里麻烦很大，估计出问题了
			//FIXME 我看来又是子线程没法操作View,弄不好又要Handler了
		}
		
		final TextView tvTitle = (TextView) findViewById(R.id.text_player_title);
		final TextView tvSinger = (TextView) findViewById(R.id.text_player_singer);
		tvTitle.setText(title + "(" + duration + ")"+ getString(R.string.very_long));
		tvSinger.setText(artist + getString(R.string.very_long));
		progressMusic=(ProgressBar)findViewById(R.id.progressMusic);
		final Button btnPP = (Button) findViewById(R.id.button_player_pause);
		final Button btnRT = (Button) findViewById(R.id.button_player_return);
		btnPP.setBackgroundDrawable(getResources().getDrawable(
				android.R.drawable.ic_media_pause));
		btnRT.setBackgroundDrawable(getResources().getDrawable(
				android.R.drawable.ic_delete));
		btnPP.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isPlaying == true) {
					musicIntent = new Intent();
					musicIntent
							.setAction("com.paperairplane.music.share.PLAYMUSIC");
					Bundle bundle = new Bundle();
					bundle.putInt("op", PAUSE);
					musicIntent.putExtras(bundle);
					startService(musicIntent);
					btnPP.setBackgroundDrawable(getResources().getDrawable(
							android.R.drawable.ic_media_play));
					isPlaying = false;
					refresh.setMean(PAUSE);
				} else if (isPlaying == false) {
					musicIntent = new Intent();
					musicIntent.setAction("com.paperairplane.music.share.PLAYMUSIC");
					Bundle bundle = new Bundle();
					bundle.putInt("op", PLAY);
					musicIntent.putExtras(bundle);
					startService(musicIntent);
					btnPP.setBackgroundDrawable(getResources().getDrawable(
							android.R.drawable.ic_media_pause));
					isPlaying = true;
					refresh.setMean(PLAY);
				}
			}
		});
		btnRT.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				musicIntent = new Intent();
				musicIntent
						.setAction("com.paperairplane.music.share.PLAYMUSIC");
				Bundle bundle = new Bundle();
				bundle.putInt("op", STOP);
				musicIntent.putExtras(bundle);
				startService(musicIntent);
				refresh.setMean(STOP);
				isPlaying = false;
				finish();
				
			}
		});
	}
	
	class RefreshProgressBar extends Thread{
		int max,now,mean;
		@Override
		public void run(){
			//FIXME 这里,这里,这些代码是废物
			progressMusic.setMax(max);
			progressMusic.setIndeterminate(false);
			try{
				while (mean==PLAY){
					Thread.sleep(1);
					now++;
					//progressMusic.setProgress(now);
					progressMusic.incrementProgressBy(1);
					Log.v(DEBUG_TAG,"Progress!");
				}
			}
			catch (Exception e){
				Log.d(DEBUG_TAG,e.getMessage());
			}
		}
		RefreshProgressBar(int _max){
			max=_max;
			now=0;
		}
		
		public void setMean(int _mean){
			mean=_mean;
		}
		
		public int getNowDuration(){
			return now;
		}
	}

}
