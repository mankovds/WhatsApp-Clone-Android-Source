package com.strolink.whatsUp.models.users.contacts;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

/**
 * Created by Abderrahim El imame on 2019-08-04.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "users_stories_privacy")
public class UsersPrivacyModel {

    @NonNull
    @PrimaryKey
    private String up_id;

    private boolean exclude;

    @Embedded
    private UsersModel usersModel;

    @NonNull
    public String getUp_id() {
        return up_id;
    }

    public void setUp_id(@NonNull String up_id) {
        this.up_id = up_id;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public UsersModel getUsersModel() {
        return usersModel;
    }

    public void setUsersModel(UsersModel usersModel) {
        this.usersModel = usersModel;
    }
}
