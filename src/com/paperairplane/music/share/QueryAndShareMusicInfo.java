package com.paperairplane.music.share;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.paperairplane.music.share.MyLogger;

class QueryAndShareMusicInfo extends Thread {
	private long mAlbumId;
	private String mArtist, mTitle, mAlbum;
	private Context mContext;
	private Handler mHandler;
	private String mArtworkPath ;
	

	public void run() {
		String[] info = Utilities.getMusicAndArtworkUrl(mTitle, mArtist, mContext,
				mHandler);
		String content;
		String fileName = null;
		content = genContent(info);

		String artworkUrl = null;
		if (info[Consts.ArraySubscript.ARTWORK] != null) {
			artworkUrl = info[Consts.ArraySubscript.ARTWORK].replace("spic",
					"lpic");
		}
		try {
			boolean flag = (mAlbumId != Consts.NULL)&& (Utilities.getLocalArtwork(mContext, mAlbumId, 10, 10) != null);
			MyLogger.d(Consts.DEBUG_TAG,"是否有本地插图："+flag);
			if (flag) {
				//你丫不能省省?1X1就够了判断啊
				//有问题啊！！！问题是！
				Utilities.saveFile(
						Utilities.getLocalArtwork(mContext, mAlbumId, 300, 300),
						mAlbum + "_" + mArtist + ".jpg", mArtworkPath);
				fileName = mArtworkPath + mAlbum + "_" + mArtist + ".jpg";
				MyLogger.d(Consts.DEBUG_TAG, "获取本地封面成功");
			} else {
				fileName = mArtworkPath
						+ Utilities.getArtwork(artworkUrl, mAlbum, mArtist,
								mArtworkPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// fileName = mArtworkPath + Utilities.getArtwork(artworkUrl, mTitle,
			// mArtworkPath);
			// 咦上面那句是什么情况……
		}
		/*
		switch (mShareMean) {
		case Consts.ShareMeans.OTHERS:
			String type = "text/plain";

			Intent intent = new Intent(Intent.ACTION_SEND);
			if (fileName != null) {
				intent.putExtra(Intent.EXTRA_STREAM,
						Uri.fromFile(new File(fileName)));
				MyLogger.d(Consts.DEBUG_TAG, "Intent " + fileName);
				type = "image/*";
			}
			intent.setType(type);
			intent.putExtra(Intent.EXTRA_SUBJECT,
					mContext.getString(R.string.app_name));
			intent.putExtra(Intent.EXTRA_TEXT, content);
			mContext.startActivity(Intent.createChooser(intent,
					mContext.getString(R.string.how_to_share)).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case Consts.ShareMeans.WEIBO:
			Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, content);

			Message m = mHandler.obtainMessage(Consts.Status.SEND_WEIBO, bundle);
			mHandler.sendMessage(m);
			break;
		}
		*/
		String type = "text/plain";

		Intent intent = new Intent(Intent.ACTION_SEND);
		if (fileName != null) {
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(fileName)));
			type = "image/*";
		}
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_SUBJECT,
				mContext.getString(R.string.app_name));
		intent.putExtra(Intent.EXTRA_TEXT, content);
		Bundle bundle = new Bundle();
		bundle.putString("artworkUrl", artworkUrl);
		bundle.putString("fileName", fileName);
		intent.putExtras(bundle);
		Message m = mHandler.obtainMessage(Consts.Status.MUSIC_INFO_FETCHED);
		m.obj = (Object) intent;
		m.sendToTarget();
	}

	private String genContent(String[] info) {
		boolean isSingle = ((info[Consts.ArraySubscript.VERSION] != null) && info[Consts.ArraySubscript.VERSION]
				.equals(mContext.getString(R.string.single)));
		String content = mContext.getString(R.string.share_by)
				+ " "
				+ ((mArtist.equals("")) ? info[Consts.ArraySubscript.ARTIST]
						: mArtist)
				+ " "
				+ (isSingle ? mContext.getString(R.string.music_single)
						: mContext.getString(R.string.music_artist))
				+ " "
				+ mTitle
				+ " "
				+ (isSingle ? ""
						: mContext.getString(R.string.music_album)
								+ " "
								+ ((mAlbum.equals("")) ? info[Consts.ArraySubscript.ALBUM]
										: mAlbum) + " ")
				+ mContext.getString(R.string.before_url)
				+ info[Consts.ArraySubscript.MUSIC] + " ";
		return content;
	}

	public QueryAndShareMusicInfo(String title, String artist, String album,
			long albumId,Context context, Handler handler) {
		mAlbumId = albumId;
		mTitle = title;
		mArtist = artist;
		mAlbum = album;
		mContext = context;
		mHandler = handler;
		mArtworkPath = context.getExternalCacheDir().getAbsolutePath() +
		 "/.artworkCache/";
	}

}
