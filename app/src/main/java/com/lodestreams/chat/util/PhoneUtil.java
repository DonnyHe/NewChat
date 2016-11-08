package com.lodestreams.chat.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

/**
 * Created by hejiyang on 2016/1/26.
 */
public class PhoneUtil {
    public static final int NET_MOBILE = 1;
    public static final int NET_WIFI = 2;
    public static final int NET_NONE = 3;

    /**
     * 判断网络连接是否可用
     * @param context
     * @return
     */
    public static boolean isNetWorkConnected(Context context){
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = manager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
    /**
     * 判断wifi网络是否可用
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context){
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
    /**
     * 判断mobile网络是否可用
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context){
        if (context == null) {
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
    /**
     * 获取网络状态
     * @param context
     * @return
     */
    public static int getNetState(Context context){
        if (context == null) {
            return 0;
        }
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        State state = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (state == State.CONNECTED) {
            return NET_MOBILE;
        }
        state = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (state == State.CONNECTED) {
            return NET_WIFI;
        }
        return NET_NONE;
    }
    /**
     * 获取SDK版本
     * @return
     */
    public static int getSDK(){
        return android.os.Build.VERSION.SDK_INT;
    }
    /**
     * 获取系统版本
     * @return
     */
    public static String getSystemRelease(){
        return android.os.Build.VERSION.RELEASE;
    }
    /**
     * 获取手机型号
     * @return
     */
    public static String getPhoneModel(){
        return android.os.Build.MODEL;
    }

    public static String getIMEI(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static boolean existSDCard() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }
}
