package com.android.gnotes;

import android.app.Activity;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
// TY zhencc 20160825 add for PROD104174124 begin
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
// TY zhencc 20160825 add for PROD104174124 end
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

public class PreviewActivity extends Activity implements OnPageChangeListener {

	private ViewPager viewPager;

	private ImageView[] mImageViews;

	// TY zhencc 20160825 add for PROD104174124 begin
	private int item_index = NotesUtils.default_item_index;
	private int item_selected_index;
	private Button apply_btn;
	private SharedPreferences mSharedPreferences;
	private Uri mUri;
	// TY zhencc 20160825 add for PROD104174124 end

	private View returnBtn;
	//private TextView previewtitle;
	
	private static final int SKIN_COUNT = 6;

	private TextView skin_name;
	private static final int SkinName[] = { R.string.skin_name0, R.string.skin_name1, R.string.skin_name2,
			R.string.skin_name3, R.string.skin_name4, R.string.skin_name5 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar_color));
		}
		setContentView(R.layout.activity_preview);

		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setHomeButtonEnabled(true);    		
		//actionBar.setTitle(R.string.template);
		initActionbar();

		skin_name = (TextView) findViewById(R.id.skin_name);

		LinearLayout container = (LinearLayout) findViewById(R.id.container);
		viewPager = (ViewPager) findViewById(R.id.previewpager);

		int[] imgIdArray = new int[] { R.drawable.item00, R.drawable.item01, R.drawable.item02, R.drawable.item03,
				R.drawable.item04, R.drawable.item05 };

		mImageViews = new ImageView[imgIdArray.length];
		for (int i = 0; i < mImageViews.length; i++) {
			ImageView imageView = new ImageView(this);
			mImageViews[i] = imageView;
			imageView.setBackgroundResource(imgIdArray[i]);
		}

		viewPager.setAdapter(new MyAdapter());
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(3);
		viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.g_note_pager_margin));

		container.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return viewPager.dispatchTouchEvent(event);
			}
		});

		// TY zhencc 20160825 add for PROD104174124 begin
		mUri = getIntent().getData();
		// TY zhencc 20160825 add for PROD104174124 end
		apply_btn = (Button) findViewById(R.id.apply_button);
		apply_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TY zhencc 20160825 add for PROD104174124 begin
				item_selected_index = item_index;
				if (mUri == null) {
					SharedPreferences.Editor editor = mSharedPreferences.edit();
					editor.putInt(NotesUtils.ITEM_SELECTED_INDEX, item_selected_index);
					editor.commit();
				} else {
					ContentValues values = new ContentValues();
					values.put(Notes.COLUMN_NAME_SKIN_INDEX, item_selected_index);
					getContentResolver().update(mUri, values, null, null);
				}
				// TY zhencc 20160825 add for PROD104174124 end

				Intent intent = getIntent();
				Bundle b = new Bundle();
				b.putInt("SkinIndex", item_index);
				intent.putExtras(b);
				setResult(RESULT_OK, intent);
				PreviewActivity.this.finish();
			}
		});

		// TY zhencc 20160825 add for PROD104174124 begin
		if (mUri == null) {
			mSharedPreferences = getSharedPreferences(NotesUtils.GOODNOTE, Activity.MODE_PRIVATE);
			item_selected_index = mSharedPreferences.getInt(NotesUtils.ITEM_SELECTED_INDEX, item_index);
		} else {
			Cursor cursor = managedQuery(mUri, Notes.PROJECTION, null, null, null);
			cursor.moveToFirst();
			item_selected_index = cursor.getInt(cursor.getColumnIndex(Notes.COLUMN_NAME_SKIN_INDEX));
		}

		if (item_selected_index == item_index) {
			apply_btn.setText(R.string.applyed);
			apply_btn.setClickable(false);
		} else {
			apply_btn.setText(R.string.apply_now);
			apply_btn.setClickable(true);
		}
		// TY zhencc 20160825 add for PROD104174124 end
	}


    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);    
        actionBar.setDisplayShowHomeEnabled(false);  
        actionBar.setDisplayShowTitleEnabled(false);  
        actionBar.setDisplayShowCustomEnabled(true);
		View preview_actionbar = LayoutInflater.from(this).inflate(
				R.layout.preview_actionbar, new LinearLayout(this), false);
        actionBar.setCustomView(preview_actionbar);
        
        returnBtn = findViewById(R.id.preview_to_edit);
			
        returnBtn.setOnClickListener(new OnClickListener() {
	@Override
            public void onClick(View view) {
                Intent intent = getIntent();
                setResult(RESULT_CANCELED, intent);
                PreviewActivity.this.finish();
            }
        });

    }


	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Log.i(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = getIntent();
			//Bundle b = new Bundle();
			//b.putInt("SkinIndex", item_index);
			//intent.putExtras(b);
			setResult(RESULT_CANCELED, intent);
			PreviewActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/

	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return SKIN_COUNT;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(mImageViews[position]);

		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(mImageViews[position]);
			return mImageViews[position];
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		item_index = arg0;
		skin_name.setText(SkinName[arg0]);

		// TY zhencc 20160825 add for PROD104174124 begin
		if (item_selected_index == item_index) {
			apply_btn.setText(R.string.applyed);
			apply_btn.setClickable(false);
		} else {
			apply_btn.setText(R.string.apply_now);
			apply_btn.setClickable(true);
		}
		// TY zhencc 20160825 add for PROD104174124 end
	}

}
