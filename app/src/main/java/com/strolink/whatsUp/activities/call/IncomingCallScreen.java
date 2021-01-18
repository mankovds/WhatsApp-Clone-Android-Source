package com.strolink.whatsUp.activities.call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.call.CallingApi;
import com.strolink.whatsUp.helpers.call.webrtc.SignallingClient;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.CallsController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.views.CallAnswerDeclineButton;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class IncomingCallScreen extends FragmentActivity implements CallAnswerDeclineButton.AnswerDeclineListener, SignallingClient.SignalingInterface {


    @BindView(R.id.tvAudioVideoCall)
    TextView tvAudioVideoCall;

    @BindView(R.id.userImage)
    AppCompatImageView userImage;


    @BindView(R.id.callerName)
    TextView tvCallerName;


    @BindView(R.id.answer_decline_button)
    CallAnswerDeclineButton callAnswerDeclineButton;
    boolean isVideoCall;
    String callType;

    String callId;
    String caller_id;

    private boolean isAttendButtonIsClicked = false;
    private boolean isAccepted = false;
    private boolean isDeclined = false;

    Ringtone ringtone;
    CountDownTimer timer;

    long autoRejectDelay = 60 * 1000;

    private UsersModel currentUser;

    private String currentUserImage;
    private String lastID;
    private boolean isSaved = false;

    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming);
        ButterKnife.bind(this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        callAnswerDeclineButton.setAnswerDeclineListener(this);
        callAnswerDeclineButton.setVisibility(View.VISIBLE);
        callAnswerDeclineButton.startRingingAnimation();


        if (getIntent().getExtras() != null) {

            this.callId = getIntent().getExtras().getString("callId", "");
            this.caller_id = getIntent().getExtras().getString("call_from", "");
            this.callType = getIntent().getExtras().getString("callType", AppConstants.VOICE_CALL);
        }
        isVideoCall = callType.equals(AppConstants.VIDEO_CALL);

        AppHelper.LogCat("caller_id " + caller_id);
        this.currentUser = UsersController.getInstance().getUserById(caller_id);
        AppHelper.LogCat("currentUser " + currentUser.get_id());

        if (!(this.currentUser == null || this.currentUser.getImage() == null)) {
            try {

                currentUserImage = currentUser.getImage();
                String currentUserId = currentUser.get_id();

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

                    GlideApp.with(this)
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                            .signature(new ObjectKey(currentUserImage))
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.bg_circle_image_holder)
                            .error(R.drawable.bg_circle_image_holder)
                            .into(target);

                } else {
                    userImage.setImageDrawable(AppHelper.getDrawable(this, R.drawable.bg_circle_image_holder));
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        try {
            this.ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(1));
            this.ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setupView();
        this.timer = new CountDownTimer(autoRejectDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //  Toast.makeText(IncomingCallScreen.this, "Timeout", Toast.LENGTH_LONG).show();
                if (!IncomingCallScreen.this.isAttendButtonIsClicked) {
                    //  Toast.makeText(IncomingCallScreen.this, "Timeout", Toast.LENGTH_LONG).show();
                    IncomingCallScreen.this.finish();
                }
            }
        };
        this.timer.start();
        addNewCall();
    }

    private void setupView() {
        if (this.currentUser == null) {
            this.tvCallerName.setText(R.string.unknown);
        } else if (this.currentUser.getUsername() != null) {
            this.tvCallerName.setText(currentUser.getUsername());
        } else {
            this.tvCallerName.setText(currentUser.getPhone());
        }
        if (this.callType.contentEquals(AppConstants.VOICE_CALL)) {
            this.tvAudioVideoCall.setText(getResources().getString(R.string.voice_call));
        } else {
            this.tvAudioVideoCall.setText(getResources().getString(R.string.video_call));
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.timer.cancel();
            this.ringtone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void addNewCall() {
        try {
            saveToDataBase();

        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
        }
    }


    @SuppressLint("CheckResult")
    public void saveToDataBase() {
        if (currentUser == null) return;
        String phone = currentUser.getPhone();
        String username = currentUser.getUsername();

        DateTime current = new DateTime();
        String callTime = String.valueOf(current);

        String historyCallId = CallsController.getInstance().getHistoryCallIdReceived(currentUser.get_id(), PreferenceManager.getInstance().getID(this), isVideoCall);


        if (historyCallId == null) {
            UsersModel contactsModel1 = UsersController.getInstance().getUserById(caller_id);

            String lastID = DbBackupRestore.getCallLastId();
            CallsModel callsModel = new CallsModel();
            callsModel.setC_id(lastID);
            if (isVideoCall)
                callsModel.setType(AppConstants.VIDEO_CALL);
            else
                callsModel.setType(AppConstants.VOICE_CALL);
            callsModel.setUsersModel(contactsModel1);
            callsModel.setCounter(1);
            callsModel.setFrom(contactsModel1.get_id());
            callsModel.setTo(PreferenceManager.getInstance().getID(this));
            callsModel.setDuration("00:00");
            callsModel.setDate(callTime);
            callsModel.setReceived(true);

            CallsInfoModel callsInfoModel = new CallsInfoModel();


            callsInfoModel.setCi_id(callId);
            if (isVideoCall)
                callsInfoModel.setType(AppConstants.VIDEO_CALL);
            else
                callsInfoModel.setType(AppConstants.VOICE_CALL);
            callsInfoModel.setUsersModel(contactsModel1);
            callsInfoModel.setCallId(lastID);
            callsInfoModel.setFrom(contactsModel1.get_id());
            callsInfoModel.setTo(PreferenceManager.getInstance().getID(this));
            callsInfoModel.setDuration("00:00");
            callsInfoModel.setDate(callTime);
            callsInfoModel.setReceived(true);


            CallsController.getInstance().insertCallInfo(callsInfoModel);
            CallsController.getInstance().insertCall(callsModel);
            this.lastID = lastID;
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, this.lastID));


        } else {
            UsersModel contactsModel1 = UsersController.getInstance().getUserById(caller_id);

            int callCounter;
            CallsModel callsModel = CallsController.getInstance().getCallById(historyCallId);

            callCounter = callsModel.getCounter();
            callCounter++;
            callsModel.setDate(callTime);
            callsModel.setCounter(callCounter);
            callsModel.setDuration("00:00");
            CallsInfoModel callsInfoModel = new CallsInfoModel();

            callsInfoModel.setCi_id(callId);
            if (isVideoCall)
                callsInfoModel.setType(AppConstants.VIDEO_CALL);
            else
                callsInfoModel.setType(AppConstants.VOICE_CALL);
            callsInfoModel.setUsersModel(contactsModel1);

            callsInfoModel.setCallId(callsModel.getC_id());
            callsInfoModel.setFrom(contactsModel1.get_id());
            callsInfoModel.setTo(PreferenceManager.getInstance().getID(this));
            callsInfoModel.setDuration("00:00");
            callsInfoModel.setDate(callTime);
            callsInfoModel.setReceived(true);

            CallsController.getInstance().insertCallInfo(callsInfoModel);
            CallsController.getInstance().updateCall(callsModel);

            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
        }


    }


    @Override
    public void onAnswered() {
        AppHelper.LogCat("onAnswered");
        Permissions.with(this)
                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.to_answer_the_call_from_s_give_app_access_to_your_microphone, currentUser.getUsername()),
                        R.drawable.ic_mic_white_24dp, R.drawable.ic_videocam_white_24dp)
                .withPermanentDenialDialog(getString(R.string.app_requires_microphone_and_camera_permissions_in_order_to_make_or_receive_calls))
                .onAllGranted(() -> {
                    this.isAttendButtonIsClicked = true;
                    if (!isAccepted) {
                        this.isAccepted = true;
                        SignallingClient.getInstance().acceptCall(caller_id);

                        CallingApi.startCall(this, this.callType, true, caller_id, callId);
                    }
                    finish();
                })
                .onAnyDenied(() -> {
                    if (!isDeclined) {
                        this.isDeclined = true;
                        SignallingClient.getInstance().createHangUp(caller_id);
                    }
                    finish();
                })
                .execute();
    }

    @Override
    public void onDeclined() {
        AppHelper.LogCat("onDeclined");
        SignallingClient.getInstance().createHangUp(caller_id);
        finish();
    }

    @Override
    public void onRemoteHangUp(String reason) {
        finish();
    }

    @Override
    public void onOfferReceived(JSONObject data) {

    }

    @Override
    public void onAnswerReceived(JSONObject data) {

    }

    @Override
    public void onIceCandidateReceived(JSONObject data) {

    }

    @Override
    public void onTryToStart() {

    }

}
