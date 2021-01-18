package com.strolink.whatsUp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Abderrahim El imame on 2019-08-05.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@Entity(tableName = "files_up_down_manager")
public class UploadInfo {

    @NonNull
    @PrimaryKey
    private String uploadId;

    @NonNull
    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(@NonNull String uploadId) {
        this.uploadId = uploadId;
    }
}
