package com.paperairplane.music.share;

import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class IntentResolver {
	private static Context mCtx;

	public static void handleIntent(Context ctx, Intent i) {
		mCtx = ctx;
		boolean view = i.getAction().equals(Intent.ACTION_VIEW);
		final PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> info = pm.queryIntentActivities(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		// 这里区分ACTION_SEND和ACTION_VIEW
		if (view) {
			// 若为ACTION_VIEW,去除分享选项
			Iterator<ResolveInfo> it = info.iterator();
			while (it.hasNext()) {
				if (it.next().labelRes == R.string.title_activity_main) {
					it.remove();
				}
			}
		}
		showDialog(info, view);
	}
//TODO To be continued.
	private static void showDialog(List<ResolveInfo> info, boolean view) {
		Dialog intentDialog = new Dialog(mCtx);
		ListView v = new ListView(mCtx);
		v.setAdapter(new ListAdapter(){
			View vwItem = LayoutInflater.from(mCtx).inflate(R.layout.intent_item, null);
			ImageView ivItemIcon = (ImageView)vwItem.findViewById(R.id.intent_item_icon);
			TextView tvItemText = (TextView)vwItem.findViewById(R.id.intent_item_text);
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getItem(int arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getItemViewType(int arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public View getView(int arg0, View arg1, ViewGroup arg2) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void registerDataSetObserver(DataSetObserver arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void unregisterDataSetObserver(DataSetObserver arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isEnabled(int arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		intentDialog.setContentView(v);
		String title = (view) ? mCtx.getString(R.string.how_to_play) : mCtx
				.getString(R.string.how_to_share);
		intentDialog.setTitle(title);
	}

}
