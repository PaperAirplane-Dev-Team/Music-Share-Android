package com.paperairplane.music.share;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
/**
 * 网络访问管理
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class HttpQuestHandler extends Handler{
	
	private static Handler mHandlerUi;
	private static HttpQuestHandler mHttpQuestHandler ;
	private static HandlerThread mHandlerThread = new HandlerThread("HttpQuest");
	
	/**
	 * 私有构造函数
	 * @param looper
	 */
	private HttpQuestHandler(Looper looper){
		super(looper);
	}
	
	/**
	 * @deprecated 私有构造函数。不应使用。
	 */
	private HttpQuestHandler() throws Exception{
		throw new Exception("What the hell");
	}
	
	@Override
	public void handleMessage(final Message m){
		final Object obj = m.obj;
		switch(m.what){
		case Consts.NetAccessIntent.SEND_FEEDBACK:
			Runnable rnFeedback = new Runnable(){
				@Override 
				/**
				 * 发送反馈
				 */
				public void run(){
					FeedbackMessage fm = (FeedbackMessage) obj;
					String[] contents = fm.mFeedbackContents;
					int vc = fm.mVersionCode;
					int mean = fm.mFeedbackMean;
					Context ctx = fm.mContext;
					
					boolean result=Utilities.sendFeedback(contents, vc, mean, ctx, mHandlerUi);
					if (result&&mean==Consts.ShareMeans.OTHERS) {
						mHandlerUi.sendEmptyMessage(Consts.Status.FEEDBACK_SUCCEED);
					}
					else if(!result){
						Message msg=mHandlerUi.obtainMessage(Consts.Status.FEEDBACK_FAIL, contents);
						mHandlerUi.sendMessage(msg);
					}
				}
			};
			this.post(rnFeedback);
			break;
		case Consts.NetAccessIntent.CHECK_FOR_UPDATE:
			final Context ctx = (Context) obj;
			this.postDelayed(new Runnable() {
			@Override
			public void run() {
				Utilities.checkForUpdate(Main.sVersionCode, mHandlerUi,
						ctx,
						ctx.getResources().getConfiguration().locale);
			}
		},5000);
			break;
		case Consts.NetAccessIntent.QUERY_AND_SHARE_MUSIC_INFO:
			this.post((Runnable)obj);
			break;
		}
	}
	/**
	 * 使用单例模式的构造函数
	 * @param uiHandler 与UI线程绑定的Handler
	 * @return HttpQuestHandler的单例
	 */
	public static HttpQuestHandler getInstance(Handler uiHandler){
		mHandlerUi = uiHandler;
		
		if(mHttpQuestHandler == null){
			mHandlerThread.start();
			mHttpQuestHandler = new HttpQuestHandler(mHandlerThread.getLooper());
		}
		
		return mHttpQuestHandler;
	}
	
	

}
