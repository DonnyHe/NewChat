package com.lodestreams.chat.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lodestreams.chat.R;
import com.lodestreams.chat.util.DisplayUtil;
import com.lodestreams.chat.util.L;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.nereo.multi_image_selector.MultiImageSelector;

/**
 * Created by hjytl on 2016/7/24.
 */

public class ChatKeyboard extends RelativeLayout {

    public static final int LAYOUT_TYPE_MESSAGE = 1;//文本框有文字时，显示发送
    public static final int LAYOUT_TYPE_MORE = 2;//文本框没文字时，显示更多编辑框
    public static final int LAYOUT_TYPE_AUDIO = 3;//点击音频，此时显示语音发送
    public static final int LAYOUT_TYPE_SHOOTING = 4;//射箭中
    public static final int LAYOUT_TYPE_BE_SHOOT = 5;//被箭射中

    public static final int KEYBOARD_HIDE = 1;
    public static final int KEYBOARD_SHOW = 2;

    @BindView(R.id.bt_audio)
    Button btAudio;
    @BindView(R.id.edt_message)
    EditText edtMessage;
    @BindView(R.id.bt_arrow)
    Button btArrow;
    @BindView(R.id.bt_send)
    Button btSend;
    @BindView(R.id.rlyt_message)
    RelativeLayout rlytMessage;
    @BindView(R.id.bt_keyboard)
    Button btKeyboard;
    @BindView(R.id.bt_press_record)
    AudioRecorderButton btPressRecord;
    @BindView(R.id.bt_more_audio)
    Button btMoreAudio;
    @BindView(R.id.rlyt_audio)
    RelativeLayout rlytAudio;
    @BindView(R.id.bt_more_send)
    Button btMoreSend;
    @BindView(R.id.rlyt_more)
    RelativeLayout rlytMore;
    @BindView(R.id.llyt_more_image)
    LinearLayout llytMoreImage;

    private Context mContext;
    private int layoutType = LAYOUT_TYPE_MORE;
    private ArrayList<String> mSelectPath;
    public static final int REQUEST_IMAGE = 2;

    public ChatKeyboard(Context context) {
        super(context);
        init(context);
    }

