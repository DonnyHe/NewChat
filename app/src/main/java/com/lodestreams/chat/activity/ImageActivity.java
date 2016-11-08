package com.lodestreams.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.lodestreams.chat.R;
import com.lodestreams.chat.adapter.ImagePagerAdapter;
import com.lodestreams.chat.greendao.entity.Message;
import com.lodestreams.chat.view.MultiTouchViewPager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageActivity extends AppCompatActivity {

    @BindView(R.id.view_pager)
    MultiTouchViewPager viewPager;

    private Context mContext;
    public static void startActivity(Activity activity, ArrayList<Message> listMessage){
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("listMessage",listMessage);
        intent.setClass(activity,ImageActivity.class);
        ActivityCompat.startActivity(activity,intent,null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mContext = this;
        ButterKnife.bind(this);
        ArrayList<Message> listMessage = getIntent().getParcelableArrayListExtra("listMessage");
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(mContext,listMessage);
        viewPager.setAdapter(pagerAdapter);

    }
}
