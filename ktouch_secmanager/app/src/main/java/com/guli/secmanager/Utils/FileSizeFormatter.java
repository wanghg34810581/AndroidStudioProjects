package com.guli.secmanager.Utils;

import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by yujie on 16-4-18.
 */
public class FileSizeFormatter {
    public static final String TAG = "FileSizeFormatter";
    public static final long ONE_KB = 1024L;
    public static final long ONE_MB = ONE_KB * 1024L;
    public static final long ONE_GB = ONE_MB * 1024L;
    public static final long ONE_TB = ONE_GB * 1024L;
    public static String transformShortType(long bytes) {
        long currenUnit = ONE_KB;
        int unitLevel = 0;
        boolean isNegative = false;
        if (bytes < 0) {
            isNegative = true;
            bytes = (-1) * bytes;
        }

        while ((bytes / currenUnit) > 0) {
            unitLevel++;
            currenUnit *= ONE_KB;
        }

        String result_text = null;
        double currenResult = 0;
//		int skipLevel = 1000;//如果大于等于1000就用更大一级单位显示
        switch (unitLevel) {
            case 0:
                result_text = "0K";
                break;
            case 1:
                currenResult = bytes / ONE_KB;
//			if (currenResult < skipLevel) {
                result_text = getFloatValue(currenResult, 1) + "K";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_MB) + "M";
//			}
                break;
            case 2:
                currenResult = bytes * 1.0 / ONE_MB;
//			if (currenResult < skipLevel) {
                result_text = getFloatValue(currenResult, 1) + "M";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_GB) + "G";
//			}
                break;
            case 3:
                currenResult = bytes * 1.0 / ONE_GB;
//			if (currenResult < skipLevel) {
                result_text = getFloatValue(currenResult, 2) + "G";
//			} else {
//				result_text = getFloatValue(bytes * 1.0 / ONE_TB) + "T";
//			}
                break;
            case 4:
                result_text = getFloatValue(bytes * 1.0 / ONE_TB, 2) + "T";
        }

        if (isNegative) {
            result_text = "-" + result_text;
        }
        return result_text;
    }

    private static String getFloatValue(double oldValue, int decimalCount){
        if (oldValue >= 1000) {//大于四位整数  不出现小数部分
            decimalCount = 0;
        }else if(oldValue >= 100){
            decimalCount = 1;
        }

        BigDecimal b = new BigDecimal(oldValue);
        try {
            if (decimalCount <= 0) {
                oldValue = b.setScale(0, BigDecimal.ROUND_DOWN).floatValue(); //ROUND_DOWN 表示舍弃末尾
            }else{
                oldValue = b.setScale(decimalCount, BigDecimal.ROUND_DOWN).floatValue(); //ROUND_DOWN 表示舍弃末尾,decimalCount 位小数保留
            }
        } catch (ArithmeticException e) {
            Log.w("Unit.getFloatValue", e.getMessage());
        }
        String decimalStr = "";
        if (decimalCount <= 0) {
            decimalStr = "#";
        } else {
            for (int i = 0; i < decimalCount; i++) {
                decimalStr += "#";
            }
        }
        // decimalCount 位小数保留
        DecimalFormat format = new DecimalFormat("###." + decimalStr);
        return  format.format(oldValue);
    }
}
