package com.paperairplane.music.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.paperairplane.music.share.R;
//import com.paperairplane.music.share.MyLogger;

public class MusicListAdapter extends BaseAdapter /*implements SectionIndexer */{
	private Context mContext;
	private MusicData mMusicDatas[];
//	private Map<Integer,String> mMap = new HashMap<Integer,String>();
	

	public MusicListAdapter(Context context, MusicData musicdatas[]) {
		mContext = context;
		mMusicDatas = musicdatas;// 不要Cursor了……
		/*
		int length = mMusicDatas.length;
		for (int i=0;i<length;i++){
			String firstChar = mMusicDatas[i].getTitle();
			if (firstChar.toLowerCase(Locale.getDefault()).startsWith(
					"the ")) {
				firstChar = firstChar.substring(4, 5);
			} else if (firstChar.toLowerCase(Locale.getDefault())
					.startsWith("a ")) {
				firstChar = firstChar.substring(2, 3);
			} else if (firstChar.toLowerCase(Locale.getDefault())
					.startsWith("an ")) {
				firstChar = firstChar.substring(3, 4);
			} else {
				firstChar = firstChar.substring(0, 1);
			}
			firstChar = PinyinHelper.toHanyuPinyinStringArray(firstChar.toCharArray()[0])[0].toUpperCase();
			MyLogger.d(Consts.DEBUG_TAG,"首字母+"+firstChar);
			mMap.put(i, firstChar);
		}
		*/
	}

	public int getCount() {
//		if (mMusicDatas != null) {
			return mMusicDatas.length;
//		}
//		return 0;
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
/*
	@Override
	public int getPositionForSection(int section) {
		//TODO Xas
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		
		return mMap.get(position).codePointAt(0);
	}

	@Override
	public Object[] getSections() {
		return null;
	}
*/
}