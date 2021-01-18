package com.strolink.whatsUp.activities.images;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PickerBuilder {
    public static final int SELECT_FROM_GALLERY = 0;
    public static final int SELECT_FROM_CAMERA = 1;
    private AppCompatActivity activity;
    private onPermissionRefusedListener permissionRefusedListener;
    protected onImageReceivedListener imageReceivedListener;
    private PickerManager pickerManager;

    public PickerBuilder(AppCompatActivity activity, int type) {
        this.activity = activity;
        pickerManager = (type == PickerBuilder.SELECT_FROM_GALLERY) ? new ImagePickerManager(activity) : new CameraPickerManager(activity);

    }

    public interface onPermissionRefusedListener {
        void onPermissionRefused();
    }

    public interface onImageReceivedListener {
        void onImageReceived(Uri imageUri);
    }


    public void start() {
        Intent intent = new Intent(activity, TempActivity.class);
        activity.startActivity(intent);

        GlobalHolder.getInstance().setPickerManager(pickerManager);

    }

    public PickerBuilder setOnImageReceivedListener(PickerBuilder.onImageReceivedListener listener) {
        pickerManager.setOnImageReceivedListener(listener);
        return this;
    }

    public PickerBuilder setOnPermissionRefusedListener(PickerBuilder.onPermissionRefusedListener listener) {
        pickerManager.setOnPermissionRefusedListener(listener);
        return this;
    }

    public PickerBuilder setCropScreenColor(int cropScreenColor) {
        pickerManager.setCropActivityColor(cropScreenColor);
        return this;
    }

    public PickerBuilder setImageName(String imageName) {
        pickerManager.setImageName(imageName);
        return this;
    }

    public PickerBuilder withTimeStamp(boolean withTimeStamp) {
        pickerManager.withTimeStamp(withTimeStamp);
        return this;
    }

    public PickerBuilder setImageFolderName(String folderName) {
        pickerManager.setImageFolderName(folderName);
        return this;
    }

    public PickerBuilder setCustomizedUcrop(UCrop ucrop) {
        pickerManager.setCustomizedUcrop(ucrop);
        return this;
    }

}