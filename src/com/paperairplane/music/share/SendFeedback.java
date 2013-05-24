package com.paperairplane.music.share;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 发送反馈的线程
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our GitHub</a>
 */
public class SendFeedback extends Thread {

	private String[] mFeedbackContents;
	private int mVersionCode;
	private Handler mHandler;
	private int mFeedbackMean;
	private Context mContext;
	
	@Override
	public void run() {
		boolean result=Utilities.sendFeedback(mFeedbackContents, mVersionCode, mFeedbackMean, mContext, mHandler);
		if (result&&mFeedbackMean==Consts.ShareMeans.OTHERS) {
			mHandler.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
		}
		else if(!result){
			Message m=mHandler.obtainMessage(Consts.Status.FEEDBACK_FAIL, mFeedbackContents);
			mHandler.sendMessage(m);
		}
	}

	/**
	 * 构造方法
	 * @param contents 反馈正文
	 * @param handler Main线程Handler
	 * @param versionCode 版本号
	 * @param context App上下文
	 */
	public SendFeedback(String[] contents, Handler handler, int versionCode, Context context) {
		mFeedbackContents = contents;
		mVersionCode = versionCode;
		mHandler = handler;
		mContext = context;
	}
	
	/**
	 * 设定分享意图
	 * @param feedbackMean 分享意图
	 * @see Consts.ShareMeans
	 */
	public void setMeans(int feedbackMean){
		this.mFeedbackMean = feedbackMean;
	}
	
}
