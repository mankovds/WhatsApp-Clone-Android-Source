package com.strolink.whatsUp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;

import org.eclipse.paho.android.service.MqttAndroidClient;


/**
 * Created by Abderrahim El imame on 1/31/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    boolean isMainActivity = false;
    public static int mSessionDepth = 0;
    private MqttAndroidClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mSessionDepth++;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mSessionDepth > 0) {
            mSessionDepth--;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void finish() {
        super.finish();
        if (isMainActivity) {
            isMainActivity = false;
        } else {
            overridePendingTransitionExit();
        }

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    public void setMainActivity(boolean mainActivity) {
        isMainActivity = mainActivity;
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        if (AppConstants.ENABLE_ANIMATIONS)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        if (AppConstants.ENABLE_ANIMATIONS)
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}