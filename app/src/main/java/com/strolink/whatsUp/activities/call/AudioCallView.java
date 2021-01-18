package com.strolink.whatsUp.activities.call;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.fragments.call.AudioCallFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.call.webrtc.CallAudioManager;
import com.strolink.whatsUp.helpers.call.webrtc.PeerConnectionClient;
import com.strolink.whatsUp.helpers.call.webrtc.SignallingClient;
import com.strolink.whatsUp.interfaces.call.OnCallAudioEvents;
import com.strolink.whatsUp.interfaces.call.PeerConnectionEvents;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.CallsController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

public class AudioCallView extends FragmentActivity implements OnCallAudioEvents, PeerConnectionEvents, SignallingClient.SignalingInterface {

    private static final String[] MANDATORY_PERMISSIONS = new String[]{"android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};


    private CallAudioManager audioManager = null;
    private AudioCallFragment callFragment;
    private boolean iceConnected;
    public boolean isMute = false;
    public Boolean isSpeaker = Boolean.FALSE;
    private MediaPlayer mediaPlayer;

    private PeerConnectionClient peerConnectionClient = null;


    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    private EglBase rootEglBase;

    private CountDownTimer timer;
    private String caller_id;
    private String callId;


    long autoHangupDelay = 60 * 1000;



    public boolean isUpdated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_audio_call);

        this.iceConnected = false;
        caller_id = getIntent().getStringExtra("caller_id");
        callId = getIntent().getStringExtra("callId");

        this.callFragment = new AudioCallFragment();

