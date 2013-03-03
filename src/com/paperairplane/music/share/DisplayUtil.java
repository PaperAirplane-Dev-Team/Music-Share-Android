package com.paperairplane.music.share;

import android.app.Activity;
import android.util.DisplayMetrics;


public class DisplayUtil {
	public static int getAdaptedSize(Activity activity){
		int size;
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		size = metrics.widthPixels /10 *3 ;
		return size;
	}
}
