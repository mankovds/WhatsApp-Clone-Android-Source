package com.strolink.whatsUp.jobs.messages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class UpdateMessageStatus extends ListenableWorker {

    public static final String TAG = UpdateMessageStatus.class.getSimpleName();

    private SettableFuture<Result> mFuture;


    public UpdateMessageStatus(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        AppHelper.LogCat("InitJob: " + "InitJob");
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {


        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String messageId = getInputData().getString("messageId");
        String recipientId = getInputData().getString("recipientId");
        int status = getInputData().getInt("status", 1);


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {

                    MessageModel messagesModel = MessagesController.getInstance().getMessageById(messageId);


                    if (messagesModel != null) {


                        if (status == AppConstants.IS_DELIVERED) {
                            if (MessagesController.getInstance().getUsersMessageStatusCounter(messageId, AppConstants.IS_DELIVERED) >= MessagesController.getInstance().getUsersMessageStatusCounter(messageId, AppConstants.IS_SENT)) {
                                messagesModel.setStatus(AppConstants.IS_DELIVERED);
                                MessagesController.getInstance().updateMessage(messagesModel);

                                AppHelper.LogCat("Delivered successfully");

                                ConversationModel conversationModel = MessagesController.getInstance().getChatById(messagesModel.getConversationId());
                                if (conversationModel != null && conversationModel.getLatest_message_id().equals(messagesModel.get_id())) {

                                    MessagesController.getInstance().changeLastMessageStatus(conversationModel, AppConstants.IS_DELIVERED);

                                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS, messagesModel.getConversationId()));

                                }
                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES, messageId));
                                mFuture.set(Result.success());
                            } else {
                                mFuture.set(Result.failure());
                            }
                        } else if (status == AppConstants.IS_SEEN) {

                            if (MessagesController.getInstance().getUsersMessageStatusCounter(messageId, AppConstants.IS_SEEN) >= MessagesController.getInstance().getUsersMessageStatusCounter(messageId, AppConstants.IS_SENT)) {

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
                                    updateMessage.put("is_group", true);


                                    try {

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


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            } else {
                                mFuture.set(Result.failure());
                            }
                        }


                    } else {
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


}
