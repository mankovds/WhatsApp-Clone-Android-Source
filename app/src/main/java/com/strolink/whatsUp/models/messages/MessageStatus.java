package com.strolink.whatsUp.models.messages;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

/**
 * Created by Abderrahim El imame on 2019-12-06.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "message_status")
public class MessageStatus {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ForeignKey(entity = MessageModel.class, parentColumns = "_id", childColumns = "messageId")
    private String messageId;

    @Embedded
    private UsersModel usersModel;

    private int status;

    private String deliveredDate;
    private String seenDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public UsersModel getUsersModel() {
        return usersModel;
    }

    public void setUsersModel(UsersModel usersModel) {
        this.usersModel = usersModel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(String deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getSeenDate() {
        return seenDate;
    }

    public void setSeenDate(String seenDate) {
        this.seenDate = seenDate;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MessageStatus{");
        sb.append("id=").append(id);
        sb.append(", messageId='").append(messageId).append('\'');
        sb.append(", usersModel=").append(usersModel);
        sb.append(", status=").append(status);
        sb.append(", deliveredDate='").append(deliveredDate).append('\'');
        sb.append(", seenDate='").append(seenDate).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
