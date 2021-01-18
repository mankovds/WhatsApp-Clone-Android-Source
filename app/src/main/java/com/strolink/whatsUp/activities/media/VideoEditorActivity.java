/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.strolink.whatsUp.activities.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.googlecode.mp4parser.authoring.Track;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.helpers.video.Mp4Cutter;
import com.strolink.whatsUp.interfaces.video.VideoTimelineViewListener;
import com.strolink.whatsUp.jobs.files.PendingFilesTask;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.strolink.whatsUp.ui.views.VideoSeekBarView;
import com.strolink.whatsUp.ui.views.VideoTimelineView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

;

public class VideoEditorActivity extends BaseActivity implements SurfaceHolder.Callback, InputGeneralPanel.Listener {

    private MediaPlayer videoPlayer = null;
    private SurfaceHolder surfaceHolder = null;

    private boolean initied = false;
    private String videoPath = null;
    private int videoWidth;
    private int videoHeight;
    private float lastProgress = 0;
    private boolean needSeek = false;
    // private static VideoEditorActivityListener videoEditorActivityListener;

    private Unbinder unbinder;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.video_container)
    View videoContainerView;

    @BindView(R.id.video_editor_layout)
    View video_editor_layout;

    @BindView(R.id.start_time)
    AppCompatTextView start_time;

    @BindView(R.id.end_time)
    AppCompatTextView end_time;

    @BindView(R.id.original_size)
    AppCompatTextView originalSizeTextView;

    @BindView(R.id.edited_size)
    AppCompatTextView editedSizeTextView;

    @BindView(R.id.info_container)
    View textContainerView;

    @BindView(R.id.play_button)
    AppCompatImageView playButton;

    @BindView(R.id.video_seekbar)
    VideoSeekBarView videoSeekBarView;

    @BindView(R.id.video_view)
    SurfaceView surfaceView;


    @BindView(R.id.video_timeline_view)
    VideoTimelineView videoTimelineView;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;


    private EmojiPopup emojiPopup;
    private boolean forStory;
    private Runnable progressRunnable = () -> {
        if (videoPlayer != null && videoTimelineView != null) {
            while (videoPlayer.isPlaying()) {
                AppHelper.runOnUIThread(() -> {
                    if (videoPlayer.isPlaying()) {
                        float startTime = videoTimelineView.getLeftProgress() * videoPlayer.getDuration();
                        float endTime = videoTimelineView.getRightProgress() * videoPlayer.getDuration();
                        if (startTime == endTime) {
                            startTime = endTime - 0.01f;
                        }
                        float progress = (videoPlayer.getCurrentPosition() - startTime) / (endTime - startTime);
                        if (progress > lastProgress) {
                            videoSeekBarView.setProgress(progress);
                            lastProgress = progress;
                        }
                        if (videoPlayer.getCurrentPosition() >= endTime) {
                            try {
                                videoPlayer.pause();
                                onPlayComplete();
                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                            }
                        }
                    }
                });
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor_layout);
        unbinder = ButterKnife.bind(this);

        videoPath = getIntent().getStringExtra(AppConstants.MediaConstants.EXTRA_VIDEO_PATH);
        forStory = getIntent().getBooleanExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, false);
        videoPlayer = new MediaPlayer();
        videoPlayer.setOnCompletionListener(mp -> AppHelper.runOnUIThread(this::onPlayComplete));

        initializerView();
        setupToolbar();
    }

    private void initializerView() {

        composeText.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                send();

            }
            return false;
        });


        inputPanel.setListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(video_editor_layout).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);


        videoTimelineView.setVideoPath(videoPath);


        videoTimelineView.setVideoTimelineViewListener(new VideoTimelineViewListener() {
            @Override
            public void onLeftProgressChanged(float progress) {
                try {
                    if (videoPlayer.isPlaying()) {
                        videoPlayer.pause();
                        playButton.setImageDrawable(AppHelper.getVectorDrawable(VideoEditorActivity.this, R.drawable.ic_play_circle_white_72dp));
                    }
                    videoPlayer.setOnSeekCompleteListener(null);
                    videoPlayer.seekTo((int) (videoPlayer.getDuration() * progress));
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
                needSeek = true;
                videoSeekBarView.setProgress(0);
                updateVideoEditedInfo();
            }

            @Override
            public void onRightProgressChanged(float progress) {
                try {
                    if (videoPlayer.isPlaying()) {
                        videoPlayer.pause();
                        playButton.setImageDrawable(AppHelper.getVectorDrawable(VideoEditorActivity.this, R.drawable.ic_play_circle_white_72dp));
                    }
                    videoPlayer.setOnSeekCompleteListener(null);
                    videoPlayer.seekTo((int) (videoPlayer.getDuration() * progress));
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
                needSeek = true;
                videoSeekBarView.setProgress(0);
                updateVideoEditedInfo();
            }

            @Override
            public void didStartDragging() {

            }

            @Override
            public void didStopDragging() {

            }
        });


        videoSeekBarView.delegate = progress -> {
            if (videoPlayer.isPlaying()) {
                try {
                    float prog = videoTimelineView.getLeftProgress() + (videoTimelineView.getRightProgress() - videoTimelineView.getLeft()) * progress;
                    videoPlayer.seekTo((int) (videoPlayer.getDuration() * prog));
                    lastProgress = progress;
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            } else {
                lastProgress = progress;
                needSeek = true;
            }
        };


        playButton.setOnClickListener(v -> {
            if (surfaceHolder.isCreating()) {
                return;
            }
            play();
        });


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setFixedSize(270, 480);

        updateVideoOriginalInfo();
        updateVideoEditedInfo();
    }


    private String getStoryId(String userId) {
        try {
            StoriesHeaderModel storiesHeaderModel = StoriesController.getInstance().getStoriesHeader(userId);
            return storiesHeaderModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("Get storyId id Exception  " + e.getMessage());
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.send_buttonn)
    public void send() {

        AppHelper.ShowProgressDialog(this, getResources().getString(R.string.loading));
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return startConvert();
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String path) {
                super.onPostExecute(path);
                AppHelper.HideProgressDialog(VideoEditorActivity.this);
                if (path == null) {
                    AppHelper.CustomToast(getApplicationContext(), getString(R.string.oops_something));
                } else {
                    String message = UtilsString.escapeJava(composeText.getText().toString().trim());

                    AppHelper.LogCat("videoPath " + path);
                    AppHelper.LogCat("message " + message);
                    if (forStory) {

                        try {
                            String storyId = getStoryId(PreferenceManager.getInstance().getID(VideoEditorActivity.this));
                            if (storyId == null) {

                                String lastID = DbBackupRestore.getStoryLastId();


                                UsersModel storyOwner = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(VideoEditorActivity.this));


                                StoryModel storyModel = new StoryModel();
                                storyModel.set_id(lastID);
                                storyModel.setUserId(PreferenceManager.getInstance().getID(VideoEditorActivity.this));
                                storyModel.setDate(AppHelper.getCurrentTime());
                                storyModel.setDownloaded(true);
                                storyModel.setUploaded(false);
                                storyModel.setDeleted(false);
                                storyModel.setStatus(AppConstants.IS_WAITING);
                                storyModel.setFile(path);
                                storyModel.setBody(message);
                                storyModel.setType("video");
                                // storyModel.setDuration(FilesManager.getDuration(videoPath));
                                storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));


                                StoriesController.getInstance().insertStoryModel(storyModel);

                                StoriesHeaderModel storiesHeaderModel = new StoriesHeaderModel();
                                storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(VideoEditorActivity.this));


                                String name = UtilsPhone.getContactName(storyOwner.getPhone());
                                if (name != null) {
                                    storiesHeaderModel.setUsername(name);
                                } else {
                                    storiesHeaderModel.setUsername(storyOwner.getPhone());
                                }

                                storiesHeaderModel.setUserImage(storyOwner.getImage());
                                storiesHeaderModel.setDownloaded(true);


                                StoriesController.getInstance().insertStoriesHeaderModel(storiesHeaderModel);


                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW, PreferenceManager.getInstance().getID(VideoEditorActivity.this)));


                                // Create the task, set the listener, add to the task controller, and run
                                PendingFilesTask.initUploadListener(lastID);


                            } else {
                                String lastID = DbBackupRestore.getStoryLastId();

                                try {


                                    UsersModel storyOwner = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(VideoEditorActivity.this));


                                    StoryModel storyModel = new StoryModel();
                                    storyModel.set_id(lastID);
                                    storyModel.setUserId(storyId);
                                    storyModel.setDate(AppHelper.getCurrentTime());
                                    storyModel.setDownloaded(true);
                                    storyModel.setUploaded(false);
                                    storyModel.setDeleted(false);
                                    storyModel.setStatus(AppConstants.IS_WAITING);
                                    storyModel.setFile(path);
                                    storyModel.setBody(message);
                                    storyModel.setType("video");
                                    // storyModel.setDuration(FilesManager.getDuration(videoPath));
                                    storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));
                                    StoriesController.getInstance().insertStoryModel(storyModel);

                                    StoriesHeaderModel storiesHeaderModel = StoriesController.getInstance().getStoriesHeader(storyId);

                                    storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(VideoEditorActivity.this));
                                    storiesHeaderModel.setUsername(storyOwner.getUsername());
                                    storiesHeaderModel.setUserImage(storyOwner.getImage());
                                    storiesHeaderModel.setDownloaded(true);


                                    StoriesController.getInstance().updateStoriesHeaderModel(storiesHeaderModel);


                                } catch (Exception e) {
                                    AppHelper.LogCat("Exception  last id  " + e.getMessage());
                                }

                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(VideoEditorActivity.this)));

                                // Create the task, set the listener, add to the task controller, and run
                                PendingFilesTask.initUploadListener(lastID);
                            }

                        } finally {

                            finish();
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(VideoEditorActivity.this);
                            localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
                        }
                    } else {

                        Intent intent = new Intent();
                        intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITED_PATH, path);
                        intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE, message);
                        setResult(Activity.RESULT_OK, intent);

                        finish();
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(VideoEditorActivity.this);
                        localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
                    }
                }


            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void setupToolbar() {

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.edit_video);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (videoTimelineView != null) {
            videoTimelineView.destroy();
        }
        unbinder.unbind();

    }


    @Override
    public void onResume() {
        super.onResume();
        //fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // fixLayout();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        videoPlayer.setDisplay(holder);
        try {
            videoPlayer.setDataSource(videoPath);
            videoPlayer.prepare();
            videoWidth = videoPlayer.getVideoWidth();
            videoHeight = videoPlayer.getVideoHeight();
            fixVideoSize();
            videoPlayer.seekTo((int) (videoTimelineView.getLeftProgress() * videoPlayer.getDuration()));
            initied = true;
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        updateVideoOriginalInfo();
        updateVideoEditedInfo();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        videoPlayer.setDisplay(null);
    }

    private void onPlayComplete() {
        playButton.setImageDrawable(AppHelper.getVectorDrawable(this, R.drawable.ic_play_circle_white_72dp));
        videoSeekBarView.setProgress(0);
        try {
            videoPlayer.seekTo((int) (videoTimelineView.getLeftProgress() * videoPlayer.getDuration()));
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }


    private void updateVideoOriginalInfo() {

        if (AppConstants.DEBUGGING_MODE) {
            if (!initied) {
                return;
            }
            File file = new File(videoPath);
            String videoDimension = String.format(Locale.getDefault(), "%dx%d", videoPlayer.getVideoWidth(), videoPlayer.getVideoHeight());
            long duration = (long) Math.ceil(videoPlayer.getDuration());

            int minutes = (int) (duration / 1000 / 60);
            int seconds = (int) Math.ceil(duration / 1000) - minutes * 60;
            String videoTimeSize = String.format(Locale.getDefault(), "%d:%02d, %s", minutes, seconds, AppHelper.formatFileSize(file.length()));
            originalSizeTextView.setVisibility(View.VISIBLE);
            originalSizeTextView.setText(String.format("%s: %s, %s", "OriginalVideo", videoDimension, videoTimeSize));
        } else {
            originalSizeTextView.setVisibility(View.GONE);
        }


    }

    private void updateVideoEditedInfo() {
        if (AppConstants.DEBUGGING_MODE) {
            if (!initied) {
                return;
            }
            long esimatedDuration = (long) Math.ceil((videoTimelineView.getRightProgress() - videoTimelineView.getLeftProgress()) * videoPlayer.getDuration());

            File file = new File(videoPath);
            long size = file.length();
            float videoWidth = videoPlayer.getVideoWidth();
            float videoHeight = videoPlayer.getVideoHeight();
            if (videoWidth > 640 || videoHeight > 640) {
                float scale = videoWidth > videoHeight ? 640.0f / videoWidth : 640.0f / videoHeight;
                videoWidth *= scale;
                videoHeight *= scale;
                size *= (scale * scale);
            }
            String videoDimension = String.format(Locale.getDefault(), "%dx%d", (int) videoWidth, (int) videoHeight);
            int minutes = (int) (esimatedDuration / 1000 / 60);
            int seconds = (int) Math.ceil(esimatedDuration / 1000) - minutes * 60;
            int estimatedSize = (int) (size * ((float) esimatedDuration / videoPlayer.getDuration()));
            String videoTimeSize = String.format(Locale.getDefault(), "%d:%02d, ~%s", minutes, seconds, AppHelper.formatFileSize(estimatedSize));

            editedSizeTextView.setVisibility(View.VISIBLE);
            editedSizeTextView.setText(String.format("%s: %s, %s", "EditedVideo", videoDimension, videoTimeSize));
        } else {
            editedSizeTextView.setVisibility(View.GONE);
        }


        long startTime = (long) Math.ceil((videoTimelineView.getLeftProgress() * videoPlayer.getDuration()));
        int startTimeMinutes = (int) (startTime / 1000 / 60);
        int startTimeSeconds = (int) Math.ceil(startTime / 1000) - startTimeMinutes * 60;
        String videoStartTime = String.format(Locale.getDefault(), "%d:%02d", startTimeMinutes, startTimeSeconds);
        start_time.setText(videoStartTime);

        long endTime = (long) Math.ceil(videoTimelineView.getRightProgress() * videoPlayer.getDuration());
        int endTimeMinutes = (int) (endTime / 1000 / 60);
        int endTimeSeconds = (int) Math.ceil(endTime / 1000) - endTimeMinutes * 60;
        String videoEndTime = String.format(Locale.getDefault(), "%d:%02d", endTimeMinutes, endTimeSeconds);

        end_time.setText(videoEndTime);
    }

    private void fixVideoSize() {
        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        int viewHeight = 0;
        if (!AppHelper.isTablet(this) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHeight = AppHelper.displaySize.y - AppHelper.statusBarHeight - AppHelper.dp(40);
        } else {
            viewHeight = AppHelper.displaySize.y - AppHelper.statusBarHeight - AppHelper.dp(48);
        }

        int width = 0;
        int height = 0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width = AppHelper.displaySize.x - AppHelper.displaySize.x / 2 - AppHelper.dp(24);
            height = viewHeight - AppHelper.dp(32);
        } else {
            width = AppHelper.displaySize.x;
            height = viewHeight - AppHelper.dp(176);
        }

        float wr = (float) width / (float) videoWidth;
        float hr = (float) height / (float) videoHeight;
        float ar = (float) videoWidth / (float) videoHeight;

        if (wr > hr) {
            width = (int) (height * ar);
        } else {
            height = (int) (width / ar);
        }

        surfaceHolder.setFixedSize(width, height);
    }

    private void fixLayout() {
        if (originalSizeTextView == null) {
            return;
        }
        originalSizeTextView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                originalSizeTextView.getViewTreeObserver().removeOnPreDrawListener(this);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) videoContainerView.getLayoutParams();
                    layoutParams.topMargin = AppHelper.dp(16);
                    layoutParams.bottomMargin = AppHelper.dp(16);
                    layoutParams.width = AppHelper.displaySize.x / 2 - AppHelper.dp(24);
                    layoutParams.leftMargin = AppHelper.dp(16);
                    videoContainerView.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) textContainerView.getLayoutParams();
                    layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.width = AppHelper.displaySize.x / 2 - AppHelper.dp(24);
                    layoutParams.leftMargin = AppHelper.displaySize.x / 2 + AppHelper.dp(8);
                    layoutParams.rightMargin = AppHelper.dp(16);
                    layoutParams.topMargin = AppHelper.dp(16);
                    textContainerView.setLayoutParams(layoutParams);
                } else {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) videoContainerView.getLayoutParams();
                    layoutParams.topMargin = AppHelper.dp(16);
                    layoutParams.bottomMargin = AppHelper.dp(160);
                    layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.leftMargin = 0;
                    videoContainerView.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) textContainerView.getLayoutParams();
                    layoutParams.height = AppHelper.dp(143);
                    layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.leftMargin = 0;
                    layoutParams.rightMargin = 0;
                    layoutParams.topMargin = 0;
                    textContainerView.setLayoutParams(layoutParams);
                }
                fixVideoSize();
                videoTimelineView.clearFrames();
                return false;
            }
        });
    }

    private void play() {
        if (videoPlayer.isPlaying()) {
            videoPlayer.pause();
            playButton.setImageDrawable(AppHelper.getVectorDrawable(this, R.drawable.ic_play_circle_white_72dp));
        } else {
            try {
                playButton.setImageDrawable(null);
                lastProgress = 0;
                if (needSeek) {
                    float prog = videoTimelineView.getLeftProgress() + (videoTimelineView.getRightProgress() - videoTimelineView.getLeft()) * videoSeekBarView.getProgress();
                    videoPlayer.seekTo((int) (videoPlayer.getDuration() * prog));
                    needSeek = false;
                }
                videoPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        float startTime = videoTimelineView.getLeftProgress() * videoPlayer.getDuration();
                        float endTime = videoTimelineView.getRightProgress() * videoPlayer.getDuration();
                        if (startTime == endTime) {
                            startTime = endTime - 0.01f;
                        }
                        lastProgress = (videoPlayer.getCurrentPosition() - startTime) / (endTime - startTime);
                        videoSeekBarView.setProgress(lastProgress);
                    }
                });
                videoPlayer.start();
                new Thread(progressRunnable).start();
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }


    private String startConvert() throws Exception {
        long esimatedDuration = (long) Math.ceil((videoTimelineView.getRightProgress() - videoTimelineView.getLeftProgress()) * videoPlayer.getDuration());
        int minutes = (int) (esimatedDuration / 1000 / 60);
        int seconds = (int) Math.ceil(esimatedDuration / 1000) - minutes * 60;
        File cacheFile;

        if (seconds <= AppConstants.MediaConstants.MAX_STORY_DURATION) {
            AppHelper.LogCat("Min duration is reached  ");
            String fileName = String.valueOf(System.currentTimeMillis()) + ".mp4";
            cacheFile = new File(FilesManager.getCacheDir(), fileName);
            int startTime = (int) (videoTimelineView.getLeftProgress() * videoPlayer.getDuration());
            int endTime = (int) (videoTimelineView.getRightProgress() * videoPlayer.getDuration());
            cacheFile = Mp4Cutter.startTrim(new File(videoPath), cacheFile, startTime, endTime);
        } else {
            AppHelper.LogCat("Max duration is reached plse ");
            cacheFile = null;
        }
        String path = FilesManager.getPath(getApplicationContext(), FilesManager.getFile(cacheFile));
        if (path == null) {
            path = FilesManager.copyDocumentToCache(FilesManager.getFile(cacheFile), ".mp4");
        }
        return path;

    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];
            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) emojiPopup.dismiss();
        else if (videoPlayer.isPlaying()) {
            videoPlayer.stop();
            videoPlayer.release();
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }


    @Override
    public void onEmojiToggle() {

        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }

}
