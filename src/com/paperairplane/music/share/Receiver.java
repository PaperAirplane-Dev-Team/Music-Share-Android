package com.paperairplane.music.share;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

/**
 * 用于处理扫描事件的IntentReceiver
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our GitHub</a>
 */
public class Receiver extends BroadcastReceiver {
	private Toast mToast = null;
	private Handler mHandler = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			mToast = Toast.makeText(context, R.string.refresh_on_process,
					Toast.LENGTH_SHORT);
			mToast.show();
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
			mHandler.sendEmptyMessage(Consts.Status.REFRESH_LIST_FINISHED);
		}
	}

	/**
	 * 构造方法
	 * @param handler Main中的线程Handler
	 */
	Receiver(Handler handler) {
		this.mHandler = handler;
	}
}
