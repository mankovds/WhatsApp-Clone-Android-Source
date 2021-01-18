package com.strolink.whatsUp.jobs.stories;

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
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.presenters.controllers.StoriesController;

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

public class SendSeenStoryToServer extends ListenableWorker {

    public static final String TAG = SendSeenStoryToServer.class.getSimpleName();

    private SettableFuture<Result> mFuture;


    public SendSeenStoryToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);


     }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        mFuture = SettableFuture.create();
        String senderId = getInputData().getString("senderId");
        String storyId = getInputData().getString("storyId");


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    AppHelper.LogCat("Job emitStorySeen. storyId : " + storyId);
                    StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
                    if (storyModel != null) {

                        if (storyModel.getStatus() != AppConstants.IS_SEEN) {
                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("storyId", storyId);
                                updateMessage.put("ownerId", senderId);
                                updateMessage.put("mine", true);
                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                //emit by mqtt to other user
                                try {

                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_SEEN, updateMessage);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                            } catch (JSONException e) {
                                // e.printStackTrace();
                            }

                        } else {

                            mFuture.set(Result.failure());
                            AppHelper.LogCat("this story is already seen failed ");
                        }
                    } else {
                        mFuture.set(Result.failure());
                        AppHelper.LogCat("this story is already seen failed ");
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
