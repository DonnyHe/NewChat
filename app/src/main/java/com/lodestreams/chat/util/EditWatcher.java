package com.lodestreams.chat.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.lodestreams.chat.R;


/**
 * Created by PuPeng on 2015/12/28.
 */
public class EditWatcher implements TextWatcher {
    private EditText[] mTexts;
    private Button mWatchedButton;

    public static void regist(Button btn,EditText... edits) {
        new EditWatcher(btn,edits);
    }

    private EditWatcher(Context context, Button btn, EditText... texts) {
        mTexts = texts;
        mWatchedButton = btn;
        for (int i = 0; i < mTexts.length; i++) {
            mTexts[i].addTextChangedListener(this);
        }
        mWatchedButton.setTextColor(context.getResources().getColorStateList(R.color.btn_color));
        mWatchedButton.setBackgroundResource(R.drawable.selector_common_btn);
    }

    private EditWatcher(Button btn, EditText... texts) {
        mTexts = texts;
        mWatchedButton = btn;
        mWatchedButton.setEnabled(false);
        for (int i = 0; i < mTexts.length; i++) {
            mTexts[i].addTextChangedListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        for (int i = 0; i < mTexts.length; i++) {
            if (TextUtils.isEmpty(mTexts[i].getText())) {
                //如果有任意一个为空，那么被监听的Button值为不可用
                mWatchedButton.setEnabled(false);
                return;
            }
        }
        //如果循环完成后，所有EditText都不为空，则，Button可用
        mWatchedButton.setEnabled(true);
    }
}
