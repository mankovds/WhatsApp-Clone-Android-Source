package com.strolink.whatsUp.activities.stories;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.settings.PreferenceSettingsManager;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.ColorGenerator;
import com.strolink.whatsUp.ui.dragView.DragListener;
import com.strolink.whatsUp.ui.dragView.DragToClose;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by Abderrahim El imame on 7/27/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesReplyActivity extends BaseActivity implements InputGeneralPanel.Listener {

    @BindView(R.id.reply_story)
    View reply_story;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;


    @BindView(R.id.send_buttonn)
    AppCompatImageButton send_buttonn;

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


    private EmojiPopup emojiPopup;

    private String storyId;
    private String recipientId;
    private String conversationID;


    @BindView(R.id.drag_to_close)
    DragToClose dragToClose;

    @Override
    public void onCreate(Bundle bundle) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_story_reply);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            storyId = getIntent().getStringExtra("storyId");
            recipientId = getIntent().getStringExtra("recipientId");
        }

        clear_btn_reply_view.setVisibility(View.VISIBLE);
        AnimationsUtil.animateWindowInTranslate(replied_message_view);
        setrepliedMessage(storyId);
        composeText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String messageBody = UtilsString.escapeJava(composeText.getText().toString().trim());
                if (messageBody.isEmpty())
                    AppHelper.CustomToast(this, getString(R.string.write_something));
                else
                    replyStory(messageBody);
            }
            return false;
        });


        inputPanel.setListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(reply_story).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);

        send_buttonn.setOnClickListener(v -> {

            String messageBody = UtilsString.escapeJava(composeText.getText().toString().trim());
            if (messageBody.isEmpty())
                AppHelper.CustomToast(this, getString(R.string.write_something));
            else
                replyStory(messageBody);

        });
        reply_story.setOnClickListener(v -> {
            hideKeyboard();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("isReply", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        });

        composeText.requestFocus();
        showKeyboard();

        dragToClose.setCloseOnClick(true);
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {
                AppHelper.LogCat("onStartDraggingView()");

            }

            @Override
            public void onDraggingView(float offset) {
                replied_message_view.setAlpha(offset);
                inputPanel.setAlpha(offset);
                send_buttonn.setAlpha(offset);
            }

            @Override
            public void onViewClosed() {
                AppHelper.LogCat("onViewClosed()");

                Intent resultIntent = new Intent();
                resultIntent.putExtra("isReply", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

    }


    public void setrepliedMessage(String storyId) {

        int maxLength = 50;

        StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);

        if (storyModel == null) return;
        UsersModel usersModel = UsersController.getInstance().getUserById(storyModel.getUserId());
        if (usersModel == null) return;
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(usersModel.getPhone());
        color_view.setBackgroundColor(color);
        if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(this))) {
            owner_name.setText(this.getString(R.string.you));
            owner_name.setTextColor(color);
        } else {
            String name = UtilsPhone.getContactName(usersModel.getPhone());
            if (name != null) {
                owner_name.setText(name);
            } else {
                owner_name.setText(usersModel.getPhone());
            }
            owner_name.setTextColor(color);
        }

        message_type.setVisibility(View.VISIBLE);
        message_type.setText(this.getString(R.string.status));

        if (storyModel.getFile() != null && !storyModel.getFile().equals("null")) {
            short_message.setVisibility(View.VISIBLE);
            short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(this));


            switch (storyModel.getType()) {
                case "image":

                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                    if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                        String message = UtilsString.unescapeJava(storyModel.getBody());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else short_message.setText(R.string.conversation_row_image);

                    break;
                case "video":
                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(this, R.drawable.ic_videocam_gray_24dp), null, null, null);
                    if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                        String message = UtilsString.unescapeJava(storyModel.getBody());
                        if (message.length() > maxLength) {

                            short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                        } else {
                            short_message.setText(message);
                        }

                    } else
                        short_message.setText(R.string.conversation_row_video);
                    break;


            }
            RequestBuilder<Drawable> thumbnailRequest;
            switch (storyModel.getType()) {
                case "image":
                    message_file_thumbnail.setVisibility(View.VISIBLE);

                    thumbnailRequest = GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()));

                    GlideApp.with(this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()))
                            .signature(new ObjectKey(storyModel.getFile()))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .placeholder(new ColorDrawable(AppHelper.getColor(this, R.color.colorHolder)))
                            .into(message_file_thumbnail);
                    break;
                case "video":

                    message_file_thumbnail.setVisibility(View.VISIBLE);

                    long interval = 5000 * 1000;
                    RequestOptions options = new RequestOptions().frame(interval);
                    GlideApp.with(this)
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + storyModel.getFile()))
                            .signature(new ObjectKey(storyModel.getFile()))
                            .dontAnimate()
                            .centerCrop()
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .apply(options)
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
            if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                String message = UtilsString.unescapeJava(storyModel.getBody());
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

    /*// slide the view from below itself to the current position
    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void onSlideViewButtonClick() {
        if (isUp) {
            slideDown(inputPanel);
        } else {
            slideUp(inputPanel);
        }
        isUp = !isUp;
    }
*/

