package com.strolink.whatsUp.api.apiServices;


import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesService {


    public MessagesService() {


    }

    /**
     * method to get all conversation messages
     *
     * @param conversationID this is the first parameter for getChatByGroupId method
     * @return return value
     */
    public Single<List<MessageModel>> getUserChat(String conversationID) {
        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadAllMessagesByChatId(conversationID).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get messages list from local
     *
     * @param groupId this is parameter for getChatByGroupId method
     * @return return value
     */
    public Single<List<MessageModel>> getGroupChat(String groupId) {
        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadAllGroupMessages(groupId).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getUserMedia(String recipientID, String senderID) {

        String conversationId = MessagesController.getInstance().getChatIdByUserId(recipientID);
        return MessagesController.getInstance().getUserMedia(conversationId, 0).subscribeOn(Schedulers.computation());

    }


    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getGroupMedia(String groupID) {
        String conversationId = MessagesController.getInstance().getChatIdByGroupId(groupID);
        return MessagesController.getInstance().getUserMedia(conversationId, 1).subscribeOn(Schedulers.computation());

    }


    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getUserDocuments(String recipientID, String senderID) {


        String conversationId = MessagesController.getInstance().getChatIdByUserId(recipientID);
        return MessagesController.getInstance().getUserDocuments(conversationId, 0).subscribeOn(Schedulers.computation());

    }

    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getGroupDocuments(String groupID) {
        String conversationId = MessagesController.getInstance().getChatIdByGroupId(groupID);
        return MessagesController.getInstance().getUserDocuments(conversationId, 1).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getUserLinks(String recipientID, String senderID) {
        String conversationId = MessagesController.getInstance().getChatIdByUserId(recipientID);
        return MessagesController.getInstance().getUserLinks(conversationId, 0, "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$").subscribeOn(Schedulers.computation());

    }

    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Single<List<MessageModel>> getGroupLinks(String groupID) {

        String conversationId = MessagesController.getInstance().getChatIdByGroupId(groupID);
        return MessagesController.getInstance().getUserLinks(conversationId, 1, "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$").subscribeOn(Schedulers.computation());

    }


}
