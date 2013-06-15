package com.paperairplane.music.share;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.paperairplane.music.share.MyLogger;

/**
 * 微博At自动补全
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class AtSuggestionActivity extends SherlockActivity {
	private ListView mLvAtSuggestion;
	private EditText mEtUserNick;
	private List<String> mListSuggestion = new ArrayList<String>();
	private Handler mHandler;
	private ArrayAdapter<String> mAdapterSugestion;
	private Intent mIntent;
	private Bundle mExtras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_suggestion);
		mIntent = new Intent(AtSuggestionActivity.this, Main.class);
		mExtras = new Bundle();
		mExtras.putAll(getIntent().getExtras());
		mIntent.putExtras(mExtras);
		setResult(RESULT_CANCELED, mIntent);
		mHandler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Consts.Status.DATA_CHANGED:
					try {
						mListSuggestion.clear();
						mListSuggestion.addAll((ArrayList<String>) msg.obj);
					} catch (Exception e) {

					}
					mAdapterSugestion.notifyDataSetChanged();
					break;
				}
			}
		};
		mAdapterSugestion = new ArrayAdapter<String>(AtSuggestionActivity.this,
				android.R.layout.simple_expandable_list_item_1, mListSuggestion);
		mLvAtSuggestion = (ListView) findViewById(R.id.listView_at);
		mLvAtSuggestion.setAdapter(mAdapterSugestion);
		mEtUserNick = (EditText) findViewById(R.id.editText_at);
		mEtUserNick.setText("@");
		mEtUserNick.setSelection(1);
		mEtUserNick.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable e) {
				try {
					mListSuggestion.remove(0);
				} catch (Exception e1) {
				}
				mListSuggestion.add(0, e.toString());
				mAdapterSugestion.notifyDataSetChanged();
				lookForSuggestions();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2,
					int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int arg2,
					int arg3) {

			}

		});
		mLvAtSuggestion.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int selection, start;
				start = getIntent().getExtras().getInt("start");
				StringBuffer result = new StringBuffer(getIntent().getExtras()
						.getString(Intent.EXTRA_TEXT));
				result.replace(start, start + 1,
						mAdapterSugestion.getItem(position));
				selection = start
						+ mAdapterSugestion.getItem(position).length();
				mExtras.putString(Intent.EXTRA_TEXT, result.toString());
				mExtras.putInt("selection", selection);
				mIntent.putExtras(mExtras);
				setResult(RESULT_OK, mIntent);
				finish();
			}

		});
	}

	/**
	 * 从新浪微博查询可能要@的人
	 */
	private void lookForSuggestions() {

		WeiboParameters params = new WeiboParameters();
		params.add("access_token", Main.sAccessToken.getToken());
		params.add("q", mEtUserNick.getText().toString().replace("@", ""));
		String url = Consts.Url.API_SUGGESTION;
		final Message m = mHandler.obtainMessage(Consts.Status.DATA_CHANGED);
		try {
			AsyncWeiboRunner.request(url, params, "GET", new RequestListener() {
				@Override
				public void onComplete(String result) {
					final List<String> fetched_data = new ArrayList<String>();
					fetched_data.add(0, mEtUserNick.getText().toString());
					try {
						JSONArray array = new JSONArray(result);
						for (int i = 0; i < array.length(); i++) {
							String nickname = "@"
									+ array.getJSONObject(i).getString(
											"nickname") + " ";
							fetched_data.add(nickname);
							MyLogger.v(Consts.DEBUG_TAG, "添加数据" + nickname);
						}
						m.obj = fetched_data;
						m.sendToTarget();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError(WeiboException e) {
					e.printStackTrace();
					MyLogger.e(Consts.DEBUG_TAG,
							"获取错误" + e.getStatusCode() + e.getMessage());
				}

				@Override
				public void onIOException(IOException arg0) {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
