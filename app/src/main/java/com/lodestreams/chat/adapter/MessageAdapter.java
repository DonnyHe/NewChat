package com.lodestreams.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.lodestreams.chat.R;
import com.lodestreams.chat.activity.ImageActivity;
import com.lodestreams.chat.bean.Content;
import com.lodestreams.chat.greendao.entity.Message;
import com.lodestreams.chat.util.DisplayUtil;
import com.lodestreams.chat.util.ThreadUtil;
import com.lodestreams.chat.view.MediaManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.lodestreams.chat.amazon.AmazonUtil.getUri;

/**
 * Created by hjytl on 2016/7/27.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Viewholder> {
    private List<Message> mListMessage;
    private Context mContext;
    private int mMinItemWidth;
    private int mMaxItemWidth;

    public MessageAdapter(Context context, List<Message> messageList) {
        mListMessage = messageList;
        mContext = context;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mMaxItemWidth = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWidth = (int) (outMetrics.widthPixels * 0.15f);
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.receive:
                layout = R.layout.chat_item_left;
                break;
            case Message.send:
                layout = R.layout.chat_item_right;
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new Viewholder(v, viewType);
    }

    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        Message message = mListMessage.get(position);
        Gson gson = new Gson();
        Content content = gson.fromJson(message.getContent(), Content.class);
        holder.setUserName(message.getReveiverUserName(), message.getSenderUserName(), content.getTime());
        switch (content.getType()) {
            case Content.MESSAGE_TYPE_TEXT:
                holder.setText(content.getText());
                break;
            case Content.MESSAGE_TYPE_AUDIO:
                holder.setAudio(content.getUrl(), content.getAudioTime());
                break;
            case Content.MESSAGE_TYPE_IMAGE:
                holder.setImage(content.getUrl(), message);
                break;
            case Content.MESSAGE_TYPE_SHOOT:

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mListMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mListMessage.get(position).getSendType();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private int mViewType;

        private TextView mDate;
        private TextView mUserName;
        private TextView mText;
        private SimpleDraweeView mImgImage;
        private ImageView mImgAudio;
        private ImageView mImgPhoto;
        private TextView mAudioTime;
        private LinearLayout mLayoutContent;
        private Bitmap mBitmap = null;

        public Viewholder(View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
            mDate = (TextView) itemView.findViewById(R.id.tv_date);
            mUserName = (TextView) itemView.findViewById(R.id.tv_username);
            mText = (TextView) itemView.findViewById(R.id.tv_text);
            mImgImage = (SimpleDraweeView) itemView.findViewById(R.id.img_image);
            mImgAudio = (ImageView) itemView.findViewById(R.id.img_audio);
            mImgPhoto = (ImageView) itemView.findViewById(R.id.img_photo);
            mAudioTime = (TextView) itemView.findViewById(R.id.tv_audio_time);
            mLayoutContent = (LinearLayout) itemView.findViewById(R.id.llyt_content);
        }

        public void setUserName(String receiver, String sender, String dateTime) {
            if (null == mUserName || null == mDate) {
                return;
            }
            mUserName.setText(sender);
            mDate.setText(dateTime);
            //int resourceId = mContext.getResources().getIdentifier("avatar_00"+ (new Random().nextInt(4)+1),"drawable",mContext.getPackageName());
            int resourceId = mViewType == Message.receive ? R.drawable.avatar_002 : R.drawable.avatar_005;
            mImgPhoto.setImageResource(resourceId);
        }

        public void setText(String text) {
            if (null == mText) {
                return;
            }
            mImgAudio.setVisibility(View.GONE);
            mImgImage.setVisibility(View.GONE);
            mAudioTime.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
            mText.setText(text);
            mLayoutContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        public void setImage(final String url, final Message message) {
            if (null == mImgImage) {
                return;
            }
            mText.setVisibility(View.GONE);
            mImgAudio.setVisibility(View.GONE);
            mAudioTime.setVisibility(View.GONE);
            mImgImage.setVisibility(View.VISIBLE);
            int width = (int) DisplayUtil.Dp2px(100);
            int height = width;

            if (mViewType == Message.receive) {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(getUri(url)))
                        .setResizeOptions(new ResizeOptions(width, height))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(mImgImage.getController())
                        .setImageRequest(request)
                        .build();
                mImgImage.setController(controller);
                //mImgImage.setImageURI(Uri.parse(getUri(url)));
            } else {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + url))
                        .setResizeOptions(new ResizeOptions(width, height))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(mImgImage.getController())
                        .setImageRequest(request)
                        .build();
                mImgImage.setController(controller);

                //mImgImage.setImageBitmap(ImageUtil.getCompressBitmap(url));
            }
            mLayoutContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Message> list = new ArrayList<Message>();
                    list.add(message);
                    ImageActivity.startActivity((Activity) mContext, list);
                }
            });
        }

        public void setAudio(String url, int audioTime) {
            if (null == mImgAudio) {
                return;
            }
            mText.setVisibility(View.GONE);
            mImgImage.setVisibility(View.GONE);
            mAudioTime.setVisibility(View.VISIBLE);
            mImgAudio.setVisibility(View.VISIBLE);
            if (mViewType == Message.receive) {
                mImgAudio.setImageResource(R.drawable.voice_left);
                mAudioTime.setText(audioTime + "``");
                mLayoutContent.setTag(getUri(url));
            } else {
                mImgAudio.setImageResource(R.drawable.voice_right);
                mAudioTime.setText("``" + audioTime);
                mLayoutContent.setTag(url);
            }
            ViewGroup.LayoutParams layoutParams = mLayoutContent.getLayoutParams();
            layoutParams.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f * audioTime));
            mLayoutContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ThreadUtil.runInSubThread(new Runnable() {
                        @Override
                        public void run() {
                            MediaManager.playSound(mLayoutContent.getTag() + "", new MediaPlayer.OnCompletionListener() {

                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                }
                            });
                        }
                    });

                }
            });
        }


        private Bitmap getDiskBitmap(String pathString) {
            try {
                File file = new File(pathString);
                if (file.exists()) {
                    mBitmap = BitmapFactory.decodeFile(pathString);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            return mBitmap;
        }
    }
}
