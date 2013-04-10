package com.paperairplane.music.share;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class QueryAndShareMusicInfo extends Thread {
	private int means;
	private long album_id;
	private String artist, title, album;
	private Context context;
	private Handler handler;
	private String ARTWORK_PATH ;
	

	public void run() {
		String[] info = Utilities.getMusicAndArtworkUrl(title, artist, context,
				handler);
		String content;
		String fileName = null;
		content = genContent(info);

		String artworkUrl = null;
		if (info[Consts.ArraySubscript.ARTWORK] != null) {
			artworkUrl = info[Consts.ArraySubscript.ARTWORK].replace("spic",
					"lpic");
		}
		try {
			boolean flag = (album_id != Consts.NULL)&& (Utilities.getLocalArtwork(context, album_id, 10, 10) != null);
			Log.d(Consts.DEBUG_TAG,"是否有本地插图："+flag);
			if (flag) {
				//你丫不能省省?1X1就够了判断啊
				//有问题啊！！！问题是！
				Utilities.saveFile(
						Utilities.getLocalArtwork(context, album_id, 300, 300),
						album + "_" + artist + ".jpg", ARTWORK_PATH);
				fileName = ARTWORK_PATH + album + "_" + artist + ".jpg";
				Log.d(Consts.DEBUG_TAG, "获取本地封面成功");
			} else {
				fileName = ARTWORK_PATH
						+ Utilities.getArtwork(artworkUrl, album, artist,
								ARTWORK_PATH);
			}
		} catch (Exception e) {
			Log.e(Consts.DEBUG_TAG, "Error Occured");
			e.printStackTrace();
			// fileName = ARTWORK_PATH + Utilities.getArtwork(artworkUrl, title,
			// ARTWORK_PATH);
			// 咦上面那句是什么情况……
		}
		switch (means) {
		case Consts.ShareMeans.OTHERS:
			String type = "text/plain";

			Intent intent = new Intent(Intent.ACTION_SEND);
			if (fileName != null) {
				intent.putExtra(Intent.EXTRA_STREAM,
						Uri.fromFile(new File(fileName)));
				Log.d(Consts.DEBUG_TAG, "Intent " + fileName);
				type = "image/*";
			}
			intent.setType(type);
			intent.putExtra(Intent.EXTRA_SUBJECT,
					context.getString(R.string.app_name));
			intent.putExtra(Intent.EXTRA_TEXT, content);
			context.startActivity(Intent.createChooser(intent,
					context.getString(R.string.how_to_share)).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case Consts.ShareMeans.WEIBO:
			Bundle bundle = new Bundle();
			bundle.putString("content", content);
			bundle.putString("artworkUrl", artworkUrl);
			bundle.putString("fileName", fileName);
			Message m = handler.obtainMessage(Consts.Status.SEND_WEIBO, bundle);
			handler.sendMessage(m);
			break;
		}

	}

	private String genContent(String[] info) {
		boolean isSingle = ((info[Consts.ArraySubscript.VERSION] != null) && info[Consts.ArraySubscript.VERSION]
				.equals(context.getString(R.string.single)));
		String content = context.getString(R.string.share_by)
				+ " "
				+ ((artist.equals("")) ? info[Consts.ArraySubscript.ARTIST]
						: artist)
				+ " "
				+ (isSingle ? context.getString(R.string.music_single)
						: context.getString(R.string.music_artist))
				+ " "
				+ title
				+ " "
				+ (isSingle ? ""
						: context.getString(R.string.music_album)
								+ " "
								+ ((album.equals("")) ? info[Consts.ArraySubscript.ALBUM]
										: album) + " ")
				+ context.getString(R.string.before_url)
				+ info[Consts.ArraySubscript.MUSIC] + " ";
		return content;
	}

	public QueryAndShareMusicInfo(String _title, String _artist, String _album,
			long _album_id, int _means, Context _context, Handler _handler) {
		album_id = _album_id;
		title = _title;
		artist = _artist;
		album = _album;
		means = _means;
		context = _context;
		handler = _handler;
		 this.ARTWORK_PATH = _context.getExternalCacheDir().getAbsolutePath() +
		 "/.artworkCache/";
	}

}
