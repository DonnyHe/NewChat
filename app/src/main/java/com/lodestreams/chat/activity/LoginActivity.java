package com.lodestreams.chat.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lodestreams.chat.ChatApplication;
import com.lodestreams.chat.R;
import com.lodestreams.chat.bean.Constant;
import com.lodestreams.chat.bean.UserModel;
import com.lodestreams.chat.util.EditWatcher;
import com.lodestreams.chat.util.SharedPreferencesUtil;
import com.lodestreams.chat.util.T;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.Ack;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_edit_nick)
    EditText mEditNick;
    @BindView(R.id.login_btn_start)
    Button mBtnStart;

    private io.socket.client.Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        EditWatcher.regist(mBtnStart, mEditNick);
        mSocket = ChatApplication.getInstance().getSocket();
        mSocket.connect();
        mEditNick.setText(SharedPreferencesUtil.getPreferString(this, SharedPreferencesUtil.USER_NAME, ""));
        //T.ShowShort(this, mSocket.connected() + "");
    }

    @OnClick(value = {R.id.login_btn_start})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn_start:
                login();
                break;
        }
    }

    private void login() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("userName",mEditNick.getText()+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Constant.Events.REGISTER, obj, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject object = new JSONObject(args[0] + "");
                    int status = object.getInt("status");
                    if (status == 1) {
                        SharedPreferencesUtil.setPreferString(LoginActivity.this, SharedPreferencesUtil.USER_NAME, mEditNick.getText() + "");
                        ChatApplication.getInstance().setUser(new UserModel(mEditNick.getText() + ""));
                        SessionActivity.startSessionActivity(LoginActivity.this);
                        LoginActivity.this.finish();
                    } else {
                        T.ShowShort(LoginActivity.this, object.getString("msg"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
