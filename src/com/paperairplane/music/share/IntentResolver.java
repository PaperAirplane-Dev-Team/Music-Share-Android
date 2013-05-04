package com.paperairplane.music.share;

import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
	private Context mCtx;
	private PackageManager mPm;
	private Handler mHandler;

	/**
	 * 重新处理Intent，在ResolveInfo中去除本应用，并加入内置分享器
	 * 
	 * @param ctx
	 *            当前Context
	 * @param i
	 *            待处理的Intent
	 * @param handler
	 *            处理UI消息的Handler
	 */
	public void handleIntent(Context ctx, Intent i, Handler handler) {
		mCtx = ctx;
		mHandler = handler;
		boolean view = i.getAction().equals(Intent.ACTION_VIEW);
		mPm = ctx.getPackageManager();
		List<ResolveInfo> info = mPm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		MyLogger.d(Consts.DEBUG_TAG, "handleIntent");
		// 去除分享选项
		for (ResolveInfo ri:info){
//TODO 待完善
		}
		/*
		Iterator<ResolveInfo> it = info.iterator();
		while (it.hasNext()) {
			MyLogger.d(Consts.DEBUG_TAG, "labelRes->"+it.next().activityInfo.packageName.startsWith("com.paperairplane"));
			if (it.next().labelRes == R.string.title_activity_main) {
				it.remove();
			}
		}
		*/
		if (!view) {
			// 若为SEND，增加内置的微博发布器
			ResolveInfo share2weibo = new ResolveInfo();
			share2weibo.icon = R.drawable.weibo_logo;
			share2weibo.labelRes = R.string.share2weibo;
			share2weibo.activityInfo = new ActivityInfo();
			share2weibo.activityInfo.flags = Consts.ShareMeans.INTERNAL;
			info.add(0, share2weibo);
		}
		// 显示Intent列表
		showDialog(info, view, i);
	}

	private class IntentListAdapter extends BaseAdapter {

		List<ResolveInfo> info;

		public IntentListAdapter(List<ResolveInfo> info) {
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
			View vwItem = LayoutInflater.from(mCtx).inflate(
					R.layout.resolve_list_item, null);
			ImageView ivItemIcon = (ImageView) vwItem.findViewById(R.id.icon);
			TextView tvItemLabel = (TextView) vwItem
					.findViewById(android.R.id.text1);
			TextView tvItemExtended = (TextView) vwItem
					.findViewById(android.R.id.text2);
			// 为控件定义资源
			Drawable icon;
			String label;
			ResolveInfo ri = info.get(position);
			if (ri.activityInfo.flags != Consts.ShareMeans.INTERNAL) {
				// 外部应用通过PackageManager获取资源
				icon = ri.activityInfo.loadIcon(mPm);
				label = ri.activityInfo.loadLabel(mPm).toString();
				/*
				 * FIXME 总觉得我们获取的东西还不对,例如(求不要吐槽)微信的分享有两个
				 * 一个分享到朋友圈一个发送给朋友,现在都显示成"微信" 我看那代码里面有一个什么来着,好像是LabeledIntent
				 * 确实不对，ResolveInfo有个方法……
				 */
			} else {
				// 内部编辑器直接从资源中获取
				icon = mCtx.getResources().getDrawable(ri.icon);
				label = mCtx.getString(ri.labelRes);
			}
			ivItemIcon.setImageDrawable(icon);
			tvItemLabel.setText(label);
			tvItemExtended.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT < 11) {
				/*
				 * 泪奔……早知当初就不支持2.X了 这里改变文字颜色以符合系统风格
				 * 另外，text_light是黑色的text_black是白色的……
				 * 我告诉你在我的Holo上面没法看,背景也是黑的
				 * FIXME
				 */
				tvItemLabel.setTextColor(mCtx.getResources().getColor(
						android.R.color.primary_text_light));

			}
			return vwItem;
		}

	}

	/**
	 * 显示ResolveInfo的List
	 * 
	 * @param info
	 * @param view
	 * @param i
	 */
	private void showDialog(final List<ResolveInfo> info, boolean view,
			final Intent i) {
		final Dialog intentDialog = new Dialog(mCtx);
		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ResolveInfo ri = info.get(position);
				boolean isInternal = (ri.activityInfo.flags == Consts.ShareMeans.INTERNAL);
				if (!isInternal) {
					// 采用其它分享方式
					Intent intent = new Intent(i);
					intent.setFlags(intent.getFlags()
							& ~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
							| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);

					intent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName,
							ri.activityInfo.name));
					mCtx.startActivity(intent);
				} else {
					// 采用内置的分享方式
					Bundle bundle;
					bundle = i.getExtras();
					Message m = mHandler.obtainMessage(
							Consts.Status.SEND_WEIBO, bundle);
					mHandler.sendMessage(m);
				}
				intentDialog.cancel();
				// 你忘了这个!不然总是留着
			}

		};

		ListView v = new ListView(mCtx);
		v.setCacheColorHint(0);
		if (Build.VERSION.SDK_INT < 11) {
			// 这里改变对话框背景以符合系统风格
			v.setBackgroundColor(mCtx.getResources().getColor(
					android.R.color.primary_text_dark));
		}
		v.setAdapter(new IntentListAdapter(info));
		v.setOnItemClickListener(listener);
		intentDialog.setContentView(v);
		String title = (view) ? mCtx.getString(R.string.how_to_play) : mCtx
				.getString(R.string.how_to_share);
		intentDialog.setTitle(title);
		intentDialog.show();

	}

}
