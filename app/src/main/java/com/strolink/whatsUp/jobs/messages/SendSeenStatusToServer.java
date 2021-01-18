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
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendSeenStatusToServer extends ListenableWorker {

    public static final String TAG = SendSeenStatusToServer.class.getSimpleName();

    private SettableFuture<Result> mFuture;
    private int mPendingMessages = 0;



    public SendSeenStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        AppHelper.LogCat("InitJob: " + "InitJob");
     }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {


        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String senderId = getInputData().getString("senderId");
        String conversationId = getInputData().getString("conversationId");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {

                    List<MessageModel> messageModelList = MessagesController.getInstance().getDeliverMessages(senderId, PreferenceManager.getInstance().getID(getApplicationContext()));

                    mPendingMessages = messageModelList.size();
                    AppHelper.LogCat("size messageModelList " + messageModelList.size());
                    if (messageModelList.size() != 0) {

                        for (MessageModel messagesModel1 : messageModelList) {
                            String messageId = messagesModel1.get_id();
                            String ownerId = messagesModel1.getSenderId();
                            String recipientId = messagesModel1.getRecipientId();


                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("messageId", messageId);
                                updateMessage.put("ownerId", ownerId);
                                updateMessage.put("recipientId", recipientId);
                                updateMessage.put("is_group", false);

                            } catch (JSONException e) {
                                // e.printStackTrace();
                            }
                            try {

                                WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsSeen(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage);


                                AppHelper.LogCat("--> Recipient mark message as  seen <--");
                                mPendingMessages--;

                            } catch (MqttException e) {
                                e.printStackTrace();
                                AppHelper.LogCat(" SendSeenStatusToServer MainService " + e.getMessage());
                            }


                        }

                        if (mPendingMessages != 0)
                            mFuture.set(Result.retry());
                        else
                            mFuture.set(Result.success());

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
