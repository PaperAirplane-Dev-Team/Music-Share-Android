package com.paperairplane.music.share;

import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.paperairplane.music.share.MyLogger;

public class IntentResolver {
	private static Context mCtx;
	private static PackageManager pm;
	private static Handler mHandler;

	public static void handleIntent(Context ctx, Intent i,Handler handler) {
		mCtx = ctx;
		mHandler = handler;
		boolean view = i.getAction().equals(Intent.ACTION_VIEW);
		pm = ctx.getPackageManager();
		List<ResolveInfo> info = pm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		MyLogger.d(Consts.DEBUG_TAG,"handleIntent");
		// 这里区分ACTION_SEND和ACTION_VIEW
		Iterator<ResolveInfo> it = info.iterator();
		while (it.hasNext()) {
			if (it.next().labelRes == R.string.title_activity_main) {
				it.remove();
			}
		}
		if (!view) {
			// 若为ACTION_VIEW,去除分享选项
			//若为SEND，增加内置的微博发布器
			//同学,SEND也要去掉自己的,不然会有自己很jiong的
			//不对,目测不对,怎么样才能让播放还有发送的时候不出现自己……
			ResolveInfo share2weibo = new ResolveInfo();
			share2weibo.icon = R.drawable.weibo_logo;
			share2weibo.labelRes = R.string.share2weibo;
			share2weibo.activityInfo = new ActivityInfo();
			share2weibo.activityInfo.flags = Consts.ShareMeans.INTERNAL;
			info.add(0,share2weibo);
		}
		showDialog(info, view,i);
	}
	private static class IntentListAdapter extends BaseAdapter{

		List<ResolveInfo> info;
		
	    public IntentListAdapter(List<ResolveInfo> info){
	    	this.info = info;
	    }

		@Override
		public int getCount() {

			return info.size();
		}

		@Override
		public Object getItem(int position) {

			return info.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//找到资源
			//这东西CM弄来的?怎么这么难看啊……
			View vwItem = LayoutInflater.from(mCtx).inflate(R.layout.intent_item, null);
			ImageView ivItemIcon = (ImageView)vwItem.findViewById(R.id.intent_item_icon);
			TextView tvItemText = (TextView)vwItem.findViewById(R.id.intent_item_text);
			//为资源定义值
			Drawable icon ;
			String label;
			ResolveInfo ri = info.get(position);
			if (ri.activityInfo.flags!=Consts.ShareMeans.INTERNAL){
				icon = ri.activityInfo.loadIcon(pm);
				label = ri.activityInfo.loadLabel(pm).toString();
			}else{
				icon = mCtx.getResources().getDrawable(ri.icon);
				label = mCtx.getString(ri.labelRes);

			}
			ivItemIcon.setImageDrawable(icon);
			tvItemText.setText(label);
			return vwItem;
		}
		
	}
	private static void showDialog(final List<ResolveInfo> info, boolean view, final Intent i) {
		Dialog intentDialog = new Dialog(mCtx);
		OnItemClickListener listener = new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				ResolveInfo ri = info.get(position);
				boolean isInternal = (ri.activityInfo.flags == Consts.ShareMeans.INTERNAL);
				if (!isInternal){
					//采用其它分享方式
				       Intent intent = new Intent(i);
				        intent.setFlags(
				                intent.getFlags() &~
				                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				        intent.addFlags(
				                Intent.FLAG_ACTIVITY_FORWARD_RESULT |
				                Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		                ComponentName cn = new ComponentName(
		                        ri.activityInfo.applicationInfo.packageName,
		                        ri.activityInfo.name);
		                intent.setComponent(new ComponentName(cn.getPackageName(),cn.getClassName()));
		                mCtx.startActivity(intent);
				}else{
					//采用内置的分享方式
					Bundle bundle ;
					bundle = i.getExtras();
					Message m = mHandler.obtainMessage(Consts.Status.SEND_WEIBO, bundle);
					mHandler.sendMessage(m);
				}
				
			}
			
		};
		ListView v = new ListView(mCtx);
		v.setAdapter(new IntentListAdapter(info));
		v.setOnItemClickListener(listener);
		intentDialog.setContentView(v);
		String title = (view) ? mCtx.getString(R.string.how_to_play) : mCtx
				.getString(R.string.how_to_share);
		intentDialog.setTitle(title);
		intentDialog.show();
	}

}
