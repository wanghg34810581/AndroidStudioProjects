package com.android.gnotes;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.format.Time;

class NotesUtils {

	public static final int ID_NOTE_LINEAR = 1;
	public static final int ID_NOTE_STAGGERED_GRID = 2;

	public static final String NOTE_ID = "noteId";

	public static final int HEAD_VIEW = 0;
	public static final int BODY_VIEW = 1;
	
	// TY zhencc 20160825 add for PROD104174124 begin
	public static final String GOODNOTE = "goodnote";
	public static final String ITEM_SELECTED_INDEX = "item_selected_index";
	public static final int default_item_index = 0;
	// TY zhencc 20160825 add for PROD104174124 end

	public static String formatTimeString(Context context, long when) {
		Time then = new Time();
		then.set(when);
		Time now = new Time();
		now.setToNow();

		String formatStr;
		//TY zhencc 20160918 modify for PROD104182067 begin
		/*if ((then.yearDay != now.yearDay) || (then.year != now.year && then.yearDay == now.yearDay)) {
			formatStr = "yyyy/MM/dd";
		} else {
			formatStr = "HH:mm";
		}*/
		if (then.year != now.year){
			formatStr = "yyyy/MM/dd";
		} else if (then.yearDay != now.yearDay){
			formatStr = "MM/dd";
		} else {
			formatStr = "HH:mm";
		}
		//TY zhencc 20160918 modify for PROD104182067 end

		if ((then.year == now.year) && ((now.yearDay - then.yearDay) == 1)) {
			return context.getString(R.string.date_yesterday);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
			return sdf.format(when);
		}
	}

	public static int computeSampleSize(Context context, BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(context, options, minSideLength, maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(Context context, BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int maxH = (int) context.getResources().getDimension(R.dimen.image_max_height);
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? maxH
				: (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}


	public static int px2dip(Context context, float pxValue) {  
		final float scale = context.getResources().getDisplayMetrics().density;  
		return (int) (pxValue / scale + 0.5f);  
	}  


}
