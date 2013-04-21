package com.paperairplane.music.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.paperairplane.music.share.R;

public class MusicListAdapter extends BaseAdapter {
	private Context mContext;
	private MusicData mMusicDatas[];

	public MusicListAdapter(Context context, MusicData musicdatas[]) {
		mContext = context;
		mMusicDatas = musicdatas;// ²»ÒªCursorÁË¡­¡­
	}

	public int getCount() {
		if (mMusicDatas != null) {
			return mMusicDatas.length;
		}
		return 0;
	}

	public Object getItem(int position) {
		return mMusicDatas[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.musiclist_item, null);
		TextView tvTitle = (TextView) convertView
				.findViewById(R.id.musicname);
		TextView tvSinger = (TextView) convertView
				.findViewById(R.id.singer);
		tvTitle.setText(mMusicDatas[position].getTitle());
		tvSinger.setText(mMusicDatas[position].getArtist());
		return convertView;
	}

}