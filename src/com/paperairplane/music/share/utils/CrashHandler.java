package com.paperairplane.music.share.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;
import android.os.Message;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.Consts.FeedbackContentsItem;
import com.paperairplane.music.share.FeedbackMessage;
import com.paperairplane.music.share.Main;
import com.paperairplane.music.share.R;

public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";
	private static CrashHandler INSTANCE = new CrashHandler();
	private Activity mMainActivity;

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Activity ctx) {
		mMainActivity = ctx;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {
		MyLogger.wtf(Consts.DEBUG_TAG, "UNCAUGHT EXCEPTION!", ex);
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				new AlertDialog.Builder(mMainActivity)
						.setTitle(R.string.notice)
						.setCancelable(false)
						.setMessage(R.string.crash)
						.setPositiveButton(android.R.string.ok,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										StringWriter sw = new StringWriter();
										PrintWriter pw = new PrintWriter(sw);
										ex.printStackTrace(pw);
										String[] contents = new String[3];
										contents[FeedbackContentsItem.CONTENT] = sw
												.toString();
										contents[FeedbackContentsItem.EMAIL] = "Crash Report";
										contents[FeedbackContentsItem.NAME] = "CRASH";
										FeedbackMessage feedback = new FeedbackMessage(
												contents, Main.sVersionCode,
												mMainActivity);
										feedback.setMeans(Consts.ShareMeans.OTHERS);
										Message m = HttpQuestHandler
												.getInstance()
												.obtainMessage(
														Consts.NetAccessIntent.SEND_FEEDBACK);
										m.obj = feedback;
										m.sendToTarget();
										mMainActivity.finish();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										throw new RuntimeException(ex);
									}
								}).setCancelable(false).create().show();
				Looper.loop();
			}
		}.start();
	}
}
