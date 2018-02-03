package com.android.gnotes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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

import com.android.swipeListView.BaseSwipeListViewListener;
import com.android.swipeListView.SwipeListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("FieldCanBeLocal")
public class NotesLinearFragment extends Fragment
        /*implements RecyclerViewItemClickListener, RecyclerViewItemLongClickListener*/ {

    private static final String TAG = "NotesLinearFragment";

    private SwipeListView mRecyclerView;
    private NotesLinearAdapter mAdapter;
    private View mSearchView;
    private EditText mSearchEditView;
    private ImageView mSearchCover;

    private boolean mInSearchMode = false;
    private boolean mDeleteBackFromNoteView = false;
    private int mOpenPosition = -1;

    // yuhf add for delete related images
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.linear_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mRecyclerView = (SwipeListView) getView().findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new NotesLinearAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);
        /*mAdapter.setOnItemClickListener(this);
		mAdapter.setOnItemLongClickListener(this);*/

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        mRecyclerView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public boolean onActionDown() {
                if(mOpenPosition != -1) {
                    mRecyclerView.closeAnimate(mOpenPosition);
                    return true;
                }
                return false;
            }

            @Override
            public void onOpened(int position, boolean toRight) {
                Log.d(TAG, "onOpened position:" + position);
                mOpenPosition = position;
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
                Log.d(TAG, "onClosed position:" + position);
                NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position);
                if (viewHolder != null && viewHolder.clipImage != null) {
                    viewHolder.clipImage.setImageResource(R.drawable.note_item_clip_normal);
                }
                mOpenPosition = -1;
            }

            @Override
            public void onListChanged() {
                Log.d(TAG, "onListChanged");
            }

            @Override
            public void onMove(int position, float x) {
                Log.d(TAG, "onMove position:" + position);
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d(TAG, String.format("onStartOpen %d - action %d", position, action));
                NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position);
                if (viewHolder != null && viewHolder.clipImage != null) {
                    viewHolder.clipImage.setImageResource(R.drawable.note_item_clip_up);
                }
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d(TAG, String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d(TAG, String.format("onClickFrontView %d", position));
                openNote(position);
            }

            @Override
            public void onClickBackView(int position) {
                Log.d(TAG, String.format("onClickBackView %d", position));
                doDeleteDirect(position);
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                Log.d(TAG, String.format("onClickBackView %d", reverseSortedPositions[0]));
            }

        });

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mRecyclerView.closeAnimate(position);
                    }
                })
        );

        mSearchView = getView().findViewById(R.id.search_layout);
        Button mSearchBack = (Button) getView().findViewById(R.id.search_back);
        mSearchEditView = (EditText) getView().findViewById(R.id.search_text);
        mSearchCover = (ImageView) getView().findViewById(R.id.search_cover);

        mSearchBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                leaveSearchMode();
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
                closeOpenedItems();
                if (mInSearchMode) {
                    querySearchData(s.toString());
                    if (s.toString().equals("")) {
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
                closeOpenedItems();
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

    private class DeleteAnimationListener extends AnimatorListenerAdapter {
        private int mDeletePosition;

        DeleteAnimationListener(int pos) {
            mDeletePosition = pos;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
            qh.startQuery(Notes.MAIN_VIEW, mDeletePosition, Notes.CONTENT_URI, Notes.PROJECTION, null, null, Notes.sSortOrder);
        }
    };

    private Handler mHandler = new Handler();
    final private class DeleteAnimator implements Runnable {
        private int mPosition;
        private int mDeletePosition;

        DeleteAnimator(int pos) {
            mDeletePosition = pos;
            mPosition = pos;
        }

        public void run() {
            mPosition++;
            NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(mPosition);

            if(viewHolder != null) {
                //Log.i("wanghg", "mPosition : " + mPosition + "   viewHolder.mRoot : " + viewHolder.mRoot);

                float curTranslationY = viewHolder.mRoot.getTop();
                float preTranslationY = ((NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(mPosition - 1)).mRoot.getTop();
                //Log.i("wanghg", "curTranslationY : " + curTranslationY + "   preTranslationY : " + preTranslationY);
                ObjectAnimator animator = ObjectAnimator.ofFloat(viewHolder.mRoot, "translationY", 0, 0, preTranslationY - curTranslationY);
                animator.setDuration(200);
                if((NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(mPosition + 1) == null) {
                    animator.addListener(new DeleteAnimationListener(mDeletePosition) {
                    });
                }
                animator.start();

                mHandler.postDelayed(this, 30);
            }
            else if((NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(mDeletePosition + 1) == null) {
                NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
                qh.startQuery(Notes.MAIN_VIEW, mDeletePosition, Notes.CONTENT_URI, Notes.PROJECTION, null, null, Notes.sSortOrder);
            }
        }
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
        if(mDeleteBackFromNoteView == false) {
            queryData();
        }
        else {
            mDeleteBackFromNoteView = false;
        }
    }

    public void queryData() {
        Editable s = mSearchEditView.getText();
        Log.d(TAG, "queryData() s:" + s.toString() + ",mInSearchMode:" + mInSearchMode);
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
                Notes.COLUMN_NAME_SEARCH_NOTE + " like ?", new String[]{"%" + query + "%"}, Notes.sSortOrder);
    }

    public boolean isInSearchMode() {
        return mInSearchMode;
    }

    public void enterSearchMode() {
        if (mInSearchMode) {
            return;
        }
        mInSearchMode = true;
        getActivity().getActionBar().hide();
        mSearchView.setVisibility(View.VISIBLE);
        setEditViewFocusable(true);
        mSearchCover.setBackgroundColor(getActivity().getResources().getColor(R.color.search_add_cover));
        mSearchCover.setTag(0);

        ((MainNoteActivity) getActivity()).setCreateNoteButtonVisible(false);

        closeOpenedItems();
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

        closeOpenedItems();
    }

	/*@Override
	public void onItemClick(View view, int position) {
		// TODO Auto-generated method stub
		if (mAdapter.getItemViewType(position) == NotesUtils.HEAD_VIEW) {
			return;
		}
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
	}*/

	/*@Override
	public void onItemLongClick(View view, int position) {
		// TODO Auto-generated method stub
		if (mAdapter.getItemViewType(position) == NotesUtils.HEAD_VIEW) {
			return;
		}
		doDelete(position);
	}*/

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

    public void doDeleteDirectQueryEnd(final int position) {
        int pos = position + 1;
        NoteItemViewHolder delete_viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position);
        delete_viewHolder.mRoot.setVisibility(View.VISIBLE);

        NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(pos);
        while (viewHolder != null)
        {
            //Log.i("wanghg", "pos : " + pos + "   viewHolder.mRoot : " + viewHolder.mRoot);
            //float curTranslationY = viewHolder.mRoot.getTranslationY();
            //float preTranslationY = ((NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(pos - 1)).mRoot.getTranslationY();
            //Log.i("wanghg", "curTranslationY : " + curTranslationY + "   preTranslationY : " + preTranslationY);
            viewHolder.mRoot.setTranslationY(0);
            pos++;
            viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(pos);
        };
    }

    public void doDeleteDirectEnd(final int position) {
        if((NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position) != null) {
            NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position);
            viewHolder.mRoot.setVisibility(View.INVISIBLE);

            mHandler.postDelayed(new DeleteAnimator(position), 300);
        }
        else {
            NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
            qh.startQuery(Notes.MAIN_VIEW, position, Notes.CONTENT_URI, Notes.PROJECTION, null, null, Notes.sSortOrder);
        }
    }

    public void setNoteVisibility(final int position, boolean visible) {
        mDeleteBackFromNoteView = true;
        NoteItemViewHolder viewHolder = (NoteItemViewHolder) mRecyclerView.findViewHolderForPosition(position);
        if(visible == true) {
            viewHolder.mRoot.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.mRoot.setVisibility(View.INVISIBLE);
        }
    }

    public void doDeleteDirect(final int position) {
        // yuhf add for delete related images begin
        NoteItem noteItem = mAdapter.getItem(position);
        imgList.clear();
        loadImage(noteItem.note, imgList);
        // yuhf add for delete related images end

        String selection = mAdapter.getDeleteString(position);

        NoteQueryHandler qh = new NoteQueryHandler(getActivity().getContentResolver(), getActivity(), mAdapter);
        qh.startDelete(Notes.DELETE_VIEW, position, Notes.CONTENT_URI, selection, null);

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

    private void openNote(int position) {
        if (mAdapter.getItemViewType(position) == NotesUtils.HEAD_VIEW) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mSearchEditView.clearFocus();
        imm.hideSoftInputFromWindow(mSearchEditView.getWindowToken(), 0);

        NoteItem noteItem = mAdapter.getItem(position);
        Uri uri = ContentUris.withAppendedId(getActivity().getIntent().getData(), noteItem.id);
        String action = getActivity().getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            getActivity().setResult(getActivity().RESULT_OK, new Intent().setData(uri));
        } else {
            Intent it = new Intent(getActivity(), NoteView.class);
            it.putExtra(Notes.VIEW_NAME_INDEX, Notes.EDIT_VIEW);
            it.putExtra("curPos", position);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.setData(uri);
            getActivity().startActivityForResult(it, 100);
            getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
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

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private int mItemSize = 1;
        private Paint mPaint;

        public DividerItemDecoration(Context context) {

            mItemSize = (int) TypedValue.applyDimension(mItemSize, TypedValue.COMPLEX_UNIT_DIP,
                    context.getResources().getDisplayMetrics());
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.LTGRAY);
            mPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, mItemSize);
        }
    }

    //TY zhencc add for activity back begin
    public void closeOpenedItems() {
        mRecyclerView.closeOpenedItems();
    }

    public boolean haveOpenItem(){
        return mRecyclerView.haveOpenItem();
    }
    //TY zhencc add for activity back end
}
