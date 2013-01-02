package com.paperairplane.music.share;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;



public class Utilities {
	private final static String DEBUG_TAG = "Music Share DEBUG";
	//哦我终于精简了它!
	public static String convertDuration(int _duration){
		_duration /= 1000;
		String min,hour,sec;
		if (_duration / 3600>0){
			return (((hour = ((Integer) (_duration / 3600)).toString()).length()==1)?"0"+hour:hour)+":"+(((min = ((Integer) (_duration / 60)).toString()).length()==1)?"0"+min:min)+":"+(((sec = ((Integer) (_duration % 60)).toString()).length()==1)?"0"+sec:sec);
		}
		else{
			return (((min = ((Integer) (_duration / 60)).toString()).length()==1)?"0"+min:min)+":"+(((sec = ((Integer) (_duration % 60)).toString()).length()==1)?"0"+sec:sec);
		}
		
	}
	
	public static long calculateLength(CharSequence c) {
		double len = 0;
		for (int i = 0; i < c.length(); i++) {
			int tmp = (int) c.charAt(i);
			if (tmp > 0 && tmp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
	}
	
	public static InputStream getImageStream(String artwork_url) throws Exception {
		URL url = new URL(artwork_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return conn.getInputStream();
		}
		return null;
	}

	public static void saveFile(Bitmap bitmap, String fileName,String artwork_path)
			throws IOException {
		File dirFile = new File(artwork_path);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File artwork = new File(artwork_path + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(artwork));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();

	}
	
	public static String getArtwork(String artwork_url, String title,String artwork_path) {
		String fileName = title + ".jpg";
		if (new File(artwork_path+fileName).exists())return fileName;
		try {
			Bitmap bitmap = BitmapFactory
					.decodeStream(Utilities.getImageStream(artwork_url));
			Utilities.saveFile(bitmap, fileName,artwork_path);
			Log.v(DEBUG_TAG, "获取专辑封面成功");
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(DEBUG_TAG, "获取专辑封面失败" + e.getMessage());
			return null;
		}
	}
}
