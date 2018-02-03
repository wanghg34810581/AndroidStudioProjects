/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.android.gnotes;

import android.app.ActionBar;
import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
// TY zhencc 20160825 add for PROD104174124 begin
import android.content.SharedPreferences;
// TY zhencc 20160825 add for PROD104174124 end
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ImageSpan;
import android.text.TextWatcher;
import android.text.InputFilter;
import android.text.Layout;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.text.TextPaint;
import android.text.TextUtils;
import android.content.DialogInterface;
import android.app.ProgressDialog;
/*TIANYURD:songbangbang 20150401 add for TOS3.0 begin*/
import android.widget.ImageButton;
/*TIANYURD:songbangbang 20150401 add for TOS3.0 end*/

public class NoteView extends Activity implements View.OnLayoutChangeListener {
    private static final String TAG = "NoteView";

    private static final int TYPE_MAX_LEN_CONTENT = 0;
    private static final int TYPE_MAX_LEN_TITLE = 1;

    private static int mMaxLength = 5001;//1501;
    private static int mMaxImgCount = 20;
    private static int mImgLength = 93;
    private static int mIconLength = 7;

    private Uri mUri;
    private Uri mPicUri;
    private int mViewndex;
    private int mCurPos;
    private int doSaveActionFlag = 0; // TY hanjq 20150114 add for PROD103537493
    private int screenHeight = 0;
    private MaxLengthFilter mTextLengthFilter;
    private boolean mFirstEnter = false;

    // private NoteEditText mText;
    private LineEditText mText;
    private int skinIndex = 0;

    // ImageButton
    private ImageButton deleteNoteBtn;
    private ImageButton saveNoteBtn;
    private ImageButton shareNoteBtn;
    private View returnListBtn;	
    private View addListBtn;
    private View capPhotoBtn;
    private View attPictureBtn;
    private View drawPadBtn;
    private View changeSkinBtn;
    private View editMenu;
    private View previewMenu;
    private View topView;
    private View bottomView;
    private View rootview; /* wanghg modify for delete note animation  20161227 */

    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();

    String s_normal = "#0chb0#"; // indicate the unselected checkbox
    String s_selected = "#1chb1#"; // indicate the selected checkbox
    String s_imgsuff = "0IMG0"; // image suffix;
    private String sel_Img;
    private int dkeycode;
    private boolean bAddListBtn;
    private int ImgCount;
    private boolean bClickOnEmpty;
	private boolean  bLastNewCursor = false;

    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int REQUEST_PICK_PAINT = 3;
    private static final int REQUEST_CHANGE_SKIN = 4;
    private static final int REQUEST_DISPLAY_IMAGE = 5;

    private static final int HeadId[] = {R.drawable.skin_default_head, R.drawable.skin_01_head, R.drawable.skin_02_head,
            R.drawable.skin_03_head, R.drawable.skin_04_head, R.drawable.skin_05_headt};

    private static final int TailId[] = {R.drawable.skin_default_head, R.drawable.skin_01_tail, R.drawable.skin_02_tail,
            R.drawable.skin_03_tail, R.drawable.skin_04_tail, R.drawable.skin_05_tailt};

    private static final int BackgroundId[] = {R.drawable.repeat_bg, R.drawable.skin_01_list, R.drawable.skin_02_list,
            R.drawable.skin_03_list,
            // R.color.skin4_background_color,
            R.drawable.repeat_bg04, R.drawable.repeat_bg05};
    // duhuan 20160719 add for share begin
    private static final int BackgroundRepeatId[] = {R.drawable.skin_default, R.drawable.skin_01_list,
            R.drawable.skin_02_list, R.drawable.skin_03_list, R.drawable.skin_04_head, R.drawable.skin_05_headt};
    // duhuan 20160719 add for share end

    private static final int TopHeight[] = {R.dimen.g_note_top0_height, R.dimen.g_note_top1_height,
            R.dimen.g_note_top2_height, R.dimen.g_note_top3_height, R.dimen.g_note_top4_height,
            R.dimen.g_note_top5_height};

    private static final int BottomHeight[] = {R.dimen.g_note_bottom0_height, R.dimen.g_note_bottom1_height,
            R.dimen.g_note_bottom2_height, R.dimen.g_note_bottom3_height, R.dimen.g_note_bottom4_height,
            R.dimen.g_note_bottom5_height};

