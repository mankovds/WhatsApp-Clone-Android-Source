package com.strolink.whatsUp.adapters.viewHolders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
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

import java.io.File;
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
public class MessagesDocumentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


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

    //for documents
    @BindView(R.id.progress_bar_upload_document)
    public ProgressBar mProgressUploadDocument;
    @BindView(R.id.progress_bar_upload_document_init)
    public ProgressBar mProgressUploadDocumentInitial;

    @BindView(R.id.cancel_upload_document)
    public AppCompatImageButton cancelUploadDocument;

    @BindView(R.id.retry_upload_document_button)
    public AppCompatImageButton retryUploadDocumentButton;

    @BindView(R.id.progress_bar_download_document)
    public ProgressBar mProgressDownloadDocument;

    @BindView(R.id.progress_bar_download_document_init)
    public ProgressBar mProgressDownloadDocumentInitial;

    @BindView(R.id.cancel_download_document)
    public AppCompatImageButton cancelDownloadDocument;


    @BindView(R.id.retry_download_document_button)
    public AppCompatImageButton retryDownloadDocumentButton;

    @BindView(R.id.document_title)
    public AppCompatTextView documentTitle;

    @BindView(R.id.document_image)
    public AppCompatImageView documentImage;

    @BindView(R.id.document_size)
    public AppCompatTextView fileSizeDocument;

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

    public MessagesDocumentViewHolder(MessagesAdapter messagesAdapter, @NonNull View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;

        ButterKnife.bind(this, itemView);
        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        //for document upload
        setupProgressBarUploadDocument();

        replied_message_view.setOnClickListener(this);
        cancelDownloadDocument.setOnClickListener(this);
        retryDownloadDocumentButton.setOnClickListener(this);
        cancelUploadDocument.setOnClickListener(this);
        retryUploadDocumentButton.setOnClickListener(this);
        documentTitle.setOnClickListener(this);
/*
        itemView.setOnClickListener(view -> {

        });*/


    }


    public void setDocumentTitle(MessageModel messagesModel) {
        setDocumentIcon(messagesModel.getDocument_type());
        String documentFile = messagesModel.getFile();
        String senderId = messagesModel.getSenderId();
        //   File file1 = new File(documentFile);
        boolean isDownLoad = messagesModel.isFile_downLoad();
        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
            File file;
            if (FilesManager.isFileDocumentsSentExists(mActivity, FilesManager.getDocument(documentFile))) {
                //   file = FilesManager.getFileDocumentSent(mActivity, documentFile);
                //  String document_title = file.getName();
                ///  documentTitle.setText(document_title);
                documentTitle.setText(messagesModel.getDocument_name());
                documentImage.setVisibility(View.VISIBLE);
            } else {
                if (messagesModel.isFile_upload())
                    documentImage.setVisibility(View.VISIBLE);


                //  FilesManager.downloadFilesToDevice(mActivity, EndPoints.MESSAGE_DOCUMENT_DOWNLOAD_URL + documentFile, documentFile, AppConstants.SENT_DOCUMENTS);
                // documentTitle.setText(R.string.document);
                documentTitle.setText(messagesModel.getDocument_name());
            }

        } else {
            if (isDownLoad) {
                documentImage.setVisibility(View.VISIBLE);
                File file;
                if (FilesManager.isFileDocumentsExists(mActivity, FilesManager.getDocument(documentFile))) {
                    //     file = FilesManager.getFileDocument(mActivity, documentFile);
                    //  String document_title = file.getName();
                    //documentTitle.setText(document_title);
                    documentTitle.setText(messagesModel.getDocument_name());
                } else {
                    //documentTitle.setText(R.string.document);
                    documentTitle.setText(messagesModel.getDocument_name());
                }
            } else {
                retryDownloadDocumentButton.setVisibility(View.VISIBLE);
                //   documentTitle.setText(R.string.document);
                documentTitle.setText(messagesModel.getDocument_name());


            }

        }

        try {
            getFileSize(messagesModel.getFile_size());
        } catch (Exception e) {
            AppHelper.LogCat("Exception of file size");
        }

    }


    private void getFileSize(String size) {
        try {
            long filesSize = Long.parseLong(size);

            fileSizeDocument.setVisibility(View.VISIBLE);
            fileSizeDocument.setText(String.valueOf(FilesManager.getFileSize(filesSize)));
        } catch (Exception e) {
            AppHelper.LogCat(" MessagesAdapter " + e.getMessage());
        }


    }

    private void setupProgressBarDownloadDocument() {
        mProgressDownloadDocumentInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
        mProgressDownloadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
    }

    private void setupProgressBarUploadDocument() {
        mProgressUploadDocumentInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
        mProgressUploadDocument.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
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


    @Override
    public void onClick(View view) {
        if (!messagesAdapter.isActivated) {
            MessageModel messagesModel = messagesAdapter.getItem(getAdapterPosition());

            String messageId = messagesModel.get_id();
            switch (view.getId()) {

                case R.id.cancel_download_document:
                    PendingFilesTask.removeFile(messageId, false, true);
                    break;
                case R.id.retry_download_document_button:

                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initDownloadListener(messageId, messagesAdapter);

                    }
                    break;

                case R.id.cancel_upload_document:
                    PendingFilesTask.removeFile(messageId, false, false);
                    break;
                case R.id.retry_upload_document_button:

                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initUploadListener(messageId, messagesAdapter);

                    }
                    break;
                case R.id.document_title:
                    if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        if (messagesModel.isFile_upload())
                            if (FilesManager.isFileDocumentsSentExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                                openDocument(FilesManager.getFileDocumentSent(mActivity, messagesModel.getFile()), messagesModel.getDocument_type());
                            } else {
                                File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                                openDocument(file, messagesModel.getDocument_type());
                            }
                    } else {
                        if (messagesModel.isFile_downLoad())
                            if (FilesManager.isFileDocumentsExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                                openDocument(FilesManager.getFileDocument(mActivity, messagesModel.getFile()), messagesModel.getDocument_type());
                            } else {
                                File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                                openDocument(file, messagesModel.getDocument_type());
                            }
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


    private void openDocument(File file, String type) {
        if (file.exists()) {
            Uri path = FilesManager.getFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            switch (type) {
                case AppConstants.MESSAGES_DOCUMENT_PDF:
                    intent.setDataAndType(path, "application/pdf");
                    if (AppHelper.isAndroid7()) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    try {
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_pdf));
                    }
                    break;
                case AppConstants.MESSAGES_DOCUMENT_DOC:
                    intent.setDataAndType(path, "application/msword");
                    if (AppHelper.isAndroid7()) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    try {
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_word));
                    }
                    break;
                case AppConstants.MESSAGES_DOCUMENT_PPT:

                    intent.setDataAndType(path, "application/vnd.ms-powerpoint");
                    if (AppHelper.isAndroid7()) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    try {
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_ppt));
                    }
                    break;
                case AppConstants.MESSAGES_DOCUMENT_EXCEL:


                    intent.setDataAndType(path, "application/vnd.ms-excel");
                    if (AppHelper.isAndroid7()) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    try {
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_excel));
                    }
                    break;
            }
        }
    }


    private void setDocumentIcon(String type) {
        switch (type) {
            case "pdf":
                documentImage.setBackgroundDrawable(AppHelper.getVectorDrawable(mActivity, R.drawable.icon_pdf));
                fileSizeDocument.setTextColor(AppHelper.getColor(mActivity, R.color.pdf));
                break;
            case "word":
                documentImage.setBackgroundDrawable(AppHelper.getVectorDrawable(mActivity, R.drawable.icon_doc));
                fileSizeDocument.setTextColor(AppHelper.getColor(mActivity, R.color.doc));
                break;
            case "ppt":
                documentImage.setBackgroundDrawable(AppHelper.getVectorDrawable(mActivity, R.drawable.icon_ppt));
                fileSizeDocument.setTextColor(AppHelper.getColor(mActivity, R.color.ppt));
                break;
            case "excel":
                documentImage.setBackgroundDrawable(AppHelper.getVectorDrawable(mActivity, R.drawable.icon_xls));
                fileSizeDocument.setTextColor(AppHelper.getColor(mActivity, R.color.xls));
                break;
            default:
                documentImage.setBackgroundDrawable(AppHelper.getVectorDrawable(mActivity, R.drawable.icon_pdf));
                fileSizeDocument.setTextColor(AppHelper.getColor(mActivity, R.color.pdf));
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

    //methods for upload process
    public void onUploadUpdate(int percentage, String type) {
        AppHelper.LogCat("percentage " + percentage + " type " + type);
        switch (type) {

            case "document":

                if (isUploadServiceStopped) return;

                //if (mProgressUploadDocument.getVisibility() == View.GONE) {
                mProgressUploadDocument.setVisibility(View.VISIBLE);
                cancelUploadDocument.setVisibility(View.VISIBLE);
                retryUploadDocumentButton.setVisibility(View.GONE);
                documentImage.setVisibility(View.GONE);
                mProgressUploadDocumentInitial.setVisibility(View.GONE);
                mProgressUploadDocument.setIndeterminate(false);
                //  }
                mProgressUploadDocument.setProgress(percentage);
                break;

        }
    }

    public void onUploadError(String type) {

        AppHelper.LogCat("on error " + type);
        switch (type) {

            case "document":
                isUploadServiceStopped = true;
                //  if (retryUploadDocumentButton.getVisibility() == View.GONE) {
                if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
                mProgressUploadDocument.setVisibility(View.GONE);
                mProgressUploadDocumentInitial.setVisibility(View.GONE);
                cancelUploadDocument.setVisibility(View.GONE);
                documentImage.setVisibility(View.GONE);
                retryUploadDocumentButton.setVisibility(View.VISIBLE);
                //  }
                break;

        }
    }

    public void onUploadFinish(String type, MessageModel messagesModel) {

        //   messagesAdapter.mWaitingTaskSparseArray.remove(mId);

        switch (type) {

            case "document":
                isUploadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, false);
                mProgressUploadDocument.setVisibility(View.GONE);
                mProgressUploadDocumentInitial.setVisibility(View.GONE);
                cancelUploadDocument.setVisibility(View.GONE);
                retryUploadDocumentButton.setVisibility(View.GONE);
                documentImage.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES, messagesModel));
                break;

        }
    }

    public void onUploadStart(String type) {
        switch (type) {

            case "document":
                isUploadServiceStopped = false;
                retryUploadDocumentButton.setVisibility(View.GONE);
                setupProgressBarUploadDocument();
                mProgressUploadDocumentInitial.setVisibility(View.VISIBLE);
                cancelUploadDocument.setVisibility(View.VISIBLE);
                documentImage.setVisibility(View.GONE);
                mProgressUploadDocumentInitial.setIndeterminate(true);
                break;

        }
    }

    //end methods for upload process
    //start methods for download process

    public void onDownloadStart(String type) {
        switch (type) {

            case "document":
                isDownloadServiceStopped = false;
                setupProgressBarDownloadDocument();
                mProgressDownloadDocumentInitial.setVisibility(View.VISIBLE);
                cancelDownloadDocument.setVisibility(View.VISIBLE);
                retryDownloadDocumentButton.setVisibility(View.GONE);
                documentImage.setVisibility(View.GONE);
                mProgressDownloadDocumentInitial.setIndeterminate(true);
                break;

        }
    }

    public void onDownloadUpdate(int percentage, String type) {
        switch (type) {

            case "document":
                if (isDownloadServiceStopped) return;
                mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                mProgressDownloadDocument.setVisibility(View.VISIBLE);
                cancelDownloadDocument.setVisibility(View.VISIBLE);
                retryDownloadDocumentButton.setVisibility(View.GONE);
                documentImage.setVisibility(View.GONE);
                mProgressDownloadDocument.setIndeterminate(false);
                mProgressDownloadDocument.setProgress(percentage);
                break;

        }


    }


    public void onDownloadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));

        switch (type) {


            case "document":
                isDownloadServiceStopped = true;
                mProgressDownloadDocument.setVisibility(View.GONE);
                mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                cancelDownloadDocument.setVisibility(View.GONE);
                retryDownloadDocumentButton.setVisibility(View.VISIBLE);
                break;

        }
    }


    public void onDownloadFinish(String type, MessageModel messagesModel) {
        switch (type) {

            case "document":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadDocument.setVisibility(View.GONE);
                mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                cancelDownloadDocument.setVisibility(View.GONE);
                retryDownloadDocumentButton.setVisibility(View.GONE);
                documentImage.setVisibility(View.VISIBLE);

                break;
        }
    }

}
