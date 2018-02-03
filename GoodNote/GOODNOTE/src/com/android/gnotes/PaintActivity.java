package com.android.gnotes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.util.Log;

public class PaintActivity extends Activity {
	private static final String TAG = "PaintActivity";

	private PaintView paintView;
	// private GridView paint_bottomMenu;

	// private Button btn_save;
	// private Button btn_back;

	private ImageButton pencil_btn;
	private ImageButton eraser_btn;
       private ImageButton undo_btn;
	private ImageButton redo_btn; 
	private ImageButton clear_btn;

	private ImageButton saveBtn;
	private View returnBtn;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar_color));
		}

		setContentView(R.layout.activity_paint);

		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		//actionBar.setTitle(R.string.paint);
		initActionbar();

		// paint_bottomMenu = (GridView)findViewById(R.id.paintBottomMenu);
		// paint_bottomMenu.setOnItemClickListener(new MenuClickEvent());

		paintView = (PaintView) findViewById(R.id.paint_layout);
		paintView.setContext(getApplicationContext());
		InitPaintMenu();

		// btn_save = (Button)findViewById(R.id.bt_save);
		// btn_back = (Button)findViewById(R.id.bt_back);
		// btn_save.setOnClickListener(new ClickEvent());
		// btn_back.setOnClickListener(new ClickEvent());

	}

    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);    
        actionBar.setDisplayShowHomeEnabled(false);  
        actionBar.setDisplayShowTitleEnabled(false);  
        actionBar.setDisplayShowCustomEnabled(true);  
        View paint_actionbar = LayoutInflater.from(this).inflate(  
                R.layout.paint_actionbar, null);  
        actionBar.setCustomView(paint_actionbar);
        
        returnBtn = findViewById(R.id.to_edit);
        saveBtn = (ImageButton) findViewById(R.id.save);

        returnBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int savePathSize = paintView.getSavePathSize();
                if (savePathSize > 0) {
                    savePaintDialog();
                } else {
                    PaintActivity.this.finish();
                }
            }
        });

        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                Bundle b = new Bundle();
                String path = paintView.saveBitmap();
                b.putString("paintPath", path);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                PaintActivity.this.finish();
            }
        });
    }


	private void InitPaintMenu() {
		paintView.selectPaintColor(2);// black
		paintView.selectPaintSize(0);// 5
		pencil_btn = (ImageButton) findViewById(R.id.pencil_button);
		eraser_btn = (ImageButton) findViewById(R.id.eraser_button);
		undo_btn = (ImageButton) findViewById(R.id.undo_button);
		redo_btn = (ImageButton) findViewById(R.id.redo_button);
		clear_btn = (ImageButton) findViewById(R.id.clear_button);
		pencil_btn.setSelected(true);
		pencil_btn.setImageResource(R.drawable.pencil_selected);

		pencil_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				paintView.selectPaintStyle(0);
				view.setSelected(true);
				pencil_btn.setImageResource(R.drawable.pencil_selected);
				eraser_btn.setSelected(false);
				eraser_btn.setImageResource(R.drawable.eraser);
			}
		});
		eraser_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				paintView.selectPaintStyle(1);
				view.setSelected(true);
				eraser_btn.setImageResource(R.drawable.eraser_selected);
				pencil_btn.setSelected(false);
				pencil_btn.setImageResource(R.drawable.pencil);
			}
		});
		undo_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				paintView.undo();
			}
		});
		redo_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				paintView.redo();
			}
		});
		clear_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TY zhencc 20160902 modify for PROD104179658 begin
				//clearpaint();
				if(paintView.getSavePathSize() > 0){					
					clearpaint();
				}
				// TY zhencc 20160902 modify for PROD104179658 end
			}
		});
	}

	private void clearpaint() {
		// TY zhencc 20160902 delete for PROD104179658 begin
		/*AlertDialog.Builder builder = new AlertDialog.Builder(PaintActivity.this, R.style.custom_dialog);
		builder.setTitle(R.string.clear_title);
		builder.setMessage(R.string.clear_confirm);
		builder.setPositiveButton(R.string.clear_confirm_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				paintView.removeAllPaint();
				dialog.cancel();
			}
		});

		builder.setNegativeButton(R.string.clear_confirm_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();*/
		// TY zhencc 20160902 delete for PROD104179658 end

		// TY zhencc 20160902 add for PROD104179658 begin
		CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
		customAlertDialog.setTitle(R.string.clear_title);
		customAlertDialog.setMessage(R.string.clear_confirm);
		customAlertDialog.setSureBtn(R.string.clear_confirm_ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				paintView.removeAllPaint();
			}
		});

		customAlertDialog.setCancelBtn(R.string.clear_confirm_cancel, null);
		customAlertDialog.showDialog();
		// TY zhencc 20160902 add for PROD104179658 end
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.paint_menu, menu);
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, PaintActivity.class), null,
				intent, 0, null);
		menu.findItem(R.id.paint_save).setVisible(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.paint_save).setEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Log.i(TAG, "onOptionsItemSelected");
		switch (item.getItemId()) {
		// TY zhencc 20160829 modify for PROD104174076 begin
		case R.id.paint_save:
			Intent intent = getIntent();
			Bundle b = new Bundle();
			String path = paintView.saveBitmap();
			b.putString("paintPath", path);
			intent.putExtras(b);
			setResult(RESULT_OK, intent);
			PaintActivity.this.finish();
			return true;
		case android.R.id.home:
			// TY zhencc 20160830 modify for PROD104179943 begin
			int savePathSize = paintView.getSavePathSize();
			if (savePathSize > 0) {
				savePaintDialog();
			} else {
				PaintActivity.this.finish();
			}
			return true;
		    // TY zhencc 20160830 modify for PROD104179943 end
		// TY zhencc 20160829 modify for PROD104174076 end
		default:
			return super.onOptionsItemSelected(item);
		}
	}*/

	// TY zhencc 20160830 modify for PROD104179943 begin
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		int savePathSize = paintView.getSavePathSize();
		if (savePathSize > 0) {
			savePaintDialog();
		} else {
			super.onBackPressed();
		}
	}

	private void savePaintDialog() {
		// TY zhencc 20160902 add for PROD104179658 begin
		CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
		customAlertDialog.hideTitleTv();
		customAlertDialog.setMessage(R.string.save_confirm);
		customAlertDialog.setSureBtn(R.string.save_confirm_ok, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intentH = getIntent();
				Bundle bundle = new Bundle();
				String pathH = paintView.saveBitmap();
				bundle.putString("paintPath", pathH);
				intentH.putExtras(bundle);
				setResult(RESULT_OK, intentH);
				PaintActivity.this.finish();
			}
		});

		customAlertDialog.setCancelBtn(R.string.save_confirm_cancel, new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PaintActivity.this.finish();
			}
		});
		customAlertDialog.showDialog();
		// TY zhencc 20160902 add for PROD104179658 end
	}
	// TY zhencc 20160830 modify for PROD104179943 end
}
