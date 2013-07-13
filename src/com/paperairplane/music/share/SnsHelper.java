package com.paperairplane.music.share;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.Consts.SNS;
import com.paperairplane.music.share.utils.MyLogger;

/**
 * 所有微博相关
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class SnsHelper {
	private StatusesAPI mApi = null;
	private Handler mHandler = null;
	private Context mContext;
	private Map<SNS, AuthDialogListener> mAuthListeners = new HashMap<SNS, AuthDialogListener>();
	private RequestListener mRequestListener;
	private SharedPreferences mPreferences;
	private Editor mEditor;
	private Map<SNS, Weibo> mSns = new HashMap<SNS, Weibo>();
	private static SnsHelper INSTANCE;

	/**
	 * SnsHelper构造函数
	 * 
	 * @param handler
	 *            用于控制UI线程的Handler
	 * @param context
	 *            用于暂存微博内容和其它信息的(Application)Context
	 */
	private SnsHelper(Handler handler, Context context) {
		mHandler = handler;
		mContext = context;
		mPreferences = mContext.getSharedPreferences(Consts.Preferences.WEIBO,
				Context.MODE_APPEND);
		mEditor = mPreferences.edit();
		initSNS();
	}

	/**
	 * 获取WeiboHandler的单例……我是有多喜欢Singleton模式啊……
	 * 
	 * @param handler
	 *            用于控制UI线程的Handler
	 * @param context
	 *            用于暂存微博内容和其它信息的(Application)Context
	 * @return SnsHelper单例
	 */
	public static SnsHelper getInstance(Handler handler, Context context) {
		if (INSTANCE == null) {
			INSTANCE = new SnsHelper(handler, context);
		}
		return INSTANCE;
	}

	public static SnsHelper getInstance() {
		if (INSTANCE == null) {
			throw new UnsupportedOperationException(
					"You should use SnsHelper::getInstance(Handler,Context) to init the instance first.");
		} else {
			return INSTANCE;
		}
	}

	/**
	 * 初始化SNS组件列表
	 */
	private void initSNS() {
		Weibo weibo = new Weibo(Consts.WEIBO_APP_KEY, Consts.Url.AUTH_REDIRECT,
				Consts.Url.WEIBO_AUTH_URL);
		weibo.accessToken = readAccessToken(SNS.WEIBO);
		mSns.put(SNS.WEIBO, weibo);
		Weibo renren = new Weibo(Consts.RENREN_APP_KEY,
				Consts.Url.RENREN_REDIRECT_URI, Consts.Url.RENREN_AUTH_URL,
				Consts.RENREN_SCOPE);
		renren.accessToken = readAccessToken(SNS.RENREN);
		mSns.put(SNS.RENREN, renren);
	}

	/**
	 * 获取微博授权监听器实例
	 * 
	 * @return 微博授权监听器
	 */
	public AuthDialogListener getListener(SNS type) {
		AuthDialogListener listener = mAuthListeners.get(type);
		if (listener == null) {
			listener = new AuthDialogListener(type);
			mAuthListeners.put(type, listener);
		}
		return listener;
	}

	public Weibo getWeibo(SNS type) {
		return mSns.get(type);
	}

	/**
	 * 检查AccessToken的存在及合法性
	 * 
	 * @return (布尔值)是否合法
	 */
	public boolean isAccessTokenExistAndValid(SNS type) {
		Weibo weibo = mSns.get(type);
		if (weibo == null) {
			return false;
		}
		Oauth2AccessToken token = weibo.accessToken;
		if (token == null || token.isSessionValid() == false) {
			return false;
		}
		return true;
	}

	/**
	 * 发送微博
	 * 
	 * @param content
	 *            要发送的微博内容
	 * @param artworkUrl
	 *            要发送的图片
	 * @param fileName
	 *            本地图标文件路径
	 * @param willFollow
	 *            是否要关注
	 */

	public void sendWeibo(String content, String artworkUrl, String fileName,
			String annotation, boolean willFollow, SNS type) {
		if (type.equals(SNS.WEIBO)) {
			mApi = new StatusesAPI(mSns.get(SNS.WEIBO).accessToken);
		}
		if (mRequestListener == null) {
			initRequestListener();
		}
		if (artworkUrl == null) {
			MyLogger.v(Consts.DEBUG_TAG, "发送无图微博");
			switch (type) {
			case WEIBO:
				mApi.update(content, null, null, annotation, mRequestListener);
				break;
			case RENREN:
				// TODO
				break;
			}
		} else if (fileName != null) {
			MyLogger.v(Consts.DEBUG_TAG, "发布带本地封面的微博");
			switch (type) {
			case WEIBO:
				mApi.upload(content, fileName, null, null,annotation, mRequestListener);
				break;
			case RENREN:
				// TODO
				break;
			}
		} else {
			switch (type) {
			case WEIBO:
				MyLogger.v(Consts.DEBUG_TAG, "发送带图微博，url=" + artworkUrl);
				String url = "https://mApi.weibo.com/2/statuses/upload_url_text.json";
				WeiboParameters params = new WeiboParameters();
				params.add("access_token",
						mSns.get(type).accessToken.getToken());
				params.add("status", content);
				params.add("url", artworkUrl);
				params.add("annotations",annotation);
				AsyncWeiboRunner.request(url, params, "POST", mRequestListener);
				break;
			case RENREN:
				WeiboParameters rrparams = new WeiboParameters();
				rrparams.add("access_token",
						mSns.get(type).accessToken.getToken());
				rrparams.add("v", "1.0");
				rrparams.add("method", "share.share ");
				rrparams.add("id", 2);
				rrparams.add("url", artworkUrl);
				rrparams.add("comment", content);
				AsyncWeiboRunner.request(Consts.Url.RENREN_API, rrparams,
						"POST", mRequestListener);
				break;
			}

		}
		if (willFollow == true) {// 判断是否要关注开发者
			follow(Consts.WeiboUid.HARRY_UID);// 关注Harry Chen
			follow(Consts.WeiboUid.XAVIER_UID);// 关注Xavier Yao
			follow(Consts.WeiboUid.APP_UID);// 关注官方微博
		}
	}

	private void initRequestListener() {
		mRequestListener = new RequestListener() {
			Message m = mHandler.obtainMessage();

			@Override
			public void onComplete(String arg0) {
				mHandler.sendEmptyMessage(Consts.Status.SEND_SUCCEED);
			}

			@Override
			public void onError(WeiboException e) {
				String error = e.getMessage();
				m.what = Consts.Status.SEND_ERROR;
				m.obj = error;
				mHandler.sendMessage(m);
			}

			@Override
			public void onIOException(IOException arg0) {
				String error = arg0.getMessage();
				m.what = Consts.Status.SEND_ERROR;
				m.obj = error;
				mHandler.sendMessage(m);
			}

		};
	}

	/**
	 * 关注特定微博用户
	 * 
	 * @param uid
	 *            用户的UID
	 */
	private void follow(int uid) {
		WeiboParameters params = new WeiboParameters();
		params.add("access_token", mSns.get(SNS.WEIBO).accessToken.getToken());
		params.add("uid", uid);
		try {
			AsyncWeiboRunner.request(Consts.Url.WEIBO_FOLLOW_API, params,
					"POST", new RequestListener() {
						@Override
						public void onComplete(String response) {
						}

						@Override
						public void onIOException(IOException e) {
						}

						@Override
						public void onError(WeiboException e) {

						}

					});
		} catch (Exception e) {
		}
		// 既然关注就悄悄地进行不报错了
	}

	private void getAndSaveUid(final SNS type) {
		WeiboParameters params = new WeiboParameters();
		params.add("access_token", mSns.get(type).accessToken.getToken());
		String url = null;
		String method = null;
		MyLogger.d(Consts.DEBUG_TAG, "SnsHelper::getAndSaveUid  被调用 ");
		RequestListener listener = new RequestListener() {
			@Override
			public void onComplete(String response) {
				try {
					MyLogger.d(Consts.DEBUG_TAG,
							"SnsHelper::getAndSaveUid  response == " + response);
					JSONObject jobj = new JSONObject(response);
					String uid = jobj.getString("uid");
					mEditor.putString(type.name() + "uid", uid);
					mEditor.commit();
					getNickname(type);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onError(WeiboException e) {
				e.printStackTrace();
			}
		};
		switch (type) {
		case WEIBO:
			url = Consts.Url.WEIBO_ACCOUNT_GET_UID_API;
			method = "GET";
			break;
		case RENREN:
			url = Consts.Url.RENREN_API;
			params.add("v", "1.0");
			params.add("format", "JSON");
			params.add("method", "users.getLoggedInUser");
			method = "POST";
			break;
		}
		AsyncWeiboRunner.request(url, params, method, listener);
	}

	private void getNickname(final SNS type) {
		String uid = mPreferences.getString(type.name() + "uid", "");
		WeiboParameters params = new WeiboParameters();
		params.add("access_token", mSns.get(type).accessToken.getToken());
		params.add("uid", uid);
		String url = null;
		String method = null;
		RequestListener listener = new RequestListener() {
			@Override
			public void onComplete(String response) {
				try {
					JSONObject jobj = new JSONObject(response);
					String name = jobj.getString("name");
					mEditor.putString(type.name() + "name", name);
					mEditor.commit();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e) {

			}

			@Override
			public void onError(WeiboException e) {
			}
		};
		switch (type) {
		case WEIBO:
			url = Consts.Url.WEIBO_USERS_SHOW_API;
			method = "GET";
			break;
		case RENREN:
			url = Consts.Url.RENREN_API;
			params.add("v", "1.0");
			params.add("format", "JSON");
			params.add("method", "users.getProfileInfo");
			method = "POST";
			break;
		}
		AsyncWeiboRunner.request(url, params, method, listener);
	}

	private class AuthDialogListener implements WeiboAuthListener {
		Message m = mHandler.obtainMessage();
		private SNS type;

		private AuthDialogListener(SNS type) {
			this.type = type;
		}

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			Oauth2AccessToken accessToken = new Oauth2AccessToken(token,
					expires_in);
			keepAccessToken(accessToken, type);
			mHandler.sendEmptyMessage(Consts.Status.AUTH_SUCCEED);
			MyLogger.v(Consts.DEBUG_TAG, "授权成功，\n AccessToken:" + token);
			SharedPreferences preferences = mContext.getSharedPreferences(
					"ShareStatus", Context.MODE_PRIVATE);
			getAndSaveUid(type);
			if (preferences.getBoolean("read", false)) {
				String content = preferences.getString("content", null);
				String artworkUrl = preferences.getString("artworkUrl", null);
				String fileName = preferences.getString("fileName", null);
				String annotation = preferences.getString("annotation", null);
				boolean willFollow = preferences
						.getBoolean("willFollow", false);
				sendWeibo(content, artworkUrl, fileName, annotation,
						willFollow, type);
				preferences.edit().putBoolean("read", false).commit();
			}
		}

		@Override
		public void onCancel() {

		}

		@Override
		public void onError(WeiboDialogError e) {
			String error = e.getMessage();
			m.what = Consts.Status.AUTH_ERROR;
			m.obj = error;
			mHandler.sendMessage(m);
		}

		@Override
		public void onWeiboException(WeiboException e) {
			String error = e.getMessage();
			m.what = Consts.Status.AUTH_ERROR;
			m.obj = error;
			mHandler.sendMessage(m);
		}
	}

	private void keepAccessToken(Oauth2AccessToken token, SNS type) {
		mEditor.putString(type.name() + "token", token.getToken());
		mEditor.putLong(type.name() + "expiresTime", token.getExpiresTime());
		mEditor.commit();
	}

	public void clear(SNS type) {
		mEditor.remove(type.name() + "token");
		mEditor.remove(type.name() + "expiresTime");
		mEditor.commit();
	}

	private Oauth2AccessToken readAccessToken(SNS weibo) {
		Oauth2AccessToken token = new Oauth2AccessToken();
		token.setToken(mPreferences.getString(weibo.name() + "token", ""));
		token.setExpiresTime(mPreferences.getLong(weibo.name() + "expiresTime",
				0));
		return token;
	}

	public void authorize(Activity activity, SNS type) {
		switch (type) {
		case WEIBO:
			SsoHandler handler = new SsoHandler(activity, mSns.get(SNS.WEIBO));
			handler.authorize(getListener(SNS.WEIBO));
			break;
		case RENREN:
			mSns.get(SNS.RENREN).authorize(activity, getListener(SNS.RENREN));
			break;
		}

	}

	public void unauthorize(SNS weibo) {
		// TODO Auto-generated method stub

	}

}
