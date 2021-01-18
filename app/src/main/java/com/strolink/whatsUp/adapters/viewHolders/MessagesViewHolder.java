package com.strolink.whatsUp.adapters.viewHolders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.settings.PreferenceSettingsManager;
import com.strolink.whatsUp.activities.stories.StoriesDetailsActivity;
import com.strolink.whatsUp.adapters.recyclerView.messages.MessagesAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;

import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.ColorGenerator;
import com.vanniktech.emoji.EmojiTextView;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 10/5/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private MessagesAdapter messagesAdapter;
    private Activity mActivity;

    @BindView(R.id.message_text)
    public EmojiTextView message;

    @BindView(R.id.status_date_container)
    public LinearLayout status_date_container;

    @BindView(R.id.date_message)
    public AppCompatTextView date;

    @BindView(R.id.sender_name)
    public AppCompatTextView senderName;

    @BindView(R.id.status_messages)
    public AppCompatImageView statusMessages;

    @BindView(R.id.date_general_message)
    public AppCompatTextView date_general_message;


    @BindView(R.id.message_layout)
    public LinearLayout message_layout;

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


    public MessagesViewHolder(MessagesAdapter messagesAdapter, View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;
        ButterKnife.bind(this, itemView);
        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        changePaddings();
        replied_message_view.setOnClickListener(this);
        itemView.setOnClickListener(view -> {

        });


    }


    private void changePaddings() {
      //  status_date_container.setPadding(3, 3, 3, 3);
       // message.setPadding(3, 3, 3, 3);
    }


    public void setHeaderDate(long now_tm, long msg_tm, DateTime time) {
        Date nowDate = new Date();
        nowDate.setTime(now_tm);
        Date msgDate = new Date();
        msgDate.setTime(msg_tm);
        Calendar now_calendar = Calendar.getInstance();
        now_calendar.setTimeInMillis(now_tm);
        Calendar msg_calendar = Calendar.getInstance();
        msg_calendar.setTimeInMillis(msg_tm);
        if (now_tm == msg_tm) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else if (msg_tm == 0) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else {
            if (msgDate.before(nowDate)) {

                boolean sameDay = now_calendar.get(Calendar.YEAR) == msg_calendar.get(Calendar.YEAR) &&
                        now_calendar.get(Calendar.MONTH) == msg_calendar.get(Calendar.MONTH)
                        && now_calendar.get(Calendar.DAY_OF_MONTH) == msg_calendar.get(Calendar.DAY_OF_MONTH);
                if (sameDay) {
                    date_general_message.setVisibility(View.GONE);
                    date_general_message.setText("");
                } else {
                    date_general_message.setVisibility(View.VISIBLE);
                    date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
                }
            } else {
                date_general_message.setVisibility(View.GONE);
                date_general_message.setText("");
            }
        }


    }

    @SuppressLint("CheckResult")
    public void setDate(String Date) {
        date.setText(UtilsTime.convertMessageDateToStringFormat(mActivity, UtilsTime.getCorrectDate(Date)));
    }


    public void setSenderName(String SendName) {
        senderName.setText(SendName);
    }

    public void setSenderColor(int Sendcolor) {
        senderName.setTextColor(Sendcolor);
    }

    public void hideSenderName() {
        senderName.setVisibility(View.GONE);
    }

    public void showSenderName() {
        senderName.setVisibility(View.VISIBLE);
    }

    public void hideSent() {
        statusMessages.setVisibility(View.GONE);
    }

    public void showSent(int status) {
        statusMessages.setVisibility(View.VISIBLE);
        switch (status) {
            case AppConstants.IS_WAITING:
                statusMessages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                break;
            case AppConstants.IS_SENT:
                if (messagesAdapter.isStatusUpdated) {
                    AppHelper.playSound(mActivity, "audio/message_is_sent.m4a");
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_gray_24dp);

                break;
            case AppConstants.IS_DELIVERED:
                if (messagesAdapter.isStatusUpdated) {
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                break;
            case AppConstants.IS_SEEN:
                if (messagesAdapter.isStatusUpdated) {
                    AnimationsUtil.rotationY(statusMessages);
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                break;

        }

    }


    public void setrepliedMessage(String messageId, boolean isMessage) {
        int maxLength = 50;
        if (isMessage) {

                MessageModel messageModel = MessagesController.getInstance().getMessageById(messageId);

                if (messageModel == null) return;

                    ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                    // generate random color
                    int color = generator.getColor(messageModel.getSender_phone());
                    color_view.setBackgroundColor(color);
                    if (messageModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        owner_name.setText(mActivity.getString(R.string.you));
                        owner_name.setTextColor(color);
                    } else {
                        String name = UtilsPhone.getContactName(messageModel.getSender_phone());

                        if (name != null) {

                            if (name.length() > 8) {

                                owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                            } else {
                                owner_name.setText(name);
                            }
                        } else {

                            owner_name.setText(messageModel.getSender_phone());
                        }
                        owner_name.setTextColor(color);
                    }

                    message_type.setVisibility(View.GONE);

                    if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
                        short_message.setVisibility(View.VISIBLE);
                        short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                        switch (messageModel.getFile_type()) {
                            case AppConstants.MESSAGES_IMAGE:
                                if (messageModel.getLatitude() != null && !messageModel.getLatitude().equals("null")) {
                                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_location_gray_24dp), null, null, null);

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
                                    short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                                    short_message.setText(R.string.conversation_row_image);
                                }
                                break;
                            case AppConstants.MESSAGES_GIF:
                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_gif_gray_24dp), null, null, null);
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
                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
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
                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_headset_gray_24dp), null, null, null);
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
                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_document_file_gray_24dp), null, null, null);
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

                                thumbnailRequest = messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()));

                                messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()))
                                        .signature(new ObjectKey(messageModel.getFile()))
                                        .dontAnimate()
                                        .thumbnail(thumbnailRequest)
                                        .centerCrop()
                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                        .into(message_file_thumbnail);
                                break;
                            case AppConstants.MESSAGES_VIDEO:

                                message_file_thumbnail.setVisibility(View.VISIBLE);

                                long interval = 5000 * 1000;
                                RequestOptions options = new RequestOptions().frame(interval);
                                messagesAdapter.glideRequests
                                        .asBitmap()
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + messageModel.getFile()))
                                        .signature(new ObjectKey(messageModel.getFile()))
                                        .dontAnimate()
                                        .centerCrop()
                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                        .apply(options)
                                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                        .into(message_file_thumbnail);
                                break;
                            case AppConstants.MESSAGES_GIF:

                                message_file_thumbnail.setVisibility(View.VISIBLE);

                                thumbnailRequest = messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()));

                                messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()))
                                        .signature(new ObjectKey(messageModel.getFile()))
                                        .dontAnimate()
                                        .thumbnail(thumbnailRequest)
                                        .centerCrop()
                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                        .into(message_file_thumbnail);
                                break;
                            default:
                                message_file_thumbnail.setVisibility(View.GONE);
                                break;
                        }
                    } else {

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

        } else {




                StoryModel storyModel = StoriesController.getInstance().getStoryById(messageId);

                if (storyModel == null) return;

                    UsersModel usersModel = UsersController.getInstance().getUserById(storyModel.getUserId());
                    if (usersModel == null) return;
                    ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                    // generate random color
                    int color = generator.getColor(usersModel.getPhone());
                    color_view.setBackgroundColor(color);
                    if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        owner_name.setText(mActivity.getString(R.string.you));
                        owner_name.setTextColor(color);
                    } else {
                        String name = UtilsPhone.getContactName(usersModel.getPhone());
                        if (name != null) {
                            if (name.length() > 8) {

                                owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                            } else {
                                owner_name.setText(name);
                            }
                        } else {
                            owner_name.setText(usersModel.getPhone());
                        }
                        owner_name.setTextColor(color);
                    }

                    message_type.setVisibility(View.VISIBLE);
                    message_type.setText(mActivity.getString(R.string.status));

                    if (storyModel.getFile() != null && !storyModel.getFile().equals("null")) {
                        short_message.setVisibility(View.VISIBLE);
                        short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                        switch (storyModel.getType()) {
                            case "image":

                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
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
                                short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
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

                                thumbnailRequest = messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()));

                                messagesAdapter.glideRequests
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()))
                                        .signature(new ObjectKey(storyModel.getFile()))
                                        .dontAnimate()
                                        .thumbnail(thumbnailRequest)
                                        .centerCrop()
                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                        .into(message_file_thumbnail);
                                break;
                            case "video":

                                message_file_thumbnail.setVisibility(View.VISIBLE);

                                long interval = 5000 * 1000;
                                RequestOptions options = new RequestOptions().frame(interval);
                                messagesAdapter.glideRequests
                                        .asBitmap()
                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + storyModel.getFile()))
                                        .signature(new ObjectKey(storyModel.getFile()))
                                        .dontAnimate()
                                        .centerCrop()
                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                        .apply(options)
                                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
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


    }

    @Override
    public void onClick(View view) {
        MessageModel messageModel = messagesAdapter.getItem(getAdapterPosition());
        if (messagesAdapter.isActivated) return;
        switch (view.getId()) {
            case R.id.replied_message_view:

                if (messageModel.isReply_message()) {

                        if (!MessagesController.getInstance().checkIfMessageExist(messageModel.getReply_id()))
                            return;
                            messagesAdapter.scrollToItem(messageModel.getReply_id());


                } else {

                    if (!StoriesController.getInstance().checkIfSingleStoryExist(messageModel.getReply_id()))
                        return;
                    StoryModel storyModel = StoriesController.getInstance().getStoryById(messageModel.getReply_id());
                    int currentStoryPosition;

                    if (storyModel.getUserId().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesHeaderById(storyModel.getUserId());
                        if (storiesModels.size() == 0) return;
                        AppHelper.LogCat("storiesModels.size() " + storiesModels.size());
                        currentStoryPosition = storiesModels.indexOf(storyModel);
                    } else {

                        List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesById(storyModel.getUserId());
                        if (storiesModels.size() == 0) return;
                        currentStoryPosition = storiesModels.indexOf(storyModel);
                    }


                    AppHelper.LogCat("currentStoryPosition " + currentStoryPosition);
                    Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                    a.putExtra("position", 0);
                    a.putExtra("currentStoryPosition", currentStoryPosition);
                    a.putExtra("storyId", storyModel.getUserId());
                    itemView.getContext().startActivity(a);
                }
                break;
        }
    }


    public void setBlinkEffect() {
        AnimationsUtil.manageBlinkEffect(message_layout);
    }

}
