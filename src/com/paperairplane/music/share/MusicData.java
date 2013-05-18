package com.paperairplane.music.share;

import java.util.Locale;

import net.sourceforge.pinyin4j.PinyinHelper;

public class MusicData implements Comparable<MusicData> {
	private String mTitle, mArtist, mDuration, mPath, mAlbum, mType;
	private char mFirstChar;
	private long mAlbumId;

	public void setPath(String path) {
		mPath = path;
	}

	public void setTitle(String title) {
		mTitle = title;
		char c = mTitle.charAt(0);
		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
			if (title.toLowerCase(Locale.getDefault()).startsWith("the ")) {
				title = title.substring(4, 5);
			} else if (title.toLowerCase(Locale.getDefault()).startsWith("a ")) {
				title = title.substring(2, 3);
			} else if (title.toLowerCase(Locale.getDefault()).startsWith("an ")) {
				title = title.substring(3, 4);
			} else {
				title = title.substring(0, 1);
			}
			mFirstChar = (char) (title.toUpperCase(Locale.getDefault())
					.charAt(0));
			// 我只有英文歌……
		} else {
			try {
				mFirstChar = (char) (PinyinHelper
						.toHanyuPinyinStringArray(title.charAt(0))[0]
						.toUpperCase(Locale.getDefault()).charAt(0));
				// 异常……
			} catch (Exception e) {
				mFirstChar = Consts.UNKNOWN_CHAR;// 这个字符的编码在字母的后面……
			}
		}

	}

	public void setArtist(String artist) {
		mArtist = artist;
	}

	public void setAlbum(String album) {
		mAlbum = album;
	}

	public void setDuration(String duration) {
		mDuration = duration;
	}

	public void setAlbumId(long albumId) {
		mAlbumId = albumId;
	}

	public void setType(String type) {
		mType = type;
	}

	public String getPath() {
		return mPath;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getArtist() {
		return mArtist;
	}

	public String getDuration() {
		return mDuration;
	}

	public String getAlbum() {
		return mAlbum;
	}

	public long getAlbumId() {
		return mAlbumId;
	}

	public String getType() {
		return mType;
	}

	public char getFirstChar() {
		return mFirstChar;
	}

	@Override
	public int compareTo(MusicData another) {
		if (this.mFirstChar - another.getFirstChar() != 0) {
			return this.mFirstChar - another.getFirstChar();
		} else {
			return this.mTitle.compareTo(another.getTitle());
		}
	}

}