/*


    //keyboard reply
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (emojiDrawerStub.resolved() && reply_story.getCurrentInput() == emojiDrawerStub.get()) {
            reply_story.hideAttachedInput(true);
        }
    }
*/


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
            //   onSlideViewButtonClick();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isReply", true);
            setResult(Activity.RESULT_OK, resultIntent);
            super.onBackPressed();
        }
    }

    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(composeText.getWindowToken(), 0);
    }

    public void showKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(composeText, InputMethodManager.SHOW_IMPLICIT);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void replyStory(String messageBody) {

        AppHelper.LogCat("storyId " + storyId);


        try {

            String senderId = PreferenceManager.getInstance().getID(this);
            String created = AppHelper.getCurrentTime();
            String file = "null";
            String file_type = "null";
            String fileSize = "0";
            String duration = "0";
            String latitude = "null";
            String longitude = "null";
            String document_type = "null";
            String document_name = "null";


            if (!MessagesController.getInstance().checkIfConversationExist(recipientId)) {

                String lastID = DbBackupRestore.getMessageLastId();


                UsersModel usersModelSender = UsersController.getInstance().getUserById(senderId);
                UsersModel usersModelRecipient = UsersController.getInstance().getUserById(recipientId);

                String lastConversationID = DbBackupRestore.getConversationLastId();
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
                messagesModel.setReply_id(storyId);
                messagesModel.setReply_message(false);
                messagesModel.setDocument_name(document_name);
                messagesModel.setDocument_type(document_type);

                messagesModel.setFile_upload(true);
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

                conversationID = lastConversationID;

                AppHelper.CustomToast(this, getString(R.string.sending_reply));
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, conversationID));
                WorkJobsManager.getInstance().sendUserMessagesToServer();

            } else {
                conversationID = MessagesController.getInstance().getChatIdByUserId(recipientId);

                String lastID = DbBackupRestore.getMessageLastId();
                try {


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
                    messagesModel.setReply_id(storyId);
                    messagesModel.setReply_message(false);
                    messagesModel.setDocument_name(document_name);
                    messagesModel.setDocument_type(document_type);
                    messagesModel.setFile_upload(true);
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

                    AppHelper.CustomToast(this, getString(R.string.sending_reply));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationID));
                    WorkJobsManager.getInstance().sendUserMessagesToServer();
                } catch (Exception e) {
                    AppHelper.LogCat("Exception  last id message  StoriesReplyActivity " + e.getMessage());
                }

            }

        } finally {
            closeReplyView();
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.clear_btn_reply_view)
    public void closeReplyView() {
        hideKeyboard();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isReply", true);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
