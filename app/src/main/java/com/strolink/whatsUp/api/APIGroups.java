package com.strolink.whatsUp.api;


import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.groups.GroupModelResponse;
import com.strolink.whatsUp.models.groups.EditGroup;
import com.strolink.whatsUp.models.groups.GroupRequest;
import com.strolink.whatsUp.models.groups.GroupResponse;
import com.strolink.whatsUp.models.groups.MemberRequest;
import com.strolink.whatsUp.models.users.status.StatusResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public interface APIGroups {


    /**
     * method to create group
     *
     * @return this is return value
     */
    @POST(EndPoints.CREATE_GROUP)
    Observable<GroupResponse> createGroup(@Body GroupRequest groupRequest);

    /**
     * method to add members to group
     *
     * @return this is return value
     */
    @POST(EndPoints.ADD_MEMBERS_TO_GROUP)
    Observable<GroupResponse> addMembers(@Body MemberRequest memberRequest);

    /**
     * method to remove member from group
     *
     * @return this is return value
     */

    @POST(EndPoints.REMOVE_MEMBER_FROM_GROUP)
    Observable<GroupResponse> removeMember(@Body MemberRequest memberRequest);

    /**
     * method to make  member as admin
     *
     * @return this is return value
     */
    @POST(EndPoints.MAKE_MEMBER_AS_ADMIN)
    Observable<GroupResponse> makeMemberAdmin(@Body MemberRequest memberRequest);

    /**
     * method to make  admin as member
     *
     * @return this is return value
     */

    @POST(EndPoints.REMOVE_MEMBER_AS_ADMIN)
    Observable<GroupResponse> makeAdminMember(@Body MemberRequest memberRequest);

    /**
     * method to get group information
     *
     * @param groupId this is  parameter for  getGroup method
     * @return this is return value
     */
    @GET(EndPoints.GET_GROUP)
    Observable<GroupModelResponse> getGroup(@Path("groupId") String groupId);

    /**
     * method to delete group
     *
     * @param groupId        this is  parameter for  deleteGroup method
     * @param userId         this is  parameter for  deleteGroup method
     * @param conversationId this is  parameter for  deleteGroup method
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_GROUP)
    Observable<GroupResponse> deleteGroup(@Path("groupId") String groupId, @Path("userId") String userId, @Path("conversationId") String conversationId);

    /**
     * method to exit a group
     *
     * @param memberRequest this is  parameter for  exitGroup method
     * @return this is return value
     */
    @POST(EndPoints.EXIT_GROUP)
    Observable<GroupResponse> exitGroup(@Body MemberRequest memberRequest);

    /**
     * method to edit group name
     *
     * @param editGroup this is parameter for  editGroupName method
     * @return this is return value
     */
    @POST(EndPoints.EDIT_GROUP_NAME)
    Observable<StatusResponse> editGroupName(@Body EditGroup editGroup);

    /**
     * method to edit group image
     *
     * @return this is return value
     */

    @POST(EndPoints.EDIT_GROUP_IMAGE)
    Observable<StatusResponse> editGroupImage(@Body EditGroup editGroup);
}
