package com.strolink.whatsUp.models.users.status;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Entity(tableName = "status")
public class StatusModel {

    @NonNull
    @PrimaryKey
    @SerializedName("_id")
    private String s_id;

    private String body;


    private String userId;


    private String created;

    private boolean current;

    private boolean is_default;

    public StatusModel() {
    }

    @NonNull
    public String getS_id() {
        return s_id;
    }

    public void setS_id(@NonNull String id) {
        this.s_id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
    }

    @Override
    public String toString() {
        return "StatusModel{" +
                "s_id='" + s_id + '\'' +
                ", body='" + body + '\'' +
                ", userId='" + userId + '\'' +
                ", created='" + created + '\'' +
                ", current=" + current +
                ", is_default=" + is_default +
                '}';
    }
}
