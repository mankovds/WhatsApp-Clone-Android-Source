package com.strolink.whatsUp.activities.status;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.presenters.users.StatusPresenter;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditStatusActivity extends BaseActivity implements InputGeneralPanel.Listener {

    @BindView(R.id.cancelStatus)
    TextView cancelStatusBtn;

    @BindView(R.id.OkStatus)
    TextView OkStatusBtn;

    @BindView(R.id.layout_container)
    LinearLayout container;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;
    private EmojiPopup emojiPopup;


    private String statusID;
    private StatusPresenter statusPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            String oldStatus = getIntent().getStringExtra("currentStatus");
            statusID = getIntent().getExtras().getString("statusID");
            composeText.setText(oldStatus);
        }
        initializerView();
        statusPresenter = new StatusPresenter(this);

        composeText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String insertedStatus = UtilsString.escapeJava(composeText.getText().toString().trim());
                AppHelper.showDialog(this, getString(R.string.adding_new_status));
                statusPresenter.EditCurrentStatus(insertedStatus, statusID);
            }
            return false;
        });


    }



    /**
     * method to initialize the view
     */
    private void initializerView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_status_activity_title);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String insertedStatus = UtilsString.escapeJava(composeText.getText().toString());
            AppHelper.showDialog(this, getString(R.string.adding_new_status));
            statusPresenter.EditCurrentStatus(insertedStatus, statusID);
        });


        inputPanel.setListener(this);

        emojiPopup = EmojiPopup.Builder.fromRootView(container).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEmojiToggle() {

        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) emojiPopup.dismiss();
        else
            super.onBackPressed();
    }
}
