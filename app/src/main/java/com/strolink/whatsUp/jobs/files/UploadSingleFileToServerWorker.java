package com.strolink.whatsUp.jobs.files;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.interfaces.UploadCallbacks;
import com.strolink.whatsUp.jobs.utils.UploadProgressRequestBody;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
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
public class UploadSingleFileToServerWorker extends ListenableWorker {


    public static final String TAG = UploadSingleFileToServerWorker.class.getSimpleName();


    private CompositeDisposable compositeDisposable;
    private static WeakReference<UploadCallbacks> mWaitingListenerWeakReference;
    private String messageId;
    private SettableFuture<Result> mFuture;

    static void setUploadCallbacks(UploadCallbacks uploadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(uploadCallbacks);
    }

    static void updateUploadCallbacks(UploadCallbacks uploadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(uploadCallbacks);
    }

    public UploadSingleFileToServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        compositeDisposable = new CompositeDisposable();
        mFuture = SettableFuture.create();

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    if (Permissions.hasAny(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        compositeDisposable = new CompositeDisposable();
                        messageId = getInputData().getString("messageId");


                        if (PendingFilesTask.containsFile(messageId)) {
                            MessageModel messageModel = MessagesController.getInstance().getMessageById(messageId);
                            if (messageModel != null) {
                                messageId = messageModel.get_id();
                                AppHelper.LogCat("messageId " + messageId);
                                try {
                                    GroupModel groupModel = null;
                                    UsersModel usersModel = null;
                                    if (messageModel.isIs_group()) {
                                        groupModel = UsersController.getInstance().getGroupById(messageModel.getGroupId());
                                    } else {
                                        usersModel = UsersController.getInstance().getUserById(messageModel.getRecipientId());
                                    }


                                    String messageId = messageModel.get_id();
                                    if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

                                        sendStartStatus("image", messageModel.get_id());

                                        if (messageModel.isIs_group()) {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    groupModel.getName(),
                                                    messageModel.get_id(),
                                                    groupModel.get_id(),
                                                    messageModel.getConversationId());
                                        } else {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    usersModel.getUsername(),
                                                    usersModel.getPhone(),
                                                    messageModel.get_id(),
                                                    usersModel.get_id(),
                                                    messageModel.getConversationId());
                                        }
                                        if (uploadImageFile(messageModel.getFile())) {
                                            mFuture.set(Result.success());
                                        } else {
                                            if (compositeDisposable.isDisposed()) {
                                                sendErrorStatus("image", messageId);
                                            }
                                            mFuture.set(Result.retry());
                                        }
                                    } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                                        sendStartStatus("gif", messageModel.get_id());
                                        if (messageModel.isIs_group()) {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    groupModel.getName(),
                                                    messageModel.get_id(),
                                                    groupModel.get_id(),
                                                    messageModel.getConversationId());
                                        } else {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    usersModel.getUsername(),
                                                    usersModel.getPhone(),
                                                    messageModel.get_id(),
                                                    usersModel.get_id(),
                                                    messageModel.getConversationId());
                                        }
                                        if (uploadGifFile(messageModel.getFile())) {
                                            mFuture.set(Result.success());
                                        } else {
                                            if (compositeDisposable.isDisposed()) {
                                                sendErrorStatus("gif", messageId);
                                            }
                                            mFuture.set(Result.retry());
                                        }
                                    } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {

                                        sendStartStatus("video", messageModel.get_id());
                                        if (messageModel.isIs_group()) {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    groupModel.getName(),
                                                    messageModel.get_id(),
                                                    groupModel.get_id(),
                                                    messageModel.getConversationId());
                                        } else {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    usersModel.getUsername(),
                                                    usersModel.getPhone(),
                                                    messageModel.get_id(),
                                                    usersModel.get_id(),
                                                    messageModel.getConversationId());
                                        }
                                        if (uploadVideoFile(messageModel.getFile())) {
                                            mFuture.set(Result.success());
                                        } else {
                                            if (compositeDisposable.isDisposed()) {
                                                sendErrorStatus("video", messageId);
                                            }
                                            mFuture.set(Result.retry());
                                        }
                                    } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

                                        sendStartStatus("audio", messageModel.get_id());
                                        if (messageModel.isIs_group()) {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    groupModel.getName(),
                                                    messageModel.get_id(),
                                                    groupModel.get_id(),
                                                    messageModel.getConversationId());
                                        } else {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    usersModel.getUsername(),
                                                    usersModel.getPhone(),
                                                    messageModel.get_id(),
                                                    usersModel.get_id(),
                                                    messageModel.getConversationId());
                                        }
                                        if (uploadAudioFile(messageModel.getFile())) {
                                            mFuture.set(Result.success());
                                        } else {
                                            if (compositeDisposable.isDisposed()) {
                                                sendErrorStatus("audio", messageId);
                                            }
                                            mFuture.set(Result.retry());
                                        }
                                    } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                                        sendStartStatus("document", messageModel.get_id());
                                        if (messageModel.isIs_group()) {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    groupModel.getName(),
                                                    messageModel.get_id(),
                                                    groupModel.get_id(),
                                                    messageModel.getConversationId());
                                        } else {

                                            NotificationsManager.getInstance().showUpDownNotification(getApplicationContext(),
                                                    usersModel.getUsername(),
                                                    usersModel.getPhone(),
                                                    messageModel.get_id(),
                                                    usersModel.get_id(),
                                                    messageModel.getConversationId());
                                        }
                                        if (uploadDocumentFile(messageModel.getFile())) {
                                            mFuture.set(Result.success());
                                        } else {
                                            if (compositeDisposable.isDisposed()) {
                                                sendErrorStatus("document", messageId);
                                            }
                                            mFuture.set(Result.retry());
                                        }
                                    } else {
                                        mFuture.set(Result.failure());
                                    }
                                } catch (Exception e) {
                                    AppHelper.LogCat("Exception " + e.getMessage());
                                    mFuture.set(Result.success());
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
                } catch (Throwable throwable) {
                    mFuture.setException(throwable);
                }
            });

        } else {
            mFuture.set(Result.failure());
        }

        return mFuture;

    }


    private void sendStartStatus(String type, String messageId) {

        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStart(type, messageId);
            }
        }
    }

    private void sendErrorStatus(String type, String messageId) {
        NotificationsManager.getInstance().cancelNotification(messageId);
        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {

                listener.onError(type, messageId);

            }
        }
    }

    private void sendFinishStatus(String type, MessageModel messageModel) {

        NotificationsManager.getInstance().cancelNotification(messageModel.get_id());
        if (mWaitingListenerWeakReference != null) {
            UploadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onFinish(type, messageModel);
            }
        }
    }

    private boolean uploadVideoFile(String fileMessage) {
        String type = "video";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadVideoFile(createMultipartBody(fileMessage, "video/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed video " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_upload(true);
                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                        messagesModel1.setFile(filesResponse.getFilename());
                        messagesModel1.setFile_type(AppConstants.MESSAGES_VIDEO);

                        MessagesController.getInstance().updateMessage(messagesModel1);


                        File file1 = new File(fileMessage);
                        try {
                            FilesManager.copyFile(file1, FilesManager.getFileVideoSent(getApplicationContext(), filesResponse.getFilename()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db video");


                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
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

    private boolean uploadGifFile(String fileMessage) {
        String type = "gif";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadGifFile(createMultipartBody(fileMessage, "image/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed gif " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .observeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_upload(true);
                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                        messagesModel1.setFile(filesResponse.getFilename());
                        messagesModel1.setFile_type(AppConstants.MESSAGES_GIF);
                        MessagesController.getInstance().updateMessage(messagesModel1);


                        File file1 = new File(fileMessage);
                        try {
                            FilesManager.copyFile(file1, FilesManager.getFileGifSent(getApplicationContext(), filesResponse.getFilename()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db gif");

                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  gif" + throwable.getMessage());
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

    private boolean uploadImageFile(String fileMessage) {
        String type = "image";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadImageFile(createMultipartBody(fileMessage, "image/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed image " + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_upload(true);
                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                        messagesModel1.setFile(filesResponse.getFilename());
                        messagesModel1.setFile_type(AppConstants.MESSAGES_IMAGE);
                        MessagesController.getInstance().updateMessage(messagesModel1);


                        File file1 = new File(fileMessage);
                        try {
                            FilesManager.copyFile(file1, FilesManager.getFileImageSent(getApplicationContext(), filesResponse.getFilename()));
                        } catch (IOException e) {
                            //e.printStackTrace();
                            AppHelper.LogCat("IOException " + e.getMessage());
                        }


                        sendFinishStatus(type, messagesModel1);

                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db image");


                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
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

    private boolean uploadAudioFile(String fileMessage) {
        String type = "audio";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadAudioFile(createMultipartBody(fileMessage, "audio/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed audio" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_upload(true);
                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                        messagesModel1.setFile(filesResponse.getFilename());
                        messagesModel1.setFile_type(AppConstants.MESSAGES_AUDIO);

                        MessagesController.getInstance().updateMessage(messagesModel1);


                        File file1 = new File(fileMessage);
                        try {
                            FilesManager.copyFile(file1, FilesManager.getFileAudioSent(getApplicationContext(), filesResponse.getFilename()));
                            file1.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();
                        AppHelper.LogCat("finish db audio");


                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
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

    private boolean uploadDocumentFile(String fileMessage) {
        String type = "document";
        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeUploadFiles().uploadDocumentFile(createMultipartBody(fileMessage, "application/*", type))
                .subscribeOn(Schedulers.io())
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribeOn(Schedulers.computation()).subscribe(filesResponse -> {
                    if (filesResponse.isSuccess()) {
                        AppHelper.LogCat("url  " + filesResponse.getFilename());

                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_upload(true);
                        messagesModel1.setCreated(String.valueOf(new DateTime()));
                        messagesModel1.setFile(filesResponse.getFilename());
                        messagesModel1.setFile_type(AppConstants.MESSAGES_DOCUMENT);
                        MessagesController.getInstance().updateMessage(messagesModel1);


                        File file1 = new File(fileMessage);
                        try {
                            FilesManager.copyFile(file1, FilesManager.getFileDocumentSent(getApplicationContext(), filesResponse.getFilename()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        sendFinishStatus(type, messagesModel1);
                        //    checkCompletion(parameters, false);
                        AppHelper.LogCat("finish db document");
                        returnItem.set(true);
                        latch.countDown();

                    } else {
                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                    }


                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
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
        return RequestBody.create( MediaType.parse(mimeType),file);
    }

    private RequestBody createCountingRequestBody(File file, String mimeType, String mType) {
        RequestBody requestBody = createRequestBody(file, mimeType);
        //  private CountDownLatch latch;
        return new UploadProgressRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (100 * bytesWritten) / contentLength;

            // AppHelper.LogCat("createCountingRequestBody " + (int) progress);

            if (mWaitingListenerWeakReference != null) {
                UploadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdate((int) progress, mType, messageId);
                        NotificationsManager.getInstance().updateUpDownNotification(getApplicationContext(), messageId, (int) progress);
                    }
                }
            }
        }, messageId);
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
