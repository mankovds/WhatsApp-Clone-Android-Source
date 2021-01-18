package com.strolink.whatsUp.helpers.call.webrtc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class CallAudioManager {
    private final Context apprtcContext;
    private final Set<AudioDevice> audioDevices = new HashSet<>();
    private AudioManager audioManager;
    private final AudioDevice defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
    private boolean initialized = false;
    private CallProximitySensor proximitySensor = null;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsMicrophoneMute = false;
    private boolean savedIsSpeakerPhoneOn = false;
    private AudioDevice selectedAudioDevice;
    private BroadcastReceiver wiredHeadsetReceiver;


    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE
    }

    private void onProximitySensorChangedState() {
        if (this.audioDevices.size() != 2 || !this.audioDevices.contains(AudioDevice.EARPIECE) || !this.audioDevices.contains(AudioDevice.SPEAKER_PHONE)) {
            return;
        }
        if (this.proximitySensor.sensorReportsNearState()) {
            setAudioDevice(AudioDevice.EARPIECE);
        } else {
            setAudioDevice(AudioDevice.SPEAKER_PHONE);
        }
    }

    public static CallAudioManager create(Context context) {
        return new CallAudioManager(context);
    }

    private CallAudioManager(Context context) {
        this.apprtcContext = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.proximitySensor = CallProximitySensor.create(context, this::onProximitySensorChangedState);
    }

    public void init() {
        if (!this.initialized) {
            this.savedAudioMode = this.audioManager.getMode();
            this.savedIsSpeakerPhoneOn = this.audioManager.isSpeakerphoneOn();
            this.savedIsMicrophoneMute = this.audioManager.isMicrophoneMute();
            this.audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            this.audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            setMicrophoneMute(false);
            updateAudioDeviceState(hasWiredHeadset());
            registerForWiredHeadsetIntentBroadcast();
            this.initialized = true;
        }
    }

    public void close() {
        if (this.initialized) {
            unregisterForWiredHeadsetIntentBroadcast();
            setSpeakerphoneOn(this.savedIsSpeakerPhoneOn);
            setMicrophoneMute(this.savedIsMicrophoneMute);
            this.audioManager.setMode(this.savedAudioMode);
            this.audioManager.abandonAudioFocus(null);
            if (this.proximitySensor != null) {
                this.proximitySensor.stop();
                this.proximitySensor = null;
            }
            this.initialized = false;
        }
    }

    public void setAudioDevice(AudioDevice device) {
        assertIsTrue(this.audioDevices.contains(device));
        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                this.selectedAudioDevice = AudioDevice.SPEAKER_PHONE;
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                this.selectedAudioDevice = AudioDevice.EARPIECE;
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                this.selectedAudioDevice = AudioDevice.WIRED_HEADSET;
                break;
            default:
                Log.e("ben", "Invalid audio device selection");
                break;
        }
        onAudioManagerChangedState();
    }

    private void registerForWiredHeadsetIntentBroadcast() {
        IntentFilter filter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        this.wiredHeadsetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean hasWiredHeadset = true;
                int state = intent.getIntExtra("state", 0);
                int microphone = intent.getIntExtra("microphone", 0);
                String name = intent.getStringExtra("name");
                if (state != 1) {
                    hasWiredHeadset = false;
                }
                switch (state) {
                    case 0:
                        updateAudioDeviceState(hasWiredHeadset);
                        return;
                    case 1:
                        if (selectedAudioDevice != AudioDevice.WIRED_HEADSET) {
                            updateAudioDeviceState(hasWiredHeadset);
                            return;
                        }
                        return;
                    default:
                        Log.e("ben", "Invalid state");
                        return;
                }
            }
        };
        this.apprtcContext.registerReceiver(this.wiredHeadsetReceiver, filter);
    }

    private void unregisterForWiredHeadsetIntentBroadcast() {
        if (wiredHeadsetReceiver != null) {
            this.apprtcContext.unregisterReceiver(this.wiredHeadsetReceiver);
            this.wiredHeadsetReceiver = null;
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        if (this.audioManager.isSpeakerphoneOn() != on) {
            this.audioManager.setSpeakerphoneOn(on);
        }
    }

    public void setMicrophoneMute(boolean on) {
        if (this.audioManager.isMicrophoneMute() != on) {
            this.audioManager.setMicrophoneMute(on);
        }
    }

    private boolean hasEarpiece() {
        return this.apprtcContext.getPackageManager().hasSystemFeature("android.hardware.telephony");
    }

    @Deprecated
    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return audioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d("ben", "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d("ben", "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }
            return false;
        }

    }

    private void updateAudioDeviceState(boolean hasWiredHeadset) {
        this.audioDevices.clear();
        if (hasWiredHeadset) {
            this.audioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            this.audioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                this.audioDevices.add(AudioDevice.EARPIECE);
            }
        }
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET);
        } else {
            setAudioDevice(this.defaultAudioDevice);
        }
    }

    private void onAudioManagerChangedState() {
        boolean z = true;
        if (this.audioDevices.size() == 2) {
            if (!(this.audioDevices.contains(AudioDevice.EARPIECE) && this.audioDevices.contains(AudioDevice.SPEAKER_PHONE))) {
                z = false;
            }
            assertIsTrue(z);
            this.proximitySensor.start();
        } else if (this.audioDevices.size() == 1) {
            this.proximitySensor.stop();
        } else {
            Log.e("ben", "Invalid device list");
        }
    }


    public void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}
