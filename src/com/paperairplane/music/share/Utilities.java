package com.paperairplane.music.share;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

class Utilities {
	private final static String API_URL = "http://paperairplane.sinaapp.com/proxy.php?q=";
	private final static int INTERNET_ERROR = 3;
	private final static String DEBUG_TAG = "Music Share DEBUG";
	final private static int MUSIC = 0, ARTWORK = 1, ARTIST = 2, ALBUM = 3,
			VERSION = 4;

	/**
	 * 将integer类型的时间长度格式化
	 * 
	 * @param _duration int类型的时间长度（ms）
	 * @return 格式化好的时长字符串
	 */
	public static String convertDuration(long _duration) {
		/*_duration /= 1000;
		String min, hour, sec;
		if (_duration / 3600 > 0) {
			return (((hour = ((Integer) (_duration / 3600)).toString())
					.length() == 1) ? "0" + hour : hour)
					+ ":"
					+ (((min = ((Integer) (_duration / 60)).toString())
							.length() == 1) ? "0" + min : min)
					+ ":"
					+ (((sec = ((Integer) (_duration % 60)).toString())
							.length() == 1) ? "0" + sec : sec);
		} else {
			return (((min = ((Integer) (_duration / 60)).toString()).length() == 1) ? "0"
					+ min
					: min)
					+ ":"
					+ (((sec = ((Integer) (_duration % 60)).toString())
							.length() == 1) ? "0" + sec : sec);
		}
		*/
        StringBuffer sb = new StringBuffer();
        long m = _duration / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (_duration % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
        //嗯,直接用人家的方法了,嘿
	}

	/**
	 * 通过豆瓣API获取音乐的信息
	 * 
	 * @param title      音乐标题
	 * @param artist     音乐作者
	 * @param context    用于获取资源的context
	 * @param handler    用于控制UI线程的Handler
	 * @return 包含音乐详情地址、歌手、专辑、单曲or专辑、专辑封面的字符串数组
	 */
	public static String[] getMusicAndArtworkUrl(String title, String artist,
			Context context, Handler handler) {
		Log.v(DEBUG_TAG, "方法 getMusicAndArtworkUrl被调用");
		String json = getJson(title, artist, handler);
		String info[] = new String[5];
		if (json == null) {
			info[MUSIC] = context.getString(R.string.no_music_url_found);
			Log.v(DEBUG_TAG, "方法 getMusicAndArtworkUrl获得空的json字符串");
		} else {
			try {
				JSONObject rootObject = new JSONObject(json);
				int count = rootObject.getInt("count");
				if (count == 1) {
					JSONArray contentArray = rootObject.getJSONArray("musics");
					JSONObject item = contentArray.getJSONObject(0);
					info[MUSIC] = item.getString("mobile_link");
					info[ARTWORK] = item.getString("image");
					info[ARTIST] = item.getJSONArray("author").getJSONObject(0)
							.getString("name");
					info[ALBUM] = item.getJSONObject("attrs")
							.getString("title");
					info[VERSION] = item.getJSONObject("attrs").getString(
							"version");
					// 这里,这里,这样就不会有蛋疼的空白错误了
				} else {
					info[MUSIC] = context
							.getString(R.string.no_music_url_found);
					info[ARTWORK] = null;
					info[ARTIST] = null;
					info[ALBUM] = null;
					info[VERSION] = null;
				}
			} catch (JSONException e) {
				Log.e(DEBUG_TAG, "JSON解析错误");
				e.printStackTrace();
				info[MUSIC] = context.getString(R.string.no_music_url_found);
				info[ARTWORK] = null;
				info[ARTIST] = null;
				info[ALBUM] = null;
				info[VERSION] = null;
			}
		}
		if (info[ALBUM] != null) {
			info[ALBUM] = info[ALBUM].replace("[\"", "").replace("\"]", "");
			Log.d(DEBUG_TAG, info[ALBUM]);
		}
		// Log.v(DEBUG_TAG, info[MUSIC]);
		// Log.v(DEBUG_TAG, info[ARTWORK]);
		// 加Log的话如果上面那两个值有null就会崩溃……懒得catch
		return info;
	}

	// 通过豆瓣API获取音乐信息
	private static String getJson(String title, String artist, Handler handler) {
		Log.v(DEBUG_TAG, "方法 getJSON被调用");
		String json = null;
		HttpResponse httpResponse;
		try {
			String api_url = API_URL
					+ java.net.URLEncoder.encode(title + "+" + artist, "UTF-8");
			Log.v(DEBUG_TAG, "方法 getJSON将要进行的请求为" + api_url);
			HttpGet httpGet = new HttpGet(api_url);

			httpResponse = new DefaultHttpClient().execute(httpGet);
			Log.v(DEBUG_TAG, "进行的HTTP GET返回状态为"
					+ httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				Log.v(DEBUG_TAG, "返回结果为" + json);
			} else {
				handler.sendEmptyMessage(INTERNET_ERROR);
				json = null;
			}
		} catch (Exception e) {
			Log.v(DEBUG_TAG, "抛出错误" + e.getMessage());
			handler.sendEmptyMessage(INTERNET_ERROR);
			e.printStackTrace();
			json = null;
		}
		return json;

	}

}
