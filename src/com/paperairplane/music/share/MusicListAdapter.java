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

/**
 * 用于填充主界面ListView的Adapater
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our GitHub</a>
 */
public class MusicListAdapter extends BaseAdapter implements SectionIndexer {
	private Context mContext;
	private MusicData mMusicDatas[];
	private HashMap<Character, Integer> mSectionMap;
	private Character[] mSectionCharArr;
	private boolean mIsTextColorSet, mHasUnknownChar;
	private int mTextColor;

	/**
	 * 构造方法
	 * @param context App上下文
	 * @param musicdatas 获取到的所有音乐信息
	 */
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
		if (mMusicDatas[mMusicDatas.length - 1].getFirstChar() == Consts.UNKNOWN_CHAR) {
			mHasUnknownChar = true;
		}
		getSections();
		// TODO 文本颜色选择
		mIsTextColorSet = false;
		SharedPreferences preference = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		if (preference.contains(Consts.Preferences.TEXT_COLOR)) {
			mTextColor = android.graphics.Color.parseColor(preference
					.getString(Consts.Preferences.TEXT_COLOR, ""));
			mIsTextColorSet = true;
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
		if (mIsTextColorSet) {
			tvSinger.setTextColor(mTextColor);
			tvTitle.setTextColor(mTextColor);
		}
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		char sectionChar = mSectionCharArr[section];
		if (mHasUnknownChar && section == mSectionMap.size() - 1)
			//同样防止多事
			sectionChar = Consts.UNKNOWN_CHAR;
		int position = mSectionMap.get(sectionChar);
		return position;
	}

	@Override
	public int getSectionForPosition(int position) {
		int section = 0;
		char charNow = mMusicDatas[position].getFirstChar();
		for (char charTemp : mSectionCharArr) {
			if (charNow != charTemp) {
				section++;
				continue;
			}
			if (charNow == charTemp) {
				break;
			}
		}
		if (charNow == Consts.UNKNOWN_CHAR) {
			// 这里应该不会有问题, 因为如果没有这样的歌也就不会触发这个语句
			return mSectionMap.size() - 1;
		}
		return section;
	}

	@Override
	public Object[] getSections() {
		// char[] s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".toCharArray();
		int arraySize = mSectionMap.size();
		mSectionCharArr = new Character[arraySize];
		char nowChar = 'A';
		for (int i = 0, j = 0; i < 26; i++, nowChar++) {
			if (mSectionMap.containsKey(Character.valueOf(nowChar))) {
				mSectionCharArr[j] = Character.valueOf(nowChar);
				j++;
			}
		}
		if (mHasUnknownChar)
			mSectionCharArr[arraySize - 1] = '#';
		/*
		 * 这样的话应该可以防止莫名其妙的ArrayIndexOutOfBound了吧 但是回过头来说, 你给我看的异常里面下标是-1,
		 * 也就是arraySize是0 这是怎样一种节奏啊,难道……没有音乐?
		 */
		return mSectionCharArr;
	}

}