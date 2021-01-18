package com.strolink.whatsUp.models.groups;

import androidx.annotation.NonNull;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

import java.util.List;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GroupModelResponse {

    private String _id;

    private String created;

    private String image;


    private String name;

    private UsersModel owner;

    private List<MembersModelResponse> members;

    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }

    public List<MembersModelResponse> getMembers() {
        return members;
    }

    public void setMembers(List<MembersModelResponse> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "GroupModelTmp{" +
                "_id='" + _id + '\'' +
                ", created='" + created + '\'' +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                ", members=" + members +
                '}';
    }
}
