package com.paperairplane.music.share.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;

import com.paperairplane.music.share.BuildConfig;
import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.Consts.SNS;
import com.paperairplane.music.share.MusicData;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.SnsHelper;
import com.paperairplane.music.share.utils.MyLogger;
import com.paperairplane.music.share.utils.lastfm.Track;

/**
 * 静态方法工具类
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class Utilities {

	/**
	 * 将integer类型的时间长度格式化
	 * 
	 * @param duration
	 *            int类型的时间长度（ms）
	 * @return 格式化好的时长字符串
	 */
	public static String convertDuration(long duration) {
		int h = (int) duration / (3600 * 1000);
		int m = (int) (duration / (60 * 1000)) % 60;
		int s = (int) (duration % (60 * 1000)) / 1000;
		if (h == 0)
			return String.format(Locale.getDefault(), "%02d:%02d", m, s);
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
		// 我真的在Java里面找到了sprintf,所以抛下JNI不干了
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
	public static MusicData getMusicAndArtworkUrlFromDouban(MusicData music,
			Context context, Handler handler) {
		MyLogger.d(Consts.DEBUG_TAG, "Querying from Douban");
		MusicData data = music;
		String json = getJsonFromDouban(data.getTitle(), data.getArtist(),
				handler);
		if (json == null) {
			data.setMusicUrl(context.getString(R.string.no_music_url_found));
		} else {
			try {
				JSONObject rootObject = new JSONObject(json);
				int count = rootObject.getInt("count");
				if (count == 1) {
					JSONArray contentArray = rootObject.getJSONArray("musics");
					JSONObject item = contentArray.getJSONObject(0);
					data.setMusicUrl(Consts.Url.INFO_REDIRECT
							+ item.getString("id"));
					data.setArtworkNetUrl(item.getString("image"));
					data.setArtist(item.getJSONArray("author").getJSONObject(0)
							.getString("name"));
					data.setTitle(item.getJSONObject("attrs")
							.getString("title"));
					data.setVersion(item.getJSONObject("attrs").getString(
							"version"));
					// 这里,这里,这样就不会有蛋疼的空白错误了
				} else {
					data.setMusicUrl(context
							.getString(R.string.no_music_url_found));
				}
			} catch (JSONException e) {
				MyLogger.e(Consts.DEBUG_TAG, "JSON解析错误");
				e.printStackTrace();
				data.setMusicUrl(context.getString(R.string.no_music_url_found));
			}
		}
		if (data.getArtworkNetUrl() != null) {
			String artworkUrl = data.getArtworkNetUrl().replace("[\"", "")
					.replace("\"]", "");
			data.setArtworkNetUrl(artworkUrl);
		}
		// MyLogger.v(Consts.DEBUG_TAG, info[MUSIC]);
		// MyLogger.v(Consts.DEBUG_TAG, info[ARTWORK]);
		// 加Log的话如果上面那两个值有null就会崩溃……懒得catch
		return data;
	}

	/**
	 * 从Last.Fm查询音乐信息(测试)
	 * 
	 * @param title
	 *            音乐标题
	 * @param artist
	 *            音乐作者
	 * @param context
	 *            用于获取资源的context
	 * @return 包含音乐详情地址、歌手、专辑、单曲or专辑(不适用)、专辑封面的字符串数组
	 */
	public static MusicData getMusicAndArtworkUrlFromLastfm(MusicData music,
			Context context) {
		MusicData data = music;
		MyLogger.d(Consts.DEBUG_TAG, "Querying from Last.fm");
		Collection<Track> results = Track.search(music.getArtist(),
				music.getTitle(), 1, Consts.LASTFM_API_KEY, context);
		if (results.size() == 0) {
			return null;
		}
		Track track = results.iterator().next();
		data.setMusicUrl(track.getUrl());
		// FIXME 天啊Last.fm更查不到这个!
		data.setArtist(track.getArtist());
		data.setArtworkNetUrl(track.getImageURL());
		// FIXME Last.Fm没有这个……
		MyLogger.d(Consts.DEBUG_TAG, "Fetch from Last.fm成功!");
		return data;
	}

	private static InputStream getImageStream(String artworkUrl)
			throws Exception {
		URL url = new URL(artworkUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return conn.getInputStream();
		}
		return null;
	}

	/**
	 * 将图标保存到本地
	 * 
	 * @param bitmap
	 *            图标
	 * @param fileName
	 *            要保存的文件名
	 * @param artworkPath
	 *            文件路径
	 * @throws IOException
	 *             如果保存失败则抛出
	 */
	public static void saveFile(Bitmap bitmap, String fileName,
			String artworkPath) throws IOException {
		File dirFile = new File(artworkPath);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File artwork = new File(artworkPath + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(artwork));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();

	}

	/**
	 * 得到图片文件
	 * 
	 * @param artworkUrl
	 *            图标的URL
	 * @param album
	 *            专辑
	 * @param artist
	 *            艺术家
	 * @param artwork_path
	 *            保存图片的路径
	 * @return 图标文件的路径
	 */
	public static String getArtwork(String artworkUrl, String album,
			String artist, String artwork_path) {
		String fileName = album + "_" + artist + ".jpg";
		if (new File(artwork_path + fileName).exists())
			return fileName;
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(Utilities
					.getImageStream(artworkUrl));
			Utilities.saveFile(bitmap, fileName, artwork_path);
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			MyLogger.e(Consts.DEBUG_TAG, "获取专辑封面失败" + e.getMessage());
			return null;
		}
	}

	// 通过豆瓣API获取音乐信息
	private static String getJsonFromDouban(String title, String artist,
			Handler handler) {
		String json = null;
		HttpResponse httpResponse;
		try {
			String api_url = Consts.Url.API_QUERY
					+ java.net.URLEncoder.encode(
							(title + "+" + artist).replaceAll(" ", "+"),
							"UTF-8");
			HttpUriRequest httpGet = new HttpGet(api_url);
			httpGet.addHeader("User-Agent",
					"Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))");
			HttpClient client = new DefaultHttpClient();
			httpResponse = client.execute(httpGet);
			MyLogger.v(Consts.DEBUG_TAG, "进行的HTTP GET返回状态为"
					+ httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				json = EntityUtils.toString(httpResponse.getEntity());
			} else {
				handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
				json = null;
			}
		} catch (Exception e) {
			handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
			e.printStackTrace();
			json = null;
		}
		return json;

	}

	/**
	 * 发送反馈
	 * 
	 * @param contents
	 *            反馈内容
	 * @param versionCode
	 *            版本号
	 * @param feedbackMean
	 *            反馈意图
	 * @param context
	 *            App上下文
	 * @param handler
	 *            主线程Handler
	 * @return (boolean)是否反馈成功
	 */
	public static boolean sendFeedback(String[] contents, int versionCode,
			int feedbackMean, Context context, Handler handler) {
		StringBuilder device_info = new StringBuilder("\r" + "App Version:");
		device_info.append(versionCode);
		if (feedbackMean == Consts.ShareMeans.OTHERS)
			device_info.append("\r" + "Device Info:" + "\r");
		device_info.append(" Model:" + Build.MODEL + "\r");
		if (feedbackMean == Consts.ShareMeans.OTHERS) {
			device_info.append(" Manufacturer:" + Build.MANUFACTURER + "\r");
			device_info.append(" Product:" + Build.PRODUCT + "\r");
			device_info.append(" SDK Version:" + Build.VERSION.SDK_INT + "\r");
			device_info.append(" Build ID:" + Build.DISPLAY + "\r");
		}
		device_info.append(" Release:" + Build.VERSION.RELEASE + "\r");
		StringBuilder contact_info = new StringBuilder(" Name:");
		contact_info.append(contents[Consts.FeedbackContentsItem.NAME]);
		contact_info.append("\r E-Mail:");
		contact_info.append(contents[Consts.FeedbackContentsItem.EMAIL]);
		HttpPost post = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		MyLogger.v(Consts.DEBUG_TAG, "content is "
				+ contents[Consts.FeedbackContentsItem.CONTENT] + "\r"
				+ "device info is :" + device_info.toString());
		try {
			switch (feedbackMean) {
			case Consts.ShareMeans.OTHERS:
				post = new HttpPost(Consts.Url.FEEDBACK);
				params.add(new BasicNameValuePair("content",
						java.net.URLEncoder.encode(
								contents[Consts.FeedbackContentsItem.CONTENT],
								"UTF-8")));
				params.add(new BasicNameValuePair("device_info",
						java.net.URLEncoder.encode(device_info.toString(),
								"UTF-8")));
				params.add(new BasicNameValuePair("contact_info",
						java.net.URLEncoder.encode(contact_info.toString(),
								"UTF-8")));
				break;
			case Consts.ShareMeans.WEIBO:
				SnsHelper helper = SnsHelper.getInstance();
				helper.sendWeibo(
						Consts.FEEDBACK
								+ contents[Consts.FeedbackContentsItem.CONTENT]
								+ "||" + device_info.toString() + "||"
								+ contact_info.toString(), null, null, null,
						false, SNS.WEIBO);
				return true;
			}
			post.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = new DefaultHttpClient().execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				MyLogger.v(Consts.DEBUG_TAG, "Feedback succeed");
				return true;
			} else
				throw new RuntimeException();

		} catch (Exception e) {
			MyLogger.d(Consts.DEBUG_TAG, "Feedbak failed");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取储存在本地MediaStore的音乐专辑封面
	 * 
	 * @param context
	 *            App上下文
	 * @param album_id
	 *            图片的专辑ID
	 * @param w
	 *            图片宽度
	 * @param h
	 *            图片高度
	 * @return 获取到的BitMap实例
	 */
	public static Bitmap getLocalArtwork(Context context, long album_id, int w,
			int h) {
		// 从Music App直接拽出来
		// NOTE: There is in fact a 1 pixel border on the right side in the
		// ImageView
		// used to display this drawable. Take it into account now, so we don't
		// have to
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
				BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),
						null, sBitmapOptionsCache);
				int nextWidth = sBitmapOptionsCache.outWidth >> 1;
				int nextHeight = sBitmapOptionsCache.outHeight >> 1;
				while (nextWidth > w && nextHeight > h) {
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
					if (sBitmapOptionsCache.outWidth != w
							|| sBitmapOptionsCache.outHeight != h) {
						Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
						// Bitmap.createScaledBitmap() can return the same
						// bitmap
						if (tmp != b)
							b.recycle();
						b = tmp;
					}
				}
				MyLogger.v(Consts.DEBUG_TAG,
						"方法Utilities.getLocalArtwork返回Bitmap");
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

	/**
	 * 构造方法(不能使用)
	 * 
	 * @throws Exception
	 *             无论何时调用
	 */
	@Deprecated
	public Utilities() throws Exception {
		throw new Exception("What the hell?You cannot do that.");
	}

	/**
	 * 获取应用显示的大小
	 * 
	 * @param activity
	 *            Main Activity
	 * @return 尺寸
	 */
	public static int getAdaptedSize(Activity activity) {
		int size;
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		size = metrics.widthPixels / 10 * 6;
		return size;
	}

	/**
	 * 检查更新
	 * 
	 * @param versionCode
	 *            当前版本号
	 * @param handler
	 *            主线程Handler
	 * @param context
	 *            App上下文
	 * @param currentLocale
	 *            当前区域
	 */
	public static void checkForUpdate(final int versionCode,
			final Handler handler, final Context context,
			final Locale currentLocale) {
		MyLogger.v(Consts.DEBUG_TAG, "方法checkForUpdate被调用");
		new Thread(new Runnable() {
			@Override
			public void run() {
				update(handler, versionCode, context, currentLocale);
			}
		}).start();

	}

	private static void update(Handler handler, int versionCode,
			Context context, Locale currentLocale) {
		String json = null;
		HttpResponse httpResponse;
		try {
			HttpGet httpGet;
			if (!BuildConfig.DEBUG) {
				httpGet = new HttpGet(Consts.Url.CHECK_UPDATE);
			} else {
				httpGet = new HttpGet(Consts.Url.CHECK_TEST_UPDATE);
			}
			httpResponse = new DefaultHttpClient().execute(httpGet);
			MyLogger.v(Consts.DEBUG_TAG, "进行的HTTP GET返回状态为"
					+ httpResponse.getStatusLine().getStatusCode());
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(httpResponse.getEntity());
				MyLogger.v("update", "返回的JSON是:" + json);
			} else {
				json = null;
				handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
				return;
			}
		} catch (Exception e) {
			MyLogger.v(Consts.DEBUG_TAG, "抛出错误" + e.getClass() + ":" + e.getMessage());
			handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
			// e.printStackTrace();
			json = null;
			return;
		}
		try {
			JSONObject rootObject = new JSONObject(json);
			int remoteVersion = rootObject.getInt("versionCode");
			if (remoteVersion <= versionCode) {
				handler.sendEmptyMessage(Consts.Status.NO_UPDATE);
			} else if (remoteVersion > versionCode) {
				StringBuffer sb = new StringBuffer(
						context.getString(R.string.update_remote_version));
				sb.append(rootObject.getString("versionName") + "\n");
				sb.append(context.getString(R.string.update_whats_new));
				if (currentLocale.equals(Locale.SIMPLIFIED_CHINESE)) {
					sb.append(rootObject.getString("whatsNewZh") + "\n");
				} else {
					sb.append(rootObject.getString("whatsNew") + "\n");
				}
				sb.append(context.getString(R.string.update_release_date));
				sb.append(rootObject.getString("releaseDate"));
				String[] info = new String[2];
				info[Consts.ArraySubscript.UPDATE_INFO] = sb.toString();
				info[Consts.ArraySubscript.DOWNLOAD_URL] = rootObject
						.getString("downloadUrl");
				Message m = handler.obtainMessage(Consts.Status.HAS_UPDATE,
						info);
				handler.sendMessage(m);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			handler.sendEmptyMessage(Consts.Status.INTERNET_ERROR);
		}
	}
}
