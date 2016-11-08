package com.lodestreams.chat;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.lodestreams.chat.bean.APIs;
import com.lodestreams.chat.bean.UserModel;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by PuPeng on 16/7/24.
 */
public class ChatApplication extends Application {

    private static ChatApplication mInstance;
    private UserModel userModel;
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocket = IO.socket(URI.create(APIs.API_ROOM));
            }
        }).start();
        CrashReport.initCrashReport(getApplicationContext(), "900058297", true);
        Fresco.initialize(ChatApplication.this);
        mInstance = this;

    }

    public UserModel getUser() {
        return userModel == null ? new UserModel() : userModel;
    }

    public void setUser(UserModel user) {
        this.userModel = user;
    }

    public static ChatApplication getInstance() {
        return mInstance;
    }

    public Socket getSocket() {
        return mSocket;
    }

}
