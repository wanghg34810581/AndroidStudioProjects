package com.android.gnotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

public class NotesApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LeakCanary.install(this);

		Cursor cursor = getContentResolver().query(Notes.CONTENT_URI, Notes.PROJECTION, null, null, Notes.sSortOrder);
		if (cursor == null || !cursor.moveToFirst()) {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.introduction);
			String path = saveResizedBitmap(bitmap);
			String nameNote = getResources().getString(R.string.note_introduction) + "\n" + path;

			ContentValues values = new ContentValues();
			values.put(Notes.COLUMN_NAME_NOTE, nameNote);
			values.put(Notes.COLUMN_NAME_GROUP, "");
			values.put(Notes.COLUMN_NAME_SEARCH_NOTE, getResources().getString(R.string.note_introduction));
			values.put(Notes.COLUMN_NAME_SKIN_INDEX, 0);
			getContentResolver().insert(Notes.CONTENT_URI, values);
		}
		if(cursor != null){
			cursor.close();
		}

		clearCacheImages();
	}

	private String saveResizedBitmap(Bitmap bm) {
		File filefolder = new File(getExternalFilesDir(null) + "/Images");
		if (!filefolder.exists()) {
			filefolder.mkdirs();
		}

		String path = getExternalFilesDir(null) + "/Images/";
		String name = "NoteImg" + String.valueOf(System.currentTimeMillis()) + ".jpg";
		File f = new File(path, name);
		path = path + name + "0IMG0";
		if (f.exists()) {
			f.delete();
		}

		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 60, out);
			out.flush();
			out.close();
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

	private void clearCacheImages() {
		Log.i("NotesApplication", "clearCacheImages");
		File filefolder = new File(getExternalCacheDir() + "/Images");
        	if (filefolder != null && filefolder.exists() && filefolder.isDirectory()) {
            		for (File item : filefolder.listFiles()) {
                		item.delete();
			}
        	}
	}

}
