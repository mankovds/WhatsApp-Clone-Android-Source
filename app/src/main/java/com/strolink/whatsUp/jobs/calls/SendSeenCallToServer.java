package com.strolink.whatsUp.jobs.calls;

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
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.presenters.controllers.CallsController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendSeenCallToServer extends ListenableWorker {

    public static final String TAG = SendSeenCallToServer.class.getSimpleName();


    private String callId;


    private SettableFuture<Result> mFuture;


    public SendSeenCallToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);


     }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        AppHelper.LogCat("onStartJob: " + "jobStarted");
        mFuture = SettableFuture.create();
        String senderId = getInputData().getString("senderId");
        callId = getInputData().getString("callId");


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {

                    AppHelper.LogCat("Job . callId : " + callId);
                    CallsInfoModel callsInfoModel = CallsController.getInstance().getCallInfoById(callId);
                    if (callsInfoModel != null) {

                        if (callsInfoModel.getStatus() != AppConstants.IS_SEEN) {
                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("callId", callId);
                                updateMessage.put("ownerId", senderId);
                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                                try {
                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_AS_SEEN, updateMessage,"call");
                                } catch (MqttException | JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                // e.printStackTrace();
                            }

                            mFuture.set(Result.success());
                        } else {
                            mFuture.set(Result.failure());
                            AppHelper.LogCat("this call is already seen failed ");
                        }
                    } else {
                        mFuture.set(Result.failure());
                        AppHelper.LogCat("this call is not exist failed ");
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
