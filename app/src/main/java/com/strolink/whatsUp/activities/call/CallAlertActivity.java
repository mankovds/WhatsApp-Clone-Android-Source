package com.strolink.whatsUp.activities.call;

import android.os.Bundle;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 5/30/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class CallAlertActivity extends BaseActivity {


    @BindView(R.id.couldnt_msg)
    TextView couldntMsg;
    @BindView(R.id.finishBtn)
    TextView finishBtn;
    @BindView(R.id.couldnt_txt)
    TextView couldntTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_alert);
        ButterKnife.bind(this);

    }



    @SuppressWarnings("unused")
    @OnClick(R.id.finishBtn)
    void finishActivity() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}