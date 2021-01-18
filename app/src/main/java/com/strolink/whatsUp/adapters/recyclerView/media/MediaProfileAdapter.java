package com.strolink.whatsUp.adapters.recyclerView.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MediaProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessageModel> mMessagesModel;
    private LayoutInflater mInflater;

    public MediaProfileAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void setMessages(List<MessageModel> mMessagesList) {
        this.mMessagesModel = mMessagesList;
        notifyDataSetChanged();
    }


    public List<MessageModel> getMessages() {
        return mMessagesModel;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_media_profile, parent, false);
        return new MediaProfileViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MediaProfileViewHolder mediaProfileViewHolder = (MediaProfileViewHolder) holder;
        final MessageModel messagesModel = this.mMessagesModel.get(position);
        try {
            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                mediaProfileViewHolder.imageFile.setVisibility(View.VISIBLE);
                mediaProfileViewHolder.setImage(messagesModel);
            } else {
                mediaProfileViewHolder.imageFile.setVisibility(View.GONE);
            }

            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                mediaProfileViewHolder.mediaAudio.setVisibility(View.VISIBLE);
            } else {
                mediaProfileViewHolder.mediaAudio.setVisibility(View.GONE);
            }
            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                mediaProfileViewHolder.mediaDocument.setVisibility(View.VISIBLE);
            } else {
                mediaProfileViewHolder.mediaDocument.setVisibility(View.GONE);
            }

            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                mediaProfileViewHolder.mediaVideo.setVisibility(View.VISIBLE);
                mediaProfileViewHolder.setMediaVideoThumbnail(messagesModel);
            } else {
                mediaProfileViewHolder.mediaVideo.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMessagesModel != null) {
            return mMessagesModel.size();
        } else {
            return 0;
        }
    }

    public class MediaProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.media_image)
        AppCompatImageView imageFile;
        @BindView(R.id.media_audio)
        AppCompatImageView mediaAudio;
        @BindView(R.id.media_document)
        AppCompatImageView mediaDocument;
        @BindView(R.id.media_video_thumbnail)
        AppCompatImageView mediaVideoThumbnail;
        @BindView(R.id.media_video)
        FrameLayout mediaVideo;
        @BindView(R.id.play_btn_video)
        ImageButton playVideo;


        MediaProfileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            imageFile.setOnClickListener(this);
            mediaVideo.setOnClickListener(this);
            mediaAudio.setOnClickListener(this);
            mediaDocument.setOnClickListener(this);
            playVideo.setOnClickListener(this);

        }


        @SuppressLint("StaticFieldLeak")
        void setImage(MessageModel messagesModel) {
            String messageId = messagesModel.get_id();
            String imageUrl = messagesModel.getFile();
            if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {


                if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(imageUrl))) {
                    GlideApp.with(mActivity)
                            .load(FilesManager.getFileImageSent(mActivity, imageUrl))

                            .signature(new ObjectKey(imageUrl))
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(imageFile);
                } else {
                    DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            imageFile.setImageDrawable(placeholder);
                        }
                    };
                    GlideApp.with(mActivity.getApplicationContext())
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + imageUrl))

                            .signature(new ObjectKey(imageUrl))
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }


            } else {

                if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(imageUrl))) {
                    GlideApp.with(mActivity)
                            .load(FilesManager.getFileImageSent(mActivity, imageUrl))

                            .signature(new ObjectKey(imageUrl))
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(imageFile);


                } else {
                    DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {


                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            imageFile.setImageDrawable(placeholder);
                        }
                    };
                    GlideApp.with(mActivity)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + imageUrl))

                            .signature(new ObjectKey(imageUrl))
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }

            }


        }


        @SuppressLint("StaticFieldLeak")
        void setMediaVideoThumbnail(MessageModel messagesModel) {

            String messageId = messagesModel.get_id();
            String imageUrl = messagesModel.getFile();

            long interval = 5000 * 1000;
            RequestOptions options = new RequestOptions().frame(interval);
            if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {


                BitmapImageViewTarget target = new BitmapImageViewTarget(mediaVideoThumbnail) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);

                        mediaVideoThumbnail.setImageBitmap(resource);
                        FilesManager.downloadMediaFile(mActivity, resource, imageUrl, AppConstants.SENT_IMAGE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        mediaVideoThumbnail.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        mediaVideoThumbnail.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(mActivity)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + imageUrl))
                        .signature(new ObjectKey(imageUrl))
                        .apply(options)
                        .centerCrop()
                        .placeholder(R.drawable.bg_rect_image_holder)
                        .error(R.drawable.bg_rect_image_holder)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);

            } else {


                if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(imageUrl))) {
                    GlideApp.with(mActivity)
                            .load(FilesManager.getFileImage(mActivity, imageUrl))

                            .signature(new ObjectKey(imageUrl))
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(mediaVideoThumbnail);

                } else {
                    BitmapImageViewTarget target = new BitmapImageViewTarget(mediaVideoThumbnail) {


                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            mediaVideoThumbnail.setImageBitmap(resource);
                            FilesManager.downloadMediaFile(mActivity, resource, imageUrl, AppConstants.RECEIVED_IMAGE);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            mediaVideoThumbnail.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            mediaVideoThumbnail.setImageDrawable(placeholder);
                        }
                    };
                    GlideApp.with(mActivity)
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + imageUrl))
                            .signature(new ObjectKey(imageUrl))
                            .apply(options)
                            .centerCrop()
                            .placeholder(R.drawable.bg_rect_image_holder)
                            .error(R.drawable.bg_rect_image_holder)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }


            }

        }


        @Override
        public void onClick(View view) {
            MessageModel messagesModel = mMessagesModel.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.media_audio:
                    playingAudio(messagesModel);
                    break;

                case R.id.media_video:
                    playingVideo(messagesModel);
                    break;
                case R.id.play_btn_video:
                    playingVideo(messagesModel);
                    break;

                case R.id.media_document:
                    if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        if (FilesManager.isFileDocumentsSentExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                            openDocument(FilesManager.getFileDocumentSent(mActivity, messagesModel.getFile()));
                        } else {
                            File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                            openDocument(file);
                        }
                    } else {
                        if (FilesManager.isFileDocumentsExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                            openDocument(FilesManager.getFileDocument(mActivity, messagesModel.getFile()));
                        } else {
                            File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                            openDocument(file);
                        }
                    }

                    break;

                case R.id.media_image:
                    showImage(messagesModel);
                    break;
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

    private void showImage(MessageModel messagesModel) {
        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(messagesModel.getFile()))) {
                AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE, messagesModel.getFile(), messagesModel.getSenderId());
            } else {
                if (messagesModel.getFile() != null)
                    AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE_FROM_SERVER, messagesModel.getFile(), messagesModel.getSenderId());
            }
        } else {

            if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(messagesModel.getFile()))) {
                AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE, messagesModel.getFile(), messagesModel.getSenderId());

            } else {
                if (messagesModel.getFile() != null)
                    AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE_FROM_SERVER, messagesModel.getFile(), messagesModel.getSenderId());
            }
        }
    }

    private void playingAudio(MessageModel messagesModel) {
        String audioFile = messagesModel.getFile();

        if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (FilesManager.isFileAudiosSentExists(mActivity, FilesManager.getAudio(audioFile))) {
                File fileAudio = FilesManager.getFileAudioSent(mActivity, audioFile);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = FilesManager.getFile(fileAudio);
                intent.setDataAndType(data, "audio/*");
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_app_to_play_audio));
                }

            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
            }
        } else {

            if (FilesManager.isFileAudioExists(mActivity, FilesManager.getAudio(audioFile))) {
                File fileAudio = FilesManager.getFileAudio(mActivity, audioFile);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = FilesManager.getFile(fileAudio);
                intent.setDataAndType(data, "audio/*");
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_app_to_play_audio));
                }

            } else {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_audio_is_not_exist));
            }
        }

    }

    private void openDocument(File file) {
        if (file.exists()) {
            Uri path = FilesManager.getFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_pdf));
            }
        }
    }

}

