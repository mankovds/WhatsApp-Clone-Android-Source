package com.strolink.whatsUp.models.users.contacts;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Abderrahim El imame on 2019-08-02.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "users_blocked")
public class UsersBlockModel {


    @NonNull
    @PrimaryKey
    @SerializedName("_id")
    private String b_id;
    @Embedded
    private UsersModel usersModel;

    @NonNull
    public String getB_id() {
        return b_id;
    }

    public void setB_id(@NonNull String b_id) {
        this.b_id = b_id;
    }

    public UsersModel getUsersModel() {
        return usersModel;
    }

    public void setUsersModel(UsersModel usersModel) {
        this.usersModel = usersModel;
    }
}
