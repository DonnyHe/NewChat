package com.lodestreams.chat.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

/**
 * Created by hjytl on 2016/7/23.
 */

public class Content implements Parcelable {
    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_AUDIO = "audio";
    public static final String MESSAGE_TYPE_SHOOT = "shoot";

    public String type;//消息类型，文本，图片，声音，射箭
    public String time;
    public String text;//文本消息内容
    public String url;//图片或声音消息保存文件的url
    public float shotXPercent;//射箭对应对方屏幕最终的x坐标百分比
    public int shotStrength;//射箭的力度等级
    public int audioTime;//音频文件时长

    public int getShotStrength() {
        return shotStrength;
    }

    public void setShotStrength(int shotStrength) {
        this.shotStrength = shotStrength;
    }

    public float getShotXPercent() {
        return shotXPercent;
    }

    public void setShotXPercent(float shotXPercent) {
        this.shotXPercent = shotXPercent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getAudioTime() {
        return audioTime;
    }

    public void setAudioTime(int audioTime) {
        this.audioTime = audioTime;
    }

    public static class Builder {
        private final String mType;
        private String mTime;
        private String mText;
        private String mUrl;
        private float mShotXPercent;
        private int mShotStrength;
        private int mAudioTime;

        public Builder(String type) {
            mType = type;
        }

        public Builder time(String time) {
            mTime = time;
            return this;
        }

        public Builder text(String text) {
            mText = text;
            return this;
        }

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public Builder shotXPercent(float shotXPercent) {
            mShotXPercent = shotXPercent;
            return this;
        }

        public Builder shotStrength(int shotStrength) {
            mShotStrength = shotStrength;
            return this;
        }

        public Builder audioTime(int audioTime){
            mAudioTime = audioTime;
            return this;
        }

        public Content build() {
            Content content = new Content();
            content.type = mType;
            content.time = mTime;
            content.text = mText;
            content.url = mUrl;
            content.shotXPercent = mShotXPercent;
            content.shotStrength = mShotStrength;
            content.audioTime = mAudioTime;
            return content;
        }

        public String toJsonString() {
            Content content = new Content();
            content.type = mType;
            content.time = mTime;
            content.text = mText;
            content.url = mUrl;
            content.shotXPercent = mShotXPercent;
            content.shotStrength = mShotStrength;
            content.audioTime = mAudioTime;
            Gson gson = new Gson();
            return gson.toJson(content);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.time);
        dest.writeString(this.text);
        dest.writeString(this.url);
        dest.writeFloat(this.shotXPercent);
        dest.writeInt(this.shotStrength);
        dest.writeInt(this.audioTime);
    }

    public Content() {
    }

    protected Content(Parcel in) {
        this.type = in.readString();
        this.time = in.readString();
        this.text = in.readString();
        this.url = in.readString();
        this.shotXPercent = in.readFloat();
        this.shotStrength = in.readInt();
        this.audioTime = in.readInt();
    }

    public static final Parcelable.Creator<Content> CREATOR = new Parcelable.Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel source) {
            return new Content(source);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };
}
