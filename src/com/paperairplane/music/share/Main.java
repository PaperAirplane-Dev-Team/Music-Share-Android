package com.paperairplane.music.share;

import java.io.File;
import java.util.Locale;
import java.util.Random;

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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

import com.paperairplane.music.share.ShakeDetector.OnShakeListener;
import com.paperairplane.music.share.MyLogger;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.sso.SsoHandler;

public class Main extends ListActivity {
	// 存储音乐信息
	private MusicData[] mMusicDatas;// 保存音乐数据
	private ListView mLvMain;// 列表对象
	public static Oauth2AccessToken sAccessToken = null;
	private Weibo mWeibo = Weibo.getInstance(Consts.APP_KEY,
			Consts.Url.AUTH_REDIRECT);
	private Receiver mReceiver;
	private AlertDialog mDialogMain, mDialogAbout, mDialogSearch, mDialogThank,
			mDialogWelcome, mDialogChangeColor, mDialogSendWeibo,
			mDialogBackgroundChooser;
	private SsoHandler mSsoHandler;
	private WeiboHelper mWeiboHelper;
	private static int sVersionCode, sCheckForUpdateCount = 0;
	private String mVersionName;
	private ImageView mIvFloatSearchButton;
	private ShakeDetector mShakeDetector;
	private boolean mCanDetectShake, mIsFullRunning; // 区分菜单项目
	private String mBackgroundPath = null;
	private SharedPreferences mPreferencesTheme;
	private Context mContext;

