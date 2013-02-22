package com.paperairplane.music.share;

public class SendFeedback extends Thread {

	private String content;
	public void run() {
		Utilities.sendFeedback(content);
	}

	public SendFeedback(String _content) {
		content = _content;
	}
}
