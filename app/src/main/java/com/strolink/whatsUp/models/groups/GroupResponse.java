package com.strolink.whatsUp.models.groups;


import java.util.List;

/**
 * Created by Abderrahim El imame on 01/11/2015.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */
public class GroupResponse {
    private boolean success;
    private String message;
    private String groupId;
    private String groupImage;
    private List<MembersModelJson> membersModels;

    public GroupResponse() {

    }

    public List<MembersModelJson> getMembersModels() {
        return membersModels;
    }

    public void setMembersModels(List<MembersModelJson> membersModels) {
        this.membersModels = membersModels;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
