package com.paperairplane.music.share;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class SendFeedback extends Thread {

	private String mFeedbackContent;
	private int mVersionCode;
	private Handler mHandler;
	private int mFeedbackMean;
	private Context mContext;
	
	public void run() {
		boolean result=Utilities.sendFeedback(mFeedbackContent, mVersionCode, mFeedbackMean, mContext, mHandler);
		if (result&&mFeedbackMean==Consts.ShareMeans.OTHERS) {
			mHandler.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
		}
		else if(!result){
			Message m=mHandler.obtainMessage(Consts.Status.FEEDBACK_FAIL, mFeedbackContent);
			mHandler.sendMessage(m);
		}
	}

	public SendFeedback(String content, Handler handler, int versionCode, Context context) {
		mFeedbackContent = content;
		mVersionCode = versionCode;
		mHandler = handler;
		mContext = context;
	}
	public void setMeans(int feedbackMean){
		this.mFeedbackMean = feedbackMean;
	}
	
}
