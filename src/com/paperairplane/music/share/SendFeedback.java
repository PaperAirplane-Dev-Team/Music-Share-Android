package com.paperairplane.music.share;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class SendFeedback extends Thread {

	private String content;
	private String versionCode;
	private Handler handler;
	private int means;
	private Context context;
	public void run() {
		boolean result=Utilities.sendFeedback(content, versionCode, means, context, handler);
		if (result&&means==Consts.ShareMeans.OTHERS) {
			handler.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
		}
		else if(!result){
			Message m=handler.obtainMessage(Consts.Status.FEEDBACK_FAIL, content);
			handler.sendMessage(m);
		}
	}

	public SendFeedback(String _content, Handler _handler, String _versionCode, Context _context) {
		content = _content;
		versionCode = _versionCode;
		handler = _handler;
		context = _context;
	}
	public void setMeans(int means){
		this.means = means;
	}
	
}
