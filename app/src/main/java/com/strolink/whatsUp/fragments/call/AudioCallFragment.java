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
import com.strolink.whatsUp.interfaces.call.OnCallAudioEvents;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class AudioCallFragment extends Fragment {


    private OnCallAudioEvents callEvents;
    boolean isMute = false;
    boolean isSpeaker = false;
    private String currentUserId;
    private String currentUserName;
    private String currentUserImage;


    String caller_id;


    public Chronometer chronometer;

    @BindView(R.id.stopWatch)
    TextView stopWatch;

    @BindView(R.id.thumbnail)
    AppCompatImageView userImage;

    @BindView(R.id.diconnect_btn)
    AppCompatImageView cancelButton;

    @BindView(R.id.mute)
    AppCompatImageView mute;

    @BindView(R.id.speaker)
    AppCompatImageView speaker;

    @BindView(R.id.callerName)
    TextView tvCallerName;


    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fargment_voice_call, container, false);
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

            tvCallerName.setText(currentUserName);
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
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
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

            UsersModel currentUser = UsersController.getInstance().getUserById(caller_id);
            if (currentUser == null) {
                tvCallerName.setText(R.string.unknown);
            } else if (currentUser.getUsername() != null) {
                tvCallerName.setText(currentUser.getUsername());
            } else {
                tvCallerName.setText(currentUser.getPhone());
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
        cancelButton.setOnClickListener(v -> callEvents.onCallHangUp(true));
        mute.setOnClickListener(v -> {
            callEvents.onMute();
            isMute = !isMute;
            if (isMute) {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_mic_off_white_24dp));
            } else {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_mic_white_24dp));
            }
        });
        speaker.setOnClickListener(v -> {
            callEvents.onSpeaker();
            isSpeaker = !isSpeaker;
            if (isSpeaker) {
                speaker.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_volume_off_white_24dp));
            } else {
                speaker.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_volume_up_white_24dp));
            }
        });


        return mView;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callEvents = (OnCallAudioEvents) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startStopWatch() {

        stopWatch.setVisibility(View.GONE);
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        speaker.performClick();

    }
}
