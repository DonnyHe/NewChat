package com.lodestreams.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.lodestreams.chat.ChatApplication;
import com.lodestreams.chat.R;
import com.lodestreams.chat.adapter.SessionAdapter;
import com.lodestreams.chat.bean.Constant;
import com.lodestreams.chat.bean.Content;
import com.lodestreams.chat.greendao.entity.Message;
import com.lodestreams.chat.greendao.entity.Room;
import com.lodestreams.chat.util.L;
import com.lodestreams.chat.util.T;
import com.lodestreams.chat.util.ThreadUtil;
import com.lodestreams.chat.view.DialogSelectUser;
import com.orhanobut.logger.Logger;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SessionActivity extends AppCompatActivity {
    @BindView(R.id.session_edit_search)
    EditText mEditSearch;
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;
    @BindView(R.id.bt_createGroup)
    Button btCreateGroup;
    private List<Room> mList;
    private SessionAdapter mAdapter;

    private Socket mSocket;
    private Context mContext;

    public static void startSessionActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, SessionActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        ButterKnife.bind(this);
        mContext = this;
        initView();

    }

    private void initView() {
        mSocket = ChatApplication.getInstance().getSocket();
        mSocket.connect();
        mList = Room.queryUserList(this);
        mAdapter = new SessionAdapter(this, mList);
        mSocket.on(Constant.Events.CREATE_ROOM, mCreateRoomListener);
        mSocket.on(Constant.Events.NEW_MESSAGE, mNewMsgListener);
        mSocket.on(Constant.Events.NEW_USER, mNewUserListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mEditSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if ((mEditSearch.getText() + "").
                            equals(ChatApplication.getInstance().getUser().userName)) {
                        T.ShowShort(SessionActivity.this, "You can't chat with yourself");
                        return false;
                    }
                    doSearch(mEditSearch.getText() + "");
                    return false;
                }
                return false;
            }
        });
        btCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogSelectUser dialogSelectUser = new DialogSelectUser(mContext);
                dialogSelectUser.setTitle("输入群聊用户名");
                dialogSelectUser.show();
                dialogSelectUser.setClickListener(new DialogSelectUser.IClickListener() {
                    @Override
                    public void onConfirm(String userNames) {
                        createGroupRoom(userNames);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialogSelectUser.dismiss();
                            }
                        }, 500);
                    }

                    @Override
                    public void onCancel() {
                        dialogSelectUser.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSocket.disconnect();
        mSocket.off(Constant.Events.CREATE_ROOM, mCreateRoomListener);
        mSocket.off(Constant.Events.NEW_MESSAGE, mNewMsgListener);
        mSocket.off(Constant.Events.NEW_USER, mNewUserListener);
        System.exit(0);
        super.onDestroy();
    }

    /**
     * 创建群聊房间
     *
     * @param userNames
     */
    private void createGroupRoom(String userNames) {
        final JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        String[] strArray = userNames.split(";");
        array.put(ChatApplication.getInstance().getUser().userName);
        for (String str : strArray) {
            array.put(str);
        }
        try {
            object.put("users", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Constant.Events.CREATE_ROOM, object, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = new JSONObject(args[0] + "");
                    if (obj.getInt("status") == 1) {

                    } else {
                        showMsg(obj.getString("msg"));
                    }
                } catch (Exception ex) {
                    showMsg(ex.getMessage());
                }
            }
        });
    }

    private void doSearch(String userName) {
        final JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            array.put(ChatApplication.getInstance().getUser().userName);
            array.put(userName);
            object.put("users", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        L.e("doSearch");
        mSocket.emit(Constant.Events.CREATE_ROOM, object, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = new JSONObject(args[0] + "");
                    if (obj.getInt("status") == 1) {
                    } else {
                        showMsg(obj.getString("msg"));
                    }
                } catch (Exception ex) {
                    showMsg(ex.getMessage());
                }
            }
        });
    }

    //监听有人加入房间
    private Emitter.Listener mNewUserListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Logger.e(args[0] + "");
                JSONObject object = new JSONObject(args[0] + "");
                String roomId = object.getString("roomId");
                String userName = object.getString("userName");
                String parent = object.getString("parent");
                if (parent.equals(Constant.Events.JOIN_ROOM)) {

                } else if (parent.equals(Constant.Events.ADD_USER_TO_ROOM)) {
                    if (userName.equals(ChatApplication.getInstance().getUser().userName)) {//自己被加入到某个已存在的群聊
                        Room room = new Room();
                        room.setRoomId(roomId);
                        room.setUserName("群聊");
                        DateTime time = DateTime.now();
                        room.setLastMessageTime(time.toString("yyyy-MM-dd HH:mm:ss"));
                        if (!containsInList(userName, room.getLastMessageTime(), roomId))
                            mList.add(0, room);
                        //joinRoom(roomId);
                        Room.insertRoom(room, SessionActivity.this);
                    }
                }
                SessionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //TODO 监听NEW_MESSAGE  收到新消息的时候,更新list,通知消息

    private Emitter.Listener mNewMsgListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //添加消息到本地记录
            try {
                JSONObject object = new JSONObject(args[0] + "");
                String userName = object.getString("userName");
                String roomId = object.getString("roomId");
                Long localId = Room.getIdByRoomId(mContext, roomId);
                String content = object.getString("content");
                if (!userName.equals(ChatApplication.getInstance().getUser().userName)) {
                    Message message = new Message.Builder(Message.receive)
                            .roomId(localId).sender(userName).receiver(ChatApplication.getInstance().getUser().userName).content(content).build();
                    Gson gson = new Gson();
                    Content detail = gson.fromJson(content, Content.class);
                    if (!detail.getType().equals(Content.MESSAGE_TYPE_SHOOT)) {
                        L.e("xxx");
                        Message.insertMessage(message, SessionActivity.this);
                    }
                }

                DateTime time = DateTime.now();
                String timeStr = time.toString("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < mList.size(); i++) {
                    //存在当前会话列表中
                    if (mList.get(i).getRoomId().equals(roomId)) {
                        mList.get(i).setLastMessageTime(timeStr);
                        Room room = mList.get(i);
                        mList.remove(i);
                        mList.add(0,room);
                        break;
                    }
                }

                SessionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener mCreateRoomListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Logger.e(args[0] + "");
                JSONObject object = new JSONObject(args[0] + "");
                String roomId = object.getString("roomId");
                /*Intent intent = new Intent(SessionActivity.this, ChatActivity.class);
                intent.putExtra("roomId", roomId);*/
                JSONArray array = object.getJSONArray("users");
                //服务器返回创建成功,将该会话,保存到本地
                Room room = new Room();
                room.setRoomId(roomId);
                DateTime time = DateTime.now();
                room.setLastMessageTime(time.toString("yyyy-MM-dd HH:mm:ss"));
                String str = "";
                for (int i = 0; i < array.length(); i++) {
                    if (!array.get(i).equals(ChatApplication.getInstance().getUser().userName)){
                        str += ","+array.get(i);
                    }
                }
                if (str.startsWith(",")){
                    str = str.substring(1,str.length());
                }
                room.setUserName(str);
                //array  0是请求发起方,1请求接收方
                if (array.get(0).toString().equals(ChatApplication.getInstance().getUser().userName)) {
                    if (!containsInList(array.get(1).toString(), room.getLastMessageTime(), roomId))
                        mList.add(0, room);
                } else {
                    //不相等,是对方发送过来的消息,添加到会话列表仅提示,,Added: 并且加入到房间中,监听NEW_MESSAGE,并且,需要更新列表中的room_id
                    if (!containsInList(array.get(0).toString(), room.getLastMessageTime(), roomId)) {
                        mList.add(0, room);
                    }
                }
                SessionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
                joinRoom(roomId);
                Room.insertRoom(room, SessionActivity.this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void joinRoom(String roomId) {
        L.e("joinRomm:" + ChatApplication.getInstance().getUser().userName);
        JSONObject obj = new JSONObject();
        JSONArray aar = new JSONArray();
        try {
            aar.put(roomId);
            obj.put("userName", ChatApplication.getInstance().getUser().userName);
            obj.put("rooms", aar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Constant.Events.JOIN_ROOM, obj, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = new JSONObject(args[0] + "");
                    if (obj.getInt("status") == 1) {
                        //加入房间成功,监听NEW_MESSAGE
                        //mSocket.on(Constant.Events.NEW_MESSAGE, mNewMsgListener);
                    } else {
                        L.e("TAG", obj.getString("msg"));
                    }
                } catch (Exception ex) {
                    L.e("TAG", ex.getMessage());
                }
            }
        });
    }

    private boolean containsInList(String name, String time, String roomId) {
        for (int i = 0; i < mList.size(); i++) {
            //并且,不存在当前会话列表中
            if (mList.get(i).getUserName().equals(name)) {
                mList.get(i).setLastMessageTime(time);
                mList.get(i).setRoomId(roomId);
                return true;
            }
        }
        return false;
    }


    private void showMsg(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                T.ShowShort(SessionActivity.this, msg);
            }
        });
    }


}
