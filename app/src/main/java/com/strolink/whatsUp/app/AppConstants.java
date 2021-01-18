package com.strolink.whatsUp.app;


import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.PreferenceManager;

import static com.strolink.whatsUp.app.WhatsCloneApplication.getInstance;

/**
 * Created by Abderrahim on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AppConstants {

    /* debugging constants  for developer */
    public static final String TAG = "benCherif";

    public static final boolean DEBUGGING_MODE = false;

    /**
     * for the application
     */


    public static final boolean ENABLE_CACHE = true;
     static final boolean ENABLE_CRASH_HANDLER = false; // this for the crash activity  you can turn on this so when user get a crash this activity will appear instead of stop the app
    public static final boolean ENABLE_ANIMATIONS = true;// this for activities animations
    public static final String DEFAULT_COUNTRY_CODE = "MA";//this for country code
    public static final String STICKERS_EDITOR_FOLDER_NAME = "stickers";//this for stickers folder name
    public static final int MEMBER_GROUP_LIMIT = 255;//number of group members
    public static final String CLIENT_ID = PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance());
    public static final String INVITE_MESSAGE_SMS = WhatsCloneApplication.getInstance().getString(R.string.Hello_get) + getInstance().getString(R.string.app_name) +   WhatsCloneApplication.getInstance().getString(R.string.so_you_can_easly);// this is the sms to invite  friends
    public static final String JOINED_MESSAGE_SMS = WhatsCloneApplication.getInstance().getString(R.string.hi_i_m_using_app) + getInstance().getString(R.string.app_name);// this is the msg when the user join to the app
    public static final int DatabaseVersion = 5;
    public static final String DatabaseName = getInstance().getString(R.string.app_name) + "_" + PreferenceManager.getInstance().getToken(getInstance()) + "_db";
    /**
     * for toast and snackbar
     */
    public static final int MESSAGE_COLOR_ERROR = R.color.colorOrangeLight;
    public static final int MESSAGE_COLOR_WARNING = R.color.colorOrange;
    public static final int MESSAGE_COLOR_SUCCESS = R.color.colorGreenDark;
    public static final int TEXT_COLOR = R.color.colorWhite;


    /**
     * upload image or video constants
     */
    public static final int UPLOAD_PICTURE_REQUEST_CODE = 1;
    public static final int SELECT_PROFILE_PICTURE = 2;
    public static final int SELECT_PROFILE_CAMERA = 3;

    public static final int SELECT_ADD_NEW_CONTACT = 6;
    public static final int SELECT_COUNTRY = 7;
    public static final int APP_REQUEST_CODE = 8;
    /* messages */
    public static final int PICK_CAMERA_MESSAGES = 9;
    public static final int PICK_GALLERY_MESSAGES = 10;
    public static final int PICK_DOCUMENT_MESSAGES = 11;
    public static final int PICK_AUDIO_MESSAGES = 12;
    public static final int PICK_CONTACT_INFO_MESSAGES = 13;
    public static final int PICK_LOCATION_MESSAGES = 14;
    public static final int PICK_GIF_MESSAGES = 15;
    public static final int PICK_CAMERA_GALLERY_STORY = 16;
    public static final int PICK_REPLY_STORY = 17;

    /**
     * Status constants
     */

    public static final int IS_WAITING = 0;
    public static final int IS_SENT = 1;
    public static final int IS_DELIVERED = 2;
    public static final int IS_SEEN = 3;
    /**
     * images size
     */
    public static final int NOTIFICATIONS_IMAGE_SIZE = 150;
    public static final int ROWS_IMAGE_SIZE = 90;
    public static final int PROFILE_PREVIEW_IMAGE_SIZE = 500;
    public static final int PROFILE_PREVIEW_BLUR_IMAGE_SIZE = 50;
    public static final int PROFILE_IMAGE_SIZE = 500;
    public static final int SETTINGS_IMAGE_SIZE = 150;
    public static final int EDIT_PROFILE_IMAGE_SIZE = 500;
    public static final int MESSAGE_IMAGE_SIZE = 50;
    public static final int PRE_MESSAGE_IMAGE_SIZE = 40;
    public static final int FULL_SCREEN_IMAGE_SIZE = 640;
    public static final int BLUR_RADIUS = 30;

    /**
     * those for EventBus tool
     */
    public static final String EVENT_UPDATE_CONVERSATION_OLD_ROW = "update_message_conversation";
    public static final String EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW = "new_message_conversation_old_row";
    public static final String EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW = "new_message_conversation_new_row";
    public static final String EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW = "new_message_messages_new_row";
    public static final String EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW = "new_group_message_messages_new_row";
    public static final String EVENT_BUS_CALL_NEW_ROW = "new_call_new_row";//
    public static final String EVENT_UPDATE_CALL_OLD_ROW = "update_call_row";
    public static final String EVENT_BUS_DELETE_CALL_ITEM = "delete_call_row";
    public static final String EVENT_BUS_ITEM_IS_ACTIVATED = "ItemIsActivated";
    public static final String EVENT_BUS_MESSAGE_IS_READ = "messages_read";
    public static final String EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS = "new_message_sent_for_conversation";
    public static final String EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS = "messages_seen_for_conversation";
    public static final String EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS = "messages_delivered_for_conversation";
    public static final String EVENT_BUS_DELETE_CONVERSATION_ITEM = "deleteConversation";
    public static final String EVENT_BUS_DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY = "DELETE_CONVERSATION_FINISH_MESSAGES_ACTIVITY ";
    public static final String EVENT_BUS_EXIT_NEW_GROUP = "exitNewGroup";
    public static final String EVENT_BUS_REFRESH_BLOCKED_LIST = "refresh_blocked_list";
    public static final String EVENT_BUS_MESSAGE_COUNTER = "MessagesCounter";
    public static final String EVENT_BUS_SEARCH_QUERY_CHAT = "SEARCH_QUERY_CHAT";
    public static final String EVENT_BUS_START_REFRESH = "START_REFRESH";
    public static final String EVENT_BUS_STOP_REFRESH = "STOP_REFRESH";
    public static final String EVENT_BUS_SEARCH_QUERY_CALLS = "SEARCH_QUERY_CALLS";
    public static final String EVENT_BUS_SEARCH_QUERY_CONTACTS = "SEARCH_QUERY_CONTACTS";
    public static final String EVENT_BUS_SESSION_EXPIRED = "SESSION_EXPIRED";
    public static final String EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES = "messages_delivered_for_messages";
    public static final String EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES = "messages_seen_for_messages";
    public static final String EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES = "new_message_sent_for_messages";
    public static final String EVENT_BUS_IMAGE_PROFILE_PATH = "ImageProfilePath";
    public static final String EVENT_BUS_UPDATE_CURRENT_SATUS = "updateCurrentStatus";
    public static final String EVENT_BUS_UPDATE_STATUS = "update";
    public static final String EVENT_BUS_DELETE_STATUS = "deleteStatus";
    public static final String EVENT_BUS_NEW_GROUP_NOTIFICATION = "NewGroupNotification";
    public static final String EVENT_BUS_NEW_USER_NOTIFICATION = "NewUserNotification";
    public static final String EVENT_BUS_REFRESH_MESSAGEGS = "REFRESH_MESSAGEGS";
    public static final String EVENT_BUS_CREATE_GROUP = "createGroup";
    public static final String EVENT_BUS_DELETE_GROUP = "deleteGroup";
    public static final String EVENT_BUS_EXIT_GROUP = "exitGroup";
    public static final String EVENT_BUS_EXIT_THIS_GROUP = "exitThisGroup";
    public static final String EVENT_BUS_ADD_MEMBER = "addMember";
    public static final String EVENT_BUS_REMOVE_CREATE_MEMBER = "removeCreateMember";
    public static final String EVENT_BUS_ADD_CREATE_MEMBER = "addCreateMember";
    public static final String EVENT_BUS_DELETE_CREATE_MEMBER = "deleteCreateMember";
    public static final String EVENT_BUS_UPDATE_GROUP_NAME = "updateGroupName";
    public static final String EVENT_BUS_PATH_GROUP = "PathGroup";
    public static final String EVENT_BUS_NEW_CONTACT_ADDED = "newContactAdded";
    public static final String EVENT_BUS_UPLOAD_MESSAGE_FILES = "uploadMessageFiles";


    public static final String EVENT_BUS_NEW_MESSAGE_STORY_OWN_ROW = "new_story_stories_own_row";
    public static final String EVENT_BUS_NEW_MESSAGE_STORY_OLD_ROW = "new_story_stories_old_row";
    public static final String EVENT_BUS_NEW_MESSAGE_STORY_NEW_ROW = "new_story_stories_new_row";
    public static final String EVENT_BUS_NEW_STORY_OWNER_NEW_ROW = "NEW_STORY_OWNER_NEW_ROW";
    public static final String EVENT_BUS_NEW_STORY_OWNER_OLD_ROW = "NEW_STORY_OWNER_OLD_ROW";
    public static final String EVENT_BUS_DELETE_STORIES_ITEM = "deleteStories";

    //new
    public static final String EVENT_BUS_IMAGE_PROFILE_UPDATED = "profileImageUpdated";
    public static final String EVENT_BUS_IMAGE_GROUP_UPDATED = "groupImageUpdated";
    public static final String EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED = "mine_profileImageUpdated";
    public static final String EVENT_BUS_USERNAME_PROFILE_UPDATED = "updateUserName";
    public static final String EVENT_BUS_START_CONVERSATION = "startConversation";
    public static final String EVENT_BUS_ACTION_MODE_STARTED = "actionModeStarted";
    public static final String EVENT_BUS_ACTION_MODE_DESTROYED = "actionModeDestroyed";
    public static final String EVENT_BUS_ACTION_MODE_FINISHED = "actionModeFinished";

    public static final String EVENT_BUS_SMS_CODE = "_SMS_CODE";
    public static final String EVENT_BUS_ERROR = "confirm_error";

    public static final String EVENT_BUS_MEMBER_TYPING = "event_member_typing";
    public static final String EVENT_BUS_MEMBER_STOP_TYPING = "event_member_stop_typing";
    public static final String EVENT_BUS_USER_TYPING = "event_user_typing";
    public static final String EVENT_BUS_USER_STOP_TYPING = "event_user_stop_typing";

    public static final String EVENT_BUS_UPDATE_USER_STATE = "updateUserState";
    public static final String EVENT_BUS_REFRESH_CONTACTS = "REFRESH_CONTACTS";

    /**
     * Media type
     */

    public static final String RECEIVED_IMAGE = "RECEIVED_IMAGE";
    public static final String SENT_IMAGE = "SENT_IMAGE";
    public static final String PROFILE_IMAGE = "PROFILE_IMAGE";

    public static final String RECEIVED_IMAGE_FROM_SERVER = "RECEIVED_IMAGE_FROM_SERVER";
    public static final String SENT_IMAGE_FROM_SERVER = "SENT_IMAGE_FROM_SERVER";
    public static final String PROFILE_IMAGE_FROM_SERVER = "PROFILE_IMAGE_FROM_SERVER";

    // FOR DOWNLOAD FILES
    public static final String SENT_AUDIO = "SENT_AUDIO";
    public static final String SENT_TEXT = "SENT_TEXT";
    public static final String SENT_IMAGES = "SENT_IMAGES";
    public static final String SENT_VIDEOS = "SENT_VIDEOS";
    public static final String SENT_DOCUMENTS = "SENT_DOCUMENTS";
    public static final String SENT_GIF = "SENT_GIF";
    /**
     * for cache
     */
    public static final String DATA_CACHED = "DATA_CACHED";
    public static final String GROUP = "gp";
    public static final String USER = "ur";
    public static final String PROFILE_PREVIEW = "prp";
    public static final String FULL_PROFILE = "fp";
    public static final String SETTINGS_PROFILE = "spr";
    public static final String EDIT_PROFILE = "epr";
    public static final String ROW_PROFILE = "rpr";
    public static final String ROW_MESSAGES_BEFORE = "rmebe";
    public static final String ROW_WALLPAPER = "rwppr";
    public static final String ROW_MESSAGES_AFTER = "rmeaf";


    public static String EXPORT_REALM_FILE_NAME = getInstance().getString(R.string.app_name) + "_msgstore.realm";


    /**
     * call
     */
    //for calls list
    public static final String VIDEO_CALL = "VIDEO_CALL";
    public static final String VOICE_CALL = "VOICE_CALL";


    /**
     * messages states
     */
    public static final String NORMAL_STATE = "normal";
    public static final String CREATE_STATE = "created";
    public static final String LEFT_STATE = "left";
    public static final String ADD_STATE = "added";
    public static final String REMOVE_STATE = "removed";
    public static final String ADMIN_STATE = "admin";
    public static final String MEMBER_STATE = "member";
    public static final String EDITED_STATE = "edited";


    /**
     * files types
     */
    public static final String MESSAGES_IMAGE = "Image";
    public static final String MESSAGES_VIDEO = "Video";
    public static final String MESSAGES_GIF = "Gif";
    public static final String MESSAGES_AUDIO = "Audio";
    public static final String MESSAGES_DOCUMENT = "Document";
    /**
     * documents file type
     */
    public static final String MESSAGES_DOCUMENT_PDF = "PDF";
    public static final String MESSAGES_DOCUMENT_DOC = "DOC";
    public static final String MESSAGES_DOCUMENT_PPT = "PPT";
    public static final String MESSAGES_DOCUMENT_EXCEL = "EXCEL";


    /**
     * Chat socket constants (be careful if u want to change them !!)
     */

    public static final String EVENT_BUS_USER_IS_ONLINE = "event_user_is_online";
    public static final String EVENT_BUS_USER_IS_OFFLINE = "event_user_is_offline";
    public static final String EVENT_BUS_USER_LAST_SEEN = "event_user_is_last_seen";
    //user  constants:
    public static final int STATUS_USER_TYPING = 11;
    public static final int STATUS_USER_STOP_TYPING = 12;
    public static final int STATUS_USER_CONNECTED = 13;
    public static final int STATUS_USER_DISCONNECTED = 14;
    public static final int STATUS_USER_LAST_SEEN = 15;


    /**
     * media  constants class
     */
    public final class MediaConstants {
        public static final int MAX_STORY_DURATION_FOR_IMAGE = 5000;
        public static final int MAX_STORY_DURATION = 30;
        /**
         * editor extra
         */
        public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";
        public static final String EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH";
        public static final String EXTRA_ORIGINAL = "EXTRA_ORIGINAL";
        public static final String EXTRA_CROP_RECT = "EXTRA_CROP_RECT";
        public static final String EXTRA_EDITED_PATH = "EXTRA_EDITED_PATH";
        public static final String EXTRA_FOR_STORY = "EXTRA_FOR_STORY";
        public static final String EXTRA_EDITOR_MESSAGE = "EXTRA_EDITOR_MESSAGE";

    }

    /**
     * stories  constants class
     */
    public final class StoriesConstants {
        public static final int STORIES_PRIVACY_ALL_CONTACTS = 0;
        public static final int STORIES_PRIVACY_ALL_CONTACTS_EXCEPT = 1;
        public static final int STORIES_PRIVACY_ALL_CONTACTS_WITH = 2;

    }


    /**
     * Mqtt constants class
     */
    public final class MqttConstants {
        //messages
        public static final String NEW_USER_MESSAGE_TO_SERVER = "new_user_message_to_server";
        public static final String UPDATE_STATUS_OFFLINE_MESSAGES_AS_DELIVERED = "update_status_offline_messages_as_delivered";
        public static final String UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN = "update_status_offline_messages_as_seen";
        public static final String UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED = "update_status_offline_messages_as_finished";
        public static final String UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED = "update_status_offline_messages_exist_as_finished";
        //messages

        //stories
        public static final String NEW_USER_STORY_TO_SERVER = "new_user_story_to_server";
        public static final String UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED = "update_status_offline_stories_as_expired";
        public static final String UPDATE_STATUS_OFFLINE_STORY_AS_SEEN = "update_status_offline_stories_as_seen";
        public static final String UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED = "update_status_offline_stories_as_finished";
        public static final String UPDATE_STATUS_OFFLINE_STORY_EXIST_AS_FINISHED = "update_status_offline_stories_exist_as_finished";
        //stories

        //calls

        public static final String NEW_USER_CALL_TO_SERVER = "new_user_call_to_server";
        public static final String UPDATE_STATUS_OFFLINE_CALL_AS_SEEN = "update_status_offline_calls_as_seen";
        public static final String UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED = "update_status_offline_calls_as_finished";
        public static final String UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED = "update_status_offline_calls_exist_as_finished";

        //calls


        //for notify user changes


        public static final String PUBLISH_USER_STATUS = "user_status_update";
        public static final String PUBLISH_WHATSCLONE_GENERAL = "whatsclone_topic";
        public static final int MQTT_TYPING_QOS = 0;
        public static final int MQTT_SUBSCRIBE_QOS = 2;
        public static final int MQTT_MESSAGE_QOS = 0;
        public static final int MQTT_MESSAGE_STATUS_QOS = 0;
        public static final int MQTT_GROUP_QOS = 0;
    }


}
