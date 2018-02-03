package com.android.gnotes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class NotesLinearAdapter extends NotesAdapter {

    private static final String TAG = "LinearAdapter";

    private Context mContext;
    private NotesLinearFragment mFragment;
    private List<NoteItem> mList = new ArrayList<NoteItem>();

    private RecyclerViewItemClickListener mItemClickListener;
    private RecyclerViewItemLongClickListener mItemLongClickListener;

    public NotesLinearAdapter(Context context, NotesLinearFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public NoteItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == NotesUtils.HEAD_VIEW) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.linear_item_head_context, viewGroup,
                    false);
        } else if (viewType == NotesUtils.BODY_VIEW) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.linear_item_context, viewGroup, false);
        }
        return new NoteItemViewHolder(view, mItemClickListener, mItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(NoteItemViewHolder notesView, int position) {
        NoteItem item = mList.get(position);

        if (getItemViewType(position) == NotesUtils.HEAD_VIEW) {
            notesView.mSearchTextView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mFragment.enterSearchMode();
                }
            });
        } else if (getItemViewType(position) == NotesUtils.BODY_VIEW) {
            if (item.search_note != null && !item.search_note.trim().equals("")) {
                Pattern p = Pattern.compile("\n");
                Matcher m = p.matcher(item.search_note);
                item.search_note = m.replaceAll(" ");

                notesView.textView.setText(item.search_note);
            } else if (item.note != null && !item.note.trim().equals("")) {
                notesView.textView.setText(R.string.default_title);
            } else {
                notesView.textView.setText(item.title);
            }
            notesView.createTime.setText(time(item.modify_time));

            if (item.bitmap != null) {
                notesView.imageView.setImageBitmap(item.bitmap);
                notesView.imageView.setVisibility(View.VISIBLE);
                notesView.mFront.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.recyclerview_selector_pic));
            } else {
                notesView.imageView.setVisibility(View.GONE);
                notesView.mFront.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.recyclerview_selector));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public NoteItem getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        if (position == 0) {
            return NotesUtils.HEAD_VIEW;
        } else {
            return NotesUtils.BODY_VIEW;
        }
    }

    public String getDeleteString(int position) {
        String mDeleteFilter = "_id in (" + String.valueOf(mList.get(position).id) + ")";
        return mDeleteFilter;
    }

    public void setOnItemClickListener(RecyclerViewItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(RecyclerViewItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    private String time(long time) {
        return NotesUtils.formatTimeString(mContext, time);
    }

    @Override
    public void setData(Cursor cursor) {
        // TODO Auto-generated method stub
        mList.clear();

        try {
            if (cursor != null && cursor.moveToFirst()) {
                NoteItem headItem = new NoteItem();
                mList.add(headItem);

                NoteItem item;
                do {
                    item = new NoteItem();
                    item.id = cursor.getInt(cursor.getColumnIndex(Notes._ID));
                    item.note = cursor.getString(cursor.getColumnIndex(Notes.COLUMN_NAME_NOTE));
                    item.search_note = cursor.getString(cursor.getColumnIndex(Notes.COLUMN_NAME_SEARCH_NOTE));
                    item.title = cursor.getString(cursor.getColumnIndex(Notes.COLUMN_NAME_TITLE));
                    item.create_time = cursor.getLong(cursor.getColumnIndex(Notes.COLUMN_NAME_CREATE_DATE));
                    item.modify_time = cursor.getLong(cursor.getColumnIndex(Notes.COLUMN_NAME_MODIFICATION_DATE));
                    item.bitmap = getImage(item.note);
                    mList.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private Bitmap getImage(String note) {
        String rootPath = mContext.getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg");
        Matcher m = p.matcher(note);

        Bitmap bm = null;
        int maxH = (int) mContext.getResources().getDimension(R.dimen.image_height_linear);
        int maxW = (int) mContext.getResources().getDimension(R.dimen.image_width_linear);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        while (m.find()) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(m.group(), opts);
            opts.inSampleSize = NotesUtils.computeSampleSize(mContext, opts, -1, maxH * maxW * 4);
            Log.d(TAG, "opts.inSampleSize:" + opts.inSampleSize);
            opts.inJustDecodeBounds = false;
            try {
                bm = BitmapFactory.decodeFile(m.group(), opts);
                break;
            } catch (OutOfMemoryError err) {
                Log.d(TAG, err.getMessage());
            }
        }

        bm = resizetoscreen(bm);
        return bm;
    }

    private Bitmap resizetoscreen(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenwidth = dm.widthPixels;
        int padding = mContext.getResources().getDimensionPixelSize(R.dimen.g_note_padding_left);
        int contentwidth = screenwidth - 2 * padding;

        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        Log.i(TAG, "resizetoscreen contentwidth=" + contentwidth + " imgWidth=" + imgWidth);

        if (contentwidth >= imgWidth) {
            return bitmap;
        } else {
            float scale = (float) (contentwidth * 1.0 / imgWidth);
            Log.i(TAG, "resizetoscreen scale=" + scale);
            Matrix mx = new Matrix();
            mx.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
            return bitmap;
        }
    }
}
