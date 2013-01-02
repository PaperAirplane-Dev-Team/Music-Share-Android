package com.paperairplane.music.share;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;
import cn.jpush.android.api.JPushInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
//import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	// 存储音乐信息
	private MusicData[] musics;// 保存音乐数据
	private ListView listview;// 列表对象
	private Intent musicIntent;
	private String[] media_info = new String[] { MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM };
	private int PLAY = 0, PAUSE = 1, STOP = 2, nowPlaying;
	final private int INTERNET_ERROR = 3, SEND_WEIBO = 4, SEND_SUCCEED = 5,
			AUTH_ERROR = 6, SEND_ERROR = 7, NOT_AUTHORIZED_ERROR = 8,
			AUTH_SUCCEED = 9;
	final private int WEIBO = 10, OTHERS = 11;
	final private int HARRY_UID = 1689129907, XAVIER_UID = 2121014783,
			APP_UID = 1153267341;
	private boolean isPlaying = false;
	private static String APP_KEY = "1006183120";
	private static String REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
	public static Oauth2AccessToken accessToken = null;
	private Weibo weibo = Weibo.getInstance(APP_KEY, REDIRECT_URI);
	private final static String ARTWORK_PATH = Environment
			.getExternalStorageDirectory() + "/music_share/";
	// fatal error哈哈谁让你打错
	private final static String DEBUG_TAG = "Music Share DEBUG";
	private StatusesAPI api = null;

	// 已精简
	@Override
	// 主体
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.main);
			listview = (ListView) findViewById(R.id.list);// 找ListView的ID
			listview.setOnItemClickListener(new MusicListOnClickListener());// 创建一个ListView监听器对象
			listview.setEmptyView(findViewById(R.id.empty));
			showMusicList();
			Log.v(DEBUG_TAG, "Push Start");
			JPushInterface.init(getApplicationContext());
		} catch (Exception e) {
			setContentView(R.layout.empty);
		}
		// 读取已存储的授权信息
		try {
			Main.accessToken = AccessTokenKeeper.readAccessToken(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void btn_empty(View v) {
		refreshMusicList();
	}

	@Override
	// 构建菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	// 菜单判断
	public boolean onOptionsItemSelected(MenuItem menu) {
		super.onOptionsItemSelected(menu);
		switch (menu.getItemId()) {
		case R.id.menu_exit:
			musicIntent = new Intent();
			musicIntent.setAction("com.paperairplane.music.share.PLAYMUSIC");
			stopService(musicIntent);
			finish();
			System.exit(0);
			break;
		case R.id.menu_about:
			showAbout();
			break;
		case R.id.menu_unauth:
			// 判断是否有已授权
			if (Main.accessToken == null) {
				handler.sendEmptyMessage(NOT_AUTHORIZED_ERROR);
			} else {
				Main.accessToken = null;
				AccessTokenKeeper.clear(Main.this);
				Toast.makeText(Main.this, getString(R.string.unauthed),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.menu_refresh:
			refreshMusicList();
			showMusicList();
			break;
		}
		return true;
	}

	// 对话框处理

	public Dialog onCreateDialog(final int _id) {
		if (_id == R.layout.about) { // 这个是关于窗口
			// 对话框尤其奇葩
			// 现在还凑合……RelativeLayout是个好东西
			View about = LayoutInflater.from(this)
					.inflate(R.layout.about, null);
			Button button_about = (Button) about
					.findViewById(R.id.button_about);
			button_about.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					removeDialog(R.layout.about);
				}
			});
			Button button_contact = (Button) about
					.findViewById(R.id.button_contact);
			button_contact.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Uri uri = Uri.parse(getString(R.string.url));
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}

			});
			return new AlertDialog.Builder(this).setView(about).create();
		} else if (_id <= 65535) { // 这个是播放音乐
			return new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(getString(R.string.choose_an_operation))
					.setPositiveButton(getString(R.string.play),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									playMusic(_id);
								}
							})
					.setNegativeButton(getString(R.string.share2others),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									shareMusic(_id, OTHERS);
								}
							})
					.setNeutralButton(getString(R.string.share2weibo),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									shareMusic(_id, WEIBO);

								}
							}).create();
		} else {
			int newid = _id - 65535;
			if (!isPlaying || nowPlaying != newid) {
				try {
					musicIntent = new Intent();
					musicIntent
							.setAction("com.paperairplane.music.share.PLAYMUSIC");
					Bundle bundle = new Bundle();
					bundle.putString("path", musics[newid].getPath());
					bundle.putInt("op", PLAY);
					musicIntent.putExtras(bundle);
					startService(musicIntent);
					isPlaying = true;
					nowPlaying = newid;
				} catch (Exception e) {
				}
			}
			final View dialogView = LayoutInflater.from(this).inflate(
					R.layout.player, null);
			final TextView tvTitle = (TextView) dialogView
					.findViewById(R.id.text_player_title);
			final TextView tvSinger = (TextView) dialogView
					.findViewById(R.id.text_player_singer);
			tvTitle.setText(musics[newid].getTitle() + "("
					+ musics[newid].getDuration() + ")"
					+ getString(R.string.very_long));
			tvSinger.setText(musics[newid].getArtist()
					+ getString(R.string.very_long));
			final Button btnPP = (Button) dialogView
					.findViewById(R.id.button_player_pause);
			final Button btnRT = (Button) dialogView
					.findViewById(R.id.button_player_return);
			btnPP.setBackgroundDrawable(getResources().getDrawable(
					android.R.drawable.ic_media_pause));
			btnRT.setBackgroundDrawable(getResources().getDrawable(
					android.R.drawable.ic_delete));
			btnPP.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (isPlaying == true) {
						musicIntent = new Intent();
						musicIntent
								.setAction("com.paperairplane.music.share.PLAYMUSIC");
						Bundle bundle = new Bundle();
						bundle.putInt("op", PAUSE);
						musicIntent.putExtras(bundle);
						startService(musicIntent);
						btnPP.setBackgroundDrawable(getResources().getDrawable(
								android.R.drawable.ic_media_play));
						isPlaying = false;
					} else if (isPlaying == false) {
						musicIntent = new Intent();
						musicIntent
								.setAction("com.paperairplane.music.share.PLAYMUSIC");
						Bundle bundle = new Bundle();
						bundle.putInt("op", PLAY);
						musicIntent.putExtras(bundle);
						startService(musicIntent);
						// mediaPlayer.start();
						btnPP.setBackgroundDrawable(getResources().getDrawable(
								android.R.drawable.ic_media_pause));
						isPlaying = true;
					}
				}
			});
			btnRT.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					musicIntent = new Intent();
					musicIntent
							.setAction("com.paperairplane.music.share.PLAYMUSIC");
					Bundle bundle = new Bundle();
					bundle.putInt("op", STOP);
					musicIntent.putExtras(bundle);
					startService(musicIntent);
					removeDialog(_id);
					isPlaying = false;
				}
			});
			return new AlertDialog.Builder(this).setView(dialogView)
					.setCancelable(true).show();
		}

	}

	// 列表点击监听类
	public class MusicListOnClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			try {
				dismissDialog(position);
			} catch (Exception e) {
			}
			showDialog(position);
		}
	}

	// 音乐列表
	private void showMusicList() {

		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_info,
				MediaStore.Audio.Media.SIZE + ">='" + 15000 + "'", null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();
		musics = new MusicData[cursor.getCount()];
		for (int i = 0; i < cursor.getCount(); i++) {
			musics[i] = new MusicData();
			musics[i].setTitle(cursor.getString(0));
			musics[i].setDuration(convertDuration(cursor.getInt(1)));
			musics[i].setArtist(cursor.getString(2));
			musics[i].setPath(cursor.getString(3));
			musics[i].setAlbum(cursor.getString(4));
			cursor.moveToNext();
		}
		listview.setAdapter(new MusicListAdapter(this, musics));

	}

	// 转换该死的Duration
	// 话说你能不能优化一下这个,看着头晕啊
	// 挺好的，简洁易懂……
	private String convertDuration(int _duration) {
		String duration = "";
		_duration /= 1000;
		String hour = ((Integer) (_duration / 3600)).toString();
		String min = ((Integer) (_duration / 60)).toString();
		String sec = ((Integer) (_duration % 60)).toString();
		if (hour.length() == 1)
			hour = "0" + hour;
		if (hour.equals("0") || hour.equals("00"))
			hour = "";
		if (min.length() == 1)
			min = "0" + min;
		if (sec.length() == 1)
			sec = "0" + sec;
		if (hour.length() != 0)
			duration = hour + ":" + min + ":" + sec;
		if (hour.length() == 0)
			duration = min + ":" + sec;
		return duration;
	}

	// 分享音乐
	private void shareMusic(int position, int means) {
		QueryAndShareMusicInfo query = new QueryAndShareMusicInfo(position,
				means);
		query.start();
		Toast.makeText(this, getString(R.string.querying), Toast.LENGTH_LONG)
				.show();

	}

	// 播放音乐
	private void playMusic(int position) {
		try {
			dismissDialog(position + 65535);
		} catch (Exception e) {
		}
		showDialog(position + 65535);
	}

	// 刷新音乐列表
	private void refreshMusicList() {
		try {
			IntentFilter filter = new IntentFilter(
					Intent.ACTION_MEDIA_SCANNER_STARTED);
			filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			filter.addDataScheme("file");
			Receiver receiver = new Receiver();
			registerReceiver(receiver, filter);
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory()
									.getAbsolutePath())));
		} catch (Exception e) {
			setContentView(R.layout.empty);
		}
	}

	private void showAbout() { // 显示关于窗口
		showDialog(R.layout.about);// 那么,这个没啥用,只是告诉系统一个标识,在onCreateDialog里面判断一下的
	}

	// 查询+分享线程
	class QueryAndShareMusicInfo extends Thread {
		private int id, means;

		public void run() {
			// 获取信息生成字符串
			String content = getString(R.string.music_title) + "：【"
					+ musics[id].getTitle() + "】"
					+ getString(R.string.music_artist) + "：【"
					+ musics[id].getArtist() + "】"
					+ getString(R.string.music_album) + "：【"
					+ musics[id].getAlbum() + "】"
					+ getString(R.string.music_url) + "：【" + getMusicUrl(id)
					+ "】(" + getString(R.string.share_by) + "："
					+ getString(R.string.app_name) + "||"
					+ getString(R.string.about_download_info) + ":"
					+ getString(R.string.url) + " )";
			String _artworkurl = getArtworkUrl(id);
			String artworkurl = null;
			ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = connectivityManager
					.getActiveNetworkInfo();
			boolean isWifi = activeNetInfo != null
					&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
			if (_artworkurl != null && isWifi) {
				artworkurl = _artworkurl.replace("spic", "lpic");
			} else
				artworkurl = _artworkurl;
			String fileName = getArtwork(artworkurl, id);
			Bundle bundle = new Bundle();
			bundle.putString("content", content);
			if (fileName != null) {
				String fileDir = ARTWORK_PATH + fileName;
				bundle.putString("fileDir", fileDir);
			} else {
				bundle.putString("fileDir", null);
			}
			Log.e(DEBUG_TAG, "获取结束。");
			switch (means) {
			// 根据分享方式执行操作
			case OTHERS:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.app_name));
				intent.putExtra(Intent.EXTRA_TEXT, content);
				startActivity(Intent.createChooser(intent,
						getString(R.string.how_to_share)));
				break;
			case WEIBO:
				Message m = handler.obtainMessage(SEND_WEIBO, bundle);
				handler.sendMessage(m);
				break;
			}

		}

		// 获取音乐地址
		private String getMusicUrl(int position) {
			Log.v(DEBUG_TAG, "方法 getMusicUrl被调用,歌曲编号为" + position);
			String json = getJson(position);
			String music_url = null;
			if (json == null) {
				music_url = getString(R.string.no_music_url_found);
				Log.v(DEBUG_TAG, "方法 getMusicUrl获得空的json字符串");
			} else {
				try {
					JSONObject rootObject = new JSONObject(json);
					int count = rootObject.getInt("count");
					if (count == 1) {
						JSONArray contentArray = rootObject
								.getJSONArray("musics");
						JSONObject item = contentArray.getJSONObject(0);
						music_url = item.getString("mobile_link");
					} else {
						music_url = getString(R.string.no_music_url_found);
					}
				} catch (JSONException e) {
					music_url = getString(R.string.no_music_url_found);
				}
			}
			Log.v(DEBUG_TAG, music_url);
			return music_url;
		}

		private String getArtworkUrl(int position) {
			Log.v(DEBUG_TAG, "方法 getArtwork被调用,歌曲编号为" + position);
			String json = getJson(position);
			String artwork_url = null;
			if (json == null) {
				artwork_url = getString(R.string.no_music_url_found);
				Log.v(DEBUG_TAG, "方法 getArtwork获得空的json字符串");
			} else {
				try {
					JSONObject rootObject = new JSONObject(json);
					int count = rootObject.getInt("count");
					if (count == 1) {
						JSONArray contentArray = rootObject
								.getJSONArray("musics");
						JSONObject item = contentArray.getJSONObject(0);
						artwork_url = item.getString("image");
					} else {
						artwork_url = null;
					}
				} catch (JSONException e) {
					artwork_url = null;
				}
			}
			Log.v(DEBUG_TAG, artwork_url);
			return artwork_url;
		}

		private String getArtwork(String artwork_url, int id) {
			try {
				String fileName = musics[id].getTitle() + ".jpg";
				Bitmap bitmap = BitmapFactory
						.decodeStream(getImageStream(artwork_url));
				saveFile(bitmap, fileName);
				Log.v(DEBUG_TAG, "获取专辑封面成功");
				return fileName;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(DEBUG_TAG, "获取专辑封面失败" + e.getMessage());
				return null;
			}
		}

		private void saveFile(Bitmap bitmap, String fileName)
				throws IOException {
			File dirFile = new File(ARTWORK_PATH);
			if (!dirFile.exists()) {
				dirFile.mkdir();
			}
			File artwork = new File(ARTWORK_PATH + fileName);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(artwork));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();
			bos.close();

		}

		private InputStream getImageStream(String artwork_url) throws Exception {
			URL url = new URL(artwork_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return conn.getInputStream();
			}
			return null;
		}

		// 通过豆瓣API获取音乐信息
		private String getJson(int position) {
			Log.v(DEBUG_TAG, "方法 getJSON被调用,歌曲编号为" + position);
			String api_url = "https://api.douban.com/v2/music/search?count=1&q="
					+ java.net.URLEncoder.encode(musics[position].getTitle()
							+ "+" + musics[position].getArtist());
			Log.v(DEBUG_TAG, "方法 getJSON将要进行的请求为" + api_url);
			String json = null;
			HttpResponse httpResponse;
			HttpGet httpGet = new HttpGet(api_url);
			try {
				httpResponse = new DefaultHttpClient().execute(httpGet);
				Log.v(DEBUG_TAG, "进行的HTTP GET返回状态为"
						+ httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					json = EntityUtils.toString(httpResponse.getEntity());
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

		public QueryAndShareMusicInfo(int _id, int _means) {
			id = _id;
			means = _means;
		}
	}

	// 这是消息处理
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INTERNET_ERROR:// 网络错误
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_internet), Toast.LENGTH_SHORT)
						.show();
				break;
			case SEND_WEIBO:// 发送微博
				View sendweibo = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.sendweibo, null);
				final EditText et = (EditText) sendweibo.getRootView()
						.findViewById(R.id.et_content);
				final CheckBox cb = (CheckBox) sendweibo
						.findViewById(R.id.cb_follow);
				Bundle bundle = (Bundle) msg.obj;
				String _content = bundle.getString("content");
				final String fileDir = bundle.getString("fileDir");
				et.setText(_content);
				new AlertDialog.Builder(Main.this)
						.setView(sendweibo)
						.setPositiveButton(getString(R.string.share),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String content = et.getText()
												.toString();
										if (calculateLength(content) > 140) {// 判断字数是否超过140
											Log.v(DEBUG_TAG, "超出字数");
											new AlertDialog.Builder(Main.this)
													.setMessage(
															getString(R.string.too_long))
													.setPositiveButton(
															getString(android.R.string.ok),
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int which) {
																}
															}).show();
										} else if (Main.accessToken == null
												|| (Main.accessToken
														.isSessionValid() == false)) {// 检测之前是否授权过
											handler.sendEmptyMessage(NOT_AUTHORIZED_ERROR);
											weibo.authorize(Main.this,
													new AuthDialogListener());// 授权
										} else {
											sendWeibo(content, fileDir,
													cb.isChecked());
										}

									}
								}).show();
				Log.e(DEBUG_TAG, "弹出对话框");
				break;
			case SEND_SUCCEED:// 发送成功
				Toast.makeText(Main.this, R.string.send_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case NOT_AUTHORIZED_ERROR:// 尚未授权
				Toast.makeText(Main.this, R.string.not_authorized_error,
						Toast.LENGTH_SHORT).show();
				break;
			case AUTH_ERROR:// 授权错误
				Toast.makeText(Main.this,
						R.string.auth_error + (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				Log.e(DEBUG_TAG, "错误" + (String) msg.obj);
				break;
			case SEND_ERROR:// 发送错误
				Toast.makeText(Main.this,
						R.string.send_error + (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				Log.e(DEBUG_TAG, "错误" + (String) msg.obj);
				break;
			case AUTH_SUCCEED:// 授权成功
				Toast.makeText(Main.this, R.string.auth_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	// 发送微博
	private void sendWeibo(String content, String fileDir, boolean willFollow) {
		api = new StatusesAPI(Main.accessToken);
		if (fileDir == null) {
			api.update(content, null, null, requestListener);
		} else {
			api.upload(content, fileDir, null, null, requestListener);
		}
		if (willFollow == true) {// 判断是否要关注开发者
			follow(HARRY_UID);// 关注Harry Chen
			follow(XAVIER_UID);// 关注Xavier Yao
			follow(APP_UID);// 关注官方微博

		}
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

	// 计算微博字数（我感觉没必要……反正超字数就会抛出WeiboException）
	private long calculateLength(CharSequence c) {
		double len = 0;
		for (int i = 0; i < c.length(); i++) {
			int tmp = (int) c.charAt(i);
			if (tmp > 0 && tmp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
	}

	// 微博授权监听器
	class AuthDialogListener implements WeiboAuthListener {
		Message m = handler.obtainMessage();

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			Main.accessToken = new Oauth2AccessToken(token, expires_in);
			AccessTokenKeeper.keepAccessToken(Main.this, accessToken);
			handler.sendEmptyMessage(AUTH_SUCCEED);
			Log.v(DEBUG_TAG, "授权成功，\n AccessToken:" + token);
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

	private RequestListener requestListener = new RequestListener() {
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
/**
 * Paper Airplane Dev Team
 * 添乱1：@author @HarryChen-依旧初三15- http://weibo.com/yszzf
 * 添乱2：@author @姚沛然 http://weibo.com/xavieryao 
 * 美工：@author @七只小鸡1997
 * http://weibo.com/u/1579617160 2013.1.2
 **/
