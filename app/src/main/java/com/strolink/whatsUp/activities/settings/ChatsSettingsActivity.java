package com.strolink.whatsUp.activities.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.popups.WallpaperSelector;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class ChatsSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.chats_settings);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
        View view = LayoutInflater.from(this).inflate(R.layout.shadow_view, root, false);
        root.addView(toolbar, 0);
        root.addView(view, 1);
        root.setBackgroundColor(AppHelper.getColor(this, R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        initializer();
        toolbar.setTitle(R.string.chats);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initializer() {
        Preference preference1 = findPreference(getString(R.string.key_wallpaper_message));
        preference1.setOnPreferenceClickListener(preference -> {
            AppHelper.LaunchActivity(this, WallpaperSelector.class);
            return true;
        });
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
