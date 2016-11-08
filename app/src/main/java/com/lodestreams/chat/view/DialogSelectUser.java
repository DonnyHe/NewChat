package com.lodestreams.chat.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.lodestreams.chat.R;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by DonnyHe on 2016/9/2.
 */

public class DialogSelectUser extends Dialog {
    private Context mContext;
    private LinearLayout mLayoutUser;
    private MaterialEditText mEditUserName;
    private IClickListener mClickListener;
    private ScrollView mScrollView;

    public interface IClickListener {
        void onConfirm(String userNames);

        void onCancel();
    }

    public void setClickListener(IClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public DialogSelectUser(Context context) {
        super(context);
        this.mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_select_user, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(view);

        Button btAddEditText = (Button) view.findViewById(R.id.bt_addEditText);
        Button btConfirm = (Button) view.findViewById(R.id.bt_confirm);
        Button btCancel = (Button) view.findViewById(R.id.bt_cancel);
        mEditUserName = (MaterialEditText) view.findViewById(R.id.edt_userName);
        mLayoutUser = (LinearLayout) view.findViewById(R.id.llyt_user);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        btAddEditText.setOnClickListener(addUserListener);
        btConfirm.setOnClickListener(new ClickListener());
        btCancel.setOnClickListener(new ClickListener());

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        lp.width = (int) (d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);


    }
    private View.OnClickListener addUserListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final MaterialEditText editText = new MaterialEditText(mContext);
            editText.setLayoutParams(mEditUserName.getLayoutParams());
            editText.setHint(mContext.getString(R.string.dialog_select_users_hint));

            mLayoutUser.addView(editText);
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    //editText.setFocusable(true);
                    editText.requestFocus();
                }
            },200);
        }
    };
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.bt_confirm:
                    int count = mLayoutUser.getChildCount();
                    String userName = "";
                    for(int i=0;i<count;i++){
                        EditText editText = (EditText) mLayoutUser.getChildAt(i);
                        if (!editText.getText().equals("")) {
                            userName += editText.getText() + ";";
                        }
                    }
                    mClickListener.onConfirm(userName);
                    break;
                case R.id.bt_cancel:
                    mClickListener.onCancel();
                    break;
            }
        }
    }
}
