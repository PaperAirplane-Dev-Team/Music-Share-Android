package com.paperairplane.musicshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class RefreshMusicListReceiver extends BroadcastReceiver{
    private Toast toast = null;
	public void onReceive(Context context, Intent intent){
		String action=intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
			//System.out.println("Scanning");
			toast = Toast.makeText(context,R.string.refresh_on_process, Toast.LENGTH_SHORT);
			toast.show();
		}else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
            toast.setText(R.string.refresh_success);
            toast.show();
		}
		
	}

}
