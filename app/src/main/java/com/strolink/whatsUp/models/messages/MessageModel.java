package com.strolink.whatsUp.models.messages;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

//@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "messages",
        indices = {@Index(value = {"_id"},
        unique = true)})
public class MessageModel {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private long id;


    private String _id;


    private String created;

    @ForeignKey(entity = ConversationModel.class, parentColumns = "_id", childColumns = "conversationId")
    private String conversationId;


    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "senderId")
    private String senderId;

    private String sender_phone;
    private String sender_image;


    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "recipientId")
    private String recipientId;
    private String recipient_phone;
    private String recipient_image;


    @ForeignKey(entity = GroupModel.class, parentColumns = "_id", childColumns = "groupId")
    private String groupId;
    private String group_name;
    private String group_image;


    private String reply_id;

    private boolean reply_message;


    private boolean is_group;

    private String duration_file;

    private String file_size;

    private String message;

    private int status;

    private String file;

    private String file_type;

    private boolean file_upload;

    private boolean file_downLoad;

    private String longitude;

    private String latitude;

    private String state;

    private String document_name;

    private String document_type;

    public MessageModel() {
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_image() {
        return group_image;
    }

    public void setGroup_image(String group_image) {
        this.group_image = group_image;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public boolean isReply_message() {
        return reply_message;
    }

    public void setReply_message(boolean reply_message) {
        this.reply_message = reply_message;
    }

    public boolean isIs_group() {
        return is_group;
    }

    public void setIs_group(boolean is_group) {
        this.is_group = is_group;
    }

    public String getDuration_file() {
        return duration_file;
    }

    public void setDuration_file(String duration_file) {
        this.duration_file = duration_file;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public boolean isFile_upload() {
        return file_upload;
    }

    public void setFile_upload(boolean file_upload) {
        this.file_upload = file_upload;
    }

    public boolean isFile_downLoad() {
        return file_downLoad;
    }

    public void setFile_downLoad(boolean file_downLoad) {
        this.file_downLoad = file_downLoad;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(String document_name) {
        this.document_name = document_name;
    }

    public String getDocument_type() {
        return document_type;
    }

    public void setDocument_type(String document_type) {
        this.document_type = document_type;
    }

    public String getSender_phone() {
        return sender_phone;
    }

    public void setSender_phone(String sender_phone) {
        this.sender_phone = sender_phone;
    }

    public String getSender_image() {
        return sender_image;
    }

    public void setSender_image(String sender_image) {
        this.sender_image = sender_image;
    }

    public String getRecipient_phone() {
        return recipient_phone;
    }

    public void setRecipient_phone(String recipient_phone) {
        this.recipient_phone = recipient_phone;
    }

    public String getRecipient_image() {
        return recipient_image;
    }

    public void setRecipient_image(String recipient_image) {
        this.recipient_image = recipient_image;
    }


    @Override
    public String toString() {
        return "{" + "id=" + id +
                ", _id='" + _id + '\'' +
                ", created='" + created + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", sender_phone='" + sender_phone + '\'' +
                ", sender_image='" + sender_image + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", recipient_phone='" + recipient_phone + '\'' +
                ", recipient_image='" + recipient_image + '\'' +
                ", groupId='" + groupId + '\'' +
                ", group_image='" + group_image + '\'' +
                ", group_name='" + group_name + '\'' +
                ", reply_id='" + reply_id + '\'' +
                ", reply_message=" + reply_message +
                ", is_group=" + is_group +
                ", duration_file='" + duration_file + '\'' +
                ", file_size='" + file_size + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", file='" + file + '\'' +
                ", file_type='" + file_type + '\'' +
                ", file_upload=" + file_upload +
                ", file_downLoad=" + file_downLoad +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", state='" + state + '\'' +
                ", document_name='" + document_name + '\'' +
                ", document_type='" + document_type + '\'' +
                '}';
    }
}
