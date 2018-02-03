package com.android.gnotes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toolbar;

public class MainNoteActivity extends Activity {

	private static final String TAG = "MainNoteActivity";

	private List<NotesFragmentInfo> mAllFragments = new ArrayList<NotesFragmentInfo>();
	private NotesFragmentInfo mNotesLinearInfo = new NotesFragmentInfo(NotesUtils.ID_NOTE_LINEAR);
	private NotesFragmentInfo mMotesStaggeredGridInfo = new NotesFragmentInfo(NotesUtils.ID_NOTE_STAGGERED_GRID);

	private View mEmptyView;
	private ImageButton mCreateNoteButton;

	private int mCurrentFragment;
	private NotesLinearFragment mNotesLinear;
	private NotesStaggeredGridFragment mNotesStaggeredGrid;

	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar_color));
		}

		setContentView(R.layout.main_note);

		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Notes.CONTENT_URI);
		}

		mSharedPreferences = getPreferences(Activity.MODE_PRIVATE);
		mCurrentFragment = mSharedPreferences.getInt(NotesUtils.NOTE_ID, NotesUtils.ID_NOTE_LINEAR);

		initViews();
		initAllFragments();
		initLayout();
	}

	private void initViews() {
		mEmptyView = findViewById(R.id.empty_layout_id);
		mCreateNoteButton = (ImageButton) findViewById(R.id.floating_action_button);

		getActionBar().setDisplayShowCustomEnabled(true);
		View actionbarLayout = LayoutInflater.from(this).inflate(
				R.layout.main_note_actionbar, new RelativeLayout(this), true);
		getActionBar().setCustomView(actionbarLayout);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Toolbar parent = (Toolbar) actionbarLayout.getParent();
			parent.setContentInsetsAbsolute(0, 0);
		}
	}

	private void initAllFragments() {
		mAllFragments.clear();
		mAllFragments.add(mNotesLinearInfo);
		mAllFragments.add(mMotesStaggeredGridInfo);
	}

	private void initLayout() {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		int stepId = mAllFragments.get(mCurrentFragment - 1).getNameId();
		Log.d(TAG, "initLayout mCurrentFragment:" + mCurrentFragment + ", stepId:" + stepId);
		switch (stepId) {
		case NotesUtils.ID_NOTE_LINEAR:
			mNotesLinear = new NotesLinearFragment();
			transaction.replace(R.id.fragment_container, mNotesLinear);
			break;
		case NotesUtils.ID_NOTE_STAGGERED_GRID:
			mNotesStaggeredGrid = new NotesStaggeredGridFragment();
			transaction.replace(R.id.fragment_container, mNotesStaggeredGrid);
			break;
		default:
			break;
		}
		transaction.commit();
	}

	public void updateEmptyView(int count) {
		try {
			Log.i(TAG, "updateEmptyView count=" + count);
			if (count <= 0) {
				mEmptyView.setVisibility(View.VISIBLE);
			} else {
				mEmptyView.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setCreateNoteButtonVisible(boolean visible) {
		/*if (visible) {
			mCreateNoteButton.setVisibility(View.VISIBLE);
		} else {
			mCreateNoteButton.setVisibility(View.INVISIBLE);
		}*/
	}

	public void updateDeleteData(int pos) {
		if (mCurrentFragment == NotesUtils.ID_NOTE_LINEAR) {
			mNotesLinear.doDeleteDirectEnd(pos);
			//mNotesLinear.queryData();
		} else {
			mNotesStaggeredGrid.queryUpdateData();
		}
	}

	public void updateQueryData(int pos) {
		if (mCurrentFragment == NotesUtils.ID_NOTE_LINEAR) {
			mNotesLinear.doDeleteDirectQueryEnd(pos);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 100 && data != null) {
			final int pos = data.getIntExtra("curPos", 0);
			mNotesLinear.setNoteVisibility(pos, false);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mNotesLinear.doDeleteDirect(pos);
				}
			}, 200);
		}
	}

	/*public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_options_menu, menu);
		MenuItem item = menu.findItem(R.id.menu_change_note);
		changeMenuIcon(item);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_change_note:
			changeFragment();
			changeMenuIcon(item);
			saveNoteId();
			return true;
		//TY zhencc add for new version begin
		case R.id.menu_create_note:
			createNote();
			return true;
		//TY zhencc add for new version end
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/

	private void changeFragment() {
		mCurrentFragment = mCurrentFragment == NotesUtils.ID_NOTE_LINEAR ? NotesUtils.ID_NOTE_STAGGERED_GRID
				: NotesUtils.ID_NOTE_LINEAR;
		initLayout();
	}

	private void changeMenuIcon(MenuItem item) {
		if (mCurrentFragment == NotesUtils.ID_NOTE_LINEAR) {
			item.setIcon(R.drawable.menu_grid);
		} else {
			item.setIcon(R.drawable.menu_list);
		}
	}

	private void saveNoteId() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putInt(NotesUtils.NOTE_ID, mCurrentFragment);
		editor.commit();
	}

	public void CreateNote(View v) {
		if (mCurrentFragment == NotesUtils.ID_NOTE_LINEAR) {
			if (mNotesLinear.haveOpenItem()) { //TY zhencc add for activity back
				mNotesLinear.closeOpenedItems();
			}
		}

		Intent intent = new Intent(getBaseContext(), NoteView.class);
		intent.putExtra(Notes.VIEW_NAME_INDEX, Notes.ADD_VIEW);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	//TY zhencc add for new version begin
	private void createNote() {
		Intent intent = new Intent(getBaseContext(), NoteView.class);
		intent.putExtra(Notes.VIEW_NAME_INDEX, Notes.ADD_VIEW);
		startActivity(intent);
	}
	//TY zhencc add for new version end

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mCurrentFragment == NotesUtils.ID_NOTE_LINEAR) {
			if (mNotesLinear.isInSearchMode()) {
				mNotesLinear.leaveSearchMode();
			} else if (mNotesLinear.haveOpenItem()) { //TY zhencc add for activity back
				mNotesLinear.closeOpenedItems();
			} else {
				super.onBackPressed();
			}
		} else {
			if (mNotesStaggeredGrid.isInSearchMode()) {
				mNotesStaggeredGrid.leaveSearchMode();
			} else {
				super.onBackPressed();
			}
		}
	}

	private class NotesFragmentInfo {

		private int mNameId;

		public NotesFragmentInfo(int nameId) {
			mNameId = nameId;
		}

		public int getNameId() {
			return mNameId;
		}
	}

}
