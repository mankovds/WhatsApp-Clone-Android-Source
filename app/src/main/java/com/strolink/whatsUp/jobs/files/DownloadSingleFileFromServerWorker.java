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
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.interfaces.DownloadCallbacks;
import com.strolink.whatsUp.jobs.utils.DownloadHelper;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.BlockingHelper;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class DownloadSingleFileFromServerWorker extends ListenableWorker {


    public static final String TAG = DownloadSingleFileFromServerWorker.class.getSimpleName();


    private CompositeDisposable compositeDisposable;
    private static WeakReference<DownloadCallbacks> mWaitingListenerWeakReference;
    private String messageId;
    private SettableFuture<Result> mFuture;


    static void setDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(downloadCallbacks);
    }

    static void updateDownloadCallbacks(DownloadCallbacks downloadCallbacks) {
        mWaitingListenerWeakReference = new WeakReference<>(downloadCallbacks);
    }

    public DownloadSingleFileFromServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        AppHelper.LogCat("onStartJob: " + TAG);
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
                                String messageId = messageModel.get_id();
                                if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

                                    sendStartStatus("image", messageModel.get_id());

                                    if (downloadImageFile(messageModel)) {
                                        mFuture.set(Result.success());
                                    } else {
                                        if (compositeDisposable.isDisposed()) {
                                            sendErrorStatus("image", messageId);
                                        }
                                        mFuture.set(Result.retry());
                                    }
                                } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                                    sendStartStatus("gif", messageModel.get_id());

                                    if (downloadGifFile(messageModel)) {
                                        mFuture.set(Result.success());
                                    } else {
                                        if (compositeDisposable.isDisposed()) {
                                            sendErrorStatus("gif", messageId);
                                        }
                                        mFuture.set(Result.retry());
                                    }
                                } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {

                                    sendStartStatus("video", messageModel.get_id());

                                    if (downloadVideoFile(messageModel)) {
                                        mFuture.set(Result.success());
                                    } else {
                                        if (compositeDisposable.isDisposed()) {
                                            sendErrorStatus("video", messageId);
                                        }
                                        mFuture.set(Result.retry());
                                    }
                                } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

                                    sendStartStatus("audio", messageModel.get_id());

                                    if (downloadAudioFile(messageModel)) {
                                        mFuture.set(Result.success());
                                    } else {
                                        if (compositeDisposable.isDisposed()) {
                                            sendErrorStatus("audio", messageId);
                                        }
                                        mFuture.set(Result.retry());
                                    }
                                } else if (messageModel.getFile_type() != null && messageModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                                    sendStartStatus("document", messageModel.get_id());

                                    if (downloadDocumentFile(messageModel)) {
                                        mFuture.set(Result.success());
                                    } else {
                                        AppHelper.LogCat("downloadDocumentFile " + compositeDisposable.isDisposed());
                                        if (compositeDisposable.isDisposed()) {
                                            sendErrorStatus("document", messageId);
                                        }
                                        mFuture.set(Result.retry());
                                    }
                                } else {
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
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onStartDownload(type, messageId);
            }
        }
    }

    private void sendErrorStatus(String type, String messageId) {
        if (mWaitingListenerWeakReference != null) {
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onErrorDownload(type, messageId);

            }
        }
    }

    private void sendFinishStatus(String type, MessageModel messageModel) {

        NotificationsManager.getInstance().cancelNotification(messageModel.get_id());
        if (mWaitingListenerWeakReference != null) {
            DownloadCallbacks listener = mWaitingListenerWeakReference.get();
            if (listener != null) {
                listener.onFinishDownload(type, messageModel);
            }
        }
    }


    private boolean downloadVideoFile(MessageModel messageModel) {
        String type = "video";

        String fileUrl = EndPoints.MESSAGE_VIDEO_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_downLoad(true);
                        MessagesController.getInstance().updateMessage(messagesModel1);
                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();

                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }
                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  video" + throwable.getMessage());
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    private boolean downloadGifFile(MessageModel messageModel) {
        String type = "gif";

        String fileUrl = EndPoints.MESSAGE_GIF_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_downLoad(true);
                        MessagesController.getInstance().updateMessage(messagesModel1);
                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();


                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }
                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  gif" + throwable.getMessage());
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    private boolean downloadImageFile(MessageModel messageModel) {
        String type = "image";

        String fileUrl = EndPoints.MESSAGE_IMAGE_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_downLoad(true);
                        MessagesController.getInstance().updateMessage(messagesModel1);
                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();


                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }
                }, throwable -> {

                    AppHelper.LogCat("error  image" + throwable.getMessage());
                    sendErrorStatus(type, messageId);
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);

        return returnItem.get();
    }

    private boolean downloadAudioFile(MessageModel messageModel) {
        String type = "audio";
        String fileUrl = EndPoints.MESSAGE_AUDIO_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_downLoad(true);
                        MessagesController.getInstance().updateMessage(messagesModel1);
                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();


                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }

                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }


    private boolean downloadDocumentFile(MessageModel messageModel) {

        String type = "document";
        String fileUrl = EndPoints.MESSAGE_DOCUMENT_DOWNLOAD_URL + messageModel.getFile();
        String fileMessage = messageModel.getFile();

        final AtomicReference<Boolean> returnItem = new AtomicReference<Boolean>();
        final CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = APIHelper.initializeDownloadFiles((bytesWritten, contentLength, done) -> {
            if (mWaitingListenerWeakReference != null) {
                DownloadCallbacks listener = mWaitingListenerWeakReference.get();
                if (PendingFilesTask.containsFile(messageId) && !isStopped()) {
                    if (listener != null) {
                        listener.onUpdateDownload((int) (100 * bytesWritten / contentLength), type, messageId);
                    }
                }
            }
        }).downloadLargeFileSizeSync(fileUrl)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(ResponseBody::byteStream)
                .observeOn(Schedulers.computation())
                .map(inputStream -> {
                    if (inputStream != null) {
                        try {
                            DownloadHelper.writeResponseBodyToDisk(getApplicationContext(), inputStream, fileMessage, type);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                })
                .doFinally(() -> {
                    AppHelper.LogCat("error isDisposed document" + compositeDisposable.isDisposed());
                    if (compositeDisposable.isDisposed()) {
                        returnItem.set(false);
                        latch.countDown();
                    }
                })
                .subscribe(response -> {
                    if (response) {
                        AppHelper.LogCat("server contacted and has file ");


                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        messagesModel1.setFile_downLoad(true);
                        MessagesController.getInstance().updateMessage(messagesModel1);
                        sendFinishStatus(type, messagesModel1);
                        returnItem.set(true);
                        latch.countDown();


                    } else {

                        sendErrorStatus(type, messageId);
                        returnItem.set(false);
                        latch.countDown();
                    }

                }, throwable -> {
                    sendErrorStatus(type, messageId);
                    AppHelper.LogCat("error  document" + throwable.getMessage());
                    returnItem.set(false);
                    latch.countDown();


                });
        compositeDisposable.add(disposable);

        BlockingHelper.awaitForComplete(latch, disposable);


        return returnItem.get();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        AppHelper.LogCat("onStopJob: " + "onStopJob " + isStopped());
        if (isStopped())
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }
    }


}
