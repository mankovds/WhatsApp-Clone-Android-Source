package com.strolink.whatsUp.activities.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.groups.AddNewMembersToGroupActivity;
import com.strolink.whatsUp.activities.groups.EditGroupActivity;
import com.strolink.whatsUp.activities.media.MediaActivity;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.adapters.recyclerView.groups.GroupMembersAdapter;
import com.strolink.whatsUp.adapters.recyclerView.media.MediaProfileAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.api.APIService;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.fragments.bottomSheets.BottomSheetEditGroupImage;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.Files.cache.ImageLoader;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.call.CallManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.images.ImageCompressionAsyncTask;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.users.ProfilePresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfileActivity extends BaseActivity {

    @BindView(R.id.cover)
    AppCompatImageView UserCover;
    @BindView(R.id.anim_toolbar)
    Toolbar toolbar;/*
    @BindView(R.id.appbar)
    AppBarLayout AppBarLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;*/
    @BindView(R.id.containerProfile)
    LinearLayout containerProfile;
    @BindView(R.id.created_title)
    EmojiTextView mCreatedTitle;
    @BindView(R.id.group_container_title)
    LinearLayout GroupTitleContainer;
    @BindView(R.id.group_edit)
    FloatingActionButton EditGroupBtn;
    @BindView(R.id.statusPhoneContainer)
    CardView statusPhoneContainer;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.numberPhone)
    TextView numberPhone;
    @BindView(R.id.status_date)
    TextView status_date;
    @BindView(R.id.send_message)
    AppCompatImageView sendMessageBtn;
    @BindView(R.id.call_voice)
    AppCompatImageView callVideoBtn;
    @BindView(R.id.call_video)
    AppCompatImageView callVoiceBtn;
    @BindView(R.id.MembersList)
    RecyclerView MembersList;
    @BindView(R.id.participantContainer)
    CardView participantContainer;
    @BindView(R.id.participantContainerExit)
    LinearLayout participantContainerExit;
    @BindView(R.id.participantContainerDelete)
    LinearLayout participantContainerDelete;
    @BindView(R.id.participantCounter)
    TextView participantCounter;
    @BindView(R.id.media_counter)
    TextView mediaCounter;
    @BindView(R.id.media_section)
    CardView mediaSection;

    @BindView(R.id.mediaProfileList)
    RecyclerView mediaList;
    @BindView(R.id.shareBtn)
    FloatingActionButton shareBtn;

    private CompositeDisposable mDisposable;
    private MediaProfileAdapter mMediaProfileAdapter;
    private GroupMembersAdapter mGroupMembersAdapter;
    private UsersModel mContactsModel;
    private GroupModel mGroupsModel;
    public String userID;
    public String groupID;
    private boolean isGroup;
    private int mutedColor;
    private int mutedColorStatusBar;
    int numberOfColors = 24;
    private ProfilePresenter mProfilePresenter;
    private boolean isAnAdmin;
    private boolean isLeft;
    private APIService mApiService;
    private String PicturePath;
    private Intent mIntent;
    private String name = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        initializerView();

        mDisposable = new CompositeDisposable();
        if (getIntent().hasExtra("userID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            userID = getIntent().getExtras().getString("userID");
        }


        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getString("groupID");
        }
        mApiService = new APIService(this);
        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.onCreate();


        participantContainerExit.setOnClickListener(v -> {

            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }


        });

        participantContainerDelete.setOnClickListener(v -> {
            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }
        });
        callVideoBtn.setOnClickListener(view -> makeCall(true));
        callVideoBtn.setOnClickListener(view -> makeCall(true));

        EditGroupBtn.setOnClickListener(view -> launchEditGroupName());
        sendMessageBtn.setOnClickListener(view -> sendMessage(mContactsModel));
        shareBtn.setOnClickListener(view -> shareContact(mContactsModel));

    }


    private void makeCall(boolean isVideoCall) {
        if (!isVideoCall) {
            CallManager.callContact(ProfileActivity.this, false, userID);
        } else {
            CallManager.callContact(ProfileActivity.this, true, userID);
        }
    }


    /**
     * method to initialize group members view
     */
    private void initializerGroupMembersView() {

        participantContainer.setVisibility(View.VISIBLE);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mGroupMembersAdapter = new GroupMembersAdapter(this);
        MembersList.setLayoutManager(mLinearLayoutManager);
        MembersList.setAdapter(mGroupMembersAdapter);

    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mediaList.setLayoutManager(linearLayoutManager);
        mMediaProfileAdapter = new MediaProfileAdapter(this);
        mediaList.setAdapter(mMediaProfileAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroup) {
            if (isAnAdmin) {
                getMenuInflater().inflate(R.menu.profile_menu_group_add, menu);
            } /*else {
                if (!left)
                    getMenuInflater().inflate(R.menu.profile_menu_group, menu);
            }*/

        } else {
            if (mContactsModel != null)
                if (UtilsPhone.checkIfContactExist(this, mContactsModel.getPhone())) {
                    if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                        getMenuInflater().inflate(R.menu.profile_menu_mine, menu);
                    } else {
                        getMenuInflater().inflate(R.menu.profile_menu, menu);
                    }
                } else if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                    getMenuInflater().inflate(R.menu.profile_menu_mine, menu);
                } else {
                    getMenuInflater().inflate(R.menu.profile_menu_user_not_exist, menu);
                }

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == R.id.add_contact) {
            Intent mIntent = new Intent(this, AddNewMembersToGroupActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("profileAdd", "add");
            startActivity(mIntent);
        } else if (item.getItemId() == R.id.edit_contact) {
            editContact(mContactsModel);
        } else if (item.getItemId() == R.id.view_contact) {
            viewContact(mContactsModel);
        } else if (item.getItemId() == R.id.add_new_contact) {
            addNewContact();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.media_selection)
    public void launchMediaActivity() {

        if (isGroup) {
            mIntent = new Intent(this, MediaActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("isGroup", true);
            mIntent.putExtra("Username", mGroupsModel.getImage());
            startActivity(mIntent);
            AnimationsUtil.setTransitionAnimation(this);

        } else {
            String finalName;
            if (mContactsModel.getUsername() != null) {
                finalName = mContactsModel.getUsername();
            } else {

                finalName = mContactsModel.getDisplayed_name();

            }
            mIntent = new Intent(this, MediaActivity.class);
            mIntent.putExtra("userID", userID);
            mIntent.putExtra("isGroup", false);
            mIntent.putExtra("Username", finalName);
            startActivity(mIntent);
            AnimationsUtil.setTransitionAnimation(this);

        }
    }

    private void addNewContact() {
        try {
            Intent mIntent = new Intent(Intent.ACTION_INSERT);
            mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            mIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mContactsModel.getPhone());
            startActivityForResult(mIntent, AppConstants.SELECT_ADD_NEW_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void launchEditGroupName() {
        try {

            Intent mIntent = new Intent(this, EditGroupActivity.class);
            if (mGroupsModel.getName() == null || mGroupsModel.getName().equals("null"))
                mIntent.putExtra("currentGroupName", "");
            else
                mIntent.putExtra("currentGroupName", mGroupsModel.getName());
            mIntent.putExtra("groupID", mGroupsModel.get_id());
            startActivity(mIntent);

        } catch (Exception e) {
            AppHelper.LogCat("Error   UI Exception " + e.getMessage());
        }
    }

    public void ShowContact(UsersModel contactsModel) {
        mContactsModel = contactsModel;

        updateUI(null, contactsModel, null);

    }

    public void ShowMedia(List<MessageModel> messagesModel) {
        if (messagesModel.size() != 0) {
            mediaSection.setVisibility(View.VISIBLE);
            mediaCounter.setText(String.valueOf(messagesModel.size()));
            mMediaProfileAdapter.setMessages(messagesModel);

        } else {
            mediaSection.setVisibility(View.GONE);
        }

    }

    public void ShowGroup(GroupModel groupsModel, List<MembersModel> membersModels) {
        mGroupsModel = groupsModel;
        try {
            updateUI(mGroupsModel, null, membersModels);
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            AppHelper.LogCat(e);
        }
    }

    @SuppressLint({"StaticFieldLeak", "CheckResult"})
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateUI(GroupModel mGroupsModel, UsersModel mContactsModel, List<MembersModel> membersModels) {


        if (isGroup) {
            GroupTitleContainer.setVisibility(View.VISIBLE);
            statusPhoneContainer.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            for (MembersModel membersModel : membersModels) {

                if (membersModel.getOwnerId().equals(PreferenceManager.getInstance().getID(this))) {
                    isAnAdmin = membersModel.isAdmin();
                    isLeft = membersModel.isLeft();
                    invalidateOptionsMenu();
                    break;
                }
            }
            ShowGroupMembers(membersModels);

            if (isAnAdmin) {
                EditGroupBtn.setVisibility(View.VISIBLE);
                if (isLeft) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                }
            } else {
                if (isLeft) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                    EditGroupBtn.setVisibility(View.GONE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                    EditGroupBtn.setVisibility(View.VISIBLE);
                }
            }


            DateTime messageDate = UtilsTime.getCorrectDate(mGroupsModel.getCreated());
            String groupDate = UtilsTime.convertDateToStringFormat(this, messageDate);
            if (mGroupsModel.getOwnerId().equals(PreferenceManager.getInstance().getID(this))) {
                mCreatedTitle.setText(String.format(getString(R.string.created_by_you_at) + " %s", groupDate));
            } else {
                String name = UtilsPhone.getContactName(mGroupsModel.getOwner_phone());
                if (name != null) {
                    mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", name, groupDate));
                } else {
                    mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", mGroupsModel.getOwner_phone(), groupDate));
                }
            }
            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10)
                getSupportActionBar().setTitle(name.substring(0, 10) + "... " + "");
            else
                getSupportActionBar().setTitle(name);


            String ImageUrl = mGroupsModel.getImage();
            String groupId = mGroupsModel.get_id();


            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_group_simple);

            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);

                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageDrawable(drawable);
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    UserCover.setImageDrawable(drawable);

                }
            };

            if (!ProfileActivity.this.isFinishing()) {
                try {
                    GlideApp.with(ProfileActivity.this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                            .into(target);

                } catch (Exception e) {
                    AppHelper.LogCat(e.getMessage());
                }


            }


            UserCover.setOnClickListener(view -> {
                BottomSheetEditGroupImage bottomSheetEditGroupImage = new BottomSheetEditGroupImage();
                bottomSheetEditGroupImage.show(getSupportFragmentManager(), bottomSheetEditGroupImage.getTag());
            });


            //    APIHelper.initializeApiGroups().updateGroupMembers(mGroupsModel.getId()).subscribe(this::ShowGroupMembers, this::onErrorLoading);


        } else {
            EditGroupBtn.setVisibility(View.GONE);
            if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                sendMessageBtn.setVisibility(View.GONE);
                callVideoBtn.setVisibility(View.GONE);
                callVoiceBtn.setVisibility(View.GONE);
                shareBtn.setVisibility(View.GONE);
            } else {
                sendMessageBtn.setVisibility(View.VISIBLE);
                callVideoBtn.setVisibility(View.VISIBLE);
                callVoiceBtn.setVisibility(View.VISIBLE);
                shareBtn.setVisibility(View.VISIBLE);
            }

          /*  if (mContactsModel.getUsername() != null) {
                name = mContactsModel.getUsername();
            } else {*/
            name = mContactsModel.getDisplayed_name();
            if (name == null) {
                name = mContactsModel.getPhone();
            }
            //  }
            getSupportActionBar().setTitle(name);
            GroupTitleContainer.setVisibility(View.GONE);
            statusPhoneContainer.setVisibility(View.VISIBLE);

            if (mContactsModel.getStatus() != null) {

                String Status = UtilsString.unescapeJava(mContactsModel.getStatus().getBody());

                status.setText(Status);
                status.setVisibility(View.VISIBLE);
                status_date.setVisibility(View.VISIBLE);
                DateTime messageDate = UtilsTime.getCorrectDate(mContactsModel.getStatus().getCreated());
                status_date.setText(UtilsTime.convertDateToStringFormat(this, messageDate));
            } else {

                status.setVisibility(View.GONE);
                status_date.setVisibility(View.GONE);
            }
            numberPhone.setText(mContactsModel.getPhone());
            String userImageUrl = mContactsModel.getImage();
            String userId = mContactsModel.get_id();


            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_user_simple);

            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });


                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);

                    UserCover.setImageDrawable(drawable);

                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);

                    UserCover.setImageDrawable(drawable);

                }
            };


            GlideApp.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + userImageUrl))
                    .signature(new ObjectKey(userImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);


            if (userImageUrl != null) {
                if (FilesManager.isFilePhotoProfileExists(this, FilesManager.getProfileImage(userImageUrl))) {
                    UserCover.setOnClickListener(view -> AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE, userImageUrl, userId));
                } else {
                    UserCover.setOnClickListener(view -> AppHelper.LaunchImagePreviewActivity(ProfileActivity.this, AppConstants.PROFILE_IMAGE_FROM_SERVER, userImageUrl, userId));
                }
            }


        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy();

    }


    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Profile throwable " + throwable.getMessage());
    }

    public void onErrorDeleting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_delete_this_group_check_connection), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

    }

    public void onErrorExiting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_exit_this_group_check_connection), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

    }

    /**
     * method to show group members list
     *
     * @param membersGroupModels this is parameter for ShowGroupMembers  method
     */
    public void ShowGroupMembers(List<MembersModel> membersGroupModels) {
        AppHelper.LogCat("membersGroupModels " + membersGroupModels.size());


        if (membersGroupModels.size() != 0) {
            initializerGroupMembersView();
            mGroupMembersAdapter.setMembers(membersGroupModels);
            participantCounter.setText(String.valueOf(membersGroupModels.size()));
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {

            if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read storage data permission already granted.");
                switch (requestCode) {
                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        break;
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(this, data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(this, data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = this.getContentResolver()
                                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String imageLocation = cursor.getString(1);
                                    cursor.close();
                                    File imageFile = new File(imageLocation);
                                    if (imageFile.exists()) {
                                        imagePath = imageFile.getPath();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("error" + e);
                            }
                        }
                        break;
                }


                if (imagePath != null) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_PATH_GROUP, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                AppHelper.LogCat("Please request Read contact data permission.");
            }

        }
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onEventMainThread(Pusher pusher) {

        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_DELETE_GROUP:
                AppHelper.Snackbar(this, containerProfile, pusher.getData(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                new Handler().postDelayed(this::finish, 500);
                break;
            case AppConstants.EVENT_BUS_PATH_GROUP:
                PicturePath = pusher.getData();
                try {
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
                                AppHelper.LogCat("requestFile is null " + requestFile);
                                AppHelper.CustomToast(ProfileActivity.this, getString(R.string.oops_something));
                            } else {
                                AppHelper.LogCat("requestFile is  " + requestFile);
                                AppHelper.LogCat("PicturePath is  " + PicturePath);

                                File file = new File(PicturePath);
                                mDisposable.add(APIHelper.initializeUploadFiles().uploadGroupImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(filesResponse -> {
                                            AppHelper.hideDialog();
                                            if (filesResponse.isSuccess()) {
                                                String groupId = groupID;

                                                mDisposable.addAll(APIHelper.initializeApiGroups().editGroupImage(filesResponse.getFilename(), groupId).subscribe(statusResponse -> {
                                                    if (statusResponse.isSuccess()) {


                                                        AppExecutors.getInstance().diskIO().execute(() -> {


                                                            GroupModel groupsModel = UsersController.getInstance().getGroupById(groupId);
                                                            groupsModel.setImage(filesResponse.getFilename());

                                                            UsersController.getInstance().updateGroup(groupsModel);

                                                            ConversationModel conversationModel = MessagesController.getInstance().getChatByGroupId(groupId);
                                                            conversationModel.setGroup_image(groupsModel.getImage());

                                                            MessagesController.getInstance().updateChat(conversationModel);

                                                            JSONObject jsonObject = new JSONObject();
                                                            try {
                                                                jsonObject.put("ownerId", groupId);
                                                                jsonObject.put("is_group", true);
                                                                jsonObject.put("image", filesResponse.getFilename());
                                                                try {
                                                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishProfileImageUpdated(groupId, jsonObject);
                                                                } catch (MqttException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }


                                                            runOnUiThread(() -> {
                                                                setImage(filesResponse.getFilename(), groupId);
                                                                AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                                            });


                                                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, conversationModel.get_id()));
                                                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY));


                                                        });
                                                    } else {
                                                        AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                                    }

                                                }, throwable -> {
                                                    AppHelper.LogCat(throwable.getMessage());
                                                }));

                                            } else {
                                                AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                            }
                                        }, throwable -> {
                                            throwable.printStackTrace();
                                            AppHelper.hideDialog();
                                            AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                                            AppHelper.CustomToast(ProfileActivity.this, getString(R.string.oops_something));
                                        }));
                            }
                        }
                    };
                    imageCompression.execute(PicturePath);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                    AppHelper.CustomToast(this, getString(R.string.oops_something));
                }
                break;
            case AppConstants.EVENT_BUS_ADD_MEMBER:
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;
            case AppConstants.EVENT_BUS_EXIT_THIS_GROUP:
                participantContainerExit.setVisibility(View.GONE);
                participantContainerDelete.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;
            case AppConstants.EVENT_BUS_UPDATE_GROUP_NAME:
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;

        }


    }


    private void editContact(UsersModel mContactsModel) {
        if (userID.equals(PreferenceManager.getInstance().getID(this))) {
            AppHelper.LaunchActivity(this, EditProfileActivity.class);
        } else {
            long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
            try {
                if (ContactID != 0) {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
                    startActivity(intent);
                }
            } catch (Exception e) {
                AppHelper.LogCat("error edit contact " + e.getMessage());
            }
        }
    }

    private void viewContact(UsersModel mContactsModel) {
        long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
        try {
            if (ContactID != 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
                startActivity(intent);
            }
        } catch (Exception e) {
            AppHelper.LogCat("error view contact " + e.getMessage());
        }
    }

    private void sendMessage(UsersModel mContactsModel) {
        Intent messagingIntent = new Intent(this, MessagesActivity.class);
        //  messagingIntent.putExtra("conversationID", "");
        messagingIntent.putExtra("recipientID", mContactsModel.get_id());
        messagingIntent.putExtra("isGroup", false);
        startActivity(messagingIntent);
        finish();
    }


    private void shareContact(UsersModel mContactsModel) {
        if (mContactsModel == null) return;
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        String subject = null;
        if (mContactsModel.getUsername() != null) {
            subject = mContactsModel.getUsername();
        }
        if (mContactsModel.getPhone() != null) {
            if (subject != null) {
                subject = subject + " " + mContactsModel.getPhone();
            } else {
                subject = mContactsModel.getPhone();
            }
        }
        if (subject != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareContact)));
    }


    @SuppressLint("StaticFieldLeak")
    private void setImage(String ImageUrl, String groupId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String groupImage = mGroupsModel.getImage();
        Bitmap holderBitmap = ImageLoader.GetCachedBitmapImage(groupImage, ProfileActivity.this, groupID, AppConstants.GROUP, AppConstants.FULL_PROFILE);
        if (holderBitmap != null) {
            Drawable drawable;
            drawable = new BitmapDrawable(getResources(), holderBitmap);
            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);

                    // AnimationsUtil.expandToolbar(containerProfile, holderBitmap, AppBarLayout);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageBitmap(holderBitmap);
                    Palette.from(holderBitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception ex) {
                                AppHelper.LogCat(" " + ex.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);

                    UserCover.setImageBitmap(holderBitmap);
                    Palette.from(holderBitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }
            };
            GlideApp.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);

        } else {
            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_group_simple);
            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    UserCover.setImageDrawable(placeholder);
                }
            };
            GlideApp.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);
        }

    }


    public void UpdateGroupUI(GroupModel groupsModel, List<MembersModel> membersModels) {
        try {
            updateUI(groupsModel, null, membersModels);
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

}
