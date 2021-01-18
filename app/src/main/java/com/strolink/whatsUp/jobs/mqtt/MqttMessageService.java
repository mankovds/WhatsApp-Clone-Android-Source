package com.strolink.whatsUp.jobs.mqtt;

import com.strolink.whatsUp.BuildConfig;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.call.CallingApi;
import com.strolink.whatsUp.helpers.call.webrtc.SignallingClient;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.CallsController;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abderrahim El imame on 2019-12-20.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MqttMessageService extends MqttService {

    private MqttClientManager mqttClientManager;
    private MqttAndroidClient mqttAndroidClient;

    @Override
    public void onCreate() {
        super.onCreate();

        AppHelper.LogCat("onStartJob: MqttMessageService ");


        if (AppConstants.CLIENT_ID != null) {
            mqttClientManager = new MqttClientManager();
            mqttAndroidClient = mqttClientManager.initializeMqttClient(getApplicationContext(), BuildConfig.MQTT_BROKER_URL, AppConstants.CLIENT_ID);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    AppHelper.LogCat("Connected to MQTT. IsReconnect ? " + reconnect);
                    /**
                     * Subcribe to user status topic
                     */
                    try {

                        mqttClientManager.publishUserStatus(AppConstants.MqttConstants.PUBLISH_USER_STATUS, true, false);
                        mqttClientManager.subscribe(mqttAndroidClient, PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                        mqttClientManager.subscribe(mqttAndroidClient, AppConstants.MqttConstants.PUBLISH_USER_STATUS);
                        mqttClientManager.subscribe(mqttAndroidClient, AppConstants.MqttConstants.PUBLISH_WHATSCLONE_GENERAL);

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }


                    AppHelper.LogCat("MQTT connection success");

                }

                @Override
                public void connectionLost(Throwable cause) {

                    AppHelper.LogCat("Connection lost. ${cause!!.message}");

                }


                @Override
                public void messageArrived(String topic, MqttMessage message) {

                    AppExecutors.getInstance().diskIO().execute(() -> {
                        try {
                            if (message != null) {
                                JSONObject packet = new JSONObject(new String(message.getPayload()));
                                AppHelper.LogCat("Received Raw Packet: " + packet.toString());

                                String action = packet.getString("action");
                                String subscriber = packet.getString("subscriber");
                                if (!subscriber.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {
                                    if (action.equals("user_status")) {
                                        onUserOnline(packet, subscriber);
                                    } else {
                                        JSONObject data = packet.getJSONObject("data");
                                        switch (action) {
                                            case "chat":
                                                onMessage(data);
                                                break;
                                            case "chat_delivered":
                                                onMessageDelivered(data);
                                                break;
                                            case "chat_mine_delivered":
                                                onMessageMineDelivered(data);
                                                break;
                                            case "chat_seen":
                                                onMessageSeen(data);
                                                break;
                                            case "chat_mine_seen":
                                                onMessageMineSeen(data);
                                                break;
                                            case "chat_is_typing":
                                                onTyping(data);
                                                break;
                                            case "chat_stop_typing":
                                                onStopTyping(data);
                                                break;
                                            case "profile_image_updated":
                                                onUpdateImageProfile(data);
                                                break;
                                            case "new_user_has_joined":
                                                onNewUserJoined(data);
                                                break;

                                            case "story":
                                                onNewStory(data);
                                                break;
                                            case "story_mine_seen":
                                                onStoryMineSeen(data);
                                                break;
                                            case "story_seen":
                                                onStorySeen(data);
                                                break;

                                            case "expired_story":
                                                onStoryExpired(data);
                                                break;

                                            case "call":
                                                onNewCall(data);
                                                break;
                                            case "call_mine_seen":
                                                onCallMineSeen(data);
                                                break;
                                            case "call_seen":
                                                onCallSeen(data);
                                                break;
                                            case "signaling_call":
                                                onSignalingCall(data);
                                                break;
                                            case "call_change_status":
                                                onCallChange(data);
                                                break;
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }


                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                    AppHelper.LogCat("delivered--------");
            /*
                try {
                    MqttDeliveryToken token = (MqttDeliveryToken) iMqttDeliveryToken;
                    String messagesModel = token.getMessage().toString();
                    AppHelper.LogCat("deliverd message :" + messagesModel);
                    JSONObject data = new JSONObject(token.getMessage().toString());

                    try {
                         WhatsCloneApplication.getInstance().getMqttClientManager().publishMessageAsDelivered(mqttAndroidClient, 1, data.getString("senderId") + "/" + PreferenceManager.getInstance().getID(getApplicationContext()), messagesModel);

                    } catch (MqttException e) {
                        e.printStackTrace();
                        AppHelper.LogCat(" sendDeliveredStatusToServer MainService " + e.getMessage());
                    }
                   // onMessageDelivered(data);
                } catch (MqttException me) {
                    System.out.println("reason " + me.getReasonCode());
                    System.out.println("msg " + me.getMessage());
                    System.out.println("loc " + me.getLocalizedMessage());
                    System.out.println("cause " + me.getCause());
                    System.out.println("excep " + me);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                }
            });
        }
    }

    /*************************************************************************************************************************************************************************
     * ***********************************************************  MQTT Methods       ***************************************************************************************
     *************************************************************************************************************************************************************************/
    private void onNewUserJoined(JSONObject data) {
        AppHelper.LogCat("NEW_USER_JOINED " + data);
        try {
            String ownerId = data.getString("senderId");
            String phone = data.getString("phone");

            if (ownerId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), phone)) {
                NotificationsManager.getInstance().showSimpleNotification(WhatsCloneApplication.getInstance(), false, phone, AppConstants.JOINED_MESSAGE_SMS, ownerId, null);
                WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void onCallChange(JSONObject data) {
        AppHelper.LogCat("CALL_CHANGE_STATUS " + data.toString());

        try {
            JSONObject object = new JSONObject();
            object.put("eventName", "CallEventChange");
            object.put("status", data.getString("status"));
            EventBus.getDefault().post(object);
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onSignalingCall(JSONObject data) {
        AppHelper.LogCat(" SIGNALING CALL " + data.toString());

        SignallingClient.getInstance().onSignalingCall(data);

    }

    private void onCallSeen(JSONObject data) {
        AppHelper.LogCat(" IS_CALL_SEEN " + data.toString());
        try {

            CallsController.getInstance().updateSeenStatus(mqttClientManager, mqttAndroidClient, data.getString("callId"), data.getJSONArray("users").getString(0));
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }

    }

    private void onCallMineSeen(JSONObject data) {
        //emit by mqtt to other user
        try {

            CallsController.getInstance().updateSeenCallStatus(data.getString("callId"));
            mqttClientManager.publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED, data, "call");
        } catch (MqttException | JSONException e) {
            e.printStackTrace();
        }
    }


    private void onNewCall(JSONObject data) {
        AppHelper.LogCat(" NEW_USER_CALL " + data.toString());
        try {


            if (!MessagesController.getInstance().checkIfUserBlockedExist(data.getString("call_from"))) {

                AppHelper.LogCat(" NEW_USER_CALL checkIfUserBlockedExist ");

                if (data.getString("status").equals("init_call")) {
                    AppHelper.LogCat("onCallEventStatus" + "init_call");

                    if (!data.getBoolean("missed")) {

                        CallingApi.OpenIncomingCallScreen(data, WhatsCloneApplication.getInstance());

                    } else {
                        if (CallsController.getInstance().checkIfSingleCallExist(data.getString("callId"))) {
                            AppHelper.LogCat("checkIfSingleCallExist ");
                            JSONObject updateMessage = new JSONObject();
                            try {
                                updateMessage.put("callId", data.getString("callId"));

                                try {


                                    mqttClientManager.publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage, "call");
                                } catch (MqttException | JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                // e.printStackTrace();
                                AppHelper.LogCat("JSONException " + e.getMessage());
                            }


                        } else {
                            boolean video;
                            video = data.getString("callType").equals(AppConstants.VIDEO_CALL);
                            try {
                                CallsController.getInstance().saveToDataBase(data.getString("call_from"),
                                        data.getString("callId"),
                                        video, UtilsTime.getCorrectDate(data.getString("date")),
                                        data.getJSONObject("owner").getString("phone"),
                                        data.getJSONObject("owner").getString("username"));

                                if (video)
                                    NotificationsManager.getInstance().showSimpleNotification(WhatsCloneApplication.getInstance(), true, WhatsCloneApplication.getInstance().getString(R.string.video_call_notify), WhatsCloneApplication.getInstance().getString(R.string.new_missed_call), String.valueOf(data.getString("call_from")), null);
                                else
                                    NotificationsManager.getInstance().showSimpleNotification(WhatsCloneApplication.getInstance(), true, WhatsCloneApplication.getInstance().getString(R.string.audio_call_notify), WhatsCloneApplication.getInstance().getString(R.string.new_missed_call), String.valueOf(data.getString("call_from")), null);

                            } catch (Exception e) {
                                AppHelper.LogCat(e.getMessage());
                            }


                        }
                    }

                }
            } else {
                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("callId", data.getString("callId"));

                    try {


                        mqttClientManager.publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage, "call");
                    } catch (MqttException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    AppHelper.LogCat("JSONException " + e.getMessage());
                }


            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onStorySeen(JSONObject data) {
        AppHelper.LogCat(" IS_STORY_SEEN " + data.toString());
        try {

            StoriesController.getInstance().updateSeenStatus(data.getString("storyId"), data.getJSONArray("users"));
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }

    }

    private void onStoryMineSeen(JSONObject data) {
        //emit by mqtt to other user
        try {
            if (data.getBoolean("mine"))
                StoriesController.getInstance().updateStoryStatus(data.getString("storyId"));
            else
                mqttClientManager.publishStory(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_STORY_EXIST_AS_FINISHED, data);
        } catch (MqttException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void onStoryExpired(JSONObject data) {

        AppHelper.LogCat("NEW_EXPIRED_STORY_FROM_SERVER " + data.toString());
        try {
            StoriesController.getInstance().deleteExpiredStory(data.getString("_id"), data.getJSONObject("owner").getString("_id"));
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onNewStory(JSONObject data) {
        AppHelper.LogCat("NEW_USER_STORY " + data.toString());
        try {


            if (!MessagesController.getInstance().checkIfUserBlockedExist(data.getJSONObject("owner").getString("_id"))) {

                StoriesController.getInstance().saveNewUserStory(data);

            } else {
// TODO: 2019-12-06 in backend side i need to check if the user is blocked

            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onUserOnline(JSONObject data, String subscriber) {
        AppHelper.LogCat("onUserOnline " + data.toString());
        try {
            if (data.getString("connected").equals("true")) {
                AppHelper.LogCat("connected");
                if (data.getString("isLastSeen").equals("true")) {

                    UsersModel usersModel = UsersController.getInstance().getUserById(subscriber);
                    if (usersModel != null) {
                        usersModel.setLast_seen(data.getString("lastSeen"));
                        usersModel.setConnected(Boolean.valueOf(data.getString("connected")));
                        UsersController.getInstance().updateUser(usersModel);
                    }
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_LAST_SEEN, data.getString("lastSeen")));
                } else {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_IS_ONLINE, subscriber));
                }
            } else {
                AppHelper.LogCat("not connected");
                if (data.getString("isLastSeen").equals("true")) {

                    UsersModel usersModel = UsersController.getInstance().getUserById(subscriber);
                    usersModel.setLast_seen(data.getString("lastSeen"));
                    usersModel.setConnected(Boolean.valueOf(data.getString("connected")));
                    UsersController.getInstance().updateUser(usersModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_LAST_SEEN, data.getString("lastSeen")));
                } else {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_USER_STATE, AppConstants.EVENT_BUS_USER_IS_OFFLINE, subscriber));
                }
            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }

    }

    private void onUpdateImageProfile(JSONObject data) {

        AppHelper.LogCat("IMAGE_PROFILE_UPDATED ");

        try {
            String ownerId = data.getString("ownerId");
            boolean is_group = data.getBoolean("is_group");

            MessagesController.getInstance().getNotifyForImageProfileChanged(ownerId, data.getString("image"), is_group);
        } catch (JSONException e) {
            AppHelper.LogCat(e);
        }

    }

    private void onTyping(JSONObject data) {

        try {
            if (data.getBoolean("is_group")) {

                AppHelper.LogCat("MEMBER_IS_TYPING ");


                String senderID = data.getString("senderId");
                String groupId = data.getString("groupId");

                if (!MessagesController.getInstance().checkIfGroupExist(groupId) || MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MEMBER_TYPING, groupId, senderID));

            } else {
                AppHelper.LogCat(" IS_TYPING " + data.toString());


                String senderID = data.getString("senderId");
                String recipientID = data.getString("recipientId");
                if (MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USER_TYPING, recipientID, senderID));

            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }

    }

    private void onStopTyping(JSONObject data) {
        try {
            if (data.getBoolean("is_group")) {
                AppHelper.LogCat("MEMBER_IS_STOP_TYPING ");

                String senderID = data.getString("senderId");
                String groupId = data.getString("groupId");
                if (!MessagesController.getInstance().checkIfGroupExist(groupId) || MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MEMBER_STOP_TYPING, groupId, senderID));
            } else {
                AppHelper.LogCat(" IS_STOP_TYPING " + data.toString());

                String senderID = data.getString("senderId");
                String recipientID = data.getString("recipientId");
                if (MessagesController.getInstance().checkIfUserBlockedExist(senderID))
                    return;
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USER_STOP_TYPING, recipientID, senderID));
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }

    }


    private void onMessageMineSeen(JSONObject data) {
        AppHelper.LogCat(" IS_MESSAGE_MINE_SEEN " + data.toString());
        try {
            MessagesController.getInstance().updateConversationStatus(data.getString("messageId"));
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onMessageSeen(JSONObject data) {

        AppHelper.LogCat(" IS_MESSAGE_SEEN " + data.toString());
        try {

            MessagesController.getInstance().updateSeenStatus(
                    data.getString("messageId"),
                    data.getString("ownerId"),
                    data.getString("recipientId"),
                    data.getBoolean("is_group"),
                    data.getJSONArray("users"));

        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onMessageMineDelivered(JSONObject data) {
        AppHelper.LogCat(" IS_MESSAGE_MINE_DELIVERED " + data.toString());
        try {
            MessageModel messagesModel = MessagesController.getInstance().getMessageById(data.getString("messageId"));

            if (messagesModel != null) {
                messagesModel.setStatus(AppConstants.IS_DELIVERED);
                MessagesController.getInstance().updateMessage(messagesModel);

                AppHelper.LogCat("RecipientMarkMessageAsDelivered successfully");
            } else {
                AppHelper.LogCat("RecipientMarkMessageAsDelivered failed ");
            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }

    }

    private void onMessageDelivered(JSONObject data) {
        AppHelper.LogCat(" IS_MESSAGE_DELIVERED " + data.toString());
        try {
            MessagesController.getInstance().updateDeliveredStatus(
                    data.getString("messageId"),
                    data.getString("ownerId"),
                    data.getString("recipientId"),
                    data.getBoolean("is_group"),
                    data.getJSONArray("users"));
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }

    private void onMessage(JSONObject data) {
        AppHelper.LogCat("NEW_USER_MESSAGE " + data.toString());
        try {

            if (!MessagesController.getInstance().checkIfUserBlockedExist(data.getJSONObject("sender").getString("_id"))) {
                if (data.getBoolean("is_group")) {
                    MessagesController.getInstance().saveNewMessageGroup(data, WhatsCloneApplication.getInstance());
                } else {
                    MessagesController.getInstance().saveNewUserMessage(data, WhatsCloneApplication.getInstance());
                }
            }
        } catch (JSONException e) {
            AppHelper.LogCat("JSONException " + e.getMessage());
        }
    }
}
