/*
 *
 * Copyright (c) 2013 Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *    * Neither the name of The Linux Foundation nor the names of its
 *      contributors may be used to endorse or promote products derived
 *      from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ktouch.lunar;

import java.lang.ref.WeakReference;
//TY: hanjq 20140829 add for PROD103062599 begin
import java.lang.ref.SoftReference;
import android.util.Log;
//TY: hanjq 20140829 add for PROD103062599 end
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.ktouch.kcalendar.R;


public class LunarService extends Service {
    private final String SEPARATION = ",";
    private final String TYPE_IS_SPECIAL = "special";
    private final String TYPE_IS_COMMON = "common";

    private String[] mLunarCalendarNumber = null;
    private String[] mLunarCalendarTen = null;
    private String[] mYear_of_birth = null;
    private String[] mLunarTerm = null;

    private String mLunarLeapTag = null;
    private String mLunarMonthTag = null;
    private String mZhengyueTag = null;

    public int lunarYear = 0;
    public int lunarMonth = 0;
    public int lunarDay = 0;
    public int solarYear = 0;
    public int solarMonth = 0;
    public int solarDay = 0;

    public boolean isLeapMonth = false;
    private final static double D_solar_terms = 0.2422;
    private final static double[] lunar_20th_century_C = new double[] { 6.11,
            20.84, 4.6295, 19.4599, 6.3826, 21.4155, 5.59, 20.888, 6.318,
            21.86, 6.5, 22.20, 7.928, 23.65, 8.38, 23.95, 8.44, 23.822, 9.098,
            24.218, 8.218, 23.08, 7.9, 22.60 };
    private final static double[] lunar_21th_century_C = new double[] { 5.4055,
            20.12, 3.87, 18.73, 5.63, 20.646, 4.81, 20.1, 5.52, 21.04, 5.678,
            21.37, 7.108, 22.83, 7.5, 23.13, 7.646, 23.042, 8.318, 23.438,
            7.438, 22.36, 7.18, 21.94 };

    public LunarService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        if (mLunarCalendarNumber == null) {
            mLunarCalendarNumber = new String[12];
            mLunarCalendarNumber[0] = getLunarString(R.string.chineseNumber1);
            mLunarCalendarNumber[1] = getLunarString(R.string.chineseNumber2);
            mLunarCalendarNumber[2] = getLunarString(R.string.chineseNumber3);
            mLunarCalendarNumber[3] = getLunarString(R.string.chineseNumber4);
            mLunarCalendarNumber[4] = getLunarString(R.string.chineseNumber5);
            mLunarCalendarNumber[5] = getLunarString(R.string.chineseNumber6);
            mLunarCalendarNumber[6] = getLunarString(R.string.chineseNumber7);
            mLunarCalendarNumber[7] = getLunarString(R.string.chineseNumber8);
            mLunarCalendarNumber[8] = getLunarString(R.string.chineseNumber9);
            mLunarCalendarNumber[9] = getLunarString(R.string.chineseNumber10);
            mLunarCalendarNumber[10] = getLunarString(R.string.chineseNumber11);
            mLunarCalendarNumber[11] = getLunarString(R.string.chineseNumber12);
        }

        if (mLunarCalendarTen == null) {
            mLunarCalendarTen = new String[5];
            mLunarCalendarTen[0] = getLunarString(R.string.chineseTen0);
            mLunarCalendarTen[1] = getLunarString(R.string.chineseTen1);
            mLunarCalendarTen[2] = getLunarString(R.string.chineseTen2);
            mLunarCalendarTen[3] = getLunarString(R.string.chineseTen3);
            mLunarCalendarTen[4] = getLunarString(R.string.chineseTen4);
        }

        if (mYear_of_birth == null) {
            mYear_of_birth = new String[12];
            mYear_of_birth[0] = getLunarString(R.string.animals0);
            mYear_of_birth[1] = getLunarString(R.string.animals1);
            mYear_of_birth[2] = getLunarString(R.string.animals2);
            mYear_of_birth[3] = getLunarString(R.string.animals3);
            mYear_of_birth[4] = getLunarString(R.string.animals4);
            mYear_of_birth[5] = getLunarString(R.string.animals5);
            mYear_of_birth[6] = getLunarString(R.string.animals6);
            mYear_of_birth[7] = getLunarString(R.string.animals7);
            mYear_of_birth[8] = getLunarString(R.string.animals8);
            mYear_of_birth[9] = getLunarString(R.string.animals9);
            mYear_of_birth[10] = getLunarString(R.string.animals10);
            mYear_of_birth[11] = getLunarString(R.string.animals11);
        }

        if (mLunarLeapTag == null)
            mLunarLeapTag = getLunarString(R.string.leap_month);
        if (mLunarMonthTag == null) mLunarMonthTag = getLunarString(R.string.month);
        if (mZhengyueTag == null) mZhengyueTag = getLunarString(R.string.zheng);

        if (mLunarTerm == null) {
            mLunarTerm = new String[24];
			//mod liutao 2016-03-07
            mLunarTerm[0] = getLunarString(R.string.terms00);
            mLunarTerm[1] = getLunarString(R.string.terms01);
            mLunarTerm[2] = getLunarString(R.string.terms10);//2);
            mLunarTerm[3] = getLunarString(R.string.terms11);//3);
            mLunarTerm[4] = getLunarString(R.string.terms20);//4);
            mLunarTerm[5] = getLunarString(R.string.terms21);//5);
            mLunarTerm[6] = getLunarString(R.string.terms30);//6);
            mLunarTerm[7] = getLunarString(R.string.terms31);//7);
            mLunarTerm[8] = getLunarString(R.string.terms40);//8);
            mLunarTerm[9] = getLunarString(R.string.terms41);//9);
            mLunarTerm[10] = getLunarString(R.string.terms50);//10);
            mLunarTerm[11] = getLunarString(R.string.terms51);//11);
            mLunarTerm[12] = getLunarString(R.string.terms60);//12);
            mLunarTerm[13] = getLunarString(R.string.terms61);//13);
            mLunarTerm[14] = getLunarString(R.string.terms70);//14);
            mLunarTerm[15] = getLunarString(R.string.terms71);//15);
            mLunarTerm[16] = getLunarString(R.string.terms80);//16);
            mLunarTerm[17] = getLunarString(R.string.terms81);//17);
            mLunarTerm[18] = getLunarString(R.string.terms90);//18);
            mLunarTerm[19] = getLunarString(R.string.terms91);//19);
            mLunarTerm[20] = getLunarString(R.string.terms100);//20);
            mLunarTerm[21] = getLunarString(R.string.terms101);//);
            mLunarTerm[22] = getLunarString(R.string.terms110);//22);
            mLunarTerm[23] = getLunarString(R.string.terms111);
			// mod end
        }
    }

    private String getLunarString(int id) {
        return getResources().getString(id);
    }

    public String getTraditionalFestival() {
        return getTraditionalFestival(lunarYear, lunarMonth, lunarDay);
    }

    public String getTraditionalFestival(int lunarYear, int lunarMonth,
            int lunarDay) {
        if (isLeapMonth) return "";
        if (lunarMonth == 1 && lunarDay == 1)
            return getLunarString(R.string.chunjie);
        if (lunarMonth == 1 && lunarDay == 15)
            return getLunarString(R.string.yuanxiao);
        if (lunarMonth == 5 && lunarDay == 5)
            return getLunarString(R.string.duanwu);
        if (lunarMonth == 7 && lunarDay == 7)
            return getLunarString(R.string.qixi);
        if (lunarMonth == 8 && lunarDay == 15)
            return getLunarString(R.string.zhongqiu);
        if (lunarMonth == 9 && lunarDay == 9)
            return getLunarString(R.string.chongyang);
        if (lunarMonth == 12 && lunarDay == 8)
            return getLunarString(R.string.laba);
        if (lunarMonth == 12 && lunarDay == 23)
            return getLunarString(R.string.xiaonian);

        if (lunarMonth == 12) {
            if (lunarDay == LunarCalendarConvertUtil.getLunarMonthDays(
                    lunarYear, lunarMonth)) return getLunarString(R.string.chuxi);
        }
        return "";
    }

    public String getFestival() {
        return getFestival(solarYear, solarMonth, solarDay);
    }

    public String getFestival(int year, int month, int day) {
        if (month == 0 && day == 1)
            return getLunarString(R.string.new_Year_day);
        if (month == 1 && day == 14)
            return getLunarString(R.string.valentin_day);
        if (month == 2 && day == 8)
            return getLunarString(R.string.women_day);
        if (year > 1978 && month == 2 && day == 12)
            return getLunarString(R.string.arbor_day);
        if (month == 4 && day == 1)
            return getLunarString(R.string.labol_day);
        if (month == 4 && day == 4)
            return getLunarString(R.string.youth_day);
        if (month == 5 && day == 1)
            return getLunarString(R.string.children_day);
        if (month == 7 && day == 1)
            return getLunarString(R.string.army_day);
        if (year > 1984 && month == 8 && day == 10)
            return getLunarString(R.string.teacher_day);
        if (month == 9 && day == 1)
            return getLunarString(R.string.national_day);
        if (month == 11 && day == 25)
            return getLunarString(R.string.christmas_day);
        return "";
    }

    public String getLunarSolarTerms(int year) {
        int yy = 0;
        double[] lunar_century_C;
        if (year > 1999) {
            yy = year - 2000;
            lunar_century_C = lunar_21th_century_C;

        } else {
            yy = year - 1900;
            lunar_century_C = lunar_20th_century_C;
        }
        for (int i = 0; i < lunar_century_C.length; i++) {
            int day = 0;
            if (i < 4) {
                day = (int) Math
                        .floor((yy * D_solar_terms + lunar_century_C[i])
                                - ((yy - 1) / 4));
            } else {
                day = (int) Math
                        .floor((yy * D_solar_terms + lunar_century_C[i])
                                - (yy / 4));
            }
			// mod liutao 2016-03-07
            if (solarMonth == 0 && solarDay == day && i == 0) {
                return getLunarString(R.string.terms00);
            }
            if (solarMonth == 0 && solarDay == day && i == 1) {
                return getLunarString(R.string.terms01);
            }
            if (solarMonth == 1 && solarDay == day && i == 2) {
                return getLunarString(R.string.terms10);//2);
            }
            if (solarMonth == 1 && solarDay == day && i == 3) {
                return getLunarString(R.string.terms11);//3);
            }
            if (solarMonth == 2 && solarDay == day && i == 4) {
                return getLunarString(R.string.terms20);//4);
            }
            if (solarMonth == 2 && solarDay == day && i == 5) {
                return getLunarString(R.string.terms21);//5);
            }
            if (solarMonth == 3 && solarDay == day && i == 6) {
                return getLunarString(R.string.terms30)//6)
                        + SEPARATION
                        + getLunarString(R.string.qingming);
            }
            if (solarMonth == 3 && solarDay == day && i == 7) {
                return getLunarString(R.string.terms31);//7);
            }
            if (solarMonth == 4 && solarDay == day && i == 8) {
                return getLunarString(R.string.terms40);//8);
            }
            if (solarMonth == 4 && solarDay == day && i == 9) {
                return getLunarString(R.string.terms41);//9);
            }
            if (solarMonth == 5 && solarDay == day && i == 10) {
                return getLunarString(R.string.terms50);//10);
            }
            if (solarMonth == 5 && solarDay == day && i == 11) {
                return getLunarString(R.string.terms51);//11);
            }
            if (solarMonth == 6 && solarDay == day && i == 12) {
                return getLunarString(R.string.terms60);//12);
            }
            if (solarMonth == 6 && solarDay == day && i == 13) {
                return getLunarString(R.string.terms61);//13);
            }
            if (solarMonth == 7 && solarDay == day && i == 14) {
                return getLunarString(R.string.terms70);//14);
            }
            if (solarMonth == 7 && solarDay == day && i == 15) {
                return getLunarString(R.string.terms71);//15);
            }
            if (solarMonth == 8 && solarDay == day && i == 16) {
                return getLunarString(R.string.terms80);//16);
            }
            if (solarMonth == 8 && solarDay == day && i == 17) {
                return getLunarString(R.string.terms81);//17);
            }
            if (solarMonth == 9 && solarDay == day && i == 18) {
                return getLunarString(R.string.terms90);//18);
            }
            if (solarMonth == 9 && solarDay == day && i == 19) {
                return getLunarString(R.string.terms91);//19);
            }
            if (solarMonth == 10 && solarDay == day && i == 20) {
                return getLunarString(R.string.terms100);//20);
            }
            if (solarMonth == 10 && solarDay == day && i == 21) {
                return getLunarString(R.string.terms101);//21);
            }
            if (solarMonth == 11 && solarDay == day && i == 22) {
                return getLunarString(R.string.terms110);//22);
            }
            if (solarMonth == 11 && solarDay == day && i == 23) {
                return getLunarString(R.string.terms111);//23);
            }
			// mod end
        }
        return "";
    }

    public String getAnimalsYear() {
        return getAnimalsYear(lunarYear);
    }

    public String getAnimalsYear(int lunarYear) {
        return mYear_of_birth[(lunarYear - 4) % 12];
    }

    public String getChinaMonthString() {
        return getChinaMonthString(lunarMonth, isLeapMonth);
    }

    public String getChinaMonthString(int lunarMonth, boolean isLeapMonth) {
        String chinaMonth = (isLeapMonth ? mLunarLeapTag : "")
                + ((lunarMonth == 1) ? mZhengyueTag
                        : mLunarCalendarNumber[lunarMonth - 1]) + mLunarMonthTag;
        return chinaMonth;
    }

    public String getChinaDayString(boolean isDisplayLunarMonthForFirstDay) {
        return getChinaDayString(lunarMonth, lunarDay, isLeapMonth,
                isDisplayLunarMonthForFirstDay);
    }

    public String getChinaDayString(int lunarMonth, int lunarDay,
            boolean isLeapMonth, boolean isDisplayLunarMonthForFirstDay) {
        if (lunarDay > 30) return "";
        if (lunarDay == 1 && isDisplayLunarMonthForFirstDay)
            return getChinaMonthString(lunarMonth, isLeapMonth);
        if (lunarDay == 10) return mLunarCalendarTen[0] + mLunarCalendarTen[1];
        if (lunarDay == 20) return mLunarCalendarTen[4] + mLunarCalendarTen[1];

        return mLunarCalendarTen[lunarDay / 10]
                + mLunarCalendarNumber[(lunarDay + 9) % 10];
    }

    public String getChinaYearString() {
        return getChinaYearString(lunarYear);
    }

    public String getChinaYearString(int lunarYear) {
        return String.valueOf(lunarYear);
    }

    public String getLunarCalendarInfo(boolean multipleInfo, boolean needAppendType) {
        if (lunarYear == 0 || lunarMonth == 0 || lunarDay == 0) return null;

        String specialString;
        String commonString;
        if (needAppendType) {
            specialString = SEPARATION + TYPE_IS_SPECIAL;
            commonString = SEPARATION + TYPE_IS_COMMON;
        } else {
            specialString = "";
            commonString = "";
        }

        // if is tradition festival, return it
        String traditionFestivalStr = getTraditionalFestival();
        // if is festival, return it
        String festivalStr = getFestival();
        // if is solar term, return it
        String[] solarTermStr = getLunarSolarTerms(solarYear).split(SEPARATION);

        String specialInfo = "";
        if (!TextUtils.isEmpty(traditionFestivalStr)) {
            if (!multipleInfo) {
                return traditionFestivalStr + specialString;
            } else {
                specialInfo = traditionFestivalStr;
            }
        }
        if (solarTermStr.length == 2
                && !TextUtils.isEmpty(solarTermStr[1])) {
            if (!multipleInfo) {
                return solarTermStr[1] + specialString;
            } else {
                specialInfo = solarTermStr[1] + SEPARATION + specialInfo;
            }
        }
        if (!TextUtils.isEmpty(festivalStr)) {
            if (!multipleInfo) {
                return festivalStr + specialString;
            } else {
                specialInfo = festivalStr + SEPARATION + specialInfo;
            }
        }
        if (!TextUtils.isEmpty(solarTermStr[0])) {
            if (!multipleInfo) {
                return solarTermStr[0] + specialString;
            } else {
                specialInfo = solarTermStr[0] + SEPARATION + specialInfo;
            }
        }
        if (!TextUtils.isEmpty(specialInfo)) {
            return specialInfo;
        }

        // return china day
        String lunarDayStr = getChinaDayString(true);
        String ss = getString(R.string.chineseTen0) + getString(R.string.chineseNumber1);
        if (ss.equals(lunarDayStr)) {
            String lunarMonthStr = getChinaMonthString();
            return lunarMonthStr + commonString;
        } else {
            String sss = getString(R.string.chineseTen1) + getString(R.string.chineseNumber1);
            String ssss = getString(R.string.chineseTen2) + getString(R.string.chineseNumber1);
            if (sss.equals(lunarDayStr)) {
                return getString(R.string.chineseTen1)
                        + getString(R.string.chineseNumber0) + commonString;
            }
            if (ssss.equals(lunarDayStr)) {
                return getString(R.string.chineseTen2)
                        + getString(R.string.chineseNumber0) + commonString;
            }
            return lunarDayStr + commonString;
        }
    }

    public String[] getLunarCalendarAllInfo() {
        if (lunarYear == 0 || lunarMonth == 0 || lunarDay == 0) return null;
        String lunarYearStr = "";    // needn't get the lunar year value, return "".
        String lunarMonthStr = getChinaMonthString();
        String lunarDayStr = getChinaDayString(true);

        String traditionFestivalStr = getTraditionalFestival();
        String festivalStr = getFestival();
        String[] solarTermStr = getLunarSolarTerms(solarYear).split(SEPARATION);
        if (solarTermStr.length == 2) {
            // Also contains the qingming festival.
            return new String[] { lunarYearStr, lunarMonthStr, lunarDayStr,
                    solarTermStr[0], solarTermStr[1], festivalStr };
        } else {
            return new String[] { lunarYearStr, lunarMonthStr, lunarDayStr,
                    solarTermStr[0], traditionFestivalStr, festivalStr };
        }

    }

    public String getLunarStringForDayView(int year, int month, int monthDay,
            boolean multipleInfo) {
        LunarCalendarConvertUtil.parseLunarCalendar(year, month, monthDay,
                this);
        String[] s = getLunarCalendarAllInfo();
        if (s == null) return "";

        if (multipleInfo) {
            String result = "";
            if (!TextUtils.isEmpty(s[3])) {
                result = result + " " + s[3];
            }
            if (!TextUtils.isEmpty(s[4])) {
                result = result + " " + s[4];
            }
            if (!TextUtils.isEmpty(s[5])) {
                result = result + " " + s[5];
            }
            if (!TextUtils.isEmpty(result)) return result;
        } else {
            if (s[3] != null && !s[3].trim().equals("")) return s[3];
            if (s[4] != null && !s[4].trim().equals("")) return s[4];
            if (s[5] != null && !s[5].trim().equals("")) return s[5];
        }

        String lunar = getString(R.string.lunar);
        if (s[2].equals(s[1])) {
            s[2] = getString(R.string.chineseTen0) + getString(R.string.chineseNumber0);
        } else if (s[2].equals(getString(R.string.chineseTen1)
                + getString(R.string.chineseNumber1))) {
            s[2] = getString(R.string.chineseTen1) + getString(R.string.chineseNumber0);
        } else if (s[2].equals(getString(R.string.chineseTen2)
                + getString(R.string.chineseNumber1))) {
            s[2] = getString(R.string.chineseTen2) + getString(R.string.chineseNumber0);
        }

        return lunar + s[1] + s[2];
    }

    public String getLunarDay(int year, int month, int monthDay,
            boolean multipleInfo, boolean needAppendType) {
        LunarCalendarConvertUtil.parseLunarCalendar(year, month, monthDay, this);
        return getLunarCalendarInfo(multipleInfo, needAppendType);
    }

    public String[] getLunarForOneMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        int startDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        String[] lunars = new String[endDay - startDay + 1];
        for (int i = startDay; i <= endDay; i++) {
            lunars[i - 1] = getLunarDay(year, month, i, false, true);
        }
        return lunars;
    }

    private final IBinder mBinder = new ServiceStub(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    static class ServiceStub extends ILunarService.Stub {
        //TY: hanjq 20140829 modify for PROD103062599 begin
        //WeakReference<LunarService> mService;
        SoftReference<LunarService> mService;
        //TY: hanjq 20140829 modify for PROD103062599 end

        ServiceStub(LunarService service) {
            //TY: hanjq 20140829 modify for PROD103062599 begin
            //mService = new WeakReference<LunarService>(service);
            mService = new SoftReference<LunarService>(service);
            //TY: hanjq 20140829 modify for PROD103062599 end
        }

        public String getLunarStringForDayView(int year, int month, int monthDay,
                boolean multipleInfo) {
            if (mService.get() == null) {
                Log.e("LunarService", "getLunarStringForDayView,LunarService reference is null!");
                return " ";
            }
            return mService.get().getLunarStringForDayView(year, month, monthDay, multipleInfo);
        }

        public String getLunarDay(int year, int month, int monthDay, boolean multipleInfo) {
            //TY: hanjq 20140829 add for PROD103062599 begin
            if (mService.get() == null) {
                Log.e("LunarService", "getLunarDay,LunarService reference is null!");
                return " ";
            }
            //TY: hanjq 20140829 add for PROD103062599 end
            return mService.get().getLunarDay(year, month, monthDay, multipleInfo, false);
        }

        public String getSeparationForMultiInfo() {
            return mService.get().SEPARATION;
        }

        public String getSeparationForType() {
            return mService.get().SEPARATION;
        }

        public String getIsSpecialFlag() {
            return mService.get().TYPE_IS_SPECIAL;
        }

        public String[] getLunarAndType(int year, int month) {
            return mService.get().getLunarForOneMonth(year, month);
        }
    }
}
