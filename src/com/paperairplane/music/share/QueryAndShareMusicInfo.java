package com.paperairplane.music.share;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.paperairplane.music.share.MyLogger;

import de.umass.lastfm.CallException;

/**
 * 查询并且分享音乐信息的线程
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our GitHub</a>
 */
class QueryAndShareMusicInfo extends Thread {
	private long mAlbumId;
	private String mArtist, mTitle, mAlbum;
	private Context mContext;
	private Handler mHandler;
	private String mArtworkPath ;
	

	@Override
	/**
	 * 主调方法,查询信息并且返回给主线程
	 */
	public void run() {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
		String[] info;
		try {
			info = Utilities.getMusicAndArtworkUrlFromLastfm(mTitle, mArtist,
					mContext);
		} catch (CallException e) {
			info = Utilities.getMusicAndArtworkUrlFromDouban(mTitle, mArtist, mContext,
					mHandler);
		}
		if(info[Consts.ArraySubscript.MUSIC]==null){
			info=Utilities.getMusicAndArtworkUrlFromDouban(mTitle, mArtist, mContext,
					mHandler);
		}
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
		intent.putExtra("sms_body", intent.getStringExtra(Intent.EXTRA_TEXT));
		Bundle bundle = new Bundle();
		bundle.putString("artworkUrl", artworkUrl);
		bundle.putString("fileName", fileName);
		intent.putExtras(bundle);
		Message m = mHandler.obtainMessage(Consts.Status.MUSIC_INFO_FETCHED);
		m.obj = (Object) intent;
		m.sendToTarget();
	}

	/**
	 * 生成分享文字内容
	 * @param info 查询到的音乐信息
	 * @return 分享的文字
	 */
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

	/**
	 * 构造方法
	 * @param title 音乐标题
	 * @param artist 艺术家
	 * @param album 专辑
	 * @param albumId 专辑ID
	 * @param context App上下文
	 * @param handler 线程Handler
	 */
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
