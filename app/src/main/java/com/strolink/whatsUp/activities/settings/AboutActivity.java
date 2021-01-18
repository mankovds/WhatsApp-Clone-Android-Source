package com.strolink.whatsUp.activities.settings;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.WaveHelper;
import com.strolink.whatsUp.ui.WaveView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 8/19/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AboutActivity extends BaseActivity {

    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.about_enjoy_it)
    TextView aboutEnjoyIt;
    @BindView(R.id.about_app_name)
    TextView appName;

    @BindView(R.id.wave)
    WaveView waveView;

    private WaveHelper mWaveHelper;


    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        waveView.setBorder(mBorderWidth, mBorderColor);
        waveView.setWaveColor(AppHelper.getColor(this, R.color.colorBehindWave), AppHelper.getColor(this, R.color.colorFrontWave));
        waveView.setShapeType(WaveView.ShapeType.SQUARE);
        mWaveHelper = new WaveHelper(waveView);


        String appVersion = AppHelper.getAppVersion(this);
        version.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.app_version), appVersion));

    }


    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWaveHelper.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveHelper.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
