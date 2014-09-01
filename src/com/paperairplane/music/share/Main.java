package com.paperairplane.music.share;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Random;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paperairplane.music.share.Consts.SNS;
import com.paperairplane.music.share.dialogs.AboutDialogFragment;
import com.paperairplane.music.share.dialogs.AuthManagerDialogFragment;
import com.paperairplane.music.share.dialogs.BackgroundChooserDialogFragment;
import com.paperairplane.music.share.dialogs.ChangeColorDialogFragment;
import com.paperairplane.music.share.dialogs.EmptyDialogFragment;
import com.paperairplane.music.share.dialogs.FeedbackDialogFragment;
import com.paperairplane.music.share.dialogs.SearchDialogFragment;
import com.paperairplane.music.share.dialogs.SendWeiboDialogFragment;
import com.paperairplane.music.share.utils.CrashHandler;
import com.paperairplane.music.share.utils.HttpQuestHandler;
import com.paperairplane.music.share.utils.IntentResolver;
import com.paperairplane.music.share.utils.MyLogger;
import com.paperairplane.music.share.utils.ShakeDetector;
import com.paperairplane.music.share.utils.Utilities;
import com.paperairplane.music.share.utils.ShakeDetector.OnShakeListener;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * 主界面
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class Main extends ActionBarActivity {
	private MusicData[] mMusicDatas;// 保存音乐数据
	private ListView mLvMain;// 列表对象
	private Weibo mWeibo = new Weibo(Consts.WEIBO_APP_KEY,
			Consts.Url.AUTH_REDIRECT, Consts.Url.WEIBO_AUTH_URL);
	private Receiver mReceiver;
	private AlertDialog mDialogMain, mDialogWelcome;
	private SsoHandler mSsoHandler;
	private SnsHelper mWeiboHelper;
	public static int sVersionCode;
	private static int sCheckForUpdateCount = 0;
	private String mVersionName;
	private ShakeDetector mShakeDetector;
	private boolean mIsFullRunning; // 区分菜单项目
	private String mBackgroundPath = null;
	private SharedPreferences mPreferencesTheme;
	private Context mContext;
	private Handler mHttpQuestHandler;
	private FragmentManager mFragmentManager;
	private static final String TAG = "Main";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CrashHandler.getInstance().init(this);
		mContext = getApplicationContext();
		mHttpQuestHandler = HttpQuestHandler.getInstance(mHandler);
		mFragmentManager = getSupportFragmentManager();
		mSsoHandler = new SsoHandler(Main.this, mWeibo);
		mWeiboHelper = SnsHelper.getInstance(mHandler, mContext);
		try {
			Main.sVersionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			this.mVersionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			Bundle bundle = i.getExtras();
			Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
			handleIntent(uri);
			mIsFullRunning = false;
			return;
		}
		setContentView(R.layout.main);
		initListView();
		checkFailFeedback();
		mPreferencesTheme = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		generateMusicList();
		firstShow();
		// 启动用于检查更新的后台线程
		mHttpQuestHandler.obtainMessage(
				Consts.NetAccessIntent.CHECK_FOR_UPDATE, Main.this)
				.sendToTarget();
		setBackground();
		MyLogger.i(TAG, "versionCode:" + Main.sVersionCode + "\nversionName:"
				+ mVersionName);
		mIsFullRunning = true;
	}

	private void checkFailFeedback() {
		if (mContext.getSharedPreferences(Consts.Preferences.FEEDBACK,
				Context.MODE_PRIVATE).getBoolean("failed", false)) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						new FeedbackDialogFragment().show(mFragmentManager,
								"Feedback");
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						mContext.getSharedPreferences(
								Consts.Preferences.FEEDBACK,
								Context.MODE_PRIVATE).edit()
								.putBoolean("failed", false).commit();
						break;
					case DialogInterface.BUTTON_NEUTRAL:
						dialog.cancel();
						break;
					}

				}
			};

			new AlertDialog.Builder(this).setTitle(R.string.notice)
					.setCancelable(false).setMessage(R.string.fail_re_report)
					.setPositiveButton(android.R.string.ok, listener)
					.setNegativeButton(R.string.no_more, listener)
					.setNeutralButton(R.string.next_time, listener).create()
					.show();
		}

	}

	/**
	 * 处理接收到的Intent
	 * 
	 * @param uri
	 *            要处理的Intent中包含的uri
	 */
	private void handleIntent(Uri uri) {
		// setTheme(R.style.DialogTheme);
		try {
			Cursor cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					Consts.MEDIA_INFO,
					MediaStore.Audio.Media.DATA + "=\"" + uri.getPath() + "\"",
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

	/**
	 * 初始化摇动检测器
	 */
	private void initShakeDetector() {
		try {
			mShakeDetector = ShakeDetector.getInstance(mContext);
			mShakeDetector.mShakeThreshold = 2000;// 这里设置振幅
			mShakeDetector.registerOnShakeListener(new OnShakeListener() {
				@Override
				public void onShake() {
					MyLogger.d(TAG, "检测到摇动");
					int position = 0;
					if (!mLvMain.getAdapter().isEmpty()) {
						Random r = new Random();
						position = r.nextInt(mLvMain.getAdapter().getCount());
						MyLogger.d(TAG, "生成随机数" + position);
						Toast.makeText(mContext, R.string.shake_random,
								Toast.LENGTH_LONG).show();
						showCustomDialog(mMusicDatas[position],
								Consts.Dialogs.SHARE);
					}
				}
			});
			mShakeDetector.start();
			ShakeDetector.sCanDetact = true; // 你难道不知道不赋值的boolean就是false么……
		} catch (Exception e) {
			MyLogger.e(TAG, "ShakeDetector初始化失败，禁用");
			ShakeDetector.sCanDetact = false;
		}
	}

	/**
	 * 初始化主界面ListView相关属性,设置Adapater
	 */
	private void initListView() {
		/*
		 * 设置按钮按下时的效果
		 */
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
	/**
	 * 设置列表的背景
	 */
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
		if (ShakeDetector.sCanDetact)
			mShakeDetector.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 恢复摇动检测
		if (ShakeDetector.sCanDetact)
			mShakeDetector.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 这里判断接收到的Intent是来自AtSuggestion还是微博SSO授权
		// 还有可爱的背景
		if (requestCode == Consts.LOOK_FOR_SUGGESTION_REQUEST_CODE) {
			// 这里根据bundle的数据重启dialogSendWeibo
			// mDialogSendWeibo.dismiss();
			// TODO 待解决
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

	@Override
	/**
	 * 对于菜单显示前内容的处理
	 */
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
		MenuItem menuRefresh = menu.add(Menu.NONE, Consts.MenuItem.REFRESH, 1,
				R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh);
		MenuItem menuSearch = menu.add(Menu.NONE, Consts.MenuItem.SEARCH, 2,
				R.string.menu_search).setIcon(R.drawable.ic_menu_search);
		MenuItemCompat.setShowAsAction(menuRefresh,
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		MenuItemCompat.setShowAsAction(menuSearch,
				MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		// XXX 下面这句是啥？！
		// menu.removeItem(R.id.menu_change_color);

		return true;
	}

	@Override
	/**
	 * 对于菜单点击事件的处理
	 */
	public boolean onOptionsItemSelected(MenuItem menu) {
		super.onOptionsItemSelected(menu);
		switch (menu.getItemId()) {
		case R.id.menu_exit:
			finish();
			System.exit(0);
			break;
		case R.id.menu_about:
			showCustomDialog(null, Consts.Dialogs.ABOUT);
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
				MyLogger.e(TAG, "Exception: NO FILE deleted.");
				// 仁慈一点，红色。不报错，不报错，不报错
			}
			String toastText = getString(R.string.clean_cache_done) + "\n"
					+ getString(R.string.delete_file_count) + fileCount;
			Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
			break;
		/*
		 * case Consts.MenuItem.UNAUTH: try { new AlertDialog.Builder(this)
		 * .setIcon(android.R.drawable.ic_dialog_alert)
		 * .setMessage(R.string.unauth_confirm) .setTitle(R.string.unauth)
		 * .setPositiveButton(android.R.string.ok, new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @SuppressLint("NewApi")
		 * 
		 * @Override public void onClick(DialogInterface arg0, int arg1) {
		 * 
		 * Main.sAccessToken = null; mWeiboHelper.clear();
		 * 
		 * if (Build.VERSION.SDK_INT > 10) { invalidateOptionsMenu(); }
		 * Toast.makeText(mContext, getString(R.string.unauthed),
		 * Toast.LENGTH_SHORT).show(); } })
		 * .setNegativeButton(android.R.string.cancel, new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface arg0, int arg1) { }
		 * }).show();
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } break; case
		 * Consts.MenuItem.AUTH:
		 * 
		 * // try { // mSsoHandler.authorize(mWeiboHelper.getListener()); // }
		 * catch (Exception e) { // e.printStackTrace(); // } break;
		 */
		case Consts.MenuItem.REFRESH:
			refreshMusicList();
			break;
		case Consts.MenuItem.SEARCH:
			showCustomDialog(null, Consts.Dialogs.SEARCH);
			break;
		case R.id.menu_update:
			Main.sCheckForUpdateCount++;
			Utilities.checkForUpdate(Main.sVersionCode, mHandler, mContext,
					getResources().getConfiguration().locale);
			break;
		case R.id.menu_change_background:
			showCustomDialog(null, Consts.Dialogs.CHANGE_BACKGROUND);
			break;
		case R.id.menu_accounts:
			DialogFragment df = new AuthManagerDialogFragment();
			df.show(mFragmentManager, "authManagerDialog");
			break;
		}
		return true;
	}

	/**
	 * 主界面显示为空时按钮点击处理
	 * 
	 * @param v
	 *            触发该方法的控件
	 */
	public void btn_empty(View v) {
		refreshMusicList();
	}

	// 对话框处理

	/**
	 * 显示程序的各种自定义对话框，包括dialogMain, mDialogAbout, mDialogSearch, mDialogThank,
	 * mDialogWelcome, mDialogChangeColor
	 * 
	 * @param music
	 *            传入分享的音乐信息(可以为null)
	 * @param whichDialog
	 *            判断对话框类型
	 * @see Consts.Dialogs
	 * 
	 */
	private void showCustomDialog(final MusicData music, int whichDialog) {
		if (ShakeDetector.sCanDetact)
			mShakeDetector.stop();
		final DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				try {
					if (ShakeDetector.sCanDetact)
						mShakeDetector.start();
				} catch (Exception e) {

				}
			}
		};
		switch (whichDialog) {
		case Consts.Dialogs.ABOUT:
			DialogFragment dialogAbout = new AboutDialogFragment();
			Bundle args = new Bundle();
			args.putString("versionName", mVersionName);
			args.putInt("versionCode", sVersionCode);
			args.putBoolean("tokenValid",
					mWeiboHelper.isAccessTokenExistAndValid(SNS.WEIBO));
			dialogAbout.setArguments(args);
			dialogAbout.show(mFragmentManager, "aboutDialog");
			break;
		case Consts.Dialogs.SHARE:
			View musicInfoView = getMusicInfoView(music);
			DialogInterface.OnClickListener listenerMain = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					switch (whichButton) {
					case DialogInterface.BUTTON_NEGATIVE:
						shareMusic(music);
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

			SearchDialogFragment.OnShareMusicListener listenerSearch = new SearchDialogFragment.OnShareMusicListener() {

				@Override
				public void onShareMusic(MusicData music) {
					shareMusic(music);
					// 烂代码典范啊……
				}
			};
			SearchDialogFragment sdf = new SearchDialogFragment();
			sdf.setOnShareMusicListener(listenerSearch);
			sdf.show(mFragmentManager, "searchDialog");
			break;
		case Consts.Dialogs.EMPTY:
			new EmptyDialogFragment().show(mFragmentManager, "emptyDialog");
			break;

		case Consts.Dialogs.CHANGE_COLOR:
			ChangeColorDialogFragment ccdf = new ChangeColorDialogFragment();
			ccdf.setOnColorChangedListener(new ChangeColorDialogFragment.OnColorChangedListener() {
				@Override
				public void onColorChanged() {
					mLvMain.setAdapter(new MusicListAdapter(mContext,
							mMusicDatas));
				}
			});
			ccdf.show(mFragmentManager, "changeColorDialog");
			break;
		case Consts.Dialogs.CHANGE_BACKGROUND:
			BackgroundChooserDialogFragment bcdf = new BackgroundChooserDialogFragment();
			final Bundle bundle = new Bundle();
			bcdf.setOnBackgroundChangedListener(new BackgroundChooserDialogFragment.OnBackgroundChangedListener() {

				@Override
				public void onBackgroundChanged(String path) {
					mBackgroundPath = path;
					setBackground();
				}

			});
			bundle.putString("backgroundPath", mBackgroundPath);
			bcdf.setArguments(bundle);
			bcdf.show(mFragmentManager, "backgroundChooserDialog");
			break;
		default:
			throw new RuntimeException("What the hell are you doing?");
		}
	}

	/**
	 * 从音乐信息构建View
	 * 
	 * @param music
	 *            音乐信息
	 * @return 音乐信息View
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
		// Bitmap bmpAlbum = Utilities.getLocalArtwork(mContext,
		// music.getAlbumId(), size, size);
		SoftReference<Bitmap> bmpAlbum = new SoftReference<Bitmap>(
				Utilities.getLocalArtwork(mContext, music.getAlbumId(), size,
						size));
		// 似乎可以省资源
		try {
			MyLogger.d(TAG, "width:" + bmpAlbum.get().getWidth());
			albumArt.setImageBitmap(bmpAlbum.get());
			MyLogger.d(TAG, "Oh Oh Oh Yeah!!");
		} catch (NullPointerException e) {
			// e.printStackTrace();
			MyLogger.v(TAG, "Oh shit, we got null again ...... Don't panic");
		}
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.image_music:
					playMusic(music);
					break;
				case R.id.btn_share:
					shareMusic(music);
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
		bmpAlbum.clear();
		return musicInfo;
	}

	/**
	 * 处理各种线程信息
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
				if (ShakeDetector.sCanDetact)
					mShakeDetector.stop();
				final Bundle bundle = (Bundle) msg.obj;
				SendWeiboDialogFragment.OnShareToWeiboListener listener = new SendWeiboDialogFragment.OnShareToWeiboListener() {

					@Override
					public void onShareToWeibo(String content,
							String artworkUrl, String fileName,
							String annotation, boolean willFollow) {
						if (!mWeiboHelper.isAccessTokenExistAndValid(SNS.WEIBO)) {// 检测之前是否授权过
							mHandler.sendEmptyMessage(Consts.Status.NOT_AUTHORIZED_ERROR);
							saveSendStatus(content, willFollow, artworkUrl,
									fileName, annotation);
							mSsoHandler.authorize(mWeiboHelper
									.getListener(SNS.WEIBO));// 授权
						} else {
							mWeiboHelper
									.sendWeibo(content, artworkUrl, fileName,
											annotation, willFollow, SNS.WEIBO);
						}
					}
				};
				SendWeiboDialogFragment swdf = new SendWeiboDialogFragment();
				swdf.setArguments(bundle);
				swdf.setOnShareToWeiboListener(listener);
				swdf.show(mFragmentManager, "sendWeiboDialog");
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
				MyLogger.e(TAG, "授权错误" + (String) msg.obj);
				break;
			case Consts.Status.SEND_ERROR:// 发送错误
				Toast.makeText(mContext,
						R.string.send_error + (String) msg.obj,
						Toast.LENGTH_LONG).show();
				MyLogger.e(TAG, "发送错误" + (String) msg.obj);
				break;
			case Consts.Status.AUTH_SUCCEED:// 授权成功
				Toast.makeText(mContext, R.string.auth_succeed,
						Toast.LENGTH_SHORT).show();
				break;
			case Consts.Status.FEEDBACK_SUCCEED:
				Toast.makeText(mContext, R.string.feedback_succeed,
						Toast.LENGTH_LONG).show();
				mContext.getSharedPreferences(Consts.Preferences.FEEDBACK,
						Context.MODE_PRIVATE).edit()
						.putBoolean("failed", false).commit();
				break;
			case Consts.Status.FEEDBACK_FAIL:
				Toast.makeText(mContext, R.string.feedback_failed,
						Toast.LENGTH_LONG).show();
				SharedPreferences preferences = mContext.getSharedPreferences(
						Consts.Preferences.FEEDBACK, Context.MODE_PRIVATE);
				String[] contents = (String[]) msg.obj;
				Editor editor = preferences.edit();
				editor.putBoolean("failed", true);
				editor.putString("content",
						contents[Consts.FeedbackContentsItem.CONTENT]);
				editor.putString("name",
						contents[Consts.FeedbackContentsItem.NAME]);
				editor.putString("email",
						contents[Consts.FeedbackContentsItem.EMAIL]);
				editor.commit();
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
	 * 列表点击监听类
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
	 * 初始化用到的音乐信息数组，填充进主界面ListView
	 * 
	 * @throws NullPointerException
	 *             当查询不到任何音乐信息时抛出
	 */
	private void generateMusicList() throws NullPointerException {
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				Consts.MEDIA_INFO,
				MediaStore.Audio.Media.DURATION + ">='" + 30000 + "' AND "
						+ MediaStore.Audio.Media.MIME_TYPE + "<>'audio/amr'",
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		// 妈妈再也不用担心我的录音!
		// 过滤小于30s的音乐
		if (cursor != null) {
			cursor.moveToFirst();
			mMusicDatas = new MusicData[cursor.getCount()];
			long now = System.nanoTime();
			for (int i = 0; i < cursor.getCount(); i++) {
				mMusicDatas[i] = generateMusicData(cursor);
				cursor.moveToNext();
			}
			MyLogger.i(TAG, "generateMusicData used "
					+ (System.nanoTime() - now) / 1000000 + " ms");
			try {
				mLvMain.setAdapter(new MusicListAdapter(this, mMusicDatas));
			} catch (Exception e) {
				MyLogger.e(TAG, "无音乐");
				setContentView(R.layout.empty);
				// XXX 先这样将就
			}
			cursor.close();
		}
		initShakeDetector();
	}

	/**
	 * 从给定的cursor取值生成MusicData
	 * 
	 * @param cursor
	 *            信息来源
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
	 * 分享音乐的主调方法，将调用QueryAndShareMusicInfo类
	 * 
	 * @param music
	 *            要分享的音乐
	 */
	private void shareMusic(MusicData music) {
		QueryAndShareMusicInfo query = new QueryAndShareMusicInfo(music,
				mContext, mHandler);
		mHttpQuestHandler.obtainMessage(
				Consts.NetAccessIntent.QUERY_AND_SHARE_MUSIC_INFO, query)
				.sendToTarget();
		Toast.makeText(this, getString(R.string.querying), Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * 播放音乐的主调方法
	 * 
	 * @param music
	 *            音乐信息
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
	 * 刷新音乐列表
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
	 * 保存微博以及发送状态，备用
	 * 
	 * @param content
	 *            微博内容
	 * @param checked
	 *            是否关注开发者
	 * @param artworkUrl
	 *            微博图片地址
	 * @param fileName
	 *            图片文件名
	 */
	private void saveSendStatus(String content, boolean checked,
			String artworkUrl, String fileName, String annotation) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				Consts.Preferences.SHARE, Context.MODE_PRIVATE).edit();
		editor.putBoolean("read", true);
		editor.putString("content", content);
		editor.putBoolean("willFollow", checked);
		editor.putString("artworkUrl", artworkUrl);
		editor.putString("fileName", fileName);
		editor.putString("annotation", annotation);
		editor.commit();
	}

	/**
	 * 通过其他App发送音乐文件
	 * 
	 * @param whichMusic
	 *            待发送的音乐信息
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
	 * 通过返回的更新信息显示对话框提示用户是否更新程序
	 * 
	 * @param info
	 *            传回的各种更新信息
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
	 * 判断是否首次启动并显示欢迎对话框
	 */
	private void firstShow() {
		SharedPreferences preferences = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		int oldVersion = preferences.getInt("versionCode", 0);
		if (oldVersion < Main.sVersionCode) {
			MyLogger.d(TAG, "版本号更新");
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
			preferences.edit().putInt("versionCode", Main.sVersionCode)
					.commit();
		}

	}
}
