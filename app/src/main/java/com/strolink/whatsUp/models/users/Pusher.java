package com.strolink.whatsUp.models.users;

import android.view.View;

import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.notifications.NotificationsModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;


import org.json.JSONObject;

import java.util.List;

/**
 * Created by Abderrahim El imame on 04/05/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class Pusher {
    private String action;
    private View view;
    private String data;
    private String title;
    private boolean bool;
    private UsersModel contactsModel;
    private List<MessageModel> messagesModelList;
    private JSONObject jsonObject;
    private MessageModel messagesModel;
    private String callId;
    private ConversationModel conversationsModel;
    private String groupID;
    private String ownerID;
    private String statusID;
    private String userID;
    private String recipientID;
    private String senderID;
    private String conversationId;
    private String last_seen;
    private String storyId;
    private NotificationsModel notificationsModel;

    private String messageId;

    public Pusher(String action) {
        this.action = action;
    }


    public Pusher(String action, String data) {
        this.action = action;
        this.data = data;

        this.messageId = data;
        this.groupID = data;
        this.ownerID = data;
        this.conversationId = data;
        this.storyId = data;
        this.callId = data;
        this.statusID = data;
    }

    public Pusher(String action, String messageId, String senderID, String recipientID) {
        this.action = action;
        this.messageId = messageId;
        this.recipientID = recipientID;
        this.senderID = senderID;
    }

    public Pusher(String action, String data, String title) {
        this.action = action;
        this.data = data;
        this.title = title;
        this.userID = title;
        this.conversationId = title;
        this.groupID = data;
        this.recipientID = data;
        this.senderID = title;
        this.last_seen = title;
    }


    public Pusher(String action, String data, Boolean bool) {
        this.action = action;
        this.data = data;
        this.bool = bool;
    }

    public Pusher(String action, List<MessageModel> messagesModelList) {
        this.action = action;
        this.messagesModelList = messagesModelList;
    }

    public Pusher(String action, UsersModel contactsModel) {
        this.action = action;
        this.contactsModel = contactsModel;
    }


    public Pusher(String action, NotificationsModel notificationsModel) {
        this.action = action;
        this.notificationsModel = notificationsModel;
    }

    public Pusher(String action, JSONObject jsonObject) {
        this.action = action;
        this.jsonObject = jsonObject;
    }

    public Pusher(String action, MessageModel messagesModel) {
        this.action = action;
        this.messagesModel = messagesModel;
    }


    public Pusher(String action, ConversationModel conversationsModel) {
        this.action = action;
        this.conversationsModel = conversationsModel;
    }

    public Pusher(String action, String groupID, MessageModel messagesModel) {
        this.action = action;
        this.messagesModel = messagesModel;
        this.groupID = groupID;
    }

    public Pusher(String itemIsActivated, View view) {
        this.action = itemIsActivated;
        this.view = view;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatusID() {
        return statusID;
    }

    public void setStatusID(String statusID) {
        this.statusID = statusID;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }


    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public NotificationsModel getNotificationsModel() {
        return notificationsModel;
    }

    public void setNotificationsModel(NotificationsModel notificationsModel) {
        this.notificationsModel = notificationsModel;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }


    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }


    public MessageModel getMessagesModel() {
        return messagesModel;
    }

    public void setMessagesModel(MessageModel messagesModel) {
        this.messagesModel = messagesModel;
    }


    public ConversationModel getConversationsModel() {
        return conversationsModel;
    }

    public void setConversationsModel(ConversationModel conversationsModel) {
        this.conversationsModel = conversationsModel;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }


    public List<MessageModel> getMessagesModelList() {
        return messagesModelList;
    }

    public void setMessagesModelList(List<MessageModel> messagesModelList) {
        this.messagesModelList = messagesModelList;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public String getAction() {
        return action;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public UsersModel getContactsModel() {
        return contactsModel;
    }

    public void setContactsModel(UsersModel contactsModel) {
        this.contactsModel = contactsModel;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
