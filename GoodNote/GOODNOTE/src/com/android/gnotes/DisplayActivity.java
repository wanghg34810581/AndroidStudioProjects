package com.android.gnotes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import android.util.Log;
import android.media.MediaScannerConnection;

public class DisplayActivity extends Activity {
    private static final String TAG = "DisplayActivity";
    private Matrix savedMatrix = new Matrix();
    private String imgPath;
    private ImageButton deleteBtn;
    private ImageButton downloadBtn;
    private View returnBtn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.statusbar_color));
        }
        setContentView(R.layout.activity_display);

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);    
        //actionBar.setTitle(R.string.show_picture);
        initActionbar();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        ImageView img = (ImageView) findViewById(R.id.display_image);
		
        Intent intent = this.getIntent();
        imgPath = intent.getStringExtra("imgPath");
        Bitmap bm = BitmapFactory.decodeFile(imgPath);

        int statusBarHeight = getStatusHeight(this);  
        int viewHeight = dm.heightPixels - getResources().getDimensionPixelSize(R.dimen.action_bar_height)
			- statusBarHeight;

        if(bm != null){ //TY zhencc 20161010 add if judgement for monkey test NullPointerException
            int viewY = (viewHeight - bm.getHeight()) /2;
            savedMatrix.setTranslate((dm.widthPixels - bm.getWidth())/2 , 0);
            if (viewHeight > bm.getHeight()) {
                img.setPadding(0, viewY, 0, 0);
            }
        }
        img.setImageMatrix(savedMatrix);
        img.setImageBitmap(bm);
        //img.setScaleType(ScaleType.MATRIX);
        //img.setOnTouchListener(new TouchEvent());
    }
	

    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);  
        actionBar.setHomeButtonEnabled(false);    
        actionBar.setDisplayShowHomeEnabled(false);  
        actionBar.setDisplayShowTitleEnabled(false);  
        actionBar.setDisplayShowCustomEnabled(true);  
        View display_actionbar = LayoutInflater.from(this).inflate(  
                R.layout.display_actionbar, null);  
        actionBar.setCustomView(display_actionbar);
        
        returnBtn = findViewById(R.id.display_to_edit);
        deleteBtn = (ImageButton) findViewById(R.id.delete_btn);
        downloadBtn = (ImageButton) findViewById(R.id.download_btn);

        returnBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayActivity.this.finish();
            }
        });

        deleteBtn.setOnClickListener(new OnClickListener() {
    @Override
            public void onClick(View view) {
                File file = new File(imgPath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }
                Intent intent = getIntent();
                Bundle b = new Bundle();
                b.putString("deletepath", imgPath);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                DisplayActivity.this.finish();	
            }
        });

        downloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage(imgPath);
            }
        });
    }

	
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_menu, menu);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
            new ComponentName(this, DisplayActivity.class), null, intent, 0, null);
        menu.findItem(R.id.display_download).setVisible(true);
        menu.findItem(R.id.display_delete).setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.display_download).setEnabled(true);
        menu.findItem(R.id.display_delete).setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Log.i(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
        case R.id.display_download:
            downloadImage(imgPath);
	 case android.R.id.home:
            DisplayActivity.this.finish();
            return true;
        case R.id.display_delete:
            File file = new File(imgPath);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
            Intent intent = getIntent();
            Bundle b = new Bundle();
            b.putString("deletepath", imgPath);
            intent.putExtras(b);
            setResult(RESULT_OK, intent);
            DisplayActivity.this.finish();		
        default:
            return super.onOptionsItemSelected(item);
        }
    }*/

    private void downloadImage(String oldPath) {
        String name = getFileName(oldPath);
        String path = Environment.getExternalStorageDirectory() + "/Download/";
        String newPath = path + name;

        File mfile = new File(path);
        if (!mfile.exists()) {
            mfile.mkdir();
        }
		
        Log.i(TAG, "downloadFile newPath is " + newPath + " oldfile is " + oldPath);		
        try {   
            int bytesum = 0;   
            int byteread = 0;   
            File oldfile = new File(oldPath);   
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);   
                byte[] buffer = new byte[1024];   
                int length;   
                while ( (byteread = inStream.read(buffer)) != -1) {   
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);   
                }   
                inStream.close();
	         fs.close();
                scan(path, newPath);
		  Toast.makeText(this, getResources().getString(R.string.download_image_to) + newPath, Toast.LENGTH_LONG).show();
                //finish();
            }
        }   
        catch (Exception e) {   
            Log.i(TAG, "catch exception for download image: " + newPath);
            e.printStackTrace();   
        }
    } 

    private String getFileName(String pathandname) {
          
        int start=pathandname.lastIndexOf("/");  
        int end=pathandname.length();  
        if(start!=-1 && end!=-1){  
            return pathandname.substring(start+1, end);    
        }else{  
            return null;  
        }  
    }

    private void scan(String path, String newpath) {
        if(hasKitkat()) {
            MediaScannerConnection.scanFile(this,
                new String[] {path}, new String[]{ "image/*" },
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        getApplicationContext().sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, uri));
                        getApplicationContext().sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
                    }
            });
            scanPhotos(newpath, this);
        }else{
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
        }

    }

    private static void scanPhotos(String filePath, Context context) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(filePath));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    private static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
	
    
    private static int getStatusHeight(Activity activity){
        int statusHeight = 0;
        Rect localRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = activity.getResources().getDimensionPixelSize(i5);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }
	
}
