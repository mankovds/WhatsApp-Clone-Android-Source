package com.strolink.whatsUp.models.users.contacts;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.strolink.whatsUp.models.users.status.StatusModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "users")
public class UsersModel {

    @NonNull
    @PrimaryKey
    private String _id;

    private int contactId;

    private String username;

    private String displayed_name;

    private String phone;

    private String phone_qurey;

    private boolean linked;

    private boolean activate;

    private boolean exist;

    private String image;

    private boolean connected;

    private String last_seen;

    @Embedded
    private StatusModel status;


    public UsersModel() {


    }

    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone_qurey() {
        return phone_qurey;
    }

    public void setPhone_qurey(String phone_qurey) {
        this.phone_qurey = phone_qurey;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public StatusModel getStatus() {
        return status;
    }

    public void setStatus(StatusModel status) {
        this.status = status;
    }

    public String getDisplayed_name() {
        return displayed_name;
    }

    public void setDisplayed_name(String displayed_name) {
        this.displayed_name = displayed_name;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

   public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }

    @Override
    public String toString() {
        return "UsersModel{" +
                "_id='" + _id + '\'' +
                ", contactId=" + contactId +
                ", username='" + username + '\'' +
                ", displayed_name='" + displayed_name + '\'' +
                ", phone='" + phone + '\'' +
                ", phone_qurey='" + phone_qurey + '\'' +
                ", linked=" + linked +
                ", activate=" + activate +
                ", exist=" + exist +
                ", image='" + image + '\'' +
                ", connected=" + connected +
                ", last_seen='" + last_seen + '\'' +
                ", status=" + status +
                '}';
    }
}