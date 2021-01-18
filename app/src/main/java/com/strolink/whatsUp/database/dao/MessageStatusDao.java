package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.messages.MessageStatus;

import java.util.List;

import io.reactivex.Single;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Dao
public interface MessageStatusDao {

    @Query("SELECT * FROM message_status WHERE messageId = :messageId  ORDER BY id ASC ")
     List<MessageStatus> loadAllUsersMessageStatus(String messageId);

    @Query("SELECT * FROM message_status   ORDER BY id ASC ")
    List<MessageStatus> loadAllUsersMessageStatus();


    @Query("SELECT * FROM message_status WHERE  username LIKE '%' || :query  || '%' ORDER BY id ASC  ")
    List<MessageStatus> loadAllUsersMessageStatusQuery(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MessageStatus messageStatus);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MessageStatus... messageStatus);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MessageStatus messageStatus);

    @Delete
    void delete(MessageStatus messageStatus);

    @Query("SELECT * FROM message_status WHERE _id = :id")
    Single<MessageStatus> loadUsersMessageStatusById(String id);

    @Query("SELECT * FROM message_status WHERE _id = :id")
    MessageStatus loadSingleUsersMessageStatusById(String id);

    @Query("SELECT _id FROM message_status WHERE _id = :id  ")
    String loadSingleUsersMessageStatusId(String id);

    @Query("SELECT COUNT(id) FROM  message_status WHERE _id = :id ")
    int messageStatusExistence(String id);

    @Query("SELECT COUNT(id) FROM  message_status WHERE _id =:userId  AND  messageId = :messageId AND  status =:status")
    int userMessageStatusExistence(String userId, String messageId, int status);

    @Query("SELECT COUNT(id) FROM message_status WHERE messageId = :messageId AND status =:status ORDER BY id ASC ")
    int loadAllUsersMessageStatusCounter(String messageId, int status);
}