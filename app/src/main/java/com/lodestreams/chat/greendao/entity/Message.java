package com.lodestreams.chat.greendao.entity;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.lodestreams.chat.greendao.DBManager;
import com.lodestreams.chat.greendao.gen.DaoMaster;
import com.lodestreams.chat.greendao.gen.DaoSession;
import com.lodestreams.chat.greendao.gen.MessageDao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjytl on 2016/7/23.
 */
@Entity
public class Message implements Parcelable {
    /**
     * @Transient 不会生成数据库表的列
     */
    @Transient
    public final static int receive = 1;
    @Transient
    public final static int send = 0;

    /**
     * @Id 主键
     */
    @Id(autoincrement = true)
    private Long messageId;
    /**
     * @Index 索引
     */
    @Index
    private Long roomId;//房间Id
    private int sendType;//消息发送类型：0位发送，1为接收
    private String reveiverUserName;//接收方用户名
    private String senderUserName;//发送方用户名
    private String content;//消息体
    private String tag;//备用

    @Generated(hash = 412350422)
    public Message(Long messageId, Long roomId, int sendType, String reveiverUserName, String senderUserName, String content, String tag) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.sendType = sendType;
        this.reveiverUserName = reveiverUserName;
        this.senderUserName = senderUserName;
        this.content = content;
        this.tag = tag;
    }

    @Generated(hash = 637306882)
    public Message() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getSenderUserName() {
        return this.senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }

    public String getReveiverUserName() {
        return this.reveiverUserName;
    }

    public void setReveiverUserName(String reveiverUserName) {
        this.reveiverUserName = reveiverUserName;
    }

    public static void insertMessage(Message msg, Context ctx) {
        DaoMaster daoMaster = new DaoMaster(DBManager.getInstance(ctx).getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        MessageDao messageDao = daoSession.getMessageDao();
        messageDao.insert(msg);
    }
    public static List<Message> queryMessageList(Context ctx, Long localRoomId) {
        List<Message> list = new ArrayList<>();
        try {
            DaoMaster daoMaster = new DaoMaster(DBManager.getInstance(ctx).getReadableDatabase());
            DaoSession daoSession = daoMaster.newSession();
            MessageDao messageDao = daoSession.getMessageDao();
            /*list = messageDao.queryBuilder().orderAsc(MessageDao.Properties.MessageId)
                    .where(MessageDao.Properties.ReveiverUserName.in(receiverUserName,senderUserName),MessageDao.Properties.SenderUserName.in(receiverUserName,senderUserName)).build().list();*/
            list = messageDao.queryBuilder().where(MessageDao.Properties.RoomId.eq(localRoomId)).build().list();
            //考虑用户再次进入app时重新输入不同的用户名时，单聊搜索可实现，但若需要考虑群聊的情况，则需要加字段，Message表加当前用户名字段即可，暂不考虑
            /*Query query = messageDao.queryBuilder().where(
                    new WhereCondition.StringCondition("ROOM_ID="+localRoomId+" and (REVEIVER_USER_NAME='"+selfUserName+"' or SENDER_USER_NAME='"+selfUserName+"')"))
                    .build();
            list = query.list();*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public static class Builder {
        private final int mSendType;
        private Long mRoomId;//房间Id
        private String mReceiver;//接收方用户名
        private String mSender;//发送方用户名
        private String mTag;//备用
        private String mContent;//消息体

        public Builder(int sendType) {
            mSendType = sendType;
        }

        public Builder roomId(Long roomId) {
            mRoomId = roomId;
            return this;
        }

        public Builder receiver(String reveiverUserName) {
            mReceiver = reveiverUserName;
            return this;
        }

        public Builder sender(String senderUserName) {
            mSender = senderUserName;
            return this;
        }
        public Builder tag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder content(String content) {
            mContent = content;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.messageId = null;
            message.sendType = mSendType;
            message.roomId = mRoomId;
            message.reveiverUserName = mReceiver;
            message.senderUserName = mSender;
            message.tag = mTag;
            message.content = mContent;
            return message;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.messageId);
        dest.writeValue(this.roomId);
        dest.writeInt(this.sendType);
        dest.writeString(this.reveiverUserName);
        dest.writeString(this.senderUserName);
        dest.writeString(this.content);
        dest.writeString(this.tag);
    }

    protected Message(Parcel in) {
        this.messageId = (Long) in.readValue(Long.class.getClassLoader());
        this.roomId = (Long) in.readValue(Long.class.getClassLoader());
        this.sendType = in.readInt();
        this.reveiverUserName = in.readString();
        this.senderUserName = in.readString();
        this.content = in.readString();
        this.tag = in.readString();
    }

    @Transient
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
