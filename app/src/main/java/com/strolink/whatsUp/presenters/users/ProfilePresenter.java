package com.strolink.whatsUp.presenters.users;


import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.profile.ProfileActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.fragments.media.DocumentsFragment;
import com.strolink.whatsUp.fragments.media.LinksFragment;
import com.strolink.whatsUp.fragments.media.MediaFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePresenter implements Presenter {
    private ProfileActivity profileActivity;
    private MediaFragment mediaFragment;
    private DocumentsFragment documentFragment;
    private LinksFragment linksFragment;
    private String groupID;
    private String userID;


    private CompositeDisposable compositeDisposable;


    public ProfilePresenter(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;


    }

    public ProfilePresenter(MediaFragment mediaFragment) {
        this.mediaFragment = mediaFragment;


    }

    public ProfilePresenter(DocumentsFragment documentFragment) {
        this.documentFragment = documentFragment;


    }

    public ProfilePresenter(LinksFragment linksFragment) {
        this.linksFragment = linksFragment;


    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        compositeDisposable = new CompositeDisposable();


        if (profileActivity != null) {
            if (!EventBus.getDefault().isRegistered(profileActivity))
                EventBus.getDefault().register(profileActivity);

            if (profileActivity.getIntent().hasExtra("userID")) {
                userID = profileActivity.getIntent().getExtras().getString("userID", "");
                getContactLocal();
                try {
                    loadUserMediaData(userID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }

            }


            if (profileActivity.getIntent().hasExtra("groupID")) {
                groupID = profileActivity.getIntent().getExtras().getString("groupID", "");
                getGroupLocal();
                try {
                    loadGroupMediaData(groupID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }
            }
        } else {

            if (mediaFragment != null) {


                if (mediaFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = mediaFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (mediaFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = mediaFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (documentFragment != null) {


                if (documentFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = documentFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (documentFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = documentFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (linksFragment != null) {


                if (linksFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = linksFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (linksFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = linksFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            }


        }


    }


    private void loadUserMediaData(String userID) {
        if (profileActivity != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getUserMedia(userID, PreferenceManager.getInstance().getID(profileActivity)).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading));
        if (mediaFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getUserMedia(userID, PreferenceManager.getInstance().getID(mediaFragment.getActivity())).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading));
        else if (documentFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getUserDocuments(userID, PreferenceManager.getInstance().getID(documentFragment.getActivity())).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading));
        else if (linksFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getUserLinks(userID, PreferenceManager.getInstance().getID(linksFragment.getActivity())).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading));

    }

    private void loadGroupMediaData(String groupID) {
        if (profileActivity != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getGroupMedia(groupID).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading));
        else if (mediaFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getGroupMedia(groupID).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading));
        else if (documentFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getGroupDocuments(groupID).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading));
        else if (linksFragment != null)
            compositeDisposable.add(APIHelper.initializeMessagesService().getGroupLinks(groupID).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading));
    }


    private void getContactLocal() {


        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(userID, compositeDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString());
                    profileActivity.ShowContact(usersModel);

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));
    }


    private void getGroupLocal() {


        compositeDisposable.add(APIHelper.initializeApiGroups()
                .getGroupInfo(groupID, compositeDisposable)
                .subscribe(groupModel -> {
                    AppHelper.LogCat("groupModel " + groupModel.toString());

                    List<MembersModel> membersModels = UsersController.getInstance().loadAllGroupMembers(groupID);
                    AppHelper.runOnUIThread(() -> {
                        profileActivity.ShowGroup(groupModel, membersModels);
                    });

                }, throwable -> {
                    AppHelper.LogCat("groupModel " + throwable.getMessage());

                }));
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (profileActivity != null)
            EventBus.getDefault().unregister(profileActivity);
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }

    public void updateUIGroupData(String groupID) {

        compositeDisposable.add(APIHelper.initializeApiGroups()
                .getGroupInfo(groupID, compositeDisposable)
                .subscribe(groupModel -> {
                    AppHelper.LogCat("groupModel " + groupModel.toString());

                    List<MembersModel> membersModels = UsersController.getInstance().loadAllGroupMembers(groupID);
                    AppHelper.runOnUIThread(() -> {
                        profileActivity.UpdateGroupUI(groupModel, membersModels);
                    });

                }, throwable -> {
                    AppHelper.LogCat("groupModel " + throwable.getMessage());
                    profileActivity.onErrorLoading(throwable);

                }));

    }

    public void ExitGroup() {
        MembersModel membersModel = UsersController.getInstance().loadSingleMemberByOwnerIdAndGroupId(groupID, PreferenceManager.getInstance().getID(profileActivity));

        if (membersModel == null) {

            AppHelper.runOnUIThread(() -> {
                AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), profileActivity.getString(R.string.failed_exit_group), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

            });

        } else {
            compositeDisposable.add(APIHelper.initializeApiGroups().ExitGroup(groupID, membersModel.get_id()).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {


                    MembersModel membersGroupModel = UsersController.getInstance().loadSingleMemberByOwnerId(PreferenceManager.getInstance().getID(profileActivity));
                    membersGroupModel.setLeft(true);
                    membersGroupModel.setAdmin(false);
                    UsersController.getInstance().updateMember(membersGroupModel);

                    AppHelper.runOnUIThread(() -> {
                        AppHelper.hideDialog();
                        AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

                    });

                    try {
                        WhatsCloneApplication.getInstance().getMqttClientManager().unSubscribe(WhatsCloneApplication.getInstance().getClient(), groupID);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.LEFT_STATE);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_EXIT_THIS_GROUP, groupID));

                } else {

                    AppHelper.runOnUIThread(() -> {
                        AppHelper.hideDialog();
                        AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                    });
                }
            }, throwable -> {
                try {

                    AppHelper.runOnUIThread(() -> {

                        AppHelper.hideDialog();
                        profileActivity.onErrorExiting();
                    });
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));
        }

    }

    public void DeleteGroup() {

        MembersModel membersModel = UsersController.getInstance().loadSingleMemberByOwnerIdAndGroupId(groupID, PreferenceManager.getInstance().getID(profileActivity));


        if (membersModel == null) {

            AppHelper.runOnUIThread(() -> {
                AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), profileActivity.getString(R.string.failed_to_delete_this_group_check_connection), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

            });

        } else {

            ConversationModel conversationsModel = MessagesController.getInstance().getChatByGroupId(groupID);

            String conversationId = conversationsModel.get_id();
            AppHelper.LogCat("conversationId " + conversationId);
            compositeDisposable.add(APIHelper.initializeApiGroups().DeleteGroup(groupID, membersModel.get_id(), conversationId).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {


                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationId));

                    List<MessageModel> messagesModel1 = MessagesController.getInstance().loadMessagesByChatId(conversationId);
                    for (MessageModel messageModel : messagesModel1)
                        MessagesController.getInstance().deleteMessage(messageModel);


                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationId);
                    MessagesController.getInstance().deleteChat(conversationsModel1);

                    GroupModel groupsModel = UsersController.getInstance().getGroupById(groupID);
                    UsersController.getInstance().deleteGroup(groupsModel);

                    AppHelper.LogCat("Conversation deleted successfully ProfilePresenter");

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_GROUP, statusResponse.getMessage()));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    NotificationsManager.getInstance().SetupBadger(profileActivity);

                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY));
                } else {

                    AppHelper.runOnUIThread(() -> {
                        AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                    });
                }
            }, throwable -> {
                try {


                    AppHelper.runOnUIThread(() -> {
                        profileActivity.onErrorDeleting();
                    });

                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }, AppHelper::hideDialog));

        }

    }


}