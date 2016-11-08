package com.lodestreams.chat.view;

import android.app.Activity;
import android.content.Context;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.lodestreams.chat.R;
import com.lodestreams.chat.util.DisplayUtil;
import com.lodestreams.chat.util.PhoneUtil;
import com.lodestreams.chat.util.ThreadUtil;
import com.lodestreams.chat.util.VibratorUtil;
import com.orhanobut.logger.Logger;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * Created by hjytl on 2016/7/25.
 */

public class AudioRecorderButton extends Button implements AudioManager.AudioStateListener {
    public static String dir = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/Audio";
    private static final int STATE_NORMAL = 1;//正常状态
    private static final int STATE_RECORDING = 2;//录音中
    private static final int STATE_CANCEL = 3;//取消
    private static final int MSG_AUDIO_PREPARED = 101;//录音开始
    private static final int MSG_VOICE_CHANGE = 102;//音量改变
    private static final int MSG_DIALOG_DISMISS = 103;//关闭提示框
    private int mCurrentState = STATE_NORMAL;
    private float DISTANCE_CANCEL;
    private DialogManager mDialogManager;
    private AudioManager mAudioManager;
    private boolean isRecording = true;
    private boolean isExistSDCard = false;
    //是否触发长按事件
    private boolean isLongTouchReady = false;
    private float mRecordTime = 0;
    private SoundPool soundPool;
    private Context mContext;
    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = getContext();
        DISTANCE_CANCEL = DisplayUtil.Dp2px(60);
        mDialogManager = new DialogManager(getContext());
        soundPool = new SoundPool(10, android.media.AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(getContext(), R.raw.recorder, 1);
        if (PhoneUtil.existSDCard()) {
            isExistSDCard = true;
            mAudioManager = AudioManager.getInstance(dir);
            mAudioManager.setAudioStateListener(this);
        }
        this.setOnLongClickListener(longClickListener);
    }

    private OnLongClickListener longClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {

            isLongTouchReady = true;
            mAudioManager.prepareAudio();
            return false;
        }
    };

    public interface RecordFinishListener{
        void onFinish(float seconds,String filePath);
    }
    private RecordFinishListener mRecordFinishlistener;
    public void setRecordFinishListener(RecordFinishListener recordFinishListener){
        mRecordFinishlistener = recordFinishListener;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    VibratorUtil.Vibrate((Activity) getContext(), 50);
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    ThreadUtil.runInSubThread(getVolumeLevel);
                    break;
                case MSG_VOICE_CHANGE:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DISMISS:
                    mDialogManager.disMissDialog();
                    break;
            }
        }
    };

    private Runnable getVolumeLevel = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mRecordTime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isExistSDCard) {
            return false;
        }
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                playPromptTone();
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    if (wantToCancel(x, y)) {
                        changeState(STATE_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isLongTouchReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                Logger.e(mRecordTime + "");
                //录音时间过短
                if (!isRecording || mRecordTime < 0.6) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1000);
                } else if (mCurrentState == STATE_RECORDING) {//正常录音
                    mDialogManager.disMissDialog();
                    mAudioManager.release();
                    if (mRecordFinishlistener != null){
                        mRecordFinishlistener.onFinish(mRecordTime,mAudioManager.getCurrentFilePath());
                    }
                    //changeState(STATE_NORMAL);
                } else if (mCurrentState == STATE_CANCEL) {//录音取消
                    mDialogManager.disMissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                mAudioManager.cancel();
                reset();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void reset() {
        mRecordTime = 0;
        isRecording = false;
        isLongTouchReady = false;
        changeState(STATE_NORMAL);
    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y > getHeight() + DISTANCE_CANCEL || y < -DISTANCE_CANCEL) {
            return true;
        }
        return false;
    }

    private void changeState(int stateRecord) {
        if (mCurrentState != stateRecord) {
            mCurrentState = stateRecord;
            switch (stateRecord) {
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.shape_btn_recorder_recording);
                    setText(getResources().getString(R.string.btn_recorder_recording));
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.shape_btn_recorder_normal);
                    setText(getResources().getString(R.string.btn_recorder_normal));
                    break;
                case STATE_CANCEL:
                    setBackgroundResource(R.drawable.shape_btn_recorder_recording);
                    setText(getResources().getString(R.string.btn_recorder_cancel));
                    mDialogManager.wantToCancel();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void isPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 点击录音按钮播放提示音
     */
    private void playPromptTone() {
        soundPool.play(1, 1, 1, 0, 0, 2);
    }
}
