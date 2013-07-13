package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;
import com.paperairplane.music.share.MusicData;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class SearchDialogFragment extends AbsDialogFragment {
	private SearchDialogFragment.OnShareMusicListener listenerSearch;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog dialogSearch;
		View search = LayoutInflater.from(getActivity()).inflate(
				R.layout.search, null);
		final EditText etTitle = (EditText) search.findViewById(R.id.et_title);
		final EditText etArtist = (EditText) search
				.findViewById(R.id.et_artist);
		final EditText etAlbum = (EditText) search.findViewById(R.id.et_album);
		DialogInterface.OnClickListener listenerClick = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (etTitle.getText().toString().trim().equals("")) {
					new EmptyDialogFragment().show(getFragmentManager(),
							"emptyDialog");
				} else {
					MusicData music = new MusicData();
					music.setAlbum(etAlbum.getText().toString());
					music.setAlbumId(Consts.NULL);
					music.setArtist(etArtist.getText().toString());
					music.setTitle(etTitle.getText().toString());
					listenerSearch.onShareMusic(music);
					getDialog().dismiss();
				}
			}
		};
		dialogSearch = new AlertDialog.Builder(getActivity()).setView(search)
				.setCancelable(true)
				.setPositiveButton(R.string.share, listenerClick)
				.setTitle(R.string.search)
				.setIcon(android.R.drawable.ic_dialog_info).create();
		return dialogSearch;
	}

	public interface OnShareMusicListener {
		public void onShareMusic(MusicData music);
	}

	public void setOnShareMusicListener(
			SearchDialogFragment.OnShareMusicListener listener) {
		this.listenerSearch = listener;

	}
}
