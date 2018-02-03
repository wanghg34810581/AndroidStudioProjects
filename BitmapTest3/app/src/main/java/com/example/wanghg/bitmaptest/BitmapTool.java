package com.example.wanghg.bitmaptest;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by wanghg on 2017/2/16.
 */

public class BitmapTool {
    public static Bitmap decodeBitmap(Resources resource, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resource, resId, options);
        options.inSampleSize = calculateInSimpleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(resource, resId, options);
    }

    public static int calculateInSimpleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        int simpleSize = 1;

        if (imageWidth > reqWidth || imageHeight > reqHeight) {
            final int widthRatio = Math.round((float) imageWidth / (float) reqWidth);
            final int heightRatio = Math.round((float) imageHeight / (float) reqHeight);
            simpleSize = widthRatio < heightRatio ? widthRatio : heightRatio;
        }

        return simpleSize;
    }
}
