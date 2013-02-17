package com.paperairplane.music.share;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;

public class WeiboHelper {
	private StatusesAPI api = null;
	final private int HARRY_UID = 1689129907, XAVIER_UID = 2121014783,
			APP_UID = 1153267341;
	private final static String DEBUG_TAG = "Music Share DEBUG";
	private Handler handler = null;
	final private int SEND_SUCCEED = 5, AUTH_ERROR = 6, SEND_ERROR = 7,
			AUTH_SUCCEED = 9;
	private Context applicationContext;
	private AuthDialogListener listener;
	private RequestListener requestListener;
/**
 * WeiboHelper构造函数
 * @param _handler 用于控制UI线程的Handler
 * @param _applicationContext 用于暂存微博内容和其它信息的（Application）Context
 */
	//那么请问……你的第二个Activity的Context要来做什么?似乎你没用到……
	public WeiboHelper(Handler _handler, Context _applicationContext) {
		handler = _handler;
		applicationContext = _applicationContext;
	}
/**
 * 获取微博授权监听器实例
 * @return 微博授权监听器
 */
	public AuthDialogListener getListener() {
		listener = new AuthDialogListener();
		return listener;
	}
/**
 * 发送微博
 * @param content 要发送的微博内容
 * @param artworkUrl 要发送的图片
 * @param willFollow 是否要关注
 */
	public void sendWeibo(String content, String artworkUrl, boolean willFollow) {
		api = new StatusesAPI(Main.accessToken);
		initRequestListener();

		if (artworkUrl == null) {
			Log.v(DEBUG_TAG, "发送无图微博");
			api.update(content, null, null, requestListener);
		} else {
			Log.v(DEBUG_TAG, "发送带图微博，url=" + artworkUrl);
			String url = "https://api.weibo.com/2/statuses/upload_url_text.json";
			WeiboParameters params = new WeiboParameters();
			params.add("access_token", Main.accessToken.getToken());
			params.add("status", content);
			params.add("url", artworkUrl);
			AsyncWeiboRunner.request(url, params, "POST", requestListener);
		}
		if (willFollow == true) {// 判断是否要关注开发者
			follow(HARRY_UID);// 关注Harry Chen
			follow(XAVIER_UID);// 关注Xavier Yao
			follow(APP_UID);// 关注官方微博
		}
	}

	private void initRequestListener() {
		requestListener = new RequestListener() {
			Message m = handler.obtainMessage();

			@Override
			public void onComplete(String arg0) {
				handler.sendEmptyMessage(SEND_SUCCEED);
			}

			@Override
			public void onError(WeiboException e) {
				String error = e.getMessage();
				m.what = SEND_ERROR;
				m.obj = error;
				handler.sendMessage(m);
			}

			@Override
			public void onIOException(IOException arg0) {
				String error = arg0.getMessage();
				m.what = SEND_ERROR;
				m.obj = error;
				handler.sendMessage(m);
			}

		};
	}

	// 关注某人
	private void follow(int uid) {
		WeiboParameters params = new WeiboParameters();
		params.add("access_token", Main.accessToken.getToken());
		params.add("uid", uid);
		String url = "https://api.weibo.com/2/friendships/create.json";
		try {
			AsyncWeiboRunner.request(url, params, "POST",
					new RequestListener() {
						@Override
						public void onComplete(String arg0) {
							Log.v("Music Share DUBUG", "followed");
						}

						@Override
						public void onError(WeiboException arg0) {
						}

						@Override
						public void onIOException(IOException arg0) {
						}
					});
		} catch (Exception e) {
		}
		// 既然关注就悄悄地进行不报错了
	}

	public class AuthDialogListener implements WeiboAuthListener {
		Message m = handler.obtainMessage();

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			Main.accessToken = new Oauth2AccessToken(token, expires_in);
			AccessTokenKeeper.keepAccessToken(applicationContext, Main.accessToken);
			handler.sendEmptyMessage(AUTH_SUCCEED);
			Log.v(DEBUG_TAG, "授权成功，\n AccessToken:" + token);
			SharedPreferences preferences = applicationContext
					.getSharedPreferences("ShareStatus", Context.MODE_PRIVATE);
			String content = preferences.getString("content", null);
			String artworkUrl = preferences.getString("artworkUrl", null);
			boolean willFollow = preferences.getBoolean("willFollow", false);
			Log.v(DEBUG_TAG, "获取状态\n" + content + "\n" + artworkUrl + "\n"
					+ willFollow);
			sendWeibo(content, artworkUrl, willFollow);
		}

		@Override
		public void onCancel() {

		}

		@Override
		public void onError(WeiboDialogError e) {
			String error = e.getMessage();
			m.what = AUTH_ERROR;
			m.obj = error;
			handler.sendMessage(m);
		}

		@Override
		public void onWeiboException(WeiboException e) {
			String error = e.getMessage();
			m.what = AUTH_ERROR;
			m.obj = error;
			handler.sendMessage(m);
		}
	}

}
