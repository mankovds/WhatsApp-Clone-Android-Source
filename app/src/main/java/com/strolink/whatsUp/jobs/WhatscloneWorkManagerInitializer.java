package com.strolink.whatsUp.jobs;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.ForegroundRuning;

/**
 * Created by Abderrahim El imame on 2020-01-01.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class WhatscloneWorkManagerInitializer extends WhasCloneContentProvider {

    @Override
    public boolean onCreate() {
        WorkManager.initialize(getContext(), new Configuration.Builder().build());
       // ForegroundRuning.init(this);
        ForegroundRuning.get().addListener(new ForegroundRuning.Listener() {
            @Override
            public void onBecameForeground() {
                AppHelper.LogCat("onBecameForeground ");
                WhatsCloneApplication.getInstance().initializerApplication();
            }

            @Override
            public void onBecameBackground() {
                AppHelper.LogCat("onBecameBackground ");
                WorkJobsManager.getInstance().cancelAllJob();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                WhatsCloneApplication.getInstance().initializerApplication();
            }
        });
        return true;
    }
}

abstract class WhasCloneContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}