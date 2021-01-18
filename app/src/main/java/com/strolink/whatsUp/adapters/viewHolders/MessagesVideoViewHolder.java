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
import com.strolink.whatsUp.helpers.glide.GlideApp;
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

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by Abderrahim El imame on 10/5/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MessagesVideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    //var for upload videos
    @BindView(R.id.video_thumbnail)
    public AppCompatImageView videoThumbnailFile;
    @BindView(R.id.play_btn_video)
    public AppCompatImageButton playBtnVideo;
    @BindView(R.id.progress_bar_upload_video)
    public ProgressBar mProgressUploadVideo;
    @BindView(R.id.progress_bar_upload_video_init)
    public ProgressBar mProgressUploadVideoInitial;
    @BindView(R.id.cancel_upload_video)
    public AppCompatImageButton cancelUploadVideo;
    @BindView(R.id.retry_upload_video)
    public LinearLayout retryUploadVideo;
    @BindView(R.id.progress_bar_download_video)
    public ProgressBar mProgressDownloadVideo;
    @BindView(R.id.progress_bar_download_video_init)
    public ProgressBar mProgressDownloadVideoInitial;
    @BindView(R.id.cancel_download_video)
    public AppCompatImageButton cancelDownloadVideo;
    @BindView(R.id.download_video)
    public LinearLayout downloadVideo;
    @BindView(R.id.file_size_video)
    public AppCompatTextView fileSizeVideo;

    @BindView(R.id.video_total_duration)
    public AppCompatTextView videoTotalDuration;
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

    private Activity mActivity;

    @BindView(R.id.message_layout)
    public LinearLayout message_layout;

    private MessagesAdapter messagesAdapter;

    private boolean isUploadServiceStopped = false;
    private boolean isDownloadServiceStopped = false;

    public MessagesVideoViewHolder(MessagesAdapter messagesAdapter, @NonNull View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;

        ButterKnife.bind(this, itemView);
        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        //for video upload
        setupProgressBarUploadVideo();


        replied_message_view.setOnClickListener(this);
        cancelDownloadVideo.setOnClickListener(this);
        downloadVideo.setOnClickListener(this);
        cancelUploadVideo.setOnClickListener(this);
        retryUploadVideo.setOnClickListener(this);
        videoThumbnailFile.setOnClickListener(this);
        playBtnVideo.setOnClickListener(this);


        itemView.setOnClickListener(view -> {


        });


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

    public void getFileSize(String size, String type) {
        try {
            long filesSize = Long.parseLong(size);
            switch (type) {
                case "video":
                    fileSizeVideo.setVisibility(View.VISIBLE);
                    fileSizeVideo.setText(String.valueOf(FilesManager.getFileSize(filesSize)));
                    break;
            }
        } catch (Exception e) {
            AppHelper.LogCat(" MessagesAdapter " + e.getMessage());
        }


    }


    public void setVideoTotalDuration(MessageModel messagesModel) {
        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
            videoTotalDuration.setVisibility(View.VISIBLE);
            try {

                long timeInMilliSecond = Long.parseLong(messagesModel.getDuration_file());
                setVideoTotalTime(timeInMilliSecond);

            } catch (Exception e) {
                AppHelper.LogCat("Exception total duration " + e.getMessage());
            }


        } else {
            videoTotalDuration.setVisibility(View.VISIBLE);
            try {
                long timeInMilliSecond = Long.parseLong(messagesModel.getDuration_file());
                setVideoTotalTime(timeInMilliSecond);
            } catch (Exception e) {
                AppHelper.LogCat("Exception total duration " + e.getMessage());
            }


        }

    }

    public void setVideoTotalTime(long totalTime) {
        videoTotalDuration.setText(UtilsTime.getFileTime(totalTime));
    }

    public void setVideoThumbnailFileOffline(MessageModel messagesModel) {

        String ImageUrl = messagesModel.getFile();
        String messageId = messagesModel.get_id();
        File file = new File(ImageUrl);

        BitmapImageViewTarget target = new BitmapImageViewTarget(videoThumbnailFile) {


            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                videoThumbnailFile.setImageBitmap(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                videoThumbnailFile.setImageDrawable(errorDrawable);
            }


            @Override
            public void onLoadStarted(Drawable placeHolderDrawable) {
                super.onLoadStarted(placeHolderDrawable);
                videoThumbnailFile.setImageDrawable(placeHolderDrawable);
            }
        };
        messagesAdapter.glideRequests
                .asBitmap()
                .dontAnimate()
                .load(file)
                .signature(new ObjectKey(ImageUrl))
                .centerCrop()
                .error(R.drawable.image_holder_full_screen)
                .placeholder(R.drawable.image_holder_full_screen)
                .into(target);


    }

    public void setVideoThumbnailFile(MessageModel messagesModel) {
        String videoUrl = messagesModel.getFile();
        String senderId = messagesModel.getSenderId();
        String messageId = messagesModel.get_id();
        boolean isDownLoad = messagesModel.isFile_downLoad();

        long interval = 3000 * 1000;
        RequestOptions options = new RequestOptions().frame(interval);
        RequestBuilder<Bitmap> thumbnailRequest = GlideApp.with(mActivity)
                .asBitmap()
                .override(10, 10)// Example
                .apply(options)
                .centerCrop()
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl));

        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (messagesModel.isFile_upload()) {
                playBtnVideo.setVisibility(View.VISIBLE);

                if (FilesManager.isFileVideosSentExists(mActivity, FilesManager.getVideo(videoUrl))) {

                   /* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                        glideRequests
                                .load(EndPoints.MESSAGE_VIDEO_THUMBNAIL_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()

                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))


                                .into(videoThumbnailFile);
                    } else {*/

                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(FilesManager.getFileVideoSent(mActivity, videoUrl))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(videoThumbnailFile);
                    // }
                } else {

                    BitmapImageViewTarget target = new BitmapImageViewTarget(videoThumbnailFile) {


                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            videoThumbnailFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            videoThumbnailFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            videoThumbnailFile.setImageDrawable(placeHolderDrawable);
                        }
                    };

                  /*  if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                        glideRequests
                                .load(EndPoints.MESSAGE_VIDEO_HOLDER_THUMBNAIL_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()

                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId()))


                                .into(target);
                    } else {*/
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                    // }


                }
            } else {
                playBtnVideo.setVisibility(View.GONE);

                if (FilesManager.isFileVideosExists(mActivity, FilesManager.getVideo(videoUrl))) {

                   /* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                        glideRequests
                                .load(EndPoints.MESSAGE_VIDEO_THUMBNAIL_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()

                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))


                                .into(videoThumbnailFile);
                    } else {*/

                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load((FilesManager.getFileVideo(mActivity, videoUrl)))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(videoThumbnailFile);
                    // }
                } else {
                    BitmapImageViewTarget target = new BitmapImageViewTarget(videoThumbnailFile) {


                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            videoThumbnailFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            videoThumbnailFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            videoThumbnailFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                  /*  if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                        glideRequests
                                .load(EndPoints.MESSAGE_VIDEO_HOLDER_THUMBNAIL_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()

                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId()))


                                .into(target);
                    } else {*/
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                    //   }
                }


            }

        } else {

            if (isDownLoad) {
                AppHelper.LogCat("isDownLoad   ");
                downloadVideo.setVisibility(View.GONE);
                playBtnVideo.setVisibility(View.VISIBLE);

                if (FilesManager.isFileVideosExists(mActivity, FilesManager.getVideo(videoUrl))) {

                    AppHelper.LogCat("isFileExists");
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(videoThumbnailFile);

                } else {
                    AppHelper.LogCat("isFileExists not " + EndPoints.MESSAGE_VIDEO_URL + videoUrl);

                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl))
                            .signature(new ObjectKey(videoUrl))
                            .apply(options)
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(videoThumbnailFile);

                }

            } else {

                downloadVideo.setVisibility(View.VISIBLE);
                playBtnVideo.setVisibility(View.GONE);
                getFileSize(messagesModel.getFile_size(), "video");

                messagesAdapter.glideRequests
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + videoUrl))
                        .signature(new ObjectKey(videoUrl))
                        .apply(options)
                        .dontAnimate()
                        .thumbnail(thumbnailRequest)
                        .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                        .centerCrop()
                        .override(AppConstants.PRE_MESSAGE_IMAGE_SIZE, AppConstants.PRE_MESSAGE_IMAGE_SIZE)
                        .error(R.drawable.image_holder_full_screen)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .into(videoThumbnailFile);


            }
        }


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

    private void setupProgressBarUploadVideo() {
        mProgressUploadVideoInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressUploadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
    }


    private void setupProgressBarDownloadVideo() {
        mProgressDownloadVideoInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressDownloadVideo.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);

    }

    @Override
    public void onClick(View view) {
        if (!messagesAdapter.isActivated) {
            MessageModel messagesModel = messagesAdapter.getItem(getAdapterPosition());

            String senderId = messagesModel.getSenderId();
            String messageId = messagesModel.get_id();
            boolean isDownLoad = messagesModel.isFile_downLoad();
            switch (view.getId()) {
                case R.id.video_thumbnail:
                    if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
                        if (messagesModel.isFile_upload()) {
                            playingVideo(messagesModel);
                        }

                    } else {
                        if (messagesModel.isFile_downLoad()) {
                            playingVideo(messagesModel);
                        }
                    }

                    break;
                case R.id.play_btn_video:
                    if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
                        if (messagesModel.isFile_upload()) {
                            playingVideo(messagesModel);
                        }

                    } else {
                        if (messagesModel.isFile_downLoad()) {
                            playingVideo(messagesModel);
                        }
                    }

                    break;


                case R.id.download_video:

                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initDownloadListener(messageId, messagesAdapter);
                    }
                    break;
                case R.id.cancel_download_video:
                    PendingFilesTask.removeFile(messageId, false, true);
                    break;
                case R.id.cancel_upload_video:
                    PendingFilesTask.removeFile(messageId, false, false);
                    setShowUpload();
                    break;
                case R.id.retry_upload_video:

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


    private void playingVideo(MessageModel messagesModel) {
        String video = messagesModel.getFile();

        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (FilesManager.isFileVideosSentExists(mActivity, FilesManager.getVideo(video))) {
                AppHelper.LaunchVideoPreviewActivity(mActivity, video, true);
            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_video_is_not_exist));
            }
        } else {

            if (FilesManager.isFileVideosExists(mActivity, FilesManager.getVideo(video))) {
                AppHelper.LaunchVideoPreviewActivity(mActivity, video, false);
            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_video_is_not_exist));
            }
        }


    }


    public void setHideUpload() {
        mProgressUploadVideo.setVisibility(View.GONE);
        mProgressUploadVideoInitial.setVisibility(View.GONE);
        cancelUploadVideo.setVisibility(View.GONE);
        retryUploadVideo.setVisibility(View.GONE);
        playBtnVideo.setVisibility(View.GONE);
    }

    public void setShowUpload() {
        mProgressUploadVideo.setVisibility(View.GONE);
        mProgressUploadVideoInitial.setVisibility(View.GONE);
        cancelUploadVideo.setVisibility(View.GONE);
        retryUploadVideo.setVisibility(View.VISIBLE);
        playBtnVideo.setVisibility(View.GONE);
    }

    //methods for upload process
    public void onUploadUpdate(int percentage, String type) {
        //     AppHelper.LogCat("percentage " + percentage + " type " + type);
        switch (type) {

            case "video":
                if (isUploadServiceStopped) return;

                mProgressUploadVideo.setVisibility(View.VISIBLE);
                cancelUploadVideo.setVisibility(View.VISIBLE);
                mProgressUploadVideoInitial.setVisibility(View.GONE);
                retryUploadVideo.setVisibility(View.GONE);
                mProgressUploadVideo.setIndeterminate(false);
                mProgressUploadVideo.setProgress(percentage);
                break;

        }
    }

    public void onUploadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        AppHelper.LogCat("on error " + type);
        switch (type) {

            case "video":
                isUploadServiceStopped = true;
                mProgressUploadVideo.setVisibility(View.GONE);
                mProgressUploadVideoInitial.setVisibility(View.GONE);
                cancelUploadVideo.setVisibility(View.GONE);
                retryUploadVideo.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void onUploadFinish(String type, MessageModel messagesModel) {
        switch (type) {

            case "video":
                isUploadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, false);
                mProgressUploadVideo.setVisibility(View.GONE);
                mProgressUploadVideoInitial.setVisibility(View.GONE);
                cancelUploadVideo.setVisibility(View.GONE);
                retryUploadVideo.setVisibility(View.GONE);
                setVideoThumbnailFile(messagesModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES, messagesModel));
                break;

        }
    }

    public void onUploadStart(String type) {
        switch (type) {

            case "video":
                isUploadServiceStopped = false;
                retryUploadVideo.setVisibility(View.GONE);
                setupProgressBarUploadVideo();
                mProgressUploadVideoInitial.setVisibility(View.VISIBLE);
                cancelUploadVideo.setVisibility(View.VISIBLE);
                mProgressUploadVideoInitial.setIndeterminate(true);
                break;

        }
    }

    //end methods for upload process
    //start methods for download process

    public void onDownloadStart(String type) {
        switch (type) {

            case "video":
                isDownloadServiceStopped = false;
                setupProgressBarDownloadVideo();
                mProgressDownloadVideoInitial.setVisibility(View.VISIBLE);
                cancelDownloadVideo.setVisibility(View.VISIBLE);
                downloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideoInitial.setIndeterminate(true);

                break;

        }
    }

    public void onDownloadUpdate(int percentage, String type) {
        switch (type) {

            case "video":
                if (isDownloadServiceStopped) return;
                mProgressDownloadVideoInitial.setVisibility(View.GONE);
                mProgressDownloadVideo.setVisibility(View.VISIBLE);
                cancelDownloadVideo.setVisibility(View.VISIBLE);
                downloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideo.setIndeterminate(false);
                mProgressDownloadVideo.setProgress(percentage);
                break;

        }


    }


    public void onDownloadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        switch (type) {


            case "video":
                isDownloadServiceStopped = true;
                mProgressDownloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideoInitial.setVisibility(View.GONE);
                cancelDownloadVideo.setVisibility(View.GONE);
                downloadVideo.setVisibility(View.VISIBLE);
                break;

        }
    }


    public void onDownloadFinish(String type, MessageModel messagesModel) {
        switch (type) {

            case "video":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadVideo.setVisibility(View.GONE);
                mProgressDownloadVideoInitial.setVisibility(View.GONE);
                cancelDownloadVideo.setVisibility(View.GONE);
                downloadVideo.setVisibility(View.GONE);
                setVideoThumbnailFile(messagesModel);
                setVideoTotalDuration(messagesModel);

                break;
        }
    }


}
