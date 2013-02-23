package com.paperairplane.music.share;

import android.os.Handler;
import android.os.Message;

public class SendFeedback extends Thread {

	private String content;
	private Handler handler;

	public void run() {
		if (Utilities.sendFeedback(content)) {
			handler.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
		}
		else {
			Message m=handler.obtainMessage(Consts.Status.FEEDBACK_FAIL, content);
			handler.sendMessage(m);
		}
	}

	public SendFeedback(String _content, Handler _handler) {
		content = _content;
		handler = _handler;
	}
}
