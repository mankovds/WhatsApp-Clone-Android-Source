package com.strolink.whatsUp.helpers.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.main.MainActivity;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.activities.settings.PreferenceSettingsManager;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;

/**
 * Created by Abderrahim El imame on 6/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NotificationsManager {

    private static volatile NotificationsManager Instance = null;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private int index = 21828;
    // private int numMessages = 0;
    //private static MemoryCache memoryCache;

    public NotificationsManager() {
    }


    public static NotificationsManager getInstance() {

        NotificationsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationsManager();
                }
            }
        }
        return localInstance;

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public Notification createNotificationChannel(Context mContext) {

        String CHANNEL_ID = mContext.getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                mContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build();

        return notification;
    }

    @SuppressLint("StaticFieldLeak")
    public void showUserNotification(Context mContext, String conversationID, String phone, String message, String userId, String Avatar) {



            //for android O
            String CHANNEL_ID;
            NotificationChannel mChannel;
            //

            // memoryCache = new MemoryCache();
            //  String text = UtilsString.unescapeJava(message);
            Intent messagingIntent = new Intent(mContext, MainActivity.class);
            messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            int counterUnreadConversation = MessagesController.getInstance().loadAllUnreadChatsCounter();

            int counterUnreadMessages = MessagesController.getInstance().getMessagesBadge(PreferenceManager.getInstance().getID(mContext));
            List<MessageModel> msgs = getNotificationMessages(userId, mContext);


            if (counterUnreadConversation == 1) {
                /**
                 * this for default activity
                 */
                messagingIntent = new Intent(mContext, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", conversationID);
                messagingIntent.putExtra("recipientID", userId);
                messagingIntent.putExtra("isGroup", false);

            }


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack
            stackBuilder.addParentStack(MessagesActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(messagingIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);



            final NotificationCompat.Builder mNotifyBuilder;

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            String username = phone;
            try {


                if (msgs.size() != 0) {
                    String name = UtilsPhone.getContactName(phone);
                    if (name != null) {
                        username = name;
                    } else {
                        username = phone;
                    }

                } else {
                    String name = UtilsPhone.getContactName(phone);
                    if (name != null) {
                        username = name;
                    } else {
                        username = phone;
                    }
                }


            } catch (Exception e) {
                AppHelper.LogCat(" " + e.getMessage());
                username = phone;
            }
            mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            if (AppHelper.isAndroid8()) {

                CHANNEL_ID = username;// The id of the channel.
                CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
                mChannel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
                mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(resultPendingIntent)
                        .setChannelId(CHANNEL_ID)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);


                mNotificationManager.createNotificationChannel(mChannel);
            } else {

                CHANNEL_ID = username;// The id of the channel.
                mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(resultPendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

            }
            if (!msgs.isEmpty()) {
                msgs.size();
                if (counterUnreadConversation == 1) {
                    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultPendingIntent).build();
                    mNotifyBuilder.addAction(action);

                    inboxStyle.setBigContentTitle(username);

                    mNotifyBuilder.setContentTitle(username);

                    mNotifyBuilder.setContentText(UtilsString.unescapeJava(msgs.get(msgs.size() - 1).getMessage()));
                    inboxStyle.setSummaryText(counterUnreadMessages + " " + mContext.getString(R.string.new_messages_notify));
                    for (MessageModel m : msgs) {
                        inboxStyle.addLine(UtilsString.unescapeJava(m.getMessage()));
                    }

                } else {
                    inboxStyle.setBigContentTitle(mContext.getResources().getString(R.string.app_name));

                    mNotifyBuilder.setContentTitle(username);

                    mNotifyBuilder.setContentText(UtilsString.unescapeJava(msgs.get(msgs.size() - 1).getMessage()));
                    inboxStyle.setSummaryText(counterUnreadMessages + " " + mContext.getString(R.string.messages_from_notify) + " " + counterUnreadConversation + " " + mContext.getString(R.string.chats_notify));
                    for (MessageModel m : msgs) {

                        if (m.getSenderId() != null) {

                            String name = UtilsPhone.getContactName(m.getSender_phone());
                            if (name != null) {
                                username = name;
                            } else {
                                username = phone;
                            }

                            inboxStyle.addLine("".concat(username).concat(" : ").concat(UtilsString.unescapeJava(m.getMessage())));
                        } else {

                            String name = UtilsPhone.getContactName(m.getSender_phone());
                            if (name != null) {
                                username = name;
                            } else {
                                username = phone;
                            }
                            inboxStyle.addLine("".concat(username).concat(" : ").concat(UtilsString.unescapeJava(m.getMessage())));
                        }

                    }
                }
            } else {
                NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultPendingIntent).build();
                mNotifyBuilder.addAction(action);

                inboxStyle.setBigContentTitle(username);

                mNotifyBuilder.setContentTitle(username);


                mNotifyBuilder.setContentText(UtilsString.unescapeJava(message));
                inboxStyle.setSummaryText(counterUnreadMessages + " " + mContext.getString(R.string.new_messages_notify));
                inboxStyle.addLine(UtilsString.unescapeJava(message));

            }
            mNotifyBuilder.setStyle(inboxStyle);
            Drawable drawable = AppHelper.getDrawable(mContext, R.drawable.holder_user);

            if (Avatar != null) {


                GlideApp.with(mContext)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + Avatar))
                        .signature(new ObjectKey(Avatar))
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                mNotifyBuilder.setLargeIcon(resource);
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                                mNotifyBuilder.setLargeIcon(bitmap);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);

                                Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                                mNotifyBuilder.setLargeIcon(bitmap);
                            }
                        });

            } else {

                Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                mNotifyBuilder.setLargeIcon(bitmap);
            }

            if (PreferenceSettingsManager.conversation_tones(mContext)) {

                Uri uri = PreferenceSettingsManager.getDefault_message_notifications_settings_tone(mContext);
                if (uri != null)
                    mNotifyBuilder.setSound(uri);
                else {
                    int defaults = 0;
                    defaults = defaults | Notification.DEFAULT_SOUND;
                    mNotifyBuilder.setDefaults(defaults);
                }


            }

            if (PreferenceSettingsManager.getDefault_message_notifications_settings_vibrate(mContext)) {

                mNotifyBuilder.setVibrate(new long[] {1000, 100, 1000, 100, 1000});
            } else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_VIBRATE;
                mNotifyBuilder.setDefaults(defaults);
            }


            String colorLight = PreferenceSettingsManager.getDefault_message_notifications_settings_light(mContext);
        mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);


        mNotifyBuilder.setAutoCancel(true);

            mNotificationManager.notify(userId, index, mNotifyBuilder.build());

            SetupBadger(mContext);
            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));


    }

    @SuppressLint("StaticFieldLeak")
    public void showSimpleNotification(Context mContext, boolean missed_call, String phone, String message, String userId, String Avatar) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //

        // memoryCache = new MemoryCache();
        String text = UtilsString.unescapeJava(message);
        Intent mIntent = new Intent(mContext, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        /**
         * this for default activity
         */
        mIntent = new Intent(mContext, MainActivity.class);
        mIntent.putExtra("missed_call", missed_call);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(mIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        final NotificationCompat.Builder mNotifyBuilder;

        String username;
        try {

            String name = UtilsPhone.getContactName(phone);
            if (name != null) {
                username = name;
            } else {
                username = phone;
            }

        } catch (Exception e) {
            AppHelper.LogCat(" " + e.getMessage());
            username = phone;
        }
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = username;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            ;

            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = username;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            ;

        }

        mNotifyBuilder.setContentTitle(username);
        mNotifyBuilder.setContentText(text);
        Drawable drawable = AppHelper.getDrawable(mContext, R.drawable.holder_user);
        if (Avatar != null) {


            GlideApp.with(mContext)
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + Avatar))
                    .signature(new ObjectKey(Avatar))
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mNotifyBuilder.setLargeIcon(resource);
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }
                    });

        } else {

            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
            mNotifyBuilder.setLargeIcon(bitmap);
        }


        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_notifications_settings_vibrate(mContext)) {

            mNotifyBuilder.setVibrate(new long[] {1000, 100, 1000, 100, 1000});
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_notifications_settings_light(mContext);
        mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);


        mNotifyBuilder.setAutoCancel(true);

        mNotificationManager.notify(userId, index, mNotifyBuilder.build());

        SetupBadger(mContext);
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));

    }

    private Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }


    private List<MessageModel> getNotificationMessages(String userId, Context mContext) {

        return MessagesController.getInstance().getNotificationMessages(userId, PreferenceManager.getInstance().getID(mContext) );
    }


    @SuppressLint("StaticFieldLeak")
    public void showGroupNotification(Context mContext, Intent resultIntent, Intent messagingGroupPopupIntent, String groupName, String message, String groupId, String Avatar) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //


        //   memoryCache = new MemoryCache();

        String text = UtilsString.unescapeJava(message);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MessagesActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        final NotificationCompat.Builder mNotifyBuilder;


        //   ++numMessages;
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_reply_black_24dp, mContext.getString(R.string.reply_message), resultPendingIntent).build();
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = groupName;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(action)
                    .setContentTitle(groupName)
                    .setContentText(text)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            ;

            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            mNotifyBuilder = new NotificationCompat.Builder(mContext)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(action)
                    .setContentTitle(groupName)
                    .setContentText(text)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    //  .setNumber(numMessages)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            ;
        }
        Drawable drawable = AppHelper.getDrawable(mContext, R.drawable.holder_group);


        if (Avatar != null) {


            GlideApp.with(mContext)
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + Avatar))

                    .signature(new ObjectKey(Avatar))
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mNotifyBuilder.setLargeIcon(resource);
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
                            mNotifyBuilder.setLargeIcon(bitmap);
                        }
                    });

        } else {

            Bitmap bitmap = convertToBitmap(drawable, AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
            mNotifyBuilder.setLargeIcon(bitmap);
        }

        mNotifyBuilder.setAutoCancel(true);


        if (PreferenceSettingsManager.conversation_tones(mContext)) {

            Uri uri = PreferenceSettingsManager.getDefault_message_group_notifications_settings_tone(mContext);
            if (uri != null)
                mNotifyBuilder.setSound(uri);
            else {
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_SOUND;
                mNotifyBuilder.setDefaults(defaults);
            }


        }

        if (PreferenceSettingsManager.getDefault_message_group_notifications_settings_vibrate(mContext)) {

            mNotifyBuilder.setVibrate(new long[] {1000, 100, 1000, 100, 1000});
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_group_notifications_settings_light(mContext);
        mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);


        mNotificationManager.notify(groupId, index, mNotifyBuilder.build());

    }


    @SuppressLint("StaticFieldLeak")
    public void showCheckMessageNotification(Context mContext, String username, String message, int notificationId) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //

        Intent mIntent = new Intent(mContext, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(mIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        final NotificationCompat.Builder mNotifyBuilder;


        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = username;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);


            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = username;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)

                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            ;

        }

        mNotifyBuilder.setContentTitle(username);
        mNotifyBuilder.setContentText(message);


        Bitmap bitmap = convertToBitmap(AppHelper.getDrawable(mContext, R.mipmap.ic_launcher), AppConstants.NOTIFICATIONS_IMAGE_SIZE, AppConstants.NOTIFICATIONS_IMAGE_SIZE);
        mNotifyBuilder.setLargeIcon(bitmap);


        mNotifyBuilder.setSound(null);

        if (PreferenceSettingsManager.getDefault_message_notifications_settings_vibrate(mContext)) {

            mNotifyBuilder.setVibrate(new long[] {1000, 100, 1000, 100, 1000});
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            mNotifyBuilder.setDefaults(defaults);
        }


        String colorLight = PreferenceSettingsManager.getDefault_message_notifications_settings_light(mContext);
        if (colorLight != null) {
            mNotifyBuilder.setLights(Color.parseColor(colorLight), 1500, 1500);
        } else {
            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            mNotifyBuilder.setDefaults(defaults);
        }


        mNotifyBuilder.setAutoCancel(true);


        mNotificationManager.notify(notificationId, mNotifyBuilder.build());


    }


    @SuppressLint("StaticFieldLeak")
    public void updateUpDownNotification(Context mContext, String messageId, int progress) {

        // mNotifyBuilder.setContentText(progress + "%");
        mNotifyBuilder.setProgress(100, progress, false);

        mNotificationManager.notify(messageId, index, mNotifyBuilder.build());
    }

    @SuppressLint("StaticFieldLeak")
    public void showUpDownNotification(Context mContext, String storyId, String userId) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //

        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = userId;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            //  mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);

            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = userId;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            //  mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);


        }


        mNotifyBuilder.setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setSound(null);

        mNotifyBuilder.setContentTitle("Sending Story ...");

        mNotifyBuilder.setProgress(0, 0, true);
        mNotificationManager.notify(storyId, index, mNotifyBuilder.build());
    }

    @SuppressLint("StaticFieldLeak")
    public void showUpDownNotification(Context mContext, String groupName, String messageId, String groupId, String conversationID) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //


        /**
         * this for default activity
         */
        Intent messagingIntent;//= new Intent(mContext, MainActivity.class);
        //messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        messagingIntent = new Intent(mContext, MessagesActivity.class);
        messagingIntent.putExtra("conversationID", conversationID);
        messagingIntent.putExtra("groupID", groupId);
        messagingIntent.putExtra("isGroup", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        //   stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(messagingIntent);
        // Adds the Intent to the top of the stack
        //  stackBuilder.addNextIntent(messagingIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = groupName;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);

            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = groupName;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);


        }


        mNotifyBuilder.setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setSound(null);
        if (!groupName.equals("null")) {
            mNotifyBuilder.setContentTitle(mContext.getString(R.string.sending_file_to) + groupName);
        }

        mNotifyBuilder.setProgress(0, 0, true);
        mNotificationManager.notify(messageId, index, mNotifyBuilder.build());
    }

    @SuppressLint("StaticFieldLeak")
    public void showUpDownNotification(Context mContext, String UserName, String phone, String messageId, String userId, String conversationID) {


        //for android O
        String CHANNEL_ID;
        NotificationChannel mChannel;
        //


        /**
         * this for default activity
         */
        Intent messagingIntent;//= new Intent(mContext, MainActivity.class);
        //messagingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        messagingIntent = new Intent(mContext, MessagesActivity.class);
        messagingIntent.putExtra("conversationID", conversationID);
        messagingIntent.putExtra("recipientID", userId);
        messagingIntent.putExtra("isGroup", false);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        //   stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntentWithParentStack(messagingIntent);
        // Adds the Intent to the top of the stack
        //  stackBuilder.addNextIntent(messagingIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        String username;
        try {
            if (UserName == null) {
                String name = UtilsPhone.getContactName(phone);
                if (name != null) {
                    username = name;
                } else {
                    username = phone;
                }
            } else {
                username = UserName;
            }

        } catch (Exception e) {
            AppHelper.LogCat(" " + e.getMessage());
            username = phone;
        }
        mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        if (AppHelper.isAndroid8()) {

            CHANNEL_ID = username;// The id of the channel.
            CharSequence name = WhatsCloneApplication.getInstance().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(CHANNEL_ID, name, IMPORTANCE_HIGH);
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setChannelId(CHANNEL_ID)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);

            mNotificationManager.createNotificationChannel(mChannel);
        } else {

            CHANNEL_ID = username;// The id of the channel.
            mNotifyBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setColor(AppHelper.getColor(mContext, R.color.colorAccent))
                    .setSmallIcon(android.R.drawable.stat_sys_upload);

            //  if (!AppHelper.isActivityRunning(mContext, "activities.messages.MessagesActivity"))
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            mNotifyBuilder.setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS);


        }


        mNotifyBuilder.setAutoCancel(true)
                .setOngoing(false)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setSound(null);
        if (UserName != null && !UserName.equals("null")) {
            mNotifyBuilder.setContentTitle(mContext.getString(R.string.sending_file_to) + UserName);
        } else {
            String name = UtilsPhone.getContactName(phone);
            if (name != null) {
                mNotifyBuilder.setContentTitle(mContext.getString(R.string.sending_file_to) + name);
            } else {
                mNotifyBuilder.setContentTitle(mContext.getString(R.string.sending_file_to) + phone);
            }
        }

        mNotifyBuilder.setProgress(0, 0, true);
        mNotificationManager.notify(messageId, index, mNotifyBuilder.build());
    }

    /**
     * method to get manager for notification
     */
    public boolean getManager() {
        return mNotificationManager != null;

    }


    /***
     * method to cancel  All notification
     *
     */
    public void cancelAllNotification() {

        mNotificationManager.cancelAll();
    }

    /***
     * method to cancel a specific notification
     *
     * @param tag
     */
    public void cancelNotification(String tag) {

        if (mNotificationManager != null)
            mNotificationManager.cancel(tag, index);
    }

    /**
     * method to set badger counter for the app
     */
    public void SetupBadger(Context mContext) {

        AppExecutors.getInstance().diskIO().execute(() -> {




            String DeviceName = android.os.Build.MANUFACTURER;
            String[] DevicesName = {
                    "Sony",
                    "Samsung",
                    "LG",
                    "HTC",
                    "Xiaomi",
                    "ASUS",
                    "ADW",
                    "APEX",
                    "NOVA",
                    "Huawei",
                    "ZUK",
                    "OPPO",
                    "EverythingMe",
                    "ZTE",
                    "KISS",
                    "LaunchTime",
                    "Yandex Launcher"
            };

            for (String device : DevicesName) {
                if (DeviceName.equals(device.toLowerCase())) {
                    try {

                        try {
                            ShortcutBadger.applyCount(mContext.getApplicationContext(), MessagesController.getInstance().getMessagesBadge(PreferenceManager.getInstance().getID(mContext)));
                        } catch (Exception e) {
                            AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                        }
                    } catch (Exception e) {
                        AppHelper.LogCat(" ShortcutBadger Exception " + e.getMessage());
                    }
                    break;
                }
            }
        });
    }
}