    public ChatKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        this.mContext = context;
        View view = View.inflate(context, R.layout.chat_box, null);
        this.addView(view);
    }

    /**
     * 发送消息接口
     */
    public interface SendTextListener {
        void sendTextMessage(String text);
    }
    private SendTextListener mSendTextListener;
    public void setSendListener(SendTextListener sendTextListener){
        mSendTextListener = sendTextListener;
    }

    public String getEdtText(){
        return edtMessage.getText()+"";
    }

    /**
     * 录音完成接口
     */
    public interface RecordFinishListener{
        void onFinish(float seconds,String filePath);
    }
    private RecordFinishListener mRecordFinishlistener;
    public void setRecordFinishListener(RecordFinishListener recordFinishListener){
        mRecordFinishlistener = recordFinishListener;
    }


    public interface KeyBoardStateListener{
        void onChange(int state);
    }
    private KeyBoardStateListener mKeyBoardStateListener;
    public void setKeyboardstateListener(KeyBoardStateListener keyboardstateListener){
        mKeyBoardStateListener = keyboardstateListener;
    }

    public interface ShowMoreLayoutListener{
        void onMoreLayoutVisibleChange(int state,int moreLayoutHeight);
    }
    private ShowMoreLayoutListener mShowMoreLayoutListener;
    public void setShowMoreLayoutListener(ShowMoreLayoutListener showMoreLayoutListener){
        mShowMoreLayoutListener = showMoreLayoutListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        edtMessage.addTextChangedListener(textWatcher);
        edtMessage.setOnClickListener(edtClickListener);
        edtMessage.setOnFocusChangeListener(edtFocusChangeListener);
        btAudio.setOnClickListener(audioClickListener);
        btMoreAudio.setOnClickListener(moreAudioClickListener);
        btMoreSend.setOnClickListener(moreSendListener);
        btSend.setOnClickListener(sendClickListener);
        btKeyboard.setOnClickListener(keyboardClickListener);
        llytMoreImage.setOnClickListener(moreImageListener);
        btPressRecord.setRecordFinishListener(new AudioRecorderButton.RecordFinishListener() {
            @Override
            public void onFinish(float seconds, String filePath) {
                mRecordFinishlistener.onFinish(seconds,filePath);
            }
        });
    }

    private OnClickListener edtClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hideMoreLayout();
        }
    };
    private OnFocusChangeListener edtFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            hideMoreLayout();
        }
    };
    private OnClickListener moreImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            /*ConcreteObservable.getInstance().notifyObserver(ChatActivity.class,"pickImage");*/
            MultiImageSelector.create(mContext)
                    .showCamera(true) // 是否显示相机. 默认为显示
                    .count(9) // 最大选择图片数量, 默认为9. 只有在选择模式为多选时有效
                    .multi() // 多选模式, 默认模式;
                    .origin(mSelectPath) // 默认已选择图片. 只有在选择模式为多选时有效
                    .start((Activity) mContext, REQUEST_IMAGE);
        }
    };

    private OnClickListener moreSendListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int visibility = rlytMore.getVisibility();
            if (visibility == View.VISIBLE){
                showKeyboard(mContext);
                setEdtFocus();
                hideMoreLayout();
            }else if (visibility == View.GONE){
                hideKeyboard(mContext);
                showMoreLayout();
            }

        }
    };

    private OnClickListener keyboardClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (layoutType == LAYOUT_TYPE_AUDIO) {
                if (edtMessage.getText().length() != 0) {
                    layoutType = LAYOUT_TYPE_MESSAGE;
                } else {
                    layoutType = LAYOUT_TYPE_MORE;
                }
                setLayoutTypeView();
                showKeyboard(mContext);
                setEdtFocus();
            }
        }
    };
    private void setEdtFocus(){
        edtMessage.setFocusable(true);
        edtMessage.setFocusableInTouchMode(true);
        edtMessage.requestFocus();
        edtMessage.findFocus();
    }
    private OnClickListener sendClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (layoutType == LAYOUT_TYPE_MESSAGE) {
                mSendTextListener.sendTextMessage(edtMessage.getText()+"");
                edtMessage.setText("");
            } else if (layoutType == LAYOUT_TYPE_MORE) {

            }
            setLayoutTypeView();
        }
    };
    private OnClickListener moreAudioClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (edtMessage.getText().length() != 0) {
                layoutType = LAYOUT_TYPE_MESSAGE;
            } else {
                layoutType = LAYOUT_TYPE_MORE;
            }
            setLayoutTypeView();
            showMoreLayout();
        }
    };
    private OnClickListener audioClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (layoutType == LAYOUT_TYPE_MESSAGE || layoutType == LAYOUT_TYPE_MORE) {
                layoutType = LAYOUT_TYPE_AUDIO;
                hideKeyboard(mContext);
                hideMoreLayout();
            } else if (layoutType == LAYOUT_TYPE_AUDIO) {
                layoutType = LAYOUT_TYPE_MESSAGE;
                showKeyboard(mContext);
                hideMoreLayout();
            }
            setLayoutTypeView();
        }
    };
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                layoutType = LAYOUT_TYPE_MORE;
            } else {
                layoutType = LAYOUT_TYPE_MESSAGE;
            }
            setLayoutTypeView();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 文本框有文字时，显示发送
     */
    private void setLayoutTypeMessage() {
        if (rlytMessage.getVisibility() != View.VISIBLE) {
            rlytAudio.setVisibility(View.GONE);
            rlytMessage.setVisibility(View.VISIBLE);
        }
        btSend.setVisibility(View.VISIBLE);
        btMoreSend.setVisibility(View.GONE);
    }

    /**
     * 文本框没文字时，显示更多编辑框
     */
    private void setLayoutTypeMore() {
        if (rlytMessage.getVisibility() != View.VISIBLE) {
            rlytAudio.setVisibility(View.GONE);
            rlytMessage.setVisibility(View.VISIBLE);
        }
        btMoreSend.setVisibility(View.VISIBLE);
        btSend.setVisibility(View.GONE);
    }

    /**
     * 点击音频，此时显示语音发送
     */
    private void setLayoutTypeAudio() {
        rlytMessage.setVisibility(View.GONE);
        rlytAudio.setVisibility(View.VISIBLE);
    }

    public void setLayoutTypeView() {
        switch (layoutType) {
            case LAYOUT_TYPE_MESSAGE:
                setLayoutTypeMessage();
                break;
            case LAYOUT_TYPE_MORE:
                setLayoutTypeMore();
                break;
            case LAYOUT_TYPE_AUDIO:
                setLayoutTypeAudio();
                break;
        }
    }

    private void showMoreLayout() {
        L.e("showMoreLayout");
        hideKeyboard(mContext);
        rlytMore.setVisibility(View.VISIBLE);
        mShowMoreLayoutListener.onMoreLayoutVisibleChange(View.VISIBLE, (int) DisplayUtil.Dp2px(200));
    }
    private void hideMoreLayout(){
        L.e("hideMoreLayout");
        rlytMore.setVisibility(View.GONE);
        mShowMoreLayoutListener.onMoreLayoutVisibleChange(View.GONE,0);
    }
    /**
     * 隐藏软键盘
     */
    public void hideKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    /**
     * 显示软键盘
     */
    public void showKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null) {
                imm.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
