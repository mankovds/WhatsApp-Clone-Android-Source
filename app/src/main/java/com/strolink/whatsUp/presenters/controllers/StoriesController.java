package com.strolink.whatsUp.presenters.controllers;

import android.annotation.SuppressLint;

import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CustomNullException;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.stories.StorySeen;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;

/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class StoriesController {

    private static volatile StoriesController Instance = null;


    public StoriesController() {
    }

    public static StoriesController getInstance() {

        StoriesController localInstance = Instance;
        if (localInstance == null) {
            synchronized (StoriesController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new StoriesController();
                }
            }
        }
        return localInstance;

    }

    private Boolean checkIfStoryExist(String storyId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().storiesExistence(storyId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }


    public void deleteStoriesModel(StoriesModel storiesModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().delete(storiesModel));

    }

    public void insertStoriesModel(StoriesModel storiesModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().insert(storiesModel));

    }

    public void updateStoriesModel(StoriesModel storiesModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().update(storiesModel));

    }
//story mine

    public void deleteStoriesHeaderModel(StoriesHeaderModel storiesHeaderModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesMineDao().delete(storiesHeaderModel));

    }

    public void insertStoriesHeaderModel(StoriesHeaderModel storiesHeaderModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesMineDao().insert(storiesHeaderModel));

    }

    public void updateStoriesHeaderModel(StoriesHeaderModel storiesHeaderModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesMineDao().update(storiesHeaderModel));

    }

    //single story
    public void deleteStoryModel(StoryModel storyModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().delete(storyModel));

    }

    public void insertStoryModel(StoryModel storyModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().insert(storyModel));

    }

    public void updateStoryModel(StoryModel storyModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().update(storyModel));

    }

    //seen story
    public void deleteStorySeen(StorySeen storySeen) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().delete(storySeen));

    }

    public void insertStorySeen(StorySeen storySeen) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().insert(storySeen));

    }

    public void updateStorySeen(StorySeen storySeen) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().update(storySeen));

    }

    public Single<List<StoriesModel>> getStoriesModelList() {


        try {
            return Observable.create((ObservableOnSubscribe<Single<List<StoriesModel>>>) subscriber -> {
                try {
                    Single<List<StoriesModel>> stories = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().loadAllStories();
                    if (stories != null)
                        subscriber.onNext(stories);
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

    public List<StoriesModel> getAllStoriesModelList() {

        try {
            return Observable.create((ObservableOnSubscribe<List<StoriesModel>>) subscriber -> {
                try {
                    List<StoriesModel> stories = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().loadAllUserStories();
                    if (stories != null)
                        subscriber.onNext(stories);
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

    public StoriesModel getStoriesModel(String storyId) {
        try {
            return Observable.create((ObservableOnSubscribe<StoriesModel>) subscriber -> {
                try {
                    StoriesModel storiesModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDao().loadSingleStoriesById(storyId);
                    if (storiesModel != null)
                        subscriber.onNext(storiesModel);
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

    public StorySeen getStorySeen(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<StorySeen>) subscriber -> {
                try {
                    StorySeen storySeen = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().loadSingleStorySeenById(userId);
                    if (storySeen != null)
                        subscriber.onNext(storySeen);
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

    public void updateStoryStatus(String storyId) {


        StoryModel storyModel = getStoryById(storyId);

        if (storyModel != null) {
            storyModel.setStatus(AppConstants.IS_SEEN);
            storyModel.setDownloaded(true);
            if (!checkIfStoryDownloadExist(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {
                StoriesModel storiesModel = getStoriesModel(storyModel.getUserId());
                storiesModel.setDownloaded(true);
                updateStoriesModel(storiesModel);
            }

            updateStoryModel(storyModel);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, storyId));
        }


    }

    public boolean checkIfStoryDownloadExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsExistenceDownload(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public boolean checkIfStoryUploadExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsExistenceUpload(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }


    public boolean checkIfStoryUploadExist(String userId, String storyId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsExistenceUpload(userId, storyId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    /**
     * method to update status as seen by sender (if recipient have been seen the story)  in  database
     */
    public void updateSeenStatus(String storyId, JSONArray users) {

        if (checkIfSingleStoryExist(storyId)) {
            AppHelper.LogCat("Seen storyId " + storyId);

            for (int i = 0; i < users.length(); i++) {
                String recipientId;
                try {
                    recipientId = users.getString(i);


                    UsersModel usersModel = UsersController.getInstance().getUserById(recipientId);

                    StoryModel storyModel = getStoryById(storyId);
                    if (storyModel != null) {
                        storyModel.setStatus(AppConstants.IS_SEEN);
                        if (!checkIfSeenUserStoryExist(recipientId)  ) {
                            AppHelper.LogCat("Seen checkIfSeenUserStoryExist not");
                            StorySeen storySeen = new StorySeen();
                            storySeen.setStoryId(storyId);
                            storySeen.setUserId(recipientId);
                            insertStorySeen(storySeen);
                        }
                        updateStoryModel(storyModel);
                        AppHelper.LogCat("Seen successfully");


                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("recipientId", recipientId);

                            try {

                                 WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                            } catch (MqttException | JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            // e.printStackTrace();
                        }


                    } else {
                        AppHelper.LogCat("Seen failed ");
                    }
                } catch (JSONException e) {
                    AppHelper.LogCat("Seen failed " + e.getMessage());
                }
            }
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, storyId));


        } else {
            JSONObject updateMessage = new JSONObject();
            try {
                updateMessage.put("storyId", storyId);
            } catch (JSONException e) {
                // e.printStackTrace();
            }


            try {

                 WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_EXIST_AS_FINISHED, updateMessage);
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /*for update message status*/


    /**
     * method to save the new stories and mark him as waiting
     */

    public void saveNewUserStory(JSONObject data) {


        try {

            //sender object
            String senderId = data.getJSONObject("owner").getString("_id");
            String sender_phone = data.getJSONObject("owner").getString("phone");
            String sender_image = data.getJSONObject("owner").getString("image");
            //story object
            String storyId = data.getString("_id");
            String storyBody = data.getString("body");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String storyOwnerId = data.getString("storyOwnerId");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");


            if (senderId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), sender_phone)) {
                if (!checkIfStoryExist(storyOwnerId)) {

                    if (!checkIfSingleStoryExist(storyId)) {//avoid duplicate stories


                        StoryModel storyModel = new StoryModel();
                        storyModel.set_id(storyId);
                        storyModel.setUserId(storyOwnerId);
                        storyModel.setDate(created);
                        storyModel.setDownloaded(false);
                        storyModel.setUploaded(true);
                        storyModel.setDeleted(false);
                        storyModel.setStatus(AppConstants.IS_WAITING);
                        storyModel.setBody(storyBody);
                        storyModel.setFile(file);
                        storyModel.setType(file_type);
                        storyModel.setDuration(duration);


                        insertStoryModel(storyModel);

                        StoriesModel storiesModel = new StoriesModel();
                        storiesModel.set_id(storyOwnerId);


                        String name = UtilsPhone.getContactName(sender_phone);
                        if (name != null) {
                            storiesModel.setUsername(name);
                        } else {
                            storiesModel.setUsername(sender_phone);
                        }
                        if (sender_image != null)
                            storiesModel.setUserImage(sender_image);
                        storiesModel.setDownloaded(false);
                        storiesModel.setPreview(file);

                        insertStoriesModel(storiesModel);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_NEW_ROW, storyId));


                    } else {

                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("ownerId", senderId);
                            updateMessage.put("mine", false);
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


                    }
                } else {

                    if (!checkIfSingleStoryExist(storyId)) {//avoid duplicate stories


                        StoryModel storyModel = new StoryModel();
                        storyModel.set_id(storyId);
                        storyModel.setUserId(storyOwnerId);
                        storyModel.setDate(created);
                        storyModel.setDownloaded(false);
                        storyModel.setUploaded(true);
                        storyModel.setDeleted(false);
                        storyModel.setStatus(AppConstants.IS_WAITING);
                        storyModel.setBody(storyBody);
                        storyModel.setFile(file);
                        storyModel.setType(file_type);
                        storyModel.setDuration(duration);


                        StoriesModel storiesModel = getStoriesModel(storyOwnerId);
                        storiesModel.set_id(storyOwnerId);
                        String name = UtilsPhone.getContactName(sender_phone);
                        if (name != null) {
                            storiesModel.setUsername(name);
                        } else {
                            storiesModel.setUsername(sender_phone);
                        }
                        if (sender_image != null)
                            storiesModel.setUserImage(sender_image);
                        storiesModel.setDownloaded(false);
                        storiesModel.setPreview(file);
                        insertStoryModel(storyModel);
                        updateStoriesModel(storiesModel);


                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_OLD_ROW, storyId));


                    } else {
                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("ownerId", senderId);
                            updateMessage.put("mine", false);
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


                    }

                }


            } else {


                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("storyId", storyId);
                    updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {

                     WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                } catch (MqttException | JSONException e) {
                    e.printStackTrace();
                }
            }


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception " + e.getMessage());
        }
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
    }


    public boolean checkIfSingleStoryExist(String storyId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsExistence(storyId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public boolean checkIfaDeletedStoryExist() {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadExistStoryNotDeleted() != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public boolean checkIfSingleStoryMineExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsMineExistence(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    private boolean checkIfSeenUserStoryExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().storySeenExistence(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    public boolean checkIfSingleStoryWaitingExist(String storyId) {


        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().storiesDetailsExistenceWaiting(storyId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }


    public List<StoryModel> getStoriesById(String ownerId) {

        try {
            return Observable.create((ObservableOnSubscribe<List<StoryModel>>) subscriber -> {
                try {
                    List<StoryModel> stories = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllUserStoriesDetails(ownerId);
                    if (stories != null)
                        subscriber.onNext(stories);
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

    public List<StoryModel> getStoriesHeaderById(String ownerId) {

        try {
            return Observable.create((ObservableOnSubscribe<List<StoryModel>>) subscriber -> {
                try {
                    List<StoryModel> stories = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllUserStoriesDetails(ownerId);
                    if (stories != null)
                        subscriber.onNext(stories);
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

    public Single<StoriesHeaderModel> getStoriesHeader() {
        try {

            return Observable.create((ObservableOnSubscribe<Single<StoriesHeaderModel>>) subscriber -> {
                try {
                    Single<StoriesHeaderModel> storiesHeaderModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesMineDao().loadSingleStoriesMine();
                    if (storiesHeaderModel != null)
                        subscriber.onNext(storiesHeaderModel);
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

    public StoriesHeaderModel getStoriesHeader(String storyId) {

        try {
            return Observable.create((ObservableOnSubscribe<StoriesHeaderModel>) subscriber -> {
                try {
                    StoriesHeaderModel storiesHeaderModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesMineDao().loadSingleStoriesMine(storyId);
                    if (storiesHeaderModel != null)
                        subscriber.onNext(storiesHeaderModel);
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

    public Single<List<StoryModel>> getWaitingStories(String userId) {
        try {

            return Observable.create((ObservableOnSubscribe<Single<List<StoryModel>>>) subscriber -> {
                try {
                    Single<List<StoryModel>> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllUserWaitingStoriesDetails(userId);
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public Single<List<StoryModel>> getAllStory(String userId) {
        try {

            return Observable.create((ObservableOnSubscribe<Single<List<StoryModel>>>) subscriber -> {
                try {
                    Single<List<StoryModel>> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllStoriesDetails(userId);
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public Single<List<StorySeen>> getAllSeenStory(String storyId) {
        try {

            return Observable.create((ObservableOnSubscribe<Single<List<StorySeen>>>) subscriber -> {
                try {
                    Single<List<StorySeen>> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().loadAllStorySeen(storyId);
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public int getAllSeenStoryCounter(String storyId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesSeenDao().loadAllStorySeenCounter(storyId));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public int getStoryDownloadSize(String storyId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().getStoryDownloadSize(storyId));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public List<StoryModel> getAllStoryNotDeleted(String userId) {


        try {

            return Observable.create((ObservableOnSubscribe<List<StoryModel>>) subscriber -> {
                try {
                    List<StoryModel> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllStoryNotDeleted(userId);
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public List<StoryModel> getAllStoryNotDeleted() {
        try {

            return Observable.create((ObservableOnSubscribe<List<StoryModel>>) subscriber -> {
                try {
                    List<StoryModel> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllStoryNotDeleted();
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public List<StoryModel> getAllExpiredStories(String expire_date) {
        try {

            return Observable.create((ObservableOnSubscribe<List<StoryModel>>) subscriber -> {
                try {
                    List<StoryModel> storyModelList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadAllExpiredStories(expire_date);
                    if (storyModelList != null)
                        subscriber.onNext(storyModelList);
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

    public StoryModel getStoryById(String storyId) {

        try {

            return Observable.create((ObservableOnSubscribe<StoryModel>) subscriber -> {
                try {
                    StoryModel storyModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadSingleStoriesDetailsById(storyId);
                    if (storyModel != null)
                        subscriber.onNext(storyModel);
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

    public StoryModel getStoryByLongId(long storyId) {

        try {

            return Observable.create((ObservableOnSubscribe<StoryModel>) subscriber -> {
                try {
                    StoryModel storyModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).storiesDetailsDao().loadSingleStoriesDetailsByLongId(storyId);
                    if (storyModel != null)
                        subscriber.onNext(storyModel);
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

    public void deleteExpiredStory(String storyId, String ownerId) {


        if (StoriesController.getInstance().checkIfSingleStoryExist(storyId)) {
            //set story as deleted

            StoryModel storyModel2 = StoriesController.getInstance().getStoryById(storyId);
            storyModel2.setDeleted(true);
            StoriesController.getInstance().updateStoryModel(storyModel2);

            List<StoryModel> storyModels1 = StoriesController.getInstance().getAllStoryNotDeleted(ownerId);

            if (storyModels1.size() == 0) {
                AppHelper.LogCat("stories deleted successfully  ");
                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));
            } else {
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
            }


            JSONObject updateMessage = new JSONObject();
            try {
                updateMessage.put("storyId", storyId);
                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                //emit by mqtt to other user
                try {

                    WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                // e.printStackTrace();
            }

           // WorkJobsManager.getInstance().sendDeletedStoryToServer();
            AppHelper.LogCat("mark story as deleted successfully  ");
        } else {

            if (ownerId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {

                APIHelper.initialApiUsersContacts().deleteStory(storyId).subscribe(statusResponse -> {
                    if (statusResponse.isSuccess()) {


                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                            //emit by mqtt to other user
                            try {

                                 WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                        AppHelper.LogCat("delete story failed  " + statusResponse.getMessage());

                    }


                }, throwable -> {

                    AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                });
            } else {


                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("storyId", storyId);
                    updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    //emit by mqtt to other user
                    try {

                         WhatsCloneApplication.getInstance().getMqttClientManager().publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                }


            }
        }

    }


    public void unSentStories() {


        AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                .storiesDetailsDao()
                .loadAllUserWaitingStoriesDetails(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                .subscribeOn(Schedulers.computation())
                .subscribe(storyModelList -> {
                    AppHelper.LogCat("Job unSentStories: " + storyModelList.size());
                    if (storyModelList.size() > 0) {
                        AppHelper.LogCat("storyModelList jjb: " + storyModelList.size());

                        for (StoryModel storyModel : storyModelList) {
                            WorkJobsManager.getInstance().sendSingleStoryToServerWorker(storyModel.getId());
                        }


                    }
                });
    }

}