package com.paperairplane.music.share;

import android.os.Handler;
import android.os.Message;

public class SendFeedback extends Thread {

	private String content;
	private String versionCode;
	private Handler handler;
	private int means;
	private String accessToken;
	public void run() {
		if (Utilities.sendFeedback(content,versionCode,means,accessToken)) {
			handler.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
		}
		else {
			Message m=handler.obtainMessage(Consts.Status.FEEDBACK_FAIL, content);
			handler.sendMessage(m);
		}
	}

	public SendFeedback(String _content, Handler _handler, String versionCode) {
		content = _content;
		this.versionCode = versionCode;
		handler = _handler;
	}
	public void setMeansAndAccessToken(int means,String token){
		this.means = means;
		this.accessToken = token;
	}
	
}
