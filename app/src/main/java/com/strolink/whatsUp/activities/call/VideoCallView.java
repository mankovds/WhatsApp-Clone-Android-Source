package com.strolink.whatsUp.activities.call;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.fragments.call.VideoCallFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.call.CallingApi;
import com.strolink.whatsUp.helpers.call.webrtc.CallAudioManager;
import com.strolink.whatsUp.helpers.call.webrtc.PeerConnectionClient;
import com.strolink.whatsUp.helpers.call.webrtc.SignallingClient;
import com.strolink.whatsUp.interfaces.call.OnCallVideoEvents;
import com.strolink.whatsUp.interfaces.call.PeerConnectionEvents;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.CallsController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;


public class VideoCallView extends FragmentActivity implements OnCallVideoEvents, PeerConnectionEvents, SignallingClient.SignalingInterface {
    private static final String[] MANDATORY_PERMISSIONS = new String[]{"android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private CallAudioManager audioManager = null;
    private boolean callControlFragmentVisible = true;
    private VideoCallFragment callFragment;
    private boolean iceConnected;

    public boolean isUpdated = false;
    public boolean isVideoAvailable = true;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private boolean isSwappedFeeds;
    private MediaPlayer mediaPlayer;
    private PeerConnectionClient peerConnectionClient = null;


    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    private EglBase rootEglBase;
    private CountDownTimer timer;
    private String caller_id;
    private String callId;

    long autoHangupDelay = 60 * 1000;

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();


    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                AppHelper.LogCat("Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EventBus.getDefault().register(this);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_video_call);
        this.iceConnected = false;

        this.localRender = findViewById(R.id.local_video_view);
        this.remoteRender = findViewById(R.id.remote_video_view);

        caller_id = getIntent().getStringExtra("caller_id");
        callId = getIntent().getStringExtra("callId");
        this.callFragment = new VideoCallFragment();
        this.remoteRender.setOnClickListener(v -> toggleCallControlFragmentVisibility());

        // Swap feeds on local view click.
        localRender.setOnClickListener(view -> setSwappedFeeds(!isSwappedFeeds));


        this.rootEglBase = EglBase.create();

        // Create video renderers.
        localRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);


        localRender.setZOrderMediaOverlay(true);
        localRender.setEnableHardwareScaler(true);
        remoteRender.setEnableHardwareScaler(false);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true);


        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                AppHelper.LogCat("Permission " + permission + " is not granted");
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
            this.mediaPlayer.setVolume(0.2f, 0.2f);
            this.mediaPlayer.start();
        }

        this.timer = new CountDownTimer(autoHangupDelay, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                onCallHangUp(true);
            }
        };
        this.timer.start();


    }


    private void setSwappedFeeds(boolean isSwappedFeeds) {
        AppHelper.LogCat(AppConstants.TAG + "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? remoteRender : localRender);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? localRender : remoteRender);
        remoteRender.setMirror(isSwappedFeeds);
        localRender.setMirror(!isSwappedFeeds);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.peerConnectionClient != null) {
            this.peerConnectionClient.stopVideoSource();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.peerConnectionClient != null) {
            this.peerConnectionClient.startVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null)
            this.mediaPlayer.stop();
        disconnect();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCallHangUp(boolean clicked) {

        disconnect();
    }


    @SuppressLint("CheckResult")
    private void updateUserCall() {
        CallsInfoModel callsInfoModel = CallsController.getInstance().getCallInfoById(callId);


        callsInfoModel.setDuration(callFragment.chronometer.getText().toString());
        CallsController.getInstance().updateCallInfo(callsInfoModel);
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callsInfoModel.getCallId()));

        if (!caller_id.equals(PreferenceManager.getInstance().getID(this))) {

            JSONObject updateMessage = new JSONObject();
            try {
                updateMessage.put("callId", callId);
                try {


                    WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage,"call");
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

    }

    @Override
    public void onCameraSwitch() {
        if (this.peerConnectionClient != null) {
            this.peerConnectionClient.switchCamera();
        }
    }

    @Override
    public void onVideoDisabled() {
        this.isVideoAvailable = !this.isVideoAvailable;
        if (this.isVideoAvailable) {
            if (this.localRender != null) {
                this.localRender.setBackgroundColor(Color.parseColor("#00000000"));
                CallingApi.sendCallEvent(this, "startVideo", getIntent().getStringExtra("caller_id"), true);
            }
            return;
        }
        this.localRender.setBackgroundColor(Color.parseColor("#000000"));
        CallingApi.sendCallEvent(this, "stopVideo", getIntent().getStringExtra("caller_id"), true);
    }

    private void toggleCallControlFragmentVisibility() {
        if (this.iceConnected && this.callFragment.isAdded()) {
            this.callControlFragmentVisible = !this.callControlFragmentVisible;
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (this.callControlFragmentVisible) {
                ft.show(this.callFragment);
            } else {
                ft.hide(this.callFragment);
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }


    private void startCall() {
        SignallingClient.getInstance().initializerCall(this, getIntent().getBooleanExtra("isAccepted", false));
        createPeerConnection();
        AppHelper.LogCat("init_call " + SignallingClient.getInstance().isInitiator);
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

        if (mediaPlayer != null)
            this.mediaPlayer.stop();
        this.peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false);

        runOnUiThread(() -> callFragment.startStopWatch());

        this.audioManager.setSpeakerphoneOn(true);

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

            remoteProxyRenderer.setTarget(null);
            localProxyVideoSink.setTarget(null);

            if (this.peerConnectionClient != null) {
                this.peerConnectionClient.close();
                this.peerConnectionClient = null;
            }
            if (this.localRender != null) {
                this.localRender.release();
                this.localRender = null;
            }
            if (this.remoteRender != null) {
                this.remoteRender.release();
                this.remoteRender = null;
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


    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;

        if (useCamera2()) {
            Logging.e(AppConstants.TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.e(AppConstants.TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        if (videoCapturer == null) {
            AppHelper.LogCat("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        AppHelper.LogCat("Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                AppHelper.LogCat("Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        AppHelper.LogCat("Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                AppHelper.LogCat("Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }


    @Override
    public void onRemoteHangUp(String reason) {

        showToast("Remote Peer hungup");
        runOnUiThread(() -> onCallHangUp(false));

    }

    /**
     * SignallingCallback - Called when remote peer sends offer
     */
    @Override
    public void onOfferReceived(final JSONObject data) {
        showToast("Received Offer");

        runOnUiThread(() -> {
            if (peerConnectionClient == null) {
                AppHelper.LogCat("Received remote SDP for non-initialized peer connection.");
                return;
            }

            if (!SignallingClient.getInstance().isInitiator && !SignallingClient.getInstance().isStarted) {
                AppHelper.LogCat("Offer not init not started");
                onTryToStart();
            }


            try {
                peerConnectionClient.setRemoteDescription(new SessionDescription(SessionDescription.Type.OFFER, data.getString("sdp")));
                if (!SignallingClient.getInstance().isInitiator) {
                    AppHelper.LogCat("Creating ANSWER...");
                    peerConnectionClient.createAnswer();
                } else {
                    AppHelper.LogCat("Received remote SDP for non-initialized.");
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
        runOnUiThread(() -> {
            if (!SignallingClient.getInstance().isStarted && peerConnectionClient.localVideoTrack != null && SignallingClient.getInstance().isChannelReady) {
                SignallingClient.getInstance().isStarted = true;
                if (SignallingClient.getInstance().isInitiator) {
                    this.peerConnectionClient.createOffer();
                }
            }
        });
    }

    private void createPeerConnection() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient(getApplicationContext(), rootEglBase, true, width, height, VideoCallView.this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        this.peerConnectionClient.createPeerConnectionFactory(options);
        VideoCapturer videoCapturer = createVideoCapturer();
        this.peerConnectionClient.createPeerConnection(localProxyVideoSink, remoteProxyRenderer, videoCapturer);

    }


    public void showToast(final String msg) {
        if (AppConstants.DEBUGGING_MODE)
            runOnUiThread(() -> Toast.makeText(VideoCallView.this, msg, Toast.LENGTH_SHORT).show());
    }

    /**
     * method of EventBus
     *
     * @param object this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JSONObject object) {
        try {
            AppHelper.LogCat("jawb " + object.toString());
            if (object.getString("eventName").contentEquals("CallEventChange")) {
                String status = object.getString("status");
                if (status.contentEquals("startVideo")) {
                    runOnUiThread(() -> remoteRender.setBackgroundColor(Color.parseColor("#00000000")));
                } else if (status.contentEquals("stopVideo")) {
                    runOnUiThread(() -> remoteRender.setBackgroundColor(Color.parseColor("#4e4e4e")));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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



