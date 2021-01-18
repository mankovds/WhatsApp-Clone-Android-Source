package com.strolink.whatsUp.jobs;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.jobs.calls.SendSeenCallToServer;
import com.strolink.whatsUp.jobs.files.DownloadSingleFileFromServerWorker;
import com.strolink.whatsUp.jobs.files.UploadSingleFileToServerWorker;
import com.strolink.whatsUp.jobs.files.UploadSingleStoryFileToServerWorker;
import com.strolink.whatsUp.jobs.messages.SendDeliveredGroupStatusToServer;
import com.strolink.whatsUp.jobs.messages.SendDeliveredStatusToServer;
import com.strolink.whatsUp.jobs.messages.SendSeenGroupStatusToServer;
import com.strolink.whatsUp.jobs.messages.SendSeenStatusToServer;
import com.strolink.whatsUp.jobs.messages.SendSingleMessageToServerWorker;
import com.strolink.whatsUp.jobs.messages.UpdateMessageStatus;
import com.strolink.whatsUp.jobs.stories.SendDeletedStoryToServer;
import com.strolink.whatsUp.jobs.stories.SendSeenStoryToServer;
import com.strolink.whatsUp.jobs.stories.SendSingleStoryToServer;
import com.strolink.whatsUp.jobs.users.InitializerApplicationService;
import com.strolink.whatsUp.jobs.users.SyncingContactsWithServerWorker;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.StoriesController;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.util.concurrent.TimeUnit;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class WorkJobsManager {

    private static volatile WorkJobsManager Instance = null;

    private WorkJobsManager() {

    }

    public static WorkJobsManager getInstance() {

        WorkJobsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (WorkJobsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new WorkJobsManager();
                }
            }
        }
        return localInstance;

    }


    /**
     * Job to send seen status to other user
     *
     * @param senderId
     */
    public void sendSeenStatusToServer(String senderId, String conversationId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("senderId", senderId);
        dataBuilder.putString("conversationId", conversationId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenStatusToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenStatusToServer.TAG + "_" + conversationId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        //  WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendSeenStatusToServer.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }

    public void updateMessage(String messageId, String recipientId, int status) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", messageId);
        dataBuilder.putString("recipientId", recipientId);
        dataBuilder.putInt("status", status);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateMessageStatus.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(UpdateMessageStatus.TAG + "_" + messageId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    /**
     * Job to send delivered status when current was offline
     */
    public void sendDeliveredStatusToServer() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeliveredStatusToServer.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeliveredStatusToServer.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        //WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendDeliveredStatusToServer.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }

    /**
     * Job to send seen group status to other user
     *
     * @param groupId
     */
    public void sendSeenGroupStatusToServer(String groupId, String conversationId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("groupId", groupId);
        dataBuilder.putString("conversationId", conversationId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenGroupStatusToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenGroupStatusToServer.TAG + "_" + conversationId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        //WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendSeenGroupStatusToServer.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }

    /**
     * Job to send delivered group status when current was offline
     */
    public void sendDeliveredGroupStatusToServer() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeliveredGroupStatusToServer.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeliveredGroupStatusToServer.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendDeliveredGroupStatusToServer.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }

    public void sendSingleMessageToServerWorker(String messageId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", messageId);
        OneTimeWorkRequest
                workRequest = new OneTimeWorkRequest.Builder(SendSingleMessageToServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSingleMessageToServerWorker.TAG + "_" + messageId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    public void sendUserMessagesToServer() {
        MessagesController.getInstance().unSentMessages();
    }

    public void sendSingleStoryToServerWorker(long storyId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putLong("storyId", storyId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSingleStoryToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSingleStoryToServer.TAG + "_" + storyId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    public void sendUserStoriesToServer() {
        StoriesController.getInstance().unSentStories();
    }

    public void downloadFileToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DownloadSingleFileFromServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(DownloadSingleFileFromServerWorker.TAG + "_" + uploadId)
                .build();
        //   WorkManager.getInstance().enqueueUniqueWork(DownloadSingleFileFromServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.KEEP, workRequest).enqueue();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    public void uploadFileToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadSingleFileToServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(UploadSingleFileToServerWorker.TAG + "_" + uploadId)
                .build();
        // WorkManager.getInstance().enqueueUniqueWork(UploadSingleFileToServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.KEEP, workRequest).enqueue();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }


    public void uploadFileStoryToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("storyId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadSingleStoryFileToServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(UploadSingleStoryFileToServerWorker.TAG + "_" + uploadId)
                .build();
        // WorkManager.getInstance().enqueueUniqueWork(UploadSingleFileToServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.KEEP, workRequest).enqueue();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }


    public void cancelJob(String tag) {
        // WorkManager.getInstance().cancelUniqueWork(tag);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).cancelAllWorkByTag(tag);
    }

    public void cancelAllJob() {
        AppHelper.LogCat("cancelAllJob");
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).cancelAllWork();
        pruneWork();//cleanup all the completed jobs from the database.
    }

    public void pruneWork() {
        AppHelper.LogCat("pruneWork");
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).pruneWork();
    }


    /**
     * Job to send seen call to other user
     *
     * @param callId
     */
    public void sendSeenCallToServer(String callId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("callId", callId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenCallToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenCallToServer.TAG + "_" + callId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendSeenCallToServer.TAG + "_" + callId, ExistingWorkPolicy.KEEP, workRequest);
    }

    /**
     * Job to send seen story to other user
     *
     * @param senderId
     */
    public void sendSeenStoryToServer(String senderId, String storyId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("senderId", senderId);
        dataBuilder.putString("storyId", storyId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenStoryToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenStoryToServer.TAG + "_" + storyId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendSeenStoryToServer.TAG + "_" + storyId, ExistingWorkPolicy.KEEP, workRequest);
    }

    public void sendDeletedStoryToServer() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeletedStoryToServer.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeletedStoryToServer.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SendDeletedStoryToServer.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }

    public void expireStoryWorker(String storyId, String date) {
        //calculate difference between 24 with the story date

        DateTime startDateValue = new DateTime();
        DateTime endDateValue = UtilsTime.getCorrectDate(date).plusHours(24);
        int hours = Hours.hoursBetween(startDateValue, endDateValue).getHours();
        int minutes = Minutes.minutesBetween(startDateValue, endDateValue).getMinutes();
        int seconds = Seconds.secondsBetween(startDateValue, endDateValue).getSeconds();

        AppHelper.LogCat("left time " + hours + ":" + minutes + ":" + seconds);// TODO: 1/10/19 i will need it for destorying messages

     /*   Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("storyId", storyId);
        PeriodicWorkRequest.Builder dayWorkBuilder = new PeriodicWorkRequest.Builder(ExpireStoryWorker.class, seconds, TimeUnit.SECONDS, 5, TimeUnit.MINUTES);
        dayWorkBuilder.setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build());
        // Add Tag to workBuilder
        dayWorkBuilder.addTag(ExpireStoryWorker.TAG + "_" + storyId);
        // Create the actual work object:
        PeriodicWorkRequest dayWork = dayWorkBuilder.build();
        // Then enqueue the recurring task:
        WorkManager.getInstance().enqueue(dayWork);*/

    }

    /**
     * Job to get initializer settings
     */
    public void initializerApplicationService() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(InitializerApplicationService.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                //     .addTag(InitializerApplicationService.TAG)
                .build();
        // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(InitializerApplicationService.TAG, ExistingWorkPolicy.KEEP, workRequest);
    }


    public void syncingContactsWithServerWorker() {

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SyncingContactsWithServerWorker.class, 24, TimeUnit.HOURS, 5, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                // .addTag(SyncingContactsWithServerWorker.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    public void syncingContactsWithServerWorkerInit() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncingContactsWithServerWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                // .addTag(SyncingContactsWithServerWorker.TAG)
                .build();
        //  WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueueUniqueWork(SyncingContactsWithServerWorker.TAG, ExistingWorkPolicy.KEEP, workRequest);


    }


}
