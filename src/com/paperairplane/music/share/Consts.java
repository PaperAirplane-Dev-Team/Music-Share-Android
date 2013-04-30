package com.paperairplane.music.share;

import android.net.Uri;
import android.provider.MediaStore;

public class Consts {

	public final static String[] MEDIA_INFO = new String[] {
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.MIME_TYPE };
	public final static String DEBUG_TAG = "Music Share DEBUG";
	public final static String APP_KEY = "1006183120";
	public final static String FEEDBACK = "#纸飞机音乐分享反馈# @姚沛然 @HarryChen-SIGKILL- ";
	public final static String RELEASE_DATE = "2013.4.30";
	public final static String WHATSNEW = "对话框处理";
	public final static String ORIGIN_COLOR = "#99000088";
	public final static Uri ARTWORK_URI = Uri
			.parse("content://media/external/audio/albumart");
	public final static int NULL = -1;
	public final static boolean DEBUG_ON = true;
	public final static boolean ON_PLAY_STORE = false;
	public final static int LOOK_FOR_SUGGESTION_REQUEST_CODE = 233;
	public final static int PICK_BACKGROUND_REQUEST_CODE = 234;
	public final static char UNKNOWN_CHAR = 91;

	public final class Url {
		public final static String API_SUGGESTION = "https://api.weibo.com/2/search/suggestions/at_users.json";
		public final static String API_QUERY = "https://api.douban.com/v2/music/search?appkey=039b0d83358026730ad12889a0807359&count=1&q=";
		public final static String INFO_REDIRECT = "http://paperairplane.sinaapp.com/redirect.php?id=";
		public final static String FEEDBACK = "http://paperairplane.sinaapp.com/feedback.php";
		public final static String CHECK_UPDATE = "http://paperairplane.sinaapp.com/music-share/update.php";
		public final static String CHECK_TEST_UPDATE = "http://paperairplane.sinaapp.com/music-share/test_version_update.php";
		public final static String AUTH_REDIRECT = "https://api.weibo.com/oauth2/default.html";
		public final static String WEIBO_STATUSES_UPDATE = "https://api.weibo.com/2/statuses/update.json";
	}

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
		public static final int NO_UPDATE = 9;
		public static final int HAS_UPDATE = 10;
		public static final int REFRESH_LIST_FINISHED = 11;
		public static final int DATA_CHANGED = 12;
		public static final int MUSIC_INFO_FETCHED = 13;
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

		public static final int UPDATE_INFO = 0;
		public static final int DOWNLOAD_URL = 1;
	}

	public final class ShareMeans {
		public static final int WEIBO = 0;
		public static final int OTHERS = 1;
		public static final int INTERNAL = 2;

	}

	public final class Dialogs {
		public static final int SHARE = 0;
		public static final int ABOUT = 1;
		public static final int SEARCH = 2;
		public static final int EMPTY = 3;
		public static final int CHANGE_COLOR = 4;
		public static final int CHANGE_BACKGROUND = 5;
	}

	public final class Preferences {
		public final static String WEIBO = "com_weibo_sdk_android";
		public final static String SHARE = "ShareStatus";
		public final static String FEEDBACK = "FeedbackStatus";
		public final static String GENERAL = "General";
		public final static String BG_COLOR = "overlay_bg_color";
		public final static String BG_PATH = "background_path";
		public final static String OVERLAY = "overlay";
	}

	public final class MenuItem {
		public final static int REFRESH = 3;
		public final static int AUTH = 1;
		public final static int UNAUTH = 2;
	}

	public final class Color {
		public final static int RED = 0;
		public final static int GREEN = 1;
		public final static int BLUE = 2;
		public final static int OPACITY = 3;
	}

}
