package com.ktouch.kcalendar.vacation;

/**
 * Created by zhuys on 2016/6/22 0022.
 */
public class Vacation {
    public int mYear;
    public int mMonth;
    public int mDay;
    public long mDate;
    public int mState; // 0,normal; 1,vacation; 2,work; 3,custom;

    public Vacation(int year, int month, int day, long date, int state){
        mYear = year;
        mMonth = month;
        mDay = day;
        mDate = date;
        mState = state;
    }

}
