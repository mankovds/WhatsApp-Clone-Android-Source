package com.strolink.whatsUp.jobs.files;

import android.content.Context;

import androidx.annotation.Nullable;

import com.strolink.whatsUp.models.UploadInfo;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.DownloadCallbacks;
import com.strolink.whatsUp.interfaces.UploadCallbacks;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.util.List;


/**
 * Created by Abderrahim El imame on 10/16/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PendingFilesTask {


    public static void initUploadListener(String messageId) {
        PendingFilesTask.addFile(messageId);

    }

    public static void initUploadListener(String messageId, UploadCallbacks waitingListener) {
        PendingFilesTask.addFile(messageId, waitingListener);

    }

    public static void initDownloadListener(String messageId, DownloadCallbacks waitingListener) {
        PendingFilesTask.addFile(messageId, waitingListener);
    }


    public static void updateUploadListener(@Nullable UploadCallbacks uploadCallbacks) {
        UploadSingleFileToServerWorker.updateUploadCallbacks(uploadCallbacks);

    }

    public static void updateDownloadListener(@Nullable DownloadCallbacks downloadCallbacks) {
        DownloadSingleFileFromServerWorker.updateDownloadCallbacks(downloadCallbacks);
    }


    /**
     * add new file for download service
     *
     * @param messageId
     * @param waitingListener
     */
    private static void addFile(String messageId, DownloadCallbacks waitingListener) {

        if (!containsFile(messageId)) {
            AppHelper.LogCat(" not containsFile");


            UploadInfo uploadInfo = new UploadInfo();
            uploadInfo.setUploadId(messageId);

            UsersController.getInstance().insertFile(uploadInfo);

            AppHelper.LogCat(" file added  ");
            WorkJobsManager.getInstance().downloadFileToServer(messageId);
            DownloadSingleFileFromServerWorker.setDownloadCallbacks(waitingListener);
        } else {
            AppHelper.LogCat("  containsFile");
            WorkJobsManager.getInstance().downloadFileToServer(messageId);
            DownloadSingleFileFromServerWorker.setDownloadCallbacks(waitingListener);
        }


    }

    /**
     * add new file for upload service
     *
     * @param messageId
     * @param waitingListener
     */
    private static void addFile(String messageId, UploadCallbacks waitingListener) {

        if (!containsFile(messageId)) {
            AppHelper.LogCat(" not containsFile");


            UploadInfo uploadInfo = new UploadInfo();
            uploadInfo.setUploadId(messageId);

            UsersController.getInstance().insertFile(uploadInfo);

            AppHelper.LogCat(" file added  ");
            WorkJobsManager.getInstance().uploadFileToServer(messageId);
            UploadSingleFileToServerWorker.setUploadCallbacks(waitingListener);


        } else {
            AppHelper.LogCat("  containsFile");
            WorkJobsManager.getInstance().uploadFileToServer(messageId);
            UploadSingleFileToServerWorker.setUploadCallbacks(waitingListener);

        }


    }

    /**
     * add new file for upload service
     *
     * @param storyId
     */
    private static void addFile(String storyId) {

        if (!containsFile(storyId)) {
            AppHelper.LogCat(" not containsFile");


            UploadInfo uploadInfo = new UploadInfo();
            uploadInfo.setUploadId(storyId);

            UsersController.getInstance().insertFile(uploadInfo);

            AppHelper.LogCat(" file added  ");
            WorkJobsManager.getInstance().uploadFileStoryToServer(storyId);


        } else {
            AppHelper.LogCat("  containsFile");
            WorkJobsManager.getInstance().uploadFileStoryToServer(storyId);


        }


    }

    /**
     * method to remove file
     *
     * @param messageId this is the first parameter for removeFile  method
     * @param isFinish
     */
    public static void removeFile(String messageId, boolean isFinish, boolean isDownload) {


        try {
            if (containsFile(messageId)) {


                UploadInfo uploadInfo = UsersController.getInstance().getSingleFileById(messageId);

                UsersController.getInstance().deleteFile(uploadInfo);


                if (!isFinish) {
                    if (isDownload) {
                        WorkJobsManager.getInstance().cancelJob(DownloadSingleFileFromServerWorker.TAG + "_" + messageId);
                    } else {
                        WorkJobsManager.getInstance().cancelJob(UploadSingleFileToServerWorker.TAG + "_" + messageId);
                    }
                }

            } else {

                if (!isFinish) {
                    if (isDownload) {
                        WorkJobsManager.getInstance().cancelJob(DownloadSingleFileFromServerWorker.TAG + "_" + messageId);
                    } else {
                        WorkJobsManager.getInstance().cancelJob(UploadSingleFileToServerWorker.TAG + "_" + messageId);
                    }
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }
    }

    public static void removeFile(String messageId) {

        try {
            if (containsFile(messageId)) {


                UploadInfo uploadInfo = UsersController.getInstance().getSingleFileById(messageId);
                UsersController.getInstance().deleteFile(uploadInfo);

                WorkJobsManager.getInstance().cancelJob(UploadSingleStoryFileToServerWorker.TAG + "_" + messageId);

            } else {

                WorkJobsManager.getInstance().cancelJob(UploadSingleStoryFileToServerWorker.TAG + "_" + messageId);
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }
    }

    public static boolean containsFile(String uploadId) {
//        AppHelper.LogCat("containsFile " + uploadId);
        return UsersController.getInstance().containsFile(uploadId) != 0;
    }

    /**
     * method to clear files
     */

    public static void clearFiles(Context mContext) {

        List<UploadInfo> uploadInfoList = UsersController.getInstance().getAllFilesById();
        for (UploadInfo uploadInfo : uploadInfoList)
            UsersController.getInstance().deleteFile(uploadInfo);

    }
}
