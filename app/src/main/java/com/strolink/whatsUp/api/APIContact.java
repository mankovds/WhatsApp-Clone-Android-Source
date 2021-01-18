package com.strolink.whatsUp.api;


import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.status.StatusModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.giph.model.GiphyResponse;
import com.strolink.whatsUp.models.NetworkModel;
import com.strolink.whatsUp.models.SettingsResponse;
import com.strolink.whatsUp.models.auth.JoinModelResponse;
import com.strolink.whatsUp.models.auth.LoginModel;
import com.strolink.whatsUp.models.calls.CallSaverModel;
import com.strolink.whatsUp.models.messages.UpdateMessageModel;
import com.strolink.whatsUp.models.stories.CreateStoryModel;
import com.strolink.whatsUp.models.users.EditUser;
import com.strolink.whatsUp.models.users.contacts.BlockResponse;
import com.strolink.whatsUp.models.users.contacts.SyncContacts;
import com.strolink.whatsUp.models.users.status.EditStatus;
import com.strolink.whatsUp.models.users.status.NewStatus;
import com.strolink.whatsUp.models.users.status.StatusResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public interface APIContact {

    /**
     * method to  syncing all contacts
     *
     * @param listString this is parameter for  contacts method
     * @return this is return value
     */
    @POST(EndPoints.CHECK_CONTACTS)
    Observable<List<UsersModel>> contacts(@Body SyncContacts listString);

    /**
     * method to get contact info
     *
     * @param userId this is parameter for  contact method
     * @return this is return value
     */
    @GET(EndPoints.GET_CONTACT)
    Observable<UsersModel> getUser(@Path("userId") String userId);


    /**
     * method to get  user  status
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_STATUS)
    Observable<List<StatusModel>> status(@Path("userId") String userId);

    /**
     * method to delete user status
     *
     * @param statusId this is parameter for  delete status method
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_STATUS)
    Observable<StatusResponse> deleteStatus(@Path("statusId") String statusId);


    /**
     * method to delete all user status
     *
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_ALL_STATUS)
    Observable<StatusResponse> deleteAllStatus();

    /**
     * method to update user status
     *
     * @param editStatus this is parameter for  update status method
     * @return this is return value
     */

    @PUT(EndPoints.UPDATE_STATUS)
    Observable<StatusResponse> updateStatus(@Body EditStatus editStatus);

    /**
     * method to edit user status
     *
     * @param newStatus this is parameter for  editStatus method
     * @return this is return value
     */

    @POST(EndPoints.EDIT_STATUS)
    Observable<StatusResponse> editStatus(@Body NewStatus newStatus);

    /**
     * method to edit username
     *
     * @param editUser this is parameter for  editUsername method
     * @return this is return value
     */
    @PUT(EndPoints.EDIT_USER_NAME)
    Observable<StatusResponse> editUsername(@Body EditUser editUser);


    /**
     * method to edit user image
     *
     * @param editUser this is parameter for  editUsername method
     * @return this is return value
     */
    @POST(EndPoints.EDIT_USER_IMAGE)
    Observable<StatusResponse> editUserImage(@Body EditUser editUser);


    /**
     * Block user
     *
     * @param userId This is the  parameter to follow method
     * @return return value
     */
    @GET(EndPoints.BLOCK_USER)
    Observable<BlockResponse> block(@Path("userId") String userId);

    /**
     * UnBlock user
     *
     * @param userId This is the  parameter to unFollow method
     * @return return value
     */
    @GET(EndPoints.UN_BLOCK_USER)
    Observable<BlockResponse> unBlock(@Path("userId") String userId);


    /**
     * method to delete account
     *
     * @param loginModel this is parameter for  deleteAccount method
     * @return this is return value
     */
    @POST(EndPoints.DELETE_ACCOUNT)
    Observable<JoinModelResponse> deleteAccount(@Body LoginModel loginModel);

    /**
     * method to verify the user code
     *
     * @param code this is parameter for verifyUser method
     * @return this is what method will return
     */
    @FormUrlEncoded
    @POST(EndPoints.DELETE_ACCOUNT_CONFIRMATION)
    Observable<StatusResponse> deleteAccountConfirmation(@Field("code") String code,@Field("phone") String phone);


    /**
     * method to get video ads info
     *
     * @return this is return value
     */
    @GET(EndPoints.GET_APPLICATION_SETTINGS)
    Observable<SettingsResponse> getAppSettings();


    /**
     * method to check network state
     *
     * @return this is return value
     */
    @GET(EndPoints.CHECK_NETWORK)
    Observable<NetworkModel> checkNetwork();


    @POST(EndPoints.SEND_MESSAGE)
    Observable<StatusResponse> sendMessage(@Body UpdateMessageModel updateMessageModel);

    @DELETE(EndPoints.DELETE_MESSAGE)
    Observable<StatusResponse> deleteMessage(@Path("messageId") String messageId);

    @DELETE(EndPoints.DELETE_CONVERSATION)
    Observable<StatusResponse> deleteConversation(@Path("conversationId") String conversationId);


    @GET(EndPoints.GET_GIFS)
    Observable<GiphyResponse> getGiphy(@Query("api_key") String api_key, @Query("offset") int offset, @Query("limit") int limit);

    @GET(EndPoints.GET_GIFS_SEARCH)
    Observable<GiphyResponse> getGiphy(@Query("api_key") String api_key, @Query("offset") int offset, @Query("limit") int limit, @Query("q") String q);


    /**
     * method to create story
     *
     * @return this is return value
     */
    @POST(EndPoints.CREATE_STORY)
    Observable<StatusResponse> createStory(@Body CreateStoryModel createStoryModel);


    @DELETE(EndPoints.DELETE_STORY)
    Observable<StatusResponse> deleteStory(@Path("storyId") String storyId);


    @POST(EndPoints.SAVE_NEW_CALL)
    Observable<StatusResponse> saveNewCall(@Body CallSaverModel callSaverModel);


}
