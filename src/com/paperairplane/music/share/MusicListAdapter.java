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
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.paperairplane.music.share.R;
import com.paperairplane.music.share.cache.ImageLoader;

/**
 * 用于填充主界面ListView的Adapater
 * 
 * @author Harry Chen (<a href="mailto:chenshengqi1@gmail.com">Harry Chen</a>)
 * @author Xavier Yao (<a href="mailto:xavieryao@me.com">Xavier Yao</a>)
 * @see <a
 *      href="http://www.github.com/PaperAirPlane-Dev-Team/Music-Share-Android">Our
 *      GitHub</a>
 */
public class MusicListAdapter extends BaseAdapter implements SectionIndexer {
	private Context mContext;
	private MusicData mMusicDatas[];
	private HashMap<Character, Integer> mSectionMap;
	private Character[] mSectionCharArr;
	private boolean mIsTextColorSet, mHasUnknownChar;
	private int mTextColor;
	private ImageLoader mImageLoader;
	
	/**
	 * 构造方法
	 * 
	 * @param context
	 *            App上下文
	 * @param musicdatas
	 *            获取到的所有音乐信息
	 */
	public MusicListAdapter(Context context, MusicData musicdatas[]) {
		mContext = context;
		mMusicDatas = musicdatas;// 不要Cursor了……
		mImageLoader = new ImageLoader(mContext, mMusicDatas);
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
		mIsTextColorSet = false;
		SharedPreferences preference = mContext.getSharedPreferences(
				Consts.Preferences.GENERAL, Context.MODE_PRIVATE);
		String color=preference
				.getString(Consts.Preferences.TEXT_COLOR, "");
		if (!color.equals("")) {
			mTextColor = android.graphics.Color.parseColor(color);
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
		ViewHolder holder = null;//我只能说我太渣渣了这个都不知道唉
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.musiclist_item, null);
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView
					.findViewById(R.id.musicname);
			holder.tvArtist = (TextView) convertView.findViewById(R.id.singer);
			holder.ivArtwork = (ImageView) convertView.findViewById(R.id.imageView1);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvTitle.setText(mMusicDatas[position].getTitle());
		holder.tvArtist.setText(mMusicDatas[position].getArtist());
		holder.ivArtwork.setImageResource(R.drawable.albumart_mp_unknown_list);
		
		mImageLoader.DisplayImage(
				"" + mMusicDatas[position].getAlbumId(),
				holder.ivArtwork,
				false);
		
		if (mIsTextColorSet) {
			holder.tvArtist.setTextColor(mTextColor);
			holder.tvTitle.setTextColor(mTextColor);
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tvTitle;
		TextView tvArtist;
		ImageView ivArtwork;
	}

	@Override
	public int getPositionForSection(int section) {
		char sectionChar = mSectionCharArr[section];
		if (mHasUnknownChar && section == mSectionMap.size() - 1)
			// 同样防止多事
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
		mSectionCharArr = mSectionMap.keySet().toArray(mSectionCharArr);
		
		// Sort
		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < arraySize; j++) {
				if (mSectionCharArr[i].charValue() < mSectionCharArr[j].charValue()) {
					Character c = mSectionCharArr[i];
					mSectionCharArr[i] = mSectionCharArr[j];
					mSectionCharArr[j] = c;
				}
			}
		}

		/*
		 * 这样的话应该可以防止莫名其妙的ArrayIndexOutOfBound了吧 但是回过头来说, 你给我看的异常里面下标是-1,
		 * 也就是arraySize是0 这是怎样一种节奏啊,难道……没有音乐?
		 */
		return mSectionCharArr;
	}

}
