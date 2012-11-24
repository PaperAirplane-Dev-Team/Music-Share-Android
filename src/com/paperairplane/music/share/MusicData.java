package com.paperairplane.music.share;

class MusicData{
	private String title,artist,duration,path,album;
	public void setPath(String _path){
		path=_path;
	}
	public void setTitle(String _title){
		title=_title;
	}
	public void setArtist(String _artist){
		artist=_artist;
	}
	public void setAlbum(String _album){
		album=_album;
	}
	public void setDuration(String _duration){
		duration=_duration;
	}
	public String getPath(){
		return path;
	}
	public String getTitle(){
		return title;
	}
	public String getArtist(){
		return artist;
	}
	public String getDuration(){
		return duration;
	}
	public String getAlbum(){
	    return album;
	}
	public MusicData(){
		
	}

}