package com.paperairplane.music.share;

import android.net.Uri;
import android.provider.MediaStore;

public class Consts {

	public final static String[] MEDIA_INFO = new String[] {
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID };
	public final static String DEBUG_TAG = "Music Share DEBUG";
	public final static String APP_KEY = "1006183120";
	public final static String API_URL = "http://paperairplane.sinaapp.com/proxy.php?q=";
	public final static String INFO_REDIRECT_URL = "http://paperairplane.sinaapp.com/redirect.php?id=";
	public final static String FEEDBACK_URL = "http://paperairplane.sinaapp.com/feedback.php";
	public final static String REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
	public final static String WEIBO_STATUSES_UPDATE = "https://api.weibo.com/2/statuses/update.json";
	public final static String FEEDBACK = " @“¶≈Ê»ª @HarryChen-SIGKILL #÷Ω∑…ª˙“Ù¿÷∑÷œÌ∑¥¿°#";
	public final static String VERY_LONG = "                                                         ";
	public final static Uri ARTWORK_URI = Uri
			.parse("content://media/external/audio/albumart");
	public final static int NULL = -1;

	public final class Status {
		public static final int INTERNET_ERROR = 0;
		public static final int SEND_WEIBO = 1;
		public static final int SEND_SUCCEED = 2;
		public static final int AUTH_ERROR = 3;
		public static final int SEND_ERROR = 4;
		public static final int NOT_AUTHORIZED_ERROR = 5;
		public static final int AUTH_SUCCEED = 6;
		public static final int FEEDBACK_SUCCEED = 7;
		public static final int FEEDBACK_FAIL = 8;
	}

	public final class WeiboUid {
		public static final int HARRY_UID = 1689129907;
		public static final int XAVIER_UID = 2121014783;
		public static final int APP_UID = 1153267341;
	}

	public final class ArraySubscript {
		public static final int MUSIC = 0;
		public static final int ARTWORK = 1;
		public static final int ARTIST = 2;
		public static final int ALBUM = 3;
		public static final int VERSION = 4;
	}

	public final class ShareMeans {
		public static final int WEIBO = 0;
		public static final int OTHERS = 1;
	}

	public final class Dialogs {
		public static final int SHARE = 0;
		public static final int ABOUT = 1;
		public static final int SEARCH = 2;
		public static final int EMPTY = 3;
	}

	public final class Preferences {
		public final static String WEIBO = "com_weibo_sdk_android";
		public final static String SHARE = "ShareStatus";
		public final static String FEEDBACK = "FeedbackStatus";
	}

	public final class MenuItem {
		public final static int REFRESH = 0;
		public final static int AUTH = 1;
		public final static int UNAUTH = 2;
	}
}
