package com.guli.secmanager.Utils;

/**
 * Created by daihongqiao on 16-4-19.
 */
public class ShareUtil {

    public final static String DATABASE_NAME = "managerData";
    public final static String PROVINCE = "provinceId";
    public final static String CITY = "cityId";
    public final static String OPERATOR = "operatorId";
    public final static String BRAND = "brandId";
    public final static String ACCOUNT_DATE = "accountDate";
    public final static String AUTO_CORRECT_STATE = "autoCorrect";
    public final static String AUTO_BREAK_STATE = "autoBreak";

    //每月限额流量,月标准套餐总量，闲时套餐总量，4G套餐专用总量之和*/KB
    //public final static String FLOW_ALL_TOTAL_FOR_MONTH = "flow_all_total_for_month";
    //public static int FLOW_ALL_TOTAL_DEFAULT = 0;

    //该月统计已用的流量,月标准套餐总量，闲时套餐总量，4G套餐专用总量之和
    //public final static String FLOW_ALL_USED_FOR_MONTH = "flow_all_used_for_month";
    //public static int FLOW_ALL_USED_DEFAULT = 0;

    //统计某个月的流量
    public final static String CURRENT_MONTH = "current_month";

    //流量超额自动断网
    //public final static String FLOW_AUTO_BREAK="flow_auto_break";

    //手动填写套餐流量菜单
    //public final static String MONTH_STANDARD_TOTAL = "month_standard_total";
    //public final static String MONTH_STANDARD_USED = "month_standard_used";

    //public final static String FREE_FLOW_TOTAL = "free_flow_total";
    //public final static String FREE_FLOW_USED = "free_flow_used";

    //public final static String FLOW_4G_TOTAL = "4G_flow_total";
    //public final static String FLOW_4G_USED = "4G_flow_used";

    //闲时套餐时间段
    public static final String START_TIME_HOUR = "start_time_hour";
    //public static final String START_TIME_MIN = "start_time_min";
    public static final String END_TIME_HOUR = "end_time_hour";
    //public static final String END_TIME_MIN = "end_time_min";

    //流量校准结果
    public static final String SIM1_COMMON_LEFT_KBYTES = "sim1_common_left_kbytes";
    public static final String SIM1_COMMON_USED_KBYTES = "sim1_common_used_kbytes";
    public static final String SIM1_COMMON_TOTAL_KBYTES = "sim1_common_total_kbytes";

    public static final String SIM1_FREE_LEFT_KBYTES = "sim1_free_left_kbytes";
    public static final String SIM1_FREE_USED_KBYTES = "sim1_free_used_kbytes";
    public static final String SIM1_FREE_TOTAL_KBYTES = "sim1_free_total_kbytes";

    //public static final String SIM1_4G_LEFT_KBYTES = "sim1_4g_left_kbytes";
    //public static final String SIM1_4G_USED_KBYTES = "sim1_4g_used_kbytes";
    //public static final String SIM1_4G_TOTAL_KBYTES = "sim1_4g_total_kbytes";

    public static final String SIM2_COMMON_LEFT_KBYTES = "sim2_common_left_kbytes";
    public static final String SIM2_COMMON_USED_KBYTES = "sim2_common_used_kbytes";
    public static final String SIM2_COMMON_TOTAL_KBYTES = "sim2_common_total_kbytes";

    public static final String SIM2_FREE_LEFT_KBYTES = "sim2_free_left_kbytes";
    public static final String SIM2_FREE_USED_KBYTES = "sim2_free_used_kbytes";
    public static final String SIM2_FREE_TOTAL_KBYTES = "sim2_free_total_kbytes";

    //public static final String SIM2_4G_LEFT_KBYTES = "sim2_4g_left_kbytes";
    //public static final String SIM2_4G_USED_KBYTES = "sim2_4g_used_kbytes";
    //public static final String SIM2_4G_TOTAL_KBYTES = "sim2_4g_total_kbytes";

    //检测的应用包名
    public static final String PKGNAME = "com.guli.secmanager";

    //超额自动断网
    public static final String ACTION_AUTO_BREAK_FLOW = "intent.action.AUTO_BREAK_FLOW";

    //需要调用setUsedForMonth设置月使用流量
    public static final String ACTION_SET_USED_FOR_MONTH = "intent.action.SET_USED_FOR_MONTH";
    //闲时流量是否存在
    //public static final String SIM1_FREE_EXIST = "sim1_free_exist";

    //开始启动流量自动校准功能
    public static final String REQUEST_FLOW_AUTO_CORRECTING  = "request_flow_auto_correcting";

    //闲时流量基值，闲时流量开始计算时，通用流量已使用数量
    public static final String FREE_BASE_VALUE_KBYTES  = "free_base_value_kbytes";



    ////////////////////////////////////////////////////////////////////////////////////////////////
    //第一次启动软件
    public static final String FIRST_OPEN = "isFirstOpen";

    //累积清理垃圾
    public static final String GARBAGE_TOTAL = "garbage_total";

    //未处理的病毒数
    public static final String VIRUS_COUNT = "virus_count";
    //未处理的病毒软件名
    public static final String VIRUS_PACKAGE_NAME = "virus_package";

}
