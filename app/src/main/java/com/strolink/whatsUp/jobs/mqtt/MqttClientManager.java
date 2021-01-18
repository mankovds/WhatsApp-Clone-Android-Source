package com.strolink.whatsUp.jobs.mqtt;

import android.content.Context;

import androidx.annotation.NonNull;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abderrahim El imame on 2019-12-01.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MqttClientManager {


    private MqttAndroidClient mqttAndroidClient;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private Context context;

    public MqttAndroidClient initializeMqttClient(Context context, String brokerUrl, String clientId) {
        this.context = context;
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId, persistence);

        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    AppHelper.LogCat("MQTT connection failed " + exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mqttAndroidClient;

    }

    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                AppHelper.LogCat("Successfully disconnected");

            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                AppHelper.LogCat("Failed to disconnected " + throwable.toString());
            }
        });
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(false);
         disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
       // mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        mqttConnectOptions.setAutomaticReconnect(true);


        try {
            byte[] bytes = new JSONObject()
                    .put("subscriber", PreferenceManager.getInstance().getID(context))
                    .put("action", "user_status")
                    .put("connected", "false")
                    .put("isLastSeen", "false")
                    .put("lastSeen", UtilsTime.getCurrentISOTime())
                    .toString()
                    .getBytes();
            mqttConnectOptions.setWill(PreferenceManager.getInstance().getID(context), bytes, AppConstants.MqttConstants.MQTT_MESSAGE_STATUS_QOS, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mqttConnectOptions.setUserName(context.getString(R.string.app_name));
        mqttConnectOptions.setPassword(PreferenceManager.getInstance().getToken(context).toCharArray());
        return mqttConnectOptions;
    }


    public void publishUserStatus(@NonNull String topic, boolean isConnected, boolean isLastSeen)
            throws MqttException {

        MqttMessage message;
        try {
            message = new MqttMessage(new
                    JSONObject()
                    .put("subscriber", PreferenceManager.getInstance().getID(context))
                    .put("action", "user_status")
                    .put("connected", String.valueOf(isConnected))
                    .put("isLastSeen", String.valueOf(isLastSeen))
                    .put("lastSeen", UtilsTime.getCurrentISOTime())
                    .toString()
                    .getBytes());


            message.setId(123);
            message.setRetained(false);
            message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_STATUS_QOS);
            WhatsCloneApplication.getInstance().getClient().publish(topic, message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishMessageAsSeen(@NonNull String topic, @NonNull JSONObject content)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "chat_seen")
                .put("data", content)
                .toString()
                .getBytes());

        message.setId(4357);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_STATUS_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishMessageAsDelivered(@NonNull String topic, @NonNull JSONObject content)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "chat_delivered")
                .put("data", content)
                .toString()
                .getBytes());

        message.setId((2345));
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_STATUS_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishMessage(@NonNull String topic, @NonNull JSONObject content)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "chat")
                .put("data", content)
                .toString()
                .getBytes());

        message.setId(1275);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_QOS);

        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishStory(@NonNull String topic, @NonNull JSONObject content)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "story")
                .put("data", content)
                .toString()
                .getBytes());

        message.setId(1295);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishCall(@NonNull String topic, @NonNull JSONObject content, String action)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", action)
                .put("data", content)
                .toString()
                .getBytes());

        message.setId(23556);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_MESSAGE_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }


    public void publishUserHasJoined(@NonNull String topic, @NonNull JSONObject data)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "new_user_has_joined")
                .put("data", data)
                .toString()
                .getBytes());

        message.setId(152);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_TYPING_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishProfileImageUpdated(@NonNull String topic, @NonNull JSONObject data)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "profile_image_updated")
                .put("data", data)
                .toString()
                .getBytes());

        message.setId(152);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_TYPING_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void publishMessageIsTyping(@NonNull String topic, @NonNull JSONObject data)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "chat_is_typing")
                .put("data", data)
                .toString()
                .getBytes());

        message.setId(153);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_TYPING_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }


    public void publishMessageStopTyping(@NonNull String topic, @NonNull JSONObject data)
            throws MqttException, JSONException {

        MqttMessage message = new MqttMessage(new
                JSONObject()
                .put("subscriber", PreferenceManager.getInstance().getID(context))
                .put("action", "chat_stop_typing")
                .put("data", data)
                .toString()
                .getBytes());

        message.setId(1253);
        message.setRetained(false);
        message.setQos(AppConstants.MqttConstants.MQTT_TYPING_QOS);
        WhatsCloneApplication.getInstance().getClient().publish(topic, message);
    }

    public void subscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {
        IMqttToken token = client.subscribe(topic, AppConstants.MqttConstants.MQTT_SUBSCRIBE_QOS);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                AppHelper.LogCat("Subscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                AppHelper.LogCat("Subscribe Failed " + topic);

            }
        });
    }

    public void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                AppHelper.LogCat("UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                AppHelper.LogCat("UnSubscribe Failed " + topic);
            }
        });
    }

}