package com.strolink.whatsUp.activities.messages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.transition.TransitionManager;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.main.MainActivity;
import com.strolink.whatsUp.activities.profile.ProfileActivity;
import com.strolink.whatsUp.activities.settings.PreferenceSettingsManager;
import com.strolink.whatsUp.adapters.others.TextWatcherAdapter;
import com.strolink.whatsUp.adapters.recyclerView.messages.MessagesAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.Files.cache.ImageLoader;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.call.CallManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.helpers.utils.concurrent.ListenableFuture;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.notifications.NotificationsModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.messages.MessagesPresenter;
import com.strolink.whatsUp.ui.ColorGenerator;
import com.strolink.whatsUp.ui.HideShowScrollListener;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;
import com.strolink.whatsUp.ui.audio.AudioRecorder;
import com.strolink.whatsUp.ui.views.AnimatingToggle;
import com.strolink.whatsUp.ui.views.AttachmentLayout;
import com.strolink.whatsUp.ui.views.CustomInputLayout;
import com.strolink.whatsUp.ui.views.HidingLinearLayout;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Abderrahim El imame on 05/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesActivity extends BaseActivity implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback,
        CustomInputLayout.Listener {


    @BindView(R.id.fab_scroll)
    FloatingActionButton fabScrollDown;

    @BindView(R.id.conversation_container)
    LinearLayout mView;

    @BindView(R.id.listMessages)
    RecyclerView messagesList;

    @BindView(R.id.add_contact)
    TextView AddContactBtn;

    @BindView(R.id.block_user)
    TextView BlockContactBtn;

    @BindView(R.id.unblock_user)
    TextView UnBlockContactBtn;

    @BindView(R.id.block_layout)
    FrameLayout blockLayout;


    @BindView(R.id.toolbar_title)
    EmojiTextView ToolbarTitle;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_image)
    AppCompatImageView ToolbarImage;

    @BindView(R.id.toolbar_status)
    TextView statusUser;

    @BindView(R.id.toolbarLinear)
    LinearLayout ToolbarLinearLayout;

    @BindView(R.id.arrow_back)
    LinearLayout BackButton;

    @BindView(R.id.groupSend)
    LinearLayout groupLeftSendMessageLayout;


    @BindView(R.id.layout_container)
    LinearLayout container;

    @BindView(R.id.button_toggle)
    AnimatingToggle buttonToggle;

    @BindView(R.id.send_buttonn)
    AppCompatImageButton sendButtonn;

    @BindView(R.id.attach_button)
    ImageButton attachButton;


    @BindView(R.id.quick_attachment_toggle)
    HidingLinearLayout quickAttachmentToggle;

    @BindView(R.id.bottom_panel)
    CustomInputLayout inputPanel;

    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;

    @BindView(R.id.quick_camera_toggle)
    ImageButton quickCameraToggle;


    //repliyed view

    @BindView(R.id.replied_message_view)
    public View replied_message_view;

    @BindView(R.id.color_view)
    public View color_view;

    @BindView(R.id.owner_name)
    public AppCompatTextView owner_name;

    @BindView(R.id.message_type)
    public AppCompatTextView message_type;

    @BindView(R.id.short_message)
    public EmojiTextView short_message;


    @BindView(R.id.message_file_thumbnail)
    public AppCompatImageView message_file_thumbnail;

    @BindView(R.id.clear_btn_reply_view)
    public AppCompatImageView clear_btn_reply_view;


    private AttachmentLayout attachmentLayout;

    private EmojiPopup emojiPopup;


    public Intent mIntent = null;
    public MessagesAdapter mMessagesAdapter;
    private PreCachingLayoutManager mLayoutManagerMessages;
    private String messageTransfer = null;
    private UsersModel mUsersModelRecipient;
    private String FileSize = "0";
    private String Duration = "0";
    private String FileImagePath = null;
    private String FileVideoPath = null;
    private String FileAudioPath = null;
    private String FileGifPath = null;
    private String FileDocumentPath = null;
    private String longitude = null;
    private String latitude = null;
    private MessagesPresenter mMessagesPresenter;
    private String ConversationID;
    private String reply_id;
    private String groupID;
    private boolean isGroup;


    private String senderId;
    private String recipientId;

    public boolean isGroup() {
        return isGroup;
    }

    public String getConversationID() {
        return ConversationID;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }


    /* for serach */
    @BindView(R.id.close_btn_search_view)
    AppCompatImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    AppCompatImageView clearBtn;
    @BindView(R.id.app_bar_search_view)
    View searchView;


    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;


    private Uri mProcessingPhotoUri;
    private AudioRecorder audioRecorder;
    Timer mPauseComposeTimer = new Timer();
    private boolean isTyping = false;
    private static final int TYPING_TIMER_LENGTH = 600;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);
        mMessagesPresenter = new MessagesPresenter(MessagesActivity.this);
        ToolbarTitle.setSelected(true);
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("recipientID")) {
                recipientId = getIntent().getExtras().getString("recipientID");
            }
            if (getIntent().hasExtra("groupID")) {
                groupID = getIntent().getExtras().getString("groupID");
            }

            if (getIntent().hasExtra("conversationID")) {
                ConversationID = getIntent().getExtras().getString("conversationID");
            }
            if (getIntent().hasExtra("isGroup")) {
                isGroup = getIntent().getExtras().getBoolean("isGroup");
            }

        }

        if (ConversationID == null) {
            if (getIntent().hasExtra("recipientID")) {
                ConversationID = MessagesController.getInstance().getChatIdByUserId(recipientId);
            } else if (getIntent().hasExtra("groupID")) {
                ConversationID = MessagesController.getInstance().getChatIdByGroupId(groupID);
            }


        }


        senderId = PreferenceManager.getInstance().getID(MessagesActivity.this);
        initializerSearchView(searchInput, clearBtn);
        initializerPanelView();
        initializerView();


        mMessagesPresenter.onCreate();
        intentHandler();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


    }


    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);

    }


    @Override
    protected void onPause() {
        super.onPause();
        mMessagesPresenter.onPause();
        inputPanel.onPause();
        saveMessageBeingComposed();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMessagesPresenter.onResume();
        emitMessageSeen();
        WorkJobsManager.getInstance().sendUserMessagesToServer();
        getMessageBeingComposed();

    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

    }


    private void intentHandler() {
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("messageCopied")) {
                ArrayList<String> messageCopied = getIntent().getExtras().getStringArrayList("messageCopied");
                for (String message : messageCopied) {
                    messageTransfer = message;
                    new Handler().postDelayed(this::sendMessage, 50);
                }
            } else if (getIntent().hasExtra("filePathList")) {
                ArrayList<String> filePathList = getIntent().getExtras().getStringArrayList("filePathList");
                File fileVideo = null;
                for (String filepath : filePathList) {
                    switch (FilesManager.getMimeType(filepath)) {
                        case "video/mp4": {
                            FileVideoPath = filepath;

                            Duration = FilesManager.getDuration(this, FileVideoPath);


                            File file = new File(FileVideoPath);
                            FileSize = String.valueOf(file.length());
                            break;
                        }
                        case "audio/mp3": {
                            FileAudioPath = filepath;
                            Duration = FilesManager.getDuration(this, FileAudioPath);
                            break;
                        }

                        case "application/msword":
                        case "application/pdf":
                        case "application/nd.ms-powerpoint":
                        case "application/vnd.ms-excel": {
                            FileDocumentPath = filepath;

                            File file = null;
                            if (FileDocumentPath != null) {
                                file = new File(FileDocumentPath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());

                            }
                            break;
                        }
                        case "image/jpeg":
                        case "image/png": {
                            FileImagePath = filepath;
                            File file = null;
                            if (FileImagePath != null) {
                                file = new File(FileImagePath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());

                            }


                            break;
                        }
                    }
                    sendMessage();
                }
            } else if (getIntent().hasExtra("filePath")) {
                String filepath = getIntent().getExtras().getString("filePath");
                File fileVideo = null;
                switch (FilesManager.getMimeType(filepath)) {
                    case "video/mp4": {
                        FileVideoPath = filepath;
                        Duration = FilesManager.getDuration(this, FileVideoPath);
                        File file = new File(FileVideoPath);
                        FileSize = String.valueOf(file.length());
                        break;
                    }
                    case "audio/mp3": {
                        FileAudioPath = filepath;
                        Duration = FilesManager.getDuration(this, FileAudioPath);

                        break;
                    }
                    case "application/pdf": {
                        FileDocumentPath = filepath;
                        File file = null;
                        if (FileDocumentPath != null) {
                            file = new File(FileDocumentPath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());

                        }
                        break;
                    }
                    case "image/jpeg":
                    case "image/png": {
                        FileImagePath = filepath;
                        File file = null;
                        if (FileImagePath != null) {
                            file = new File(FileImagePath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());

                        }
                        break;
                    }
                }
                sendMessage();
            }

        }
    }


    public void initializerPanelView() {

        inputPanel.setListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(container).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);

        quickCameraToggle.setOnClickListener(v -> {
            FilesManager.capturePhoto(MessagesActivity.this, AppConstants.PICK_CAMERA_MESSAGES, false);
        });
        attachmentLayout = null;
        ComposeKeyPressedListener composeKeyPressedListener = new ComposeKeyPressedListener();
        attachButton.setOnClickListener(v -> handleAddAttachment());
        sendButtonn.setEnabled(true);
        sendButtonn.setOnClickListener(v -> sendMessage());
        composeText.setOnKeyListener(composeKeyPressedListener);
        composeText.addTextChangedListener(composeKeyPressedListener);
        composeText.setOnClickListener(composeKeyPressedListener);
        composeText.setOnFocusChangeListener(composeKeyPressedListener);
        composeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        composeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        composeText.setSingleLine(false);

    }

    /**
     * method initialize the view
     */
    @SuppressLint("StaticFieldLeak")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mMessagesAdapter = new MessagesAdapter(GlideApp.with(this), this, messagesList);

        mMessagesAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        mLayoutManagerMessages = new PreCachingLayoutManager(getApplicationContext());
        mLayoutManagerMessages.setOrientation(RecyclerView.VERTICAL);
        mLayoutManagerMessages.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        mLayoutManagerMessages.setStackFromEnd(true);
        messagesList.setLayoutManager(mLayoutManagerMessages);
        messagesList.setAdapter(mMessagesAdapter);/*
        messagesList.setItemAnimator(new DefaultItemAnimator());
        messagesList.getItemAnimator().setChangeDuration(0);*/
        ((SimpleItemAnimator) messagesList.getItemAnimator()).setSupportsChangeAnimations(false);

        //fix slow recyclerview start
        messagesList.setHasFixedSize(true);
        messagesList.setItemViewCacheSize(20);
        messagesList.setDrawingCacheEnabled(true);
        messagesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end

        messagesList.addOnItemTouchListener(this);
        fabScrollDown.setOnClickListener(v -> scrollToEnd());
        messagesList.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
                fabScrollDown.hide();
            }

            @Override
            public void onShow() {
                fabScrollDown.show();
            }
        });
        audioRecorder = new AudioRecorder(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());

        String ImageUrl = PreferenceManager.getInstance().getWallpaper(this);
        if (ImageUrl != null) {


            Bitmap bitmap = ImageLoader.GetCachedBitmapImage(ImageUrl, MessagesActivity.this, PreferenceManager.getInstance().getID(MessagesActivity.this), AppConstants.USER, AppConstants.ROW_WALLPAPER);
            if (bitmap != null) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

                getWindow().setBackgroundDrawable(bitmapDrawable);
                // mView.setBackground(bitmapDrawable);
            } else {
                // mView.setBackground(AppHelper.getDrawable(MessagesActivity.this, R.drawable.bg_msg));
                getWindow().setBackgroundDrawable(AppHelper.getDrawable(MessagesActivity.this, R.drawable.bg_msg));
            }
        } else {
            //   mView.setBackground(AppHelper.getDrawable(this, R.drawable.bg_msg));
            getWindow().setBackgroundDrawable(AppHelper.getDrawable(MessagesActivity.this, R.drawable.bg_msg));
        }


        AddContactBtn.setOnClickListener(v -> addNewContact());

        BlockContactBtn.setOnClickListener(v -> {
            blockContact();
        });
        UnBlockContactBtn.setOnClickListener(v -> {
            unBlockContact();
        });

        ToolbarLinearLayout.setOnClickListener(v -> {
            if (isGroup) {

                mIntent = new Intent(this, ProfileActivity.class);
                mIntent.putExtra("groupID", groupID);
                mIntent.putExtra("isGroup", true);
                startActivity(mIntent);
            } else {
                mIntent = new Intent(this, ProfileActivity.class);
                mIntent.putExtra("userID", recipientId);
                mIntent.putExtra("isGroup", false);
                startActivity(mIntent);
            }
        });
        BackButton.setOnClickListener(v -> onBackPressed());


    }


    private void handleAddAttachment() {
        if (attachmentLayout == null) {
            attachmentLayout = new AttachmentLayout(this, LoaderManager.getInstance(this), new AttachmentTypeListener());
        }
        attachmentLayout.show(this, attachButton);

    }

    private boolean isMessageBeingComposed() {

        ConversationModel conversationModel = MessagesController.getInstance().getChatById(ConversationID);
        if (conversationModel == null) return false;
        String message = conversationModel.getMessageBeingComposed();
        return message != null && message.length() > 0;
    }

    private void getMessageBeingComposed() {


        ConversationModel conversationModel = MessagesController.getInstance().getChatById(ConversationID);
        if (conversationModel == null) return;
        String message = conversationModel.getMessageBeingComposed();
        if (message != null && message.length() > 0) {
            composeText.setText(message);

        }


    }

    private void scrollToEnd() {
        if (mMessagesAdapter != null) {
            if (!isMessagesListScrolledToBottom() && messagesList != null) {
                messagesList.scrollToPosition(mMessagesAdapter.getItemCount() - 1);
            }
        }
    }

    private boolean isMessagesListScrolledToBottom() {
        int lastPosition = mLayoutManagerMessages.findLastVisibleItemPosition();
        return !(lastPosition <= mMessagesAdapter.getItemCount() - 2);
    }

    @SuppressLint("CheckResult")
    private void unBlockContact() {


        AlertDialog.Builder builderUnblock = new AlertDialog.Builder(this);
        builderUnblock.setMessage(R.string.unblock_user_make_sure);
        builderUnblock.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {

            APIHelper.initialApiUsersContacts().unbBlock(recipientId).subscribe(blockResponse -> {
                if (blockResponse.isSuccess()) {


                    UsersBlockModel usersBlockModel2 = UsersController.getInstance().getUserBlockedById(recipientId);
                    UsersController.getInstance().deleteUserBlocked(usersBlockModel2);


                    refreshMenu();
                    if (AddContactBtn.getVisibility() == View.VISIBLE) {
                        UnBlockContactBtn.setVisibility(View.GONE);
                        BlockContactBtn.setVisibility(View.VISIBLE);
                    }
                    AppHelper.Snackbar(this, mView, blockResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

                } else {
                    AppHelper.Snackbar(this, mView, blockResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                AppHelper.CustomToast(this, getString(R.string.oops_something));
            });


        });

        builderUnblock.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builderUnblock.show();
    }

    @SuppressLint("CheckResult")
    private void blockContact() {


        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage(R.string.block_user_make_sure);
        builder2.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {

            APIHelper.initialApiUsersContacts().block(recipientId).subscribe(blockResponse -> {
                if (blockResponse.isSuccess()) {


                    UsersModel usersModel = UsersController.getInstance().getUserById(recipientId);
                    UsersBlockModel usersBlockModel = new UsersBlockModel();
                    usersBlockModel.setB_id(DbBackupRestore.getBlockUserLastId());
                    usersBlockModel.setUsersModel(usersModel);
                    UsersController.getInstance().insertUserBlocked(usersBlockModel);


                    refreshMenu();
                    if (AddContactBtn.getVisibility() == View.VISIBLE) {
                        BlockContactBtn.setVisibility(View.GONE);
                        UnBlockContactBtn.setVisibility(View.VISIBLE);
                    }


                    AppHelper.Snackbar(this, mView, blockResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                } else {
                    AppHelper.Snackbar(this, mView, blockResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                AppHelper.LogCat("throwable " + throwable.getMessage());
                AppHelper.CustomToast(this, getString(R.string.oops_something));
            });


        });

        builder2.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builder2.show();
    }

    private void addNewContact() {
        try {
            Intent mIntent = new Intent(Intent.ACTION_INSERT);
            mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            mIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mUsersModelRecipient.getPhone());
            startActivityForResult(mIntent, AppConstants.SELECT_ADD_NEW_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FileAudioPath = null;
        FileVideoPath = null;
        FileDocumentPath = null;
        FileImagePath = null;
        FileGifPath = null;
        latitude = null;
        longitude = null;
        FileSize = "0";
        Duration = "0";
        // Get file from file name
        File file = null;


        AppHelper.LogCat("resultCode " + resultCode);
        AppHelper.LogCat("requestCode " + requestCode);
        if (resultCode == RESULT_OK) {

            if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                switch (requestCode) {

                    case AppConstants.PICK_GALLERY_MESSAGES:


                        String mimeType = FilesManager.getMimeType(this, data.getData());
                        AppHelper.LogCat("Read contfact data permission already granted. " + mimeType);
                        if (FilesManager.isGif(mimeType)) {


                            FileGifPath = FilesManager.getPath(getApplicationContext(), data.getData());
                            if (FileGifPath == null) {
                                FileGifPath = FilesManager.copyDocumentToCache(data.getData(), ".gif");
                            }
                            if (FileGifPath != null) {
                                file = new File(FileGifPath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());
                            }
                            sendMessage();
                        } else if (FilesManager.isVideo(mimeType)) {
                            try {
                                FileVideoPath = FilesManager.getPath(getApplicationContext(), data.getData());

                                if (FileVideoPath == null) {
                                    FileVideoPath = FilesManager.copyDocumentToCache(data.getData(), ".mp4");
                                }


                                if (FileVideoPath != null) {
                                    file = new File(FileVideoPath);
                                    Duration = FilesManager.getDuration(this, FileVideoPath);

                                }
                                if (file != null) {
                                    FileSize = String.valueOf(file.length());
                                }

                                sendMessage();
                            } catch (Exception e) {
                                AppHelper.LogCat(" Exception " + e.getMessage());
                                return;
                            }
                        } else if (FilesManager.isImageType(mimeType)) {

                            FileImagePath = FilesManager.getPath(getApplicationContext(), data.getData());
                            AppHelper.LogCat("FileImagePath 1 " + FileImagePath);
                            if (FileImagePath == null) {
                                FileImagePath = FilesManager.copyDocumentToCache(data.getData(), ".jpg");
                            }
                            AppHelper.LogCat("FileImagePath " + FileImagePath);
                            if (FileImagePath != null) {
                                file = new File(FileImagePath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());
                            }
                            sendMessage();
                        }

                        break;

                    case AppConstants.PICK_CAMERA_MESSAGES:
                        AppHelper.LogCat("mProcessingPhotoUri " + mProcessingPhotoUri);
                        String pathReturned = data.getStringExtra(AppConstants.MediaConstants.EXTRA_EDITED_PATH);
                        if (data.getStringExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE) != null)
                            messageTransfer = data.getStringExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE);
                        mProcessingPhotoUri = FilesManager.getFile(new File(pathReturned));
                        AppHelper.LogCat("mProcessingPhotoUri2 " + mProcessingPhotoUri);

                        mimeType = FilesManager.getMimeType(this, mProcessingPhotoUri);
                        if (FilesManager.isGif(mimeType)) {


                            FileGifPath = FilesManager.getPath(getApplicationContext(), mProcessingPhotoUri);
                            if (FileGifPath == null) {
                                FileGifPath = FilesManager.copyDocumentToCache(mProcessingPhotoUri, ".gif");
                            }
                            if (FileGifPath != null) {
                                file = new File(FileGifPath);
                            }
                            if (file != null) {
                                FileSize = String.valueOf(file.length());
                            }
                            sendMessage();
                            mProcessingPhotoUri = null;
                        } else if (FilesManager.isVideo(mimeType)) {
                            try {
                                FileVideoPath = FilesManager.getPath(getApplicationContext(), mProcessingPhotoUri);
                                if (FileVideoPath == null) {
                                    FileVideoPath = FilesManager.copyDocumentToCache(mProcessingPhotoUri, ".mp4");
                                }
                                if (FileVideoPath != null) {
                                    AppHelper.LogCat("FileVideoPath " + FileVideoPath);
                                    file = new File(FileVideoPath);
                                    Duration = FilesManager.getDuration(this, FileVideoPath);

                                }
                                if (file != null) {
                                    FileSize = String.valueOf(file.length());
                                }

                                sendMessage();

                                mProcessingPhotoUri = null;
                            } catch (Exception e) {
                                AppHelper.LogCat(" Exception " + e.getMessage());
                                return;
                            }
                        } else if (FilesManager.isImageType(mimeType)) {
                            try {
                                FileImagePath = FilesManager.getPath(getApplicationContext(), mProcessingPhotoUri);

                                if (FileImagePath == null) {
                                    FileImagePath = FilesManager.copyDocumentToCache(mProcessingPhotoUri, ".jpg");
                                }
                                if (FileImagePath != null) {
                                    file = new File(FileImagePath);
                                }
                                if (file != null) {
                                    FileSize = String.valueOf(file.length());

                                }

                                sendMessage();
                                mProcessingPhotoUri = null;
                            } catch (Exception e) {
                                AppHelper.LogCat(" Exception " + e.getMessage());
                                return;
                            }
                        }


                        //   }
                        break;


                    case AppConstants.PICK_AUDIO_MESSAGES:
                        try {
                            FileAudioPath = FilesManager.getPath(getApplicationContext(), data.getData());
                            if (FileAudioPath == null) {
                                FileAudioPath = FilesManager.copyDocumentToCache(data.getData(), ".mp3");
                            }

                            Duration = FilesManager.getDuration(this, FileAudioPath);

                            sendMessage();
                        } catch (Exception e) {
                            AppHelper.LogCat(" Exception " + e.getMessage());
                            return;
                        }
                        break;

                    case AppConstants.PICK_DOCUMENT_MESSAGES:

                        FileDocumentPath = FilesManager.getPath(getApplicationContext(), data.getData());

                        if (FileDocumentPath == null) {
                            if (data.getData() != null) {
                                String mimeTyp = FilesManager.getMimeType(this, data.getData());
                                if (mimeTyp != null) {
                                    switch (mimeTyp) {
                                        case "application/msword":
                                            FileDocumentPath = FilesManager.copyDocumentToCache(data.getData(), ".doc");
                                            break;
                                        case "application/pdf":
                                            FileDocumentPath = FilesManager.copyDocumentToCache(data.getData(), ".pdf");
                                            break;
                                        case "application/nd.ms-powerpoint":
                                            FileDocumentPath = FilesManager.copyDocumentToCache(data.getData(), ".ppt");
                                            break;
                                        case "application/vnd.ms-excel":
                                            FileDocumentPath = FilesManager.copyDocumentToCache(data.getData(), ".xls");
                                            break;
                                    }
                                } else {
                                    FileDocumentPath = null;
                                }
                            } else {
                                FileDocumentPath = null;
                            }
                        }

                        if (FileDocumentPath != null) {
                            file = new File(FileDocumentPath);

                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());
                        }
                        if (FileDocumentPath == null) {
                            AppHelper.CustomToast(this, getString(R.string.oops_something));
                        } else {
                            if (!FileSize.equals("0")) {
                                sendMessage();
                            } else {
                                FileSize = null;
                                FileDocumentPath = null;
                                AppHelper.CustomToast(this, getString(R.string.this_type_of_file_not_supported));
                            }
                        }
                        break;

                    case AppConstants.PICK_CONTACT_INFO_MESSAGES:


                        Cursor cursor = null;
                        try {
                            String phoneNo = null;
                            String name = null;
                            // getData() method will have the Content Uri of the selected contact
                            Uri uri = data.getData();
                            // Log.e(AppConstants.TAG,"data" + uri);
                            //Query the content uri
                            cursor = getContentResolver().query(uri, null, null, null, null);
                            cursor.moveToFirst();
                            // column index of the phone number
                            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            // column index of the contact nameMessaging
                            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            phoneNo = cursor.getString(phoneIndex);
                            name = cursor.getString(nameIndex);
                            composeText.setText(String.format("%s : %s", name, phoneNo));
                            sendMessage();

                        } catch (Exception e) {
                            e.printStackTrace();
                            if (cursor != null)
                                cursor.close();
                        }
                        break;
                    case AppConstants.PICK_LOCATION_MESSAGES:

                        longitude = data.getStringExtra("longitude");
                        latitude = data.getStringExtra("latitude");
                        FileImagePath = data.getStringExtra("image");
                        if (FileImagePath == null) {
                            FileImagePath = FilesManager.copyDocumentToCache(Uri.parse(data.getStringExtra("image")), ".jpg");
                        }
                        if (FileImagePath != null) {
                            file = new File(FileImagePath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());
                        }
                        sendMessage();
                        break;
                    case AppConstants.PICK_GIF_MESSAGES:
                        AppHelper.LogCat("PICK_GIF_MESSAGES " + data.getData());

                        FileGifPath = FilesManager.getPath(getApplicationContext(), data.getData());
                        AppHelper.LogCat("FileGifPath " + FileGifPath);
                        if (FileGifPath == null) {
                            FileGifPath = FilesManager.copyDocumentToCache(data.getData(), ".gif");
                        }
                        if (FileGifPath != null) {
                            file = new File(FileGifPath);
                        }
                        if (file != null) {
                            FileSize = String.valueOf(file.length());
                        }
                        sendMessage();
                        break;

                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        mMessagesPresenter.getContactRecipientLocal();
                        break;

                }
            } else {

                Permissions.with(MessagesActivity.this)
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


    /**
     * method to send the new message
     */
    private void sendMessage() {


        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_START_CONVERSATION));//for change viewpager current item to 0
        String messageBody = UtilsString.escapeJava(composeText.getText().toString().trim());
        if (messageTransfer != null)
            messageBody = messageTransfer;

        if (FileImagePath == null && FileAudioPath == null && FileDocumentPath == null && FileVideoPath == null && FileGifPath == null) {
            if (messageBody.isEmpty()) return;
        }

        String sendTime = AppHelper.getCurrentTime();

        if (isGroup) {
            final JSONObject messageGroup = new JSONObject();
            try {
                messageGroup.put("messageBody", messageBody);
                messageGroup.put("date", sendTime);

                if (FileImagePath != null) {
                    messageGroup.put("file", FileImagePath);
                    messageGroup.put("file_type", AppConstants.MESSAGES_IMAGE);
                    messageGroup.put("document_name", "null");
                    messageGroup.put("document_type", "null");
                } else if (FileGifPath != null) {
                    messageGroup.put("file", FileGifPath);
                    messageGroup.put("file_type", AppConstants.MESSAGES_GIF);
                    messageGroup.put("document_name", "null");
                    messageGroup.put("document_type", "null");
                } else if (FileVideoPath != null) {
                    messageGroup.put("file", FileVideoPath);
                    messageGroup.put("file_type", AppConstants.MESSAGES_VIDEO);
                    messageGroup.put("document_name", "null");
                    messageGroup.put("document_type", "null");
                } else if (FileAudioPath != null) {
                    messageGroup.put("file", FileAudioPath);
                    messageGroup.put("file_type", AppConstants.MESSAGES_AUDIO);

                    messageGroup.put("document_name", "null");
                    messageGroup.put("document_type", "null");
                } else if (FileDocumentPath != null) {
                    messageGroup.put("file", FileDocumentPath);
                    messageGroup.put("file_type", AppConstants.MESSAGES_DOCUMENT);

                    messageGroup.put("document_name", FilesManager.getName(FileDocumentPath));

                    switch (FilesManager.getExtension(FilesManager.getName(FileDocumentPath))) {
                        case "pdf":
                            messageGroup.put("document_type", AppConstants.MESSAGES_DOCUMENT_PDF);
                            break;
                        case "doc":
                        case "docx":
                            messageGroup.put("document_type", AppConstants.MESSAGES_DOCUMENT_DOC);
                            break;
                        case "ppt":
                        case "pptx":
                            messageGroup.put("document_type", AppConstants.MESSAGES_DOCUMENT_PPT);
                            break;
                        case "xls":
                        case "xlsx":
                            messageGroup.put("document_type", AppConstants.MESSAGES_DOCUMENT_EXCEL);
                            break;
                    }

                } else {
                    messageGroup.put("file", "null");
                    messageGroup.put("file_type", "null");

                    messageGroup.put("document_name", "null");
                    messageGroup.put("document_type", "null");
                }


                if (reply_id != null)
                    messageGroup.put("reply_id", reply_id);
                else
                    messageGroup.put("reply_id", "null");

                messageGroup.put("reply_message", true);
                if (!FileSize.equals("0"))
                    messageGroup.put("fileSize", FileSize);
                else
                    messageGroup.put("fileSize", "0");

                if (!Duration.equals("0"))
                    messageGroup.put("duration", Duration);
                else
                    messageGroup.put("duration", "0");

                if (longitude != null)
                    messageGroup.put("longitude", longitude);
                else
                    messageGroup.put("longitude", "null");

                if (latitude != null)
                    messageGroup.put("latitude", latitude);
                else
                    messageGroup.put("latitude", "null");

            } catch (JSONException e) {
                AppHelper.LogCat("send group message " + e.getMessage());
            }
            setStatusAsWaiting(messageGroup, true);

        } else {
            final JSONObject message = new JSONObject();
            try {
                message.put("messageBody", messageBody);

                message.put("date", sendTime);

                if (FileImagePath != null) {
                    message.put("file", FileImagePath);
                    message.put("file_type", AppConstants.MESSAGES_IMAGE);
                    message.put("document_name", "null");
                    message.put("document_type", "null");
                } else if (FileGifPath != null) {
                    message.put("file", FileGifPath);
                    message.put("file_type", AppConstants.MESSAGES_GIF);
                    message.put("document_name", "null");
                    message.put("document_type", "null");
                } else if (FileVideoPath != null) {
                    message.put("file", FileVideoPath);
                    message.put("file_type", AppConstants.MESSAGES_VIDEO);

                    message.put("document_name", "null");
                    message.put("document_type", "null");
                } else if (FileAudioPath != null) {
                    message.put("file", FileAudioPath);
                    message.put("file_type", AppConstants.MESSAGES_AUDIO);

                    message.put("document_name", "null");
                    message.put("document_type", "null");
                } else if (FileDocumentPath != null) {
                    message.put("file", FileDocumentPath);
                    message.put("file_type", AppConstants.MESSAGES_DOCUMENT);

                    message.put("document_name", FilesManager.getName(FileDocumentPath));
                    switch (FilesManager.getExtension(FilesManager.getName(FileDocumentPath))) {
                        case "pdf":
                            message.put("document_type", AppConstants.MESSAGES_DOCUMENT_PDF);
                            break;
                        case "doc":
                        case "docx":
                            message.put("document_type", AppConstants.MESSAGES_DOCUMENT_DOC);
                            break;
                        case "ppt":
                        case "pptx":
                            message.put("document_type", AppConstants.MESSAGES_DOCUMENT_PPT);
                            break;
                        case "xls":
                        case "xlsx":
                            message.put("document_type", AppConstants.MESSAGES_DOCUMENT_EXCEL);
                            break;
                    }


                } else {
                    message.put("file", "null");
                    message.put("file_type", "null");
                    message.put("document_name", "null");
                    message.put("document_type", "null");
                }


                if (reply_id != null)
                    message.put("reply_id", reply_id);
                else
                    message.put("reply_id", "null");

                message.put("reply_message", true);


                if (longitude != null)
                    message.put("longitude", longitude);
                else
                    message.put("longitude", "null");

                if (latitude != null)
                    message.put("latitude", latitude);
                else
                    message.put("latitude", "null");


                if (!FileSize.equals("0"))
                    message.put("fileSize", FileSize);
                else
                    message.put("fileSize", "0");

                if (!Duration.equals("0"))
                    message.put("duration", Duration);
                else
                    message.put("duration", "0");
            } catch (JSONException e) {
                AppHelper.LogCat("send message " + e.getMessage());
            }
            setStatusAsWaiting(message, false);
        }
        composeText.setText("");
        messageTransfer = null;
        mProcessingPhotoUri = null;
        saveMessageBeingComposed();
    }


    /**
     * method to save new message as waitng messages
     *
     * @param data     this is the first parameter for setStatusAsWaiting method
     * @param is_group this is the second parameter for setStatusAsWaiting method
     */
    private void setStatusAsWaiting(JSONObject data, boolean is_group) {


        try {
            if (is_group) {

                String messageBody = data.getString("messageBody");
                String created = data.getString("date");
                String file = data.getString("file");
                String file_type = data.getString("file_type");
                String fileSize = data.getString("fileSize");
                String duration = data.getString("duration");
                String latitude = data.getString("latitude");
                String longitude = data.getString("longitude");

//new fields
                boolean reply_message = data.getBoolean("reply_message");
                String reply_id = data.getString("reply_id");
                String document_type = data.getString("document_type");
                String document_name = data.getString("document_name");


                String lastID = DbBackupRestore.getMessageLastId();


                UsersModel usersModelSender = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(this));
                GroupModel groupModel = UsersController.getInstance().getGroupById(groupID);

                ConversationModel conversationsModel = MessagesController.getInstance().getChatByGroupId(groupID);

                MessageModel messagesModel = new MessageModel();
                messagesModel.set_id(lastID);
                messagesModel.setCreated(created);
                messagesModel.setStatus(AppConstants.IS_WAITING);
                messagesModel.setGroupId(groupModel.get_id());
                messagesModel.setGroup_image(groupModel.getImage());
                messagesModel.setGroup_name(groupModel.getName());
                messagesModel.setSenderId(usersModelSender.get_id());
                messagesModel.setSender_image(usersModelSender.getImage());
                messagesModel.setSender_phone(usersModelSender.getPhone());
                messagesModel.setIs_group(true);
                messagesModel.setMessage(messageBody);
                messagesModel.setLatitude(latitude);
                messagesModel.setLongitude(longitude);
                messagesModel.setFile(file);
                messagesModel.setFile_type(file_type);
                messagesModel.setState(AppConstants.NORMAL_STATE);
                messagesModel.setFile_size(fileSize);
                messagesModel.setDuration_file(duration);
                messagesModel.setReply_id(reply_id);
                messagesModel.setReply_message(reply_message);
                messagesModel.setDocument_name(document_name);
                messagesModel.setDocument_type(document_type);
                if (!file.equals("null")) {
                    messagesModel.setFile_upload(false);

                } else {
                    messagesModel.setFile_upload(true);
                }
                messagesModel.setFile_downLoad(true);
                messagesModel.setConversationId(conversationsModel.get_id());

                MessagesController.getInstance().insertMessage(messagesModel);

                conversationsModel.setLatest_message_id(messagesModel.get_id());
                conversationsModel.setLatest_message(messagesModel.getMessage());
                conversationsModel.setFile_type(messagesModel.getFile_type());
                conversationsModel.setLatest_message_latitude(messagesModel.getLatitude());
                conversationsModel.setLatest_message_state(messagesModel.getState());
                conversationsModel.setLatest_message_created(messagesModel.getCreated());
                conversationsModel.setLatest_message_status(messagesModel.getStatus());
                conversationsModel.setLatest_message_sender_id(usersModelSender.get_id());
                conversationsModel.setLatest_message_sender_phone(usersModelSender.getPhone());
                String name = UtilsPhone.getContactName(usersModelSender.getPhone());
                conversationsModel.setLatest_message_sender__displayed_name(name);

                conversationsModel.setCreated(created);

                conversationsModel.setGroup_id(groupModel.get_id());
                conversationsModel.setGroup_image(groupModel.getImage());
                conversationsModel.setGroup_name(groupModel.getName());

                conversationsModel.setUnread_message_counter(0);


                MessagesController.getInstance().updateChat(conversationsModel);


                addMessage(lastID);

                if (!file.equals("null"))
                    return;

                ConversationID = conversationsModel.get_id();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, ConversationID));
                WorkJobsManager.getInstance().sendUserMessagesToServer();


            } else {


                String messageBody = data.getString("messageBody");
                String created = data.getString("date");
                String file = data.getString("file");
                String file_type = data.getString("file_type");
                String fileSize = data.getString("fileSize");
                String duration = data.getString("duration");
                String latitude = data.getString("latitude");
                String longitude = data.getString("longitude");


                boolean reply_message = data.getBoolean("reply_message");
                String reply_id = data.getString("reply_id");
                String document_type = data.getString("document_type");
                String document_name = data.getString("document_name");


                if (!MessagesController.getInstance().checkIfConversationExist(recipientId)) {

                    String lastID = DbBackupRestore.getMessageLastId();


                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    UsersModel usersModelRecipient = UsersController.getInstance().getUserById(recipientId);

                    String lastConversationID = DbBackupRestore.getConversationLastId();
                    //  String lastID = RealmBackupRestore.getMessageLastId();
                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(lastID);

                    messagesModel.setSenderId(usersModelSender.get_id());
                    messagesModel.setSender_image(usersModelSender.getImage());
                    messagesModel.setSender_phone(usersModelSender.getPhone());


                    messagesModel.setRecipientId(usersModelRecipient.get_id());
                    messagesModel.setRecipient_image(usersModelRecipient.getImage());
                    messagesModel.setRecipient_phone(usersModelRecipient.getPhone());


                    messagesModel.setCreated(created);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setIs_group(false);
                    messagesModel.setConversationId(lastConversationID);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setState(AppConstants.NORMAL_STATE);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);
                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);

                    if (!file.equals("null")) {
                        messagesModel.setFile_upload(false);

                    } else {
                        messagesModel.setFile_upload(true);
                    }
                    messagesModel.setFile_downLoad(true);

                    MessagesController.getInstance().insertMessage(messagesModel);

                    ConversationModel conversationsModel1 = new ConversationModel();
                    conversationsModel1.set_id(lastConversationID);

                    conversationsModel1.setOwner_id(usersModelRecipient.get_id());
                    conversationsModel1.setOwner_image(usersModelRecipient.getImage());
                    conversationsModel1.setOwner_phone(usersModelRecipient.getPhone());

                    String displayed_name = UtilsPhone.getContactName(usersModelRecipient.getPhone());
                    conversationsModel1.setOwner_displayed_name(displayed_name);

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

                    conversationsModel1.setCreated(created);
                    conversationsModel1.setIs_group(false);
                    conversationsModel1.setUnread_message_counter(0);
                    MessagesController.getInstance().insertChat(conversationsModel1);
                    ConversationID = lastConversationID;

                    addMessage(lastID);
                    if (!file.equals("null"))
                        return;


                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, ConversationID));
                    WorkJobsManager.getInstance().sendUserMessagesToServer();

                } else {
                    String conversationID = MessagesController.getInstance().getChatIdByUserId(recipientId);
                    String lastID = DbBackupRestore.getMessageLastId();


                    //   try {


                    // AppHelper.LogCat("last ID  message   MessagesActivity" + lastID);

                    UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                    UsersModel usersModelRecipient = UsersController.getInstance().getUserById(recipientId);
                    ConversationModel conversationsModel = MessagesController.getInstance().getChatById(conversationID);

                    MessageModel messagesModel = new MessageModel();
                    messagesModel.set_id(lastID);

                    messagesModel.setSenderId(usersModelSender.get_id());
                    messagesModel.setSender_image(usersModelSender.getImage());
                    messagesModel.setSender_phone(usersModelSender.getPhone());


                    messagesModel.setRecipientId(usersModelRecipient.get_id());
                    messagesModel.setRecipient_image(usersModelRecipient.getImage());
                    messagesModel.setRecipient_phone(usersModelRecipient.getPhone());

                    messagesModel.setCreated(created);
                    messagesModel.setStatus(AppConstants.IS_WAITING);
                    messagesModel.setIs_group(false);
                    messagesModel.setConversationId(conversationID);
                    messagesModel.setMessage(messageBody);
                    messagesModel.setState(AppConstants.NORMAL_STATE);
                    messagesModel.setLongitude(longitude);
                    messagesModel.setLatitude(latitude);
                    messagesModel.setFile(file);
                    messagesModel.setFile_type(file_type);
                    messagesModel.setFile_size(fileSize);
                    messagesModel.setDuration_file(duration);
                    messagesModel.setReply_id(reply_id);
                    messagesModel.setReply_message(reply_message);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);
                    if (!file.equals("null")) {
                        messagesModel.setFile_upload(false);

                    } else {
                        messagesModel.setFile_upload(true);
                    }
                    messagesModel.setFile_downLoad(true);

                    MessagesController.getInstance().insertMessage(messagesModel);

                    conversationsModel.setLatest_message_id(messagesModel.get_id());
                    conversationsModel.setLatest_message(messagesModel.getMessage());
                    conversationsModel.setFile_type(messagesModel.getFile_type());
                    conversationsModel.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel.setLatest_message_state(messagesModel.getState());
                    conversationsModel.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel.setLatest_message_sender_id(usersModelSender.get_id());
                    conversationsModel.setLatest_message_sender_phone(usersModelSender.getPhone());

                    String name = UtilsPhone.getContactName(usersModelSender.getPhone());
                    conversationsModel.setLatest_message_sender__displayed_name(name);

                    conversationsModel.setCreated(created);
                    MessagesController.getInstance().updateChat(conversationsModel);


                    addMessage(lastID);

                    if (!file.equals("null"))
                        return;

                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationID));
                    WorkJobsManager.getInstance().sendUserMessagesToServer();

              /*      } catch (Exception e) {
                        e.printStackTrace();
                        AppHelper.LogCat("Exception  last id message  MessagesActivity " + e.getMessage());
                    }*/


                }

            }


        } catch (JSONException e) {
            AppHelper.LogCat("JSONException  MessagesActivity " + e);
        } finally {

            FileAudioPath = null;
            FileVideoPath = null;
            FileDocumentPath = null;
            FileImagePath = null;
            FileGifPath = null;
            latitude = null;
            longitude = null;
            FileSize = "0";
            Duration = "0";
            closeReplyView();
        }

    }

    /**
     * refresh the menu for new contact
     * doesn't exist in contactModel
     */
    public void refreshMenu() {
        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
        if (isGroup) {
            if (UsersController.getInstance().memberIsLeft(senderId, groupID) > 0)
                getMenuInflater().inflate(R.menu.groups_menu_user_left, menu);
            else
                getMenuInflater().inflate(R.menu.groups_menu, menu);
        } else {

            if (mUsersModelRecipient != null)
                if (mUsersModelRecipient.getPhone() != null && UtilsPhone.checkIfContactExist(this, mUsersModelRecipient.getPhone())) {
                    if (MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {

                        getMenuInflater().inflate(R.menu.messages_menu_unblock, menu);

                    } else {
                        getMenuInflater().inflate(R.menu.messages_menu, menu);
                    }

                } else {
                    if (MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
                        getMenuInflater().inflate(R.menu.messages_menu_user_not_exist_unblock, menu);
                    } else {
                        getMenuInflater().inflate(R.menu.messages_menu_user_not_exist, menu);
                    }

                }


        }

        super.onCreateOptionsMenu(menu);
        return true;
    }


    private void makeCall(boolean isVideoCall) {
        if (isVideoCall) {
            CallManager.callContact(MessagesActivity.this, true, recipientId);
        } else {
            CallManager.callContact(MessagesActivity.this, false, recipientId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (isGroup) {

            switch (item.getItemId()) {
                case R.id.search_messages_group:
                    launcherSearchView();
                    break;
                case R.id.view_group:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent);
                    break;
            }
        } else {

            switch (item.getItemId()) {

                case R.id.call_video:
                    makeCall(true);
                    break;
                case R.id.call_voice:
                    makeCall(false);
                    break;
                case R.id.search_messages:
                    launcherSearchView();
                    break;
                case R.id.add_contact:
                    addNewContact();
                    break;
                case R.id.view_contact:
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", recipientId);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent);
                    break;
                case R.id.clear_chat:

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.clear_chat);
                    builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                        AppHelper.showDialog(this, getString(R.string.clear_chat));
                        mMessagesPresenter.deleteConversation(ConversationID);
                        AppHelper.hideDialog();

                    });

                    builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                    });

                    builder.show();

                    break;
                case R.id.block_user:
                    blockContact();
                    break;
                case R.id.unblock_user:
                    unBlockContact();
                    break;


            }
        }
        return true;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.clear_btn_reply_view)
    public void closeReplyView() {
        reply_id = null;
        // AnimationsUtil.animateWindowOutTranslate(replied_message_view);
        replied_message_view.setVisibility(View.GONE);
    }

    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                messagesList.smoothScrollToPosition(mMessagesAdapter.getItemCount());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to clear/reset search view
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.clear_btn_search_view)
    public void clearSearchView() {
        searchInput.setText("");
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(MessagesActivity.this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    /**
     * method to initialize the search view
     *
     * @param searchInput    this is the  first parameter for initializerSearchView method
     * @param clearSearchBtn this is the second parameter for initializerSearchView method
     */
    public void initializerSearchView(TextInputEditText searchInput, AppCompatImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                AppHelper.LogCat("Has focused");

            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mMessagesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);

                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this  is the parameter for Search method
     */
    public void Search(String string) {


        List<MessageModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {

            mMessagesAdapter.animateTo(filteredModelList);
            //  messagesList.smoothScrollToPosition(0);
            mLayoutManagerMessages.scrollToPositionWithOffset(0, 0);

        }

    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<MessageModel> FilterList(String query) {

        List<MessageModel> messagesModels;
        if (isGroup) {
            messagesModels = MessagesController.getInstance().getMessagesByQuery(ConversationID, query);
        } else {


            messagesModels = MessagesController.getInstance().loadAllMessagesQuery(ConversationID, query);


        }

        return messagesModels;
    }


    /**
     * method to emit that message are seen by user
     */
    private void emitMessageSeen() {
        if (isGroup) {
            WorkJobsManager.getInstance().sendSeenGroupStatusToServer(groupID, ConversationID);
        } else {
            if (!MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
                WorkJobsManager.getInstance().sendSeenStatusToServer(recipientId, ConversationID);
            }
        }
    }

    /**
     * method to show all user messages
     *
     * @param messagesModels this is parameter for ShowMessages method
     */
    public void ShowMessages(List<MessageModel> messagesModels) {

        mMessagesAdapter.setMessages(messagesModels);

    }


    /**
     * method to update group information
     *
     * @param groupsModel
     */
    @SuppressLint("StaticFieldLeak")
    public void updateGroupInfo(GroupModel groupsModel, List<MembersModel> groupsModelMembers) {


        refreshMenu();
        String groupImage = groupsModel.getImage();
        try {
            String name = UtilsString.unescapeJava(groupsModel.getName());
            ToolbarTitle.setText(name);

            if (groupsModel.getImage() != null) {
                GlideApp.with(getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + groupImage))

                        .signature(new ObjectKey(groupsModel.getImage()))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(AppHelper.getDrawable(this, R.drawable.holder_group))
                        .error(AppHelper.getDrawable(this, R.drawable.holder_group))
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(ToolbarImage);

            } else {
                ToolbarImage.setImageDrawable(AppHelper.getDrawable(this, R.drawable.holder_group));
            }


            if (groupsModelMembers == null || groupsModelMembers.size() == 0) return;
            int arraySize = groupsModelMembers.size();
            StringBuilder names = new StringBuilder();
            for (int x = 0; x <= arraySize - 1; x++) {
                if (!groupsModelMembers.get(x).isDeleted()) {
                    if (x <= 1) {
                        String finalName;
                        if (groupsModelMembers.get(x).getOwnerId().equals(PreferenceManager.getInstance().getID(this))) {
                            if (groupsModelMembers.get(x).isLeft()) {
                                groupLeftSendMessageLayout.setVisibility(View.VISIBLE);
                                inputPanel.setVisibility(View.GONE);
                                try {
                                    WhatsCloneApplication.getInstance().getMqttClientManager().unSubscribe( WhatsCloneApplication.getInstance().getClient(), groupID);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                groupLeftSendMessageLayout.setVisibility(View.GONE);
                                inputPanel.setVisibility(View.VISIBLE);

                                try {
                                    WhatsCloneApplication.getInstance().getMqttClientManager().subscribe( WhatsCloneApplication.getInstance().getClient(), groupID);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                            finalName = getString(R.string.you);
                        } else {
                            String phone = UtilsPhone.getContactName(groupsModelMembers.get(x).getOwner_phone());
                            if (phone != null) {
                                try {
                                    finalName = phone.substring(0, 5);
                                } catch (Exception e) {
                                    AppHelper.LogCat(e);
                                    finalName = phone;
                                }
                            } else {
                                finalName = groupsModelMembers.get(x).getOwner_phone().substring(0, 5);
                            }

                        }
                        names.append(finalName);
                        names.append(",");
                    }
                }
            }
            String groupsNames = UtilsString.removelastString(names.toString());
            statusUser.setVisibility(View.VISIBLE);
            statusUser.setText(groupsNames);
            AnimationsUtil.slideStatus(statusUser);

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            AppHelper.LogCat(e);
        }
    }


    @SuppressLint("StaticFieldLeak")
    public void updateContactRecipient(UsersModel usersModel) {
        mUsersModelRecipient = usersModel;

        refreshMenu();
        try {

            if (UtilsPhone.checkIfContactExist(this, usersModel.getPhone())) {
                AddContactBtn.setVisibility(View.GONE);
                blockLayout.setVisibility(View.GONE);
            } else {
                AddContactBtn.setVisibility(View.VISIBLE);
                blockLayout.setVisibility(View.VISIBLE);
                if (MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
                    UnBlockContactBtn.setVisibility(View.VISIBLE);
                    BlockContactBtn.setVisibility(View.GONE);
                } else {
                    UnBlockContactBtn.setVisibility(View.GONE);
                    BlockContactBtn.setVisibility(View.VISIBLE);
                }
            }
            ToolbarTitle.setText(usersModel.getDisplayed_name());
            if (AppHelper.internetAvailable())
                if (usersModel.isConnected()) {
                    updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
                } else {
                    updateUserStatus(AppConstants.STATUS_USER_LAST_SEEN, usersModel.getLast_seen());
                }

        } catch (Exception e) {
            AppHelper.LogCat(" Recipient username  is null MessagesPopupActivity" + e.getMessage());
        }

        String imageUser = usersModel.getImage();
        if (imageUser != null) {

            GlideApp.with(MessagesActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + usersModel.get_id() + "/" + usersModel.getImage()))

                    .signature(new ObjectKey(usersModel.getImage()))
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(AppHelper.getDrawable(this, R.drawable.holder_user))
                    .error(AppHelper.getDrawable(this, R.drawable.holder_user))
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(ToolbarImage);
        } else {
            ToolbarImage.setImageDrawable(AppHelper.getDrawable(this, R.drawable.holder_user));
        }

    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        if (reply_id != null) {
            closeReplyView();
        } else {
            saveMessageBeingComposed();
            mMessagesAdapter.stopPlayer();
            if (NotificationsManager.getInstance().getManager()) {
                if (isGroup)
                    NotificationsManager.getInstance().cancelNotification(groupID);
                else
                    NotificationsManager.getInstance().cancelNotification(recipientId);
            }


            if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            } else {

                if (mSessionDepth == 1) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    super.onBackPressed();
                }
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMessagesPresenter.onDestroy();
        saveMessageBeingComposed();

    }


    private void saveMessageBeingComposed() {
        String message = composeText.getText().toString();

        ConversationModel conversationModel = MessagesController.getInstance().getChatById(ConversationID);
        if (conversationModel != null) {
            conversationModel.set_id(ConversationID);
            conversationModel.setMessageBeingComposed(message);
            MessagesController.getInstance().updateChat(conversationModel);
        }


    }

    @Override
    public void onShowLoading() {
        AppHelper.LogCat("onShowLoading ");


    }

    @Override
    public void onHideLoading() {
        AppHelper.LogCat("onHideLoading ");

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Messages " + throwable.getMessage());
    }


    /**
     * method to update  group members  to show them on toolbar status
     *
     * @param statusUserTyping this is the first parameter for  updateGroupMemberStatus method
     * @param memberName       this is the second parameter for updateGroupMemberStatus method
     */
    private void updateGroupMemberStatus(int statusUserTyping, String memberName) {


        StringBuilder names = new StringBuilder();
        //
        List<MembersModel> membersModels = UsersController.getInstance().loadAllGroupMembers(groupID);

        int arraySize = membersModels.size();
        if (arraySize != 0) {
            for (int x = 0; x < arraySize; x++) {
                if (x <= 1) {
                    String finalName;
                    if (membersModels.get(x).getOwnerId().equals(PreferenceManager.getInstance().getID(this))) {
                        finalName = getString(R.string.you);

                    } else {
                        String phone = UtilsPhone.getContactName(membersModels.get(x).getOwner_phone());
                        if (phone != null) {
                            try {
                                finalName = phone.substring(0, 7);
                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                                finalName = phone;
                            }

                        } else {
                            finalName = membersModels.get(x).getOwner_phone().substring(0, 7);
                        }

                    }
                    names.append(finalName);
                    names.append(",");

                }

            }
        } else {
            names.append("");
        }

        String groupsNames = UtilsString.removelastString(names.toString());

        switch (statusUserTyping) {
            case AppConstants.STATUS_USER_TYPING:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(String.format("%s %s", memberName, getString(R.string.isTyping)));
                break;
            case AppConstants.STATUS_USER_STOP_TYPING:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(groupsNames);
                break;
            default:
                statusUser.setVisibility(View.VISIBLE);
                statusUser.setText(groupsNames);
                break;
        }

    }

    private void showStatus() {
        TransitionManager.beginDelayedTransition(mView);
        statusUser.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        TransitionManager.beginDelayedTransition(mView);
        statusUser.setVisibility(View.GONE);
    }

    /**
     * method to update user status
     *
     * @param statusUserTyping this is the first parameter for  updateUserStatus method
     */
    private void updateUserStatus(int statusUserTyping, String last_seen) {
        if (isGroup) return;
        if (!MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
            switch (statusUserTyping) {
                case AppConstants.STATUS_USER_TYPING:
                    showStatus();
                    statusUser.setText(getString(R.string.isTyping));
                    AppHelper.LogCat("typing...");
                    break;
                case AppConstants.STATUS_USER_DISCONNECTED:
                    showStatus();
                    statusUser.setText(getString(R.string.isOffline));
                    AppHelper.LogCat("Offline...");
                    break;
                case AppConstants.STATUS_USER_CONNECTED:
                    showStatus();
                    statusUser.setText(getString(R.string.isOnline));
                    AnimationsUtil.slideStatus(statusUser);
                    AppHelper.LogCat("Online...");
                    break;
                case AppConstants.STATUS_USER_STOP_TYPING:
                    showStatus();
                    statusUser.setText(getString(R.string.isOnline));
                    break;
                case AppConstants.STATUS_USER_LAST_SEEN:
                    showStatus();
                    statusUser.setText(String.format("%s %s", getString(R.string.last_seen), UtilsTime.convertDateToStringFormatLastSeen(this, UtilsTime.getCorrectDate(last_seen))));
                    AnimationsUtil.slideStatus(statusUser);
                    break;
                default:
                    showStatus();
                    statusUser.setText(getString(R.string.isOffline));
                    break;

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
            case AppConstants.EVENT_BUS_DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY:
                onBackPressed();
                break;
            case AppConstants.EVENT_BUS_REFRESH_MESSAGEGS:
                mMessagesPresenter.onCreate();
                break;
            case AppConstants.EVENT_BUS_EXIT_NEW_GROUP:
                finish();
                break;
            case AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW:


                MessageModel messagesModel = pusher.getMessagesModel();
                if (pusher.getSenderID().equals(recipientId) && pusher.getRecipientID().equals(senderId)) {
                    new Handler().postDelayed(() -> addMessage(pusher.getMessageId()), 500);
                    AppHelper.playSound(this, "audio/incoming_message.m4a");

                    new Handler().postDelayed(() -> {
                        if (AppHelper.isActivityRunning(WhatsCloneApplication.getInstance(), "activities.messages.MessagesActivity")) {
                            AppHelper.LogCat("MessagesActivity running");
                            if (!MessagesController.getInstance().checkIfUserBlockedExist(recipientId))
                                WorkJobsManager.getInstance().sendSeenStatusToServer(recipientId, ConversationID);

                        }
                    }, 1000);
                }

                break;
            case AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW:


                if (isGroup) {
                    MessageModel messagesModel1 = pusher.getMessagesModel();
                    if (!messagesModel1.getSenderId().equals(PreferenceManager.getInstance().getID(this))) {

                        new Handler().postDelayed(() -> addMessage(messagesModel1.get_id()), 500);


                        new Handler().postDelayed(() -> {
                            if (AppHelper.isActivityRunning(WhatsCloneApplication.getInstance(), "activities.messages.MessagesActivity")) {
                                AppHelper.LogCat("MessagesActivity running");
                                WorkJobsManager.getInstance().sendSeenGroupStatusToServer(groupID, ConversationID);
                            }
                        }, 1000);

                    }
                }

                break;


            case AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES:
            case AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES:
            case AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES:

                new Handler().postDelayed(() -> mMessagesAdapter.updateStatusMessageItem(pusher.getMessageId()), 500);


                break;
            case AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES:
                WorkJobsManager.getInstance().sendUserMessagesToServer();
                break;


            case AppConstants.EVENT_BUS_NEW_USER_NOTIFICATION:
                NotificationsModel newUserNotification = pusher.getNotificationsModel();
                if (newUserNotification.getSenderId().equals(recipientId)) {
                    return;
                } else {

                    if (newUserNotification.getAppName() != null && newUserNotification.getAppName().equals(getApplicationContext().getPackageName())) {

                        if (newUserNotification.getFile() != null) {
                            NotificationsManager.getInstance().showUserNotification(getApplicationContext(), newUserNotification.getConversationID(), newUserNotification.getPhone(), newUserNotification.getFile(), newUserNotification.getSenderId(), newUserNotification.getImage());
                        } else {
                            NotificationsManager.getInstance().showUserNotification(getApplicationContext(), newUserNotification.getConversationID(), newUserNotification.getPhone(), newUserNotification.getMessage(), newUserNotification.getSenderId(), newUserNotification.getImage());
                        }
                    }

                }

                break;
            case AppConstants.EVENT_BUS_NEW_GROUP_NOTIFICATION:
                NotificationsModel newGroupNotification = pusher.getNotificationsModel();
                if (newGroupNotification.getGroupID().equals(groupID)) {
                    return;
                } else {
                    if (newGroupNotification.getAppName() != null && newGroupNotification.getAppName().equals(getApplicationContext().getPackageName())) {

                        /**
                         * this for default activity
                         */
                        Intent messagingGroupIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                        messagingGroupIntent.putExtra("conversationID", newGroupNotification.getConversationID());
                        messagingGroupIntent.putExtra("groupID", newGroupNotification.getGroupID());
                        messagingGroupIntent.putExtra("isGroup", newGroupNotification.isGroup());
                        messagingGroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        /**
                         * this for popup activity
                         */
                        Intent messagingGroupPopupIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                        messagingGroupPopupIntent.putExtra("conversationID", newGroupNotification.getConversationID());
                        messagingGroupPopupIntent.putExtra("groupID", newGroupNotification.getGroupID());
                        messagingGroupPopupIntent.putExtra("isGroup", newGroupNotification.isGroup());
                        messagingGroupPopupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String message;
                        String userName = UtilsPhone.getContactName(newGroupNotification.getPhone());
                        switch (newGroupNotification.getState()) {
                            case AppConstants.CREATE_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_created_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_created_this_group);
                                }


                                break;
                            case AppConstants.LEFT_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_left);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_left);
                                }


                                break;
                            case AppConstants.ADD_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_added_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_added_this_group);
                                }


                                break;

                            case AppConstants.REMOVE_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_removed_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_removed_this_group);
                                }


                                break;
                            case AppConstants.ADMIN_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_make_admin_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_make_admin_this_group);
                                }


                                break;
                            case AppConstants.MEMBER_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_make_member_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_make_member_this_group);
                                }


                                break;
                            case AppConstants.EDITED_STATE:
                                if (userName != null) {
                                    message = "" + userName + " " + getString(R.string.he_edited_this_group);
                                } else {
                                    message = "" + newGroupNotification.getPhone() + " " + getString(R.string.he_edited_this_group);
                                }


                                break;
                            default:
                                message = newGroupNotification.getMessage();
                                break;
                        }
                        if (newGroupNotification.getFile() != null) {
                            NotificationsManager.getInstance().showGroupNotification(getApplicationContext(), messagingGroupIntent, messagingGroupPopupIntent, newGroupNotification.getGroupName(), newGroupNotification.getMemberName() + " : " + newGroupNotification.getFile(), newGroupNotification.getGroupID(), newGroupNotification.getImage());
                        } else {
                            NotificationsManager.getInstance().showGroupNotification(getApplicationContext(), messagingGroupIntent, messagingGroupPopupIntent, newGroupNotification.getGroupName(), newGroupNotification.getMemberName() + " : " + message, newGroupNotification.getGroupID(), newGroupNotification.getImage());
                        }
                    }
                }

                break;

            case AppConstants.EVENT_BUS_USER_TYPING:
                if (!MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
                    if (pusher.getSenderID().equals(recipientId) && pusher.getRecipientID().equals(senderId)) {
                        updateUserStatus(AppConstants.STATUS_USER_TYPING, null);
                    }
                }

                break;

            case AppConstants.EVENT_BUS_USER_STOP_TYPING:
                if (!MessagesController.getInstance().checkIfUserBlockedExist(recipientId)) {
                    if (pusher.getSenderID().equals(recipientId) && pusher.getRecipientID().equals(senderId)) {
                        updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, null);
                    }
                }

                break;


            case AppConstants.EVENT_BUS_UPDATE_USER_STATE:
                switch (pusher.getData()) {
                    case AppConstants.EVENT_BUS_USER_IS_ONLINE:
                        if (!MessagesController.getInstance().checkIfUserBlockedExist(pusher.getSenderID()))
                            updateUserStatus(AppConstants.STATUS_USER_CONNECTED, null);
                        break;
                    case AppConstants.EVENT_BUS_USER_IS_OFFLINE:
                        updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, null);
                        break;
                    case AppConstants.EVENT_BUS_USER_LAST_SEEN:
                        updateUserStatus(AppConstants.STATUS_USER_LAST_SEEN, pusher.getLast_seen());
                        break;
                }
                break;

            case AppConstants.EVENT_BUS_MEMBER_TYPING:
                if (pusher.getGroupID() != null) {

                    UsersModel contactsModel = UsersController.getInstance().getUserById(pusher.getSenderID());

                    if (pusher.getGroupID().equals(groupID)) {
                        updateGroupMemberStatus(AppConstants.STATUS_USER_TYPING, contactsModel.getDisplayed_name());
                    }

                }

                break;

            case AppConstants.EVENT_BUS_MEMBER_STOP_TYPING:
                updateGroupMemberStatus(AppConstants.STATUS_USER_STOP_TYPING, null);
                break;


        }
        //  });
    }


    /**
     * method to add a new message to list messages
     *
     * @param messageId this is the parameter for addMessage
     */

    private void addMessage(String messageId) {

        MessageModel messageModel = MessagesController.getInstance().getMessageById(messageId);

        runOnUiThread(() -> {
            mMessagesAdapter.addMessage(messageModel);
            scrollToEnd();
        });


    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * method to toggle the selection
     *
     * @param position this is parameter for  ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mMessagesAdapter.toggleSelection(position);
        String title = String.format(" " + getString(R.string.selected_items), mMessagesAdapter.getSelectedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();

        inflater.inflate(R.menu.select_share_messages_menu, menu);

        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorActionMode));
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        int arraySize = mMessagesAdapter.getSelectedItems().size();
        int currentPosition;
        switch (menuItem.getItemId()) {
            case R.id.share_content:
                if (arraySize != 0 && arraySize == 1) {
                    currentPosition = mMessagesAdapter.getSelectedItems().get(0);
                    MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                    if (messagesModel.getMessage() != null) {
                        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(this))) {
                            if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileVideosSentExists(this, FilesManager.getVideo(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileVideoSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_VIDEOS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileAudiosSentExists(this, FilesManager.getAudio(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileAudioSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_AUDIO);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileImagesSentExists(this, FilesManager.getImage(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileImageSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileGifSentExists(this, FilesManager.getGif(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileGifSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileDocumentsSentExists(this, FilesManager.getDocument(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileDocumentSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_DOCUMENTS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                }
                            } else {
                                AppHelper.shareIntent(null, this, messagesModel.getMessage(), AppConstants.SENT_TEXT);
                            }
                        } else {
                            if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileVideosExists(this, FilesManager.getVideo(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileVideo(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_VIDEOS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileAudioExists(this, FilesManager.getAudio(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileAudio(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_AUDIO);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileImagesExists(this, FilesManager.getImage(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileImage(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileGifExists(this, FilesManager.getGif(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileGif(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileDocumentsExists(this, FilesManager.getDocument(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileDocument(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, messagesModel.getMessage(), AppConstants.SENT_DOCUMENTS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                }
                            } else {
                                AppHelper.shareIntent(null, this, messagesModel.getMessage(), AppConstants.SENT_TEXT);
                            }
                        }

                    } else {
                        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(this))) {
                            if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileVideosSentExists(this, FilesManager.getVideo(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileVideoSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_VIDEOS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileAudiosSentExists(this, FilesManager.getAudio(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileAudioSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_AUDIO);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileImagesSentExists(this, FilesManager.getImage(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileImageSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileGifSentExists(this, FilesManager.getGif(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileGifSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileDocumentsSentExists(this, FilesManager.getDocument(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileDocumentSent(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_DOCUMENTS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                }
                            }
                        } else {
                            if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileVideosExists(this, FilesManager.getVideo(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileVideo(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_VIDEOS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_video_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileAudioExists(this, FilesManager.getAudio(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileAudio(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_AUDIO);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_audio_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileImagesExists(this, FilesManager.getImage(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileImage(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileGifExists(this, FilesManager.getGif(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileGif(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_IMAGES);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_image_is_not_exist));
                                }
                            } else if (messagesModel.getFile() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT) && !messagesModel.getFile().equals("null")) {
                                if (FilesManager.isFileDocumentsExists(this, FilesManager.getDocument(messagesModel.getFile()))) {
                                    File file = FilesManager.getFileDocument(this, messagesModel.getFile());
                                    AppHelper.shareIntent(file, this, null, AppConstants.SENT_DOCUMENTS);
                                } else {
                                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_document_is_not_exist));
                                }
                            }
                        }
                    }

                    break;

                } else {
                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.you_can_share_more_then_one));
                }

                break;
            case R.id.copy_message:
                if (arraySize != 0 && arraySize == 1) {

                    currentPosition = mMessagesAdapter.getSelectedItems().get(0);
                    MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                    if (messagesModel.getMessage() != null) {
                        if (AppHelper.copyText(this, messagesModel)) {
                            AppHelper.CustomToast(MessagesActivity.this, getString(R.string.message_is_copied));
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }
                    } else {
                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_empty));
                    }

                } else {
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.you_can_copy_more_then_one));
                }

                break;
            case R.id.reply_message:
                if (arraySize != 0 && arraySize == 1) {
                    for (int x = 0; x < arraySize; x++) {
                        currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                        MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);

                        //  if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                        if (messagesModel.getStatus() != AppConstants.IS_WAITING) {

                            clear_btn_reply_view.setVisibility(View.VISIBLE);
                            AnimationsUtil.animateWindowInTranslate(replied_message_view);
                            setRepliedMessage(messagesModel.get_id());
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        } else {
                            AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_unsent));
                        }
                  /*      } else {
                            AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_empty));
                        }*/
                    }

                } else {
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    AppHelper.CustomToast(MessagesActivity.this, getString(R.string.you_can_copy_more_then_one));
                }

                break;
            case R.id.delete_message:
                if (arraySize != 0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.message_delete);

                    builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {

                        AppHelper.showDialog(this, getString(R.string.deleting_chat));
                        for (int x = 0; x < arraySize; x++) {
                            int currentPosition1 = mMessagesAdapter.getSelectedItems().get(x);
                            MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition1);
                            mMessagesPresenter.deleteMessage(messagesModel, currentPosition1);

                        }
                        AppHelper.hideDialog();

                        if (actionMode != null) {
                            actionMode.finish();
                        }

                    });

                    builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                    });

                    builder.show();

                }
                break;
            case R.id.transfer_message:
                if (arraySize != 0) {
                    ArrayList<String> messagesModelList = new ArrayList<>();
                    for (int x = 0; x < arraySize; x++) {
                        currentPosition = mMessagesAdapter.getSelectedItems().get(x);
                        MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                        String message = UtilsString.unescapeJava(messagesModel.getMessage());
                        messagesModelList.add(message);
                    }
                    if (messagesModelList.size() != 0) {
                        Intent intent = new Intent(this, TransferMessageContactsActivity.class);
                        intent.putExtra("messageCopied", messagesModelList);
                        startActivity(intent);
                        finish();
                    } else {
                        AppHelper.CustomToast(MessagesActivity.this, getString(R.string.this_message_empty));
                    }
                }
                break;
            default:
                return false;
        }


        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        mMessagesAdapter.clearSelections();
        if (AppHelper.isAndroid5()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
        }
    }
/*

    @Override
    public void onClick(View view) {
        int position = messagesList.getChildAdapterPosition(view);
        if (actionMode != null) {
            ToggleSelection(position);
        }
    }
*/


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            View view = messagesList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = messagesList.getChildAdapterPosition(view);
            try {
                MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                if (messagesModel.getState().equals(AppConstants.NORMAL_STATE)) {
                    if (actionMode != null) {
                        ToggleSelection(currentPosition);
                        Menu menu = actionMode.getMenu();
                        if (mMessagesAdapter.getSelectedItemCount() > 1) {
                            menu.findItem(R.id.reply_message).setVisible(false);
                            menu.findItem(R.id.copy_message).setVisible(false);
                            menu.findItem(R.id.share_content).setVisible(false);
                        } else {

                            menu.findItem(R.id.reply_message).setVisible(true);
                            menu.findItem(R.id.copy_message).setVisible(true);
                            menu.findItem(R.id.share_content).setVisible(true);
                        }
                        boolean hasCheckedItems = mMessagesAdapter.getSelectedItems().size() > 0;//Check if any items are already selected or not
                        if (!hasCheckedItems && actionMode != null) {
                            // there no selected items, finish the actionMode
                            actionMode.finish();
                        }
                    }
                }

            } catch (Exception ex) {
                AppHelper.LogCat(" onSingleTapConfirmed " + ex.getMessage());
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            AppHelper.LogCat(" onLongPress ");
            try {
                View view = messagesList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = messagesList.getChildAdapterPosition(view);
                MessageModel messagesModel = mMessagesAdapter.getItem(currentPosition);
                if (messagesModel.getState().equals(AppConstants.NORMAL_STATE)) {
                    if (actionMode != null) {
                        return;
                    }
                    actionMode = startActionMode(MessagesActivity.this);
                    ToggleSelection(currentPosition);
                }
                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }
        }

    }


    public void setRepliedMessage(String messageId) {
        reply_id = messageId;
        int maxLength = 50;


        MessageModel messageModel = MessagesController.getInstance().getMessageById(messageId);

        if (messageModel == null) return;


        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(messageModel.getSender_phone());
        color_view.setBackgroundColor(color);
        if (messageModel.getSenderId().equals(PreferenceManager.getInstance().getID(this))) {
            owner_name.setText(this.getString(R.string.you));
            owner_name.setTextColor(color);
        } else {
            String name = UtilsPhone.getContactName(messageModel.getSender_phone());
            if (name != null) {
                owner_name.setText(name);
            } else {
                owner_name.setText(messageModel.getSender_phone());
            }
            owner_name.setTextColor(color);
        }

        //  if (isMessage) {
        message_type.setVisibility(View.GONE);
        /*} else {
            message_type.setVisibility(View.VISIBLE);
            message_type.setText(this.getString(R.string.status));
        }*/

        if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
            short_message.setVisibility(View.VISIBLE);
            short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(this));


            switch (messageModel.getFile_type()) {
                case AppConstants.MESSAGES_IMAGE:
                    if (messageModel.getLatitude() != null && !messageModel.getLatitude().equals("null")) {
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_location_gray_24dp), null, null, null);

                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_location);
                    } else {
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                        short_message.setText(R.string.conversation_row_image);
                    }
                    break;
                case AppConstants.MESSAGES_GIF:
                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_gif_gray_24dp), null, null, null);
                    if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                        String message = UtilsString.unescapeJava(messageModel.getMessage());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else
                        short_message.setText(R.string.conversation_row_gif);
                    break;
                case AppConstants.MESSAGES_VIDEO:
                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_videocam_gray_24dp), null, null, null);
                    if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                        String message = UtilsString.unescapeJava(messageModel.getMessage());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else
                        short_message.setText(R.string.conversation_row_video);
                    break;
                case AppConstants.MESSAGES_AUDIO:
                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_headset_gray_24dp), null, null, null);
                    if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                        String message = UtilsString.unescapeJava(messageModel.getMessage());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else
                        short_message.setText(R.string.conversation_row_audio);
                    break;
                case AppConstants.MESSAGES_DOCUMENT:
                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_document_file_gray_24dp), null, null, null);
                    if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                        String message = UtilsString.unescapeJava(messageModel.getMessage());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else
                        short_message.setText(R.string.conversation_row_document);
                    break;

            }
            RequestBuilder<Drawable> thumbnailRequest;
            switch (messageModel.getFile_type()) {
                case AppConstants.MESSAGES_IMAGE:
                    message_file_thumbnail.setVisibility(View.VISIBLE);

                    thumbnailRequest = GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()));

                    GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()))
                            .signature(new ObjectKey(messageModel.getFile()))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .placeholder(new ColorDrawable(AppHelper.getColor(this, R.color.colorHolder)))
                            .into(message_file_thumbnail);
                    break;
                case AppConstants.MESSAGES_VIDEO:

                    message_file_thumbnail.setVisibility(View.VISIBLE);

                    long interval = 5000 * 1000;
                    RequestOptions options = new RequestOptions().frame(interval);
                    GlideApp.with(this)
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + messageModel.getFile()))
                            .signature(new ObjectKey(messageModel.getFile()))
                            .dontAnimate()
                            .centerCrop()
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .apply(options)
                            .placeholder(new ColorDrawable(AppHelper.getColor(this, R.color.colorHolder)))
                            .into(message_file_thumbnail);
                    break;
                case AppConstants.MESSAGES_GIF:

                    message_file_thumbnail.setVisibility(View.VISIBLE);

                    thumbnailRequest = GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()));

                    GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()))
                            .signature(new ObjectKey(messageModel.getFile()))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .placeholder(new ColorDrawable(AppHelper.getColor(this, R.color.colorHolder)))
                            .into(message_file_thumbnail);
                    break;
                default:
                    message_file_thumbnail.setVisibility(View.GONE);
                    break;
            }
        } else {
            short_message.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            message_file_thumbnail.setVisibility(View.GONE);
            if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                String message = UtilsString.unescapeJava(messageModel.getMessage());
                if (message.length() > maxLength) {

                    short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                } else {
                    short_message.setText(message);
                }
            } else {
                short_message.setVisibility(View.GONE);
            }
        }


    }

    @Override
    public void onRecorderStarted() {

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        audioRecorder.startRecording();
    }

    @Override
    public void onRecorderFinished() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(20);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ListenableFuture<Pair<String, Long>> future = audioRecorder.stopRecording();
        future.addListener(new ListenableFuture.Listener<Pair<String, Long>>() {
            @Override
            public void onSuccess(final @NonNull Pair<String, Long> result) {
                FileAudioPath = result.first;
                Duration = FilesManager.getDuration(MessagesActivity.this, FileAudioPath);
                File file = null;
                if (FileAudioPath != null) {
                    file = new File(FileAudioPath);
                }
                if (file != null) {
                    FileSize = String.valueOf(file.length());

                }

                sendMessage();
            }

            @Override
            public void onFailure(ExecutionException e) {
                AppHelper.LogCat("ExecutionException " + e.getMessage());
                Toast.makeText(MessagesActivity.this, R.string.unable_to_record_audio, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRecorderCanceled() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ListenableFuture<Pair<String, Long>> future = audioRecorder.stopRecording();
        future.addListener(new ListenableFuture.Listener<Pair<String, Long>>() {
            @Override
            public void onSuccess(final Pair<String, Long> result) {
                if (FilesManager.isFileRecordExists(result.first)) {
                    FilesManager.getFileRecord(result.first).delete();
                }
            }

            @Override
            public void onFailure(ExecutionException e) {
            }
        });
    }

    @Override
    public void onRecorderPermissionRequired() {
        Permissions.with(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.record_audio_permission_message), R.drawable.ic_mic_white_24dp)
                .withPermanentDenialDialog(getString(R.string.record_audio_permission_message))
                .execute();

    }

    @Override
    public void onEmojiToggle() {
        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }

// Listeners

    private class AttachmentTypeListener implements AttachmentLayout.AttachmentClickedListener {
        @Override
        public void onClick(int type) {
            switch (type) {

                case AttachmentLayout.ADD_GALLERY:
                    FilesManager.selectGallery(MessagesActivity.this, AppConstants.PICK_GALLERY_MESSAGES);
                    break;
                case AttachmentLayout.ADD_DOCUMENT:
                    FilesManager.selectDocument(MessagesActivity.this, AppConstants.PICK_DOCUMENT_MESSAGES);
                    break;
                case AttachmentLayout.ADD_AUDIO:
                    FilesManager.selectAudio(MessagesActivity.this, AppConstants.PICK_AUDIO_MESSAGES);
                    break;
                case AttachmentLayout.ADD_CONTACT_INFO:
                    FilesManager.selectContactInfo(MessagesActivity.this, AppConstants.PICK_CONTACT_INFO_MESSAGES);
                    break;
                case AttachmentLayout.ADD_LOCATION:
                    FilesManager.selectLocation(MessagesActivity.this, AppConstants.PICK_LOCATION_MESSAGES);
                    break;
                case AttachmentLayout.TAKE_PHOTO:
                    FilesManager.capturePhoto(MessagesActivity.this, AppConstants.PICK_CAMERA_MESSAGES, false);
                    break;
                case AttachmentLayout.ADD_GIF:
                    FilesManager.selectGif(MessagesActivity.this, AppConstants.PICK_GIF_MESSAGES);
                    break;
            }
        }

        @Override
        public void onQuickAttachment(Uri uri) {
            Intent intent = new Intent();
            intent.setData(uri);
            AppHelper.LogCat("hana " + intent.getData());
            MessagesActivity.this.onActivityResult(AppConstants.PICK_GALLERY_MESSAGES, RESULT_OK, intent);
        }
    }

    private class ComposeKeyPressedListener implements View.OnKeyListener, View.OnClickListener, TextWatcher, View.OnFocusChangeListener {

        int beforeLength;

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (PreferenceSettingsManager.enter_send(MessagesActivity.this)) {
                        sendButtonn.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                        sendButtonn.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            emitMessageSeen();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeLength = composeText.getText().length();
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (composeText.getText().length() == 0 || beforeLength == 0) {
                composeText.postDelayed(MessagesActivity.this::updateToggleButtonState, 50);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (composeText.getLineCount() >= 6) {
                composeText.setScroller(new Scroller(MessagesActivity.this));
                composeText.setMaxLines(6);
                composeText.setVerticalScrollBarEnabled(true);
                composeText.setMovementMethod(new ScrollingMovementMethod());
            }


            if (PreferenceSettingsManager.enter_send(MessagesActivity.this)) {
                composeText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                composeText.setSingleLine(true);
                composeText.setOnEditorActionListener((v, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                        sendMessage();
                    }
                    return false;
                });
            }
            if (!isMessageBeingComposed())
                if (s.length() > 0) { // compose message
                    mPauseComposeTimer.cancel();
                    if (!isTyping) {
                        isTyping = true;
                        if (isGroup) {


                            JSONObject data = new JSONObject();
                            try {
                                data.put("senderId", senderId);
                                data.put("groupId", groupID);
                                data.put("is_group", true);
                                try {
                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageIsTyping(groupID, data);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                AppHelper.LogCat(e);
                            }


                        } else {

                            JSONObject data = new JSONObject();
                            try {
                                data.put("recipientId", recipientId);
                                data.put("senderId", senderId);
                                data.put("is_group", false);

                                try {
                                    WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageIsTyping(recipientId, data);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                AppHelper.LogCat(e);
                            }

                        }
                    }
                    schedulePauseTimer();
                } else { // delete or send message
                    mPauseComposeTimer.cancel();
                    sendInactiveTypingStatus();
                }

        }


        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                AppHelper.LogCat("Has focused");
                //emitMessageSeen();

            }
        }

    }

    private void schedulePauseTimer() {
        mPauseComposeTimer = new Timer();
        mPauseComposeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> sendInactiveTypingStatus());
            }
        }, TYPING_TIMER_LENGTH);
    }

    private void sendInactiveTypingStatus() {
        if (!isTyping) return;

        isTyping = false;
        if (isGroup) {

            JSONObject json = new JSONObject();
            try {
                json.put("senderId", senderId);
                json.put("groupId", groupID);
                json.put("is_group", true);
                try {
                    WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageStopTyping(groupID, json);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                AppHelper.LogCat(e);
            }

            isTyping = false;


        } else {
            JSONObject json = new JSONObject();
            try {
                json.put("recipientId", recipientId);
                json.put("senderId", senderId);
                json.put("is_group", false);
                try {
                    WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageStopTyping(recipientId, json);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                AppHelper.LogCat(e);

            }


            isTyping = false;

        }

    }


    private void updateToggleButtonState() {
        if (composeText.getText().length() == 0) {
            buttonToggle.display(attachButton);
            quickAttachmentToggle.show();
        } else {
            buttonToggle.display(sendButtonn);
            quickAttachmentToggle.hide();
        }
    }


}
