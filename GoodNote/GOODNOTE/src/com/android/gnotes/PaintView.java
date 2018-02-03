package com.android.gnotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Rect;
import android.util.Log;

public class PaintView extends View  {
    private static final String TAG = "PaintView";	
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Bitmap  mBitmap;
    private Paint mPaint;
    private Context mAppContext;		
    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    private DrawPath dp;
    	
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
        
    private int bitmapWidth;
    private int bitmapHeight;
    private boolean isMoving = false;
        
    private int[] paintColor = {
        Color.RED,
        Color.BLUE,
        Color.BLACK,
        Color.GREEN,
        Color.YELLOW,
        Color.CYAN,
        Color.LTGRAY
    };
        
    private int currentColor = Color.BLACK;
    private int currentSize = 5;
    private int currentStyle = 1;
        
    public PaintView(Context c) {
        super(c);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        int statusBarHeight = getStatusHeight((Activity) c);  
        Log.i(TAG, "statusBarHeight="+ statusBarHeight);
			
        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - getResources().getDimensionPixelSize(R.dimen.action_bar_height)
			- getResources().getDimensionPixelSize(R.dimen.g_note_bottom_menu_height) - statusBarHeight;
        Log.i(TAG, "dm.heightPixels=" + dm.heightPixels + " bottom height="  + getResources().getDimensionPixelSize(R.dimen.g_note_bottom_menu_height));        
        initCanvas();
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }
	
    public PaintView(Context c, AttributeSet attrs) {
        super(c,attrs);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) c).getWindowManager().getDefaultDisplay().getMetrics(dm);

        int statusBarHeight = getStatusHeight((Activity) c);  
        Log.i(TAG, "statusBarHeight="+ statusBarHeight);
			
        bitmapWidth = dm.widthPixels;
        bitmapHeight = dm.heightPixels - getResources().getDimensionPixelSize(R.dimen.action_bar_height)
			- getResources().getDimensionPixelSize(R.dimen.g_note_bottom_menu_height) - statusBarHeight;
	 Log.i(TAG, "dm.heightPixels=" + dm.heightPixels + " bottom height="  + getResources().getDimensionPixelSize(R.dimen.g_note_bottom_menu_height)); 	
        initCanvas();
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    public void setContext(Context c) {
	mAppContext = c;
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


    private void initCanvas() {
        Log.i(TAG, "bitmapHeight="+ bitmapHeight);

        setPaintStyle();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            
        mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, 
        Bitmap.Config.ARGB_8888);//RGB_565);
        mCanvas = new Canvas(mBitmap);
            
        mCanvas.drawColor(getResources().getColor(R.color.paint_bg_color));//(Color.WHITE);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
   }
	
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(currentSize);
        if(currentStyle == 1)
            mPaint.setColor(currentColor);
        else{
            mPaint.setColor(getResources().getColor(R.color.paint_bg_color));
        }
    }
        
    @Override
    protected void onDraw(Canvas canvas) {   
        	
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if(mPath != null){
            canvas.drawPath(mPath, mPaint);
            /*if(this.isMoving && currentColor != Color.WHITE) {
                Bitmap pen = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.pen);
                canvas.drawBitmap(pen, this.mX, this.mY - pen.getHeight(),
                    new Paint(Paint.DITHER_FLAG));
            }	*/
        }
    }

    class DrawPath{
        Path path;
        Paint paint;
    }

    public void selectPaintStyle(int which) {
        	
        if(which == 0){
            currentStyle = 1;
            setPaintStyle();
        }
		
        if(which == 1){
            currentStyle = 2;
            setPaintStyle();
            mPaint.setStrokeWidth(20);
        }
    }
	
    public void selectPaintSize(int which) {

        currentSize = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        setPaintStyle();
    }

    public void selectPaintColor(int which) {
        	
        currentColor = paintColor[which];
        setPaintStyle();
    }
	
    public void undo() {
        	
        //System.out.println(savePath.size()+"--------------");
        if(savePath != null && savePath.size() > 0){
        initCanvas();
        		
        DrawPath drawPath = savePath.get(savePath.size() - 1);
        deletePath.add(drawPath);
        savePath.remove(savePath.size() - 1);
            	
        Iterator<DrawPath> iter = savePath.iterator();	
            while (iter.hasNext()) {
                DrawPath dp = iter.next();
                mCanvas.drawPath(dp.path, dp.paint);	
            }
            invalidate();
            }
    }

    public void redo() {
        if(deletePath.size() > 0){
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            mCanvas.drawPath(dp.path, dp.paint);
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    public void removeAllPaint() {
    		initCanvas();
    		invalidate();
    		savePath.clear();
    		deletePath.clear();
    }
        
    public String saveBitmap() {
        SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmmss");  
        Date   curDate   =   new   Date(System.currentTimeMillis());
        String   str   =   formatter.format(curDate);  
        String paintPath = "";
        str = str + "paint.png";

        File filefolder = new File(mAppContext.getExternalFilesDir(null) + "/Images");
        if(!filefolder.exists()){
            filefolder.mkdirs();
        }
		
        //File dir = new File("/sdcard/notes/");
        File file = new File(mAppContext.getExternalFilesDir(null) +"/Images/",str);
        //if (!dir.exists()) { 
            //dir.mkdir(); 
        //} else {
            if(file.exists()){
                file.delete();
            }
        //}
            
        try {
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); 
            out.flush(); 
            out.close(); 
            paintPath = mAppContext.getExternalFilesDir(null) +"/Images/" + str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
            
        return paintPath;
    }
        
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        this.isMoving = false;
    }
	
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            this.isMoving = true;
        }
            
        mX = x;
        mY = y;
        this.isMoving = true;    
    }
	
    private void touch_up(float x,float y) {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        	
        savePath.add(dp);
        mPath = null;
        this.isMoving = false;       
    }
        
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                    
                touch_start(x, y);
                invalidate();
                break;
        case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
        case MotionEvent.ACTION_UP:
                touch_up(x,y);
                invalidate();
                break;
        }
        return true;
    }
    
	// TY zhencc 20160830 modify for PROD104179943 begin
    public int getSavePathSize(){
    	return savePath.size();
    }
	// TY zhencc 20160830 modify for PROD104179943 end
  
}

