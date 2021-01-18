package com.strolink.whatsUp.models.messages;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "chats",
        indices = {@Index(value = {"_id"},
                unique = true)})
public class ConversationModel {

    @PrimaryKey(autoGenerate = true)
    private long id;


    private String _id;

    private String created;

    private int status;

    private boolean is_group;

    private int unread_message_counter;

    private String messageBeingComposed;

    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "owner_id")
    private String owner_id;
    private String owner_image;
    private String owner_phone;
    private String owner_displayed_name;


    @ForeignKey(entity = GroupModel.class, parentColumns = "_id", childColumns = "group_id")
    private String group_id;
    private String group_image;
    private String group_name;


    @ForeignKey(entity = MessageModel.class, parentColumns = "_id", childColumns = "latest_message_id")
    private String latest_message_id;

    private String latest_message;

    private String file_type;

    private String latest_message_latitude;

    private String latest_message_state;

    private String latest_message_created;

    private int latest_message_status;


    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "latest_message_sender_id")
    private String latest_message_sender_id;
    private String latest_message_sender_phone;
    private String latest_message_sender__displayed_name;

    public ConversationModel() {

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isIs_group() {
        return is_group;
    }

    public void setIs_group(boolean is_group) {
        this.is_group = is_group;
    }

    public int getUnread_message_counter() {
        return unread_message_counter;
    }

    public void setUnread_message_counter(int unread_message_counter) {
        this.unread_message_counter = unread_message_counter;
    }

    public String getMessageBeingComposed() {
        return messageBeingComposed;
    }

    public void setMessageBeingComposed(String messageBeingComposed) {
        this.messageBeingComposed = messageBeingComposed;
    }


    public String getLatest_message() {
        return latest_message;
    }

    public void setLatest_message(String latest_message) {
        this.latest_message = latest_message;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getOwner_image() {
        return owner_image;
    }

    public void setOwner_image(String owner_image) {
        this.owner_image = owner_image;
    }

    public String getOwner_phone() {
        return owner_phone;
    }

    public void setOwner_phone(String owner_phone) {
        this.owner_phone = owner_phone;
    }

    public String getOwner_displayed_name() {
        return owner_displayed_name;
    }

    public void setOwner_displayed_name(String owner_displayed_name) {
        this.owner_displayed_name = owner_displayed_name;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_image() {
        return group_image;
    }

    public void setGroup_image(String group_image) {
        this.group_image = group_image;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getLatest_message_id() {
        return latest_message_id;
    }

    public void setLatest_message_id(String latest_message_id) {
        this.latest_message_id = latest_message_id;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }


    public String getLatest_message_latitude() {
        return latest_message_latitude;
    }

    public void setLatest_message_latitude(String latest_message_latitude) {
        this.latest_message_latitude = latest_message_latitude;
    }

    public String getLatest_message_state() {
        return latest_message_state;
    }

    public void setLatest_message_state(String latest_message_state) {
        this.latest_message_state = latest_message_state;
    }

    public String getLatest_message_created() {
        return latest_message_created;
    }

    public void setLatest_message_created(String latest_message_created) {
        this.latest_message_created = latest_message_created;
    }

    public String getLatest_message_sender_phone() {
        return latest_message_sender_phone;
    }

    public void setLatest_message_sender_phone(String latest_message_sender_phone) {
        this.latest_message_sender_phone = latest_message_sender_phone;
    }

    public String getLatest_message_sender__displayed_name() {
        return latest_message_sender__displayed_name;
    }

    public void setLatest_message_sender__displayed_name(String latest_message_sender__displayed_name) {
        this.latest_message_sender__displayed_name = latest_message_sender__displayed_name;
    }

    public String getLatest_message_sender_id() {
        return latest_message_sender_id;
    }

    public void setLatest_message_sender_id(String latest_message_sender_id) {
        this.latest_message_sender_id = latest_message_sender_id;
    }

    public int getLatest_message_status() {
        return latest_message_status;
    }

    public void setLatest_message_status(int latest_message_status) {
        this.latest_message_status = latest_message_status;
    }

    @Override
    public String toString() {
        return "ConversationModel{" +
                "id=" + id +
                ", _id='" + _id + '\'' +
                ", created='" + created + '\'' +
                ", status=" + status +
                ", is_group=" + is_group +
                ", unread_message_counter=" + unread_message_counter +
                ", messageBeingComposed='" + messageBeingComposed + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", owner_image='" + owner_image + '\'' +
                ", owner_phone='" + owner_phone + '\'' +
                ", owner_displayed_name='" + owner_displayed_name + '\'' +
                ", group_id='" + group_id + '\'' +
                ", group_image='" + group_image + '\'' +
                ", group_name='" + group_name + '\'' +
                ", latest_message_id='" + latest_message_id + '\'' +
                ", latest_message='" + latest_message + '\'' +
                ", file_type='" + file_type + '\'' +
                ", latest_message_latitude='" + latest_message_latitude + '\'' +
                ", latest_message_state='" + latest_message_state + '\'' +
                ", latest_message_created='" + latest_message_created + '\'' +
                ", latest_message_status=" + latest_message_status +
                ", latest_message_sender_id='" + latest_message_sender_id + '\'' +
                ", latest_message_sender_phone='" + latest_message_sender_phone + '\'' +
                ", latest_message_sender__displayed_name='" + latest_message_sender__displayed_name + '\'' +
                '}';
    }
}

