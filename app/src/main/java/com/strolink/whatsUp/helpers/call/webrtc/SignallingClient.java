package com.strolink.whatsUp.helpers.call.webrtc;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;


public class SignallingClient {
    private static SignallingClient instance;
    private String roomName = null;

    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;
    private SignalingInterface callback;
    private boolean isHangedUp = false;


    public static SignallingClient getInstance() {
        if (instance == null) {
            instance = new SignallingClient();
        }

        return instance;
    }

    /**
     * Emitters
     */
    public void initializerCall(SignalingInterface signalingInterface, boolean isAccepted) {
        this.callback = signalingInterface;
        if (!isAccepted)
            isInitiator = true;
    }


    public void onSignalingCall(JSONObject data) {


        try {


            AppHelper.LogCat("SignallingClient" + " Json Received :: " + data.toString());

            String reason = data.getString("reason");

            String type = data.getString("type");
            if (type.equalsIgnoreCase("accept")) {

                isChannelReady = true;

                callback.onTryToStart();
            } else if (type.equalsIgnoreCase("offer")) {
                callback.onOfferReceived(data);
            } else if (type.equalsIgnoreCase("answer") && isStarted) {
                callback.onAnswerReceived(data);
            } else if (type.equalsIgnoreCase("candidate") && isStarted) {
                callback.onIceCandidateReceived(data);
            } else if (type.equalsIgnoreCase("bye")) {
                if (data.getString("recipientId").equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                    callback.onRemoteHangUp(reason);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void createOfferCall(SessionDescription sdp, String recipientId) {

        try {

            JSONObject obj = new JSONObject();
            obj.put("room", roomName);
            obj.put("reason", "init_call");
            obj.put("type", sdp.type.canonicalForm());
            obj.put("sdp", sdp.description);
            obj.put("recipientId", recipientId);


            try {


                WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(recipientId, obj, "signaling_call");
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void createIceCandidate(IceCandidate iceCandidate, String recipientId) {

        try {
            JSONObject object = new JSONObject();
            object.put("reason", "init_call");
            object.put("type", "candidate");
            object.put("label", iceCandidate.sdpMLineIndex);
            object.put("id", iceCandidate.sdpMid);
            object.put("candidate", iceCandidate.sdp);
            object.put("recipientId", recipientId);

            try {


                WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(recipientId, object, "signaling_call");
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void acceptCall(String recipientId) {

        try {

            JSONObject obj = new JSONObject();
            obj.put("reason", "Accept");
            obj.put("type", "accept");
            obj.put("recipientId", recipientId);

            try {


                WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(recipientId, obj, "signaling_call");
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void createHangUp(String recipientId) {
        if (!isHangedUp) {
            isHangedUp = true;

            try {
                JSONObject obj = new JSONObject();
                obj.put("reason", "close Exception");
                obj.put("type", "bye");
                obj.put("ownerId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                obj.put("recipientId", recipientId);

                try {


                    WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(recipientId, obj, "signaling_call");
                } catch (MqttException | JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    public interface SignalingInterface {
        void onRemoteHangUp(String reason);

        void onOfferReceived(JSONObject data);

        void onAnswerReceived(JSONObject data);

        void onIceCandidateReceived(JSONObject data);

        void onTryToStart();

    }
}
