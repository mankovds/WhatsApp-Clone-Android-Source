package com.strolink.whatsUp.api.apiServices;

import android.content.Context;

import com.strolink.whatsUp.BuildConfig;
import com.strolink.whatsUp.api.APIGroups;
import com.strolink.whatsUp.api.APIService;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.models.groups.EditGroup;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.GroupModelResponse;
import com.strolink.whatsUp.models.groups.GroupRequest;
import com.strolink.whatsUp.models.groups.GroupResponse;
import com.strolink.whatsUp.models.groups.MemberRequest;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.groups.MembersModelResponse;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.status.StatusResponse;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupsService {
    private APIGroups mApiGroups;
    private Context mContext;
    private APIService mApiService;

    public GroupsService(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api groups
     *
     * @return return value
     */
    public APIGroups initializeApiGroups() {
        if (mApiGroups == null) {
            mApiGroups = this.mApiService.RootService(APIGroups.class, BuildConfig.BACKEND_BASE_URL);
        }
        return mApiGroups;
    }


    public Observable<GroupModel> getGroupInfo(String groupID, CompositeDisposable compositeDisposable) {

        if (AppHelper.internetAvailable()) {
            compositeDisposable.add(initializeApiGroups().getGroup(groupID)
                    // Request API data on IO Scheduler
                    .subscribeOn(Schedulers.io())
                    // Write to db on Computation scheduler
                    .subscribeOn(Schedulers.computation())
                    // Read results in Android Main Thread (UI)
                    //   .observeOn(AndroidSchedulers.mainThread())
                    .map(this::copyOrUpdateGroup).subscribe(groupModel -> {

                    }, throwable -> {

                    }));


            // Read any cached results
            return AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                    .groupDao()
                    .loadGroupById(groupID)
                    .subscribeOn(Schedulers.computation());

        } else {
            // Read any cached results
            return AppDatabase.getInstance(WhatsCloneApplication.getInstance())
                    .groupDao()
                    .loadGroupById(groupID)
                    .subscribeOn(Schedulers.computation());

        }


    }


    /**
     * method to copy or update a single group
     *
     * @param groupsModel this is parameter for copyOrUpdateGroup method
     * @return return value
     */
    public GroupModel copyOrUpdateGroup(GroupModelResponse groupsModel) {
        AppHelper.LogCat("groupModel " + groupsModel.toString());
        GroupModel groupModel = new GroupModel();

        groupModel.set_id(groupsModel.get_id());
        groupModel.setImage(groupsModel.getImage());
        groupModel.setCreated(groupsModel.getCreated());
        groupModel.setName(groupsModel.getName());
        groupModel.setOwnerId(groupsModel.getOwner().get_id());
        groupModel.setOwner_phone(groupsModel.getOwner().getPhone());
        AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().insert(groupModel);
        List<MembersModel> membersModelList = UsersController.getInstance().loadAllGroupMembers(groupsModel.get_id());
        for (MembersModel membersModel : membersModelList) {
            UsersController.getInstance().deleteMember(membersModel);
        }


        for (MembersModelResponse membersModelJson : groupsModel.getMembers()) {
            MembersModel membersModel = new MembersModel();
            membersModel.set_id(membersModelJson.get_id());
            membersModel.setAdmin(membersModelJson.isAdmin());
            membersModel.setDeleted(membersModelJson.isDeleted());
            membersModel.setLeft(membersModelJson.isLeft());
            membersModel.setGroupId(membersModelJson.getGroupId());
            membersModel.setOwnerId(membersModelJson.getOwner().get_id());
            membersModel.setOwner_phone(membersModelJson.getOwner().getPhone());
            UsersController.getInstance().insertMember(membersModel);
        }

        ConversationModel conversationModel = MessagesController.getInstance().getChatByGroupId(groupsModel.get_id());
        if (conversationModel != null) {
            conversationModel.setGroup_image(groupsModel.getImage());
            conversationModel.setGroup_name(groupsModel.getName());
            MessagesController.getInstance().updateChat(conversationModel);

            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, conversationModel.get_id()));
        }


        return groupModel;
    }


    public Observable<GroupResponse> createGroup(GroupRequest groupRequest) {
        return initializeApiGroups().createGroup(groupRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }


    public Observable<GroupResponse> addMembers(String groupId,
                                                String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupId);
        memberRequest.setUserId(userId);
        return initializeApiGroups().addMembers(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }

    /**
     * method to exit a group
     *
     * @param groupID this is parameter for ExitGroup method
     * @return return value
     */
    public Observable<GroupResponse> ExitGroup(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().exitGroup(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * method to edit group name
     *
     * @param newName this is the first parameter for editGroupName method
     * @param groupID this is the second parameter for editGroupName method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupName(String newName, String groupID) {
        EditGroup editGroupName = new EditGroup();
        editGroupName.setName(newName);
        editGroupName.setGroupId(groupID);
        return initializeApiGroups().editGroupName(editGroupName)
                .subscribeOn(Schedulers.io())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit group name
     *
     * @param newImage this is the first parameter for editGroupImage method
     * @param groupID  this is the second parameter for editGroupImage method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupImage(String newImage, String groupID) {
        EditGroup editGroupName = new EditGroup();
        editGroupName.setImage(newImage);
        editGroupName.setGroupId(groupID);
        return initializeApiGroups().editGroupImage(editGroupName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete group
     *
     * @param groupID this is parameter for DeleteGroup method
     * @return return value
     */
    public Observable<GroupResponse> DeleteGroup(String groupID, String userId, String conversationId) {
        return initializeApiGroups().deleteGroup(groupID, userId, conversationId)
                .subscribeOn(Schedulers.io());
    }

    /**
     * method to make user as member
     *
     * @param groupID this is parameter for makeAdminMember method
     * @param userId  this is parameter for makeAdminMember method
     * @return return value
     */
    public Observable<GroupResponse> makeAdminMember(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().makeAdminMember(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * method to make user as admin
     *
     * @param groupID this is parameter for makeMemberAdmin method
     * @param userId  this is parameter for makeMemberAdmin method
     * @return return value
     */
    public Observable<GroupResponse> makeMemberAdmin(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().makeMemberAdmin(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * method to remove member from group
     *
     * @param groupID this is parameter for removeMember method
     * @param userId  this is parameter for removeMember method
     * @return return value
     */
    public Observable<GroupResponse> removeMember(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().removeMember(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
