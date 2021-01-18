package com.strolink.whatsUp.jobs.messages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.messages.MessageStatus;
import com.strolink.whatsUp.models.messages.UpdateMessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.status.StatusResponse;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 2019-07-16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SendSingleMessageToServerWorker extends ListenableWorker {


    public static final String TAG = SendSingleMessageToServerWorker.class.getSimpleName();

    private SettableFuture<Result> mFuture;
    private CompositeDisposable compositeDisposable;

     public SendSingleMessageToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

     }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        compositeDisposable = new CompositeDisposable();
        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {


                    String messageId = getInputData().getString("messageId");

                    MessageModel messageModel = MessagesController.getInstance().getMessageById(messageId);
                    if (messageModel != null) {
                        AppHelper.LogCat("messageModel: " + "jobStarted");


                        UpdateMessageModel updateMessageModel = new UpdateMessageModel();
                        updateMessageModel.setSenderId(messageModel.getSenderId());
                        if (messageModel.isIs_group()) {
                            updateMessageModel.setGroupId(messageModel.getGroupId());
                            List<MembersModel> membersModels = UsersController.getInstance().loadAllGroupMembers(messageModel.getGroupId());
                            int arraySize = membersModels.size();
                            if (arraySize != 0) {
                                List<String> ids = new ArrayList<>();
                                for (int x = 0; x <= arraySize - 1; x++) {
                                    if (!membersModels.get(x).getOwnerId().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))

                                        ids.add(membersModels.get(x).getOwnerId());
                                }
                                updateMessageModel.setMembers_ids(ids);

                                updateMessageModel.setState(messageModel.getState());
                                updateMessageModel.setMessageId(messageModel.get_id());
                                updateMessageModel.setConversationId(messageModel.getConversationId());
                                updateMessageModel.setMessage(messageModel.getMessage());
                                updateMessageModel.setCreated(messageModel.getCreated());
                                updateMessageModel.setFile(messageModel.getFile());
                                updateMessageModel.setFile_type(messageModel.getFile_type());
                                updateMessageModel.setDuration_file(messageModel.getDuration_file());
                                updateMessageModel.setFile_size(messageModel.getFile_size());
                                updateMessageModel.setLatitude(messageModel.getLatitude());
                                updateMessageModel.setLongitude(messageModel.getLongitude());

                                updateMessageModel.setReply_id(messageModel.getReply_id());
                                updateMessageModel.setReply_message(messageModel.isReply_message());
                                updateMessageModel.setDocument_name(messageModel.getDocument_name());
                                updateMessageModel.setDocument_type(messageModel.getDocument_type());


                                AppHelper.LogCat("ids " + ids);


                            } else {
                                mFuture.set(Result.retry());
                            }


                        } else {

                            updateMessageModel.setOtherUserId(messageModel.getRecipientId());
                            updateMessageModel.setState(messageModel.getState());
                            updateMessageModel.setMessageId(messageModel.get_id());
                            updateMessageModel.setConversationId(messageModel.getConversationId());
                            updateMessageModel.setMessage(messageModel.getMessage());
                            updateMessageModel.setCreated(messageModel.getCreated());
                            updateMessageModel.setFile(messageModel.getFile());
                            updateMessageModel.setFile_type(messageModel.getFile_type());
                            updateMessageModel.setDuration_file(messageModel.getDuration_file());
                            updateMessageModel.setFile_size(messageModel.getFile_size());
                            updateMessageModel.setLatitude(messageModel.getLatitude());
                            updateMessageModel.setLongitude(messageModel.getLongitude());

                            updateMessageModel.setReply_id(messageModel.getReply_id());
                            updateMessageModel.setReply_message(messageModel.isReply_message());
                            updateMessageModel.setDocument_name(messageModel.getDocument_name());
                            updateMessageModel.setDocument_type(messageModel.getDocument_type());


                        }
                        AppHelper.LogCat("messageModel: " + "jobStarted222");
                        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                                .sendMessage(updateMessageModel)
                                .subscribe(statusResponse -> {
                                    AppHelper.LogCat("statusResponse  " + statusResponse.getMessage());
                                    if (statusResponse.isSuccess()) {

                                        markMessageAsSent(statusResponse, updateMessageModel);
                                        mFuture.set(Result.success());


                                    } else {
                                        mFuture.set(Result.failure());
                                    }


                                }, throwable -> {
                                    AppHelper.LogCat("error  " + throwable.getMessage());
                                    mFuture.set(Result.failure());


                                }));
                    } else {
                        AppHelper.LogCat("retry: " + "jobStarted");
                        mFuture.set(Result.failure());
                    }
                } catch (Throwable throwable) {
                    mFuture.setException(throwable);
                }
            });

        } else {
            mFuture.set(Result.failure());
        }

        return mFuture;
    }

    private String generate(List<String> members) {

        Gson gson = new Gson();

        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < members.size(); i++) {
            jsonArray.add(new JsonPrimitive(members.get(i)));
        }

        return gson.toJson(jsonArray);
    }

    private void markMessageAsSent(StatusResponse statusResponse, UpdateMessageModel updateMessageModel) {
        AppHelper.LogCat("statusResponse  " + statusResponse.getMessageId());
        AppExecutors.getInstance().diskIO().execute(() -> {


            try {


                if (updateMessageModel.getSenderId().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {
                    String messageId = updateMessageModel.getMessageId();
                    String newMessageId = statusResponse.getMessageId();
                    String oldConversationId = updateMessageModel.getConversationId();
                    String newConversationId = statusResponse.getConversationId();


                    MessageModel messagesModel = MessagesController.getInstance().loadMessagesByIdAndStatus(messageId, AppConstants.IS_WAITING);


                    messagesModel.setStatus(AppConstants.IS_SENT);
                    messagesModel.setId(messagesModel.getId());
                    messagesModel.set_id(newMessageId);
                    messagesModel.setConversationId(newConversationId);

                    MessagesController.getInstance().updateMessage(messagesModel);


                    // if (!oldConversationId.equals(newConversationId)) {
                    ConversationModel conversationModel = MessagesController.getInstance().getChatById(oldConversationId);

                    if (conversationModel != null) {
                        conversationModel.setId(conversationModel.getId());
                        conversationModel.set_id(newConversationId);
                        conversationModel.setLatest_message_id(newMessageId);
                        MessagesController.getInstance().changeLastMessageStatus(conversationModel, AppConstants.IS_SENT);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS, newConversationId));
                    }
                    //   }
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES, newMessageId));

                    JSONObject updateMessage = new JSONObject();

                    if (messagesModel.isIs_group()) {
                        List<MembersModel> membersModelList = UsersController.getInstance().loadAllGroupMembers(messagesModel.getGroupId());
                        for (MembersModel membersModel : membersModelList) {
                            if (!membersModel.getOwnerId().equals(PreferenceManager.getInstance().getID(getApplicationContext()))) {
                                UsersModel usersModel = UsersController.getInstance().getUserById(membersModel.getOwnerId());
                                MessageStatus messageStatus = new MessageStatus();
                                messageStatus.setMessageId(newMessageId);
                                messageStatus.setUsersModel(usersModel);
                                messageStatus.setStatus(AppConstants.IS_SENT);

                                //date
                                MessagesController.getInstance().insertMessageInfo(messageStatus);
                            }


                        }

                         updateMessage.put("ids", updateMessageModel.getMembers_ids());
                        updateMessage.put("is_group", messagesModel.isIs_group());
                        //emit by mqtt to other user
                        try {
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.NEW_USER_MESSAGE_TO_SERVER, updateMessage);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    } else {
                        UsersModel usersModel = UsersController.getInstance().getUserById(messagesModel.getRecipientId());
                        MessageStatus messageStatus = new MessageStatus();
                        messageStatus.setMessageId(newMessageId);
                        messageStatus.setUsersModel(usersModel);
                        messageStatus.setStatus(AppConstants.IS_SENT);

                        MessagesController.getInstance().insertMessageInfo(messageStatus);


                        updateMessage.put("is_group", messagesModel.isIs_group());
                        updateMessage.put("ownerId", updateMessageModel.getOtherUserId());
                        updateMessage.put("senderId", PreferenceManager.getInstance().getID(getApplicationContext()));
                        //emit by mqtt to other user
                        try {
                            WhatsCloneApplication.getInstance().getMqttClientManager().publishMessage(AppConstants.MqttConstants.NEW_USER_MESSAGE_TO_SERVER, updateMessage);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }


                    AppHelper.LogCat("finish db ");


                }


            } catch (Exception e) {
                AppHelper.LogCat("finish  Exception" + e);

            }
        });

    }

    @Override
    public void onStopped() {
        super.onStopped();
        AppHelper.LogCat("onStopJob: " + "onStopJob");
        if (isStopped()) {
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }
        }

    }
}
