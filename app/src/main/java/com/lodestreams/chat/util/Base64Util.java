package com.lodestreams.chat.util;

import android.util.Base64;

/**
 * Created by hejiyang on 2016/5/3.
 */
public class Base64Util {
    public static String getBase64Encode(String str){
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    public static String getBase64Decode(String strBase64){
        return new String(Base64.decode(strBase64.getBytes(), Base64.DEFAULT));
    }
}