	@Override
	// 主体
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 由于需要在AtSuggetstion中调用，必须先进行
		mContext = getApplicationContext();
		mSsoHandler = new SsoHandler(Main.this, mWeibo);
		mWeiboHelper = new WeiboHelper(mHandler, mContext);
		try {
			Main.sVersionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			this.mVersionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 读取已存储的授权信息
		Main.sAccessToken = mWeiboHelper.readAccessToken();
		// 此处判断是否接收到其它App发来的Intent，并判断Intent携带的Uri是否为null。符合则处理。
		Intent i = getIntent();
		String action = i.getAction();
		// boolean isDataNull = i.getData() == null;
		if (action.equals("android.intent.action.VIEW")) {
			handleIntent(i.getData());
			mIsFullRunning = false;
			return;
		}
		if (action.equals("android.intent.action.SEND")) {
			// Toast.makeText(mContext, "抱歉,暂时不可用", Toast.LENGTH_LONG).show();
			Bundle bundle = i.getExtras();
			// if(bundle!=null)Log.d(Consts.DEBUG_TAG,
			// "I HAVE DATA! "+bundle.size());
			// Object keyset[]=bundle.keySet().toArray();
			// Log.d(Consts.DEBUG_TAG, "KEY IS "+keyset[0]);
			// finish();
			Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
			// Log.d(Consts.DEBUG_TAG, "Type is "+uri.getClass());
			// Log.d(Consts.DEBUG_TAG, "path is "+uri.getPath());
			// 我终于是试出来了啊
			handleIntent(uri);
			mIsFullRunning = false;
			return;
		}
		setContentView(R.layout.main);
		initListView();
		mPreferencesTheme = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		generateMusicList();

		firstShow();
		// 启动用于检查更新的后台线程
		Thread updateThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Utilities.checkForUpdate(Main.sVersionCode, mHandler,
								mContext,
								getResources().getConfiguration().locale);

					}
				}, 5000);
				// 如果用Thread.sleep会让整个程序ANR..
			}
		});
		updateThread.setPriority(Thread.MIN_PRIORITY);
		updateThread.start();
		setBackground();
		/*
		 * System.loadLibrary("utilities"); MyLogger.w(Consts.DEBUG_TAG,
		 * doNothing());
		 */
		MyLogger.i(Consts.DEBUG_TAG, "versionCode:" + Main.sVersionCode
				+ "\nversionName:" + mVersionName);
		mIsFullRunning = true;
	}

	/**
	 * 处理接收到的Intent
	 * 
	 * @author Xavier Yao
	 * @param uri
	 *            要处理的Intent
	 */
	private void handleIntent(Uri uri) {
		// setTheme(R.style.DialogTheme);
		try {
			Cursor cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					Consts.MEDIA_INFO,
					MediaStore.Audio.Media.DATA + "='" + uri.getPath() + "'",
					null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			cursor.moveToFirst();
			MusicData data = generateMusicData(cursor);
			View v = getMusicInfoView(data);
			setContentView(v);
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// private native String doNothing();

	/**
	 * 一个脑残的功能=.=
	 */

	private void initShakeDetector() {
		try {
			mShakeDetector = new ShakeDetector(mContext);
			mShakeDetector.mShakeThreshold = 2000;// 这里设置振幅
			mShakeDetector.registerOnShakeListener(new OnShakeListener() {
				@Override
				public void onShake() {
					MyLogger.d(Consts.DEBUG_TAG, "检测到摇动");
					int position = 0;
					if (!mLvMain.getAdapter().isEmpty()) {
						Random r = new Random();
						position = r.nextInt(mLvMain.getAdapter().getCount());
						MyLogger.d(Consts.DEBUG_TAG, "生成随机数" + position);
						Toast.makeText(mContext, R.string.shake_random,
								Toast.LENGTH_LONG).show();
						showCustomDialog(mMusicDatas[position],
								Consts.Dialogs.SHARE);
					}
				}
			});
			mShakeDetector.start();
			mCanDetectShake = true; // 你难道不知道不赋值的boolean就是false么……
		} catch (Exception e) {
			MyLogger.e(Consts.DEBUG_TAG, "ShakeDetector初始化失败，禁用");
			mCanDetectShake = false;
		}
	}

	/**
	 * @author Xavier Yao 初始化主界面ListView相关属性，初始化文字遮罩
	 * 
	 */
	private void initListView() {
		/*
		 * 设置按钮按下时的效果
		 */
		mIvFloatSearchButton = (ImageView) findViewById(R.id.float_search_button);
		mIvFloatSearchButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mIvFloatSearchButton
							.setImageResource(R.drawable.search_button_pressed);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mIvFloatSearchButton
							.setImageResource(R.drawable.search_button_normal);
				}
				return false;
			}
		});
		/*
		 * 初始化ListView
		 */
		mLvMain = (ListView) findViewById(android.R.id.list);// 找LisView的ID
		View vwEmpty = LayoutInflater.from(this).inflate(R.layout.empty, null);
		mLvMain.setEmptyView(vwEmpty);
		// FIXME:EmptyView突然很无力……
		mLvMain.setOnItemClickListener(new MusicListOnClickListener());// 创建一个ListView监听器对象
	}

	@SuppressWarnings("deprecation")
	private void setBackground() {
		mBackgroundPath = mPreferencesTheme.getString(
				Consts.Preferences.BG_PATH, null);
		View main_layout = findViewById(R.id.main_linearLayout);
		/*
		 * 这里判断SharedPreferences里读到的背景是否存在并设置，不存在则使用默认壁纸
		 */
		if (mBackgroundPath == null || !new File(mBackgroundPath).exists()) {
			// 原来可以不用catch...
			main_layout.setBackgroundResource(R.drawable.background_holo_dark);
		} else {
			main_layout.setBackgroundDrawable(Drawable
					.createFromPath(mBackgroundPath));
		}
	}

	@Override
	protected void onStop() {

		// 关闭摇动检查
		if (mCanDetectShake)
			mShakeDetector.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 恢复摇动检测
		if (mCanDetectShake)
			mShakeDetector.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 这里判断接收到的Intent是来自AtSuggestion还是微博SSO授权
		// 还有可爱的背景
		if (requestCode == Consts.LOOK_FOR_SUGGESTION_REQUEST_CODE) {
			// 这里根据bundle的数据重启dialogSendWeibo
			mDialogSendWeibo.dismiss();
			Message m = mHandler.obtainMessage(Consts.Status.SEND_WEIBO);
			m.obj = data.getExtras();
			m.sendToTarget();
		} else if (requestCode == Consts.PICK_BACKGROUND_REQUEST_CODE
				&& resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			mBackgroundPath = cursor.getString(columnIndex);
			cursor.close();
			showCustomDialog(null, Consts.Dialogs.CHANGE_BACKGROUND);

		} else {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (!mIsFullRunning) {
			menu.add(Menu.NONE, R.id.menu_exit, 0, R.string.menu_exit).setIcon(
					android.R.drawable.ic_menu_delete);
			return true;
		}
		getMenuInflater().inflate(R.menu.main, menu);
		SubMenu submenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 3,
				R.string.menu_customize).setIcon(
				android.R.drawable.ic_menu_manage);
		getMenuInflater().inflate(R.menu.customize, submenu);
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
		if (!isAccessTokenExistAndValid()) {
			menu.add(Menu.NONE, Consts.MenuItem.AUTH, 2, R.string.auth)
					.setIcon(android.R.drawable.ic_menu_add);
		} else {
			menu.add(Menu.NONE, Consts.MenuItem.UNAUTH, 2, R.string.unauth)
					.setIcon(android.R.drawable.ic_menu_delete);
		}
		menu.removeItem(R.id.menu_change_color);
		return true;
	}

	@Override
	// 菜单判断
	public boolean onOptionsItemSelected(MenuItem menu) {
		super.onOptionsItemSelected(menu);
		switch (menu.getItemId()) {
		case R.id.menu_exit:
			finish();
			System.exit(0);
			break;
		case R.id.menu_about:
			showAbout();
			break;
		case R.id.menu_change_color:
			// Solved
			showCustomDialog(null, Consts.Dialogs.CHANGE_COLOR);
			break;
		case R.id.menu_clean_cache:
			String ARTWORK_PATH = getExternalCacheDir().getAbsolutePath()
					+ "/.artworkCache/";
			int fileCount = 0;
			try {
				File[] files = new File(ARTWORK_PATH).listFiles();
				fileCount = files.length;
				for (File f : files) {
					f.delete();
					// 虽然比起来常规for可能性能差……不过不过不过！好歹我发现了for-each!
					// Effective Java建议多用for-each……
				}
			} catch (Exception e) {
				// e.printStackTrace();
				MyLogger.e(Consts.DEBUG_TAG, "Exception: NO FILE deleted.");
				// 仁慈一点，红色。不报错，不报错，不报错
			}
			String toastText = getString(R.string.clean_cache_done) + "\n"
					+ getString(R.string.delete_file_count) + fileCount;
			Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
			break;
		case Consts.MenuItem.UNAUTH:
			try {
				new AlertDialog.Builder(this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage(R.string.unauth_confirm)
						.setTitle(R.string.unauth)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@SuppressLint("NewApi")
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										Main.sAccessToken = null;
										mWeiboHelper.clear();
										if (Build.VERSION.SDK_INT > 10) {
											invalidateOptionsMenu();
										}
										Toast.makeText(mContext,
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
			}
			break;
		case Consts.MenuItem.AUTH:
			try {
				mSsoHandler.authorize(mWeiboHelper.getListener());
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case Consts.MenuItem.REFRESH:
			refreshMusicList();
			break;
		case R.id.menu_update:
			Main.sCheckForUpdateCount++;
			Utilities.checkForUpdate(Main.sVersionCode, mHandler, mContext,
					getResources().getConfiguration().locale);
			break;
		case R.id.menu_change_background:
			showCustomDialog(null, Consts.Dialogs.CHANGE_BACKGROUND);
			break;
		}
		return true;
	}

	/**
	 * @param View
	 *            v
	 * @return void 主界面显示为空时按钮点击处理
	 * 
	 */
	public void btn_empty(View v) {
		refreshMusicList();
	}

	/**
	 * 
	 * @return 检查AccessToken的存在及合法性
	 */
	private boolean isAccessTokenExistAndValid() {
		boolean flag = true;
		if (Main.sAccessToken == null
				|| Main.sAccessToken.isSessionValid() == false) {
			flag = false;
		}
		return flag;
	}

	/**
	 * @param View
	 *            v
	 * @return void 搜索互联网按钮点击处理
	 * 
	 */
	public void footer(View v) {
		showCustomDialog(null, Consts.Dialogs.SEARCH);
	}

	// 对话框处理

	/**
	 * @param music
	 *            传入分享的音乐信息
	 * @param whichDialog
	 *            根据Consts.Dialog下面的编号判断是什么对话框
	 * @author Harry Chen 显示程序的各种自定义对话框，包括dialogMain, mDialogAbout,
	 *         mDialogSearch, mDialogThank, mDialogWelcome, mDialogChangeColor
	 * 
	 */
	private void showCustomDialog(final MusicData music, int whichDialog) {
		if (mCanDetectShake)
			mShakeDetector.stop();
		final DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				try {
					if (mCanDetectShake)
						mShakeDetector.start();
				} catch (Exception e) {

				}
			}
		};
		switch (whichDialog) {
		case Consts.Dialogs.ABOUT:
			DialogInterface.OnClickListener listenerAbout = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						mDialogThank = new AlertDialog.Builder(Main.this)
								.setOnCancelListener(onCancelListener)
								.setTitle(R.string.thank_title)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setMessage(R.string.thank_content)
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												mDialogThank.cancel();
												mDialogAbout.show();
											}
										}).create();
						mDialogThank.show();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						Uri uri = Uri.parse(getString(R.string.url));
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						View feedback = LayoutInflater.from(Main.this).inflate(
								R.layout.feedback, null);
						final EditText etContent = (EditText) feedback
								.findViewById(R.id.et_feedback);
						final EditText etName = (EditText) feedback
								.findViewById(R.id.et_name);
						final EditText etEmail = (EditText) feedback
								.findViewById(R.id.et_email);
						TextWatcher twEmail = new TextWatcher() {
							@Override
							public void onTextChanged(CharSequence s,
									int start, int before, int count) {
							}

							@Override
							public void beforeTextChanged(CharSequence s,
									int start, int count, int after) {
							}

							@Override
							public void afterTextChanged(Editable s) {
								String address = s.toString();
								if (!address
										.matches("(?:\\w+)@(?:\\w+)(?:(\\.[a-zA-z]{2,4})+)$")) {
									etEmail.setTextColor(Color.RED);
								} else {
									etEmail.setTextColor(getResources()
											.getColor(
													android.R.color.primary_text_light));
								}
							}
						};
						etEmail.addTextChangedListener(twEmail);
						final ImageView[] ivClearButtons = new ImageView[3];

						ivClearButtons[0] = (ImageView) feedback
								.findViewById(R.id.btn_clear_content);
						ivClearButtons[1] = (ImageView) feedback
								.findViewById(R.id.btn_clear_name);
						ivClearButtons[2] = (ImageView) feedback
								.findViewById(R.id.btn_clear_email);

						OnClickListener listenerClear = new OnClickListener() {
							@Override
							public void onClick(View v) {
								int id = v.getId();
								switch (id) {
								case R.id.btn_clear_content:
									etContent.setText("");
									break;
								case R.id.btn_clear_name:
									etName.setText("");
									break;
								case R.id.btn_clear_email:
									etEmail.setText("");
									break;
								}

							}
						};
						for (ImageView iv : ivClearButtons) {
							iv.setOnClickListener(listenerClear);
						}

						SharedPreferences pref = getSharedPreferences(
								Consts.Preferences.FEEDBACK, MODE_PRIVATE);
						String text = pref.getString("content", "");
						etContent.setText(text);
						pref.edit().clear().commit();
						final AlertDialog.Builder builder = new AlertDialog.Builder(
								Main.this);
						DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								String strContent = etContent.getText()
										.toString().trim();
								String strName = etName.getText().toString()
										.trim();
								String strEmail = etEmail.getText().toString()
										.trim();
								if (strContent.equals("")
										|| strEmail.equals("")) {
									showCustomDialog(null, Consts.Dialogs.EMPTY);
								} else {
									String[] contents = new String[3];
									contents[0] = strContent;
									contents[1] = strName;
									contents[2] = strEmail;
									SendFeedback feedback = new SendFeedback(
											contents, mHandler,
											Main.sVersionCode, mContext);
									switch (whichButton) {
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

						builder.setView(feedback)
								.setPositiveButton(R.string.send_feedback,
										listener)
								.setTitle(R.string.thank_for_feedback)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setOnCancelListener(onCancelListener);
						if (isAccessTokenExistAndValid()) {
							builder.setNegativeButton(R.string.feedback_weibo,
									listener);
						}
						builder.show();
						break;
					}
				}
			};
			mDialogAbout = new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(getString(R.string.menu_about))
					.setOnCancelListener(onCancelListener)
					.setMessage(
							getString(R.string.about_content) + "\n\n"
									+ Consts.RELEASE_DATE + "\nVer "
									+ mVersionName + " / " + sVersionCode
									+ "\n"
									+ getString(R.string.update_whats_new)
									+ getString(R.string.whats_new))
					.setPositiveButton(R.string.thank_list, listenerAbout)
					.setNegativeButton(R.string.about_contact, listenerAbout)
					.setNeutralButton(R.string.send_feedback, listenerAbout)
					.create();
			mDialogAbout.show();
			break;
		case Consts.Dialogs.SHARE:
			View musicInfoView = getMusicInfoView(music);
			DialogInterface.OnClickListener listenerMain = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_NEGATIVE:
						shareMusic(music.getTitle(), music.getArtist(),
								music.getAlbum(), music.getAlbumId());
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						sendFile(music);
						break;
					}
				}
			};
			mDialogMain = new AlertDialog.Builder(this)
					.setOnCancelListener(onCancelListener)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.choose_an_operation)
					.setView(musicInfoView)
					.setNegativeButton(R.string.share, listenerMain)
					.setNeutralButton(R.string.send_file, listenerMain).show();
			break;
		case Consts.Dialogs.SEARCH:
			View search = LayoutInflater.from(this).inflate(R.layout.search,
					null);
			final EditText et_title = (EditText) search
					.findViewById(R.id.et_title);
			final EditText et_artist = (EditText) search
					.findViewById(R.id.et_artist);
			final EditText et_album = (EditText) search
					.findViewById(R.id.et_album);
			DialogInterface.OnClickListener listenerSearch = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					if (et_title.getText().toString().trim().equals("")) {
						showCustomDialog(null, Consts.Dialogs.EMPTY);

					} else {
						shareMusic(et_title.getText().toString(), et_artist
								.getText().toString(), et_album.getText()
								.toString(), Consts.NULL);
						mDialogSearch.cancel();
					}
				}
			};
			mDialogSearch = new AlertDialog.Builder(this).setView(search)
					.setCancelable(true).setOnCancelListener(onCancelListener)
					.setPositiveButton(R.string.share, listenerSearch)
					.setTitle(R.string.search)
					.setIcon(android.R.drawable.ic_dialog_info).create();
			mDialogSearch.show();
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
			View changeColor = View.inflate(mContext, R.layout.color_chooser,
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
			// 实际测试,不透明度是必须的
			// 我昨天也发现了……

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
					String color[] = new String[4];
					color[Consts.Color.RED] = Integer
							.toHexString(seekColor[Consts.Color.RED]
									.getProgress());
					color[Consts.Color.GREEN] = Integer
							.toHexString(seekColor[Consts.Color.GREEN]
									.getProgress());
					color[Consts.Color.BLUE] = Integer
							.toHexString(seekColor[Consts.Color.BLUE]
									.getProgress());
					color[Consts.Color.OPACITY] = "FF";
					for (int i = 0; i < 4; i++) {
						if (color[i].length() == 1)
							color[i] = "0" + color[i];
					}
					String hexColor = ("#" + color[Consts.Color.OPACITY]
							+ color[Consts.Color.RED]
							+ color[Consts.Color.GREEN] + color[Consts.Color.BLUE])
							.toUpperCase(Locale.getDefault());
					// MyLogger.d(Consts.DEBUG_TAG, "Color: "+hexColor);
					textColorCode.setText(hexColor);
					textShowColor.setBackgroundColor(android.graphics.Color
							.parseColor(hexColor));
				}
			};
			for (int i = 0; i < 3; i++) {
				seekColor[i].setOnSeekBarChangeListener(seekListener);
			}
			String nowColor;
			if (mPreferencesTheme.contains(Consts.Preferences.TEXT_COLOR)) {
				nowColor = mPreferencesTheme.getString(
						Consts.Preferences.TEXT_COLOR, "");
			} else {
				nowColor = Consts.ORIGIN_COLOR;
			}
			int colorInt[] = new int[3];
			colorInt[Consts.Color.RED] = Integer.valueOf(
					nowColor.substring(3, 5), 16);
			colorInt[Consts.Color.GREEN] = Integer.valueOf(
					nowColor.substring(5, 7), 16);
			colorInt[Consts.Color.BLUE] = Integer.valueOf(
					nowColor.substring(7, 9), 16);
			MyLogger.i(Consts.DEBUG_TAG, "Integers are: " + colorInt[0] + " "
					+ colorInt[1] + " " + colorInt[2]);
			for (int i = 0; i < 3; i++) {
				seekColor[i].setProgress(colorInt[i]);
			}

			DialogInterface.OnClickListener listenerColor = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						String color = textColorCode.getText().toString();
						if (color.contains("#")) {
							mPreferencesTheme
									.edit()
									.putString(Consts.Preferences.TEXT_COLOR,
											color).commit();
							mLvMain.setAdapter(new MusicListAdapter(mContext,
									mMusicDatas));
							MyLogger.d(Consts.DEBUG_TAG, "自定义颜色:" + color);
						}
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						mDialogChangeColor.cancel();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						if (mPreferencesTheme
								.contains(Consts.Preferences.TEXT_COLOR))
							mPreferencesTheme.edit()
									.remove(Consts.Preferences.TEXT_COLOR)
									.commit();
						mLvMain.setAdapter(new MusicListAdapter(mContext,
								mMusicDatas));
						break;
					}
				}
			};
			mDialogChangeColor = new AlertDialog.Builder(Main.this)
					.setOnCancelListener(onCancelListener).setView(changeColor)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.change_text_color)
					.setPositiveButton(android.R.string.ok, listenerColor)
					.setNegativeButton(android.R.string.cancel, listenerColor)
					.setNeutralButton(R.string.reset, listenerColor).create();
			mDialogChangeColor.show();
			break;
		case Consts.Dialogs.CHANGE_BACKGROUND:
			View v = View.inflate(mContext, R.layout.background_chooser, null);
			final ImageView iv_background = (ImageView) v
					.findViewById(R.id.imageView_background);
			if (mBackgroundPath != null) {
				Drawable background = Drawable.createFromPath(mBackgroundPath);
				BitmapDrawable bd = (BitmapDrawable) background;
				Bitmap bm = bd.getBitmap();
				iv_background.setImageBitmap(bm);
			}
			DialogInterface.OnClickListener listenerBackground = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_POSITIVE:
						if (mBackgroundPath != null) {
							mPreferencesTheme
									.edit()
									.putString(Consts.Preferences.BG_PATH,
											mBackgroundPath).commit();
						}
						setBackground();
						DialogInterface.OnClickListener listenerNotice = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (whichButton == DialogInterface.BUTTON_POSITIVE)
									showCustomDialog(null,
											Consts.Dialogs.CHANGE_COLOR);
							}
						};
						new AlertDialog.Builder(Main.this)
								.setIcon(android.R.drawable.ic_dialog_info)
								.setTitle(android.R.string.dialog_alert_title)
								.setMessage(R.string.if_change_text_color)
								.setPositiveButton(android.R.string.yes,
										listenerNotice)
								.setNegativeButton(android.R.string.no,
										listenerNotice).show();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						Intent i = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i,
								Consts.PICK_BACKGROUND_REQUEST_CODE);
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						mBackgroundPath = null;
						iv_background
								.setImageResource(R.drawable.background_holo_dark);
						mPreferencesTheme.edit()
								.remove(Consts.Preferences.BG_PATH).commit();
						setBackground();
						break;
					}

				}
			};
			mDialogBackgroundChooser = new AlertDialog.Builder(Main.this)
					.setOnCancelListener(onCancelListener)
					.setView(v)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.menu_change_background)
					.setPositiveButton(android.R.string.ok, listenerBackground)
					.setNegativeButton(R.string.choose_picture,
							listenerBackground)
					.setNeutralButton(R.string.choose_default,
							listenerBackground).create();
			mDialogBackgroundChooser.show();
			break;
		default:
			throw new RuntimeException("What the hell are you doing?");
		}
	}

	/**
	 * @param int _id 传入音乐所在数组的位置id
	 * @return View 用于初始化对话框的View
	 * @author Harry Chen 用于dialogMain，显示音乐信息
	 * 
	 */
	private View getMusicInfoView(final MusicData music) {
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
		Button btnShare = (Button) musicInfo.findViewById(R.id.btn_share);
		Button btnSendFile = (Button) musicInfo
				.findViewById(R.id.btn_send_file);
		textTitle.setText(getString(R.string.title) + " : " + music.getTitle());
		textArtist.setText(getString(R.string.artist) + " : "
				+ music.getArtist());
		textAlbum.setText(getString(R.string.album) + " : " + music.getAlbum());
		textDuration.setText(getString(R.string.duration) + " : "
				+ music.getDuration());
		int size = Utilities.getAdaptedSize(Main.this);
		Bitmap bmpAlbum = Utilities.getLocalArtwork(mContext,
				music.getAlbumId(), size, size);
		try {
			MyLogger.d(Consts.DEBUG_TAG, "width:" + bmpAlbum.getWidth());
			albumArt.setImageBitmap(bmpAlbum);
			MyLogger.d(Consts.DEBUG_TAG, "Oh Oh Oh Yeah!!");
		} catch (NullPointerException e) {
			e.printStackTrace();
			MyLogger.v(Consts.DEBUG_TAG,
					"Oh shit, we got null again ...... Don't panic");
		}
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.image_music:
					playMusic(music);
					break;
				case R.id.btn_share:
					shareMusic(music.getTitle(), music.getArtist(),
							music.getAlbum(), music.getAlbumId());
					break;
				case R.id.btn_send_file:
					sendFile(music);
					break;
				}
			}
		};
		albumArt.setOnClickListener(listener);
		btnSendFile.setOnClickListener(listener);
		btnShare.setOnClickListener(listener);
		if (mIsFullRunning) {
			btnSendFile.setVisibility(View.GONE);
			btnShare.setVisibility(View.GONE);
		}
		return musicInfo;
	}

	/**
	 * @author Xavier Yao 处理各种线程信息
	 * 
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Consts.Status.INTERNET_ERROR:// 网络错误
				Toast.makeText(mContext, getString(R.string.error_internet),
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.SEND_WEIBO:// 发送微博
				if (mCanDetectShake)
					mShakeDetector.stop();
				View sendweibo = LayoutInflater.from(mContext).inflate(
						R.layout.sendweibo, null);
				final EditText et = (EditText) sendweibo.getRootView()
						.findViewById(R.id.et_content);
				final CheckBox cb = (CheckBox) sendweibo
						.findViewById(R.id.cb_follow);
				final ImageView iv_clear = (ImageView) sendweibo
						.findViewById(R.id.clear_button);
				iv_clear.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						et.setText("");
					}
				});
				final Bundle bundle = (Bundle) msg.obj;
				String _content = bundle.getString(Intent.EXTRA_TEXT);
				final String artworkUrl = bundle.getString("artworkUrl");
				final String fileName = bundle.getString("fileName");
				int selection = bundle.getInt("selection", _content.length());
				// MyLogger.v(Consts.DEBUG_TAG, artworkUrl);
				cb.setChecked(bundle.getBoolean("isChecked", true));
				et.setText(_content);
				et.setSelection(selection);
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
						try {
							if (s.toString().charAt(start) == '@') {
								MyLogger.i(Consts.DEBUG_TAG, "@ CAUGHT!"); // @提醒
								// 我有错，我悔过
								Intent i = new Intent(mContext,
										AtSuggestionActivity.class);
								bundle.putString(Intent.EXTRA_TEXT,
										s.toString());
								bundle.putBoolean("isChecked", cb.isChecked());
								bundle.putInt("start", start);
								i.putExtras(bundle);
								startActivityForResult(i,
										Consts.LOOK_FOR_SUGGESTION_REQUEST_CODE);
							}
						} catch (Exception e) {

						}
					}
				});
				mDialogSendWeibo = new AlertDialog.Builder(Main.this)
						.setView(sendweibo)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										if (mCanDetectShake)
											mShakeDetector.start();
									}
								})
						.setPositiveButton(getString(R.string.share),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										String content = et.getText()
												.toString();

										if (!isAccessTokenExistAndValid()) {// 检测之前是否授权过
											mHandler.sendEmptyMessage(Consts.Status.NOT_AUTHORIZED_ERROR);
											saveSendStatus(content,
													cb.isChecked(), artworkUrl,
													fileName);
											mSsoHandler.authorize(mWeiboHelper
													.getListener());// 授权
										} else {
											mWeiboHelper.sendWeibo(content,
													artworkUrl, fileName,
													cb.isChecked());
										}

									}

								}).show();
				break;
			case Consts.Status.SEND_SUCCEED:// 发送成功
				Toast.makeText(mContext, R.string.send_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.NOT_AUTHORIZED_ERROR:// 尚未授权
				Toast.makeText(mContext, R.string.not_authorized_error,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.AUTH_ERROR:// 授权错误
				Toast.makeText(mContext,
						R.string.auth_error + (String) msg.obj,
						Toast.LENGTH_LONG).show();
				MyLogger.e(Consts.DEBUG_TAG, "授权错误" + (String) msg.obj);
				break;
			case Consts.Status.SEND_ERROR:// 发送错误
				Toast.makeText(mContext,
						R.string.send_error + (String) msg.obj,
						Toast.LENGTH_LONG).show();
				MyLogger.e(Consts.DEBUG_TAG, "发送错误" + (String) msg.obj);
				break;
			case Consts.Status.AUTH_SUCCEED:// 授权成功
				Toast.makeText(mContext, R.string.auth_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.FEEDBACK_SUCCEED:
				Toast.makeText(mContext, R.string.feedback_succeed,
						Toast.LENGTH_LONG).show();
				break;
			case Consts.Status.FEEDBACK_FAIL:
				Toast.makeText(mContext, R.string.feedback_failed,
						Toast.LENGTH_LONG).show();
				SharedPreferences preferences = mContext.getSharedPreferences(
						Consts.Preferences.FEEDBACK, Context.MODE_PRIVATE);
				preferences.edit().putString("content", (String) msg.obj)
						.commit();
				break;
			case Consts.Status.NO_UPDATE:
				Toast toast = Toast.makeText(mContext, R.string.no_update,
						Toast.LENGTH_LONG);
				if (Main.sCheckForUpdateCount != 0) {
					toast.show();
				}
				break;
			case Consts.Status.HAS_UPDATE:
				updateApp((String[]) msg.obj);
				break;
			case Consts.Status.REFRESH_LIST_FINISHED:
				Toast.makeText(mContext, R.string.refresh_success,
						Toast.LENGTH_SHORT).show();
				try {
					generateMusicList();
					unregisterReceiver(mReceiver);
				} catch (Throwable t) {

				}
				break;
			case Consts.Status.MUSIC_INFO_FETCHED:
				IntentResolver ir = new IntentResolver();
				ir.handleIntent(Main.this, (Intent) msg.obj, mHandler);
			}

		}
	};

	/**
	 * @author Xavier Yao 列表点击监听类
	 * 
	 */
	private class MusicListOnClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			// if (position != mLvMain.getCount()) {
			try {
				mDialogMain.cancel();
			} catch (Exception e) {
			}
			// } 注释掉的代码好象是以前给footer用的
			showCustomDialog(mMusicDatas[position], Consts.Dialogs.SHARE);
		}
	}

	/**
	 * @param void
	 * @return void
	 * @author Xavier Yao 初始化用到的音乐信息数组，填充进主界面ListView
	 * 
	 */
	private void generateMusicList() throws NullPointerException {
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				Consts.MEDIA_INFO,
				MediaStore.Audio.Media.DURATION + ">='" + 30000 + "' AND "
						+ MediaStore.Audio.Media.MIME_TYPE + "<>'audio/amr'",
				// 妈妈再也不用担心我的录音!
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 过滤小于30s的音乐
		if (cursor != null) {
			cursor.moveToFirst();
			mMusicDatas = new MusicData[cursor.getCount()];
			for (int i = 0; i < cursor.getCount(); i++) {
				mMusicDatas[i] = generateMusicData(cursor);
				cursor.moveToNext();
			}
			mLvMain.setAdapter(new MusicListAdapter(this, mMusicDatas));
			cursor.close();
		}
		initShakeDetector();
	}

	/**
	 * 从cursor取值生成MusicData
	 * 
	 * @param cursor
	 * @return 生成的MusicData
	 */

	private MusicData generateMusicData(Cursor cursor) {
		MusicData musicData = new MusicData();
		musicData.setTitle(cursor.getString(0).trim());
		musicData.setDuration(Utilities.convertDuration(cursor.getInt(1)));
		musicData.setArtist(cursor.getString(2).trim());
		musicData.setPath(cursor.getString(3));
		musicData.setAlbum(cursor.getString(4).trim());
		musicData.setAlbumId(cursor.getLong(5));
		musicData.setType(cursor.getString(6));
		return musicData;
	}

	/**
	 * @param String
	 *            title 音乐标题
	 * @param String
	 *            artist音乐艺术家
	 * @param String
	 *            album 音乐专辑名
	 * @param long album_id 音乐专辑封面ID
	 * @param int means 分享意图，源自Consts.ShareMeans
	 * @return void
	 * @author Xavier Yao 分享音乐的主调方法，将调用QueryAndShareMusicInfo类
	 * 
	 */
	private void shareMusic(String title, String artist, String album,
			long albumId) {
		QueryAndShareMusicInfo query = new QueryAndShareMusicInfo(title,
				artist, album, albumId, mContext, mHandler);
		query.start();
		Toast.makeText(this, getString(R.string.querying), Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * @param int position 音乐在信息数组中的位置
	 * @return void
	 * @author Xavier Yao 播放音乐的主调方法
	 * 
	 */
	private void playMusic(MusicData music) {
		Intent musicIntent = new Intent();
		musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		musicIntent.setAction(android.content.Intent.ACTION_VIEW);
		musicIntent.setDataAndType(Uri.fromFile(new File(music.getPath())),
				"audio/*");
		new IntentResolver().handleIntent(Main.this, musicIntent, mHandler);
	}

	/**
	 * @param void
	 * @return void
	 * @author Xavier Yao 刷新音乐列表
	 * 
	 */
	private void refreshMusicList() {
		try {
			IntentFilter filter = new IntentFilter(
					Intent.ACTION_MEDIA_SCANNER_STARTED);
			filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			filter.addDataScheme("file");
			mReceiver = new Receiver(mHandler);
			registerReceiver(mReceiver, filter);
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory()
									.getAbsolutePath())));

		} catch (Exception e) {
			e.printStackTrace();
			setContentView(R.layout.empty);
		}
	}

	/**
	 * @param void
	 * @return void
	 * @author Harry Chen 显示关于窗口
	 * 
	 */
	private void showAbout() {
		showCustomDialog(null, Consts.Dialogs.ABOUT);
	}

	/**
	 * @author Xavier Yao
	 * @param String
	 *            content 微博内容
	 * @param boolean checked 是否关注开发者
	 * @param String
	 *            artworkUrl 微博图片地址
	 * @param String
	 *            fileName 图片文件名
	 * @return void 保存微博以及发送状态，备用
	 */
	private void saveSendStatus(String content, boolean checked,
			String artworkUrl, String fileName) {
		SharedPreferences preferences = mContext.getSharedPreferences(
				Consts.Preferences.SHARE, Context.MODE_PRIVATE);
		preferences.edit().putBoolean("read", true).commit();
		preferences.edit().putString("content", content).commit();
		preferences.edit().putBoolean("willFollow", checked).commit();
		preferences.edit().putString("artworkUrl", artworkUrl).commit();
		preferences.edit().putString("fileName", fileName);

	}

	/**
	 * @author Harry Chen
	 * @param MusicData
	 *            whichMusic 待发送的音乐信息
	 * @return void 发送音乐文件，通过其他App
	 */
	private void sendFile(MusicData whichMusic) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(whichMusic.getType());
		intent.putExtra(Intent.EXTRA_STREAM,
				Uri.fromFile(new File(whichMusic.getPath())));
		IntentResolver ir = new IntentResolver();
		ir.handleIntent(Main.this, intent, mHandler);

	}

	/**
	 * @author Xavier Yao
	 * @return void
	 * @param String
	 *            [] info传回的各种更新信息 通过返回的更新信息显示对话框让用户决定是否更新程序
	 */
	private void updateApp(final String[] info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(Main.this)
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
						});
		if (!Consts.ON_PLAY_STORE) {
			builder.setNegativeButton(R.string.update_view,

			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Uri uri = Uri
							.parse("market://details?id=com.paperairplane.music.share");
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					try {
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
						Toast.makeText(mContext,
								getString(R.string.update_no_market_found),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
		builder.show();

	}

	/**
	 * @author Harry Chen
	 * @param void
	 * @return void 判断是否首次启动并显示欢迎对话框
	 */
	private void firstShow() {
		SharedPreferences preferences = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		if (!preferences.getBoolean("hasFirstStarted", false)) {
			MyLogger.d(Consts.DEBUG_TAG, "首次启动");
			mDialogWelcome = new AlertDialog.Builder(Main.this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.welcome_title)
					.setMessage(
							getString(R.string.welcome_content)
									+ getString(R.string.update_whats_new)
									+ getString(R.string.whats_new))
					.setPositiveButton(R.string.welcome_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mDialogWelcome.cancel();
								}
							}).create();
			mDialogWelcome.show();
			preferences.edit().putBoolean("hasFirstStarted", true).commit();
		}

	}
}
