package com.strolink.whatsUp.activities.profile;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.presenters.users.EditProfilePresenter;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class EditUsernameActivity extends BaseActivity implements InputGeneralPanel.Listener {

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


    private String oldName;
    private EditProfilePresenter mEditProfilePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        initializerView();

        mEditProfilePresenter = new EditProfilePresenter(this, true);
        mEditProfilePresenter.onCreate();
        if (getIntent().getExtras() != null) {
            oldName = getIntent().getStringExtra("currentUsername");
        }
        composeText.setText(oldName);

    }
    /**
     * method to initialize the view
     */
    private void initializerView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        cancelStatusBtn.setOnClickListener(v -> finish());
        OkStatusBtn.setOnClickListener(v -> {
            String newUsername = composeText.getText().toString().trim();
            if (newUsername.isEmpty()) {
                AppHelper.CustomToast(this, getString(R.string.username_required));
            } else {
                try {
                    mEditProfilePresenter.editCurrentName(newUsername, false, true);
                } catch (Exception e) {
                    AppHelper.LogCat("Edit  name  Exception " + e.getMessage());
                }
            }

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
