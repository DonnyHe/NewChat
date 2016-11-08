package com.lodestreams.chat.util;

import android.os.Handler;

/**
 * Created by hjytl on 2016/7/23.
 */

public class ThreadUtil {
    /**
     * 运行在子线程
     *
     * @param r
     */
    public static void runInSubThread(Runnable r) {
        new Thread(r).start();
    }

    private static Handler handler = new Handler();

    /**
     * 运行在主线程
     *
     * @param r
     */
    public static void runInUiThread(Runnable r) {
        handler.post(r);
    }
}
