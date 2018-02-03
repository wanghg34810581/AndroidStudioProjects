package com.android.gnotes;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotesStaggeredGridFragment extends Fragment
		implements RecyclerViewItemClickListener, RecyclerViewItemLongClickListener {

	private static final String TAG = "NotesStaggeredGrid";

	private NotesAdapter mAdapter;
	private View mSearchView;
	private EditText mSearchEditView;
	private ImageView mSearchCover;

	private boolean mInSearchMode = false;

	// yuhf add for delete related images
	private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.notesgrid_main, container, false);
	}

	@SuppressWarnings("RedundantCast")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RecyclerView mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler);
		mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

		mAdapter = new NotesStaggeredGridAdapter(getActivity(), this);
		mRecyclerView.setAdapter(mAdapter);
		mAdapter.setOnItemClickListener(this);
		mAdapter.setOnItemLongClickListener(this);

		int spacesItemDecoration = getResources().getDimensionPixelSize(R.dimen.spaces_item_decoration);
		SpacesItemDecoration decoration = new SpacesItemDecoration(spacesItemDecoration);
		mRecyclerView.addItemDecoration(decoration);

		mSearchView = getView().findViewById(R.id.search_layout);
		Button mSearchBack = (Button) getView().findViewById(R.id.search_back);
		mSearchEditView = (EditText) getView().findViewById(R.id.search_text);
		mSearchCover = (ImageView) getView().findViewById(R.id.search_cover);

		mSearchBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mInSearchMode) {
					leaveSearchMode();
				}
			}
		});

		mSearchCover.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (mSearchCover.getTag() != null && (Integer) (mSearchCover.getTag()) == 0) {
					return true;
				} else {
					return false;
				}

			}
		});

		mSearchEditView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onTextChanged mInSearchMode:" + mInSearchMode + ", s:" + s);
				if (mInSearchMode) {
					querySearchData(s.toString());
					if (s.toString().trim().equals("")) {
						mSearchCover
								.setBackgroundColor(getActivity().getResources().getColor(R.color.search_add_cover));
						mSearchCover.setTag(0);
					} else {
						mSearchCover
								.setBackgroundColor(getActivity().getResources().getColor(R.color.search_dev_cover));
						mSearchCover.setTag(1);
					}
				} else {
					queryUpdateData();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});

		mSearchEditView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Drawable drawable = mSearchEditView.getCompoundDrawables()[2];
				if (drawable == null) {
					return false;
				}
				if (event.getAction() != MotionEvent.ACTION_UP) {
					return false;
				}
				String text = mSearchEditView.getText().toString();
				Log.d(TAG, "mSearchEditView.getText().toString():" + text);
				if (!text.trim().equals("") && (event.getX() > mSearchEditView.getWidth()
						- mSearchEditView.getPaddingRight() - drawable.getIntrinsicWidth())) {
					mSearchEditView.setText(null);
				}
				return false;
			}
		});
	}

	private void setEditViewFocusable(boolean focusable) {
		mSearchEditView.setFocusable(focusable);
		mSearchEditView.setFocusableInTouchMode(focusable);
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (focusable) {
			mSearchEditView.requestFocus();
			imm.showSoftInput(mSearchEditView, InputMethodManager.SHOW_IMPLICIT);
		} else {
			mSearchEditView.clearFocus();
			imm.hideSoftInputFromWindow(mSearchEditView.getWindowToken(), 0);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Editable s = mSearchEditView.getText();
		Log.d(TAG, "onResume() s:" + s.toString());
		if (mInSearchMode) {
			querySearchData(s.toString());
		} else {
			queryUpdateData();
		}
	}

	public void queryUpdateData() {
		ProgressDialog progress = ProgressDialog.show(getActivity(), "", getString(R.string.title_loading), true);
		NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
		qh.startQuery(Notes.MAIN_VIEW, progress, Notes.CONTENT_URI, Notes.PROJECTION, null, null, Notes.sSortOrder);
	}

	private void querySearchData(String query) {
		NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
		qh.startQuery(Notes.MAIN_VIEW, null, Notes.CONTENT_URI, Notes.PROJECTION,
				Notes.COLUMN_NAME_SEARCH_NOTE + " like ?", new String[] { "%" + query + "%" }, Notes.sSortOrder);
	}

	public boolean isInSearchMode() {
		return mInSearchMode;
	}

	public void enterSearchMode() {
		if (mInSearchMode) {
			return;
		}
		mInSearchMode = true;
		// noinspection ConstantConditions
		getActivity().getActionBar().hide();
		mSearchView.setVisibility(View.VISIBLE);
		setEditViewFocusable(true);
		mSearchCover.setBackgroundColor(getActivity().getResources().getColor(R.color.search_add_cover));
		mSearchCover.setTag(0);

		((MainNoteActivity) getActivity()).setCreateNoteButtonVisible(false);
	}

	public void leaveSearchMode() {
		if (!mInSearchMode) {
			return;
		}
		mInSearchMode = false;
		getActivity().getActionBar().show();
		mSearchView.setVisibility(View.GONE);
		setEditViewFocusable(false);
		mSearchCover.setBackgroundColor(getActivity().getResources().getColor(R.color.search_dev_cover));
		mSearchCover.setTag(1);

		mSearchEditView.setText(null);
		((MainNoteActivity) getActivity()).setCreateNoteButtonVisible(true);
	}

	@SuppressWarnings("RedundantCast")
	@Override
	public void onItemClick(View view, int position) {
		// TODO Auto-generated method stub
		NoteItem noteItem = mAdapter.getItem(position);
		Uri uri = ContentUris.withAppendedId(getActivity().getIntent().getData(), noteItem.id);
		String action = getActivity().getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
			getActivity().setResult(getActivity().RESULT_OK, new Intent().setData(uri));
		} else {
			Intent it = new Intent(getActivity(), NoteView.class);
			it.putExtra(Notes.VIEW_NAME_INDEX, Notes.EDIT_VIEW);
			it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			it.setData(uri);
			getActivity().startActivity(it);
		}
	}

	@Override
	public void onItemLongClick(View view, int position) {
		// TODO Auto-generated method stub
		doDelete(position);
	}

	@SuppressWarnings("RedundantCast")
	private void doDelete(final int position) {
		// yuhf add for delete related images begin
		NoteItem noteItem = mAdapter.getItem(position);
		imgList.clear();
		loadImage(noteItem.note, imgList);
		// yuhf add for delete related images end

		// TY zhencc 20160902 delete for PROD104179658 begin
		/*AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ProgressDialog mDeleting = ProgressDialog.show(getActivity(), "", getString(R.string.delete_progress),
						true);
				String selection = mAdapter.getDeleteString(position);

				NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
				qh.startDelete(Notes.DELETE_VIEW, mDeleting, Notes.CONTENT_URI, selection, null);

				// yuhf add for delete related images begin
				String path = null;
				for (int i = 0; i < imgList.size(); i++) {
					path = imgList.get(i).get("path");
					Log.d(TAG, "doDelete path is " + path + " location is " + imgList.get(i).get("location"));
					File file = new File(path);
					if (file.isFile() && file.exists()) {
						file.delete();
					}
				}
				// yuhf add for delete related images end
			}
		});
		bld.setNegativeButton(getString(R.string.delete_confirm_cancel), null);
		bld.setCancelable(true);
		bld.setMessage(getString(R.string.delete_confirm));
		bld.setTitle(getString(R.string.delete_confirm_title));
		AlertDialog dlg = bld.create();
		dlg.show();*/
		// TY zhencc 20160902 delete for PROD104179658 end

		// TY zhencc 20160902 add for PROD104179658 begin
		CustomAlertDialog customAlertDialog = new CustomAlertDialog(getActivity());
		customAlertDialog.setTitle(R.string.delete_confirm_title);
		customAlertDialog.setMessage(R.string.delete_confirm);
		customAlertDialog.setSureBtn(R.string.delete_confirm_ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ProgressDialog mDeleting = ProgressDialog.show(getActivity(), "", getString(R.string.delete_progress),
						true);
				String selection = mAdapter.getDeleteString(position);

				NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
				qh.startDelete(Notes.DELETE_VIEW, mDeleting, Notes.CONTENT_URI, selection, null);

				// yuhf add for delete related images begin
				String path = null;
				for (int i = 0; i < imgList.size(); i++) {
					path = imgList.get(i).get("path");
					Log.d(TAG, "doDelete path is " + path + " location is " + imgList.get(i).get("location"));
					File file = new File(path);
					if (file.isFile() && file.exists()) {
						file.delete();
					}
				}
				// yuhf add for delete related images end
			}
		});

		customAlertDialog.setCancelBtn(R.string.delete_confirm_cancel, null);
		customAlertDialog.showDialog();
		// TY zhencc 20160902 add for PROD104179658 end
	}

	// yuhf add for delete related images begin
	private void loadImage(String note, List imagelist) {
		String rootPath = getActivity().getExternalFilesDir(null).getPath();
		Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");
		Matcher m = p.matcher(note);

		while (m.find()) {
			String fullpath = m.group().toString();
			String path = fullpath.substring(0, fullpath.length() - 5);
			if (IsfileExists(path)) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("location", m.start() + "-" + m.end());
				map.put("path", path);
				imagelist.add(map);
			}
		}
	}

	private boolean IsfileExists(String path) {
		Log.d(TAG, "IsfileExists path is " + path);
		try {
			File f = new File(path);
			if (!f.exists()) {
				Log.d(TAG, "IsfileExists no1");
				return false;
			}
		} catch (Exception e) {
			Log.d(TAG, "IsfileExists no2");
			// TODO: handle exception
			return false;
		}
		Log.d(TAG, "IsfileExists yes");
		return true;
	}
	// yuhf add for delete related images end

	public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

		private int space;

		public SpacesItemDecoration(int space) {
			this.space = space;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			outRect.left = space;
			outRect.right = space;
			outRect.bottom = space;
			outRect.top = space;
		}
	}
}
