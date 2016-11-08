package com.lodestreams.chat.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lodestreams.chat.R;

/**
 * Created by hjytl on 2016/7/25.
 */

public class DialogManager {
    private ImageView imgDialogRecorder;
    private ImageView imgDialogVoiceLevel;
    private TextView tvDialogLabel;

    private Dialog mDialog;
    private Context mContext;

    public DialogManager(Context context) {
        mContext = context;
    }

    public void showRecordingDialog() {
        mDialog = new Dialog(mContext, R.style.ThemeAudioDialog);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_audio, null);
        mDialog.setContentView(view);
        imgDialogRecorder = (ImageView) mDialog.findViewById(R.id.img_dialog_recorder);
        imgDialogVoiceLevel = (ImageView) mDialog.findViewById(R.id.img_dialog_voice_level);
        tvDialogLabel = (TextView) mDialog.findViewById(R.id.tv_dialog_label);

        mDialog.show();
    }

    public void recording() {
        if(mDialog != null && mDialog.isShowing())
        {
            imgDialogRecorder.setVisibility(View.VISIBLE);
            imgDialogVoiceLevel.setVisibility(View.VISIBLE);
            tvDialogLabel.setVisibility(View.VISIBLE);

            imgDialogRecorder.setImageResource(R.drawable.dialog_audio_recorder);
            tvDialogLabel.setBackgroundColor(Color.TRANSPARENT);
            tvDialogLabel.setText(mContext.getString(R.string.dialog_audio_cancel));
        }
    }

    public void wantToCancel() {
        if(mDialog != null && mDialog.isShowing())
        {
            imgDialogRecorder.setVisibility(View.VISIBLE);
            imgDialogVoiceLevel.setVisibility(View.GONE);
            tvDialogLabel.setVisibility(View.VISIBLE);

            imgDialogRecorder.setImageResource(R.drawable.dialog_audio_cancel);
            tvDialogLabel.setBackgroundResource(R.color.recorder_dialog_text);
            tvDialogLabel.setText(mContext.getString(R.string.btn_recorder_cancel));
        }
    }

    public void tooShort() {
        if(mDialog != null && mDialog.isShowing())
        {
            imgDialogRecorder.setVisibility(View.VISIBLE);
            imgDialogVoiceLevel.setVisibility(View.GONE);
            tvDialogLabel.setVisibility(View.VISIBLE);

            imgDialogRecorder.setImageResource(R.drawable.dialog_audio_short);
            tvDialogLabel.setBackgroundColor(Color.TRANSPARENT);
            tvDialogLabel.setText(mContext.getString(R.string.dialog_audio_too_short));
        }
    }

    public void disMissDialog() {
        if(mDialog != null && mDialog.isShowing())
        {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()){
            int resourceId = mContext.getResources().getIdentifier("v"+level,"drawable",mContext.getPackageName());
            imgDialogVoiceLevel.setImageResource(resourceId);
        }
    }
}
