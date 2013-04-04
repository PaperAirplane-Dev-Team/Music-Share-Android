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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class AtSuggestionActivity extends Activity {
	private ListView listView;
	private EditText et;
	private List<String> data = new ArrayList<String>();
	private Handler handler;
	private ArrayAdapter<String> adapter;
	private Thread refreshThread;
	private Intent i;
	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_suggestion);
		i =new Intent(AtSuggestionActivity.this,Main.class);
		extras = new Bundle();
		extras.putAll(getIntent().getExtras());
		i.putExtras(extras);
		setResult(RESULT_CANCELED,i);
		handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Consts.Status.DATA_CHANGED:
					try {
						data.clear();
						data.addAll((ArrayList<String>) msg.obj);
					} catch (Exception e) {

					}
					adapter.notifyDataSetChanged();
					break;
				}
			}
		};
		adapter = new ArrayAdapter<String>(AtSuggestionActivity.this,
				android.R.layout.simple_expandable_list_item_1, data);
		listView = (ListView) findViewById(R.id.listView_at);
		listView.setAdapter(adapter);
		et = (EditText) findViewById(R.id.editText_at);
		et.setText("@");
		et.setSelection(1);
		et.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable e) {
				try {
					data.remove(0);
				} catch (Exception e1) {
				}
				data.add(0, e.toString());
				adapter.notifyDataSetChanged();
				lookForSuggestions();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int arg2,
					int arg3) {

			}

		});
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				int selection,start;
				start = getIntent().getExtras().getInt("start");
				StringBuffer result = new StringBuffer(getIntent().getExtras().getString("content"));
				result.replace(start, start+1, adapter.getItem(position));
				selection = start + adapter.getItem(position).length();
				Log.d(Consts.DEBUG_TAG,result.toString());
				extras.putString("content", result.toString());
				extras.putInt("selection", selection);
				i.putExtras(extras);
				setResult(RESULT_OK,i);
				finish();
			}
			
		});
	}

	private void lookForSuggestions() {
		refreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(Consts.DEBUG_TAG, "搜寻建议");
				WeiboParameters params = new WeiboParameters();
				params.add("access_token", Main.accessToken.getToken());
				params.add("q", et.getText().toString().replace("@", ""));
				String url = Consts.Url.API_SUGGESTION;
				final Message m = handler
						.obtainMessage(Consts.Status.DATA_CHANGED);
				try {
					AsyncWeiboRunner.request(url, params, "GET",
							new RequestListener() {
								@Override
								public void onComplete(String result) {
									Log.v("Music Share DUBUG", "获取到结果："
											+ result);
									final List<String> fetched_data = new ArrayList<String>();
									fetched_data.add(0, et.getText().toString());
									try {
										JSONArray array = new JSONArray(result);
										for (int i = 0; i < array.length(); i++) {
											String nickname = "@"
													+ array.getJSONObject(i)
															.getString(
																	"nickname")+" ";
											fetched_data.add(nickname);
											Log.v(Consts.DEBUG_TAG, "添加数据"
													+ nickname);
										}
										m.obj = fetched_data;
										m.sendToTarget();
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								@Override
								public void onError(WeiboException e) {
									e.printStackTrace();
									Log.e("Music Share DUBUG", "获取错误"+e.getStatusCode()+e.getMessage());
								}

								@Override
								public void onIOException(IOException arg0) {
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		refreshThread.start();
	}


}
