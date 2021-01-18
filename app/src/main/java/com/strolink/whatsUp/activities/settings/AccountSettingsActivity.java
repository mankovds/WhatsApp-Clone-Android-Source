package com.strolink.whatsUp.activities.settings;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.BlockedContactsActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class AccountSettingsActivity extends BaseActivity {
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.delete_account_text)
    TextView deleteAccText;
    @BindView(R.id.blocked_contacts_text)
    TextView blockedContacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        ButterKnife.bind(this);
        setupToolbar();

    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }



/*

    @SuppressWarnings("unused")
    @OnClick(R.id.change_number)
    public void launchChangeNumber() {
        AppHelper.LaunchActivity(this, ChangeNumberActivity.class);
    }

*/

    @SuppressWarnings("unused")
    @OnClick(R.id.delete_account)
    public void launchDeleteAccount() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_message_delete_account);
        builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
            AppHelper.LaunchActivity(this, DeleteAccountActivity.class);
        });
        builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

        });

        builder.show();

    }


    @SuppressWarnings("unused")
    @OnClick(R.id.blocked_contacts)
    public void launchBlockedContacts() {
        AppHelper.LaunchActivity(this, BlockedContactsActivity.class);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
