package com.paperairplane.music.share;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.paperairplane.music.share.R;

public class MusicListAdapter extends BaseAdapter implements SectionIndexer {
	private Context mContext;
	private MusicData mMusicDatas[];
	private HashMap<Character, Integer> mSectionMap;
	private Character[] sectionCharArr;
	private boolean isTextColorSet;
	private int textColor;

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
		mSectionMap = new HashMap<Character, Integer>();
		char charTemp = 0, charNow;// 随手赋值不然第一次会出错
		for (int i = length - 1; i >= 0; i--) {
			charNow = mMusicDatas[i].getFirstChar();
			if (charTemp == charNow) {
				mSectionMap.remove(charTemp);
			}
			charTemp = mMusicDatas[i].getFirstChar();
			mSectionMap.put(charTemp, i);
			/*
			 * 说说我的思路,倒序遍历,先只管放进去,遇到字符相同的话扔掉原来的写新的 这样不会出现没有的字符
			 */
		}
		getSections();
		// TODO 文本颜色选择
		isTextColorSet=false;
		SharedPreferences preference = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		if (preference.contains(Consts.Preferences.TEXT_COLOR)) {
			textColor = android.graphics.Color.parseColor(preference.getString(
					Consts.Preferences.TEXT_COLOR, ""));
			isTextColorSet=true;
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
		if(isTextColorSet){
			tvSinger.setTextColor(textColor);
			tvTitle.setTextColor(textColor);
		}
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		char sectionChar = sectionCharArr[section];
		if (section == mSectionMap.size() - 1)
			sectionChar = Consts.UNKNOWN_CHAR;
		int position = mSectionMap.get(sectionChar);
		return position;
	}

	@Override
	public int getSectionForPosition(int position) {
		int section = 0;
		char charNow = mMusicDatas[position].getFirstChar();
		for (char charTemp : sectionCharArr) {
			if (charNow != charTemp) {
				section++;
				continue;
			}
			if (charNow == charTemp) {
				break;
			}
		}
		if (charNow == Consts.UNKNOWN_CHAR) {
			return mSectionMap.size() - 1;
		}
		return section;
	}

	@Override
	public Object[] getSections() {
		// char[] s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".toCharArray();
		int arraySize = mSectionMap.size();
		sectionCharArr = new Character[arraySize];
		char nowChar = 'A';
		for (int i = 0, j = 0; i < 26; i++, nowChar++) {
			if (mSectionMap.containsKey(Character.valueOf(nowChar))) {
				sectionCharArr[j] = Character.valueOf(nowChar);
				j++;
			}
		}
		sectionCharArr[arraySize - 1] = '#';
		// 我觉着这样应该是可以的吧
		return sectionCharArr;
	}

}