package com.strolink.whatsUp.helpers;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.strolink.whatsUp.R;

/**
 * Created by Abderrahim El imame on 11/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class RateHelper {

    private static final String PREF_LAUNCH_COUNT = "launch_count";
    private static final String PREF_EVENT_COUNT = "event_count";
    private static final String PREF_RATE_CLICKED = "rateclicked";
    private static final String PREF_DONT_SHOW = "dontshow";
    private static final String PREF_DATE_REMINDER_PRESSED = "date_reminder_pressed";
    private static final String PREF_DATE_FIRST_LAUNCHED = "date_firstlaunch";
    private static final String PREF_APP_VERSION_CODE = "versioncode";

    public static void appLaunched(Context mContext) {
        boolean testMode = mContext.getResources().getBoolean(R.bool.rate_helper_test_mode);
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName() + ".rateHelper", 0);
        if (!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        if (testMode) {
            showRateDialog(mContext, editor);
            return;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0);

        // Get events counter
        long event_count = prefs.getLong(PREF_EVENT_COUNT, 0);

        // Get date of first launch
        long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCHED, 0);

        // Get reminder date pressed
        long date_reminder_pressed = prefs.getLong(PREF_DATE_REMINDER_PRESSED, 0);

        try {
            int appVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            if (prefs.getInt(PREF_APP_VERSION_CODE, 0) != appVersionCode) {
                //Reset the launch and event counters to help assure users are rating based on the latest version.
                launch_count = 0;
                event_count = 0;
                editor.putLong(PREF_EVENT_COUNT, event_count);
            }
            editor.putInt(PREF_APP_VERSION_CODE, appVersionCode);
        } catch (Exception e) {
            //do nothing
        }

        launch_count++;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCHED, date_firstLaunch);
        }

        // Wait at least n days or m events before opening
        if (launch_count >= mContext.getResources().getInteger(R.integer.rate_helper_launches_until_prompt)) {
            long millisecondsToWait = mContext.getResources().getInteger(R.integer.rate_helper_days_until_prompt) * 24 * 60 * 60 * 1000L;
            if (System.currentTimeMillis() >= (date_firstLaunch + millisecondsToWait) || event_count >= mContext.getResources().getInteger(R.integer.rate_helper_events_until_prompt)) {
                if (date_reminder_pressed == 0) {
                    showRateDialog(mContext, editor);
                } else {
                    long remindMillisecondsToWait = mContext.getResources().getInteger(R.integer.rate_helper_days_before_reminding) * 24 * 60 * 60 * 1000L;
                    if (System.currentTimeMillis() >= (remindMillisecondsToWait + date_reminder_pressed)) {
                        showRateDialog(mContext, editor);
                    }
                }
            }
        }

        editor.apply();
    }

    public static void rateApp(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName() + ".rateHelper", 0);
        SharedPreferences.Editor editor = prefs.edit();
        rateApp(mContext, editor);
    }

    public static void significantEvent(Context mContext) {
        boolean testMode = mContext.getResources().getBoolean(R.bool.rate_helper_test_mode);
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName() + ".rateHelper", 0);
        if (!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {
            return;
        }

        long event_count = prefs.getLong(PREF_EVENT_COUNT, 0);
        event_count++;
        prefs.edit().putLong(PREF_EVENT_COUNT, event_count).apply();
    }

    private static void rateApp(Context mContext, final SharedPreferences.Editor editor) {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(mContext.getString(R.string.rate_helper_google_play_url), mContext.getPackageName()))));
        if (editor != null) {
            editor.putBoolean(PREF_RATE_CLICKED, true);
            editor.commit();
        }
    }

    @SuppressLint("NewApi")
    private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        String appName = mContext.getString(R.string.app_name);
        final Dialog dialog = new Dialog(mContext);

        if (android.os.Build.VERSION.RELEASE.startsWith("1.") || android.os.Build.VERSION.RELEASE.startsWith("2.0") || android.os.Build.VERSION.RELEASE.startsWith("2.1")) {
            //No dialog title on pre-froyo devices
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else if (mContext.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_LOW || mContext.getResources().getDisplayMetrics().densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
            Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int rotation = display.getRotation();
            if (rotation == 90 || rotation == 270) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            } else {
                dialog.setTitle(String.format(mContext.getString(R.string.rate_title), appName));
            }
        } else {
            dialog.setTitle(String.format(mContext.getString(R.string.rate_title), appName));
        }

        LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.rate_helper, null);

        TextView tv = (TextView) layout.findViewById(R.id.message);
        tv.setText(String.format(mContext.getString(R.string.rate_message), appName));

        TextView rateButton = (TextView) layout.findViewById(R.id.rate);
        rateButton.setText(String.format(mContext.getString(R.string.rate), appName));
        rateButton.setOnClickListener(v -> {
            rateApp(mContext, editor);
            dialog.dismiss();
        });

        TextView rateLaterButton = (TextView) layout.findViewById(R.id.rateLater);
        rateLaterButton.setText(mContext.getString(R.string.rate_later));
        rateLaterButton.setOnClickListener(v -> {
            if (editor != null) {
                editor.putLong(PREF_DATE_REMINDER_PRESSED, System.currentTimeMillis());
                editor.commit();
            }
            dialog.dismiss();
        });

        TextView cancelButton = (TextView) layout.findViewById(R.id.cancel);
        cancelButton.setText(mContext.getString(R.string.rate_cancel));
        cancelButton.setOnClickListener(v -> {
            if (editor != null) {
                editor.putBoolean(PREF_DONT_SHOW, true);
                editor.commit();
            }
            dialog.dismiss();
        });

        dialog.setContentView(layout);
        dialog.show();

    }
}
