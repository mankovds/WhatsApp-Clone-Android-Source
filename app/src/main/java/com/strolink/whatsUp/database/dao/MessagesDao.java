package com.strolink.whatsUp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.messages.MessageModel;

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
public interface MessagesDao {

    @Query("SELECT * FROM messages WHERE senderId = :userId AND file_upload = :uploaded AND status = :status ORDER BY created ASC")
    Single<List<MessageModel>> loadAllMessages(int status, int uploaded, String userId);

    @Query("SELECT * FROM messages WHERE senderId = :recipientId OR recipientId = :recipientId AND is_group = 0 ORDER BY created ASC")
    Single<List<MessageModel>> loadAllMessagesByUserId(String recipientId);


    @Query("SELECT * FROM messages WHERE conversationId = :conversationId  AND is_group = 0 ORDER BY created ASC")
    Single<List<MessageModel>> loadAllMessagesByChatId(String conversationId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId  AND is_group = 0 ORDER BY created ASC")
    List<MessageModel> loadMessagesByChatId(String conversationId);


    @Query("SELECT * FROM messages WHERE conversationId = :conversationId  AND status =:status AND is_group = 0 ORDER BY created ASC")
    List<MessageModel> loadMessagesByChatId(String conversationId, int status);


    @Query("SELECT * FROM messages WHERE groupId = :groupId  ORDER BY created ASC")
    Single<List<MessageModel>> loadAllGroupMessages(String groupId);
//media

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId  AND file != 'null' AND file_type != :document AND file_upload = 1 AND file_downLoad = 1 AND is_group = :is_group ORDER BY created ASC")
    Single<List<MessageModel>> getUserMedia(String conversationId, String document, int is_group);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId  AND file != 'null' AND file_type = :document AND file_upload = 1 AND file_downLoad = 1 AND is_group = :is_group ORDER BY created ASC")
    Single<List<MessageModel>> getUserDocuments(String conversationId, String document, int is_group);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId   AND LOWER(message)  LIKE LOWER('%' || :query  || '%')  AND file_upload = 1 AND file_downLoad = 1 AND is_group = :is_group ORDER BY created ASC")
    Single<List<MessageModel>> getUserLinks(String conversationId, int is_group, String query);

    @Insert
    void insertAll(MessageModel... messageModels);

    @Insert
    void insert(MessageModel messageModel);

    @Update
    void update(MessageModel messageModel);

    @Delete
    void delete(MessageModel messageModel);


    @Query("SELECT * FROM messages WHERE _id = :id AND status = :status")
    MessageModel loadMessagesByIdAndStatus(String id, int status);

    @Query("SELECT * FROM messages WHERE _id = :id")
    MessageModel loadMessagesById(String id);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY created DESC LIMIT 1;")
    MessageModel getLastMessageById(String conversationId);

    @Query("SELECT * FROM messages WHERE id = :id")
    MessageModel getMessageByLongId(long id);

    @Query("SELECT * FROM messages WHERE senderId = :userId")
    MessageModel loadMessagesByUserId(String userId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND LOWER(message) LIKE  LOWER('%' || :query  || '%') ORDER BY created ASC")
    List<MessageModel> loadMessagesByQuery(String conversationId, String query);


    @Query("SELECT * FROM messages WHERE (senderId =:senderId AND recipientId= :recipientId OR (senderId=:recipientId   AND recipientId= :senderId))AND is_group = 0 AND LOWER(message) LIKE LOWER('%' || :query  || '%') ORDER BY created ASC")
    List<MessageModel> loadAllMessagesQuery(String recipientId, String senderId, String query);

    @Query("SELECT * FROM messages WHERE senderId =:senderId AND recipientId= :recipientId AND (status = 1 OR status = 2 ) AND is_group = 0   ORDER BY created ASC")
    List<MessageModel> getDeliveredMessages(String senderId, String recipientId);


    @Query("SELECT * FROM messages WHERE senderId !=:userId AND groupId= :groupId AND (status = 1 OR status = 2 ) AND is_group = 1   ORDER BY created ASC")
    List<MessageModel> getGroupDeliveredMessages(String userId, String groupId);

    @Query("SELECT * FROM messages WHERE senderId =:userId AND conversationId= :conversationId AND status =:status AND is_group = 0   ORDER BY created ASC")
    List<MessageModel> getMessages(String conversationId, String userId, int status);

    @Query("SELECT * FROM messages WHERE senderId !=:userId  AND status =:status AND is_group = :is_group   ORDER BY created ASC")
    List<MessageModel> getMessages(String userId, int status, int is_group);


    @Query("SELECT * FROM messages WHERE senderId =:senderId  AND recipientId =:recipientId  AND status != 3 AND is_group = 0   ORDER BY created ASC")
    List<MessageModel> getNotificationMessages(String senderId, String recipientId);

    @Query("SELECT * FROM messages WHERE senderId !=:userId AND conversationId= :conversationId AND groupId =:groupId AND status =:status AND is_group = 0   ORDER BY created ASC")
    List<MessageModel> getMessages(String conversationId, String userId, String groupId, int status);


    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND is_group = 0 AND LOWER(message) LIKE LOWER('%' || :query  || '%') ORDER BY created ASC")
    List<MessageModel> loadAllMessagesQuery(String conversationId, String query);

    @Query("SELECT COUNT(_id) FROM  messages WHERE _id = :id ")
    int messageExistence(String id);

    @Query("SELECT COUNT(_id) FROM  messages WHERE _id = :messageId AND status = :status ")
    int messageExistenceStatus(String messageId, int status);

    @Query("SELECT COUNT(_id) FROM  messages WHERE file = :hash ")
    int messageHashFileExistence(String hash);


    @Query("SELECT  COUNT(_id)  FROM messages WHERE senderId !=:userId  AND status != 3  ORDER BY created ASC")
    int getMessages(String userId);

}
