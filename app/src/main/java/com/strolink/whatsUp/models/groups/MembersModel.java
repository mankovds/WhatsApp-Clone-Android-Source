package com.strolink.whatsUp.models.groups;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

/**
 * Created by Abderrahim El imame on 2019-07-27.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@Entity(tableName = "members")
public class MembersModel {


    @NonNull
    @PrimaryKey
    private String _id;


    @ForeignKey(entity = GroupModel.class, parentColumns = "_id", childColumns = "groupId")
    private String groupId;

    private boolean left;

    private boolean deleted;

    private boolean admin;


    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "ownerId")
    private String ownerId;
    private String owner_phone;


    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwner_phone() {
        return owner_phone;
    }

    public void setOwner_phone(String owner_phone) {
        this.owner_phone = owner_phone;
    }

    @Override
    public String toString() {
        return "MembersModel{" +
                "_id='" + _id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", left=" + left +
                ", deleted=" + deleted +
                ", admin=" + admin +
                ", ownerId='" + ownerId + '\'' +
                ", owner_phone='" + owner_phone + '\'' +
                '}';
    }
}
