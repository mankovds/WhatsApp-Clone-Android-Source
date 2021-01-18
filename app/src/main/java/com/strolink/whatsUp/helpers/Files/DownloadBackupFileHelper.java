package com.strolink.whatsUp.helpers.Files;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.interfaces.DownloadCallbacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class DownloadBackupFileHelper {

    private DownloadCallbacks mDownloadCallbacks;
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private String type;
    private ResponseBody mFile;

    public DownloadBackupFileHelper(final ResponseBody mFile, String type, final DownloadCallbacks mDownloadCallbacks) {
        this.mFile = mFile;
        this.type = type;
        this.mDownloadCallbacks = mDownloadCallbacks;

    }


    public boolean writeResponseBodyToDisk(Activity mActivity) {
        try {

            File futureStudioIconFile = new File(FilesManager.getFilesCachePath(mActivity), AppConstants.EXPORT_REALM_FILE_NAME);

            futureStudioIconFile.delete();


            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[DEFAULT_BUFFER_SIZE];

                long fileSize = mFile.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = mFile.byteStream();
                try {
                    outputStream = new FileOutputStream(futureStudioIconFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler(Looper.getMainLooper());
                while (true) {
                    int read = 0;
                    try {
                        read = inputStream.read(fileReader);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                    if (read == -1) {
                        break;
                    }

                    try {
                        outputStream.write(fileReader, 0, read);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                    fileSizeDownloaded += read;
                    // update progress on UI thread
                    handler.post(new Updater(fileSizeDownloaded, fileSize));
                    //AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                try {
                    outputStream.flush();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                return true;
            } catch (Exception e) {
                return false;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    private class Updater implements Runnable {
        private long mUploaded;
        private long mTotal;

        Updater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }


        @Override
        public void run() {
            //mDownloadCallbacks.onUpdate((int) (100 * mUploaded / mTotal), type);
        }
    }

}
