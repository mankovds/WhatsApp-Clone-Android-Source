package com.strolink.whatsUp.adapters.viewHolders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.files.PendingFilesTask;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.ColorGenerator;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import rm.com.audiowave.AudioWaveView;
import rm.com.audiowave.OnProgressListener;

/**
 * Created by Abderrahim El imame on 10/5/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MessagesAudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnProgressListener {


    @BindView(R.id.message_text)
    public EmojiTextView message;

    @BindView(R.id.date_message)
    public AppCompatTextView date;

    @BindView(R.id.sender_name)
    public AppCompatTextView senderName;

    @BindView(R.id.status_messages)
    public AppCompatImageView statusMessages;

    @BindView(R.id.date_general_message)
    public AppCompatTextView date_general_message;

    //var for audio
    @BindView(R.id.audio_user_image)
    public AppCompatImageView userAudioImage;

    @BindView(R.id.progress_bar_upload_audio)
    public ProgressBar mProgressUploadAudio;

    @BindView(R.id.progress_bar_upload_audio_init)
    public ProgressBar mProgressUploadAudioInitial;

    @BindView(R.id.cancel_upload_audio)
    public AppCompatImageButton cancelUploadAudio;


    @BindView(R.id.retry_upload_audio_button)
    public AppCompatImageButton retryUploadAudioButton;

    @BindView(R.id.progress_bar_download_audio)
    public ProgressBar mProgressDownloadAudio;

    @BindView(R.id.progress_bar_download_audio_init)
    public ProgressBar mProgressDownloadAudioInitial;

    @BindView(R.id.cancel_download_audio)
    public AppCompatImageButton cancelDownloadAudio;


    @BindView(R.id.retry_download_audio_button)
    public AppCompatImageButton retryDownloadAudioButton;

    @BindView(R.id.play_btn_audio)
    public AppCompatImageButton playBtnAudio;

    @BindView(R.id.pause_btn_audio)
    public AppCompatImageButton pauseBtnAudio;

    @BindView(R.id.audio_progress_bar)
    public AudioWaveView audioSeekBar;

    @BindView(R.id.audio_current_duration)
    public AppCompatTextView audioCurrentDurationAudio;

    @BindView(R.id.audio_total_duration)
    public AppCompatTextView audioTotalDurationAudio;

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

    @BindView(R.id.message_layout)
    public LinearLayout message_layout;


    private MessagesAdapter messagesAdapter;
    private Activity mActivity;
    private boolean isUploadServiceStopped = false;
    private boolean isDownloadServiceStopped = false;

    public MessagesAudioViewHolder(MessagesAdapter messagesAdapter, @NonNull View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;

        ButterKnife.bind(this, itemView);

        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        setupProgressBarUploadAudio();

        replied_message_view.setOnClickListener(this);
        cancelDownloadAudio.setOnClickListener(this);
        retryDownloadAudioButton.setOnClickListener(this);
        cancelUploadAudio.setOnClickListener(this);
        retryUploadAudioButton.setOnClickListener(this);
        audioSeekBar.setOnProgressListener(this);
        playBtnAudio.setOnClickListener(this);
        pauseBtnAudio.setOnClickListener(this);

        itemView.setOnClickListener(view -> {

        });


    }


    public void setAudioTotalDurationAudio(MessageModel messagesModel) {

        try {

            String time = messagesModel.getDuration_file();
            long timeInMilliSecond = Long.parseLong(time);
            setTotalTime(timeInMilliSecond);
        } catch (Exception e) {
            AppHelper.LogCat("Exception total duration " + e.getMessage());
        }


    }

    public void setTotalTime(long totalTime) {

        audioTotalDurationAudio.setText(UtilsTime.getFileTime(totalTime));
    }


    public void setUserAudioImage(String userId, String userImage) {

        if (AppHelper.hasImage(userAudioImage)) return;
        try {
            Drawable drawable = AppHelper.getDrawable(mActivity, R.drawable.holder_user);


            BitmapImageViewTarget target = new BitmapImageViewTarget(userAudioImage) {


                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    userAudioImage.setImageBitmap(resource);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    userAudioImage.setImageDrawable(errorDrawable);
                }


                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    userAudioImage.setImageDrawable(placeHolderDrawable);
                }
            };

            messagesAdapter.glideRequests
                    .asBitmap()
                    .dontAnimate()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + userImage))

                    .signature(new ObjectKey(userImage))
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(target);

        } catch (Exception e) {

        }


    }

    private void setupProgressBarUploadAudio() {
        mProgressUploadAudioInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
        mProgressUploadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);

    }


    private void setupProgressBarDownloadAudio() {
        mProgressDownloadAudioInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
        mProgressDownloadAudio.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
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


    private void playAudio(String AudioDataSource) {

        if (getAdapterPosition() == messagesAdapter.playingPosition) {
            // toggle between play/pause of audio
         /*   if (messagesAdapter.isPlaying) {
                if (messagesAdapter.getPlayer() != null)
                    messagesAdapter.getPlayer().setPlayWhenReady(false);
            } else {*/
            if (messagesAdapter.getPlayer() != null)
                messagesAdapter.getPlayer().setPlayWhenReady(true);
            //}
        } else {
            // start another audio playback
            messagesAdapter.playingPosition = getAdapterPosition();
            if (messagesAdapter.getPlayer() != null) {
                if (null != messagesAdapter.mMessagesAudioViewHolder) {
                    messagesAdapter.updateNonPlayingView((MessagesAudioViewHolder) messagesAdapter.mMessagesAudioViewHolder);
                }
                messagesAdapter.getPlayer().release();
            }
            messagesAdapter.mMessagesAudioViewHolder = this;
            messagesAdapter.startMediaPlayer(AudioDataSource);
        }
        // messagesAdapter.updatePlayingView();

    }

    private void playingAudio(MessageModel messagesModel) {

        setAnimation(messagesModel.getDuration_file());

        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
            String AudioDataSource;

            try {

                if (FilesManager.isFileAudiosSentExists(mActivity, FilesManager.getAudio(messagesModel.getFile()))) {
                    AudioDataSource = FilesManager.getFileAudiosSentPath(mActivity, messagesModel.getFile());
                    playAudio(AudioDataSource);

                } else {

                    AudioDataSource = EndPoints.MESSAGE_AUDIO_URL + messagesModel.getFile();
                    playAudio(AudioDataSource);
                    // FilesManager.downloadFilesToDevice(mActivity, EndPoints.MESSAGE_AUDIO_URL + messagesModel.getFile(), messagesModel.getFile(), AppConstants.SENT_AUDIO);

                }

            } catch (IllegalArgumentException | IllegalStateException e) {
                e.printStackTrace();
            }

        } else {

            String AudioDataSource;

            try {
                if (FilesManager.isFileAudioExists(mActivity, FilesManager.getAudio(messagesModel.getFile()))) {
                    AudioDataSource = FilesManager.getFileAudioPath(mActivity, messagesModel.getFile());
                    playAudio(AudioDataSource);

                } else {
                    AudioDataSource = EndPoints.MESSAGE_AUDIO_URL + messagesModel.getFile();
                    playAudio(AudioDataSource);
                }

            } catch (Exception e) {
                AppHelper.LogCat("IOException audio recipient " + e.getMessage());
            }
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

    private byte[] generateDummyItem(int length, @NonNull Random rnd) {
        final byte[] raw = new byte[length];
        rnd.nextBytes(raw);

        return raw;
    }

    public void setAnimation(String duration) {
        audioSeekBar.setRawData(generateDummyItem(Integer.parseInt(duration), new Random()));

    }

    @Override
    public void onClick(View view) {
        if (!messagesAdapter.isActivated) {
            MessageModel messagesModel = messagesAdapter.getItem(getAdapterPosition());

            String senderId = messagesModel.getSenderId();
            String messageId = messagesModel.get_id();
            boolean isDownLoad = messagesModel.isFile_downLoad();
            switch (view.getId()) {

                case R.id.pause_btn_audio:
                    messagesAdapter.stopPlayer();
                    break;
                case R.id.play_btn_audio:

                    if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
                        if (messagesModel.isFile_upload()) {
                            playingAudio(messagesModel);
                        } else {
                            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
                        }
                    } else {
                        if (messagesModel.isFile_downLoad()) {
                            playingAudio(messagesModel);
                        } else {
                            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
                        }
                    }
                    break;

                case R.id.cancel_download_audio:
                    PendingFilesTask.removeFile(messageId, false, true);
                    break;
                case R.id.retry_download_audio_button:

                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initDownloadListener(messageId, messagesAdapter);
                    }
                    break;
                case R.id.cancel_upload_audio:
                    PendingFilesTask.removeFile(messageId, false, false);
                    break;
                case R.id.retry_upload_audio_button:

                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initUploadListener(messageId, messagesAdapter);
                    }
                    break;


                case R.id.replied_message_view:

                    if (messagesModel.isReply_message()) {

                        if (!MessagesController.getInstance().checkIfMessageExist(messagesModel.getReply_id()))
                            return;

                        messagesAdapter.scrollToItem(messagesModel.getReply_id());


                    } else {

                        if (!StoriesController.getInstance().checkIfSingleStoryExist(messagesModel.getReply_id()))
                            return;
                        StoryModel storyModel = StoriesController.getInstance().getStoryById(messagesModel.getReply_id());
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
    }

    public void setBlinkEffect() {
        AnimationsUtil.manageBlinkEffect(message_layout);
    }

    public void setListeners(final MessageModel messageModel) {


        if (messageModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
            if (!messageModel.isFile_upload()) {
                if (PendingFilesTask.containsFile(messageModel.get_id())) {
                    PendingFilesTask.updateUploadListener(messagesAdapter);
                }
            }
        } else {
            if (!messageModel.isFile_downLoad()) {
                if (PendingFilesTask.containsFile(messageModel.get_id())) {
                    PendingFilesTask.updateDownloadListener(messagesAdapter);
                }
            }
        }

    }


    @Override
    public void onProgressChanged(float progress, boolean fromUser) {
        if (fromUser) {
            if (messagesAdapter.getPlayer() != null)
                messagesAdapter.getPlayer().seekTo((long) progress);
        }
    }

    @Override
    public void onStartTracking(float progress) {
/*
        int totalDuration = (int) messagesAdapter.getPlayer().getDuration();
        int currentPosition = (int) UtilsTime.progressToTimer((int) progress, totalDuration);
        messagesAdapter.getPlayer().seekTo(currentPosition);
        messagesAdapter.updatePlayingView();*/
    }

    @Override
    public void onStopTracking(float progress) {
        AppHelper.LogCat("onStopTracking " + progress);
    }


    //methods for upload process
    public void onUploadUpdate(int percentage, String type) {
        //   AppHelper.LogCat("percentage " + percentage + " type " + type);
        switch (type) {

            case "audio":
                if (isUploadServiceStopped) return;

                mProgressUploadAudio.setVisibility(View.VISIBLE);
                cancelUploadAudio.setVisibility(View.VISIBLE);
                mProgressUploadAudioInitial.setVisibility(View.GONE);
                retryUploadAudioButton.setVisibility(View.GONE);
                mProgressUploadAudio.setIndeterminate(false);
                mProgressUploadAudio.setProgress(percentage);
                break;

        }
    }

    public void onUploadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        AppHelper.LogCat("on error " + type);
        switch (type) {

            case "audio":
                isUploadServiceStopped = true;
                mProgressUploadAudio.setVisibility(View.GONE);
                mProgressUploadAudioInitial.setVisibility(View.GONE);
                cancelUploadAudio.setVisibility(View.GONE);
                playBtnAudio.setVisibility(View.GONE);
                pauseBtnAudio.setVisibility(View.GONE);
                audioSeekBar.setEnabled(false);
                retryUploadAudioButton.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void onUploadFinish(String type, MessageModel messagesModel) {
        switch (type) {

            case "audio":
                isUploadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, false);
                mProgressUploadAudio.setVisibility(View.GONE);
                mProgressUploadAudioInitial.setVisibility(View.GONE);
                cancelUploadAudio.setVisibility(View.GONE);
                retryUploadAudioButton.setVisibility(View.GONE);
                playBtnAudio.setVisibility(View.VISIBLE);
                audioSeekBar.setEnabled(true);
                setAudioTotalDurationAudio(messagesModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES, messagesModel));
                break;

        }
    }

    public void onUploadStart(String type) {
        switch (type) {

            case "audio":
                isUploadServiceStopped = false;
                retryUploadAudioButton.setVisibility(View.GONE);
                setupProgressBarUploadAudio();
                mProgressUploadAudioInitial.setVisibility(View.VISIBLE);
                cancelUploadAudio.setVisibility(View.VISIBLE);
                mProgressUploadAudioInitial.setIndeterminate(true);
                break;

        }
    }

    //end methods for upload process
    //start methods for download process

    public void onDownloadStart(String type) {
        switch (type) {

            case "audio":
                isDownloadServiceStopped = false;
                setupProgressBarDownloadAudio();
                mProgressDownloadAudioInitial.setVisibility(View.VISIBLE);
                cancelDownloadAudio.setVisibility(View.VISIBLE);
                retryDownloadAudioButton.setVisibility(View.GONE);
                mProgressDownloadAudioInitial.setIndeterminate(true);

                break;

        }
    }

    public void onDownloadUpdate(int percentage, String type) {
        switch (type) {

            case "audio":
                if (isDownloadServiceStopped) return;
                mProgressDownloadAudioInitial.setVisibility(View.GONE);
                mProgressDownloadAudio.setVisibility(View.VISIBLE);
                cancelDownloadAudio.setVisibility(View.VISIBLE);
                retryDownloadAudioButton.setVisibility(View.GONE);
                mProgressDownloadAudio.setIndeterminate(false);
                mProgressDownloadAudio.setProgress(percentage);
                break;

        }


    }


    public void onDownloadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));

        switch (type) {

            case "audio":
                isDownloadServiceStopped = true;
                mProgressDownloadAudio.setVisibility(View.GONE);
                mProgressDownloadAudioInitial.setVisibility(View.GONE);
                cancelDownloadAudio.setVisibility(View.GONE);
                retryDownloadAudioButton.setVisibility(View.VISIBLE);
                break;

        }
    }


    public void onDownloadFinish(String type, MessageModel messagesModel) {
        switch (type) {

            case "audio":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadAudio.setVisibility(View.GONE);
                mProgressDownloadAudioInitial.setVisibility(View.GONE);
                cancelDownloadAudio.setVisibility(View.GONE);
                retryDownloadAudioButton.setVisibility(View.GONE);
                cancelDownloadAudio.setVisibility(View.GONE);
                playBtnAudio.setVisibility(View.VISIBLE);
                audioSeekBar.setEnabled(true);
                break;
        }
    }


}
