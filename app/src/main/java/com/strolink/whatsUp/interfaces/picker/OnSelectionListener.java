package com.strolink.whatsUp.interfaces.picker;

import android.view.View;

import com.strolink.whatsUp.models.MediaPicker;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public interface OnSelectionListener {
    void OnClick(MediaPicker MediaPicker, View view, int position);

    void OnLongClick(MediaPicker mediaPicker, View view, int position);
}
