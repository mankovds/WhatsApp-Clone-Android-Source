package com.strolink.whatsUp.helpers.Files;

import androidx.annotation.NonNull;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.interfaces.UploadCallbacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by Abderrahim El imame on 7/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UploadFilesHelper extends RequestBody {


    private File mFile;

    private String mimeType;
    private String mType;
    private String messageId;
    private WeakReference<UploadCallbacks> mWaitingListenerWeakReference;
    private boolean isStopped = false;
    private FileInputStream fileInputStream;
    private static final int DEFAULT_BUFFER_SIZE = 2048;


    public UploadFilesHelper(final File mFile, String mimeType, String mType, WeakReference<UploadCallbacks> mWaitingListenerWeakReference) {
        this.mFile = mFile;
        this.mimeType = mimeType;
        this.mType = mType;
        this.mWaitingListenerWeakReference = mWaitingListenerWeakReference;
    }


    public UploadFilesHelper(final File mFile, String mimeType, String mType,
                             WeakReference<UploadCallbacks> mWaitingListenerWeakReference,
                             String messageId) {
        this.mFile = mFile;
        this.mimeType = mimeType;
        this.mType = mType;
        this.mWaitingListenerWeakReference = mWaitingListenerWeakReference;
        this.messageId = messageId;
    }

    public void setmWaitingListenerWeakReference(WeakReference<UploadCallbacks> mWaitingListenerWeakReference) {
        this.mWaitingListenerWeakReference = mWaitingListenerWeakReference;
    }

    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
        try {

            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mimeType);

    }


    @Override
    public long contentLength() {
        return mFile.length();
    }


    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength;


        if (mFile != null) {
            fileLength = mFile.length();
            fileInputStream = new FileInputStream(mFile);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long uploaded = 0;
            try {
                int read;
                while ((read = fileInputStream.read(buffer)) != -1) {

                    uploaded += read;
                    sink.write(buffer, 0, read);
                    if (mWaitingListenerWeakReference != null) {
                        UploadCallbacks listener = mWaitingListenerWeakReference.get();
                        if (!isStopped) {

                            if (listener != null) {
                                listener.onUpdate((int) (100 * uploaded / fileLength), mType, messageId);
                                NotificationsManager.getInstance().updateUpDownNotification(WhatsCloneApplication.getInstance(), messageId, (int) (100 * uploaded / fileLength));
                            }
                        } else {
                            if (listener != null) {
                                listener.onError(mType, messageId);
                                NotificationsManager.getInstance().cancelNotification(messageId);
                            }
                            break;
                        }
                    }

                }
            } catch (Exception e) {
                AppHelper.LogCat("EOFException " + e.getMessage());

                if (mWaitingListenerWeakReference != null) {
                    UploadCallbacks listener = mWaitingListenerWeakReference.get();
                    if (listener != null) {
                        listener.onError(mType, messageId);
                        NotificationsManager.getInstance().cancelNotification(messageId);
                    }
                }
            } finally {
                fileInputStream.close();
            }
        } else {
            if (mWaitingListenerWeakReference != null) {
                UploadCallbacks listener = mWaitingListenerWeakReference.get();
                if (listener != null) {
                    listener.onError(mType, messageId);
                }
            }
        }


    }

}
