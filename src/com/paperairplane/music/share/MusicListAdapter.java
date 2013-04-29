package com.paperairplane.music.share;

import java.util.Arrays;
import java.util.Comparator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.paperairplane.music.share.R;

//TODO 有点错乱的说…太晚了脑子不清醒
public class MusicListAdapter extends BaseAdapter implements SectionIndexer {
	private Context mContext;
	private MusicData mMusicDatas[];
	private int[] mMap;

	public MusicListAdapter(Context context, MusicData musicdatas[]) {
		mContext = context;
		mMusicDatas = musicdatas;// 不要Cursor了……
		Arrays.sort(mMusicDatas, new Comparator<MusicData>() {
			@Override
			public int compare(MusicData lhs, MusicData rhs) {
				return lhs.compareTo(rhs);
			}
		});
		int length = mMusicDatas.length;
		mMap = new int[27];
		for (int i = length - 1; i >= 0; i--) {
			mMap[mMusicDatas[i].getFirstChar() - 65] = i;
		}
	}

	public int getCount() {
		// if (mMusicDatas != null) {
		return mMusicDatas.length;
		// }
		// return 0;
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
		TextView tvTitle = (TextView) convertView.findViewById(R.id.musicname);
		TextView tvSinger = (TextView) convertView.findViewById(R.id.singer);
		tvTitle.setText(mMusicDatas[position].getTitle());
		tvSinger.setText(mMusicDatas[position].getArtist());
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		if (section != 0 && mMap[section] == 0) {
			int i = section++;
			while (mMap[i] == 0){
				i++;
			}
			return mMap[i];
		}
		return mMap[section];
	}

	@Override
	public int getSectionForPosition(int position) {

		return mMusicDatas[position].getFirstChar() - 65;
	}

	@Override
	public Object[] getSections() {
		char[] s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".toCharArray();
		Character[] charArr = new Character[27];
		for (int i = 0; i < 27; i++)
			charArr[i] = Character.valueOf(s[i]);
		return charArr;
	}

}