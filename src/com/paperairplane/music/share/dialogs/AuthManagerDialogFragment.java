package com.paperairplane.music.share.dialogs;

import java.text.DateFormat;
import java.util.Date;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.Consts.SNS;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.SnsHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AuthManagerDialogFragment extends AbsDialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ListView v = new ListView(getActivity());
		v.setAdapter(new BaseAdapter(){

			@Override
			public int getCount() {
				return 2;
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public long getItemId(int arg0) {
				return 0;
			}
			@Override
			public View getView(int id, View arg1, ViewGroup arg2) {
				View vItem = LayoutInflater.from(getActivity()).inflate(R.layout.sns_list_item, null);
				ImageView ivIcon = (ImageView) vItem.findViewById(R.id.icon);
				ImageView ivDelete = (ImageView) vItem.findViewById(R.id.clear);
				TextView tvName = (TextView)vItem.findViewById(android.R.id.text1);
				TextView tvTime = (TextView)vItem.findViewById(android.R.id.text2);
				String name = null;
				long time=0 ;
				SharedPreferences preferences = getActivity().getApplicationContext().getSharedPreferences(Consts.Preferences.WEIBO,
						Context.MODE_APPEND);
				final SnsHelper sh = SnsHelper.getInstance();
				switch(id){
				case 0:
					ivIcon.setImageResource(R.drawable.weibo_logo);
					name = preferences.getString(SNS.WEIBO.name()+"name",null);
				    time = preferences.getLong(SNS.WEIBO.name()+"expiresTime", 0);
					vItem.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							sh.authorize(getActivity(),SNS.WEIBO);
						}
					});
					ivDelete.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							sh.unauthorize(SNS.WEIBO);
						}
						
					});
				    break;
				case 1:
					ivIcon.setImageResource(R.drawable.renren_logo);
					name = preferences.getString(SNS.RENREN.name()+"name", null);
				    time = preferences.getLong(SNS.RENREN.name()+"expiresTime", 0);
					vItem.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							sh.authorize(getActivity(),SNS.RENREN);
						}
					});
					ivDelete.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							sh.unauthorize(SNS.RENREN);
						}
						
					});
					break;
				}
				DateFormat df = DateFormat.getDateTimeInstance();
				Date date = new Date(time);
				String formattedTime = df.format(date);
				tvTime .setText(getString(R.string.expires_time)+formattedTime);
				if (name == null){
					name = getString(R.string.auth);
					tvTime.setVisibility(View.GONE);
					ivDelete.setVisibility(View.GONE);
				}
				tvName.setText(name);


				return vItem;
			}
			
		});
		Dialog dialog = new AlertDialog.Builder(getActivity())
		.setTitle(R.string.account_manager)
		.setView(v)
		.create();
		return dialog;
	}
}
