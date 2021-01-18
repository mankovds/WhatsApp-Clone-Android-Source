package com.strolink.whatsUp.helpers.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.strolink.whatsUp.activities.call.AudioCallView;
import com.strolink.whatsUp.activities.call.IncomingCallScreen;
import com.strolink.whatsUp.activities.call.VideoCallView;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.presenters.controllers.CallsController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;


public class CallingApi {


    public static void OpenIncomingCallScreen(JSONObject data, Context context) {

        try {

            AppHelper.LogCat(data.toString());
            Intent incomingScreen = new Intent(context, IncomingCallScreen.class);
            incomingScreen.putExtra("call_from", data.getString("call_from"));
            incomingScreen.putExtra("callId", data.getString("callId"));
            incomingScreen.putExtra("callType", data.getString("callType"));
            incomingScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(incomingScreen);

        } catch (JSONException e3) {

            AppHelper.LogCat("JSONException " + e3.getMessage());

            e3.printStackTrace();
        }
    }

    public static void sendCallEvent(Context context, String status, String recipientId, boolean videoCall) {
        try {

            JSONObject updateMessage = new JSONObject();


            updateMessage.put("call_from", PreferenceManager.getInstance().getID(context));

            if (videoCall) {
                updateMessage.put("callType", AppConstants.VIDEO_CALL);
            } else {
                updateMessage.put("callType", AppConstants.VOICE_CALL);
            }
            updateMessage.put("status", status);
            updateMessage.put("recipientId", recipientId);


            try {


                WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(recipientId, updateMessage, "call_change_status");
            } catch (MqttException | JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }


    @SuppressLint("CheckResult")
    public static void callInit(Context context, String recipientId, String type) {
        CallsController.getInstance().saveCallToLocalDB(context, recipientId, type);
    }


    public static void startCall(Context context, String callType, boolean accepted, String caller_id, String callId) {

        boolean videoCallEnabled = true;
        Intent intent;
        if (callType.contentEquals(AppConstants.VOICE_CALL)) {
            videoCallEnabled = false;
            intent = new Intent(context, AudioCallView.class);
        } else {
            intent = new Intent(context, VideoCallView.class);
        }

        intent.putExtra("isAccepted", accepted);
        intent.putExtra("caller_id", caller_id);
        intent.putExtra("callId", callId);
        intent.putExtra(WhatsCloneApplication.getInstance().getPackageName() + ".VIDEO_CALL", videoCallEnabled);
        context.startActivity(intent);

    }

    static void initiateCall(Context context, String to, String callType) {
        callInit(context, to, callType);
    }


}
