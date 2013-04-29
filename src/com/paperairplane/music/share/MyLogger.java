package com.paperairplane.music.share;

import android.util.Log;

@SuppressWarnings("unused")
public class MyLogger {

	public static int d(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.d(tag, msg);
		return Consts.NULL;
	}

	public static int d(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.d(tag, msg, tr);
		return Consts.NULL;
	}

	public static int e(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.e(tag, msg);
		return Consts.NULL;
	}

	public static int e(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.e(tag, msg, tr);
		return Consts.NULL;
	}

	public static String getStackTraceString(Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.getStackTraceString(tr);
		return null;
	}

	public static int i(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.i(tag, msg);
		return Consts.NULL;
	}

	public static int i(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.i(tag, msg, tr);
		return Consts.NULL;
	}

	public static boolean isLoggable(String tag, int level) {
		if (Consts.DEBUG_ON)
			return Log.isLoggable(tag, level);
		return false;
	}

	public static int println(int priority, String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.println(priority, tag, msg);
		return Consts.NULL;
	}

	public static int v(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.v(tag, msg);
		return Consts.NULL;
	}

	public static int v(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.v(tag, msg, tr);
		return Consts.NULL;
	}

	public static int w(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.w(tag, msg);
		return Consts.NULL;
	}

	public static int w(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.w(tag, msg, tr);
		return Consts.NULL;
	}

	public static int wtf(String tag, String msg) {
		if (Consts.DEBUG_ON)
			return Log.wtf(tag, msg);
		return Consts.NULL;
	}

	public static int wtf(String tag, String msg, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.wtf(tag, msg, tr);
		return Consts.NULL;
	}

	public static int wtf(String tag, Throwable tr) {
		if (Consts.DEBUG_ON)
			return Log.wtf(tag, tr);
		return Consts.NULL;
	}
}
