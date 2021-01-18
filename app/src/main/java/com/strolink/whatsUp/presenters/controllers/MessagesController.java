package com.strolink.whatsUp.presenters.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CustomNullException;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.messages.MessageStatus;
import com.strolink.whatsUp.models.notifications.NotificationsModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;

/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class MessagesController {

    private static volatile MessagesController Instance = null;


    public MessagesController() {
    }


    public static MessagesController getInstance() {

        MessagesController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MessagesController();
                }
            }
        }
        return localInstance;

    }


    public void updateConversationStatus(String messageId) {

        try {
            AppExecutors.getInstance().diskIO().execute(() -> {


                MessageModel messagesModel = MessagesController.getInstance().getMessageById(messageId);


                if (messagesModel != null) {
                    messagesModel.setStatus(AppConstants.IS_SEEN);
                    MessagesController.getInstance().updateMessage(messagesModel);

                    AppHelper.LogCat("RecipientMarkMessageAsSeen successfully");

                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(messagesModel.getConversationId());


                    if (conversationsModel1 != null) {
                        conversationsModel1.setUnread_message_counter(0);
                        MessagesController.getInstance().updateChat(conversationsModel1);

                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_READ, conversationsModel1.get_id()));
                        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
                    }
                } else {
                    AppHelper.LogCat("RecipientMarkMessageAsSeen failed ");
                }


            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no conversation unRead MessagesPresenter ");
        }
    }

    public boolean checkIfUserBlockedExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(UsersController.getInstance().userBlockedExistence(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public Boolean checkIfConversationExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().chatExistenceByUserId(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public Boolean checkIfChatConversationExist(String conversationId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().chatExistence(conversationId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public boolean checkIfGroupConversationExist(String groupId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().chatExistenceByGroupId(groupId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public boolean checkIfUserMessageStatusExist(String userId, String messageId, int status) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().userMessageStatusExistence(userId, messageId, status) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public int getUsersMessageStatusCounter(String messageId, int status) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().loadAllUsersMessageStatusCounter(messageId, status));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public void insertMessageInfo(MessageStatus messageStatus) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().insert(messageStatus));

    }


    public void updateMessageInfo(MessageStatus messageStatus) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().update(messageStatus));

    }


    public List<MessageStatus> getMessageInfo(String messageId) {

        try {

            return Observable.create((ObservableOnSubscribe<List<MessageStatus>>) subscriber -> {
                try {

                    List<MessageStatus> messageInfo = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().loadAllUsersMessageStatus(messageId);
                    List<MessageStatus> messageInfo2 = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messageStatusDao().loadAllUsersMessageStatus();
                    AppHelper.LogCat("messageInfo2 " + messageInfo2.toString());
                    if (messageInfo != null)
                        subscriber.onNext(messageInfo);
                    else
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }


    /**
     * method to update status as seen by sender (if recipient have been seen the message)  in realm database
     */
    public void updateSeenStatus(String messageId, String senderId, String recipientId, boolean is_group, JSONArray users) {


        if (is_group) {
            AppHelper.LogCat("Seen messageId " + messageId);

            for (int i = 0; i < users.length(); i++) {
                String recipientID;
                try {
                    recipientID = users.getString(i);
                    UsersModel usersModel = UsersController.getInstance().getUserById(recipientID);
                    MessageModel messagesModel = MessagesController.getInstance().getMessageById(messageId);
                    if (messagesModel != null) {
                        AppHelper.LogCat("Seen messagesModel exist");
                        if (!checkIfUserMessageStatusExist(users.getString(i), messageId, AppConstants.IS_SEEN)) {
                            MessageStatus messageStatus = new MessageStatus();
                            messageStatus.setMessageId(messageId);
                            messageStatus.setUsersModel(usersModel);
                            messageStatus.setStatus(AppConstants.IS_SEEN);
                            messageStatus.setSeenDate(UtilsTime.getCurrentISOTime());
                            insertMessageInfo(messageStatus);
                            WorkJobsManager.getInstance().updateMessage(messageId, recipientId, AppConstants.IS_SEEN);
                        }


                    } else {
                        AppHelper.LogCat("Seen failed ");
                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("messageId", messageId);

                            //emit by mqtt to other user
                            try {

                                WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } else {
            MessageModel messagesModel = MessagesController.getInstance().getMessageById(messageId);
            if (messagesModel != null) {
                messagesModel.setStatus(AppConstants.IS_SEEN);
                MessagesController.getInstance().updateMessage(messagesModel);
                AppHelper.LogCat("Seen successfully");

                ConversationModel conversationModel = MessagesController.getInstance().getChatById(messagesModel.getConversationId());
                if (conversationModel != null && conversationModel.getLatest_message_id().equals(messagesModel.get_id())) {

                    MessagesController.getInstance().changeLastMessageStatus(conversationModel, AppConstants.IS_SEEN);

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS, conversationModel.get_id()));

                }
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES, messageId));

                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("messageId", messageId);

                    updateMessage.put("is_group", false);
                    updateMessage.put("recipientId", recipientId);
                    //emit by mqtt to other user
                    try {

                        WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                AppHelper.LogCat("Seen failed ");
                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("messageId", messageId);

                    //emit by mqtt to other user
                    try {

                        WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    /**
     * method to update status for a specific  message (as delivered by sender) in realm database
     *
     * @param messageId this is parameter for  updateDeliveredStatus
     * @param senderId  this is parameter for  updateDeliveredStatus
     * @param is_group
     */
    public void updateDeliveredStatus(String messageId, String senderId, String recipientId, boolean is_group, JSONArray users) {

        AppHelper.LogCat("Delivered messageId " + messageId);

        if (is_group) {

            for (int i = 0; i < users.length(); i++) {
                String recipientID;
                try {
                    //  recipientID = users.getString(i);
                    // UsersModel usersModel = UsersController.getInstance().getUserById(recipientID);
                    MessageModel messagesModel = MessagesController.getInstance().getMessageById(messageId);
                    if (messagesModel != null) {

                        if (!checkIfUserMessageStatusExist(users.getString(i), messageId, AppConstants.IS_DELIVERED)) {
                            UsersModel usersModel = UsersController.getInstance().getUserById(users.getString(i));
                            MessageStatus messageStatus = new MessageStatus();
                            messageStatus.setMessageId(messageId);
                            messageStatus.setUsersModel(usersModel);
                            messageStatus.setStatus(AppConstants.IS_DELIVERED);
                            messageStatus.setDeliveredDate(String.valueOf(new DateTime()));

                            //date
                            MessagesController.getInstance().insertMessageInfo(messageStatus);
                            WorkJobsManager.getInstance().updateMessage(messageId, recipientId, AppConstants.IS_DELIVERED);

                        }


                    } else {
                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("messageId", messageId);
                            //emit by mqtt to other user
                            try {

                                WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                } catch (JSONException e) {
                    AppHelper.LogCat("Seen failed " + e.getMessage());
                }
            }
        } else {

            MessageModel messagesModel = MessagesController.getInstance().loadMessagesByIdAndStatus(messageId, AppConstants.IS_SENT);
            if (messagesModel != null) {
                messagesModel.setStatus(AppConstants.IS_DELIVERED);

                MessagesController.getInstance().updateMessage(messagesModel);

                AppHelper.LogCat("Delivered successfully");

                ConversationModel conversationModel = MessagesController.getInstance().getChatById(messagesModel.getConversationId());
                if (conversationModel != null && conversationModel.getLatest_message_id().equals(messagesModel.get_id())) {

                    MessagesController.getInstance().changeLastMessageStatus(conversationModel, AppConstants.IS_DELIVERED);

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS, messagesModel.getConversationId()));

                }
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES, messageId));

            } else {
                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("messageId", messageId);
                    //emit by mqtt to other user
                    try {

                        WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /*for update message status*/

    public boolean checkIfGroupExist(String groupId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(UsersController.getInstance().groupExistence(groupId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public boolean checkIfUserExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(UsersController.getInstance().userExistence(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    /**
     * method when a user change the image profile
     *
     * @param ownerId
     * @param isGroup
     */
    public void getNotifyForImageProfileChanged(String ownerId, String image, boolean isGroup) {


        if (isGroup) {
            if (checkIfGroupExist(ownerId)) {


                GroupModel groupModel = UsersController.getInstance().getGroupById(ownerId);
                groupModel.setImage(image);
                UsersController.getInstance().updateGroup(groupModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED, ownerId));
            }

        } else {
            if (checkIfUserExist(ownerId)) {

                UsersModel usersModel = UsersController.getInstance().getUserById(ownerId);
                if (usersModel != null) {
                    usersModel.setImage(image);
                    UsersController.getInstance().updateUser(usersModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_PROFILE_UPDATED, ownerId));

                }

            }
        }


    }


    public ConversationModel getChatById(String conversationId) {


        try {
            return Observable.create((ObservableOnSubscribe<ConversationModel>) subscriber -> {
                try {
                    ConversationModel conversationModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadChatById(conversationId);
                    if (conversationModel != null)
                        subscriber.onNext(conversationModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }


    }

    public String getChatIdByUserId(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                try {
                    String id = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().getChatIdByUserId(userId);
                    if (id != null)
                        subscriber.onNext(id);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public String getChatIdByGroupId(String groupId) {
        try {
            return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                try {
                    String id = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().getChatIdByGroupId(groupId);

                    if (id != null)
                        subscriber.onNext(id);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }
    }


    public ConversationModel getChatByGroupId(String groupId) {
        try {
            return Observable.create((ObservableOnSubscribe<ConversationModel>) subscriber -> {
                try {
                    ConversationModel conversationModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadChatByGroupId(groupId);

                    if (conversationModel != null)
                        subscriber.onNext(conversationModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public ConversationModel getChatByUserId(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<ConversationModel>) subscriber -> {
                try {
                    ConversationModel conversationModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadChatByUserId(userId);

                    if (conversationModel != null)
                        subscriber.onNext(conversationModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public List<MessageModel> loadMessagesByChatId(String conversationId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadMessagesByChatId(conversationId);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public Single<List<MessageModel>> getUserMedia(String conversationId, int is_group) {
        try {
            return Observable.create((ObservableOnSubscribe<Single<List<MessageModel>>>) subscriber -> {
                try {
                    Single<List<MessageModel>> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getUserMedia(conversationId, AppConstants.MESSAGES_DOCUMENT, is_group);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return Single.just(new ArrayList<>());
        }
    }

    public Single<List<MessageModel>> getUserDocuments(String conversationId, int is_group) {
        try {
            return Observable.create((ObservableOnSubscribe<Single<List<MessageModel>>>) subscriber -> {
                try {
                    Single<List<MessageModel>> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getUserDocuments(conversationId, AppConstants.MESSAGES_DOCUMENT, is_group);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return Single.just(new ArrayList<>());
        }

    }

    public Single<List<MessageModel>> getUserLinks(String conversationId, int is_group, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<Single<List<MessageModel>>>) subscriber -> {
                try {
                    Single<List<MessageModel>> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getUserLinks(conversationId, is_group, query);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return Single.just(new ArrayList<>());
        }

    }

    public List<MessageModel> loadMessagesByChatId(String conversationId, int status) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadMessagesByChatId(conversationId, status);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MessageModel> getMessagesByQuery(String conversationId, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadMessagesByQuery(conversationId, query);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public List<MessageModel> loadAllMessagesQuery(String recipientId, String senderId, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadAllMessagesQuery(recipientId, senderId, query);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<MessageModel> loadAllMessagesQuery(String conversationId, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadAllMessagesQuery(conversationId, query);

                    if (messageModels != null)
                        subscriber.onNext(messageModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public List<ConversationModel> loadAllChatsQuery(String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<ConversationModel>>) subscriber -> {
                try {
                    List<ConversationModel> conversationModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadAllChatsQuery(query);

                    if (conversationModels != null)
                        subscriber.onNext(conversationModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public int loadAllUnreadChatsCounter() {
        try {
            return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
                try {
                    int counter = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadAllUnreadChatCounter();


                    subscriber.onNext(counter);

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return 0;
        }
    }

    public List<ConversationModel> loadAllUnreadChats() {
        try {
            return Observable.create((ObservableOnSubscribe<List<ConversationModel>>) subscriber -> {
                try {
                    List<ConversationModel> conversationModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadAllUnreadChats();

                    if (conversationModels != null)
                        subscriber.onNext(conversationModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public void changeLastMessageStatus(ConversationModel conversationModel, int status) {

        conversationModel.setLatest_message_status(status);
        conversationModel.setStatus(status);
        updateChat(conversationModel);

    }

    public MessageModel getLastMessageById(String messageId) {
        try {
            return Observable.create((ObservableOnSubscribe<MessageModel>) subscriber -> {
                try {
                    MessageModel messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getLastMessageById(messageId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }


    }

    public MessageModel getMessageById(String messageId) {
        try {
            return Observable.create((ObservableOnSubscribe<MessageModel>) subscriber -> {
                try {
                    MessageModel messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadMessagesById(messageId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public List<MessageModel> getDeliverMessages(String senderId, String recipientId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getDeliveredMessages(senderId, recipientId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<MessageModel> getGroupDeliveredMessages(String userId, String groupId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getGroupDeliveredMessages(userId, groupId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<MessageModel> getMessages(String conversationId, String recipientId, int status) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getMessages(conversationId, recipientId, status);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<MessageModel> getMessages(String userId, int status, int is_group) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getMessages(userId, status, is_group);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }

    public int getMessagesBadge(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
                try {
                    int counter = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getMessages(userId);
                    subscriber.onNext(counter);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return 0;
        }


    }

    public List<MessageModel> getNotificationMessages(String senderId, String recipientId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getNotificationMessages(senderId, recipientId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<MessageModel> getMessages(String conversationId, String userId, String groupId, int status) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MessageModel>>) subscriber -> {
                try {
                    List<MessageModel> messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getMessages(conversationId, userId, groupId, status);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }


    }


    public MessageModel getMessageByLongId(long messageId) {
        try {
            return Observable.create((ObservableOnSubscribe<MessageModel>) subscriber -> {
                try {
                    MessageModel messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().getMessageByLongId(messageId);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }


    }

    public MessageModel loadMessagesByIdAndStatus(String messageId, int status) {
        try {
            return Observable.create((ObservableOnSubscribe<MessageModel>) subscriber -> {
                try {
                    MessageModel messageModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().loadMessagesByIdAndStatus(messageId, status);

                    if (messageModel != null)
                        subscriber.onNext(messageModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }


    }


    /**
     * method to save the incoming message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    public void saveNewUserMessage(JSONObject data, Context context) {


        try {
            //recipient object
            String recipientId = data.getJSONObject("recipient").getString("_id");
            String recipient_phone = data.getJSONObject("recipient").getString("phone");
            String recipient_name = data.getJSONObject("recipient").getString("username");
            String recipient_image = data.getJSONObject("recipient").getString("image");
            boolean recipient_activated = data.getJSONObject("recipient").getBoolean("activated");
            boolean recipient_linked = data.getJSONObject("recipient").getBoolean("linked");
            //sender object
            String senderId = data.getJSONObject("sender").getString("_id");
            String sender_phone = data.getJSONObject("sender").getString("phone");
            String sender_name = data.getJSONObject("sender").getString("username");
            String sender_image = data.getJSONObject("sender").getString("image");
            boolean sender_activated = data.getJSONObject("sender").getBoolean("activated");
            boolean sender_linked = data.getJSONObject("sender").getBoolean("linked");
            //message object
            String messageId = data.getString("_id");
            String messageBody = data.getString("message");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String conversationId = data.getString("conversationId");

            String latitude = data.getString("latitude");
            String longitude = data.getString("longitude");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");

            String fileSize = data.getString("file_size");
            String state = data.getString("state");
            int status = data.getInt("status");

            boolean reply_message = data.getBoolean("reply_message");
            String reply_id = data.getString("reply_id");
            String document_type = data.getString("document_type");
            String document_name = data.getString("document_name");

            if (senderId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (!file.equals("null") && checkIfFileHashMessageExist(file)) return;

            if (!MessagesController.getInstance().checkIfChatConversationExist(conversationId)) {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages

                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    UsersModel usersModelRecipient = UsersController.getInstance().getUserById(recipientId);
                    UsersModel usersModelSenderFinal;
                    if (usersModelSender == null) {
                        UsersModel usersModelSenderNew = new UsersModel();
                        usersModelSenderNew.set_id(senderId);
                        usersModelSenderNew.setPhone(sender_phone);
                        if (!sender_name.equals("null"))
                            usersModelSenderNew.setUsername(sender_name);
                        usersModelSenderNew.setDisplayed_name(sender_name);
                        usersModelSenderNew.setImage(sender_image);
                        usersModelSenderNew.setActivate(sender_activated);
                        usersModelSenderNew.setLinked(sender_linked);
                        usersModelSenderFinal = usersModelSenderNew;
                        UsersController.getInstance().insertUser(usersModelSenderNew);
                    } else {
                        usersModelSenderFinal = usersModelSender;
                    }

                    UsersModel usersModelRecipientFinal;
                    if (usersModelRecipient == null) {
                        UsersModel usersModelRecipientNew = new UsersModel();
                        usersModelRecipientNew.set_id(recipientId);
                        usersModelRecipientNew.setPhone(recipient_phone);
                        if (!recipient_name.equals("null"))
                            usersModelRecipientNew.setUsername(recipient_name);
                        usersModelRecipientNew.setDisplayed_name(sender_name);
                        usersModelRecipientNew.setImage(recipient_image);
                        usersModelRecipientNew.setActivate(recipient_activated);
                        usersModelRecipientNew.setLinked(recipient_linked);
                        usersModelRecipientFinal = usersModelRecipientNew;
                        UsersController.getInstance().insertUser(usersModelRecipientNew);
                    } else {
                        usersModelRecipientFinal = usersModelRecipient;
                    }
                    int unreadMessageCounter = 0;
                    unreadMessageCounter++;
                    //   String lastConversationID = RealmBackupRestore.getConversationLastId();
                    //  String lastID = RealmBackupRestore.getMessageLastId();
                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(messageId);


                    messagesModel.setSenderId(usersModelSenderFinal.get_id());
                    messagesModel.setSender_image(usersModelSenderFinal.getImage());
                    messagesModel.setSender_phone(usersModelSenderFinal.getPhone());


                    messagesModel.setRecipientId(usersModelRecipientFinal.get_id());
                    messagesModel.setRecipient_image(usersModelRecipientFinal.getImage());
                    messagesModel.setRecipient_phone(usersModelRecipientFinal.getPhone());

                    messagesModel.setCreated(created);
                    messagesModel.setStatus(status);
                    messagesModel.setIs_group(false);
                    messagesModel.setConversationId(conversationId);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setState(state);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);

                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);

                    messagesModel.setFile_upload(true);
                    if (!file.equals("null")) {
                        if (!longitude.equals("null")) {
                            messagesModel.setFile_downLoad(true);
                        } else {
                            messagesModel.setFile_downLoad(false);
                        }
                    } else {
                        messagesModel.setFile_downLoad(true);
                    }

                    if (!MessagesController.getInstance().insertIncomingMessage(messagesModel))
                        return;

                    ConversationModel conversationsModel1 = new ConversationModel();
                    conversationsModel1.set_id(conversationId);

                    conversationsModel1.setOwner_id(usersModelSenderFinal.get_id());
                    conversationsModel1.setOwner_image(usersModelSenderFinal.getImage());
                    conversationsModel1.setOwner_phone(usersModelSenderFinal.getPhone());

                    String displayed_name = UtilsPhone.getContactName(usersModelSenderFinal.getPhone());
                    conversationsModel1.setOwner_displayed_name(displayed_name);

                    conversationsModel1.setLatest_message_id(messagesModel.get_id());
                    conversationsModel1.setLatest_message(messagesModel.getMessage());
                    conversationsModel1.setFile_type(messagesModel.getFile_type());
                    conversationsModel1.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel1.setLatest_message_state(messagesModel.getState());
                    conversationsModel1.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel1.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel1.setLatest_message_sender_id(usersModelSenderFinal.get_id());
                    conversationsModel1.setLatest_message_sender_phone(usersModelSenderFinal.getPhone());

                    String name = UtilsPhone.getContactName(usersModelSenderFinal.getPhone());
                    conversationsModel1.setLatest_message_sender__displayed_name(name);
                    conversationsModel1.setCreated(created);
                    conversationsModel1.setIs_group(false);
                    conversationsModel1.setUnread_message_counter(unreadMessageCounter);

                    MessagesController.getInstance().insertChat(conversationsModel1);

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messageId, senderId, recipientId));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, conversationId));


                    String FileType = null;
                    if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                        FileType = "Image";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                        FileType = "Gif";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                        FileType = "Video";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                        FileType = "Audio";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                        FileType = "Document";
                    }


                    if (AppHelper.isActivityRunning(context, "activities.messages.MessagesActivity")) {
                        NotificationsModel notificationsModel = new NotificationsModel();
                        notificationsModel.setConversationID(conversationId);
                        notificationsModel.setFile(FileType);
                        notificationsModel.setGroup(false);
                        notificationsModel.setImage(sender_image);
                        notificationsModel.setPhone(sender_phone);
                        notificationsModel.setMessage(messageBody);
                        notificationsModel.setRecipientId(recipientId);
                        notificationsModel.setSenderId(senderId);
                        notificationsModel.setAppName(WhatsCloneApplication.getInstance().getPackageName());
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION, notificationsModel));
                    } else {
                        if (FileType != null) {
                            NotificationsManager.getInstance().showUserNotification(context, conversationId, sender_phone, file, senderId, sender_image);
                        } else {
                            NotificationsManager.getInstance().showUserNotification(context, conversationId, sender_phone, messageBody, senderId, sender_image);
                        }
                    }


                } else {

                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", recipientId);


                        try {

                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);


                            AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");


                        } catch (MqttException e) {
                            e.printStackTrace();
                            AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } else {


                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages


                    int unreadMessageCounter = 0;
                    // String lastID = RealmBackupRestore.getMessageLastId();


                    ConversationModel conversationsModel = MessagesController.getInstance().getChatById(conversationId);

                    unreadMessageCounter = conversationsModel.getUnread_message_counter();
                    unreadMessageCounter++;

                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    UsersModel usersModelRecipient = UsersController.getInstance().getUserById(recipientId);
                    UsersModel usersModelSenderFinal;
                    if (usersModelSender == null) {
                        UsersModel usersModelSenderNew = new UsersModel();
                        usersModelSenderNew.set_id(senderId);
                        usersModelSenderNew.setPhone(sender_phone);
                        if (!sender_name.equals("null"))
                            usersModelSenderNew.setUsername(sender_name);
                        usersModelSenderNew.setDisplayed_name(sender_name);
                        usersModelSenderNew.setImage(sender_image);
                        usersModelSenderNew.setActivate(sender_activated);
                        usersModelSenderNew.setLinked(sender_linked);
                        usersModelSenderFinal = usersModelSenderNew;
                        UsersController.getInstance().insertUser(usersModelSenderNew);
                    } else {
                        usersModelSenderFinal = usersModelSender;
                    }

                    UsersModel usersModelRecipientFinal;
                    if (usersModelRecipient == null) {
                        UsersModel usersModelRecipientNew = new UsersModel();
                        usersModelRecipientNew.set_id(recipientId);
                        usersModelRecipientNew.setPhone(recipient_phone);
                        if (!recipient_name.equals("null"))
                            usersModelRecipientNew.setUsername(recipient_name);
                        usersModelRecipientNew.setDisplayed_name(sender_name);
                        usersModelRecipientNew.setImage(recipient_image);
                        usersModelRecipientNew.setActivate(recipient_activated);
                        usersModelRecipientNew.setLinked(recipient_linked);
                        usersModelRecipientFinal = usersModelRecipientNew;
                        UsersController.getInstance().insertUser(usersModelRecipientNew);
                    } else {
                        usersModelRecipientFinal = usersModelRecipient;
                    }

                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(messageId);


                    messagesModel.setSenderId(usersModelSenderFinal.get_id());
                    messagesModel.setSender_image(usersModelSenderFinal.getImage());
                    messagesModel.setSender_phone(usersModelSenderFinal.getPhone());


                    messagesModel.setRecipientId(usersModelRecipientFinal.get_id());
                    messagesModel.setRecipient_image(usersModelRecipientFinal.getImage());
                    messagesModel.setRecipient_phone(usersModelRecipientFinal.getPhone());

                    messagesModel.setCreated(created);
                    messagesModel.setStatus(status);
                    messagesModel.setIs_group(false);
                    messagesModel.setConversationId(conversationId);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setState(state);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);

                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);

                    messagesModel.setFile_upload(true);
                    if (!file.equals("null")) {
                        if (!longitude.equals("null")) {
                            messagesModel.setFile_downLoad(true);
                        } else {
                            messagesModel.setFile_downLoad(false);
                        }

                    } else {
                        messagesModel.setFile_downLoad(true);
                    }


                    if (!MessagesController.getInstance().insertIncomingMessage(messagesModel))
                        return;


                    conversationsModel.setLatest_message_id(messagesModel.get_id());
                    conversationsModel.setLatest_message(messagesModel.getMessage());
                    conversationsModel.setFile_type(messagesModel.getFile_type());
                    conversationsModel.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel.setLatest_message_state(messagesModel.getState());
                    conversationsModel.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel.setLatest_message_sender_id(usersModelSenderFinal.get_id());
                    conversationsModel.setLatest_message_sender_phone(usersModelSenderFinal.getPhone());

                    String name = UtilsPhone.getContactName(usersModelSenderFinal.getPhone());
                    conversationsModel.setLatest_message_sender__displayed_name(name);
                    conversationsModel.setCreated(created);
                    conversationsModel.setUnread_message_counter(unreadMessageCounter);
                    MessagesController.getInstance().updateChat(conversationsModel);

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messageId, senderId, recipientId));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationId));

                    String FileType = null;
                    if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                        FileType = "Image";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                        FileType = "Gif";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                        FileType = "Video";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                        FileType = "Audio";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                        FileType = "Document";
                    }


                    if (AppHelper.isActivityRunning(context, "activities.messages.MessagesActivity")) {
                        NotificationsModel notificationsModel = new NotificationsModel();
                        notificationsModel.setConversationID(conversationId);
                        notificationsModel.setFile(FileType);
                        notificationsModel.setGroup(false);
                        notificationsModel.setImage(sender_image);
                        notificationsModel.setPhone(sender_phone);
                        notificationsModel.setMessage(messageBody);
                        notificationsModel.setRecipientId(recipientId);
                        notificationsModel.setSenderId(senderId);
                        notificationsModel.setAppName(WhatsCloneApplication.getInstance().getPackageName());
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION, notificationsModel));
                    } else {
                        if (FileType != null) {
                            NotificationsManager.getInstance().showUserNotification(context, conversationId, sender_phone, file, senderId, sender_image);
                        } else {
                            NotificationsManager.getInstance().showUserNotification(context, conversationId, sender_phone, messageBody, senderId, sender_image);
                        }
                    }
                } else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", recipientId);


                        try {

                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);


                            AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");


                        } catch (MqttException e) {
                            e.printStackTrace();
                            AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            AppHelper.runOnUIThread(() -> {
                new Handler().postDelayed(() -> {
                    WorkJobsManager.getInstance().sendDeliveredStatusToServer();
                }, 500);

            });
        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());

    }


    /**
     * method to save the incoming group message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    public void saveNewMessageGroup(JSONObject data, Context context) {


        try {
            //recipient object
            String groupId = data.getJSONObject("group").getString("_id");
            String group_name = data.getJSONObject("group").getString("name");
            String group_image = data.getJSONObject("group").getString("image");
            String group_owner = data.getJSONObject("group").getString("owner");
            //sender object
            String senderId = data.getJSONObject("sender").getString("_id");
            String sender_phone = data.getJSONObject("sender").getString("phone");
            String sender_name = data.getJSONObject("sender").getString("username");
            String sender_image = data.getJSONObject("sender").getString("image");
            boolean sender_activated = data.getJSONObject("sender").getBoolean("activated");
            boolean sender_linked = data.getJSONObject("sender").getBoolean("linked");
            //message object
            String messageId = data.getString("_id");
            String messageBody = data.getString("message");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String conversationId = data.getString("conversationId");

            String latitude = data.getString("latitude");
            String longitude = data.getString("longitude");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");

            String fileSize = data.getString("file_size");
            String state = data.getString("state");
            int status = data.getInt("status");

            boolean reply_message = data.getBoolean("reply_message");
            String reply_id = data.getString("reply_id");
            String document_type = data.getString("document_type");
            String document_name = data.getString("document_name");

            if (senderId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (!file.equals("null") && checkIfFileHashMessageExist(file)) return;

            if (!MessagesController.getInstance().checkIfGroupConversationExist(groupId)) {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages


                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    GroupModel groupModel = UsersController.getInstance().getGroupById(groupId);
                    UsersModel usersModelSenderFinal;
                    if (usersModelSender == null) {
                        UsersModel usersModelSenderNew = new UsersModel();
                        usersModelSenderNew.set_id(senderId);
                        usersModelSenderNew.setPhone(sender_phone);
                        if (!sender_name.equals("null"))
                            usersModelSenderNew.setUsername(sender_name);
                        usersModelSenderNew.setDisplayed_name(sender_name);
                        usersModelSenderNew.setImage(sender_image);
                        usersModelSenderNew.setActivate(sender_activated);
                        usersModelSenderNew.setLinked(sender_linked);
                        usersModelSenderFinal = usersModelSenderNew;
                        UsersController.getInstance().insertUser(usersModelSenderNew);
                    } else {
                        usersModelSenderFinal = usersModelSender;
                    }

                    GroupModel groupModelFinal;
                    if (groupModel == null) {
                        GroupModel groupModelNew = new GroupModel();
                        groupModelNew.set_id(groupId);
                        if (!group_name.equals("null"))
                            groupModelNew.setName(group_name);
                        groupModelNew.setImage(group_image);
                        //  groupModelNew.setOwner(recipient_activated);
                        groupModelFinal = groupModelNew;
                        UsersController.getInstance().insertGroup(groupModelNew);
                    } else {
                        groupModelFinal = groupModel;
                    }
                    int unreadMessageCounter = 0;
                    unreadMessageCounter++;

                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(messageId);

                    messagesModel.setGroupId(groupModelFinal.get_id());

                    messagesModel.setGroup_image(groupModelFinal.getImage());
                    messagesModel.setGroup_name(groupModelFinal.getName());
                    messagesModel.setSenderId(usersModelSenderFinal.get_id());
                    messagesModel.setSender_image(usersModelSenderFinal.getImage());
                    messagesModel.setSender_phone(usersModelSenderFinal.getPhone());

                    messagesModel.setCreated(created);
                    messagesModel.setStatus(status);
                    messagesModel.setIs_group(true);
                    messagesModel.setConversationId(conversationId);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setState(state);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);

                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);

                    messagesModel.setFile_upload(true);
                    if (!file.equals("null")) {
                        if (!longitude.equals("null")) {
                            messagesModel.setFile_downLoad(true);
                        } else {
                            messagesModel.setFile_downLoad(false);
                        }

                    } else {
                        messagesModel.setFile_downLoad(true);
                    }


                    if (!MessagesController.getInstance().insertIncomingMessage(messagesModel))
                        return;

                    ConversationModel conversationsModel1 = new ConversationModel();
                    conversationsModel1.set_id(conversationId);


                    conversationsModel1.setGroup_id(groupModelFinal.get_id());
                    conversationsModel1.setGroup_image(groupModelFinal.getImage());
                    conversationsModel1.setGroup_name(groupModelFinal.getName());

                    conversationsModel1.setIs_group(true);

                    conversationsModel1.setLatest_message_id(messagesModel.get_id());
                    conversationsModel1.setLatest_message(messagesModel.getMessage());
                    conversationsModel1.setFile_type(messagesModel.getFile_type());
                    conversationsModel1.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel1.setLatest_message_state(messagesModel.getState());
                    conversationsModel1.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel1.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel1.setLatest_message_sender_id(usersModelSenderFinal.get_id());
                    conversationsModel1.setLatest_message_sender_phone(usersModelSenderFinal.getPhone());

                    String name = UtilsPhone.getContactName(usersModelSenderFinal.getPhone());
                    conversationsModel1.setLatest_message_sender__displayed_name(name);
                    conversationsModel1.setCreated(created);


                    conversationsModel1.setUnread_message_counter(unreadMessageCounter);

                    MessagesController.getInstance().insertChat(conversationsModel1);

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW, messagesModel));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, conversationId));


                    String FileType = null;
                    if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                        FileType = "Image";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                        FileType = "Gif";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                        FileType = "Video";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                        FileType = "Audio";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                        FileType = "Document";
                    }


                    String memberName;
                    String name_member = UtilsPhone.getContactName(sender_phone);
                    if (name_member != null) {
                        memberName = name_member;
                    } else {
                        memberName = sender_phone;
                    }


                    String message;
                    String userName = UtilsPhone.getContactName(sender_phone);
                    switch (state) {
                        case AppConstants.CREATE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_created_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_created_this_group);
                            }


                            break;
                        case AppConstants.LEFT_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_left);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_left);
                            }


                            break;
                        case AppConstants.ADD_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_added_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_added_this_group);
                            }


                            break;

                        case AppConstants.REMOVE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_removed_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_removed_this_group);
                            }


                            break;
                        case AppConstants.ADMIN_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_make_admin_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_make_admin_this_group);
                            }


                            break;
                        case AppConstants.MEMBER_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_make_member_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_make_member_this_group);
                            }


                            break;
                        case AppConstants.EDITED_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_edited_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_edited_this_group);
                            }


                            break;
                        default:
                            message = messageBody;
                            break;
                    }

                    /**
                     * this for default activity
                     */
                    Intent messagingGroupIntent = new Intent(context, MessagesActivity.class);
                    messagingGroupIntent.putExtra("conversationID", conversationId);
                    messagingGroupIntent.putExtra("groupID", groupId);
                    messagingGroupIntent.putExtra("isGroup", true);
                    messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    /**
                     * this for popup activity
                     */
                    Intent messagingGroupPopupIntent = new Intent(context, MessagesActivity.class);
                    messagingGroupPopupIntent.putExtra("conversationID", conversationId);
                    messagingGroupPopupIntent.putExtra("groupID", groupId);
                    messagingGroupPopupIntent.putExtra("isGroup", true);
                    messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (AppHelper.isActivityRunning(context, "activities.messages.MessagesActivity")) {
                        NotificationsModel notificationsModel = new NotificationsModel();
                        notificationsModel.setConversationID(conversationId);
                        notificationsModel.setFile(FileType);
                        notificationsModel.setGroup(true);
                        notificationsModel.setImage(group_image);
                        notificationsModel.setPhone(sender_phone);
                        notificationsModel.setMessage(messageBody);
                        notificationsModel.setState(state);
                        notificationsModel.setMemberName(memberName);
                        notificationsModel.setGroupID(groupId);
                        notificationsModel.setGroupName(group_name);
                        notificationsModel.setAppName(context.getPackageName());
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION, notificationsModel));
                    } else {
                        if (FileType != null) {
                            NotificationsManager.getInstance().showGroupNotification(context, messagingGroupIntent, messagingGroupPopupIntent, group_name, memberName + " : " + FileType, groupId, group_image);
                        } else {
                            NotificationsManager.getInstance().showGroupNotification(context, messagingGroupIntent, messagingGroupPopupIntent, group_name, memberName + " : " + message, groupId, group_image);
                        }
                    }


                } else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));


                        try {

                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);


                            AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");


                        } catch (MqttException e) {
                            e.printStackTrace();
                            AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages


                    int unreadMessageCounter = 0;
                    // String lastID = RealmBackupRestore.getMessageLastId();

                    ConversationModel conversationsModel = MessagesController.getInstance().getChatById(conversationId);
                    unreadMessageCounter = conversationsModel.getUnread_message_counter();
                    unreadMessageCounter++;

                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    GroupModel groupModel = UsersController.getInstance().getGroupById(groupId);
                    UsersModel usersModelSenderFinal;
                    if (usersModelSender == null) {
                        UsersModel usersModelSenderNew = new UsersModel();
                        usersModelSenderNew.set_id(senderId);
                        usersModelSenderNew.setPhone(sender_phone);
                        if (!sender_name.equals("null"))
                            usersModelSenderNew.setUsername(sender_name);
                        usersModelSenderNew.setDisplayed_name(sender_name);
                        usersModelSenderNew.setImage(sender_image);
                        usersModelSenderNew.setActivate(sender_activated);
                        usersModelSenderNew.setLinked(sender_linked);
                        usersModelSenderFinal = usersModelSenderNew;
                        UsersController.getInstance().insertUser(usersModelSenderNew);
                    } else {
                        usersModelSenderFinal = usersModelSender;
                    }

                    GroupModel groupModelFinal;
                    if (groupModel == null) {
                        GroupModel groupModelNew = new GroupModel();
                        groupModelNew.set_id(groupId);
                        if (!group_name.equals("null"))
                            groupModelNew.setName(group_name);
                        groupModelNew.setImage(group_image);
                        //   groupModelNew.setOwner(group_owner);
                        groupModelFinal = groupModelNew;
                        UsersController.getInstance().insertGroup(groupModelNew);
                    } else {
                        groupModelFinal = groupModel;
                    }

                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(messageId);

                    messagesModel.setGroupId(groupModelFinal.get_id());
                    messagesModel.setGroup_image(groupModelFinal.getImage());
                    messagesModel.setGroup_name(groupModelFinal.getName());
                    messagesModel.setSenderId(usersModelSenderFinal.get_id());
                    messagesModel.setSender_image(usersModelSenderFinal.getImage());
                    messagesModel.setSender_phone(usersModelSenderFinal.getPhone());

                    messagesModel.setCreated(created);
                    messagesModel.setStatus(status);
                    messagesModel.setIs_group(true);
                    messagesModel.setConversationId(conversationId);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setState(state);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);

                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);

                    messagesModel.setFile_upload(true);
                    if (!file.equals("null")) {

                        if (!longitude.equals("null")) {
                            messagesModel.setFile_downLoad(true);
                        } else {
                            messagesModel.setFile_downLoad(false);
                        }

                    } else {
                        messagesModel.setFile_downLoad(true);
                    }

                    if (!MessagesController.getInstance().insertIncomingMessage(messagesModel))
                        return;

                    conversationsModel.set_id(conversationId);

                    conversationsModel.setLatest_message_id(messagesModel.get_id());
                    conversationsModel.setLatest_message(messagesModel.getMessage());
                    conversationsModel.setFile_type(messagesModel.getFile_type());
                    conversationsModel.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel.setLatest_message_state(messagesModel.getState());
                    conversationsModel.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel.setLatest_message_sender_id(usersModelSenderFinal.get_id());
                    conversationsModel.setLatest_message_sender_phone(usersModelSenderFinal.getPhone());

                    String name = UtilsPhone.getContactName(usersModelSenderFinal.getPhone());
                    conversationsModel.setLatest_message_sender__displayed_name(name);

                    conversationsModel.setCreated(created);


                    conversationsModel.setGroup_id(groupModelFinal.get_id());
                    conversationsModel.setGroup_image(groupModelFinal.getImage());
                    conversationsModel.setGroup_name(groupModelFinal.getName());
                    conversationsModel.setUnread_message_counter(unreadMessageCounter);


                    MessagesController.getInstance().updateChat(conversationsModel);


                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW, messagesModel));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationId));

                    String FileType = null;
                    if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                        FileType = "Image";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                        FileType = "Gif";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                        FileType = "Video";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                        FileType = "Audio";
                    } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                        FileType = "Document";
                    }


                    String memberName;
                    String name_member = UtilsPhone.getContactName(sender_phone);
                    if (name_member != null) {
                        memberName = name_member;
                    } else {
                        memberName = sender_phone;
                    }


                    String message;
                    String userName = UtilsPhone.getContactName(sender_phone);
                    switch (state) {
                        case AppConstants.CREATE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_created_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_created_this_group);
                            }


                            break;
                        case AppConstants.LEFT_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_left);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_left);
                            }


                            break;
                        case AppConstants.ADD_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_added_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_added_this_group);
                            }


                            break;

                        case AppConstants.REMOVE_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_removed_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_removed_this_group);
                            }


                            break;
                        case AppConstants.ADMIN_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_make_admin_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_make_admin_this_group);
                            }


                            break;
                        case AppConstants.MEMBER_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_make_member_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_make_member_this_group);
                            }


                            break;
                        case AppConstants.EDITED_STATE:
                            if (userName != null) {
                                message = "" + userName + " " + context.getString(R.string.he_edited_this_group);
                            } else {
                                message = "" + sender_phone + " " + context.getString(R.string.he_edited_this_group);
                            }


                            break;
                        default:
                            message = messageBody;
                            break;
                    }

                    /**
                     * this for default activity
                     */
                    Intent messagingGroupIntent = new Intent(context, MessagesActivity.class);
                    messagingGroupIntent.putExtra("conversationID", conversationId);
                    messagingGroupIntent.putExtra("groupID", groupId);
                    messagingGroupIntent.putExtra("isGroup", true);
                    messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    /**
                     * this for popup activity
                     */
                    Intent messagingGroupPopupIntent = new Intent(context, MessagesActivity.class);
                    messagingGroupPopupIntent.putExtra("conversationID", conversationId);
                    messagingGroupPopupIntent.putExtra("groupID", groupId);
                    messagingGroupPopupIntent.putExtra("isGroup", true);
                    messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    if (AppHelper.isActivityRunning(context, "activities.messages.MessagesActivity")) {
                        NotificationsModel notificationsModel = new NotificationsModel();
                        notificationsModel.setConversationID(conversationId);
                        notificationsModel.setFile(FileType);
                        notificationsModel.setGroup(true);
                        notificationsModel.setImage(group_image);
                        notificationsModel.setPhone(sender_phone);
                        notificationsModel.setMessage(messageBody);
                        notificationsModel.setState(state);
                        notificationsModel.setMemberName(memberName);
                        notificationsModel.setGroupID(groupId);
                        notificationsModel.setGroupName(group_name);
                        notificationsModel.setAppName(context.getPackageName());
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION, notificationsModel));
                    } else {
                        if (FileType != null) {
                            NotificationsManager.getInstance().showGroupNotification(context, messagingGroupIntent, messagingGroupPopupIntent, group_name, memberName + " : " + FileType, groupId, group_image);
                        } else {
                            NotificationsManager.getInstance().showGroupNotification(context, messagingGroupIntent, messagingGroupPopupIntent, group_name, memberName + " : " + message, groupId, group_image);
                        }
                    }
                } else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                        try {

                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);


                            AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");


                        } catch (MqttException e) {
                            e.printStackTrace();
                            AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

            AppHelper.runOnUIThread(() -> {
                new Handler().postDelayed(() -> {
                    WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
                }, 500);
            });


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }

        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
    }


    public Boolean checkIfMessageExist(String messageId) {


        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().messageExistence(messageId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public boolean checkIfMessageExistByStatus(String messageId, int status) {


        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().messageExistenceStatus(messageId, status) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();


    }

    public boolean checkIfFileHashMessageExist(String hash) {


        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().messageHashFileExistence(hash) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();


    }


    public void sendMessageGroupActions(String groupID, String created, String state) {

        AppExecutors.getInstance().diskIO().execute(() -> {


            String lastID = DbBackupRestore.getMessageLastId();

            UsersModel usersModelSender = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

            GroupModel groupModel = UsersController.getInstance().getGroupById(groupID);

            ConversationModel conversationsModel = MessagesController.getInstance().getChatByGroupId(groupID);

            MessageModel messagesModel = new MessageModel();
            messagesModel.set_id(lastID);
            messagesModel.setCreated(created);
            messagesModel.setStatus(AppConstants.IS_WAITING);
            messagesModel.setGroupId(groupModel.get_id());
            messagesModel.setGroup_image(groupModel.getImage());
            messagesModel.setGroup_name(groupModel.getName());
            messagesModel.setSenderId(usersModelSender.get_id());
            messagesModel.setSender_image(usersModelSender.getImage());
            messagesModel.setSender_phone(usersModelSender.getPhone());
            messagesModel.setIs_group(true);
            messagesModel.setMessage("null");
            messagesModel.setLatitude("null");
            messagesModel.setLongitude("null");
            messagesModel.setFile("null");
            messagesModel.setFile_type("null");
            messagesModel.setState(state);
            messagesModel.setFile_size("0");
            messagesModel.setDuration_file("0");
            messagesModel.setReply_id("null");
            messagesModel.setReply_message(true);
            messagesModel.setDocument_name("null");
            messagesModel.setDocument_type("null");
            messagesModel.setFile_upload(true);
            messagesModel.setFile_downLoad(true);
            messagesModel.setConversationId(conversationsModel.get_id());

            MessagesController.getInstance().insertMessage(messagesModel);


            conversationsModel.setLatest_message_id(messagesModel.get_id());
            conversationsModel.setLatest_message(messagesModel.getMessage());
            conversationsModel.setFile_type(messagesModel.getFile_type());
            conversationsModel.setLatest_message_latitude(messagesModel.getLatitude());
            conversationsModel.setLatest_message_state(messagesModel.getState());
            conversationsModel.setLatest_message_created(messagesModel.getCreated());
            conversationsModel.setLatest_message_status(messagesModel.getStatus());
            conversationsModel.setLatest_message_sender_id(usersModelSender.get_id());
            conversationsModel.setLatest_message_sender_phone(usersModelSender.getPhone());
            String name = UtilsPhone.getContactName(usersModelSender.getPhone());
            conversationsModel.setLatest_message_sender__displayed_name(name);
            conversationsModel.setGroup_name(groupModel.getName());
            conversationsModel.setGroup_image(groupModel.getImage());
            conversationsModel.setCreated(created);
            conversationsModel.setUnread_message_counter(0);

            MessagesController.getInstance().updateChat(conversationsModel);


            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationsModel.get_id()));
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REFRESH_MESSAGEGS));

            WorkJobsManager.getInstance().sendUserMessagesToServer();


        });
    }


    public void insertMessage(MessageModel messageModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {


                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().insert(messageModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }

    public boolean insertIncomingMessage(MessageModel messageModel) {


        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {
                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().insert(messageModel);
                subscriber.onNext(true);
                subscriber.onComplete();
            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
                subscriber.onNext(false);
                subscriber.onComplete();
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();


    }

    public void updateMessage(MessageModel messageModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {

                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().update(messageModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }

    public void deleteMessage(MessageModel messageModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {

                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).messagesDao().delete(messageModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }

    public void updateChat(ConversationModel conversationModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {

                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().update(conversationModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }

    public void insertChat(ConversationModel conversationModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {

                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().insert(conversationModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }

    public void deleteChat(ConversationModel conversationModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {

                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().delete(conversationModel);

            } catch (Exception e) {
                AppHelper.LogCat("Exception duplicate " + e.getMessage());
            }
        });
    }


    public void unSentMessages() {

        AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                .messagesDao()
                .loadAllMessages(AppConstants.IS_WAITING, 1, PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                .subscribeOn(Schedulers.computation())
                .subscribe(messagesModelsList -> {
                    AppHelper.LogCat("Job unSentMessages: " + messagesModelsList.size());
                    if (messagesModelsList.size() > 0) {
                        AppHelper.LogCat("messagesModelsList jjb: " + messagesModelsList.size());

                        for (MessageModel messageModel : messagesModelsList) {
                            WorkJobsManager.getInstance().sendSingleMessageToServerWorker(messageModel.get_id());

                        }


                    }
                });

    }


}