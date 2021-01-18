package com.strolink.whatsUp.jobs.files;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.ForegroundRuning;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.jobs.utils.UploadProgressRequestBody;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.presenters.controllers.StoriesController;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class UploadSingleStoryFileToServerWorker extends ListenableWorker {


    public static final String TAG = UploadSingleStoryFileToServerWorker.class.getSimpleName();


    private CompositeDisposable compositeDisposable;
    private SettableFuture<Result> mFuture;
    private String storyId;

    public UploadSingleStoryFileToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        compositeDisposable = new CompositeDisposable();
        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + TAG);

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            if (Permissions.hasAny(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                storyId = getInputData().getString("storyId");

                if (PendingFilesTask.containsFile(storyId)) {
                    StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
                    if (storyModel != null) {
                        storyId = storyModel.get_id();

                        String storyId = storyModel.get_id();
                        if (storyModel.getType() != null && storyModel.getType().equals("image")) {

                            sendStartStatus("image", storyModel.get_id());

                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                    storyModel.get_id(),
                                    storyModel.getUserId());
                            if (uploadImageFile(storyModel.getFile())) {
                                mFuture.set(Result.success());
                            } else {
                                if (compositeDisposable.isDisposed()) {
                                    sendErrorStatus("image", storyId);
                                }
                                mFuture.set(Result.retry());
                            }
                        } else if (storyModel.getType() != null && storyModel.getType().equals("video")) {

                            sendStartStatus("video", storyModel.get_id());

                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                    storyModel.get_id(),
                                    storyModel.getUserId());
                            if (uploadVideoFile(storyModel.getFile())) {
                                mFuture.set(Result.success());
                            } else {
                                if (compositeDisposable.isDisposed()) {
                                    sendErrorStatus("video", storyId);
                                }
                                mFuture.set(Result.retry());
                            }
                        } else {
                            mFuture.set(Result.failure());
                        }
                    } else {
                        mFuture.set(Result.retry());
                    }
                } else {
                    mFuture.set(Result.success());
                }


            } else {
                mFuture.set(Result.retry());
            }
        } else {
            mFuture.set(Result.failure());
        }
        return mFuture;
    }

    private void sendStartStatus(String type, String storyId) {

      /*  if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStart(type, storyId);
            }
        }*/
    }

    private void sendErrorStatus(String type, String storyId) {
        NotificationsManager.getInstance().cancelNotification(storyId);
        if (ForegroundRuning.get().isForeground()) {

            AppHelper.CustomToast(getApplicationContext(), getApplicationContext().getString(R.string.oops_something));
        }

   /*     if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {

                listener.onError(type, storyId);

            }
        }*/
    }

    private void sendFinishStatus(String type, StoryModel storyModel) {
        NotificationsManager.getInstance().cancelNotification(storyModel.get_id());
        PendingFilesTask.removeFile(storyModel.get_id());
        WorkJobsManager.getInstance().sendUserStoriesToServer();
    }


    private boolean uploadVideoFile(String fileStory) {
        String type = "video";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadVideoFile(createMultipartBody(fileStory, "video/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed video " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
                        storyModel.setUploaded(true);
                        storyModel.setFile(filesResponse.getFilename());
                        storyModel.setType("video");

                        StoriesController.getInstance().updateStoryModel(storyModel);


                        File file1 = new File(fileStory);
                        file1.delete();


                        sendFinishStatus(type, storyModel);
                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db video");


                    } else {
                        sendErrorStatus(type, storyId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, storyId);
                    AppHelper.LogCat("error  video" + throwable.getMessage());
                    returnException.set(throwable);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        try {
            if (returnException.get() != null) {
                Exceptions.propagate(returnException.get());
            }
        } catch (Exception e) {
            returnItem.set(false);
        }

        return returnItem.get();
    }


    private boolean uploadImageFile(String fileStory) {
        String type = "image";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadImageFile(createMultipartBody(fileStory, "image/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat(" isDisposed image " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                }).subscribeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
                        storyModel.setUploaded(true);
                        storyModel.setFile(filesResponse.getFilename());
                        storyModel.setType("image");

                        StoriesController.getInstance().updateStoryModel(storyModel);

                        File file1 = new File(fileStory);
                        file1.delete();


                        sendFinishStatus(type, storyModel);

                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db image");

                    } else {
                        sendErrorStatus(type, storyId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, storyId);
                    AppHelper.LogCat("error  image" + throwable.getMessage());
                    returnException.set(throwable);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        try {
            if (returnException.get() != null) {
                Exceptions.propagate(returnException.get());
            }
        } catch (Exception e) {
            returnItem.set(false);
        }
        return returnItem.get();
    }


    private MultipartBody.Part createMultipartBody(String filePath, String mimeType, String mType) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("file", file.getName(), createCountingRequestBody(file, mimeType, mType));
    }

    private RequestBody createRequestBody(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, String mType) {
        RequestBody requestBody = createRequestBody(file, mimeType);
        //  private CountDownLatch latch;
        return new UploadProgressRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (100 * bytesWritten) / contentLength;

            // AppHelper.LogCat("createCountingRequestBody " + (int) progress);

            //if (mWaitingListenerWeakReference != null) {
            //   UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (PendingFilesTask.containsFile(storyId) && !isStopped()) {
                // if (listener != null) {
                // listener.onUpdate((int) progress, mType, storyId);
                NotificationsManager.getInstance().updateUpDownNotification(getApplicationContext(), storyId, (int) progress);
                //   }
            }
            // }
        }, storyId);
    }

    @Override
    public void onStopped() {
        super.onStopped();
        AppHelper.LogCat("onStopJob: " + "onStopJob");
        if (isStopped())
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }

    }


}
