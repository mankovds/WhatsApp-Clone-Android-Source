package com.strolink.whatsUp.jobs.utils;

import android.content.Context;

import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.jobs.files.PendingFilesTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 * Created by Abderrahim El imame on 10/21/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class DownloadHelper {

    private Context context;
    private String messageId;
    private ResponseBody responseBody;
    private String Identifier;
    private String type;
    private Listener listener;

    public DownloadHelper(Context context, String messageId, ResponseBody responseBody, String Identifier, String type, Listener listener) {
        this.context = context;
        this.messageId = messageId;
        this.responseBody = responseBody;
        this.Identifier = Identifier;
        this.type = type;
        this.listener = listener;
    }

    public static void writeResponseBodyToDisk(Context context, InputStream inputStream, String Identifier, String type) {


        if (isFileGifExists(context, Identifier)) {
            getFileGif(context, Identifier).delete();

        } else if (isFileImagesExists(context, Identifier)) {
            getFileImage(context, Identifier).delete();
        } else if (isFileVideosExists(context, Identifier)) {
            getFileVideo(context, Identifier).delete();
        } else if (isFileAudioExists(context, Identifier)) {
            getFileAudio(context, Identifier).delete();

        } else if (isFileDocumentsExists(context, Identifier)) {
            getFileDocument(context, Identifier).delete();
        }


        File downloadedFile = null;
        switch (type) {
            case "image":
                downloadedFile = new File(FilesManager.getFileImagesPath(context, Identifier));
                break;
            case "gif":
                downloadedFile = new File(FilesManager.getFileGifPath(context, Identifier));
                break;
            case "video":
                downloadedFile = new File(FilesManager.getFileVideoPath(context, Identifier));
                break;
            case "audio":
                downloadedFile = new File(FilesManager.getFileAudioPath(context, Identifier));
                break;
            case "document":
                downloadedFile = new File(FilesManager.getFileDocumentsPath(context, Identifier));
                break;
        }

        try {
            FileOutputStream out = new FileOutputStream(downloadedFile);
            byte[] buffer = new byte[1024 * 128];
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean writeResponseBodyToDisk() {


        if (isFileGifExists(context, Identifier)) {
            getFileGif(context, Identifier).delete();
            return false;
        } else if (isFileImagesExists(context, Identifier)) {
            getFileImage(context, Identifier).delete();
            return false;
        } else if (isFileVideosExists(context, Identifier)) {
            getFileVideo(context, Identifier).delete();
            return false;
        } else if (isFileAudioExists(context, Identifier)) {
            getFileAudio(context, Identifier).delete();
            return false;
        } else if (isFileDocumentsExists(context, Identifier)) {
            getFileDocument(context, Identifier).delete();
            return false;
        }


        File downloadedFile = null;
        switch (type) {
            case "image":
                downloadedFile = new File(FilesManager.getFileImagesPath(context, Identifier));
                break;
            case "gif":
                downloadedFile = new File(FilesManager.getFileGifPath(context, Identifier));
                break;
            case "video":
                downloadedFile = new File(FilesManager.getFileVideoPath(context, Identifier));
                break;
            case "audio":
                downloadedFile = new File(FilesManager.getFileAudioPath(context, Identifier));
                break;
            case "document":
                downloadedFile = new File(FilesManager.getFileDocumentsPath(context, Identifier));
                break;
        }


        if (downloadedFile != null) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[1024 * 128];

                long fileSize = responseBody.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = responseBody.byteStream();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    fileSizeDownloaded += read;
                    AppHelper.LogCat("file download: " + fileSizeDownloaded + " of " + fileSize);
                    if (PendingFilesTask.containsFile(messageId)) {
                        if (listener != null)
                            listener.onRequestProgress(fileSizeDownloaded, fileSize);
                    } else {
                        listener = null;
                        return false;
                    }


                }

                try {
                    outputStream.flush();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                if (fileSizeDownloaded == fileSize) {
                    return true;
                } else {
                    return false;
                }


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
            return false;
        }
    }


    public interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength);
    }
}
