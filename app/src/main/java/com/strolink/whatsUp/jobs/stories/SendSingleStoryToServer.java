package com.strolink.whatsUp.jobs.stories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.models.stories.CreateStoryModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;
import com.strolink.whatsUp.models.users.status.StatusResponse;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
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
public class SendSingleStoryToServer extends ListenableWorker {


    public static final String TAG = SendSingleStoryToServer.class.getSimpleName();

    private SettableFuture<Result> mFuture;
    private CompositeDisposable compositeDisposable;


    public SendSingleStoryToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
                    long storyId = getInputData().getLong("storyId", 0);

                    StoryModel storyModel = StoriesController.getInstance().getStoryByLongId(storyId);


                    if (storyModel != null) {
                        AppHelper.LogCat("storyModel: " + "jobStarted");


                        String lastTime = AppHelper.getCurrentTime();
                        CreateStoryModel createStoryModel = new CreateStoryModel();
                        createStoryModel.setStoryId(storyModel.get_id());
                        createStoryModel.setBody(storyModel.getBody());
                        createStoryModel.setCreated(lastTime);
                        createStoryModel.setDuration(storyModel.getDuration());
                        createStoryModel.setFile(storyModel.getFile());
                        createStoryModel.setType(storyModel.getType());
                        List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
                        int arraySize = usersPrivacyModels.size();
                        List<String> ids = new ArrayList<>();
                        if (arraySize == 0) {

                            List<UsersModel> usersModels = UsersController.getInstance().loadAllUsers(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                            arraySize = usersModels.size();
                            if (arraySize != 0) {

                                for (int x = 0; x <= arraySize - 1; x++) {
                                    if (!usersModels.get(x).get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                        ids.add(usersModels.get(x).get_id());
                                }
                            }
                        } else {

                            for (int x = 0; x <= arraySize - 1; x++) {
                                if (!usersPrivacyModels.get(x).getUsersModel().get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                    ids.add(usersPrivacyModels.get(x).getUsersModel().get_id());
                            }
                        }

                        createStoryModel.setIds(ids);
                        AppHelper.LogCat("storyModel: " + "jobStarted222");

                        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                                .createStory(createStoryModel)
                                .subscribe(statusResponse -> {
                                    if (statusResponse.isSuccess()) {
                                        AppHelper.LogCat("statusResponse  " + statusResponse.getMessageId());
                                        markStoryAsSent(statusResponse, createStoryModel, lastTime);
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

    private void markStoryAsSent(StatusResponse statusResponse, CreateStoryModel createStoryModel, String lastTime) {
        AppHelper.LogCat("statusResponse  " + statusResponse.getStoryId());

        String storyId = createStoryModel.getStoryId();
        String newStoryId = statusResponse.getStoryId();
        String oldUserId = PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance());


        StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);


        storyModel.setStatus(AppConstants.IS_SENT);
        storyModel.set_id(newStoryId);
        storyModel.setUserId(oldUserId);
        storyModel.setDate(lastTime);

        StoriesController.getInstance().updateStoryModel(storyModel);


        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, oldUserId));


        JSONObject updateMessage = new JSONObject();


        try {


            updateMessage.put("message", storyModel);
            updateMessage.put("storyId", newStoryId);
            updateMessage.put("ownerId", oldUserId);
            //emit by mqtt to other user
            try {

                WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.NEW_USER_STORY_TO_SERVER, updateMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

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
