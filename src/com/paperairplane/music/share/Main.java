package com.paperairplane.music.share;

import java.io.File;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.sso.SsoHandler;

public class Main extends ListActivity {
	// 存储音乐信息
	private MusicData[] musics;// 保存音乐数据
	private ListView listview;// 列表对象
	public static Oauth2AccessToken accessToken = null;
	private Weibo weibo = Weibo.getInstance(Consts.APP_KEY,
			Consts.Url.AUTH_REDIRECT);
	private Receiver receiver;
	private AlertDialog dialogMain, dialogAbout, dialogSearch, dialogThank,
			dialogWelcome, dialogChangeColor;
	private SsoHandler ssoHandler;
	private WeiboHelper weiboHelper;
	private TextView indexOverlay;
	private View footerButton;
	private static int versionCode, checkForUpdateCount = 0;
	private String versionName;

	@Override
	// 主体
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.main);
			initListView();
			showMusicList();
			firstShow();
			ssoHandler = new SsoHandler(Main.this, weibo);
			weiboHelper = new WeiboHelper(handler, getApplicationContext());
			Main.versionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			this.versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
			setContentView(R.layout.empty);
		}
		// 读取已存储的授权信息
		try {
			Main.accessToken = AccessTokenKeeper
					.readAccessToken(getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Utilities.checkForUpdate(Main.versionCode, handler, Main.this,
				getResources().getConfiguration().locale);
		listview.setBackgroundResource(R.drawable.listview_background);
	}

	private void initListView() {
		indexOverlay = (TextView) View.inflate(Main.this, R.layout.indexer,
				null);
		getWindowManager()
				.addView(
						indexOverlay,
						new WindowManager.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT,
								WindowManager.LayoutParams.TYPE_APPLICATION,
								WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
										| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
								PixelFormat.TRANSLUCENT));
		SharedPreferences preference=getSharedPreferences(Consts.Preferences.GENERAL, MODE_PRIVATE);
		if (preference.contains(Consts.Preferences.BG_COLOR)){
			indexOverlay.setBackgroundColor(android.graphics.Color.parseColor(preference.getString(Consts.Preferences.BG_COLOR, "")));
		}
		listview = (ListView) findViewById(android.R.id.list);// 找LisView的ID
		footerButton = findViewById(R.id.foot_button);
		listview.setOnItemClickListener(new MusicListOnClickListener());// 创建一个ListView监听器对象
		listview.setOnScrollListener(new OnScrollListener() {

			boolean visible;

			@SuppressLint("NewApi")
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				footerButton.setVisibility(View.VISIBLE);
				if (visible) {
					String firstChar = musics[firstVisibleItem].getTitle();
					if (firstChar.toLowerCase(Locale.getDefault()).startsWith(
							"the ")) {
						firstChar = firstChar.substring(4, 5);
					} else if (firstChar.toLowerCase(Locale.getDefault())
							.startsWith("a ")) {
						firstChar = firstChar.substring(2, 3);
					} else if (firstChar.toLowerCase(Locale.getDefault())
							.startsWith("an ")) {
						firstChar = firstChar.substring(3, 4);
					} else {
						firstChar = firstChar.substring(0, 1);
					}
					indexOverlay.setText(firstChar.toUpperCase(Locale
							.getDefault()));
					indexOverlay.setVisibility(View.VISIBLE);
				}
				if (firstVisibleItem == 0
						|| (firstVisibleItem + visibleItemCount) == totalItemCount) {
					indexOverlay.setVisibility(View.INVISIBLE);
					// footerButton.setVisibility(View.GONE);
				}
				if ((firstVisibleItem + visibleItemCount) >= (totalItemCount - 3)
						&& visibleItemCount < totalItemCount) {
					footerButton.setVisibility(View.GONE);
				}
				if (visibleItemCount >= totalItemCount) {
					footerButton.setVisibility(View.VISIBLE);
					footerButton.setTop(android.R.id.list);
					// FIXME 如果歌曲不够一屏幕……仍然叠加就难看死了，这个方法只有API 11以上才可用，求解。
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				visible = true;
				if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
					indexOverlay.setVisibility(View.INVISIBLE);
					// footerButton.setVisibility(View.VISIBLE);
				}
				if (scrollState == ListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
					// footerButton.setVisibility(View.GONE);
				}
			}

		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {
			unregisterReceiver(receiver);
			getWindowManager().removeView(indexOverlay);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.main, menu);
		if (Build.VERSION.SDK_INT >= 11) {
			menu.add(Menu.NONE, Consts.MenuItem.REFRESH, 1,
					R.string.menu_refresh)
					.setIcon(android.R.drawable.ic_popup_sync)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			menu.add(Menu.NONE, Consts.MenuItem.REFRESH, 1,
					R.string.menu_refresh).setIcon(
					android.R.drawable.ic_menu_recent_history);
		}
		if (Main.accessToken == null) {
			menu.add(Menu.NONE, Consts.MenuItem.AUTH, 2, R.string.auth)
					.setIcon(android.R.drawable.ic_menu_add);
		} else {
			menu.add(Menu.NONE, Consts.MenuItem.UNAUTH, 2, R.string.unauth)
					.setIcon(android.R.drawable.ic_menu_delete);
		}
		return true;
	}

	@Override
	// 菜单判断
	public boolean onOptionsItemSelected(MenuItem menu) {
		super.onOptionsItemSelected(menu);
		Log.e(Consts.DEBUG_TAG, "id:" + menu.getItemId());
		switch (menu.getItemId()) {
		case R.id.menu_exit:
			finish();
			System.exit(0);
			break;
		case R.id.menu_about:
			showAbout();
			break;
		case R.id.menu_change_color:
			showCustomDialog(0, Consts.Dialogs.CHANGE_COLOR);
			break;
		case Consts.MenuItem.UNAUTH:
			// 我特意把这里反了过来否则没法用啊
			try {

				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.unauth_confirm)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@SuppressLint("NewApi")
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										Main.accessToken = null;
										AccessTokenKeeper
												.clear(getApplicationContext());
										if (Build.VERSION.SDK_INT > 10) {
											invalidateOptionsMenu();
										}
										Toast.makeText(Main.this,
												getString(R.string.unauthed),
												Toast.LENGTH_SHORT).show();
									}
								})
						.setNegativeButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
									}
								}).show();

			} catch (Exception e) {
				e.printStackTrace();
				Log.v(Consts.DEBUG_TAG, e.getMessage());
			}
			break;
		case Consts.MenuItem.AUTH:
			try {
				ssoHandler.authorize(weiboHelper.getListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case Consts.MenuItem.REFRESH:
			refreshMusicList();
			showMusicList();
			break;
		case R.id.menu_update:
			Main.checkForUpdateCount++;
			Utilities.checkForUpdate(Main.versionCode, handler, Main.this,
					getResources().getConfiguration().locale);
			break;
		}
		return true;
	}

	public void btn_empty(View v) {
		refreshMusicList();
	}

	public void footer(View v) {
		showCustomDialog(0, Consts.Dialogs.SEARCH);
	}

	// 对话框处理

	private void showCustomDialog(final int _id, int whichDialog) {
		switch (whichDialog) {
		case Consts.Dialogs.ABOUT:
			DialogInterface.OnClickListener listenerAbout = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						dialogThank = new AlertDialog.Builder(Main.this)
								.setTitle(R.string.thank_title)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setMessage(R.string.thank_content)
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												dialogThank.cancel();
											}
										}).create();
						dialogThank.show();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						Uri uri = Uri.parse(getString(R.string.url));
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						View feedback = LayoutInflater.from(Main.this).inflate(
								R.layout.feedback, null);
						final EditText content = (EditText) feedback
								.findViewById(R.id.et_feedback);
						DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String contentString = content.getText()
										.toString().trim();
								if (contentString.equals("")) {
									showCustomDialog(0, Consts.Dialogs.EMPTY);
								} else {
									String versionCode = "NameNotFoundException";
									versionCode = Integer
											.toString(Main.versionCode);
									SendFeedback feedback = new SendFeedback(
											contentString, handler,
											versionCode, Main.this);
									switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										feedback.setMeans(Consts.ShareMeans.OTHERS);
										feedback.start();
										break;
									case DialogInterface.BUTTON_NEGATIVE:
										feedback.setMeans(Consts.ShareMeans.WEIBO);
										feedback.start();
										break;
									}
								}
							}
						};
						AlertDialog.Builder builder = new AlertDialog.Builder(
								Main.this).setView(feedback).setPositiveButton(
								R.string.send_feedback, listener);
						if (Main.accessToken != null
								&& (Main.accessToken.isSessionValid() != false)) {
							builder.setNegativeButton(R.string.feedback_weibo,
									listener);
						}
						builder.show();
						break;
					}
				}
			};
			// 既然你说它奇葩,嗯,那这样子就不奇葩了
			// 不过在显示关于窗口是方法第一个传入参数没啥用
			// ……我只能说奇葩那个注释是你加上去的……还有你是不是忘加内容了？
			dialogAbout = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(getString(R.string.menu_about))
					.setMessage(
							getString(R.string.about_content) + "\nn"
									+ Consts.RELEASE_DATE + "\nVer "
									+ versionName + " / " + versionCode + "\n"
									+ getString(R.string.update_whats_new)
									+ Consts.WHATSNEW)
					.setPositiveButton(R.string.thank_list, listenerAbout)
					.setNegativeButton(R.string.about_contact, listenerAbout)
					.setNeutralButton(R.string.send_feedback, listenerAbout)
					.create();
			dialogAbout.show();
			break;
		case Consts.Dialogs.SHARE:
			View musicInfoView = getMusicInfoView(_id);
			DialogInterface.OnClickListener listenerMain = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						shareMusic(musics[_id].getTitle(),
								musics[_id].getArtist(),
								musics[_id].getAlbum(),
								musics[_id].getAlbumId(),
								Consts.ShareMeans.WEIBO);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						shareMusic(musics[_id].getTitle(),
								musics[_id].getArtist(),
								musics[_id].getAlbum(),
								musics[_id].getAlbumId(),
								Consts.ShareMeans.OTHERS);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						sendFile(musics[_id].getPath());
						break;
					}
				}
			};
			dialogMain = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.choose_an_operation)
					.setView(musicInfoView)
					.setNegativeButton(R.string.share2others, listenerMain)
					.setPositiveButton(R.string.share2weibo, listenerMain)
					.setNeutralButton(R.string.send_file, listenerMain).show();
			break;
		case Consts.Dialogs.SEARCH:
			Log.v(Consts.DEBUG_TAG, "点击footer");
			View search = LayoutInflater.from(this).inflate(R.layout.search,
					null);
			final EditText et_title = (EditText) search
					.findViewById(R.id.et_title);
			final EditText et_artist = (EditText) search
					.findViewById(R.id.et_artist);
			final EditText et_album = (EditText) search
					.findViewById(R.id.et_album);
			Button button_weibo = (Button) search
					.findViewById(R.id.btn_share2weibo);
			OnClickListener listenerButton = new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.btn_share2weibo:
						if (et_title.getText().toString().trim().equals("")) {
							showCustomDialog(0, Consts.Dialogs.EMPTY);
						} else {
							shareMusic(et_title.getText().toString(), et_artist
									.getText().toString(), et_album.getText()
									.toString(), Consts.NULL,
									Consts.ShareMeans.WEIBO);
							dialogSearch.cancel();
						}
						break;
					case R.id.btn_share2others:
						if (et_title.getText().toString().trim().equals("")) {
						} else {
							shareMusic(et_title.getText().toString(), et_artist
									.getText().toString(), et_album.getText()
									.toString(), Consts.NULL,
									Consts.ShareMeans.OTHERS);
							dialogSearch.cancel();
						}
						break;
					}
				}
			};
			button_weibo.setOnClickListener(listenerButton);
			Button button_others = (Button) search
					.findViewById(R.id.btn_share2others);
			button_others.setOnClickListener(listenerButton);
			dialogSearch = new AlertDialog.Builder(this).setView(search)
					.setCancelable(true).create();
			dialogSearch.show();
			break;
		case Consts.Dialogs.EMPTY:
			new AlertDialog.Builder(Main.this)
					.setMessage(getString(R.string.empty))
					.setPositiveButton(getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
			break;

		case Consts.Dialogs.CHANGE_COLOR:
			View changeColor = View.inflate(Main.this, R.layout.color_chooser,
					null);
			final SeekBar seekColor[] = new SeekBar[3];
			final TextView textColor[] = new TextView[3];
			final TextView textColorCode = (TextView) changeColor
					.findViewById(R.id.text_color);
			final TextView textShowColor = (TextView) changeColor
					.findViewById(R.id.text_show_color);
			seekColor[Consts.Color.RED] = (SeekBar) changeColor
					.findViewById(R.id.seek_red);
			seekColor[Consts.Color.GREEN] = (SeekBar) changeColor
					.findViewById(R.id.seek_green);
			seekColor[Consts.Color.BLUE] = (SeekBar) changeColor
					.findViewById(R.id.seek_blue);
			textColor[Consts.Color.RED] = (TextView) changeColor
					.findViewById(R.id.text_red);
			textColor[Consts.Color.GREEN] = (TextView) changeColor
					.findViewById(R.id.text_green);
			textColor[Consts.Color.BLUE] = (TextView) changeColor
					.findViewById(R.id.text_blue);
			OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					switch (seekBar.getId()) {
					case R.id.seek_red:
						textColor[Consts.Color.RED]
								.setText(getString(R.string.red) + ":"
										+ progress);
						break;
					case R.id.seek_green:
						textColor[Consts.Color.GREEN]
								.setText(getString(R.string.green) + ":"
										+ progress);
						break;
					case R.id.seek_blue:
						textColor[Consts.Color.BLUE]
								.setText(getString(R.string.blue) + ":"
										+ progress);
						break;
					}
					changeColor();

				}

				private void changeColor() {
					String color[] = new String[3];
					color[Consts.Color.RED] = Integer
							.toHexString(seekColor[Consts.Color.RED]
									.getProgress());
					color[Consts.Color.GREEN] = Integer
							.toHexString(seekColor[Consts.Color.GREEN]
									.getProgress());
					color[Consts.Color.BLUE] = Integer
							.toHexString(seekColor[Consts.Color.BLUE]
									.getProgress());
					for (int i = 0; i < 3; i++) {
						if (color[i].length() == 1)
							color[i] = "0" + color[i];
					}
					String hexColor = ("#" + color[0] + color[1] + color[2]).toUpperCase(Locale.getDefault());
					//Log.d(Consts.DEBUG_TAG, "Color: "+hexColor);
					textColorCode.setText(hexColor);
					textShowColor.setBackgroundColor(android.graphics.Color
							.parseColor(hexColor));
				}
			};
			for (int i = 0; i < 3; i++) {
				seekColor[i].setOnSeekBarChangeListener(seekListener);
			}
			DialogInterface.OnClickListener listenerColor = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						SharedPreferences preferences = getApplicationContext()
						.getSharedPreferences(Consts.Preferences.GENERAL,
								Context.MODE_PRIVATE);
						String color=textColorCode.getText().toString();
				preferences.edit().putString(Consts.Preferences.BG_COLOR, color)
						.commit();
				indexOverlay.setBackgroundColor(android.graphics.Color
							.parseColor(color));
				Log.d(Consts.DEBUG_TAG, "自定义颜色:"+color);
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						dialogChangeColor.cancel();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						SharedPreferences prefer = getApplicationContext()
						.getSharedPreferences(Consts.Preferences.GENERAL,
								Context.MODE_PRIVATE);
				prefer.edit().putString(Consts.Preferences.BG_COLOR, Consts.ORIGIN_COLOR)
						.commit();
						indexOverlay.setBackgroundColor(android.graphics.Color
								.parseColor(Consts.ORIGIN_COLOR));
						dialogChangeColor.cancel();
						break;
					}
				}
			};
			dialogChangeColor = new AlertDialog.Builder(Main.this)
					.setView(changeColor)
					.setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.change_overlay_color)
					.setPositiveButton(android.R.string.ok, listenerColor)
					.setNegativeButton(android.R.string.cancel, listenerColor)
					.setNeutralButton(R.string.reset, listenerColor)
					.create();
			dialogChangeColor.show();
			break;

		default:
			throw new RuntimeException("What the hell are you doing?");
		}
	}

	private View getMusicInfoView(final int _id) {
		View musicInfo = LayoutInflater.from(this).inflate(R.layout.music_info,
				null);
		ImageView albumArt = (ImageView) musicInfo
				.findViewById(R.id.image_music);
		TextView textTitle = (TextView) musicInfo.findViewById(R.id.text_title);
		TextView textArtist = (TextView) musicInfo
				.findViewById(R.id.text_artist);
		TextView textAlbum = (TextView) musicInfo.findViewById(R.id.text_album);
		TextView textDuration = (TextView) musicInfo
				.findViewById(R.id.text_duration);
		textTitle.setText(getString(R.string.title) + " : "
				+ musics[_id].getTitle());
		textArtist.setText(getString(R.string.artist) + " : "
				+ musics[_id].getArtist());
		textAlbum.setText(getString(R.string.album) + " : "
				+ musics[_id].getAlbum());
		textDuration.setText(getString(R.string.duration) + " : "
				+ musics[_id].getDuration());
		int size = Utilities.getAdaptedSize(Main.this);
		Bitmap bmpAlbum = Utilities.getLocalArtwork(Main.this,
				musics[_id].getAlbumId(), size, size);
		try {
			Log.d(Consts.DEBUG_TAG,
					"width:" + bmpAlbum.getWidth() + bmpAlbum.toString());
			albumArt.setImageBitmap(bmpAlbum);
			Log.d(Consts.DEBUG_TAG, "Oh Oh Oh Yeah!!");
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.d(Consts.DEBUG_TAG,
					"Oh shit, we got null again ...... Don't panic");
		}
		albumArt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				playMusic(_id);
			}
		});
		// Log.d(Consts.DEBUG_TAG,"view:"+
		// albumArt.getHeight()+","+albumArt.getWidth());
		return musicInfo;
	}

	// 列表点击监听类
	private class MusicListOnClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			if (position != listview.getCount()) {
				try {
					dialogMain.cancel();
				} catch (Exception e) {
				}
			}
			indexOverlay.setVisibility(View.INVISIBLE);
			showCustomDialog(position, Consts.Dialogs.SHARE);
		}
	}

	// 音乐列表
	private void showMusicList() {

		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				Consts.MEDIA_INFO,
				MediaStore.Audio.Media.DURATION + ">='" + 30000 + "' AND "
						+ MediaStore.Audio.Media.MIME_TYPE + "<>'audio/amr'",
				// 妈妈再也不用担心我的录音!
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 过滤小于30s的音乐
		cursor.moveToFirst();
		musics = new MusicData[cursor.getCount()];
		for (int i = 0; i < cursor.getCount(); i++) {
			musics[i] = new MusicData();
			musics[i].setTitle(cursor.getString(0).trim());
			musics[i].setDuration(Utilities.convertDuration(cursor.getInt(1)));
			musics[i].setArtist(cursor.getString(2).trim());
			musics[i].setPath(cursor.getString(3));
			musics[i].setAlbum(cursor.getString(4).trim());
			musics[i].setAlbumId(cursor.getLong(5));
			cursor.moveToNext();
		}
		listview.setAdapter(new MusicListAdapter(this, musics));
		cursor.close();

	}

	// 分享音乐
	private void shareMusic(String title, String artist, String album,
			long album_id, int means) {
		QueryAndShareMusicInfo query = new QueryAndShareMusicInfo(title,
				artist, album, album_id, means, getApplicationContext(),
				handler);
		query.start();
		Toast.makeText(this, getString(R.string.querying), Toast.LENGTH_LONG)
				.show();
	}

	// 播放音乐
	private void playMusic(int position) {
		Intent musicIntent = new Intent();
		musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		musicIntent.setAction(android.content.Intent.ACTION_VIEW);
		musicIntent.setDataAndType(
				Uri.fromFile(new File(musics[position].getPath())), "audio/*");
		try {
			startActivity(musicIntent);
		} catch (ActivityNotFoundException e) {
			new AlertDialog.Builder(Main.this)
					.setMessage(getString(R.string.no_app_found))
					.setPositiveButton(getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}

	}

	// 刷新音乐列表
	private void refreshMusicList() {
		try {
			IntentFilter filter = new IntentFilter(
					Intent.ACTION_MEDIA_SCANNER_STARTED);
			filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			filter.addDataScheme("file");
			receiver = new Receiver();
			registerReceiver(receiver, filter);
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory()
									.getAbsolutePath())));
			showMusicList();// 我、我肯定是哪次改的时候脑残把这句删了
			// 然后它就没没效果?
		} catch (Exception e) {
			e.printStackTrace();
			setContentView(R.layout.empty);
		}
	}

	private void showAbout() { // 显示关于窗口
		showCustomDialog(0, Consts.Dialogs.ABOUT);
	}

	// 这是消息处理
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Consts.Status.INTERNET_ERROR:// 网络错误
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_internet), Toast.LENGTH_SHORT)
						.show();
				break;
			case Consts.Status.SEND_WEIBO:// 发送微博
				View sendweibo = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.sendweibo, null);
				final EditText et = (EditText) sendweibo.getRootView()
						.findViewById(R.id.et_content);
				final CheckBox cb = (CheckBox) sendweibo
						.findViewById(R.id.cb_follow);
				Bundle bundle = (Bundle) msg.obj;
				String _content = bundle.getString("content");
				final String artworkUrl = bundle.getString("artworkUrl");
				final String fileName = bundle.getString("fileName");
				// Log.v(Consts.DEBUG_TAG, artworkUrl);

				et.setText(_content);
				et.setSelection(_content.length());
				et.addTextChangedListener(new TextWatcher() {
					@Override
					public void afterTextChanged(Editable arg0) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						if ((s.toString() + " ").charAt(start) == '@')
							Log.d(Consts.DEBUG_TAG, "@ CATCHED!"); // TODO @提醒
						// XXX 为什么要这么做,因为不这么的话一上来就FC
					}
				});
				new AlertDialog.Builder(Main.this)
						.setView(sendweibo)
						.setPositiveButton(getString(R.string.share),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String content = et.getText()
												.toString();

										if (Main.accessToken == null
												|| (Main.accessToken
														.isSessionValid() == false)) {// 检测之前是否授权过
											handler.sendEmptyMessage(Consts.Status.NOT_AUTHORIZED_ERROR);
											saveSendStatus(content,
													cb.isChecked(), artworkUrl,
													fileName);
											ssoHandler.authorize(weiboHelper
													.getListener());// 授权
										} else {
											weiboHelper.sendWeibo(content,
													artworkUrl, fileName,
													cb.isChecked());
										}

									}

								}).show();
				Log.v(Consts.DEBUG_TAG, "弹出对话框");
				break;
			case Consts.Status.SEND_SUCCEED:// 发送成功
				Toast.makeText(Main.this, R.string.send_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.NOT_AUTHORIZED_ERROR:// 尚未授权
				Toast.makeText(Main.this, R.string.not_authorized_error,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.AUTH_ERROR:// 授权错误
				Toast.makeText(Main.this,
						R.string.auth_error + (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				Log.e(Consts.DEBUG_TAG, "错误" + (String) msg.obj);
				break;
			case Consts.Status.SEND_ERROR:// 发送错误
				Toast.makeText(Main.this,
						R.string.send_error + (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				Log.e(Consts.DEBUG_TAG, "错误" + (String) msg.obj);
				break;
			case Consts.Status.AUTH_SUCCEED:// 授权成功
				Toast.makeText(Main.this, R.string.auth_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.FEEDBACK_SUCCEED:
				Toast.makeText(Main.this, R.string.feedback_succeed,
						Toast.LENGTH_LONG).show();
				break;
			case Consts.Status.FEEDBACK_FAIL:
				Toast.makeText(Main.this, R.string.feedback_failed,
						Toast.LENGTH_LONG).show();
				SharedPreferences preferences = getApplicationContext()
						.getSharedPreferences(Consts.Preferences.FEEDBACK,
								Context.MODE_PRIVATE);
				preferences.edit().putString("content", (String) msg.obj)
						.commit();
				// TODO 完善一下这里需要重试
				break;
			case Consts.Status.NO_UPDATE:
				Toast toast = Toast.makeText(Main.this, R.string.no_update,
						Toast.LENGTH_LONG);
				if (Main.checkForUpdateCount != 0) {
					toast.show();
				}
				break;
			case Consts.Status.HAS_UPDATE:
				updateApp((String[]) msg.obj);
				break;
			}
		}
	};

	private void saveSendStatus(String content, boolean checked,
			String artworkUrl, String fileName) {
		SharedPreferences preferences = getApplicationContext()
				.getSharedPreferences(Consts.Preferences.SHARE,
						Context.MODE_PRIVATE);
		preferences.edit().putBoolean("read", true).commit();
		preferences.edit().putString("content", content).commit();
		preferences.edit().putBoolean("willFollow", checked).commit();
		preferences.edit().putString("artworkUrl", artworkUrl).commit();
		preferences.edit().putString("fileName", fileName);

	}

	private void sendFile(String path) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("*/*");
		// 果然是个好东西，不过多出来不少无关的，我在考虑要不要改回去呢
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			new AlertDialog.Builder(Main.this)
					.setMessage(getString(R.string.no_app_found))
					.setPositiveButton(getString(android.R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
		}
	}

	private void updateApp(final String[] info) {
		new AlertDialog.Builder(Main.this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.update_found)
				.setMessage(info[Consts.ArraySubscript.UPDATE_INFO])
				.setPositiveButton(R.string.update_download,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Uri uri = Uri
										.parse(info[Consts.ArraySubscript.DOWNLOAD_URL]);
								Intent intent = new Intent(Intent.ACTION_VIEW,
										uri);
								startActivity(intent);
							}
						})
				.setNegativeButton(R.string.update_view,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Uri uri = Uri
										.parse("market://details?id=com.paperairplane.music.share");
								Intent intent = new Intent(Intent.ACTION_VIEW,
										uri);
								try {
									startActivity(intent);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
									Toast.makeText(
											getApplicationContext(),
											getString(R.string.update_no_market_found),
											Toast.LENGTH_SHORT).show();
								}
							}
						}).show();

	}

	private void firstShow() {
		SharedPreferences preferences = getApplicationContext()
				.getSharedPreferences(Consts.Preferences.GENERAL,
						Context.MODE_PRIVATE);
		// if(!preferences.getBoolean("hasFirstStarted", false)){
		Log.d(Consts.DEBUG_TAG, "首次启动");
		dialogWelcome = new AlertDialog.Builder(Main.this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.welcome_title)
				.setMessage(
						getString(R.string.welcome_content)
								+ getString(R.string.update_whats_new)
								+ Consts.WHATSNEW + "\n\nP.S.测试版，所以每次都显示")
				.setPositiveButton(R.string.welcome_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialogWelcome.cancel();
							}
						}).create();
		Log.v(Consts.DEBUG_TAG, "首次启动对话框已初始化");
		dialogWelcome.show();
		Log.v(Consts.DEBUG_TAG, "首次启动对话框已显示");
		preferences.edit().putBoolean("hasFirstStarted", true).commit();
		// }
		// else Log.d(Consts.DEBUG_TAG, "非首次启动");
		// FIXME 发布的时候去掉这些注释就行，我是为了每次都显示
	}

}
