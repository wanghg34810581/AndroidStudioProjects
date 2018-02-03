package com.android.gnotes;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteItemViewHolder extends ViewHolder implements OnClickListener, OnLongClickListener {

	View mRoot;
	View mFront;
	ImageView imageView;
	TextView textView;
	TextView createTime;
	
	TextView mSearchTextView;
	ImageView clipImage;

	private RecyclerViewItemClickListener mListener;
	private RecyclerViewItemLongClickListener mLongClickListener;

	public NoteItemViewHolder(View itemView, RecyclerViewItemClickListener listener,
			RecyclerViewItemLongClickListener longClickListener) {
		super(itemView);

		mRoot = itemView;
		mFront = (View) itemView.findViewById(R.id.front);
		imageView = (ImageView) itemView.findViewById(R.id.image);
		textView = (TextView) itemView.findViewById(R.id.title);
		createTime = (TextView) itemView.findViewById(R.id.create_time);
		mSearchTextView = (TextView) itemView.findViewById(R.id.search_show);
		clipImage = (ImageView) itemView.findViewById(R.id.clip_img);

		mListener = listener;
		mLongClickListener = longClickListener;
		itemView.setOnClickListener(this);
		itemView.setOnLongClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mListener != null) {
			mListener.onItemClick(v, getPosition());
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if (mLongClickListener != null) {
			mLongClickListener.onItemLongClick(v, getPosition());
		}
		return true;
	}
}
