package com.strolink.whatsUp.activities.groups;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.groups.CreateGroupMembersToGroupAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.images.ImageCompressionAsyncTask;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.GroupRequest;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.groups.MembersModelJson;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

;


/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class CreateGroupActivity extends BaseActivity implements InputGeneralPanel.Listener {


    @BindView(R.id.layout_container)
    LinearLayout container;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;


    @BindView(R.id.group_image)
    AppCompatImageView groupImage;
    @BindView(R.id.add_image_group)
    AppCompatImageView addImageGroup;
    @BindView(R.id.fab)
    FloatingActionButton doneBtn;

    @BindView(R.id.create_group_pro_bar)
    ProgressBar progressBarGroup;

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.participantCounter)
    TextView participantCounter;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private CreateGroupMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private String selectedImagePath = null;

    private String lastConversationID;
    private EmojiPopup emojiPopup;
    private CompositeDisposable mDisposable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        mDisposable = new CompositeDisposable();
        initializeView();
        setupToolbar();
        loadData();


    }


    /**
     * method to loadCircleImage members form shared preference
     */
    private void loadData() {


        List<UsersModel> usersModels = new ArrayList<>();
        if (PreferenceManager.getInstance().getMembers(this) == null) return;
        int arraySize = PreferenceManager.getInstance().getMembers(this).size();

        String id;
        for (int x = 0; x < arraySize; x++) {
            id = PreferenceManager.getInstance().getMembers(this).get(x).getUserId();
            UsersModel usersModel = UsersController.getInstance().getUserById(id);
            usersModels.add(usersModel);
        }

        mAddMembersToGroupListAdapter.setContacts(usersModels);

        String text = String.format(getString(R.string.participants) + " %s/%s ", mAddMembersToGroupListAdapter.getItemCount(), PreferenceManager.getInstance().getContactSize(this));
        participantCounter.setText(text);

    }

    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
    }


    /**
     * method to initialize  the view
     */
    private void initializeView() {
        GridLayoutManager mLinearLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new CreateGroupMembersToGroupAdapter(this);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        doneBtn.setOnClickListener(v -> createGroupOffline());
        addImageGroup.setOnClickListener(v -> launchImageChooser());
        if (AppHelper.isAndroid5()) {
            Transition enterTrans = new Fade();
            getWindow().setEnterTransition(enterTrans);
            enterTrans.setDuration(300);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            doneBtn.setLayoutParams(params);
        }

        composeText.setHint(getString(R.string.type_group_subject_here));
        inputPanel.setListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(container).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);
    }

    /**
     * method to select an image
     */
    private void launchImageChooser() {
        Intent mIntent = new Intent();
        mIntent.setType("image/*");
        mIntent.setAction(Intent.ACTION_GET_CONTENT);
        mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(
                Intent.createChooser(mIntent, getString(R.string.select_picture)),
                AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
    }

    /**
     * method to create group in offline mode
     */
    private void createGroupOffline() {

        String groupName = UtilsString.escapeJava(composeText.getText().toString().trim());
        if (groupName.length() <= 3) {
            setProgressBarGroup();
            composeText.setError(getString(R.string.name_is_too_short));
        } else {
            getProgressBarGroup();
            DateTime current = new DateTime();
            String createTime = String.valueOf(current);

            int arraySize = PreferenceManager.getInstance().getMembers(CreateGroupActivity.this).size();
            List<String> ids = new ArrayList<>();
            for (int x = 0; x <= arraySize - 1; x++) {
                ids.add(PreferenceManager.getInstance().getMembers(CreateGroupActivity.this).get(x).getUserId());
            }
            ids.add(PreferenceManager.getInstance().getID(this));
            AppHelper.LogCat("ids " + ids);


            GroupRequest groupRequest = new GroupRequest();
            groupRequest.setCreateTime(createTime);
            groupRequest.setIds(ids);
            if (selectedImagePath != null)
                groupRequest.setImage(selectedImagePath);
            else
                groupRequest.setImage("null");
            groupRequest.setName(groupName);
            mDisposable.add(APIHelper.initializeApiGroups().createGroup(groupRequest).subscribe(groupResponse -> {
                if (groupResponse.isSuccess()) {
                    setProgressBarGroup();

                    AppHelper.LogCat("group id created 2 e " + groupResponse.toString());


                    String lastConversationID = DbBackupRestore.getConversationLastId();
                    String lastID = DbBackupRestore.getConversationLastId();


                    UsersModel usersModelSender = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(this));

                    GroupModel groupModel = new GroupModel();
                    groupModel.set_id(groupResponse.getGroupId());

                    for (MembersModelJson membersModelJson : groupResponse.getMembersModels()) {
                        UsersModel userOwner = UsersController.getInstance().getUserById(membersModelJson.getUserId());
                        MembersModel membersModel = new MembersModel();
                        membersModel.set_id(membersModelJson.get_id());
                        membersModel.setAdmin(membersModelJson.isAdmin());
                        membersModel.setDeleted(membersModelJson.isDeleted());
                        membersModel.setLeft(membersModelJson.isLeft());
                        membersModel.setGroupId(membersModelJson.getGroupId());
                        membersModel.setOwnerId(userOwner.get_id());
                        membersModel.setOwner_phone(userOwner.getPhone());
                        UsersController.getInstance().insertMember(membersModel);

                    }

                    if (groupResponse.getGroupImage() != null)
                        groupModel.setImage(groupResponse.getGroupImage());
                    else
                        groupModel.setImage("null");
                    groupModel.setName(groupName);
                    groupModel.setOwnerId(usersModelSender.get_id());
                    groupModel.setOwner_phone(usersModelSender.getPhone());

                    UsersController.getInstance().insertGroup(groupModel);

                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(lastID);
                    messagesModel.setConversationId(lastConversationID);
                    messagesModel.setCreated(createTime);
                    messagesModel.setStatus(AppConstants.IS_WAITING);

                    messagesModel.setGroupId(groupModel.get_id());
                    messagesModel.setGroup_image(groupModel.getImage());
                    messagesModel.setGroup_name(groupModel.getName());
                    messagesModel.setSenderId(usersModelSender.get_id());
                    messagesModel.setSender_image(usersModelSender.getImage());
                    messagesModel.setSender_phone(usersModelSender.getPhone());

                    messagesModel.setIs_group(true);
                    messagesModel.setMessage("null");
                    messagesModel.setLatitude("null");
                    messagesModel.setLongitude("null");
                    messagesModel.setFile("null");
                    messagesModel.setFile_type("null");
                    messagesModel.setState(AppConstants.CREATE_STATE);
                    messagesModel.setFile_size("0");
                    messagesModel.setDuration_file("0");
                    messagesModel.setReply_id("null");
                    messagesModel.setReply_message(true);
                    messagesModel.setDocument_name("null");
                    messagesModel.setDocument_type("null");
                    messagesModel.setFile_upload(true);
                    messagesModel.setFile_downLoad(true);

                    MessagesController.getInstance().insertMessage(messagesModel);

                    ConversationModel conversationsModel1 = new ConversationModel();
                    conversationsModel1.set_id(lastConversationID);


                    conversationsModel1.setGroup_id(groupModel.get_id());
                    conversationsModel1.setGroup_image(groupModel.getImage());
                    conversationsModel1.setGroup_name(groupModel.getName());

                    conversationsModel1.setIs_group(true);

                    conversationsModel1.setLatest_message_id(messagesModel.get_id());
                    conversationsModel1.setLatest_message(messagesModel.getMessage());
                    conversationsModel1.setFile_type(messagesModel.getFile_type());
                    conversationsModel1.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel1.setLatest_message_state(messagesModel.getState());
                    conversationsModel1.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel1.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel1.setLatest_message_sender_id(usersModelSender.get_id());
                    conversationsModel1.setLatest_message_sender_phone(usersModelSender.getPhone());
                    String name = UtilsPhone.getContactName(usersModelSender.getPhone());
                    conversationsModel1.setLatest_message_sender__displayed_name(name);

                    conversationsModel1.setCreated(createTime);
                    conversationsModel1.setUnread_message_counter(0);

                    MessagesController.getInstance().insertChat(conversationsModel1);


                    // EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                    //  EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ADD_MEMBER, lastConversationID));

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                    PreferenceManager.getInstance().clearMembers(CreateGroupActivity.this);
                    AppHelper.CustomToast(CreateGroupActivity.this, groupResponse.getMessage());
                    if (emojiPopup.isShowing()) emojiPopup.dismiss();
                    WorkJobsManager.getInstance().sendUserMessagesToServer();
                    // new Handler().postDelayed(() -> JobsManager.getInstance().sendGroupMessagesToServer(), 500);
                    finish();


                } else {
                    setProgressBarGroup();
                    AppHelper.CustomToast(CreateGroupActivity.this, groupResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat(throwable.getMessage());

                setProgressBarGroup();
                AppHelper.CustomToast(CreateGroupActivity.this, CreateGroupActivity.this.getString(R.string.oops_something));
            }));

        }


    }

    void getProgressBarGroup() {
        progressBarGroup.setVisibility(View.VISIBLE);
        doneBtn.setVisibility(View.GONE);
        doneBtn.setEnabled(false);
    }

    void setProgressBarGroup() {
        progressBarGroup.setVisibility(View.GONE);
        doneBtn.setVisibility(View.VISIBLE);
        doneBtn.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPLOAD_PICTURE_REQUEST_CODE) {

                if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                    selectedImagePath = FilesManager.getPath(this, data.getData());
                    @SuppressLint("StaticFieldLeak")
                    ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
                        @Override
                        protected void onPostExecute(byte[] imageBytes) {

                            // image here is compressed & ready to be sent to the server
                            // create RequestBody instance from file
                            RequestBody requestFile;
                            if (imageBytes == null)
                                requestFile = null;
                            else
                                requestFile = RequestBody.create( MediaType.parse("image*//*"),imageBytes);
                            if (requestFile == null) {
                                AppHelper.CustomToast(CreateGroupActivity.this, getString(R.string.oops_something));
                            } else {
                                File file = new File(selectedImagePath);
                                mDisposable.add(APIHelper.initializeUploadFiles().uploadGroupImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe(filesResponse -> {
                                            if (filesResponse.isSuccess()) {
                                                AppHelper.LogCat("throwable " + filesResponse.getFilename());
                                                selectedImagePath = filesResponse.getFilename();
                                                Drawable drawable = AppHelper.getDrawable(CreateGroupActivity.this, R.drawable.holder_user);
                                                GlideApp.with(CreateGroupActivity.this)
                                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + filesResponse.getFilename()))
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                                        .placeholder(drawable)
                                                        .error(drawable)
                                                        .into(groupImage);
                                                if (groupImage.getVisibility() != View.VISIBLE) {
                                                    groupImage.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                AppHelper.LogCat("throwable hjjh ");
                                                selectedImagePath = null;
                                                AppHelper.CustomToast(CreateGroupActivity.this, filesResponse.getMessage());
                                            }

                                        }, throwable -> {
                                            AppHelper.LogCat("throwable " + throwable);
                                            AppHelper.CustomToast(CreateGroupActivity.this, getString(R.string.oops_something));
                                        })
                                );

                            }
                        }
                    };
                    imageCompression.execute(selectedImagePath);

                } else {
                    Permissions.with(this)
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .ifNecessary()
                            .withRationaleDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                                    R.drawable.ic_folder_white_24dp)
                            .onAnyResult(() -> {

                            })
                            .execute();
                }


            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getContacts() != null && mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.getInstance().clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();
            }
            finish();


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onEmojiToggle() {

        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) emojiPopup.dismiss();
        else {

            if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.getInstance().clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();

            }
            super.onBackPressed();
        }
    }

}
