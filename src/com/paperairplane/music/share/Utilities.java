package com.paperairplane.music.share;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

class Utilities {

	/**
	 * 将integer类型的时间长度格式化
	 * 
	 * @param _duration
	 *            int类型的时间长度（ms）
	 * @return 格式化好的时长字符串
	 */
	public static String convertDuration(long _duration) {
		/*
		 * _duration /= 1000; String min, hour, sec; if (_duration / 3600 > 0) {
		 * return (((hour = ((Integer) (_duration / 3600)).toString()) .length()
		 * == 1) ? "0" + hour : hour) + ":" + (((min = ((Integer) (_duration /
		 * 60)).toString()) .length() == 1) ? "0" + min : min) + ":" + (((sec =
		 * ((Integer) (_duration % 60)).toString()) .length() == 1) ? "0" + sec
		 * : sec); } else { return (((min = ((Integer) (_duration /
		 * 60)).toString()).length() == 1) ? "0" + min : min) + ":" + (((sec =
		 * ((Integer) (_duration % 60)).toString()) .length() == 1) ? "0" + sec
		 * : sec); }
		 */
		StringBuffer sb = new StringBuffer();
		long m = _duration / (60 * 1000);
		sb.append(m < 10 ? "0" + m : m);
		sb.append(":");
		long s = (_duration % (60 * 1000)) / 1000;
		sb.append(s < 10 ? "0" + s : s);
		return sb.toString();
		// 嗯,直接用人家的方法了,嘿
	}

	/**
	 * 通过豆瓣API获取音乐的信息
	 * 
	 * @param title
	 *            音乐标题
	 * @param artist
	 *            音乐作者
	 * @param context
	 *            用于获取资源的context
	 * @param handler
	 *            用于控制UI线程的Handler
	 * @return 包含音乐详情地址、歌手、专辑、单曲or专辑、专辑封面的字符串数组
	 */
	public static String[] getMusicAndArtworkUrl(String title, String artist,
			Context context, Handler handler) {
		Log.v(Consts.DEBUG_TAG, "方法 getMusicAndArtworkUrl被调用");
		String json = getJson(title, artist, handler);
		String info[] = new String[5];
		if (json == null) {
			info[Consts.ArraySubscript.MUSIC] = context
					.getString(R.string.no_music_url_found);
			Log.v(Consts.DEBUG_TAG, "方法 getMusicAndArtworkUrl获得空的json字符串");
		} else {
			try {
				JSONObject rootObject = new JSONObject(json);
				int count = rootObject.getInt("count");
				if (count == 1) {
					JSONArray contentArray = rootObject.getJSONArray("musics");
					JSONObject item = contentArray.getJSONObject(0);
					info[Consts.ArraySubscript.MUSIC] = item
							.getString("mobile_link");
					info[Consts.ArraySubscript.ARTWORK] = item
							.getString("image");
					info[Consts.ArraySubscript.ARTIST] = item
							.getJSONArray("author").getJSONObject(0)
							.getString("name");
					info[Consts.ArraySubscript.ALBUM] = item.getJSONObject(
							"attrs").getString("title");
					info[Consts.ArraySubscript.VERSION] = item.getJSONObject(
							"attrs").getString("version");
					// 这里,这里,这样就不会有蛋疼的空白错误了
				} else {
					info[Consts.ArraySubscript.MUSIC] = context
							.getString(R.string.no_music_url_found);
					info[Consts.ArraySubscript.ARTWORK] = null;
					info[Consts.ArraySubscript.ARTIST] = null;
					info[Consts.ArraySubscript.ALBUM] = null;
					info[Consts.ArraySubscript.VERSION] = null;
				}
			} catch (JSONException e) {
				Log.e(Consts.DEBUG_TAG, "JSON解析错误");
				e.printStackTrace();
				info[Consts.ArraySubscript.MUSIC] = context
						.getString(R.string.no_music_url_found);
				info[Consts.ArraySubscript.ARTWORK] = null;
				info[Consts.ArraySubscript.ARTIST] = null;
				info[Consts.ArraySubscript.ALBUM] = null;
				info[Consts.ArraySubscript.VERSION] = null;
			}
		}
		if (info[Consts.ArraySubscript.ALBUM] != null) {
			info[Consts.ArraySubscript.ALBUM] = info[Consts.ArraySubscript.ALBUM]
					.replace("[\"", "").replace("\"]", "");
			Log.d(Consts.DEBUG_TAG, info[Consts.ArraySubscript.ALBUM]);
		}
		// Log.v(Consts.DEBUG_TAG, info[MUSIC]);
		// Log.v(Consts.DEBUG_TAG, info[ARTWORK]);
		// 加Log的话如果上面那两个值有null就会崩溃……懒得catch
		return info;
	}

