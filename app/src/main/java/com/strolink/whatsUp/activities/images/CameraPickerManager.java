package com.strolink.whatsUp.activities.images;

import android.content.Intent;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CameraPickerManager extends PickerManager {

    public CameraPickerManager(AppCompatActivity activity) {
        super(activity);
    }

    protected void sendToExternalApp() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mProcessingPhotoUri = getImageFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mProcessingPhotoUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }
}