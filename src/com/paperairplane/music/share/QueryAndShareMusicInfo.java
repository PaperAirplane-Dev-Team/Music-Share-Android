package com.paperairplane.music.share;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.paperairplane.music.share.utils.MyLogger;
import com.paperairplane.music.share.utils.Utilities;

import de.umass.lastfm.CallException;

/**
 * 查询并且分享音乐信息的线程
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
class QueryAndShareMusicInfo implements Runnable {
	private MusicData mMusic;
	private Context mContext;
	private Handler mHandler;
	private String mArtworkPath;

	@Override
	/**
	 * 主调方法,查询信息并且返回给主线程
	 */
	public void run() {
		try {
			mMusic = Utilities
					.getMusicAndArtworkUrlFromLastfm(mMusic, mContext);
		} catch (CallException e) {
			mMusic = Utilities.getMusicAndArtworkUrlFromDouban(mMusic,
					mContext, mHandler);
		}
		if (mMusic.getMusicUrl() == null) {
			mMusic = Utilities.getMusicAndArtworkUrlFromDouban(mMusic,
					mContext, mHandler);
		}
		String content = genContent();
		String artworkLocalUrl = null;

		try {
			long albumId = mMusic.getAlbumId();
			boolean flag = (albumId != Consts.NULL)
					&& (Utilities.getLocalArtwork(mContext, albumId, 10, 10) != null);
			MyLogger.d(Consts.DEBUG_TAG, "是否有本地插图：" + flag);
			if (flag) {
				// 你丫不能省省?1X1就够了判断啊
				// 有问题啊！！！问题是！
				Utilities
						.saveFile(Utilities.getLocalArtwork(mContext, albumId,
								300, 300),
								mMusic.getAlbumId() + "_" + mMusic.getArtist()
										+ ".jpg", mArtworkPath);
				artworkLocalUrl = mArtworkPath + mMusic.getAlbumId() + "_"
						+ mMusic.getArtist() + ".jpg";
				MyLogger.d(Consts.DEBUG_TAG, "获取本地封面成功");
			} else {
				artworkLocalUrl = mArtworkPath
						+ Utilities.getArtwork(mMusic.getArtworkNetUrl(),
								mMusic.getTitle(), mMusic.getArtist(),
								mArtworkPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mMusic.setArtworkLocalUrl(artworkLocalUrl);

		String type = "text/plain";

		Intent intent = new Intent(Intent.ACTION_SEND);
		if (artworkLocalUrl != null) {
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(new File(artworkLocalUrl)));
			type = "image/*";
		}
		intent.setType(type);
		intent.putExtra(Intent.EXTRA_SUBJECT,
				mContext.getString(R.string.app_name));
		intent.putExtra(Intent.EXTRA_TEXT, content);
		intent.putExtra("sms_body", intent.getStringExtra(Intent.EXTRA_TEXT));
		Bundle bundle = new Bundle();
		bundle.putString("artworkUrl", mMusic.getArtworkNetUrl());
		bundle.putString("fileName", artworkLocalUrl);
		bundle.putString("annotation", mMusic.toJsonString());
		intent.putExtras(bundle);
		Message m = mHandler.obtainMessage(Consts.Status.MUSIC_INFO_FETCHED);
		m.obj = (Object) intent;
		m.sendToTarget();
	}

	/**
	 * 生成分享文字内容
	 * 
	 * @param info
	 *            查询到的音乐信息
	 * @return 分享的文字
	 */
	private String genContent() {
		boolean isSingle = ((mMusic.getVersion() != null) && mMusic
				.getVersion().equals(mContext.getString(R.string.single)));
		String content = mContext.getString(R.string.share_by)
				+ " "
				+ mMusic.getArtist()
				+ " "
				+ (isSingle ? mContext.getString(R.string.music_single)
						: mContext.getString(R.string.music_artist))
				+ " "
				+ mMusic.getTitle()
				+ " "
				+ (isSingle ? "" : mContext.getString(R.string.music_album)
						+ " " + mMusic.getAlbum() + " ")
				+ mContext.getString(R.string.before_url)
				+ mMusic.getMusicUrl() + " ";
		return content;
	}

	/**
	 * 构造方法
	 * 
	 * @param title
	 *            音乐标题
	 * @param artist
	 *            艺术家
	 * @param album
	 *            专辑
	 * @param albumId
	 *            专辑ID
	 * @param context
	 *            App上下文
	 * @param handler
	 *            线程Handler
	 */
	public QueryAndShareMusicInfo(MusicData music, Context context,
			Handler handler) {
		this.mMusic = music;
		mContext = context;
		mHandler = handler;
		mArtworkPath = context.getExternalCacheDir().getAbsolutePath()
				+ "/.artworkCache/";
	}

}
