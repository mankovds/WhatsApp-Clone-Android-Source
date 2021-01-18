package com.strolink.whatsUp.ui.audio;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.utils.Util;
import com.strolink.whatsUp.helpers.utils.concurrent.ListenableFuture;
import com.strolink.whatsUp.helpers.utils.concurrent.SettableFuture;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getSimpleName();

    private static final ExecutorService executor = newDynamicSingleThreadedExecutor();

    private final Context context;

    private AudioCodec audioCodec;
    private String FileAudioPath;

    public AudioRecorder(@NonNull Context context) {
        this.context = context;
    }

    public void startRecording() {
        Log.w(TAG, "startRecording()");

        executor.execute(() -> {
            Log.w(TAG, "Running startRecording() + " + Thread.currentThread().getId());
            try {
                if (audioCodec != null) {
                    throw new AssertionError("We can only record once at a time.");
                }

                FileAudioPath = FilesManager.getFileRecordPath(context);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(FileAudioPath), 1024);
                audioCodec = new AudioCodec();

                audioCodec.start(outputStream);
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        });
    }

    public @NonNull
    ListenableFuture<Pair<String, Long>> stopRecording() {
        Log.w(TAG, "stopRecording()");

        final SettableFuture<Pair<String, Long>> future = new SettableFuture<>();

        executor.execute(() -> {
            if (audioCodec == null) {
                sendToFuture(future, new IOException("MediaRecorder was never initialized successfully!"));
                return;
            }

            audioCodec.stop();
            sendToFuture(future, new Pair<>(FileAudioPath, 0L));
            audioCodec = null;
            FileAudioPath = null;
        });

        return future;
    }

    private <T> void sendToFuture(final SettableFuture<T> future, final Exception exception) {
        Util.runOnMain(() -> future.setException(exception));
    }

    private <T> void sendToFuture(final SettableFuture<T> future, final T result) {
        Util.runOnMain(() -> future.set(result));
    }


    public static ExecutorService newDynamicSingleThreadedExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }
}
