package com.lodestreams.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.gson.Gson;
import com.lodestreams.chat.ChatApplication;
import com.lodestreams.chat.R;
import com.lodestreams.chat.adapter.MessageAdapter;
import com.lodestreams.chat.amazon.AmazonUtil;
import com.lodestreams.chat.amazon.Constants;
import com.lodestreams.chat.bean.Constant;
import com.lodestreams.chat.bean.Content;
import com.lodestreams.chat.greendao.entity.Message;
import com.lodestreams.chat.greendao.entity.Room;
import com.lodestreams.chat.util.DisplayUtil;
import com.lodestreams.chat.util.ImageUtil;
import com.lodestreams.chat.util.L;
import com.lodestreams.chat.util.T;
import com.lodestreams.chat.util.ThreadUtil;
import com.lodestreams.chat.view.Arrow;
import com.lodestreams.chat.view.ChatKeyboard;
import com.lodestreams.chat.view.DialogSelectUser;
import com.lodestreams.chat.view.Target;
import com.lodestreams.chat.view.WrapContentLinearLayoutManager;
import com.orhanobut.logger.Logger;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import me.nereo.multi_image_selector.MultiImageSelector;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_message)
    RecyclerView rvMessage;
    @BindView(R.id.chat_keyboard)
    ChatKeyboard chatKeyboard;
    @BindView(R.id.flyt_shoot)
    FrameLayout flytShoot;
    @BindView(R.id.arrow)
    Arrow arrow;
    @BindView(R.id.arrow_receive)
    Arrow arrowReceive;
    @BindView(R.id.target)
    Target target;
    @BindView(R.id.rlyt_chat)
    RelativeLayout rlytChat;

    private ArrayList<String> mSelectPath;
    private Context mContext;
    private MessageAdapter mAdapter;
    private List<Message> mListMessage = new ArrayList<>();
    private Socket mSocket;
    private String mServerRoomId;//服务端RoomId，用于发送消息
    private Long mLocalRoomId;//本地数据库保存的RoomId（Room表的主键），方便查询信息

    private static final int UPDATE_LIST = 101;
    private static final int RECEIVE_SHOOT = 102;
    private static final int TARGET_SCROLL = 103;
    private static final int USER_JOIN = 104;
    private View btShootArrow;

    private int mDownX;
    private int mDownY;
    private int mUpx;
    private int mUpY;
    private boolean mIsInRect = false;
    private VelocityTracker mTracker;//监测滑动速度
    private String mSenderUserName;//发送方用户名,也即自己
    private String mReceiverUserName;//接收方用户名
    private TransferUtility transferUtility;
    private int mFlag = 30;//判断射箭起点容错值，值越高，容错率越高
    private int mRealKeyboardHeight = 0;//键盘高度
    private int mFrameHeight;
    private static String TAG = "ChatActivity";
    private boolean mIsShowMoreLayout = false;
    private int mMoreLayoutHeight = 0;

    public static void startChatActivity(Activity activity, String roomId, Long id, String userName) {
        Intent intent = new Intent();
        intent.putExtra("roomId", roomId);//服务器的房间号
        intent.putExtra("id", id);//Id（Room主键）
        intent.putExtra("userName", userName);
        intent.setClass(activity, ChatActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mContext = this;
        initView();

    }

    private void initView() {
        mSenderUserName = ChatApplication.getInstance().getUser().userName;
        mServerRoomId = getIntent().getStringExtra("roomId");
        mLocalRoomId = getIntent().getLongExtra("id", 0);
        mReceiverUserName = getIntent().getStringExtra("userName");
        L.e(mReceiverUserName);
        mListMessage.clear();
        mListMessage = Message.queryMessageList(mContext, mLocalRoomId);
        mAdapter = new MessageAdapter(mContext, mListMessage);
        toolbar.setTitle(mReceiverUserName);//对方用户名
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chatKeyboard.setSendListener(sendTextListener);
        btShootArrow = chatKeyboard.findViewById(R.id.bt_arrow);
        chatKeyboard.setRecordFinishListener(recordFinishListener);
        chatKeyboard.setShowMoreLayoutListener(showmoreLayoutListener);
        rvMessage.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvMessage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvMessage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rvMessage.smoothScrollToPosition(rvMessage.getAdapter().getItemCount());
                        }
                    }, 100);
                }
            }
        });
        rvMessage.setAdapter(mAdapter);
        scroollToBottom();
        mSocket = ChatApplication.getInstance().getSocket();
        mSocket.on(Constant.Events.NEW_MESSAGE, onNewMessageListener);
        mSocket.on(Constant.Events.NEW_USER, mNewUserListener);
        mSocket.connect();
        flytShoot.bringToFront();
        transferUtility = AmazonUtil.getTransferUtility(this);
        //监控输入法隐藏和显示，改变箭靶位置
        rlytChat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                // r will be populated with the coordinates of your view that area still visible.
                rlytChat.getWindowVisibleDisplayFrame(r);
                int screenHeight = rlytChat.getRootView().getHeight();
                int heightDiff = screenHeight - (r.bottom - r.top);
                int statusBarHeight = 0;
                if (heightDiff > 100)
                    // if more than 100 pixels, its probably a keyboard
                    // get status bar height
                    statusBarHeight = 0;
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mRealKeyboardHeight = heightDiff - statusBarHeight;
                if (mRealKeyboardHeight == 0) {
                    mFrameHeight = ((ViewGroup) target.getParent()).getHeight();
                    if (mIsShowMoreLayout) {
                        target.setTranslationY(mFrameHeight - DisplayUtil.Dp2px(mContext, 100) - mMoreLayoutHeight);
                    } else {
                        target.setTranslationY(mFrameHeight - DisplayUtil.Dp2px(mContext, 100));
                    }
                    //L.e(TAG,"mFrameHeight:" + mFrameHeight);
                    //L.e(TAG,"keyboard height = " + mRealKeyboardHeight);
                } else {
                    //L.e(TAG,"mFrameHeight:" + mFrameHeight);
                    //L.e(TAG,"keyboard height = " + mRealKeyboardHeight);
                    target.setTranslationY(mFrameHeight - mRealKeyboardHeight - DisplayUtil.Dp2px(mContext, 100));
                }
            }
        });
        joinRoom();
    }

    private void joinRoom() {
        JSONObject obj = new JSONObject();
        JSONArray aar = new JSONArray();
        try {
            aar.put(mServerRoomId);
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

    @Override
    protected void onStart() {
        super.onStart();
        int[] location = new int[2];
        btShootArrow.getLocationOnScreen(location);
        int arrowX = location[0];
        DisplayUtil.setMargins(arrowReceive, arrowX, 0, 0, 0);
        mHandler.sendEmptyMessageDelayed(TARGET_SCROLL, 500);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mIsInRect = pointIsInRect(mDownX, mDownY);
                if (mIsInRect) {
                    if (mTracker == null) {
                        mTracker = VelocityTracker.obtain();
                    } else {
                        mTracker.clear();
                    }
                    mTracker.addMovement(event);
                }
                L.e(TAG, "MotionEvent.ACTION_DOWN:" + mIsInRect);
                L.e(TAG, "mDownX:" + mDownX);
                L.e(TAG, "mDownY:" + mDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsInRect) {
                    mTracker.addMovement(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                L.e(TAG, "mUpx:" + mUpx);
                L.e(TAG, "mUpY:" + mUpY);
                L.e(TAG, "MotionEvent.ACTION_UP:" + mIsInRect);
                if (mIsInRect && !arrow.isShooting()) {
                    mUpx = (int) event.getX();
                    mUpY = (int) event.getY();
                    int moveY = Math.abs(mDownY - mUpY);
                    L.e(TAG, "moveY:" + moveY);
                    if (moveY > 100) {
                        mTracker.computeCurrentVelocity(1000);
                        L.e(TAG, "mTracker.getXVelocity(0)" + mTracker.getXVelocity(0));
                        L.e(TAG, "mTracker.getYVelocity(0)" + mTracker.getYVelocity(0));
                        shootArrow(mDownX, mDownY, mUpx, mUpY, calculateVelocity(mTracker.getXVelocity(0), mTracker.getYVelocity(0)));
                    }
                }
                mIsInRect = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 根据x方向和y方向的速度，算出总速度
     *
     * @param xVelocity
     * @param yVelocity
     * @return
     */
    private float calculateVelocity(float xVelocity, float yVelocity) {
        return (float) Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
    }

    /**
     * 当前触摸的点是否位于Arrow按钮内
     *
     * @return
     */
    private boolean pointIsInRect(int pointX, int pointY) {
        int[] location = new int[2];
        btShootArrow.getLocationOnScreen(location);
        int arrowX = location[0];
        int arrowY = location[1];
        if (pointX + mFlag > arrowX && pointX - mFlag < arrowX + btShootArrow.getWidth() && pointY + mFlag > arrowY && pointY - mFlag < arrowY + btShootArrow.getHeight()) {
            return true;
        }
        return false;
    }

    /**
     * 根据此四个参数计算角度
     *
     * @param startX
     * @param startY
     * @param upX    手指抬起的横坐标
     * @param upY    手指抬起的纵坐标
     */
    private void shootArrow(float startX, float startY, float upX, float upY, float speed) {
        int[] location = new int[2];
        btShootArrow.getLocationOnScreen(location);
        int x = location[0] - DisplayUtil.Dp2px(mContext, 10);//箭尾相对保持在按钮中央部位
        int y = location[1] - DisplayUtil.Dp2px(mContext, 100);//按钮自身高度50dp，

        startX = x;//取射箭按钮中心位置为起点，若以接触点为起点，箭的初始点相对按钮可能偏移较多，不美观
        startY = y;
        upY -= DisplayUtil.Dp2px(mContext, 100);
        int endY = 0;
        int endX = (int) (startX - ((startX - upX) * (startY - endY) / (startY - upY)));//三角函数
        L.e("endx:" + endX);
        arrow.shoot(startX, startY, endX, endY, upX, upY, speed);
        String content = new Content.Builder(Content.MESSAGE_TYPE_SHOOT).shotStrength(Arrow.getShotStrength(speed)).shotXPercent(getShootPercent(endX)).toJsonString();
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).content(content).build();
        sendMessage(message);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_LIST:
                    mAdapter.notifyItemInserted(mListMessage.size() - 1);
                    scroollToBottom();
                    break;
                case RECEIVE_SHOOT:
                    Content detail = msg.getData().getParcelable("detail");
                    receiveShoot(detail.getShotXPercent(), detail.getShotStrength());
                    break;
                case TARGET_SCROLL:
                    target.startScroll();
                    break;
                case USER_JOIN:
                    String username = msg.getData().getString("username");
                    T.ShowShort(mContext, username + "加入房间!");
                    break;
            }
        }
    };

    private ChatKeyboard.ShowMoreLayoutListener showmoreLayoutListener = new ChatKeyboard.ShowMoreLayoutListener() {
        @Override
        public void onMoreLayoutVisibleChange(int state, int moreLayoutHeight) {
            if (state == View.VISIBLE) {
                mMoreLayoutHeight = moreLayoutHeight;
                mIsShowMoreLayout = true;
            } else if (state == View.GONE) {
                mMoreLayoutHeight = moreLayoutHeight;
                mIsShowMoreLayout = false;
            }
        }
    };

    private Emitter.Listener onNewMessageListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //Logger.e(args[0] + "");
            try {
                JSONObject object = new JSONObject(args[0] + "");
                String userName = object.getString("userName");
                String roomId = object.getString("roomId");
                String content = object.getString("content");
                if (!userName.equals(mSenderUserName) && roomId.equals(mServerRoomId)) {
                    Gson gson = new Gson();
                    Content detail = gson.fromJson(content, Content.class);
                    Message message = new Message.Builder(Message.receive).roomId(mLocalRoomId).sender(userName).receiver(mReceiverUserName).content(content).build();
                    if (detail.getType().equals(Content.MESSAGE_TYPE_SHOOT)) {
                        android.os.Message msg = mHandler.obtainMessage();
                        msg.what = RECEIVE_SHOOT;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("detail", detail);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    } else {
                        mListMessage.add(message);
                        //Message.insertMessage(message, mContext);
                        mHandler.sendEmptyMessage(UPDATE_LIST);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private ChatKeyboard.RecordFinishListener recordFinishListener = new ChatKeyboard.RecordFinishListener() {
        @Override
        public void onFinish(float seconds, String filePath) {
            addAudio(filePath, (int) seconds);
        }
    };

    private ChatKeyboard.SendTextListener sendTextListener = new ChatKeyboard.SendTextListener() {
        @Override
        public void sendTextMessage(String text) {
            addText(text);
        }
    };

    private void addText(String text) {
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).roomId(mLocalRoomId)
                .content(new Content.Builder(Content.MESSAGE_TYPE_TEXT).time(new DateTime().toString("HH:mm")).text(text).toJsonString()).build();
        L.e("send message!!!!!!!");
        sendMessage(message);
        mListMessage.add(message);
        mHandler.sendEmptyMessage(UPDATE_LIST);
        insertMessage(message);
    }

    private void sendMessage(Message message) {
        JSONObject object = new JSONObject();
        try {
            object.put("roomId", mServerRoomId);
            object.put("userName", mSenderUserName);
            object.put("content", message.getContent());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Constant.Events.NEW_MESSAGE, object, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = new JSONObject(args[0] + "");
                    if (obj.getInt("status") == 1) {
                        //发送成功
                    } else {
                        L.e(TAG, obj.getString("msg"));
                    }
                } catch (Exception ex) {
                    L.e(TAG, ex.getMessage());
                }
            }
        });
    }

    private void insertMessage(Message message) {
        Message.insertMessage(message, mContext);
    }

    private void addAudio(String filePath, int seconds) {
        uploadFile(filePath, Content.MESSAGE_TYPE_AUDIO, seconds);
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).roomId(mLocalRoomId)
                .content(new Content.Builder(Content.MESSAGE_TYPE_AUDIO).time(new DateTime().toString("HH:mm")).url(filePath).audioTime(seconds).toJsonString()).build();
        mListMessage.add(message);
        insertMessage(message);
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }

    private void addImage(String filePath) {
        uploadFile(filePath, Content.MESSAGE_TYPE_IMAGE, 0);
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).roomId(mLocalRoomId)
                .content(new Content.Builder(Content.MESSAGE_TYPE_IMAGE).time(new DateTime().toString("HH:mm")).url(filePath).toJsonString()).build();
        mListMessage.add(message);
        insertMessage(message);
        mHandler.sendEmptyMessage(UPDATE_LIST);
    }

    private void uploadFile(final String filePath, String type, int seconds) {
        final File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(), file);
        observer.setTransferListener(new UploadListener(file.getName(), type, seconds));
    }

    private class UploadListener implements TransferListener {
        private String mFileName;
        private String mType;
        private int mSeconds;

        public UploadListener(String fileName, String type, int seconds) {
            mFileName = fileName;
            mType = type;
            mSeconds = seconds;
        }

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.e(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.e(TAG, "onStateChanged: " + id + ", " + newState);
            if (newState.equals(TransferState.COMPLETED)) {
                if (mType.equals(Content.MESSAGE_TYPE_IMAGE)) {
                    sendImageMessage(mFileName);
                } else if (mType.equals(Content.MESSAGE_TYPE_AUDIO)) {
                    sendAudioMessage(mFileName, mSeconds);
                }
            }
        }
    }

    private void sendAudioMessage(String fileName, int seconds) {
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).roomId(mLocalRoomId)
                .content(new Content.Builder(Content.MESSAGE_TYPE_AUDIO).time(new DateTime().toString("HH:mm")).url(fileName).audioTime(seconds).toJsonString()).build();
        sendMessage(message);
    }

    private void sendImageMessage(String fileName) {
        Message message = new Message.Builder(Message.send).sender(mSenderUserName).receiver(mReceiverUserName).roomId(mLocalRoomId)
                .content(new Content.Builder(Content.MESSAGE_TYPE_IMAGE).time(new DateTime().toString("HH:mm")).url(fileName).toJsonString()).build();
        sendMessage(message);
    }

    private void scroollToBottom() {
        if (mAdapter.getItemCount() > 0) {
            rvMessage.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatKeyboard.REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (final String p : mSelectPath) {
                    ThreadUtil.runInSubThread(new Runnable() {
                        @Override
                        public void run() {
                            File file = ImageUtil.scalImgFile(p, 50);
                            addImage(file.getAbsolutePath());
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mTracker != null) {
            mTracker.recycle();
            mTracker = null;
        }
        L.e(TAG, "onDestroy");
        mSocket.off(Constant.Events.NEW_MESSAGE, onNewMessageListener);
        mSocket.off(Constant.Events.NEW_USER, mNewUserListener);
        super.onDestroy();
    }

    /**
     * 计算箭到达标题栏时坐标所占百分比
     *
     * @param endX
     * @return
     */
    private float getShootPercent(float endX) {
        int width = chatKeyboard.getWidth();
        return endX / width;
    }

    /**
     * 当接收到对方的箭时，根据百分比计算终点横坐标
     *
     * @param percent
     * @return
     */
    private float getShootEndxByPercent(float percent) {
        int width = chatKeyboard.getWidth();
        return width * percent;
    }

    /**
     * 显示接收到的对方射箭动画
     *
     * @param shootXpercent
     * @param shootStength
     */
    private void receiveShoot(float shootXpercent, int shootStength) {
        int[] keyboardLocation = new int[2];
        chatKeyboard.getLocationOnScreen(keyboardLocation);
        int keyboardY = keyboardLocation[1] - 2 * btShootArrow.getHeight();

        int[] shootArrowLocation = new int[2];
        btShootArrow.getLocationOnScreen(shootArrowLocation);
        int shootArrowX = shootArrowLocation[0];
        float endX = getShootEndxByPercent(shootXpercent);
        float endY = keyboardY;
        float startY = 0;
        float startX = shootArrowX;
        arrowReceive.setTarget(target);
        arrowReceive.receiveShoot(startX, startY, endX, endY, shootStength);
        arrowReceive.setOnShotListener(new Arrow.OnShotListener() {
            @Override
            public void onShoot() {
                String text = chatKeyboard.getEdtText();
                if (!TextUtils.isEmpty(text)) {
                    addText(text);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add:
                createAddUserDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAddUserDialog() {
        final DialogSelectUser dialogSelectUser = new DialogSelectUser(mContext);
        dialogSelectUser.show();
        dialogSelectUser.setClickListener(new DialogSelectUser.IClickListener() {
            @Override
            public void onConfirm(String userNames) {
                String[] strArray = userNames.split(";");
                for (String str : strArray) {
                    pullUserToRoom(str);
                }
                dialogSelectUser.dismiss();
            }

            @Override
            public void onCancel() {
                dialogSelectUser.dismiss();
            }
        });
    }

    private void pullUserToRoom(final String userName) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("roomId", mServerRoomId);
            obj.put("userName", userName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(Constant.Events.ADD_USER_TO_ROOM, obj, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject obj = new JSONObject(args[0] + "");
                    if (obj.getInt("status") == 1) {
                    } else {
                        L.e("TAG", obj.getString("msg"));
                    }
                } catch (Exception ex) {
                    L.e("TAG", ex.getMessage());
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
                if (roomId.equals(mServerRoomId) && parent.equals(Constant.Events.ADD_USER_TO_ROOM)) {
                    android.os.Message message = android.os.Message.obtain();
                    message.what = USER_JOIN;
                    Bundle bundle = new Bundle();
                    bundle.putString("username", userName);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
