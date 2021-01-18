package com.strolink.whatsUp.models.groups;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

/**
 * Created by Abderrahim El imame on 2019-09-12.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MembersModelResponse {



    private String _id;


    private String groupId;

    private boolean left;

    private boolean deleted;

    private boolean admin;


    private UsersModel owner;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
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

    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "MembersModelResponse{" +
                "_id='" + _id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", left=" + left +
                ", deleted=" + deleted +
                ", admin=" + admin +
                ", owner=" + owner +
                '}';
    }
}
