package com.guli.secmanager.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.guli.secmanager.R;

/**
 * Created by yujie on 16-4-10.
 */
public class UnitConverter {
    public static final String TAG = "UnitConverter";

    public static int dp2px(View v, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, v.getResources().getDisplayMetrics());
    }

    public static int sp2px(View v, int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, v.getResources().getDisplayMetrics());
    }

    public static void updateButtonBg2(Context context, View view) {
        //Log.v("flow", "updateButtonBg view = " + view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            view.setBackgroundResource(outValue.resourceId);
        } else {
            view.setBackgroundResource(R.drawable.complete_btn_on_selector);
        }
    }
}
