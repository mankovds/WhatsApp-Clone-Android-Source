package com.strolink.whatsUp.activities.welcome;

import android.Manifest;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.fragments.bottomSheets.BottomSheetEditProfile;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.images.ImageUtils;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.ProfileResponse;
import com.strolink.whatsUp.presenters.users.EditProfilePresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH;

;

/**
 * Created by Abderrahim El imame on 4/1/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CompleteRegistrationActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.username_input)
    AppCompatEditText usernameInput;

    @BindView(R.id.userAvatar)
    AppCompatImageView userAvatar;

    @BindView(R.id.addAvatar)
    FloatingActionButton addAvatar;

    @BindView(R.id.progress_bar_edit_profile)
    ProgressBar progressBar;

    @BindView(R.id.completeRegistration)
    TextView completeRegistration;


    @BindView(R.id.registerBtn)
    TextView registerBtn;

    @BindView(R.id.completeRegistrationLayout)
    NestedScrollView mView;
    private CompositeDisposable mDisposable;
    private String ImageUrlFinal;
    private String PicturePath;
    private EditProfilePresenter mEditProfilePresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_registration_activity);
        ButterKnife.bind(this);

        if (PreferenceManager.getInstance().getToken(this) != null) {

            WhatsCloneApplication.getInstance().preInitMqtt();
            initializerApplication();
        }
        mDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);

        mEditProfilePresenter = new EditProfilePresenter(this);
        mEditProfilePresenter.onCreate();
        registerBtn.setOnClickListener(this);
        addAvatar.setOnClickListener(v -> {
            BottomSheetEditProfile bottomSheetEditProfile = new BottomSheetEditProfile();
            bottomSheetEditProfile.show(getSupportFragmentManager(), bottomSheetEditProfile.getTag());
        });

    }

    public void setInfo(String imageUrl, String name) {
        usernameInput.setText(name);
        setImage(imageUrl);
    }


    public void initializerApplication() {


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {

            WorkJobsManager.getInstance().initializerApplicationService();
            WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
            WorkJobsManager.getInstance().sendUserMessagesToServer();
            WorkJobsManager.getInstance().sendUserStoriesToServer();
            WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
            WorkJobsManager.getInstance().sendDeletedStoryToServer();

        }

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registerBtn:


                Permissions.with(this)
                        .request(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .ifNecessary()
                        .withRationaleDialog(getString(R.string.app_needs_access_to_your_contacts_and_media_in_order_to_connect_with_friends), R.drawable.ic_contacts_white_24dp, R.drawable.ic_folder_white_24dp)
                        .withPermanentDenialDialog(getString(R.string.app_needs_access_to_your_contacts_and_media_in_order_to_connect_with_friends))
                        .onAllGranted(this::complete)
                        .onAnyDenied(() -> {
                            Toast.makeText(this, getString(R.string.contact_media_permission_required), Toast.LENGTH_LONG).show();

                        })
                        .execute();


                break;

        }
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case EVENT_BUS_IMAGE_PROFILE_PATH:
                progressBar.setVisibility(View.VISIBLE);
                PicturePath = String.valueOf(pusher.getData());
                if (PicturePath != null) {
                    try {
                        new UploadFileToServer().execute();
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                        AppHelper.CustomToast(this, getString(R.string.oops_something));
                    }

                }
                break;

        }

    }

    private void complete() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {

            AppHelper.CustomToast(this, getString(R.string.username_required));

    /*        if (ImageUrlFinal != null) {
                mEditProfilePresenter.editCurrentImage(ImageUrlFinal, true);
            } else {

                PreferenceManager.getInstance().setIsNeedInfo(this, false);
              *//*  if (!AppHelper.isServiceRunning(this, MainService.class)
                        && PreferenceManager.getInstance().getToken(this) != null
                        && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {

                }*//*
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent);
                this.finish();
                AnimationsUtil.setTransitionAnimation(this);
            }*/
        } else {
            if (ImageUrlFinal != null) {
                mEditProfilePresenter.editCurrentName(username, true, false);
                mEditProfilePresenter.editCurrentImage(ImageUrlFinal, true);

            } else {
                mEditProfilePresenter.editCurrentName(username, true, true);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditProfilePresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mDisposable != null) mDisposable.dispose();
    }

    private void setImage(String ImageUrl) {
        ImageUrlFinal = ImageUrl;
        DrawableImageViewTarget target = new DrawableImageViewTarget(userAvatar) {


            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                userAvatar.setImageDrawable(resource);

            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                userAvatar.setImageDrawable(errorDrawable);
            }


            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                userAvatar.setImageDrawable(placeholder);
            }
        };

        GlideApp.with(this)
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + PreferenceManager.getInstance().getID(this) + "/" + ImageUrl))
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.image_holder_ur_circle)
                .error(R.drawable.image_holder_ur_circle)
                .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                .into(target);
    }

    /**
     * Uploading the image  to server
     */
    private class UploadFileToServer extends AsyncTask<Void, Integer, ProfileResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AppHelper.LogCat("onPreExecute  image ");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            AppHelper.LogCat("progress image " + (int) (progress[0]));
        }

        @Override
        protected ProfileResponse doInBackground(Void... params) {
            return uploadFile();
        }


        private ProfileResponse uploadFile() {

            RequestBody requestFile;
            final ProfileResponse profileResponse = null;
            if (PicturePath != null) {
                byte[] imageByte = ImageUtils.compressImage(PicturePath);
                // create RequestBody instance from file
                requestFile = RequestBody.create( MediaType.parse("image/*"),imageByte);
            } else {
                requestFile = null;
            }
            if (requestFile == null) {
                AppHelper.CustomToast(CompleteRegistrationActivity.this, getString(R.string.oops_something));
            } else {
                File file = new File(PicturePath);
                mDisposable.add(APIHelper.initializeUploadFiles().uploadUserImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile), PreferenceManager.getInstance().getID(CompleteRegistrationActivity.this))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (response.isSuccess()) {

                                if (PicturePath != null) {
                                    file.delete();
                                }


                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    AppHelper.CustomToast(CompleteRegistrationActivity.this, response.getMessage());
                                    setImage(response.getFilename());
                                });
                            } else {
                                AppHelper.CustomToast(CompleteRegistrationActivity.this, response.getMessage());
                            }
                        }, throwable -> {
                            AppHelper.CustomToast(CompleteRegistrationActivity.this, getString(R.string.failed_upload_image));
                            AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                        }))
                ;
            }

            return profileResponse;
        }

        @Override
        protected void onPostExecute(ProfileResponse response) {
            super.onPostExecute(response);
            // AppHelper.LogCat("Response from server: " + response);

        }


    }

}
