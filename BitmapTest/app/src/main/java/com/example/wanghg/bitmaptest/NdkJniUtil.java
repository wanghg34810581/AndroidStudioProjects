package com.example.wanghg.bitmaptest;

/**
 * Created by wanghg on 2017/3/6.
 */

public class NdkJniUtil {
    static {
        System.loadLibrary("MyTestJniLib");
    }

    public native String getCLanguageString();
    public native int calAAddB(int a, int b);
    public native int getRandom(int i);
}
