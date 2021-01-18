package com.strolink.whatsUp.activities.images;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.strolink.whatsUp.R;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class ImagePickerManager extends PickerManager {

    public ImagePickerManager(AppCompatActivity activity) {
        super(activity);
    }

    protected void sendToExternalApp() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_picture)), REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void setUri(Uri uri) {
        mProcessingPhotoUri = uri;
    }

}