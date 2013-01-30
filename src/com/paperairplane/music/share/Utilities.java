package com.paperairplane.music.share;



public class Utilities {
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
	

}
