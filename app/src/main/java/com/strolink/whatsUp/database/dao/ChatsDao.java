package com.strolink.whatsUp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.messages.ConversationModel;

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
public interface ChatsDao {

    @Query("SELECT  * FROM chats GROUP BY _id ORDER BY created DESC")
    Single<List<ConversationModel>> loadAllChats();


    @Query("SELECT  * FROM chats  WHERE  LOWER(owner_displayed_name) LIKE  LOWER('%' || :query  || '%')   ORDER BY created DESC")
    List<ConversationModel> loadAllChatsQuery(String query);

    @Query("SELECT * FROM chats  WHERE   unread_message_counter  != 0 ORDER BY created DESC")
    List<ConversationModel> loadAllUnreadChats();


    @Insert
    void insertAll(ConversationModel... conversationModels);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConversationModel conversationModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ConversationModel conversationModel);

    @Delete
    void delete(ConversationModel conversationModel);

    @Query("SELECT * FROM chats WHERE _id = :id")
    ConversationModel loadChatById(String id);


    @Query("SELECT * FROM chats WHERE owner_id = :id")
    ConversationModel loadChatByUserId(String id);

    @Query("SELECT _id FROM chats WHERE owner_id = :id")
    String getChatIdByUserId(String id);

    @Query("SELECT _id FROM chats WHERE group_id = :id")
    String getChatIdByGroupId(String id);

    @Query("SELECT * FROM chats WHERE group_id = :id")
    ConversationModel loadChatByGroupId(String id);

    @Query("SELECT COUNT(id) FROM  chats WHERE owner_id = :id ")
    int chatExistenceByUserId(String id);

    @Query("SELECT COUNT(id) FROM  chats WHERE group_id = :id ")
    int chatExistenceByGroupId(String id);

    @Query("SELECT COUNT(id) FROM  chats WHERE _id = :id ")
    int chatExistence(String id);

    @Query("SELECT COUNT(id) FROM chats  WHERE   unread_message_counter  != 0 ORDER BY created DESC")
    int loadAllUnreadChatCounter();
}
