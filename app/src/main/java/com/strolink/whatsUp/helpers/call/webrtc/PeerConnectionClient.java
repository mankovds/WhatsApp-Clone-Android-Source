package com.strolink.whatsUp.helpers.call.webrtc;

import android.content.Context;

import androidx.annotation.Nullable;

import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.call.PeerConnectionEvents;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.BundlePolicy;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.KeyType;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.RTCConfiguration;
import org.webrtc.PeerConnection.RtcpMuxPolicy;
import org.webrtc.PeerConnection.TcpCandidatePolicy;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerConnectionClient {
    private MediaConstraints audioConstraints;
    private PeerConnectionEvents events;

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PeerConnectionFactory factory;
    private boolean isError;
    private boolean isInitiator;

    private VideoSink localRender;
    private SessionDescription localSdp;
    public VideoTrack localVideoTrack;
    private PeerConnection peerConnection;
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private VideoSink remoteRender;
    private VideoTrack remoteVideoTrack;
    private boolean renderVideo;
    private MediaConstraints sdpMediaConstraints;
    private final CustomSdpObserver customSdpObserver = new CustomSdpObserver();
    private Timer statsTimer;
    private boolean videoCallEnabled;
    private VideoCapturer videoCapturer;
    private MediaConstraints videoConstraints;
    private VideoSource videoSource;
    @Nullable
    private AudioSource audioSource;
    private boolean videoSourceStopped;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio = true;
    @Nullable
    public AudioTrack localAudioTrack;

    private EglBase renderEGLContext;
    private Context context;

    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;

    private static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";


    public PeerConnectionClient(final Context context, EglBase eglBase, boolean videoCallEnabled, int videoWidth, int videoHeight, PeerConnectionEvents events) {
        if (videoCallEnabled) {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
        }
        this.events = events;
        this.videoCallEnabled = videoCallEnabled;
        this.factory = null;
        this.peerConnection = null;
        this.videoSourceStopped = false;
        this.isError = false;
        this.queuedRemoteCandidates = null;
        this.localSdp = null;
        this.videoCapturer = null;
        this.renderVideo = videoCallEnabled;
        this.localVideoTrack = null;
        this.remoteVideoTrack = null;
        this.statsTimer = new Timer();
        this.renderEGLContext = eglBase;
        this.context = context;
        executor.execute(() -> {
            AppHelper.LogCat("Initialize WebRTC. Field trials: " );
            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                            .setFieldTrials("WebRTC-IntelVP8/Enabled")
                            .setEnableInternalTracer(true)
                            .createInitializationOptions());
        });
    }


    /**
     * This function should only be called once.
     */
    public void createPeerConnectionFactory(PeerConnectionFactory.Options options) {
        if (factory != null) {
            throw new IllegalStateException("PeerConnectionFactory has already been constructed");
        }
        executor.execute(() -> createPeerConnectionFactoryInternal(options));
    }


    public void createPeerConnection(VideoSink localRender, VideoSink remoteRender, final VideoCapturer videoCapturer) {
        this.localRender = localRender;
        this.remoteRender = remoteRender;
        this.videoCapturer = videoCapturer;
    }

    public void createPeerConnection() {
        executor.execute(() -> {
            createMediaConstraintsInternal();
            createPeerConnectionInternal();
        });
    }

    private List<PeerConnection.IceServer> getIceServers() {

        List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();
        List<IceServer> iceServers = new ArrayList<IceServer>();
        iceServers.add(new IceServer("stun:stun.l.google.com:19302", null, null));
        iceServers.add(new IceServer("turn:champier02.ibername.com:3478?transport=tcp", "bencherif", "Cm35sBnpD82RK"));
        iceServers.add(new IceServer("stun:champier02.ibername.com", "bencherif", "Cm35sBnpD82RK"));



        for (IceServer iceServer : iceServers) {
            if (iceServer.credential == null) {
                PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url).createIceServer();
                peerIceServers.add(peerIceServer);
            } else {
                PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer.url)
                        .setUsername(iceServer.username)
                        .setPassword(iceServer.credential)
                        .createIceServer();
                peerIceServers.add(peerIceServer);
            }
        }

        return peerIceServers;

    }

    public void close() {
        executor.execute(this::closeInternal);
    }

    private void createPeerConnectionFactoryInternal(PeerConnectionFactory.Options options) {


        this.isError = false;

        final AudioDeviceModule adm = JavaAudioDeviceModule.builder(context)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                //  .setSamplesReadyCallback(getUserMediaImpl.inputSamplesInterceptor)
                .createAudioDeviceModule();

        // Create peer connection factory.
        if (options != null) {
            AppHelper.LogCat("Factory networkIgnoreMask option: " + options.networkIgnoreMask);
        }

        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                renderEGLContext.getEglBaseContext(), true, true);
        decoderFactory = new DefaultVideoDecoderFactory(renderEGLContext.getEglBaseContext());

        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        AppHelper.LogCat("Peer connection factory created.");
    }


    private void createMediaConstraintsInternal() {

        if (this.videoCallEnabled) {
            this.videoConstraints = new MediaConstraints();

            this.videoConstraints.mandatory.add(new KeyValuePair("minWidth", Integer.toString(videoWidth)));
            this.videoConstraints.mandatory.add(new KeyValuePair("maxWidth", Integer.toString(videoWidth)));
            this.videoConstraints.mandatory.add(new KeyValuePair("minHeight", Integer.toString(videoHeight)));
            this.videoConstraints.mandatory.add(new KeyValuePair("maxHeight", Integer.toString(videoHeight)));
            videoFps = 32;
            videoFps = Math.min(videoFps, 30);
            this.videoConstraints.mandatory.add(new KeyValuePair("minFrameRate", Integer.toString(videoFps)));
            this.videoConstraints.mandatory.add(new KeyValuePair("maxFrameRate", Integer.toString(videoFps)));
        }
        this.audioConstraints = new MediaConstraints();

        this.sdpMediaConstraints = new MediaConstraints();
        this.sdpMediaConstraints.mandatory.add(new KeyValuePair("OfferToReceiveAudio", "true"));
        if (this.videoCallEnabled) {
            this.sdpMediaConstraints.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "true"));
        } else {
            this.sdpMediaConstraints.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "false"));
        }
    }

    private void createPeerConnectionInternal() {
        if (this.factory == null || this.isError) {
            AppHelper.LogCat("Peerconnection factory is not created");
            return;
        }
        if (this.videoConstraints != null) {
            this.queuedRemoteCandidates = new LinkedList<>();
        } else {
            this.queuedRemoteCandidates = new LinkedList<>();
        }
        AppHelper.LogCat("Create peer connection.");

        RTCConfiguration rtcConfig = new RTCConfiguration(getIceServers());
        rtcConfig.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = KeyType.ECDSA;
        this.peerConnection = this.factory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver());
        this.isInitiator = false;
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        peerConnection.addTrack(createAudioTrack(), mediaStreamLabels);
        AppHelper.LogCat("isVideoCallEnabled " + isVideoCallEnabled());

        if (isVideoCallEnabled()) {

            peerConnection.addTrack(createVideoTrack(videoCapturer), mediaStreamLabels);
            // We can add the renderers right away because we don't need to wait for an
            // answer to get the remote track.
        }


    }

    private boolean isVideoCallEnabled() {
        return videoCallEnabled && videoCapturer != null;
    }

    @Nullable
    private AudioTrack createAudioTrack() {
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    @Nullable
    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", renderEGLContext.getEglBaseContext());
        videoSource = factory.createVideoSource(capturer.isScreencast());
        capturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
        capturer.startCapture(videoWidth, videoHeight, videoFps);
        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addSink(localRender);
        return localVideoTrack;
    }



    public void setAudioEnabled(final boolean enable) {
        executor.execute(() -> {
            enableAudio = enable;
            if (localAudioTrack != null) {
                localAudioTrack.setEnabled(enableAudio);
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(() -> {
            renderVideo = enable;
            if (localVideoTrack != null) {
                localVideoTrack.setEnabled(renderVideo);
            }
            if (remoteVideoTrack != null) {
                remoteVideoTrack.setEnabled(renderVideo);
            }
        });
    }

    private void closeInternal() {
        this.statsTimer.cancel();

        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
        }
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoSourceStopped = true;
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }

        localRender = null;
        remoteRender = null;
        if (this.factory != null) {
            this.factory.dispose();
            this.factory = null;
        }

        renderEGLContext.release();
        AppHelper.LogCat("Closing peer connection done.");

        this.events.onPeerConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }

    private void getStats() {
        if (this.peerConnection != null && !this.isError && !this.peerConnection.getStats(statsReports -> events.onPeerConnectionStatsReady(statsReports), null)) {
            AppHelper.LogCat("getStats() returns false!");
        }
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        if (enable) {
            try {
                this.statsTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executor.execute(() -> getStats());
                    }
                }, 0, (long) periodMs);
                return;
            } catch (Exception e) {
                AppHelper.LogCat("Can not schedule statistics timer" + e);
                return;
            }
        }
        this.statsTimer.cancel();
    }

    public void createOffer() {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                isInitiator = true;
                peerConnection.createOffer(customSdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void createAnswer() {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                isInitiator = false;
                peerConnection.createAnswer(customSdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                if (queuedRemoteCandidates != null) {
                    queuedRemoteCandidates.add(candidate);
                } else {
                    peerConnection.addIceCandidate(candidate);
                }
            }
        });
    }

    public void setRemoteDescription(final SessionDescription sdp) {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                peerConnection.setRemoteDescription(customSdpObserver, sdp);
            }
        });
    }

    public void stopVideoSource() {
        executor.execute(() -> {
            if (videoCapturer != null && !videoSourceStopped) {
                AppHelper.LogCat("Stop video source.");
                try {
                    videoCapturer.stopCapture();
                } catch (InterruptedException e) {
                }
                videoSourceStopped = true;
            }
        });
    }

    public void startVideoSource() {
        executor.execute(() -> {
            if (videoCapturer != null && videoSourceStopped) {
                AppHelper.LogCat("Restart video source.");
                videoCapturer.startCapture(videoWidth, videoHeight, videoFps);
                videoSourceStopped = false;
            }
        });
    }

    private void reportError(final String errorMessage) {
        AppHelper.LogCat("Peerconnection error: " + errorMessage);
        executor.execute(() -> {
            if (!isError) {
                events.onPeerConnectionError(errorMessage);
                isError = true;
            }
        });
    }


    private void drainCandidates() {
        if (this.queuedRemoteCandidates != null) {
            for (IceCandidate queuedRemoteCandidate : this.queuedRemoteCandidates) {
                this.peerConnection.addIceCandidate(queuedRemoteCandidate);
            }
            this.queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal() {
        if (videoCapturer instanceof CameraVideoCapturer) {
            if (!isVideoCallEnabled() || isError) {
                AppHelper.LogCat(
                        "Failed to switch camera. Video: " + isVideoCallEnabled() + ". Error : " + isError);
                return; // No video is sent or only one camera is available or error happened.
            }
            AppHelper.LogCat("Switch camera");
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        } else {
            AppHelper.LogCat("Will not switch camera, video caputurer is not a camera");
        }
    }

    public void switchCamera() {
        executor.execute(this::switchCameraInternal);
    }


    private class CustomPeerConnectionObserver implements Observer {


        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            executor.execute(() -> events.onIceCandidate(candidate));
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(() -> events.onIceCandidatesRemoved(candidates));
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            AppHelper.LogCat("SignalingState: " + newState);
        }

        @Override
        public void onIceConnectionChange(final IceConnectionState newState) {
            executor.execute(() -> {
                AppHelper.LogCat("IceConnectionState: " + newState);
                if (newState == IceConnectionState.CONNECTED) {
                    events.onIceConnected();
                } else if (newState == IceConnectionState.DISCONNECTED) {
                    events.onIceDisconnected();
                } else if (newState == IceConnectionState.FAILED) {
                    reportError("ICE connection failed.");
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            AppHelper.LogCat("IceGatheringState: " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            AppHelper.LogCat("IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onAddStream(final MediaStream stream) {
            AppHelper.LogCat("Received Remote stream");

            if (videoCallEnabled) {
                remoteVideoTrack = stream.videoTracks.get(0);

                remoteVideoTrack.setEnabled(renderVideo);
                remoteVideoTrack.addSink(remoteRender);
            }

        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            AppHelper.LogCat("New Data channel " + dc.label());
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        @Override
        public void onAddTrack(final RtpReceiver receiver, final MediaStream[] mediaStreams) {
        }
    }


    private class CustomSdpObserver implements SdpObserver {

        private CustomSdpObserver() {
        }

        @Override
        public void onCreateSuccess(SessionDescription origSdp) {
            if (localSdp != null) {
                reportError("Multiple SDP create.");
                return;
            }
            localSdp = origSdp;
            executor.execute(() -> {
                if (peerConnection != null && !isError) {
                    peerConnection.setLocalDescription(customSdpObserver, origSdp);
                }
            });

        }

        @Override
        public void onSetSuccess() {
            executor.execute(() -> {
                if (peerConnection != null && !isError) {
                    if (isInitiator) {
                        if (peerConnection.getRemoteDescription() == null) {
                            events.onLocalDescription(localSdp);
                        } else {
                            drainCandidates();
                        }
                    } else if (peerConnection.getLocalDescription() != null) {
                        events.onLocalDescription(localSdp);
                        drainCandidates();
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(String error) {
            reportError("createSDP error: " + error);
        }

        @Override
        public void onSetFailure(String error) {
            reportError("setSDP error: " + error);
        }
    }
}
