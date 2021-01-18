package com.strolink.whatsUp.fragments.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.interfaces.call.OnCallVideoEvents;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class VideoCallFragment extends Fragment {
    private View mView;
    private long countUp;
    public String time = null;

    private OnCallVideoEvents callEvents;

    boolean isFrontCamera = false;
    boolean isDisabled = false;

    private UsersModel currentUser;


    private String currentUserId;
    private String currentUserName;

    private String currentUserImage;
    private String caller_id;

    @BindView(R.id.camera)
    AppCompatImageView camera;


    @BindView(R.id.cancelCall)
    AppCompatImageView cancelCall;


    @BindView(R.id.disable_video)
    AppCompatImageView disableVideo;

    @BindView(R.id.callerName)
    TextView callerName;

    @BindView(R.id.userImage)
    AppCompatImageView userImage;

    public Chronometer chronometer;

    @BindView(R.id.stopWatch)
    TextView stopWatch;

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragement_video_call, container, false);
        ButterKnife.bind(this, mView);
        chronometer = mView.findViewById(R.id.chrono);
        caller_id = getActivity().getIntent().getStringExtra("caller_id");


        UsersModel user = UsersController.getInstance().getUserById(caller_id);

        if (user != null) {
            currentUserId = user.get_id();
            if (user.getUsername() != null)
                currentUserName = user.getUsername();
            else
                currentUserName = user.getPhone();

            currentUserImage = user.getImage();
        }

        if (caller_id.equals(currentUserId)) {
            callerName.setText(currentUserName);

            try {


                if (currentUserImage != null) {
                    BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            userImage.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            userImage.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            userImage.setImageDrawable(placeholder);
                        }


                    };

                    GlideApp.with(getActivity())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                            .signature(new ObjectKey(currentUserImage))
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.bg_circle_image_holder)
                            .error(R.drawable.bg_circle_image_holder)
                            .into(target);

                } else {
                    userImage.setImageDrawable(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_image_holder));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            this.currentUser = UsersController.getInstance().getUserById(caller_id);
            if (this.currentUser == null) {
                callerName.setText(R.string.unknown);
            } else if (this.currentUser.getUsername() != null) {
                callerName.setText(this.currentUser.getUsername());
            } else {
                callerName.setText(currentUser.getPhone());
            }

            currentUserImage = currentUser.getImage();
            if (currentUserImage != null) {
                BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        userImage.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        userImage.setImageDrawable(placeholder);
                    }


                };

                GlideApp.with(getActivity())
                        .asBitmap()

                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                        .signature(new ObjectKey(currentUserImage))
                        .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.bg_circle_image_holder)
                        .error(R.drawable.bg_circle_image_holder)
                        .into(target);

            } else {
                userImage.setImageDrawable(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_image_holder));
            }


        }
        this.cancelCall.setOnClickListener(v -> callEvents.onCallHangUp(true));
        this.camera.setOnClickListener(v -> {

            callEvents.onCameraSwitch();
            isFrontCamera = !isFrontCamera;
        });
        this.disableVideo.setOnClickListener(v -> {
            callEvents.onVideoDisabled();
            isDisabled = !isDisabled;
            if (isDisabled) {
                disableVideo.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_videocam_off_white_24dp));
            } else {
                disableVideo.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_videocam_white_24dp));
            }
        });
        return mView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callEvents = (OnCallVideoEvents) activity;
    }

    public void startStopWatch() {
        AppHelper.LogCat("startStopWatch");
        stopWatch.setVisibility(View.GONE);
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }
}
