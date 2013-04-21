package com.paperairplane.music.share;

public class MusicData {
	private String mTitle, mArtist, mDuration, mPath, mAlbum, mType;
	private long mAlbumId;

	public void setPath(String path) {
		mPath = path;
	}

	public void setTitle(String title) {
		mTitle = title;
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
	public String getType(){
		return mType;
	}

}