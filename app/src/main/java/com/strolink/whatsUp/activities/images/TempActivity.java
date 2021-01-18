package com.strolink.whatsUp.activities.images;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.helpers.AppHelper;

import static com.yalantis.ucrop.UCrop.REQUEST_CROP;

/**
 * Created by Abderrahim El imame on 1/11/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class TempActivity extends BaseActivity {

    PickerManager pickerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.pickerManager = GlobalHolder.getInstance().getPickerManager();
        if (this.pickerManager != null) {
            this.pickerManager.setActivity(TempActivity.this);
            this.pickerManager.pickPhotoWithPermission();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

     /*   if (resultCode != RESULT_OK) {
            finish();
            return;
        }*/
        AppHelper.LogCat("requestCode " + requestCode);

        switch (requestCode) {
            case PickerManager.REQUEST_CODE_SELECT_IMAGE:
                Uri uri;
                if (data != null)
                    uri = data.getData();
                else
                    uri = pickerManager.getImageFile();

                pickerManager.setUri(uri);
                pickerManager.startCropActivity();
                AppHelper.LogCat("pickerManager " + uri.toString());
                break;
            case REQUEST_CROP:
                try {

                    if (data != null) {
                        pickerManager.handleCropResult(data);
                    } else
                        finish();
                } catch (Exception e) {
                    AppHelper.LogCat("Exception " + e.getMessage());
                }
                break;
        }
        //   AppHelper.LogCat("requestCode w "+ UCrop.getError(data));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PickerManager.REQUEST_CODE_IMAGE_PERMISSION)
            pickerManager.handlePermissionResult(grantResults);
        else
            finish();

    }

}