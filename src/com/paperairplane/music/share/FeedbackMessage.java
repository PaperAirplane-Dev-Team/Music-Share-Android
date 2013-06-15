package com.paperairplane.music.share;

import android.content.Context;


/**
 * 发送反馈的信息集合
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our GitHub</a>
 */
public class FeedbackMessage {

	public String[] mFeedbackContents;
	public int mVersionCode;
	public int mFeedbackMean;
	public Context mContext;
	

	/**
	 * 构造方法
	 * @param contents 反馈正文
	 * @param handler Main线程Handler
	 * @param versionCode 版本号
	 * @param context App上下文
	 */
	public FeedbackMessage(String[] contents, int versionCode, Context context) {
		mFeedbackContents = contents;
		mVersionCode = versionCode;
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
