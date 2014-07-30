package com.paperairplane.music.share;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.paperairplane.music.share.utils.HanziToPinyin;

/**
 * 存储的音乐信息
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class MusicData implements Comparable<MusicData> {
	private String mTitle, mArtist, mDuration, mPath, mAlbum, mType, mArtworkNetUrl,mArtworkLocalUrl,
			mVersion, mMusicUrl;
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
				mFirstChar = (char) (HanziToPinyin.convert(String.valueOf(title.charAt(0)))
												.toUpperCase(Locale.getDefault()).charAt(0));
			} catch (Exception e) {
				mFirstChar = Consts.UNKNOWN_CHAR;// 这个字符的编码在字母的后面……
			}
		}
	}

	public void setArtist(String artist) {
		if(artist!=null){
			mArtist = artist;
		}
	}

	public void setAlbum(String album) {
		if(album!=null){
			mAlbum = album;			
		}
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

	public String toJsonString() {
		String strJson = null;
		JSONArray jarr = new JSONArray();
		JSONObject jobj = new JSONObject();
		try {
			jobj.put("title", mTitle);
			jobj.put("artist", mArtist);
			jobj.put("album", mAlbum);
			jarr.put(jobj);
			} catch (JSONException e) {
			e.printStackTrace();
		}
		strJson = jarr.toString();
		return strJson;
	}

	@Override
	public int compareTo(MusicData another) {
		if (this.mFirstChar - another.getFirstChar() != 0) {
			return this.mFirstChar - another.getFirstChar();
		} else {
			return this.mTitle.compareTo(another.getTitle());
		}
	}

	/**
	 * @return the mArtwork
	 */
	public String getArtworkNetUrl() {
		return mArtworkNetUrl;
	}

	/**
	 * @param artwork
	 *            the mArtwork to set
	 */
	public void setArtworkNetUrl(String artwork) {
		this.mArtworkNetUrl = artwork;
		if (this.mArtworkNetUrl != null) {
			this.mArtworkNetUrl = this.mArtworkNetUrl.replace("spic", "lpic");
		}
	}

	/**
	 * @return the mVersion
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * @param version
	 *            the mVersion to set
	 */
	public void setVersion(String version) {
		this.mVersion = version;
	}

	/**
	 * @return the mMusicUrl
	 */
	public String getMusicUrl() {
		return mMusicUrl;
	}

	/**
	 * @param musicUrl
	 *            the mMusicUrl to set
	 */
	public void setMusicUrl(String musicUrl) {
		this.mMusicUrl = musicUrl;
	}

	/**
	 * @return the mArtworkLocalUrl
	 */
	public String getArtworkLocalUrl() {
		return mArtworkLocalUrl;
	}

	/**
	 * @param mArtworkLocalUrl the mArtworkLocalUrl to set
	 */
	public void setArtworkLocalUrl(String mArtworkLocalUrl) {
		this.mArtworkLocalUrl = mArtworkLocalUrl;
	}

}
