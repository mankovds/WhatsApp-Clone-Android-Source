package com.strolink.whatsUp.activities.images;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;
import com.yalantis.ucrop.UCrop;

import java.io.File;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public abstract class PickerManager {
    public static final int REQUEST_CODE_SELECT_IMAGE = 200;
    public static final int REQUEST_CODE_IMAGE_PERMISSION = 201;
    protected Uri mProcessingPhotoUri;
    private boolean withTimeStamp = true;
    private String folder = null;
    private String imageName;
    protected AppCompatActivity activity;
    private UCrop uCrop;
    protected PickerBuilder.onImageReceivedListener imageReceivedListener;
    protected PickerBuilder.onPermissionRefusedListener permissionRefusedListener;
    private int cropActivityColor = R.color.colorPrimary;

    public PickerManager setOnImageReceivedListener(PickerBuilder.onImageReceivedListener listener) {
        this.imageReceivedListener = listener;
        return this;
    }

    public PickerManager setOnPermissionRefusedListener(PickerBuilder.onPermissionRefusedListener listener) {
        this.permissionRefusedListener = listener;
        return this;
    }

    public PickerManager(AppCompatActivity activity) {
        this.activity = activity;
        this.imageName = activity.getString(R.string.app_name);
    }


    public void pickPhotoWithPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_IMAGE_PERMISSION);
        } else
            sendToExternalApp();
    }

    public void handlePermissionResult(int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted
            sendToExternalApp();

        } else {

            // permission denied
            if (permissionRefusedListener != null)
                permissionRefusedListener.onPermissionRefused();
            activity.finish();
        }
    }


    protected abstract void sendToExternalApp();

    protected Uri getImageFile() {
        String imagePathStr = Environment.getExternalStorageDirectory() + "/" +
                (folder == null ?
                        Environment.DIRECTORY_DCIM + "/" + activity.getString(R.string.app_name) :
                        folder);

        File path = new File(imagePathStr);
        if (!path.exists()) {
            path.mkdir();
        }

        String finalPhotoName = imageName + ".jpg";

        File photo = new File(path, finalPhotoName);
        return Uri.fromFile(photo);
    }

    public void setUri(Uri uri) {

    }

    public void startCropActivity() {
        if (uCrop == null) {
            uCrop = UCrop.of(mProcessingPhotoUri, getImageFile());

            uCrop = uCrop.withAspectRatio(1, 1);//.useSourceImageAspectRatio();
            UCrop.Options options = new UCrop.Options();
            options.setFreeStyleCropEnabled(false);

            options.setToolbarColor(AppHelper.getColor(activity, R.color.colorPrimary));
            options.setStatusBarColor(AppHelper.getColor(activity, R.color.colorPrimaryDark));
            options.setActiveWidgetColor(AppHelper.getColor(activity, R.color.colorAccent));
            uCrop = uCrop.withOptions(options);
        }

        uCrop.start(activity);
    }

    public void handleCropResult(Intent data) {
        AppHelper.LogCat("Readdata."+data);
        Uri resultUri = UCrop.getOutput(data);
        if (imageReceivedListener != null)
            imageReceivedListener.onImageReceived(resultUri);

        activity.finish();
    }


    public PickerManager setActivity(AppCompatActivity activity) {
        this.activity = activity;
        return this;
    }

    public PickerManager setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public PickerManager setCropActivityColor(int cropActivityColor) {
        this.cropActivityColor = cropActivityColor;
        return this;
    }

    public PickerManager withTimeStamp(boolean withTimeStamp) {
        this.withTimeStamp = withTimeStamp;
        return this;
    }

    public PickerManager setImageFolderName(String folder) {
        this.folder = folder;
        return this;
    }

    public PickerManager setCustomizedUcrop(UCrop customizedUcrop) {
        this.uCrop = customizedUcrop;
        return this;
    }
}