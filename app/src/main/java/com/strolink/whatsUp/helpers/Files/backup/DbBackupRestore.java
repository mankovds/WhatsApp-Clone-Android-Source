package com.strolink.whatsUp.helpers.Files.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.jobs.WorkJobsManager;

import java.io.File;

/**
 * Created by Abderrahim El imame on 10/31/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class DbBackupRestore {

    private final static String TAG = DbBackupRestore.class.getName();


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private static void checkStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permissionWrite = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionWrite != PackageManager.PERMISSION_GRANTED || permissionRead != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static void deleteData(Activity activity) {
        checkStoragePermissions(activity);

        try {
            WhatsCloneApplication.getInstance().DeleteDatabaseInstance();
            //db file has been deleted.
            AppHelper.LogCat(" db file has been deleted.");
        } catch (Exception ex) {
            ex.printStackTrace();
            //No Realm file to  remove.
            AppHelper.LogCat(" Failed to delete db file or there is No db file to  remove");
        }
        clearApplicationData(activity);
        PreferenceManager.getInstance().clearPreferences(activity);
        WorkJobsManager.getInstance().cancelAllJob();
    }

    public static void deleteData(Context activity) {

        try {
            WhatsCloneApplication.getInstance().DeleteDatabaseInstance();
            //db file has been deleted.
            AppHelper.LogCat(" db file has been deleted.");
        } catch (Exception ex) {
            ex.printStackTrace();
            //No db file to  remove.
            AppHelper.LogCat(" Failed to delete db file or there is No db file to  remove");
        }
        clearApplicationData(activity);
        PreferenceManager.getInstance().clearPreferences(activity);
    }


    private static void clearApplicationData(Context mContext) {
        File cache = mContext.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib") /*&& !s.equals("files")*/) {
                    boolean deleted = deleteDir(new File(appDir, s));
                    if (!deleted) {
                        AppHelper.LogCat("Cached not deleted ");
                    } else {
                        AppHelper.LogCat("Cached deleted");
                        AppHelper.LogCat("File /data/data/" + mContext.getPackageName() + "/" + s + " DELETED");
                    }

                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static String getMessageLastId() {
        return UtilsTime.getCurrentISOTime();
    }

    public static String getStoryLastId() {
        return UtilsTime.getCurrentISOTime();
    }

    public static String getConversationLastId() {
        return UtilsTime.getCurrentISOTime();
    }

    public static String getCallLastId() {
        return UtilsTime.getCurrentISOTime();
    }

    public static String getMemberLastId() {
        return UtilsTime.getCurrentISOTime();
    }


    public static String getCallInfoLastId() {
        return UtilsTime.getCurrentISOTime();
    }

    public static String getBlockUserLastId() {
        return UtilsTime.getCurrentISOTime();
    }
}