        this.rootEglBase = EglBase.create();

        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(0);
                finish();
                return;
            }
        }
        Intent intent = getIntent();

        this.callFragment.setArguments(intent.getExtras());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, this.callFragment);
        ft.commit();
        startCall();


        if (!intent.getBooleanExtra("isAccepted", false)) {
            this.mediaPlayer = MediaPlayer.create(this, R.raw.outgoin_call);
            this.mediaPlayer.setLooping(true);
            this.mediaPlayer.start();
        }
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(3, 20, 0);
        this.timer = new CountDownTimer(autoHangupDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                onCallHangUp(true);
            }
        };
        this.timer.start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null)
            this.mediaPlayer.stop();
        disconnect();

        super.onDestroy();
    }

    @Override
    public void onCallHangUp(boolean clicked) {



        disconnect();
    }


    @SuppressLint("CheckResult")
    private void updateUserCall() {

        try {

            AppHelper.LogCat("caller_id " + caller_id);


            CallsInfoModel callsInfoModel = CallsController.getInstance().getCallInfoById(callId);
            callsInfoModel.setDuration(callFragment.chronometer.getText().toString());

            CallsController.getInstance().updateCallInfo(callsInfoModel);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callsInfoModel.getCallId()));


            if (!caller_id.equals(PreferenceManager.getInstance().getID(this))) {

                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("callId", callId);
                    try {


                        WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage, "call");
                    } catch (MqttException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                    AppHelper.LogCat("JSONException " + e.getMessage());
                }


            } else {


                    if (!callFragment.chronometer.getText().toString().equals("00:00"))
                        CallsController.getInstance().sendUserCallToServer(this, callsInfoModel, "true");
            }

        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
        }

    }


    @Override
    public void onMute() {
        this.isMute = !this.isMute;
        this.audioManager.setMicrophoneMute(this.isMute);
    }

    @Override
    public void onSpeaker() {
        this.isSpeaker = !this.isSpeaker;
        this.audioManager.setSpeakerphoneOn(this.isSpeaker);
    }


    private void startCall() {
        SignallingClient.getInstance().initializerCall(this, getIntent().getBooleanExtra("isAccepted", false));
        createPeerConnection();
        peerConnectionClient.createPeerConnection();
        this.audioManager = CallAudioManager.create(this);
        this.audioManager.init();
    }

    private void callConnected() {
        AppHelper.LogCat("Call connected: delay=");
        if (this.peerConnectionClient == null) {
            AppHelper.LogCat("Call is connected in closed or error state");
            return;
        }
        this.timer.cancel();
        this.peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);


        if (mediaPlayer != null)
            this.mediaPlayer.stop();
        runOnUiThread(() -> callFragment.startStopWatch());
    }


    private void disconnect() {
        if (!isUpdated) {
            isUpdated = true;
            try {

                updateUserCall();
            } catch (Exception e) {
                AppHelper.LogCat("" + e.getMessage());
            }
        }
        try {
            if (mediaPlayer != null)
                this.mediaPlayer.stop();
            this.timer.cancel();


            SignallingClient.getInstance().createHangUp(caller_id);
            if (this.peerConnectionClient != null) {
                this.peerConnectionClient.close();
                this.peerConnectionClient = null;
            }

            if (this.audioManager != null) {
                this.audioManager.close();
                this.audioManager = null;
            }
            if (!this.iceConnected) {
                setResult(RESULT_OK);
                finish();
            }
            setResult(RESULT_CANCELED);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logAndToast(String msg) {
        AppHelper.LogCat(msg);
    }

    @Override
    public void onLocalDescription(final SessionDescription sdp) {

        runOnUiThread(() -> {
            if (SignallingClient.getInstance() != null) {
                AppHelper.LogCat("Sending " + sdp.type + ", delay= ms");
                SignallingClient.getInstance().createOfferCall(sdp, caller_id);
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        runOnUiThread(() -> {
            SignallingClient.getInstance().createIceCandidate(candidate, caller_id);
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

    }

    @Override
    public void onIceConnected() {

        runOnUiThread(() -> {
            AppHelper.LogCat("ICE connected, delay= ms");
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(() -> {
            AppHelper.LogCat("ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
    }

    @Override
    public void onPeerConnectionError(String description) {
    }


    @Override
    public void onRemoteHangUp(String reason) {
        showToast("Remote Peer hungup");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onCallHangUp(false);
            }
        });
    }

    /**
     * SignallingCallback - Called when remote peer sends offer
     */
    @Override
    public void onOfferReceived(final JSONObject data) {
        showToast("Received Offer");

        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                AppHelper.LogCat("Received remote SDP for non-initilized peer connection.");
                return;
            }
            if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                onTryToStart();
            }


            try {
                peerConnectionClient.setRemoteDescription(new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                if (!SignallingClient.getInstance().isInitiator) {
                    AppHelper.LogCat("Creating ANSWER...");
                    peerConnectionClient.createAnswer();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * SignallingCallback - Called when remote peer sends answer to your offer
     */

    @Override
    public void onAnswerReceived(JSONObject data) {
        showToast("Received Answer");
        try {
            peerConnectionClient.setRemoteDescription(new SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remote IceCandidate received
     */
    @Override
    public void onIceCandidateReceived(JSONObject data) {
        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                AppHelper.LogCat("Received ICE candidate for non-initilized peer connection.");
            } else {
                try {
                    peerConnectionClient.addRemoteIceCandidate(new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTryToStart() {
        AppHelper.LogCat("onTryToStart " + SignallingClient.getInstance().isInitiator);
        AppHelper.LogCat("onTryToStart " + SignallingClient.getInstance().isChannelReady);
        AppHelper.LogCat("onTryToStart " + SignallingClient.getInstance().isStarted);
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isStarted/* && peerConnectionClient.localAudioTrack != null*/ && SignallingClient.getInstance().isChannelReady) {
                SignallingClient.getInstance().isStarted = true;
                if (SignallingClient.getInstance().isInitiator) {
                    this.peerConnectionClient.createOffer();
                }
            }
        });
    }

    private void createPeerConnection() {
        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient(getApplicationContext(), rootEglBase, false, 0, 0, AudioCallView.this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        this.peerConnectionClient.createPeerConnectionFactory(options);
        this.peerConnectionClient.createPeerConnection(null, null, null);

    }


    public void showToast(final String msg) {
        if (AppConstants.DEBUGGING_MODE)
            runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }


    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }
}
