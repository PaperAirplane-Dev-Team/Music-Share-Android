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
	private MusicData musicDatas[];

	public MusicListAdapter(Context context, MusicData _musicdata[]) {
		mContext = context;
		musicDatas = _musicdata;// ²»ÒªCursorÁË¡­¡­
	}

	public int getCount() {
		if (musicDatas != null) {
			return musicDatas.length;
		}
		return 0;
	}

	public Object getItem(int position) {
		return musicDatas[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.musiclist_item, null);
		TextView textTitle = (TextView) convertView
				.findViewById(R.id.musicname);
		TextView textSingerAndAlbum = (TextView) convertView
				.findViewById(R.id.singer_and_album);
		TextView textDuration = (TextView) convertView
				.findViewById(R.id.duration);
		textTitle.setText(musicDatas[position].getTitle());
		textSingerAndAlbum.setText(musicDatas[position].getArtist()+"\n"+musicDatas[position].getAlbum());
		textDuration.setText(musicDatas[position].getDuration());
		return convertView;
	}

}