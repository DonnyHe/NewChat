package com.lodestreams.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.google.gson.Gson;
import com.lodestreams.chat.amazon.AmazonUtil;
import com.lodestreams.chat.bean.Content;
import com.lodestreams.chat.greendao.entity.Message;
import com.lodestreams.chat.util.L;

import java.util.List;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by hjytl on 2016/08/07.
 */

public class ImagePagerAdapter extends PagerAdapter {
    private List<Message> mListMessage;
    private Gson mGson;
    private Context mContext;
    public ImagePagerAdapter(Context context, List<Message> listMessage) {
        mListMessage = listMessage;
        mGson = new Gson();
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListMessage.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final PhotoDraweeView photoDraweeView = new PhotoDraweeView(container.getContext());
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        int messageType = mListMessage.get(position).getSendType();
        Content content = mGson.fromJson(mListMessage.get(position).getContent(), Content.class);
        L.e(content.getUrl());
        if (messageType == Message.receive) {
            controller.setUri(Uri.parse(AmazonUtil.getUri(content.getUrl())));
        } else {
            controller.setUri("file://" + Uri.parse(content.getUrl()));//加载本地图片
        }
        controller.setOldController(photoDraweeView.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) {
                    return;
                }
                photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        photoDraweeView.setController(controller.build());
        try {
            container.addView(photoDraweeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        photoDraweeView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                ((Activity)mContext).finish();
            }
        });
        return photoDraweeView;
    }
}
