package com.lodestreams.chat.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hejiyang on 2016/1/27.
 */
public class AppUtil {
    /**
     * 获取APP版本号
     */
    public static int getVersionCode(Context context){
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo!=null) {
            return getPackageInfo(context).versionCode;
        }else{
            return -1;
        }
    }
    /**
     * 获取APP版本名称
     */
    public static String getVersionName(Context context){
        PackageInfo packageInfo = getPackageInfo(context);
        if (packageInfo!=null) {
            return getPackageInfo(context).versionName;
        }else{
            return "-1";
        }
    }

    private static PackageInfo getPackageInfo(Context context){
        PackageInfo packageInfo = null;
        PackageManager manager = context.getPackageManager();
        try {
            packageInfo = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static String getMD5(String s){
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            byte[] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte b = md[i];
                str[k++] = hexDigits[b >> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

}
