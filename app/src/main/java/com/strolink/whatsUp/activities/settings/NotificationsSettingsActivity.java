package com.strolink.whatsUp.activities.settings;

import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;

/**
 * Created by Abderrahim El imame on 8/17/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class NotificationsSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notifications_settings);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
        View view = LayoutInflater.from(this).inflate(R.layout.shadow_view, root, false);
        root.addView(toolbar, 0);
        root.addView(view, 1);
        root.setBackgroundColor(AppHelper.getColor(this, R.color.colorWhite));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        toolbar.setTitle(R.string.notifications);
        toolbar.setTitleTextColor(Color.WHITE);
        setupMessageRingTone();
        setupMessageLight();
        setupMessageGroupRingTone();
        setupMessageGroupLight();

        setupCallRingTone();

    }

    private void setupMessageGroupLight() {

        ListPreference lightPreferenceMessage = (ListPreference) findPreference(getString(R.string.key_message_group_notifications_settings_light));
        String lightNameSummary = PreferenceSettingsManager.getDefault_message_group_notifications_settings_light(this);
        switch (lightNameSummary) {
            case "#03A9F4":
                lightPreferenceMessage.setSummary("Blue");
                break;
            case "#ffffff":
                lightPreferenceMessage.setSummary("White");
                break;
            case "#f11409":
                lightPreferenceMessage.setSummary("Red");
                break;
            case "#EEFF41":
                lightPreferenceMessage.setSummary("Yellow");
                break;
            case "#0EC654":
                lightPreferenceMessage.setSummary("Green");
                break;
            case "#00FFFF":
                lightPreferenceMessage.setSummary("Cyan");
                break;
            case "#0A7E8C":
                lightPreferenceMessage.setSummary("Metalic");
                break;
        }


        lightPreferenceMessage.setDefaultValue(lightNameSummary);
        lightPreferenceMessage.setOnPreferenceChangeListener((preference, o) -> {
            String stringValue = o.toString();
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            preference.setDefaultValue(stringValue);
            return true;
        });
    }

    private void setupMessageGroupRingTone() {

        RingtonePreference ringtonePreferenceMessage = (RingtonePreference) findPreference(getString(R.string.key_message_group_notifications_settings_tone));
        Ringtone userRingtoneDefault = RingtoneManager.getRingtone(ringtonePreferenceMessage.getContext(), PreferenceSettingsManager.getDefault_message_group_notifications_settings_tone(this));
        ringtonePreferenceMessage.setSummary(userRingtoneDefault.getTitle(this));
        ringtonePreferenceMessage.setDefaultValue(userRingtoneDefault);
        ringtonePreferenceMessage.setOnPreferenceChangeListener((preference, o) -> {
            String stringValue = o.toString();
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.message_notifications_settings_tone_silent);
            } else {

                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
                preference.setDefaultValue(Uri.parse(stringValue));
            }
            return true;
        });
    }


    private void setupMessageLight() {

        ListPreference lightPreferenceMessage = (ListPreference) findPreference(getString(R.string.key_message_notifications_settings_light));
        String lightNameSummary = PreferenceSettingsManager.getDefault_message_notifications_settings_light(this);
        switch (lightNameSummary) {
            case "#03A9F4":
                lightPreferenceMessage.setSummary("Blue");
                break;
            case "#ffffff":
                lightPreferenceMessage.setSummary("White");
                break;
            case "#f11409":
                lightPreferenceMessage.setSummary("Red");
                break;
            case "#EEFF41":
                lightPreferenceMessage.setSummary("Yellow");
                break;
            case "#0EC654":
                lightPreferenceMessage.setSummary("Green");
                break;
            case "#00FFFF":
                lightPreferenceMessage.setSummary("Cyan");
                break;
            case "#0A7E8C":
                lightPreferenceMessage.setSummary("Metalic");
                break;
        }


        lightPreferenceMessage.setDefaultValue(lightNameSummary);
        lightPreferenceMessage.setOnPreferenceChangeListener((preference, o) -> {
            String stringValue = o.toString();
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            preference.setDefaultValue(stringValue);
            return true;
        });
    }

    private void setupMessageRingTone() {

        RingtonePreference ringtonePreferenceMessage = (RingtonePreference) findPreference(getString(R.string.key_message_notifications_settings_tone));
        Ringtone userRingtoneDefault = RingtoneManager.getRingtone(ringtonePreferenceMessage.getContext(), PreferenceSettingsManager.getDefault_message_notifications_settings_tone(this));
        ringtonePreferenceMessage.setSummary(userRingtoneDefault.getTitle(this));
        ringtonePreferenceMessage.setDefaultValue(userRingtoneDefault);
        ringtonePreferenceMessage.setOnPreferenceChangeListener((preference, o) -> {
            String stringValue = o.toString();
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.message_notifications_settings_tone_silent);
            } else {

                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
                preference.setDefaultValue(Uri.parse(stringValue));
            }
            return true;
        });
    }


    private void setupCallRingTone() {

        RingtonePreference ringtonePreferenceCall = (RingtonePreference) findPreference(getString(R.string.key_call_notifications_settings_tone));
        Ringtone userRingtoneDefault = RingtoneManager.getRingtone(ringtonePreferenceCall.getContext(), PreferenceSettingsManager.getDefault_calls_notifications_settings_tone(this));
        ringtonePreferenceCall.setSummary(userRingtoneDefault.getTitle(this));
        ringtonePreferenceCall.setDefaultValue(userRingtoneDefault);
        ringtonePreferenceCall.setOnPreferenceChangeListener((preference, o) -> {
            String stringValue = o.toString();
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.call_notifications_settings_tone_silent);
            } else {

                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
                preference.setDefaultValue(Uri.parse(stringValue));
            }
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
