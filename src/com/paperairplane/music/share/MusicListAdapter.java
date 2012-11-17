package com.paperairplane.music.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.paperairplane.music.share.R;

public class MusicListAdapter extends BaseAdapter {
	private Context mcontext;
	private MusicData musicdata[];
	String VERY_LONG="                                                                                    ";
	
	public MusicListAdapter(Context context, MusicData _musicdata[]){
		mcontext = context;
		musicdata = _musicdata;//²»ÒªCursorÁË¡­¡­
	}
	
	public int getCount() {
		return musicdata.length;
	}
	
	public Object getItem(int position) {
		return musicdata[position];
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(mcontext).inflate(
				R.layout.musiclist_item, null);
		TextView music_title = (TextView) convertView.findViewById(R.id.musicname);
		TextView music_singer = (TextView) convertView.findViewById(R.id.singer);
		music_title.setText(musicdata[position].getTitle()+"("+musicdata[position].getDuration()+")"+VERY_LONG);
		music_singer.setText(musicdata[position].getArtist()+VERY_LONG);
		return convertView;
	}
	
	
}