package com.strolink.whatsUp.app;

import com.strolink.whatsUp.BuildConfig;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EndPoints { ;


    /**
     * Authentication
     */
    public static final String JOIN = "auth/Join";
    public static final String VERIFY_USER = "auth/verifyUser";
    public static final String RESEND_REQUEST_SMS = "auth/resend";
    public static final String DELETE_ACCOUNT = "auth/deleteAccount";
    public static final String DELETE_ACCOUNT_CONFIRMATION = "auth/deleteConfirmation";


    /**
     * Users
     */

    public static final String CHECK_NETWORK = "users/checkNetwork";
    public static final String CHECK_CONTACTS = "users/all";
    public static final String GET_CONTACT = "users/get/{userId}";
    public static final String EDIT_USER_NAME = "users/editName";
    public static final String GET_APPLICATION_SETTINGS = "users/getAppSettings";
    public static final String BLOCK_USER = "users/blockUser/{userId}";
    public static final String UN_BLOCK_USER = "users/unBlockUser/{userId}";
    public static final String EDIT_USER_IMAGE = "users/editImage";


    /**
     * Calls
     */

    public static final String SAVE_NEW_CALL = "users/saveNewCall";
    public static final String DELETE_CALL = "users/deleteCall/{callId}";
    /**
     * Stories
     */
    public static final String CREATE_STORY = "users/createStory";
    public static final String DELETE_STORY = "users/deleteStory/{storyId}";

    /**
     * Groups
     */
    public static final String CREATE_GROUP = "groups/createGroup";
    public static final String GET_GROUP = "groups/get/{groupId}";
    public static final String REMOVE_MEMBER_FROM_GROUP = "groups/removeMemberFromGroup";
    public static final String MAKE_MEMBER_AS_ADMIN = "groups/makeMemberAdmin";
    public static final String REMOVE_MEMBER_AS_ADMIN = "groups/makeAdminMember";
    public static final String ADD_MEMBERS_TO_GROUP = "groups/addMembersToGroup";
    public static final String EDIT_GROUP_NAME = "groups/editGroupName";
    public static final String EDIT_GROUP_IMAGE = "groups/editGroupImage";
    public static final String EXIT_GROUP = "groups/exitGroup";
    public static final String DELETE_GROUP = "groups/deleteGroup/{groupId}/{userId}/{conversationId}";


    /**
     * Status
     */

    public static final String EDIT_STATUS = "users/addStatus";
    public static final String UPDATE_STATUS = "users/setCurrentStatus";
    public static final String DELETE_STATUS = "users/deleteStatus/{statusId}";
    public static final String DELETE_ALL_STATUS = "users/deleteAllStatus";
    public static final String GET_STATUS = "users/getStatus/{userId}";


    /**
     * Messages
     */
    public static final String SEND_MESSAGE = "chats/sendMessage";
    public static final String DELETE_MESSAGE = "chats/deleteMessage/{messageId}";
    public static final String DELETE_CONVERSATION = "chats/deleteConversation/{conversationId}";


    /**
     * Files Upload URL
     */


    public static final String UPLOAD_USER_IMAGE = "files/user_avatars/{userId}";
    public static final String UPLOAD_GROUP_IMAGE = "files/group_avatars";
    public static final String UPLOAD_MESSAGES_IMAGE = "files/images_files";
    public static final String UPLOAD_MESSAGES_VIDEO = "files/videos_files";
    public static final String UPLOAD_MESSAGES_AUDIO = "files/audios_files";
    public static final String UPLOAD_MESSAGES_DOCUMENT = "files/documents_files";
    public static final String UPLOAD_MESSAGES_GIF = "files/gifs_files";


    /**
     * Files Get URL
     */


    public static final String ROWS_GROUP_IMAGE_URL = BuildConfig.BACKEND_BASE_URL + "files/group_avatars/";
    public static final String ROWS_IMAGE_URL = BuildConfig.BACKEND_BASE_URL + "files/user_avatars/";
    public static final String MESSAGE_IMAGE_URL = BuildConfig.BACKEND_BASE_URL + "files/images_files/";
    public static final String MESSAGE_GIF_URL = BuildConfig.BACKEND_BASE_URL + "files/gifs_files/";
    public static final String MESSAGE_VIDEO_URL = BuildConfig.BACKEND_BASE_URL + "files/videos_files/";
    public static final String MESSAGE_DOCUMENT_URL = BuildConfig.BACKEND_BASE_URL + "files/documents_files/";
    public static final String MESSAGE_AUDIO_URL = BuildConfig.BACKEND_BASE_URL + "files/audios_files/";


    /**
     * Files Downloads URL
     */
    public static final String MESSAGE_DOCUMENT_DOWNLOAD_URL = "files/documents_files/";
    public static final String MESSAGE_VIDEO_DOWNLOAD_URL = "files/videos_files/";
    public static final String MESSAGE_AUDIO_DOWNLOAD_URL = "files/audios_files/";
    public static final String MESSAGE_IMAGE_DOWNLOAD_URL = "files/images_files/";
    public static final String MESSAGE_GIF_DOWNLOAD_URL = "files/gifs_files/";

    /**
     * Gif
     */
    public static final String GET_GIF_BASE = "https://api.giphy.com/v1/gifs/";
    public static final String GET_GIFS = "trending";
    public static final String GET_GIFS_SEARCH = "search";


}
