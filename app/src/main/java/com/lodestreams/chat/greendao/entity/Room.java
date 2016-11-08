package com.lodestreams.chat.greendao.entity;

import android.content.Context;

import com.lodestreams.chat.greendao.DBManager;
import com.lodestreams.chat.greendao.gen.DaoMaster;
import com.lodestreams.chat.greendao.gen.DaoSession;
import com.lodestreams.chat.greendao.gen.RoomDao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import static android.R.id.list;

/**
 * Created by hjytl on 2016/7/23.
 */
@Entity
public class Room {
    @Id(autoincrement = true)
    private Long id;
    /**
     * @Index 索引
     */
    @Index
    private String roomId;//房间Id
    private String userName;//对方用户名
    private String avatar;//对方头像
    private String lastMessageTime;//最后一条消息的时间
    private String lastMessage;//最后一条消息
    private String tag;//备用

    @Generated(hash = 434480643)
    public Room(Long id, String roomId, String userName, String avatar, String lastMessageTime,
                String lastMessage, String tag) {
        this.id = id;
        this.roomId = roomId;
        this.userName = userName;
        this.avatar = avatar;
        this.lastMessageTime = lastMessageTime;
        this.lastMessage = lastMessage;
        this.tag = tag;
    }

    @Generated(hash = 703125385)
    public Room() {
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static void insertRoom(Room room, Context ctx) {
        DaoMaster daoMaster = new DaoMaster(DBManager.getInstance(ctx).getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        RoomDao roomDao = daoSession.getRoomDao();
        QueryBuilder<Room> query = roomDao.queryBuilder();
        List<Room> list = query.where(RoomDao.Properties.RoomId.eq(room.getRoomId())).list();
        if (list.size() > 0){
            return;
        }
        list = query.where(RoomDao.Properties.UserName.eq(room.getUserName())).list();
        if (list.size() > 0) {
            room.setId(list.get(0).id);
            roomDao.update(room);
        } else {
            room.id = null;
            roomDao.insert(room);
        }
    }

    public static Long getIdByRoomId(Context ctx, String roomId){
        DaoMaster daoMaster = new DaoMaster(DBManager.getInstance(ctx).getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        RoomDao roomDao = daoSession.getRoomDao();
        Room room = roomDao.queryBuilder().where(RoomDao.Properties.RoomId.eq(roomId)).build().unique();
        return room.getId();
    }

    public static List<Room> queryUserList(Context ctx) {
        DaoMaster daoMaster = new DaoMaster(DBManager.getInstance(ctx).getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        RoomDao roomDao = daoSession.getRoomDao();
        QueryBuilder<Room> qb = roomDao.queryBuilder();
        qb.orderAsc(RoomDao.Properties.LastMessageTime);
        List<Room> list = qb.list();
        return list;
    }
}
