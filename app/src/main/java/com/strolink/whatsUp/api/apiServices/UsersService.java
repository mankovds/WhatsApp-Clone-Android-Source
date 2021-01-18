package com.strolink.whatsUp.api.apiServices;

import android.content.Context;

import com.strolink.whatsUp.BuildConfig;
import com.strolink.whatsUp.api.APIContact;
import com.strolink.whatsUp.api.APIService;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CustomNullException;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.models.NetworkModel;
import com.strolink.whatsUp.models.SettingsResponse;
import com.strolink.whatsUp.models.auth.JoinModelResponse;
import com.strolink.whatsUp.models.auth.LoginModel;
import com.strolink.whatsUp.models.calls.CallSaverModel;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.UpdateMessageModel;
import com.strolink.whatsUp.models.stories.CreateStoryModel;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.stories.StorySeen;
import com.strolink.whatsUp.models.users.EditUser;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.BlockResponse;
import com.strolink.whatsUp.models.users.contacts.SyncContacts;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.status.EditStatus;
import com.strolink.whatsUp.models.users.status.NewStatus;
import com.strolink.whatsUp.models.users.status.StatusModel;
import com.strolink.whatsUp.models.users.status.StatusResponse;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UsersService {
    private APIContact mApiContact;
    private Context mContext;

    private APIService mApiService;


    public UsersService(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api contact
     *
     * @return return value
     */
    public APIContact initializeApiContact() {
        if (mApiContact == null) {
            mApiContact = this.mApiService.RootService(APIContact.class, BuildConfig.BACKEND_BASE_URL);
        }
        return mApiContact;
    }


    /**
     * method to get all contacts
     *
     * @return return value
     */
    public Single<List<UsersModel>> getAllContacts() {

        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadAllUsers(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Single<List<UsersModel>> getLinkedContacts() {


        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadLinkedContacts(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());


    }

    public int getLinkedContactsSize() {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadLinkedContactsSize(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Single<List<UsersBlockModel>> getBlockedContacts() {

        try {
            return Observable.create((ObservableOnSubscribe<Single<List<UsersBlockModel>>>) subscriber -> {
                try {
                    Single<List<UsersBlockModel>> userList = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().loadBlockedContacts(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());

                    if (userList != null)
                        subscriber.onNext(userList);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return Single.just(new ArrayList<>());
        }


    }

    /**
     * method to update(syncing) contacts
     *
     * @param contacts
     * @return return value
     */
    public Observable<List<UsersModel>> updateContacts(List<UsersModel> contacts) {

        SyncContacts syncContacts = new SyncContacts();
        syncContacts.setUsersModelList(contacts);
        return initializeApiContact().contacts(syncContacts)
                // Request API data on IO Scheduler
                .subscribeOn(Schedulers.io())
                // Write to db on Computation scheduler
                .subscribeOn(Schedulers.computation())
                // Read results in Android Main Thread (UI)
                // .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContacts);


    }


    public Observable<UsersModel> getUserInfo(String userID, CompositeDisposable compositeDisposable) {


        if (AppHelper.internetAvailable()) {


            compositeDisposable.add(initializeApiContact().getUser(userID)
                    // Request API data on IO Scheduler
                    .subscribeOn(Schedulers.io())
                    // Write to database on Computation scheduler
                    .subscribeOn(Schedulers.computation())
                    // Read results in Android Main Thread (UI)
                    //  .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::copyOrUpdateUserInfo).subscribe(usersModel -> {
                        AppHelper.LogCat("usersModel " + usersModel.toString());


                    }, throwable -> {
                        AppHelper.LogCat("usersModel " + throwable.getMessage());

                    }));

            return AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                    .userDao()
                    .loadUserById(userID)
                    .subscribeOn(Schedulers.computation());
        } else {
            return AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                    .userDao()
                    .loadUserById(userID)
                    .subscribeOn(Schedulers.computation());
        }

    }

    /**
     * method to get all status
     *
     * @return return value
     */
    public Single<List<StatusModel>> getAllStatus() {
        return UsersController.getInstance().getAllUserStatusById(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get user status from server
     *
     * @return return value
     */
    public Single<List<StatusModel>> getUserStatus(String userId, CompositeDisposable compositeDisposable) {

        AppHelper.LogCat("PreferenceManager.getInstance().getID(view) " + userId);
        if (AppHelper.internetAvailable()) {

            compositeDisposable.add(initializeApiContact().status(userId)
                    // Request API data on IO Scheduler
                    .subscribeOn(Schedulers.io())
                    // Write to database on Computation scheduler
                    .subscribeOn(Schedulers.computation())
                    // Read results in Android Main Thread (UI)
                    //  .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(this::copyOrUpdateStatus)
                    .subscribe(statusModels -> {
                        AppHelper.LogCat("statusModels " + statusModels.toString());


                    }, throwable -> {
                        AppHelper.LogCat("statusModels " + throwable.getMessage());

                    }));


            // Read any cached results
            return UsersController
                    .getInstance()
                    .getAllUserStatusById(userId)
                    .subscribeOn(Schedulers.computation());
        } else {

            // Read any cached results

            return UsersController
                    .getInstance()
                    .getAllUserStatusById(userId)
                    .subscribeOn(Schedulers.computation());
        }


    }

    /**
     * method to delete user status
     *
     * @param status this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<StatusResponse> deleteStatus(String status) {
        return initializeApiContact().deleteStatus(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete all user status
     *
     * @return return value
     */
    public Observable<StatusResponse> deleteAllStatus() {
        return initializeApiContact().deleteAllStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to update user status
     *
     * @param statusID this is parameter for update method
     * @return return  value
     */
    public Observable<StatusResponse> updateStatus(String statusID, String currentStatusId) {
        EditStatus editStatus = new EditStatus();
        editStatus.setCurrentStatusId(currentStatusId);
        editStatus.setStatusId(statusID);
        return initializeApiContact().updateStatus(editStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit user status
     *
     * @param newStatus this is the first parameter for editStatus method
     * @param statusID  this is the second parameter for editStatus method
     * @return return  value
     */
    public Observable<StatusResponse> editStatus(String newStatus, String statusID) {
        NewStatus newStatus1 = new NewStatus();
        newStatus1.setNewStatus(newStatus);
        newStatus1.setStatusId(statusID);
        return initializeApiContact().editStatus(newStatus1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit username
     *
     * @param newName this is parameter for editUsername method
     * @return return  value
     */
    public Observable<StatusResponse> editUsername(String newName) {
        EditUser editUser = new EditUser();
        editUser.setUsername(newName);
        return initializeApiContact().editUsername(editUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }


    /**
     * method to edit user image
     *
     * @param newImage this is parameter for editUsername method
     * @return return  value
     */
    public Observable<StatusResponse> editUserImage(String newImage) {
        EditUser editUser = new EditUser();
        editUser.setImage(newImage);
        return initializeApiContact().editUserImage(editUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get current status fron local
     *
     * @return return value
     */
    public Single<StatusModel> getCurrentStatusFromLocal() {
        return UsersController.getInstance().getCurrentUserStatusById(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());
    }

    public Observable<StatusResponse> saveNewCall(CallSaverModel callSaverModel) {
        return initializeApiContact().saveNewCall(callSaverModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);

    }


    public Observable<BlockResponse> block(String userId) {
        return initializeApiContact().block(userId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(usersResponse -> usersResponse);

    }

    public Observable<BlockResponse> unbBlock(String userId) {
        return initializeApiContact().unBlock(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(usersResponse -> usersResponse);

    }

    /**
     * method to delete user status
     *
     * @return return  value
     */
    public Observable<JoinModelResponse> deleteAccount(LoginModel loginModel) {
        return initializeApiContact().deleteAccount(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> response);
    }


    /**
     * method to copy or update user status
     *
     * @param statusModels this is parameter for copyOrUpdateStatus method
     * @return return  value
     */
    private void copyOrUpdateStatus(List<StatusModel> statusModels) {
        List<StatusModel> statusModels1 = UsersController.getInstance().getAllUserStatusByUserId(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
        if (statusModels1.size() != 0)
            UsersController.getInstance().deleteAllOldStatus(statusModels1);
        UsersController.getInstance().insertAllStatus(statusModels);

    }

    /**
     * method to copy or update contacts list
     *
     * @param mListContacts this is parameter for copyOrUpdateContacts method
     * @return return  value
     */
    private List<UsersModel> copyOrUpdateContacts(List<UsersModel> mListContacts) {


        for (UsersModel usersModel : mListContacts) {

            String displayed_name = usersModel.getPhone();
            try {

                displayed_name = UtilsPhone.getContactName(usersModel.getPhone());
                usersModel.setDisplayed_name(displayed_name);

            } catch (Exception e) {
                usersModel.setDisplayed_name(usersModel.getPhone());
            }
            UsersController.getInstance().insertUser(usersModel);


            if (!usersModel.get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())) && usersModel.isActivate() && usersModel.isLinked()) {
                ConversationModel conversationModel = MessagesController.getInstance().getChatByUserId(usersModel.get_id());
                if (conversationModel != null) {
                    conversationModel.setOwner_image(usersModel.getImage());
                    conversationModel.setOwner_phone(usersModel.getPhone());
                    conversationModel.setOwner_displayed_name(displayed_name);
                    MessagesController.getInstance().updateChat(conversationModel);
                }
            }
        }
        return mListContacts;
    }


    /**
     * method to copy or update user information
     *
     * @param usersModel this is parameter for copyOrUpdateContactInfo method
     * @return return  value
     */
    private void copyOrUpdateUserInfo(UsersModel usersModel) {
        String displayed_name = usersModel.getPhone();
        try {

            displayed_name = UtilsPhone.getContactName(usersModel.getPhone());
            usersModel.setDisplayed_name(displayed_name);

        } catch (Exception e) {
            usersModel.setDisplayed_name(usersModel.getPhone());
        }
        if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), usersModel.getPhone())) {
            usersModel.setExist(true);
            UsersController.getInstance().insertUser(usersModel);
        } else {
            usersModel.setExist(false);
            UsersController.getInstance().insertUser(usersModel);
        }

        if (!usersModel.get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())) && usersModel.isActivate() && usersModel.isLinked()) {

            ConversationModel conversationModel = MessagesController.getInstance().getChatByUserId(usersModel.get_id());
            if (conversationModel != null) {
                conversationModel.setOwner_image(usersModel.getImage());
                conversationModel.setOwner_phone(usersModel.getPhone());
                conversationModel.setOwner_displayed_name(displayed_name);

                MessagesController.getInstance().updateChat(conversationModel);

                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, conversationModel.get_id()));

            }
        }


    }


    public Observable<SettingsResponse> getAppSettings() {
        return initializeApiContact().getAppSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation());
    }


    /**
     * *
     * method to get all calls
     *
     * @return return value
     */
    public Single<List<CallsModel>> getAllCalls() {

        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadAllCalls().subscribeOn(Schedulers.computation());
    }

    /**
     * *
     * method to get all calls details
     *
     * @return return value
     */
    public Single<List<CallsInfoModel>> getAllCallsDetails(String callID) {

        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadAllCallsInfo(callID).subscribeOn(Schedulers.computation());
    }

    /**
     * method to get general call information
     *
     * @param callID this is parameter  getContact for method
     * @return return value
     */
    public Single<CallsModel> getCallDetails(String callID) {

        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadCallById(callID).subscribeOn(Schedulers.computation());
    }


    public Observable<NetworkModel> checkIfUserSession() {
        return initializeApiContact().checkNetwork()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(networkModel -> networkModel);
    }


    public Observable<StatusResponse> sendMessage(UpdateMessageModel updateMessageModel) {

        return initializeApiContact()
                .sendMessage(updateMessageModel)
                .subscribeOn(Schedulers.io())
                .map(statusResponse -> statusResponse);
    }

    public Single<StoriesHeaderModel> getMineStories() {
        return StoriesController.getInstance().getStoriesHeader().subscribeOn(Schedulers.computation());
    }

    public Single<List<StoriesModel>> getAllStories() {
        return StoriesController.getInstance().getStoriesModelList().subscribeOn(Schedulers.computation());
    }

    public Single<List<StoryModel>> getStories() {
        return StoriesController.getInstance().getAllStory(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).subscribeOn(Schedulers.computation());

    }

    public Single<List<StorySeen>> getSeenList(String storyId) {
        return StoriesController.getInstance().getAllSeenStory(storyId).subscribeOn(Schedulers.computation());
    }

    public Observable<StatusResponse> createStory(CreateStoryModel createStoryModel) {
        return initializeApiContact().createStory(createStoryModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }

    public Observable<StatusResponse> deleteStory(String storyId) {
        return initializeApiContact().deleteStory(storyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }


    public Observable<StatusResponse> deleteAccountConfirmation(String code, String phone) {
        return initializeApiContact().deleteAccountConfirmation(code, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }


    public Observable<StatusResponse> deleteConversation(String conversationId) {
        return initializeApiContact().deleteConversation(conversationId)
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .map(statusResponse -> statusResponse);
    }

    public Observable<StatusResponse> deleteMessage(String messageId) {
        return initializeApiContact().deleteMessage(messageId)
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .map(statusResponse -> statusResponse);
    }


}
