package com.strolink.whatsUp.helpers.call;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.util.Locale;

/**
 * Created by Abderrahim El imame on 12/21/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallManager {


    /**
     * method to call a user
     */
    public static void callContact(Activity activity, boolean isVideoCall, String userID) {


        UsersModel contactsModel = UsersController.getInstance().getUserById(userID);
        if (isVideoCall) {

            Permissions.with(activity)
                    .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                    .ifNecessary()
                    .withRationaleDialog(String.format(Locale.getDefault(), activity.getString(R.string.to_call_s_app_needs_access_to_your_microphone_and_camera), contactsModel.getUsername()), R.drawable.ic_mic_white_24dp, R.drawable.ic_videocam_white_24dp)
                    .withPermanentDenialDialog(String.format(Locale.getDefault(), activity.getString(R.string.to_call_s_app_needs_access_to_your_microphone_and_camera), contactsModel.getUsername()))
                    .onAllGranted(() -> {

                        if (!isNetworkAvailable(activity)) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                            alert.setMessage(activity.getString(R.string.you_couldnt_call_this_user_network));
                            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                            });
                            alert.setCancelable(false);
                            alert.show();
                        } else {
                            CallingApi.initiateCall(activity, userID, AppConstants.VIDEO_CALL);
                        }
                    })
                    .execute();
        } else {

            Permissions.with(activity)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .ifNecessary()
                    .withRationaleDialog(activity.getString(R.string.to_call_s_app_needs_access_to_your_microphone_and_camera, activity.getString(R.string.app_name)), R.drawable.ic_mic_white_24dp)
                    .withPermanentDenialDialog(activity.getString(R.string.app_needs_the_microphone_and_camera_permissions_in_order_to_call_s, activity.getString(R.string.app_name)))
                    .onAllGranted(() -> {

                        if (!isNetworkAvailable(activity)) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                            alert.setMessage(activity.getString(R.string.you_couldnt_call_this_user_network));
                            alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                            });
                            alert.setCancelable(false);
                            alert.show();
                        } else {
                            CallingApi.initiateCall(activity, userID, AppConstants.VOICE_CALL);
                        }
                    })
                    .execute();
        }




    }

    private static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
