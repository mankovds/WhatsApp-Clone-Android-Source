package com.strolink.whatsUp.models.stories;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.RoomWarnings;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

/**
 * Created by Abderrahim El imame on 2019-08-04.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "story_seen")
public class StorySeen {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ForeignKey(entity = StoryModel.class, parentColumns = "_id", childColumns = "storyId")
    private String storyId;

    @ForeignKey(entity = UsersModel.class, parentColumns = "_id", childColumns = "userId")
    private String userId;


    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
