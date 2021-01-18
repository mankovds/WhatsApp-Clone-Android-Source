package com.strolink.whatsUp.helpers.Files;

import android.content.Context;

import com.strolink.whatsUp.interfaces.DownloadCallbacks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;

import static com.strolink.whatsUp.helpers.Files.FilesManager.getFileAudio;
import static com.strolink.whatsUp.helpers.Files.FilesManager.getFileDocument;
import static com.strolink.whatsUp.helpers.Files.FilesManager.getFileGif;
import static com.strolink.whatsUp.helpers.Files.FilesManager.getFileImage;
import static com.strolink.whatsUp.helpers.Files.FilesManager.getFileVideo;
import static com.strolink.whatsUp.helpers.Files.FilesManager.isFileAudioExists;
import static com.strolink.whatsUp.helpers.Files.FilesManager.isFileDocumentsExists;
import static com.strolink.whatsUp.helpers.Files.FilesManager.isFileGifExists;
import static com.strolink.whatsUp.helpers.Files.FilesManager.isFileImagesExists;
import static com.strolink.whatsUp.helpers.Files.FilesManager.isFileVideosExists;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class DownloadFilesHelper {


    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private String type;
    private String Identifier;
    private ResponseBody mFile;
    private boolean isStopped = false;
    private WeakReference<DownloadCallbacks> mWaitingListenerWeakReference;
    private String messageId;

    public DownloadFilesHelper(ResponseBody mFile, String Identifier, String type, WeakReference<DownloadCallbacks> mWaitingListenerWeakReference) {
        this.mFile = mFile;
        this.Identifier = Identifier;
        this.type = type;
        this.mWaitingListenerWeakReference = mWaitingListenerWeakReference;
    }

    public void setmWaitingListenerWeakReference(WeakReference<DownloadCallbacks> mWaitingListenerWeakReference) {
        this.mWaitingListenerWeakReference = mWaitingListenerWeakReference;
    }

    public void setIsStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }


    public boolean writeResponseBodyToDisk(Context mActivity) {
        try {
            try {
                if (isFileGifExists(mActivity, Identifier)) {
                    getFileGif(mActivity, Identifier).delete();
                    return false;
                } else if (isFileImagesExists(mActivity, Identifier)) {
                    getFileImage(mActivity, Identifier).delete();
                    return false;
                } else if (isFileVideosExists(mActivity, Identifier)) {
                    getFileVideo(mActivity, Identifier).delete();
                    return false;
                } else if (isFileAudioExists(mActivity, Identifier)) {
                    getFileAudio(mActivity, Identifier).delete();
                    return false;
                } else if (isFileDocumentsExists(mActivity, Identifier)) {
                    getFileDocument(mActivity, Identifier).delete();
                    return false;
                }
            } catch (Exception ignored) {
            }

            File downloadedFile = null;
            switch (type) {
                case "image":
                    downloadedFile = new File(FilesManager.getFileImagesPath(mActivity, Identifier));
                    break;
                case "gif":
                    downloadedFile = new File(FilesManager.getFileGifPath(mActivity, Identifier));
                    break;
                case "video":
                    downloadedFile = new File(FilesManager.getFileVideoPath(mActivity, Identifier));
                    break;
                case "audio":
                    downloadedFile = new File(FilesManager.getFileAudioPath(mActivity, Identifier));
                    break;
                case "document":
                    downloadedFile = new File(FilesManager.getFileDocumentsPath(mActivity, Identifier));
                    break;
            }

            if (downloadedFile != null) {
                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[DEFAULT_BUFFER_SIZE];

                    long fileSize = mFile.contentLength();
                    long fileSizeDownloaded = 0;

                    inputStream = mFile.byteStream();
                    try {
                        outputStream = new FileOutputStream(downloadedFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
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
                        if (mWaitingListenerWeakReference != null) {
                            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                            if (!isStopped) {

                                if (listener != null) {
                                    listener.onUpdateDownload((int) (100 * fileSizeDownloaded / fileSize), type, messageId);
                                }
                            } else {
                                if (listener != null) {
                                    listener.onErrorDownload(type, messageId);
                                }
                                break;
                            }
                        }
                        // AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);
                    }

                    try {
                        outputStream.flush();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                } catch (Exception e) {

                    if (mWaitingListenerWeakReference != null) {
                        DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                        if (listener != null) {
                            listener.onErrorDownload(type, messageId);
                        }
                    }
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
            } else {

                if (mWaitingListenerWeakReference != null) {
                    DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                    if (listener != null) {
                        listener.onErrorDownload(type, messageId);
                    }
                }
                return false;
            }


        } catch (Exception e) {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (listener != null) {
                    listener.onErrorDownload(type, messageId);
                }
            }
            return false;
        }
    }

}
