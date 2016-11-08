package com.lodestreams.chat.view;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by hjytl on 2016/7/25.
 */

public class AudioManager {

    private static AudioManager mInstance;
    private String mDir;//存储的文件夹
    private boolean isPrepare = false;
    private MediaRecorder mMediaRecorder;
    private String mCurrentFilePath;

    public AudioManager(String dir) {
        this.mDir = dir;
    }
    public interface AudioStateListener{
        void isPrepared();
    }
    private AudioStateListener mAudioStateListener;
    public void setAudioStateListener(AudioStateListener listener){
        mAudioStateListener = listener;
    }
    public static AudioManager getInstance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }

    public void prepareAudio() {

        try {
            isPrepare = false;
            File dir = new File(mDir);
            if (!dir.exists()){
                dir.mkdirs();
            }
            String fileName = generateName();
            File file = new File(dir,fileName);
            mCurrentFilePath = file.getAbsolutePath();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            isPrepare = true;
            if (mAudioStateListener != null){
                mAudioStateListener.isPrepared();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String generateName() {
        return UUID.randomUUID().toString()+".amr";
    }

    public int getVoiceLevel(int maxLevel){
        if (isPrepare){
            //getMaxAmplitude 取值范围为1-32767
            //mMediaRecorder.getMaxAmplitude() * maxLevel) / 32768 假设maxlevel为7，则mMediaRecorder.getMaxAmplitude() * maxLevel) / 32768的值为0到6
            try {
                return (mMediaRecorder.getMaxAmplitude() * maxLevel) / 32768 + 1;
            }catch (Exception e){

            }
        }
        return 1;
    }

    public void release(){
        if (mMediaRecorder != null){
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void cancel(){
        release();
        if (mCurrentFilePath != null){
            File file = new File(mCurrentFilePath);
            if (file.exists()) {
                file.delete();
                mCurrentFilePath = null;
            }
        }
    }

    public String getCurrentFilePath(){
        return mCurrentFilePath;
    }


}
