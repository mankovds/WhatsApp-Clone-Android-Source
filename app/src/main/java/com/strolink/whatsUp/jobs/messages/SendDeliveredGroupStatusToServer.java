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

public class SendDeliveredGroupStatusToServer extends ListenableWorker {

    public static final String TAG = SendDeliveredGroupStatusToServer.class.getSimpleName();
    private int mPendingMessages = 0;
    private SettableFuture<Result> mFuture;


    public SendDeliveredGroupStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {

                    List<MessageModel> messagesModels = MessagesController.getInstance().getMessages(PreferenceManager.getInstance().getID(getApplicationContext()), AppConstants.IS_SENT, 1);

                    mPendingMessages = messagesModels.size();
                    if (messagesModels.size() != 0) {
                        for (MessageModel messagesModel1 : messagesModels) {
                            String messageId = messagesModel1.get_id();
                            String ownerId = messagesModel1.getSenderId();
                            String groupId = messagesModel1.getGroupId();


                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("messageId", messageId);
                                updateMessage.put("ownerId", ownerId);
                                updateMessage.put("groupId", groupId);
                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(getApplicationContext()));
                                updateMessage.put("is_group", true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            try {
                                WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsDelivered(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_MESSAGES_AS_DELIVERED, updateMessage);

                                mPendingMessages--;
                            } catch (MqttException e) {
                                e.printStackTrace();
                                AppHelper.LogCat(" sendDeliveredStatusToServer MainService " + e.getMessage());
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