	public static InputStream getImageStream(String artwork_url)
			throws Exception {
		URL url = new URL(artwork_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return conn.getInputStream();
		}
		return null;
	}

	public static void saveFile(Bitmap bitmap, String fileName,
			String artwork_path) throws IOException {
		File dirFile = new File(artwork_path);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File artwork = new File(artwork_path + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(artwork));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();

	}

	public static String getArtwork(String artwork_url, String title,
			String artwork_path) {
		String fileName = title + ".jpg";
		if (new File(artwork_path + fileName).exists())
			return fileName;
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(Utilities
					.getImageStream(artwork_url));
			Utilities.saveFile(bitmap, fileName, artwork_path);
			Log.v(Consts.DEBUG_TAG, "获取专辑封面成功");
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(Consts.DEBUG_TAG, "获取专辑封面失败" + e.getMessage());
			return null;
		}
	}

	// 通过豆瓣API获取音乐信息
	private static String getJson(String title, String artist, Handler handler) {
		Log.v(Consts.DEBUG_TAG, "方法 getJSON被调用");
		String json = null;
		HttpResponse httpResponse;
		try {
			String api_url = Consts.API_URL
					+ java.net.URLEncoder.encode(title + "+" + artist, "UTF-8");
			Log.v(Consts.DEBUG_TAG, "方法 getJSON将要进行的请求为" + api_url);
			HttpGet httpGet = new HttpGet(api_url);

			httpResponse = new DefaultHttpClient().execute(httpGet);
			Log.v(Consts.DEBUG_TAG, "进行的HTTP GET返回状态为"
					+ httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				Log.v(Consts.DEBUG_TAG, "返回结果为" + json);
			} else {
				handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
				json = null;
			}
		} catch (Exception e) {
			Log.v(Consts.DEBUG_TAG, "抛出错误" + e.getMessage());
			handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
			e.printStackTrace();
			json = null;
		}
		return json;

	}

	public static boolean sendFeedback(String content) {
		StringBuffer feedback = new StringBuffer(content);
		feedback.append(" Model:" + Build.MODEL);
		feedback.append(" Manufacturer:" + Build.MANUFACTURER);
		feedback.append(" Product:" + Build.PRODUCT);
		feedback.append(" SDK Version" + Build.VERSION.SDK_INT);
		feedback.append(" System Codename" + Build.VERSION.CODENAME);
		HttpPost post = new HttpPost(Consts.FEEDBACK_URL);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Log.v(Consts.DEBUG_TAG, "content is " + content);
		try {
			params.add(new BasicNameValuePair("content", java.net.URLEncoder
					.encode(content, "UTF-8")));
			Log.v(Consts.DEBUG_TAG, "param is " + params.toString());
			post.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = new DefaultHttpClient().execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				Log.v(Consts.DEBUG_TAG, "Feedback succeed");
				return true;
			} else
				throw new RuntimeException();
		} catch (Exception e) {
			Log.d(Consts.DEBUG_TAG, "Feedbak failed");
			e.printStackTrace();
			return false;
		}
	}
	
    public static Bitmap getLocalArtwork(Context context, long album_id, int w, int h) {
    	//从Music App直接拽出来
        // NOTE: There is in fact a 1 pixel border on the right side in the ImageView
        // used to display this drawable. Take it into account now, so we don't have to
        // scale later.
        BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Consts.ARTWORK_URI, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;
                
                // Compute the closest power-of-two scale factor 
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
                
                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

	public Utilities() throws Exception{
		throw new Exception("What the hell?You cannot do that.");
	}
}
