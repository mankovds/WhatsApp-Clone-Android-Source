package com.strolink.whatsUp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.AppCompatImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.main.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/2/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CrashActivity extends BaseActivity {
    private static Boolean ENABLE_RESTART = false;
    @BindView(R.id.opsText)
    TextView opsText;
    @BindView(R.id.emoText)
    AppCompatImageView emoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        ButterKnife.bind(this);

        final Animation animTranslate_progress_layout = AnimationUtils.loadAnimation(this, R.anim.crash_anim);
        animTranslate_progress_layout.setDuration(1200);
        emoText.startAnimation(animTranslate_progress_layout);
        final Animation animTranslate_text_layout = AnimationUtils.loadAnimation(this, R.anim.crash_anim);
        animTranslate_text_layout.setDuration(1400);
        opsText.startAnimation(animTranslate_text_layout);
        ENABLE_RESTART = true;
        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(this::restartMain, SPLASH_TIME_OUT);
    }


    @Override
    public void onRestart() {
        super.onRestart();
        restartMain();
    }

    /**
     * method to restart Main Activity
     */
    public void restartMain() {
        if (ENABLE_RESTART) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        } else {
            finish();
        }
        ENABLE_RESTART = false;
    }
}