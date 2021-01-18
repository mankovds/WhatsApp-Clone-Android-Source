package com.strolink.whatsUp.models.groups;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.strolink.whatsUp.models.users.contacts.UsersModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "groups")
public class GroupModel {

    @NonNull
    @PrimaryKey
    private String _id;

    private String created;

    private String image;


    private String name;

    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "ownerId")
    private String ownerId;

    private String owner_phone;


/*
    @Embedded
    private UsersModel owner;nfkko*/



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
/*
    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }*/
/*
    public List<MembersModel> getMembers() {
        return members;
    }

    public void setMembers(List<MembersModel> members) {
        this.members = members;
    }*/

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
        return "GroupModel{" +
                "_id='" + _id + '\'' +
                ", created='" + created + '\'' +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", owner_phone='" + owner_phone + '\'' +
                '}';
    }
}
