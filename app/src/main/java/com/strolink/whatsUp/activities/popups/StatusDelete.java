package com.strolink.whatsUp.activities.popups;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.presenters.users.StatusPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusDelete extends Activity {

    @BindView(R.id.deleteStatus)
    TextView deleteStatus;

    private StatusPresenter mStatusPresenter;
    private String statusID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_status_delete);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("statusID") && getIntent().getExtras().getString("statusID") != null) {
            statusID = getIntent().getExtras().getString("statusID");
        }
        mStatusPresenter = new StatusPresenter(this);
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.deleteStatus)
    public void DeleteStatus() {
        mStatusPresenter.DeleteStatus(statusID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatusPresenter.onDestroy();
    }
}
