package com.android.gnotes;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.ViewGroup;

class NotesAdapter extends Adapter<NoteItemViewHolder> {

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onBindViewHolder(NoteItemViewHolder arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public NoteItemViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public NoteItem getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDeleteString(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setData(Cursor cursor) {
		
	}

	public void setOnItemClickListener(RecyclerViewItemClickListener listener) {

	}

	public void setOnItemLongClickListener(RecyclerViewItemLongClickListener listener) {

	}
}
