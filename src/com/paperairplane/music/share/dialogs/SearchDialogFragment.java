package com.paperairplane.music.share.dialogs;

import com.paperairplane.music.share.Consts;
import com.paperairplane.music.share.R;
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
		final EditText et_title = (EditText) search.findViewById(R.id.et_title);
		final EditText et_artist = (EditText) search
				.findViewById(R.id.et_artist);
		final EditText et_album = (EditText) search.findViewById(R.id.et_album);
		DialogInterface.OnClickListener listenerClick = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (et_title.getText().toString().trim().equals("")) {
					new EmptyDialogFragment().show(getFragmentManager(),
							"emptyDialog");

				} else {
					listenerSearch.onShareMusic(et_title.getText().toString(), et_artist
							.getText().toString(), et_album.getText()
							.toString(), Consts.NULL);
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
		public void onShareMusic(String title, String artist, String album,
				long albumId);
	}

	public void setOnShareMusicListener(
			SearchDialogFragment.OnShareMusicListener listener) {
		this.listenerSearch = listener;

	}
}
