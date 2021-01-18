package com.strolink.whatsUp.activities.media;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.images.ImageUtils;
import com.strolink.whatsUp.ui.TouchImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 9/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ImagePreviewActivity extends BaseActivity {


    @BindView(R.id.image_file)
    TouchImageView imageView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private String ImageType;
    private String Identifier;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        if (AppHelper.isAndroid5()) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.activity_image_preview);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            ImageType = getIntent().getExtras().getString("ImageType");
            Identifier = getIntent().getExtras().getString("Identifier");
            currentUserId = getIntent().getExtras().getString("currentUserId");
            boolean saveIntent = getIntent().getExtras().getBoolean("SaveIntent");
            if (saveIntent) {
                getImage(ImageType, Identifier, Identifier, true, currentUserId);
            } else {
                getImage(ImageType, Identifier, Identifier, false, currentUserId);
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.backBtn)
    void back() {
        finish();

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.shareBtn)
    void ShareContent() {
        switch (ImageType) {
            case AppConstants.SENT_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.SENT_IMAGE, currentUserId));
                break;
            case AppConstants.RECEIVED_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.RECEIVED_IMAGE, currentUserId));
                break;
            case AppConstants.PROFILE_IMAGE:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.PROFILE_IMAGE, currentUserId));
                break;
            case AppConstants.SENT_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.SENT_IMAGE_FROM_SERVER, currentUserId));
                break;
            case AppConstants.RECEIVED_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.RECEIVED_IMAGE_FROM_SERVER, currentUserId));
                break;
            case AppConstants.PROFILE_IMAGE_FROM_SERVER:
                startActivity(ImageUtils.getNativeShareIntent(this, Identifier, AppConstants.PROFILE_IMAGE_FROM_SERVER, currentUserId));
                break;
        }

    }


    private void getImage(String ImageType, String ImageUrl, String ImageUrlHolder, boolean forSave, String currentUserId) {
        progressBar.setVisibility(View.VISIBLE);
        String ImageUrlFinal;

        RequestBuilder<Bitmap> thumbnailRequest;
        BitmapImageViewTarget bitmapImageViewTarget;

        File fileUrl;
        switch (ImageType) {
            case AppConstants.SENT_IMAGE:
                fileUrl = FilesManager.getFileImageSent(this, ImageUrl);
                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this)
                        .asBitmap()
                        .load(fileUrl)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);

                break;
            case AppConstants.RECEIVED_IMAGE:
                fileUrl = FilesManager.getFileImage(this, ImageUrl);

                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this)
                        .asBitmap()
                        .load(fileUrl)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);
                break;
            case AppConstants.PROFILE_IMAGE:
                fileUrl = FilesManager.getFileProfilePhoto(this, ImageUrl);
                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this)
                        .asBitmap()
                        .load(fileUrl)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);
                break;
            case AppConstants.SENT_IMAGE_FROM_SERVER:

                ImageUrlFinal = EndPoints.MESSAGE_IMAGE_URL + ImageUrl;

                thumbnailRequest = GlideApp.with(this).asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrlFinal))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        if (forSave) {
                            new Handler().postDelayed(() -> {

                                FilesManager.downloadMediaFile(ImagePreviewActivity.this, resource, Identifier, AppConstants.SENT_IMAGE);
                                AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                finish();
                            }, 1000);
                        }
                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrlFinal))
                        .thumbnail(thumbnailRequest)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);
                break;
            case AppConstants.RECEIVED_IMAGE_FROM_SERVER:


                ImageUrlFinal = EndPoints.MESSAGE_IMAGE_URL + ImageUrl;


                thumbnailRequest = GlideApp.with(this).asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrlFinal))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        if (forSave) {
                            new Handler().postDelayed(() -> {

                                FilesManager.downloadMediaFile(ImagePreviewActivity.this, resource, Identifier, AppConstants.RECEIVED_IMAGE);
                                AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                finish();
                            }, 1000);
                        }
                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this).asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrlFinal))
                        .thumbnail(thumbnailRequest)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);
                break;
            case AppConstants.PROFILE_IMAGE_FROM_SERVER:


                ImageUrlFinal = EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + ImageUrl;


                bitmapImageViewTarget = new BitmapImageViewTarget(imageView) {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        if (forSave) {
                            new Handler().postDelayed(() -> {
                                FilesManager.downloadMediaFile(ImagePreviewActivity.this, resource, Identifier, AppConstants.PROFILE_IMAGE);
                                AppHelper.CustomToast(ImagePreviewActivity.this, getString(R.string.image_saved));
                                finish();
                            }, 1000);
                        }

                        imageView.setImageBitmap(resource);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.setImageDrawable(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        imageView.setImageDrawable(placeholder);
                    }
                };
                GlideApp.with(this)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrlFinal))
                        .signature(new ObjectKey(ImageUrl))
                        .placeholder(R.drawable.image_holder_full_screen)
                        .error(R.drawable.image_holder_full_screen)
                        .into(bitmapImageViewTarget);
                break;
            default:

                break;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