    private static final int TextColor[] = {R.color.color_txt0, R.color.color_txt1, R.color.color_txt2,
            R.color.color_txt3, R.color.color_txt4, R.color.color_txt5};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar_color));
        }

        setContentView(R.layout.noteslist_item_editor);

        // @20151216 start lxm modify for changing the statusbar color in
        // hunan's guide
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // end lxm modify
        // getWindow().setStatusBarColor(getResources().getColor(R.color.actionbar_background_color));
        // Log.i(TAG, "onCreate");

        Intent intent = getIntent();
        if (getIntent().hasExtra(Notes.VIEW_NAME_INDEX)) {
            mViewndex = intent.getIntExtra(Notes.VIEW_NAME_INDEX, Notes.ADD_VIEW);
        }

        if (getIntent().hasExtra("curPos")) {
            mCurPos = intent.getIntExtra("curPos", 0);
        }

        mUri = intent.getData();

        mTextLengthFilter = new MaxLengthFilter(this, mMaxLength, TYPE_MAX_LEN_CONTENT);
        initActionbar();		
        initViews();
        mText.setSelection(0);

        mFirstEnter = true;
    }

    private void initViews() {
        mText = (LineEditText) findViewById(R.id.note);

	CharSequence hint = mText.getHint();
	SpannableString ss =  new SpannableString(hint);
	int hintSize = NotesUtils.px2dip(this, getResources().getDimensionPixelSize(R.dimen.g_note_text_hint_size));	
	AbsoluteSizeSpan ass = new AbsoluteSizeSpan(hintSize, true);
	mText.setHintTextColor(Color.parseColor("#bbaca0"));
	ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	mText.setHint(new SpannedString(ss));

        mText.setFilters(new InputFilter[]{mTextLengthFilter});

        mText.setOnClickListener(new TextClickEvent());

        addListBtn = findViewById(R.id.add_button);
        attPictureBtn = findViewById(R.id.att_picture);
        capPhotoBtn = findViewById(R.id.cap_photo);
        drawPadBtn = findViewById(R.id.draw_pad);
        changeSkinBtn = findViewById(R.id.chg_skin);

        editMenu = findViewById(R.id.edit_bottom_menu);
        //previewMenu = findViewById(R.id.preview_bottom_menu);

        bottomView = findViewById(R.id.note_bottom);

        if (mUri == null) {
            mText.setText("");
        } else {
            Cursor mCursor = managedQuery(mUri, Notes.PROJECTION, null, null, null);
            mCursor.moveToFirst();

            // int noteId = mCursor.getInt(mCursor.getColumnIndex(Notes._ID));
            String note = mCursor.getString(mCursor.getColumnIndex(Notes.COLUMN_NAME_NOTE));

            // mText.setText(note);
            // mText.setSelection(note.length());
            loadData(note);

            // yuhf add for change skin function begin
            skinIndex = mCursor.getInt(mCursor.getColumnIndex(Notes.COLUMN_NAME_SKIN_INDEX));
            //Log.i(TAG, "initViews skinIndex=" + skinIndex);
            setSelectedStyle(skinIndex);
            // yuhf add for change skin function end
        }

        addListBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AddListbutton();
            }
        });
        capPhotoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                intentCamera();
            }
        });
        attPictureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getimage();
            }
        });
        drawPadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawpad();
            }
        });
        changeSkinBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSkin();
            }
        });

        mText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //Log.i(TAG, "mText ACTION_DOWN");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "mText ACTION_UP");
                    Unlock();
                    //mText.setCursorVisible(true);

                    int start = mText.getSelectionStart();
                    int end = mText.getSelectionEnd();
                    //Log.i(TAG, "getSelectionStart()=" + start + " getSelectionEnd()=" + end);
                    if (start != end) {
                        setImageSpanRange(start, end);
                    }

                    int y = (int) event.getY();
                    int bottom = mText.getLineEditTextBottom();
                    //Log.i(TAG, "y=" + y + " bottom=" + bottom);
                    if (y > bottom) {
                        bClickOnEmpty = true;
                    } else {
                        bClickOnEmpty = false;
                    }
                }
                return false;
            }
        });

        mText.addTextChangedListener(editWatcher);

        mText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i(TAG, "onKey keycode=" + keyCode + " KeyEvent=" + event);
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    dkeycode = keyCode;
                } else {
                    dkeycode = 0;
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        int selectionStart = mText.getSelectionStart();
                        int startOffset = getCurrentCursorLineStart();
                        int end_offset = getCurrentCursorLineEnd();
                        //Log.i(TAG, "KEYCODE_ENTER ACTION_DOWN curcor position=" + selectionStart + " startOffset=" + startOffset);

                        Spanned s = mText.getText();
                        String text = mText.getText().toString();
                        ImageSpan[] imageSpans;
                        imageSpans = s.getSpans(startOffset, selectionStart, ImageSpan.class);
                        for (ImageSpan span : imageSpans) {
                            int start = s.getSpanStart(span);
                            int end = s.getSpanEnd(span);
                            //Log.i(TAG, "start=" + start + " end=" + end);
                            if (start == end) {
                                continue;
                            }
                            int index = 0;
                            String id = text.substring(start, end);
                            //Log.i(TAG, "id is " + id);
                            if (id != null && (id.equals(s_normal) || id.equals(s_selected))) {
                                bAddListBtn = true;
                                return false;
                            }
                        }

                    } else {
                        int selectionStart = mText.getSelectionStart();
                        int startOffset = getCurrentCursorLineStart();
                        //Log.i(TAG, "KEYCODE_ENTER ACTION_UP curcor position=" + selectionStart + " startOffset=" + startOffset);
                        if (bAddListBtn == true) {
                            insertIcon(startOffset);
                        }
                        bAddListBtn = false;
                    }

                }
                return false;
            }
        });

        bottomView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mText, InputMethodManager.SHOW_FORCED);
                //Log.i(TAG, "bottomView onClick");
            }
        });

        // yanghong add for PROD104153695
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        rootview = findViewById(R.id.layout_root); /* wanghg modify for delete note animation  20161227 */
        rootview.addOnLayoutChangeListener(this);
    }

    private void loadData(String note_content) {
        // Pattern p=Pattern.compile("/([^\\.]*)\\.\\w{3}");
        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath(); //Environment.getExternalStorageDirectory().getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");

        // Pattern p=Pattern.compile("/(.*)\\.(jpg|png|bmp)");
        Matcher m = p.matcher(note_content);
        //TY zhencc 20161031 delete for missing image error begin
        //Pattern p1 = Pattern.compile(s_normal);
        //Matcher m1 = p1.matcher(note_content);
        //Pattern p2 = Pattern.compile(s_selected);
        //Matcher m2 = p2.matcher(note_content);
        //TY zhencc 20161031 delete for missing image error end
        int startIndex = 0;

        while (m.find()) {
            if (m.start() > 0) {
                mText.append(note_content.substring(startIndex, m.start()));
            }

            String fullpath = m.group().toString();
            SpannableString ss = new SpannableString(m.group());
            String path = fullpath.substring(0, fullpath.length() - 5);
            // String type = path.substring(path.length() - 3, path.length());
            Bitmap bm = null;
            Bitmap rbm = null;
            Log.i(TAG, "loadData path is " + path + " m.group()=" + m.group());
            //Log.i(TAG, "loadData m.start()=" + m.start() + " m.end()=" + m.end());
            if (IsfileExists(path) && ImgCount < mMaxImgCount) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inTempStorage = new byte[100 * 1024];
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inPurgeable = true;
                options.inInputShareable = true;
                bm = BitmapFactory.decodeFile(path, options);
                //bm = BitmapFactory.decodeFile(path);
                rbm = resizetoscreen(bm);

                rbm = AddFrame(rbm);
                BigImageSpan span = new BigImageSpan(this, rbm);
                // ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mText.append(ss);
                startIndex = m.end();
                ImgCount++;
            } else {
                startIndex = m.end();
            }
        }

        mText.append(note_content.substring(startIndex, note_content.length()));

        //TY zhencc 20161031 add for missing image error begin
        String text_content = mText.getText().toString();
        Pattern p1 = Pattern.compile(s_normal);
        Matcher m1 = p1.matcher(text_content);
        Pattern p2 = Pattern.compile(s_selected);
        Matcher m2 = p2.matcher(text_content);
        //TY zhencc 20161031 add for missing image error end

        startIndex = 0;
        while (m1.find()) {
			/*
			 * if(m1.start() > 0){
			 * mText.append(note_content.substring(startIndex, m1.start())); }
			 */

            SpannableString ss = new SpannableString(m1.group());
            // String path = m1.group().toString();

            Drawable d = getResources().getDrawable(R.drawable.circle_normal);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            // VerticalImageSpan span = new VerticalImageSpan(d,
            // ImageSpan.ALIGN_BASELINE);
            BitmapDrawable bd = (BitmapDrawable) d;
            Bitmap bitmap = bd.getBitmap();
            bitmap = AddBorderToIcon(bitmap);
            VerticalImageSpan span = new VerticalImageSpan(this, bitmap);
            ss.setSpan(span, 0, m1.end() - m1.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // mText.append(ss);
            Editable editable = mText.getEditableText();
            editable.replace(m1.start(), m1.end(), ss);
            startIndex = m1.end();

			/*
			 * Map<String,String> map = new HashMap<String,String>();
			 * map.put("location", m1.start()+"-"+m1.end()); map.put("ID",
			 * s_normal); iconList.add(map);
			 */
        }

        startIndex = 0;
        while (m2.find()) {
			/*
			 * if(m2.start() > 0){
			 * mText.append(note_content.substring(startIndex, m2.start())); }
			 */

            SpannableString ss = new SpannableString(m2.group());
            // String path = m2.group().toString();

            Drawable d = getResources().getDrawable(R.drawable.circle_selected);
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            // VerticalImageSpan span = new VerticalImageSpan(d,
            // ImageSpan.ALIGN_BASELINE);
            BitmapDrawable bd = (BitmapDrawable) d;
            Bitmap bitmap = bd.getBitmap();
            bitmap = AddBorderToIcon(bitmap);
            VerticalImageSpan span = new VerticalImageSpan(this, bitmap);
            ss.setSpan(span, 0, m2.end() - m2.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // mText.append(ss);
            Editable editable = mText.getEditableText();
            editable.replace(m2.start(), m2.end(), ss);
            startIndex = m2.end();

			/*
			 * Map<String,String> map = new HashMap<String,String>();
			 * map.put("location", m2.start()+"-"+m2.end()); map.put("ID",
			 * s_selected); iconList.add(map);
			 */
        }

        // Log.i(TAG, "loadData iconList is " + iconList);
        mText.setCursorVisible(false);
        lock();
    }

    private void setImageSpanRange(int start, int end) {
        Spanned s = mText.getText();
        String text = mText.getText().toString();
        ImageSpan[] imageSpans;
        imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
        //int selectionStart = mText.getSelectionStart();
        Log.i(TAG, "setImageSpanRange");
        int newStart = start;
        int newEnd = end;

        for (ImageSpan span : imageSpans) {
            int spanStart = s.getSpanStart(span);
            int spanEnd = s.getSpanEnd(span);
            //Log.i(TAG, "spanStart=" + spanStart + " spanEnd=" + spanEnd);
            if (start >= spanStart && start < spanEnd) {
                newStart = spanStart;
            }
            if (end > spanStart && end < spanEnd) {
                newEnd = spanEnd;
            }
        }

        if (newStart != start || newEnd != end) {
            mText.setSelection(newStart, newEnd);
        }
	 /*bClipImage = true;
	 clipStart = newStart;
	 clipEnd = newEnd-1;
	 Log.i(TAG, "clipStart=" + clipStart + " clipEnd=" + clipEnd);*/
    }

    private boolean IsfileExists(String path) {
        Log.i(TAG, "IsfileExists path is " + path);
        try {
            File f = new File(path);
            if (!f.exists()) {
                Log.i(TAG, "IsfileExists no1");
                return false;
            }
        } catch (Exception e) {
            Log.i(TAG, "IsfileExists no2");
            // TODO: handle exception
            return false;
        }

        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg");
        Matcher m = p.matcher(path);
        if (m.find()) {
            Log.i(TAG, "IsfileExists yes");
            return true;
        }

        rootPath = getApplicationContext().getExternalCacheDir().getPath();
        Pattern p1 = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg");
        Matcher m1 = p1.matcher(path);
        if (m1.find()) {
            Log.i(TAG, "IsfileExists cache yes");
            return true;
        }
        return false;
    }

    private void Unlock() {
        editMenu.setVisibility(View.VISIBLE);
        //previewMenu.setVisibility(View.GONE);
        deleteNoteBtn.setVisibility(View.GONE);	

		/*mText.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				return null;
			}
		} });*/
    }

    private void lock() {
        editMenu.setVisibility(View.GONE);
        //previewMenu.setVisibility(View.VISIBLE);
        deleteNoteBtn.setVisibility(View.VISIBLE);        

		/*mText.setFilters(new InputFilter[] { new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				return source.length() < 1 ? dest.subSequence(dstart, dend) : "";
			}
		} });*/
    }

    String sBefore, sAfter;
    int delstart, delend, addstart, addcount;
    boolean bDel, bAdd, bCopy;

    private TextWatcher editWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Log.i(TAG, "onTextChanged: " + s + " start=" + start + " before=" + before + " count=" + count + " bCopy=" + bCopy);
            //Log.i(TAG, "onTextChanged the new changed string are " + s.toString().substring(start, (start + count)));

            if (count > 0) {
                if (bCopy == true) {
                    bCopy = false;
                    bAdd = false;
                } else {
                    bAdd = true;
                    addstart = start;
                    addcount = count;
                }
            } else {
                bAdd = false;
            }
            //Log.i(TAG, "onTextChanged end bAdd=" + bAdd + " bCopy=" + bCopy);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            float add = mText.getLineSpacingExtra();
            float mul = mText.getLineSpacingMultiplier();
            mText.setLineSpacing(0f, 1f);
            mText.setLineSpacing(add, mul);
            //Log.i(TAG, "beforeTextChanged: " + s + " start=" + start + " count=" + count + " after=" + after);
            if (count > after) {// for delete action
                String tempStr = s.toString().substring(start, (start + count));
                Log.i(TAG, "the old changed string are " + tempStr);
                if (dkeycode == KeyEvent.KEYCODE_DEL && count > 35) {
                    String temppath = tempStr.substring(0, tempStr.length() - 5);
                    //Log.i(TAG, "temppath is " + temppath);
                    if (IsfileExists(temppath)) {
                        deleteImage(temppath);
                    }
                } else {
                    beforeDelete((start + after), (start + count - 1));
                }
            }
            dkeycode = 0;
        }

        @Override
        public void afterTextChanged(Editable s) {
            //Log.i(TAG, "afterTextChanged: " + s + " bDel=" + bDel + " delstart=" + delstart + " delend=" + delend);
            if (bDel == true) {
                bDel = false;
                dkeycode = KeyEvent.KEYCODE_DEL;
                s.delete(delstart, delend);
            }
            bDel = false;

            if (bAdd == true) {
                //Log.i(TAG, "afterTextChanged the new changed string are " + s.toString().substring(addstart, (addstart + addcount)));
                String text = mText.getText().toString().substring(addstart, (addstart + addcount));
                //Log.i(TAG, "mText the new changed string are " + text);
                copyImage(text);
            }
            bAdd = false;
            //Log.i(TAG, "afterTextChanged end bAdd=" + bAdd);
            invalidateOptionsMenu();
        }
    };

    private void deleteImage(String note) {
        int len = note.length();
       // Log.i(TAG, "deleteImage: " + note + " substring is " + note.substring(len - 4, len));
        if (note.substring(len - 4, len).equals(".jpg")) {
            File file = new File(note);
            file.delete();
            ImgCount--;
            Log.i(TAG, "deleteImage " + note);
        }
    }

    public void beforeDelete(int tstart, int tend) {
        //Log.i(TAG, "beforeDelete mText are " + mText.getText().toString());
        Spanned s = mText.getText();
        String text = mText.getText().toString();
        ImageSpan[] imageSpans;
        imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
        int selectionStart = mText.getSelectionStart();
        Log.i(TAG, "beforeDelete " + selectionStart + " tstart=" + tstart + " tend=" + tend);

        for (ImageSpan span : imageSpans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            Log.i(TAG, "start=" + start + " end=" + end);
            if (start < tstart && (end - 1) >= tend) {
                int index = 0;
                String id = text.substring(start, end);
                //Log.i(TAG, "id is " + id);
                if (id != null) {
                    if (id.equals(s_normal)) {
                        bDel = true;
                        delstart = start;
                        delend = tstart;
                    } else if (id.equals(s_selected)) {
                        bDel = true;
                        delstart = start;
                        delend = tstart;
                    } else {
                        //TY zhencc 20161025 modify for monkey test StringIndexOutOfBoundsException begin
                        //String temppath = id.substring(0, id.length() - 5);
                        String temppath = id.substring(0, (id.length() - 5 > 0) ? (id.length() - 5) : id.length());
                        //TY zhencc 20161025 modify for monkey test StringIndexOutOfBoundsException end
                        if (IsfileExists(temppath)) {
                            File file = new File(temppath);
                            file.delete();
                            ImgCount--;                                                               
                            bDel = true;
                            delstart = start;
                            delend = tstart;
                        }
                    }
                    break;
                }
            } else if (tstart <= start && tend >= (end - 1)) {
                String id = text.substring(start, end);
                //Log.i(TAG, "more del id is " + id);
                if (id != null && id.length() > 10) {
                    //TY zhencc 20161025 modify for monkey test StringIndexOutOfBoundsException begin
                    //String temppath = id.substring(0, id.length() - 5);
                    String temppath = id.substring(0, (id.length() - 5 > 0) ? (id.length() - 5) : id.length());
                    //TY zhencc 20161025 modify for monkey test StringIndexOutOfBoundsException end
                    if (IsfileExists(temppath)) {
                        //Log.i(TAG, "before backupImage clipStart=" + clipStart + " clipEnd=" + clipEnd + " tstart="+ tstart + " tend=" + tend);
                        backupImage(temppath);
                        File file = new File(temppath);
                        file.delete();
                        ImgCount--;
                    }
                }
            }
        }
    }

    private void backupImage(String oldPath) {
        String name = getFileName(oldPath);
        String path = getApplicationContext().getExternalCacheDir().getPath();
        String newPath = path + "/Images/" + name;
        File filefolder = new File(getApplicationContext().getExternalCacheDir() + "/Images");
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }

        Log.i(TAG, "backupImage newPath is " + newPath + " oldfile is " + oldPath);
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
            //ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            //cm.setText();
        } catch (Exception e) {
            Log.i(TAG, "catch exception for download image: " + newPath);
            e.printStackTrace();
        }
    }


    private void PasteBitmap(Bitmap bitmap, int st, int en) {
        bitmap = resizetoscreen(bitmap);
        String newpath = saveResizedBitmap(bitmap);
        Log.i(TAG, "PasteBitmap newpath is " + newpath);
        if (newpath != null) {
            bitmap = AddFrame(bitmap);

            final BigImageSpan imageSpan = new BigImageSpan(this, bitmap);
            SpannableString spannableString = new SpannableString(newpath);
            spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            Editable editable = mText.getEditableText();
            //int selectionIndex = mText.getSelectionStart();
            //spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
            bCopy = true;
            //editable.insert(selectionIndex, spannableString);
            editable.replace(st, en, spannableString);
            //mText.append("\n");
            ImgCount++;
        }
    }


    private void copyImage(String note_content) {
        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");
        Matcher m = p.matcher(note_content);
        int deleteIndex = 0;

        if (note_content.equals("jpg0IMG0")) {
            Editable editable = mText.getEditableText();
            bAdd = false;
            editable.delete(addstart, (addstart + addcount));
        }

        while (m.find()) {
            String fullpath = m.group().toString();
            //SpannableString ss = new SpannableString(m.group());
            String path = fullpath.substring(0, fullpath.length() - 5);
            Bitmap bm = null;
            Bitmap rbm = null;

            String name = getFileName(path);
            String newPath = getApplicationContext().getExternalCacheDir().getPath();
            newPath = newPath + "/Images/" + name;

            Log.i(TAG, "copyImage path is " + path + " newPath is " + newPath);
            //Log.i(TAG, "m.start()=" + m.start() + " m.end()=" + m.end() + " deleteIndex=" + deleteIndex);
            if (IsfileExists(path)) {
                if (ImgCount < mMaxImgCount) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inTempStorage = new byte[100 * 1024];
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inPurgeable = true;
                options.inInputShareable = true;
                bm = BitmapFactory.decodeFile(path, options);
                PasteBitmap(bm, addstart + m.start() - deleteIndex, addstart + m.end() - deleteIndex);
                //Log.i(TAG, "copyImage after replace");
                }
            } else if (IsfileExists(newPath)) {
                if (ImgCount < mMaxImgCount) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inTempStorage = new byte[100 * 1024];
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inPurgeable = true;
                options.inInputShareable = true;
                bm = BitmapFactory.decodeFile(newPath, options);
                PasteBitmap(bm, addstart + m.start() - deleteIndex, addstart + m.end() - deleteIndex);
                    // Log.i(TAG, "copyImage after cut");
                }
            } else {
                Editable editable = mText.getEditableText();
                editable.delete(addstart + m.start() - deleteIndex, addstart + m.end() - deleteIndex);
                note_content = note_content.replace(fullpath, "");
                //Log.i(TAG, "m after delete mText is " + editable);
                Log.i(TAG, "m after delete note_content is " + note_content);
                deleteIndex += m.end() - m.start();
            }
        }

        Pattern p1 = Pattern.compile(s_normal);
        Matcher m1 = p1.matcher(note_content);
        deleteIndex = 0;
        while (m1.find()) {
            //Log.i(TAG, "copyImage m1.group()=" + m1.group());
            //Log.i(TAG, "m1.start()=" + m1.start() + " m1.end()=" + m1.end() + " deleteIndex=" + deleteIndex);
            Editable editable = mText.getEditableText();
            editable.delete(addstart + m1.start() - deleteIndex, addstart + m1.end() - deleteIndex);
            note_content = note_content.replace(s_normal, "");
            //Log.i(TAG, "m1 after delete mText is " + editable);
            //Log.i(TAG, "m1 after delete note_content is " + note_content);
            deleteIndex += m1.end() - m1.start();
        }

        Pattern p2 = Pattern.compile(s_selected);
        Matcher m2 = p2.matcher(note_content);
        deleteIndex = 0;
        while (m2.find()) {
            //Log.i(TAG, "copyImage m2.group()=" + m2.group());
            //Log.i(TAG, "copyImage m2.start()=" + m2.start() + " m2.end()=" + m2.end() + " deleteIndex=" + deleteIndex);
            Editable editable = mText.getEditableText();
            editable.delete(addstart + m2.start() - deleteIndex, addstart + m2.end() - deleteIndex);
            note_content = note_content.replace(s_selected, "");
            //Log.i(TAG, "m2 after delete mText is " + editable);
            //Log.i(TAG, "m2 after delete note_content is " + note_content);
            deleteIndex += m2.end() - m2.start();
        }
        //Log.i(TAG, "copyImage end");
    }

    private String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.length();
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }

    // yanghong add for PROD104153695
    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                               int oldBottom) {
        // if(oldBottom != 0 && bottom != 0 && ((oldBottom-bottom) >
        // screenHeight/3)){
        // the soft keyboard is popup
        // }else if(oldBottom != 0 && bottom != 0 && ((oldBottom-bottom) >
        // screenHeight/3)){
        // the soft keyboard is hidden
        // }
        mTextLengthFilter.setY(bottom);
    }

    @Override
    protected void onDestroy() {
        //Log.i(TAG, "View onDestroy");
        //TY zhencc 20160913 add for PROD104182065 begin
        SharedPreferences mSharedPreferences = getSharedPreferences(NotesUtils.GOODNOTE, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(NotesUtils.ITEM_SELECTED_INDEX, NotesUtils.default_item_index);
        editor.commit();
        //TY zhencc 20160913 add for PROD104182065 end
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //Log.i(TAG, "View onPause");
        if (doSaveActionFlag == 0) {
            doSave();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        //Log.i(TAG, "View onStop");
        // TY hanjq 20150113 add for PROD103512250 begin
		/*
		 * if(doSaveActionFlag == 0){ doSaveAction(); }
		 */
        // TY hanjq 20150113 add for PROD103512250 end
        super.onStop();
    }

    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);  
        actionBar.setHomeButtonEnabled(false);    
        actionBar.setDisplayShowHomeEnabled(false);  
        actionBar.setDisplayShowTitleEnabled(false);  
        actionBar.setDisplayShowCustomEnabled(true);  
        View edit_actionbar = LayoutInflater.from(this).inflate(  
                R.layout.edit_actionbar, null);  
        actionBar.setCustomView(edit_actionbar);
        
        returnListBtn = findViewById(R.id.to_list);
        deleteNoteBtn = (ImageButton) findViewById(R.id.delete_note);
        saveNoteBtn = (ImageButton) findViewById(R.id.save_note);
        shareNoteBtn = (ImageButton) findViewById(R.id.share_note);

        returnListBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doSaveAction();
                // TY hanjq 20150114 add for PROD103537493
                doSaveActionFlag = 1;

            }
        });

        deleteNoteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDlg();
            }
        });

        shareNoteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doShareAction();
            }
        });

        saveNoteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doSaveAction();
                // TY hanjq 20150114 add for PROD103537493
                doSaveActionFlag = 1;
            }
        });
    }

    private class TextClickEvent implements OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = mText.getText();
            String text = mText.getText().toString();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = mText.getSelectionStart();
            //Log.i(TAG, "onClick " + selectionStart);
					 
            if (bClickOnEmpty) {
                //Log.i(TAG, "onClick on bottom empty");
                return;
            }
			
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                //Log.i(TAG, "start=" + start + " end=" + end);
                if (selectionStart >= start && selectionStart <= end) {
                    int index = 0;
                    String id = text.substring(start, end);
                    //Log.i(TAG, "id is " + id);
                    if (id != null && id.length() > 0) {
                        if (id.equals(s_normal)) {
                            SpannableString ss = new SpannableString(s_selected);
                            Drawable d = getResources().getDrawable(R.drawable.circle_selected);
                            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                            BitmapDrawable bd = (BitmapDrawable) d;
                            Bitmap bitmap = bd.getBitmap();
                            bitmap = AddBorderToIcon(bitmap);
                            VerticalImageSpan tempspan = new VerticalImageSpan(NoteView.this, bitmap);
                            ss.setSpan(tempspan, 0, s_selected.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            Editable editable = mText.getEditableText();
                            dkeycode = KeyEvent.KEYCODE_DEL;
                            editable.delete(start, end);
                            bCopy = true;
                            editable.insert(start, ss);
                        } else if (id.equals(s_selected)) {
                            SpannableString ss = new SpannableString(s_normal);
                            Drawable d = getResources().getDrawable(R.drawable.circle_normal);
                            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                            BitmapDrawable bd = (BitmapDrawable) d;
                            Bitmap bitmap = bd.getBitmap();
                            bitmap = AddBorderToIcon(bitmap);
                            VerticalImageSpan tempspan = new VerticalImageSpan(NoteView.this, bitmap);
                            ss.setSpan(tempspan, 0, s_normal.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            Editable editable = mText.getEditableText();
                            dkeycode = KeyEvent.KEYCODE_DEL;
                            editable.delete(start, end);
                            bCopy = true;
                            editable.insert(start, ss);
                        } else {
                            //TY zhencc 20161010 modify for monkey test StringIndexOutOfBoundsException begin
                            //String temppath = id.substring(0, id.length() - 5);
                            String temppath = id.substring(0, (id.length() - 5 > 0) ? (id.length() - 5) : id.length());
                            //TY zhencc 20161010 modify for monkey test StringIndexOutOfBoundsException end
                            if (IsfileExists(temppath)) {
                                //if (id.equals(sel_Img)) {
                                Intent intent = new Intent(NoteView.this, DisplayActivity.class);
                                intent.putExtra("imgPath", temppath);
                                startActivityForResult(intent, REQUEST_DISPLAY_IMAGE);
                                return;
                            }
                        }
                        break;
                    }
                }
            }
            //sel_Img = "";
        }
    }

    // TY hanjq 20150114 delete for PROD103537493 begin
	/*
	 * TIANYURD:songbangbang 20150417 recover this process for PROD103706221
	 * begin
	 */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doSaveAction();
            doSaveActionFlag = 1;
        }
        return super.onKeyDown(keyCode, event);
    }
	/*
	 * TIANYURD:songbangbang 20150417 recover this process for PROD103706221 end
	 */
    // TY hanjq 20150114 delete for PROD103537493 end

    @Override
    protected void onResume() {
        super.onResume();
        //Log.i(TAG, "onResume");
        Log.i(TAG, "density=" + getResources().getDisplayMetrics().density);

        if(mFirstEnter) {
            mFirstEnter = false;

            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(200);
            scaleAnimation.setStartOffset(300);
            scaleAnimation.setFillAfter(true);
            scaleAnimation.setFillEnabled(true);
            animationSet.addAnimation(scaleAnimation);
            rootview.startAnimation(animationSet);
        }
    }

    private void doSaveAction() {
        String text = mText.getText().toString();
        //Log.i(TAG, "doSaveAction text is " + text);

        if (mUri == null) {
            //Log.i(TAG, "new create note");
            if (TextUtils.isEmpty(text) || text.equals(s_selected) || text.equals(s_normal)) {
                // TY zhencc 20160826 delete for PROD104174127 begin
                // Toast.makeText(this, R.string.save_none,
                // Toast.LENGTH_LONG).show();
                // TY zhencc 20160826 delete for PROD104174127 end
                finish();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(Notes.COLUMN_NAME_NOTE, text);
            // chendy 20131108 add for PROD102320952 start
            values.put(Notes.COLUMN_NAME_GROUP, "");

            // TY zhencc add for search begin
            values.put(Notes.COLUMN_NAME_SEARCH_NOTE, getSearchNote(text));
            // TY zhencc add for search end

            // yuhf add for change skin function
            Log.i(TAG, "doSaveAction1 skinIndex=" + skinIndex);
            values.put(Notes.COLUMN_NAME_SKIN_INDEX, skinIndex);

            Uri retrunUri = getContentResolver().insert(Notes.CONTENT_URI, values);
            Log.i(TAG, "doSaveAction retrunUri=" + retrunUri);
            if (retrunUri == null) {
                Toast.makeText(this, R.string.sdcard_full, Toast.LENGTH_LONG).show();
            } else {
                // TY zhencc 20160825 add for PROD104174124 begin
                SharedPreferences mSharedPreferences = getSharedPreferences(NotesUtils.GOODNOTE, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(NotesUtils.ITEM_SELECTED_INDEX, NotesUtils.default_item_index);
                editor.commit();
                // TY zhencc 20160825 add for PROD104174124 end

                // Toast.makeText(this, R.string.note_saved,
                // Toast.LENGTH_LONG).show();
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                finish();
            }
        } else {
            Log.i(TAG, "update old note");
            if (TextUtils.isEmpty(text) || text.equals(s_selected) || text.equals(s_normal)) {
                // TY zhencc 20160829 modify for PROD104174151 begin
                // Toast.makeText(this, R.string.empty_note,
                // Toast.LENGTH_LONG).show();
                deleteEmptyNote();
                // TY zhencc 20160829 modify for PROD104174151 end
                return;
            }

            Log.i(TAG, "doSaveAction 3");
            int count = updateNote(text);
            if (count > 0) {
                // Toast.makeText(this, R.string.note_saved,
                // Toast.LENGTH_LONG).show();
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                finish();
            } else {
                Toast.makeText(this, R.string.sdcard_full, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private int updateNote(String text) {// , String title) {
        ContentValues values = new ContentValues();
        Log.i(TAG, "updateNote");
		/*
		 * if (TextUtils.isEmpty(title)) { Log.w(TAG, "updateNote title kong");
		 * values.put(Notes.COLUMN_NAME_TITLE, ""); } else { Log.w(TAG,
		 * "updateNote title= " + title); values.put(Notes.COLUMN_NAME_TITLE,
		 * title); }
		 */
        values.put(Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        values.put(Notes.COLUMN_NAME_NOTE, text);
        // TY zhencc add for search begin
        values.put(Notes.COLUMN_NAME_SEARCH_NOTE, getSearchNote(text));
        // TY zhencc add for search end
        // chendy 20131108 add for PROD102320952 start
        values.put(Notes.COLUMN_NAME_GROUP, "");
        // chendy 20131108 add for PROD102320952 end

        // yuhf add for change skin function
        Log.i(TAG, "updateNote skinIndex=" + skinIndex);
        values.put(Notes.COLUMN_NAME_SKIN_INDEX, skinIndex);
        return getContentResolver().update(mUri, values, null, null);
    }

    // TY zhencc add for search begin
    private String getSearchNote(String note) {
        // Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
        // Pattern p=Pattern.compile("/(.*)\\.(jpg|png|bmp)");
        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");
        Matcher m = p.matcher(note);
        while (m.find()) {
            note = m.replaceAll("");
        }

        Pattern p1 = Pattern.compile(s_normal);
        Matcher m1 = p1.matcher(note);
        while (m1.find()) {
            note = m1.replaceAll("");
        }

        Pattern p2 = Pattern.compile(s_selected);
        Matcher m2 = p2.matcher(note);
        while (m2.find()) {
            note = m2.replaceAll("");
        }

        return note;
    }
    // TY zhencc add for search end

    private void doSave() {
        String text = mText.getText().toString();
        Log.i(TAG, "doSave text is " + text);

        if (mUri == null) {
            if (TextUtils.isEmpty(text) || text.equals(s_selected) || text.equals(s_normal)) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(Notes.COLUMN_NAME_NOTE, text);
            // chendy 20131108 add for PROD102320952 start
            values.put(Notes.COLUMN_NAME_GROUP, "");

            // TY zhencc add for search begin
            values.put(Notes.COLUMN_NAME_SEARCH_NOTE, getSearchNote(text));
            // TY zhencc add for search end

            // yuhf add for change skin function
            values.put(Notes.COLUMN_NAME_SKIN_INDEX, skinIndex);

            Uri retrunUri = getContentResolver().insert(Notes.CONTENT_URI, values);
            if (retrunUri == null) {
            } else {
                mUri = retrunUri;
            }
        } else {
            if (TextUtils.isEmpty(text) || text.equals(s_selected) || text.equals(s_normal)) {
                return;
            }
            int count = updateNote(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_view_menu, menu);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, NoteView.class), null, intent, 0,
                null);*/
		/* TIANYURD:songbangbang 20150401 add for TOS3.0 begin */
        // menu.findItem(R.id.menu_delete).setVisible(true);
        //menu.findItem(R.id.menu_save).setVisible(true);
		/* TIANYURD:songbangbang 20150401 add for TOS3.0 end */
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        String text = mText.getText().toString();
        int textLen = text.length();
        //Log.i(TAG, "initActionbar textLen=" + textLen);

        if (textLen > 0) {
            if (text.equals(s_selected) || text.equals(s_normal)) {
                shareNoteBtn.setVisibility(View.GONE);				
            } else {
                shareNoteBtn.setVisibility(View.VISIBLE);
            }
            saveNoteBtn.setVisibility(View.VISIBLE);
        } else {
            shareNoteBtn.setVisibility(View.GONE);	
            saveNoteBtn.setVisibility(View.GONE);	
        }

        if (mViewndex == Notes.EDIT_VIEW) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

	if (mViewndex == Notes.ADD_VIEW) {
		deleteNoteBtn.setVisibility(View.GONE);
		mText.setCursorDrawableColor();
		if (!mText.isCursorVisible()) {
			mText.setCursorVisible(true);
		}
	}

        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Log.i(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                doSaveAction();
                // TY hanjq 20150114 add for PROD103537493 begin
                doSaveActionFlag = 1;
                // TY hanjq 20150114 add for PROD103537493 end
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    /* duhuan 20160713 add for share begin */
    private void doShareAction() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.shareimagecreaate), Toast.LENGTH_LONG)
                .show();
        String text = mText.getText().toString();
        Thread t = new Thread(new shareRunnable(text));
        t.start();
        Log.i(TAG, "doSaveAction text is " + text);
    }

    public class shareRunnable implements Runnable {
        String Rtext;

        shareRunnable(String text) {
            Rtext = text;
        }

        @Override
        public void run() {
            drawBitmap(Rtext);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            String path = Environment.getExternalStorageDirectory() + "/gnotes/share.png";
            Uri uri = Uri.parse("file:///" + path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share)));
        }

    }

    // duhuan20160824 add for PROD104174075 begin
    public int textsizeMatchDiffScr() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float ratioWidth = (float) screenWidth / 720;
        float ratioHeight = (float) screenHeight / 1280;
        float RATIO = Math.min(ratioWidth, ratioHeight);
        int TEXT_SIZE = Math.round(18 * RATIO);
        return TEXT_SIZE;
    }

    // duhuan20160824 add for PROD104174075 end
    private void drawBitmap(String text) {
	int xStart = 40;
	// duhuan 20160819 change for PROD104174075 begin
	int stringlength = textsizeMatchDiffScr();
        // duhuan 20160819 change for PROD104174075 end
	int lineHeight = 75;
	int bitmapHeight = 300;
	int lineNumber = 0;
	int bitmapNumber = 0;
	int bitmapTotalHeight = 0;
	int heightCount = 80;
        String s1 = "/storage/";
        String s2 = "";
        Resources res = getResources();
        Bitmap skin_head = BitmapFactory.decodeResource(res, HeadId[skinIndex]);
        Bitmap skin_list = BitmapFactory.decodeResource(res, BackgroundRepeatId[skinIndex]);
        Bitmap skin_tail = BitmapFactory.decodeResource(res, TailId[skinIndex]);
        int skin_headHeight = skin_head.getHeight();
        int skin_listHeight = skin_list.getHeight();
        int skin_listWidth = skin_list.getWidth();
        int skin_tailHeight = skin_tail.getHeight();
        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");

        String[] texts = text.split("\n");
        for (int j = 0; j < texts.length; j++) {
            Matcher m = p.matcher(texts[j]);
            int pathStrLen = p.toString().length();
            int containBmNuber = 0;
            while (m.find()) {
                bitmapNumber = bitmapNumber + 1;
                containBmNuber = containBmNuber + 1;
                // String path = texts[j].substring(m.start(), m.end());
                String fullpath = m.group().toString();
                String path = fullpath.substring(0, fullpath.length() - 5);
                Log.v("duhuan", "path=" + path);
                Bitmap bm = BitmapFactory.decodeFile(path);
                bitmapTotalHeight = bitmapTotalHeight + lineHeight + bm.getHeight();
            }
            lineNumber = lineNumber + (texts[j].length() - pathStrLen * containBmNuber) / stringlength + 1;
        }
        int heightBitmap = (lineNumber + 2) * lineHeight + bitmapTotalHeight;
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        // int widthBitmap = wm.getDefaultDisplay().getWidth();
        int widthBitmap = skin_head.getWidth();
        int duanshu = text.length() / stringlength + 1;
        Bitmap bitmap = Bitmap.createBitmap(widthBitmap, heightBitmap, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setTextSize(35);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        int skinHeight = 0;
        canvas.drawBitmap(skin_head, 0, 0, paint);
        skinHeight = skinHeight + skin_headHeight;
        while (skinHeight < heightBitmap) {
            canvas.drawBitmap(skin_list, 0, skinHeight, paint);
            for (int i = skin_listWidth; i < widthBitmap; i = i + skin_listWidth) {
                canvas.drawBitmap(skin_list, i, skinHeight, paint);
            }
            skinHeight = skinHeight + skin_listHeight;
        }
        canvas.drawBitmap(skin_tail, 0, heightBitmap - skin_tailHeight, paint);

        Bitmap bitmapCircleNormal = BitmapFactory.decodeResource(res, R.drawable.circle_normal);
        Bitmap bitmapCircleSelected = BitmapFactory.decodeResource(res, R.drawable.circle_selected);
        int circleWidth = bitmapCircleNormal.getWidth() + 10;
        for (int j = 0; j < texts.length; j++) {

            ArrayList<String> noteList = new ArrayList<String>();
            if (texts[j].contains(s_normal)) {
		noteList.add(s_normal);
                texts[j] = texts[j].substring(7, texts[j].length());
            } else if (texts[j].contains(s_selected)) {
                noteList.add(s_selected);
                texts[j] = texts[j].substring(7, texts[j].length());
            }
            Matcher mnote = p.matcher(texts[j]);
            int textstart = 0;
            int textend = 0;
            while (mnote.find()) {
		if (textend < mnote.start()) {
                    textend = mnote.start();
                    noteList.add(texts[j].substring(textstart, textend));
                    String fullpath = mnote.group().toString();
                    String path = fullpath.substring(0, fullpath.length() - 5);
                    noteList.add(path);
                    textstart = mnote.end();
                    textend = mnote.end();
		} else {
                    String fullpath = mnote.group().toString();
                    String path = fullpath.substring(0, fullpath.length() - 5);
                    noteList.add(path);
                    textstart = mnote.end();
                    textend = mnote.end();
                }
            }
            if (textstart < texts[j].length()) {
                noteList.add(texts[j].substring(textstart, texts[j].length()));
            }

            boolean hasCircle = false;
            for (String tmp : noteList) {
                if (tmp.contains(s_normal)) {
                    canvas.drawBitmap(bitmapCircleNormal, xStart, heightCount + lineHeight - circleWidth / 2, paint);
                    hasCircle = true;
                } else if (tmp.contains(s_selected)) {
                    canvas.drawBitmap(bitmapCircleSelected, xStart, heightCount + lineHeight - circleWidth / 2, paint);
                    hasCircle = true;
                } else {
                    if (tmp.contains("/Images")) {
                        //duhuan20151021 change for Wrong ImageUri begin
                        String path = tmp;
                        Bitmap bm = null;
                        if (IsfileExists(path)) {
                            bm = BitmapFactory.decodeFile(path);
                        }
                        if (null != bm) {
                            bm = resizetoshare(bm, widthBitmap);
                            int height = bm.getHeight();
                            heightCount = heightCount + lineHeight;
                            canvas.drawBitmap(bm, xStart, heightCount, paint);
                            heightCount = heightCount + height;
                        } else {
                            int mline = tmp.length() / stringlength + 1;
                            for (int i = 0; i < mline; i++) {
                                heightCount = heightCount + lineHeight;
                                int XIncludeBitmapStart = xStart + circleWidth;
                                if (i == mline - 1) {
                                    String drawtext = tmp.substring(stringlength * i, tmp.length());
                                    if (hasCircle) {
                                        canvas.drawText(drawtext, xStart + circleWidth, heightCount, paint);
                                        hasCircle = false;
                                    } else {
                                        canvas.drawText(drawtext, xStart, heightCount, paint);
                                    }
                                } else {
                                    String drawtext = tmp.substring(stringlength * i, stringlength * (i + 1));
                                    if (hasCircle) {
                                        canvas.drawText(drawtext, xStart + circleWidth, heightCount, paint);
                                        hasCircle = false;
                                    } else {
                                        canvas.drawText(drawtext, xStart, heightCount, paint);
                                    }
                                }
                            }
                        }
                        //duhuan20151021 change for Wrong ImageUri end
                    } else {
                        int mline = tmp.length() / stringlength + 1;
                        for (int i = 0; i < mline; i++) {
                            heightCount = heightCount + lineHeight;
                            int XIncludeBitmapStart = xStart + circleWidth;
                            if (i == mline - 1) {
                                String drawtext = tmp.substring(stringlength * i, tmp.length());
                                if (hasCircle) {
                                    canvas.drawText(drawtext, xStart + circleWidth, heightCount, paint);
                                    hasCircle = false;
                                } else {
                                    canvas.drawText(drawtext, xStart, heightCount, paint);
                                }
                            } else {
                                String drawtext = tmp.substring(stringlength * i, stringlength * (i + 1));
                                if (hasCircle) {
                                    canvas.drawText(drawtext, xStart + circleWidth, heightCount, paint);
                                    hasCircle = false;
                                } else {
                                    canvas.drawText(drawtext, xStart, heightCount, paint);
                                }
                            }
                        }
                    }
                }
            }
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        String path = Environment.getExternalStorageDirectory() + "/gnotes";
        File mfile = new File(path);

        if (!mfile.exists()) {
            mfile.mkdir();
        }
        try {
            FileOutputStream fos = new FileOutputStream(mfile + "/share.png");
            Log.v("duhuan", "mfile=" + mfile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Bitmap resizetoshare(Bitmap bitmap, int widthBitmap) {
        // DisplayMetrics dm = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenwidth = widthBitmap;
        int padding = getResources().getDimensionPixelSize(R.dimen.g_note_padding_left);
        int border = getResources().getDimensionPixelSize(R.dimen.g_note_image_border);
        int shadow = getResources().getDimensionPixelSize(R.dimen.g_note_image_shadow);
        int contentwidth = screenwidth - 2 * padding - 2 * border - 2 * shadow;

        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();

        if (contentwidth >= imgWidth) {
            return bitmap;
        } else {
            float scale = (float) (contentwidth * 1.0 / imgWidth);
            Matrix mx = new Matrix();
            mx.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
            return bitmap;
        }
    }

    public void deleteCurrentNoteEnd() {
        deleteNoteBtn.setImageResource(R.drawable.title_bar_delete_button_normal);
        rootview.setVisibility(View.INVISIBLE);

        Intent data = new Intent();
        data.putExtra("curPos", mCurPos);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    private class ScaleAnimationListener implements Animation.AnimationListener {

        ScaleAnimationListener() {

        }

        @Override
        public void onAnimationStart(Animation var1) {

        };

        @Override
        public void onAnimationEnd(Animation var1) {
            deleteCurrentNoteEnd();
        };

        @Override
        public void onAnimationRepeat(Animation var1) {

        };
    };

    /* duhuan 20160713 add for share end */
    private void deleteCurrentNote() {
        /* wanghg modify for delete note animation  20161227 start */
        //ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.delete_progress), true);
        deleteNoteBtn.setImageResource(R.drawable.title_delete);
        AnimationDrawable anim = (AnimationDrawable) deleteNoteBtn.getDrawable();
        anim.setOneShot(true);
        anim.start();

		/* wanghg modify for delete note animation  20161227 start */
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 0.1f, 1, 0.1f,
                Animation.RELATIVE_TO_SELF, 0.61f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setFillEnabled(true);
        animationSet.addAnimation(scaleAnimation);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, -2000);
        translateAnimation.setDuration(600);
        translateAnimation.setFillAfter(true);
        translateAnimation.setFillEnabled(true);
        translateAnimation.setAnimationListener(new ScaleAnimationListener());
        animationSet.addAnimation(translateAnimation);

        rootview.startAnimation(animationSet);
        /* wanghg modify end */

        /*String selection = "_id=" + mUri.getLastPathSegment();
        NoteQueryHandler qh = new NoteQueryHandler(this.getContentResolver(), this, null);
        /* wanghg modify for delete note animation  20161227 start */
        //qh.startDelete(Notes.NOTEREADING_DELETE_TOKEN, rootview, Notes.CONTENT_URI, selection, null);
        /* wanghg modify end */

        /*imgList.clear();
        loadImage(mText.getText().toString(), imgList);
        String path = null;
        for (int i = 0; i < imgList.size(); i++) {
            Map map = imgList.get(i);
            path = imgList.get(i).get("path");
            Log.i(TAG, "deleteCurrentNote path is " + path + " location is " + imgList.get(i).get("location"));
            File file = new File(path);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        }*/

        doSaveActionFlag = 1; // to avoid the save action when calling onStop()
    }

    // TY zhencc 20160829 add for PROD104174151 begin
    private void deleteEmptyNote() {
        String selection = "_id=" + mUri.getLastPathSegment();
        NoteQueryHandler qh = new NoteQueryHandler(this.getContentResolver(), this, null);
        qh.startDelete(Notes.NOTEEDITING_DELETE_TOKEN, null, Notes.CONTENT_URI, selection, null);

        imgList.clear();
        loadImage(mText.getText().toString(), imgList);
        String path = null;
        for (int i = 0; i < imgList.size(); i++) {
            Map map = imgList.get(i);
            path = imgList.get(i).get("path");
            Log.d(TAG, "deleteCurrentNote path is " + path + " location is " + imgList.get(i).get("location"));
            File file = new File(path);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        }
        doSaveActionFlag = 1; // to avoid the save action when calling onStop()
    }
    // TY zhencc 20160829 add for PROD104174151 end

    private void loadImage(String note, List imagelist) {
        String rootPath = getApplicationContext().getExternalFilesDir(null).getPath();
        Pattern p = Pattern.compile(rootPath + "/Images/NoteImg\\d+\\.jpg0IMG0");
        Matcher m = p.matcher(note);
        // int startIndex = 0;

        while (m.find()) {
            String fullpath = m.group().toString();
            String path = fullpath.substring(0, fullpath.length() - 5);
            if (IsfileExists(path)) {
                // startIndex = m.end();
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imagelist.add(map);
            } /*
				 * else { startIndex = m.end(); }
				 */
        }

    }

    private void deleteDlg() {
        CustomDeleteDialog delDialog = new CustomDeleteDialog(this);
        delDialog.setMessage(R.string.del_confirm);
        delDialog.setSureBtn(R.string.sure_del, new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                deleteCurrentNote();
            }
        });

        delDialog.setCancelBtn(R.string.cancel, null);
        delDialog.showDialog();
    }

    private void getimage() {
	String text = mText.getText().toString();
	int textLen = text.length();
	int leftLen = mMaxLength - 1 - textLen;

	if (leftLen < mImgLength) {
 		Toast.makeText(getApplicationContext(), R.string.editor_full, Toast.LENGTH_SHORT).show();
		return;
	}
	
        if (ImgCount < mMaxImgCount) {
            Intent intent;
            intent = new Intent();
            intent.setType("image/*");
            // TY zhencc 20160816 modify for PROD104174071 begin
            // intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setAction(Intent.ACTION_PICK);
            // TY zhencc 20160816 modify for PROD104174071 end
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } else {
            Toast.makeText(getApplicationContext(), R.string.image_full, Toast.LENGTH_SHORT).show();
        }
    }

	/*
	 * private void takepic() { Intent intent; intent = new
	 * Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	 * intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
	 * startActivityForResult(intent, 2); }
	 */

    private void intentCamera() {
	String text = mText.getText().toString();
	int textLen = text.length();
	int leftLen = mMaxLength - 1 - textLen;

	if (leftLen < mImgLength) {
 		Toast.makeText(getApplicationContext(), R.string.editor_full, Toast.LENGTH_SHORT).show();
		return;
	}
	
        if (ImgCount < mMaxImgCount) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File filefolder = new File(getApplicationContext().getExternalFilesDir(null) + "/Images");
            if (!filefolder.exists()) {
                filefolder.mkdirs();
            }

            mPicUri = Uri.fromFile(new File(getApplicationContext().getExternalFilesDir(null) + "/Images/",
                    "cameraImg" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mPicUri);
            cameraIntent.putExtra("return-data", true);
            Log.i(TAG, "mPicUri is " + mPicUri);
            startActivityForResult(cameraIntent, REQUEST_PICK_PHOTO);
        } else {
            Toast.makeText(getApplicationContext(), R.string.image_full, Toast.LENGTH_SHORT).show();
        }
    }

    private void AddListbutton() {
        int offset = getCurrentCursorLineStart();
        int end_offset = getCurrentCursorLineEnd();

        Spanned s = mText.getText();
        String text = mText.getText().toString();
        ImageSpan[] imageSpans;
        imageSpans = s.getSpans(offset, end_offset, ImageSpan.class);
        // int selectionStart = mText.getSelectionStart();
        Log.i(TAG, "AddListbutton offset=" + offset + " end_offset=" + end_offset);
        for (ImageSpan span : imageSpans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            Log.i(TAG, "start=" + start + " end=" + end);
            if (start == end) {
                continue;
            }
            int index = 0;
            String id = text.substring(start, end);
            Log.i(TAG, "id is " + id);

            if (id != null && (id.equals(s_normal) || id.equals(s_selected))) {
                Editable editable = mText.getEditableText();
                editable.delete(start, end);
                // iconList.remove(index);
                return;
            }
        }
        insertIcon(offset);
    }

    private void drawpad() {
	String text = mText.getText().toString();
	int textLen = text.length();
	int leftLen = mMaxLength - 1 - textLen;

	if (leftLen < mImgLength) {
 		Toast.makeText(getApplicationContext(), R.string.editor_full, Toast.LENGTH_SHORT).show();
		return;
	}
		
        if (ImgCount < mMaxImgCount) {
            Intent intent;
            intent = new Intent(this, PaintActivity.class);
            startActivityForResult(intent, REQUEST_PICK_PAINT);
        } else {
            Toast.makeText(getApplicationContext(), R.string.image_full, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeSkin() {
        Intent intent;
        intent = new Intent(this, PreviewActivity.class);
        // TY zhencc 20160825 add for PROD104174124 begin
        intent.setData(mUri);
        // TY zhencc 20160825 add for PROD104174124 end
        startActivityForResult(intent, REQUEST_CHANGE_SKIN);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri;// = data.getData();
            ContentResolver cr = getContentResolver();
            // Log.i(TAG, "uri is " + uri);
            Bitmap bitmap = null;
            Bundle extras = null;
            if (requestCode == REQUEST_PICK_IMAGE) {
                uri = data.getData();
                Log.i(TAG, "uri is " + uri);
                String path = null;
                final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

                if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
                    if (isMediaDocument(uri)) {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        }
                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[]{split[1]};
                        path = getDataColumn(this, contentUri, selection, selectionArgs);
                    }
                } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                    path = getDataColumn(this, uri, null, null);
                } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    path = uri.getPath();
                }

                if (path != null) {
                    Log.i(TAG, "path is " + path);
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inTempStorage = new byte[100 * 1024];
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
                        //bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        Log.i(TAG, "bitmap width is " + bitmap.getWidth());
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    InsertBitmap(bitmap, path, false);
                }
            } else if (requestCode == REQUEST_PICK_PHOTO) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inTempStorage = new byte[100 * 1024];
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inPurgeable = true;
                    options.inInputShareable = true;
                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(mPicUri), null, options);
                    //bitmap = BitmapFactory.decodeStream(cr.openInputStream(mPicUri));
                    Log.i(TAG, "bitmap width is " + bitmap.getWidth());
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String path = mPicUri.getPath();
                Log.i(TAG, "path 2 is " + path);
                InsertBitmap(bitmap, path, true);
            } else if (requestCode == REQUEST_PICK_PAINT) {
                extras = data.getExtras();
                String path = extras.getString("paintPath");
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inTempStorage = new byte[100 * 1024];
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inPurgeable = true;
                options.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(path, options);
                //bitmap = BitmapFactory.decodeFile(path);
                InsertBitmap(bitmap, path, true);
            } else if (requestCode == REQUEST_CHANGE_SKIN) {
                extras = data.getExtras();
                int index = extras.getInt("SkinIndex");
                Log.i(TAG, "index=" + index);
                setSelectedStyle(index);
            } else if (requestCode == REQUEST_DISPLAY_IMAGE) {
                extras = data.getExtras();
                String delpath = extras.getString("deletepath");
                delpath = delpath + s_imgsuff;
                String text = mText.getText().toString();
                Log.i(TAG, "text=" + text + " delpath=" + delpath);
                Pattern p = Pattern.compile(delpath);
                Matcher m = p.matcher(text);
                if (m.find()) {
                    Editable editable = mText.getEditableText();
                    Log.i(TAG, "editable=" + editable + " m.start()=" + m.start() + " m.end()=" + m.end());
                    try {
                        editable.delete(m.start(), m.end());
                    } catch (Exception e) {
                        Log.i(TAG, "catch exception: " + e);
                    }
                    ImgCount--;//for PROD104184067
                }
            }
        }
    }

    private void setSelectedStyle(int index) {
        skinIndex = index;
        LayoutParams lp;
        topView = findViewById(R.id.note_top);
        lp = topView.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(TopHeight[index]);
        topView.setLayoutParams(lp);
        topView.setBackgroundResource(HeadId[index]);

        bottomView = findViewById(R.id.note_bottom);
        lp = bottomView.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(BottomHeight[index]);
        bottomView.setLayoutParams(lp);
        bottomView.setBackgroundResource(TailId[index]);
        bottomView.setVisibility(View.VISIBLE);
        if (index > 0) {
            mText.SetLineColor(Color.TRANSPARENT);
        } else {
            mText.SetLineColor(getResources().getColor(R.color.edit_text_line_color));
        }
        //Log.i(TAG, "index=" + index + " head=" + HeadId[index] + " tail=" + TailId[index]);
        mText.setBackgroundResource(BackgroundId[index]);
        int padding0 = getResources().getDimensionPixelSize(R.dimen.g_note_padding_left);
        int padding1 = getResources().getDimensionPixelSize(R.dimen.g_note_padding_top);
        int padding2 = getResources().getDimensionPixelSize(R.dimen.g_note_text_padding_left);		
        mText.setPadding(padding2, padding1, padding0, 0);
        mText.setTextColor(getResources().getColor(TextColor[index]));
    }

    private void InsertBitmap(Bitmap bitmap, String imgPath, boolean isDelete) {
        bitmap = resizetoscreen(bitmap);
        String newpath = saveResizedBitmap(bitmap);
        Log.i(TAG, "InsertBitmap imgPath is " + imgPath + " newpath is " + newpath);
        if (newpath != null) {
            if (isDelete) {
                File file = new File(imgPath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
            }
            bitmap = AddFrame(bitmap);

            final BigImageSpan imageSpan = new BigImageSpan(this, bitmap);
            // final ImageSpan imageSpan = new ImageSpan(this, bitmap);
            SpannableString spannableString = new SpannableString(newpath);
            spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);

            Editable editable = mText.getEditableText();
            int selectionIndex = mText.getSelectionStart();
            spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
            editable.insert(selectionIndex, "\n");
            bCopy = true;			
            editable.insert(selectionIndex + 1, spannableString);
            editable.insert(selectionIndex + 1 + spannableString.length(), "\n");
            ImgCount++;
        }
    }

    private int getCurrentCursorLineStart() {
        int selectionStart = mText.getSelectionStart();
        Layout layout = mText.getLayout();
        int offset = 0;
        int line = 0;
        String text = mText.getText().toString();

        offset = selectionStart;
        Log.i(TAG, "text length=" + text.length() + " offset=" + offset);
        if (offset > 0) {
            if (offset == text.length()) {
                if (text.charAt(offset - 1) == '\n') {
                    return offset;
                } else {
                    offset--;
                }
            }

            if (text.charAt(offset) == '\n' && offset == selectionStart) {
                if (text.charAt(offset - 1) == '\n') {
                    return offset;
                } else {
                    offset--;
                }
            }

            while (offset > 0 && text.charAt(offset) != '\n') {
                Log.i(TAG, "text[" + offset + "]=" + text.charAt(offset));
                offset--;
            }

            if (text.charAt(offset) == '\n' && offset < selectionStart) {
                offset++;
            }
        } else {
            return 0;
        }

		/*
		 * if (selectionStart != -1) { line =
		 * layout.getLineForOffset(selectionStart); } offset =
		 * layout.getLineStart(line);
		 */
        Log.i(TAG, "start offset=" + offset + " selectionStart=" + selectionStart);
        return offset;
    }

    private int getCurrentCursorLineEnd() {
        int selectionStart = mText.getSelectionStart();
        Layout layout = mText.getLayout();
        int offset = 0;
        int line = 0;
        String text = mText.getText().toString();

        offset = selectionStart;
        while (offset < text.length() && text.charAt(offset) != '\n') {
            Log.i(TAG, "text[" + offset + "]=" + text.charAt(offset));
            offset++;
        }
        // offset--;

		/*
		 * if (selectionStart != -1) { line =
		 * layout.getLineForOffset(selectionStart); } offset =
		 * layout.getLineEnd(line);
		 */
        Log.i(TAG, "end offset=" + offset + " selectionStart=" + selectionStart);
        return offset;
    }

    private void insertIcon(int offset) {
	String text = mText.getText().toString();
	int textLen = text.length();
	int leftLen = mMaxLength - 1 - textLen;

	if (leftLen < mIconLength) {
 		Toast.makeText(getApplicationContext(), R.string.editor_full, Toast.LENGTH_SHORT).show();
		return;
	}		

        SpannableString ss;

        ss = new SpannableString(s_normal);
        Drawable d = getResources().getDrawable(R.drawable.circle_normal);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap bitmap = bd.getBitmap();
        bitmap = AddBorderToIcon(bitmap);
        // VerticalImageSpan span = new VerticalImageSpan(bitmap,
        // ImageSpan.ALIGN_BASELINE );
        VerticalImageSpan span = new VerticalImageSpan(this, bitmap);
        ss.setSpan(span, 0, s_normal.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        Editable editable = mText.getEditableText();
        int selectionIndex = mText.getSelectionStart();
        ss.getSpans(0, ss.length(), ImageSpan.class);
        bCopy = true;
        editable.insert(offset, ss);
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private Bitmap resizetoscreen(Bitmap bitmap) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenwidth = dm.widthPixels;
        int padding0 = getResources().getDimensionPixelSize(R.dimen.g_note_text_padding_left);
        int padding1 = getResources().getDimensionPixelSize(R.dimen.g_note_padding_left);
        int border = getResources().getDimensionPixelSize(R.dimen.g_note_image_border);
        int shadow = getResources().getDimensionPixelSize(R.dimen.g_note_image_shadow);
        int contentwidth = screenwidth - padding0 - padding1 - 2 * border - 2 * shadow;

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

    private String saveResizedBitmap(Bitmap bm) {
        File filefolder = new File(getApplicationContext().getExternalFilesDir(null) + "/Images");
        if (!filefolder.exists()) {
            filefolder.mkdirs();
        }

        String path = getApplicationContext().getExternalFilesDir(null) + "/Images/";
        String name = "NoteImg" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File f = new File(path, name);
        path = path + name;
        if (f.exists()) {
            f.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
            out.close();
            Log.i(TAG, "saveResizedBitmap path " + path + " saved");
            path = path + s_imgsuff;
            return path;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap AddFrame(Bitmap bitmap) {
        int border = getResources().getDimensionPixelSize(R.dimen.g_note_image_border);
        int shadow = getResources().getDimensionPixelSize(R.dimen.g_note_image_shadow);

        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth() + border * 2 + shadow * 2,
                bitmap.getHeight() + border * 2 + shadow, Bitmap.Config.RGB_565);//Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        canvas.drawRect(new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()), paint);

        int scolor0 = getResources().getColor(R.color.shadow_color0);
        int scolor1 = getResources().getColor(R.color.shadow_color1); // 0x40FFFFFF&scolor;
        int scolor2 = getResources().getColor(R.color.shadow_color2);// 0x80FFFFFF&scolor;
        int scolor3 = getResources().getColor(R.color.shadow_color3);// 0xC0FFFFFF&scolor;
        Shader mLinearGradient1 = null;
        Shader mLinearGradient2 = null;
        Shader mLinearGradient3 = null;
        mLinearGradient1 = new LinearGradient(0, bitmap.getHeight() + border * 2, 0, bitmapbg.getHeight(),
                new int[]{ /* scolor, scolor3, scolor2, scolor1}, */ scolor0, scolor1, scolor2, scolor3}, null,
                Shader.TileMode.CLAMP);
        mLinearGradient2 = new LinearGradient(0, 0, shadow, 0,
                new int[]{ /* scolor1, scolor2, scolor3, scolor}, */scolor3, scolor2, scolor1, scolor0}, null,
                Shader.TileMode.CLAMP);
        mLinearGradient3 = new LinearGradient(bitmap.getWidth() + border * 2 + shadow, 0,
                bitmap.getWidth() + border * 2 + shadow * 2, 0,
                new int[]{ /* scolor, scolor3, scolor2, scolor1}, */scolor0, scolor1, scolor2, scolor3}, null,
                Shader.TileMode.CLAMP);
        Paint Spaint = new Paint();

        Spaint.setShader(mLinearGradient1);
        canvas.drawRect(0, bitmap.getHeight() + border * 2, bitmapbg.getWidth(), bitmapbg.getHeight(), Spaint);

        Spaint.setShader(mLinearGradient2);
        canvas.drawRect(0, 0, shadow, bitmap.getHeight() + border * 2, Spaint);

        Spaint.setShader(mLinearGradient3);
        canvas.drawRect(bitmap.getWidth() + border * 2 + shadow, 0, bitmapbg.getWidth(),
                bitmap.getHeight() + border * 2, Spaint);

        canvas.drawBitmap(bitmap, border + shadow, border, paint);

        return bitmapbg;
    }

    private Bitmap AddBorderToIcon(Bitmap bitmap) {
        int border = getResources().getDimensionPixelSize(R.dimen.g_note_icon_border);

        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth() + border, bitmap.getHeight(), Bitmap.Config.ARGB_4444);//Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        canvas.drawRect(new Rect(0, 0, bitmapbg.getWidth() + border, bitmapbg.getHeight()), paint);

        canvas.drawBitmap(bitmap, 0, 0, null);

        return bitmapbg;
    }

    class MaxLengthFilter implements InputFilter {
        private int mMaxLength;
        private Toast mMaxNoteToast;
        private int mType;
        private int mY = 0;

        public MaxLengthFilter(Context context, int max, int type) {
            mMaxLength = max - 1;
            mType = type;
            if (type == TYPE_MAX_LEN_TITLE) {
                mMaxNoteToast = Toast.makeText(context, R.string.toast_edit_title_max_length, Toast.LENGTH_SHORT);
                mMaxNoteToast.setGravity(Gravity.TOP, 0, 135);
            } else {
                mMaxNoteToast = Toast.makeText(context, R.string.editor_full, Toast.LENGTH_SHORT);
            }
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            invalidateOptionsMenu();
            int keep = mMaxLength - (dest.length() - (dend - dstart));
            if (keep < (end - start)) {
                if (mType == TYPE_MAX_LEN_CONTENT) {
                    mMaxNoteToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, mY - 100);
                }
                mMaxNoteToast.show();
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= (end - start)) {
                return null;
            } else {
                return source.subSequence(start, start + keep);
            }
        }

        public void setY(int y) {
            mY = y;
        }
    }

    public class VerticalImageSpan extends ImageSpan {
        public VerticalImageSpan(Bitmap arg0, int arg1) {
            super(arg0, arg1);
        }

        public VerticalImageSpan(Drawable arg0, int arg1) {
            super(arg0, arg1);
        }

        public VerticalImageSpan(Context arg0, Bitmap arg1) {
            super(arg0, arg1);
        }

        public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                FontMetricsInt fmPaint = paint.getFontMetricsInt();
                int fontHeight = fmPaint.bottom - fmPaint.top;
                int drHeight = rect.bottom - rect.top;

                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fm.ascent = -bottom;
                fm.top = -bottom;
                fm.bottom = top;
                fm.descent = top;
            }
            return rect.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                         Paint paint) {
            Drawable b = getDrawable();
            canvas.save();
            int transY = 0;
            transY = ((bottom - top) - b.getBounds().bottom) / 8 + top;
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

    public class BigImageSpan extends ImageSpan {
        public BigImageSpan(Context arg0, Bitmap arg1) {
            super(arg0, arg1);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                         Paint paint) {
            Drawable b = getDrawable();
            canvas.save();

            int transY = 0;
            transY = ((bottom - top) - b.getBounds().bottom) / 4 + top;
			/*
			 * int transY = bottom -b.getBounds().bottom; if (mVerticalAlignment
			 * == ALIGN_BASELINE) { transY -= paint.getFontMetricsInt().descent;
			 * }
			 */
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

}
