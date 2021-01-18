/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.strolink.whatsUp.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.video.VideoTimelineViewListener;

import java.util.ArrayList;


public class VideoTimelineView extends View {

    private long videoLength;
    private float progressLeft;
    private float progressRight = 1;
    private Paint paint;
    private Paint paint2;
    private boolean pressedLeft;
    private boolean pressedRight;
    private float pressDx;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private VideoTimelineViewListener videoTimelineViewListener;
    private ArrayList<Bitmap> frames = new ArrayList<>();
    private AsyncTask<Integer, Integer, Bitmap> currentTask;
    private static final Object sync = new Object();
    private long frameTimeOffset;
    private int frameWidth;
    private int frameHeight;
    private int framesToLoad;
    private float maxProgressDiff = 1.0f;
    private float minProgressDiff = 0.0f;
    private boolean isRoundFrames;
    private Rect rect1;
    private Rect rect2;

    public VideoTimelineView(Context context) {
        super(context);
        init();
    }

    public VideoTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTimelineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoTimelineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(AppHelper.getColor(getContext(), R.color.colorPrimary));
        paint2 = new Paint();
        paint2.setColor(0x7f000000);
    }

    public float getLeftProgress() {
        return progressLeft;
    }

    public float getRightProgress() {
        return progressRight;
    }

    public void setMinProgressDiff(float value) {
        minProgressDiff = value;
    }

    public void setMaxProgressDiff(float value) {
        maxProgressDiff = value;
        if (progressRight - progressLeft > maxProgressDiff) {
            progressRight = progressLeft + maxProgressDiff;
            invalidate();
        }
    }

    public void setRoundFrames(boolean value) {
        isRoundFrames = value;
        if (isRoundFrames) {
            rect1 = new Rect(AppHelper.dp(14), AppHelper.dp(14), AppHelper.dp(14 + 28), AppHelper.dp(14 + 28));
            rect2 = new Rect();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        int width = getMeasuredWidth() - AppHelper.dp(32);
        int startX = (int) (width * progressLeft) + AppHelper.dp(16);
        int endX = (int) (width * progressRight) + AppHelper.dp(16);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (mediaMetadataRetriever == null) {
                return false;
            }
            int additionWidth = AppHelper.dp(12);
            if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.didStartDragging();
                }
                pressedLeft = true;
                pressDx = (int) (x - startX);
                invalidate();
                return true;
            } else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.didStartDragging();
                }
                pressedRight = true;
                pressDx = (int) (x - endX);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedLeft) {
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.didStopDragging();
                }
                pressedLeft = false;
                return true;
            } else if (pressedRight) {
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.didStopDragging();
                }
                pressedRight = false;
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedLeft) {
                startX = (int) (x - pressDx);
                if (startX < AppHelper.dp(16)) {
                    startX = AppHelper.dp(16);
                } else if (startX > endX) {
                    startX = endX;
                }
                progressLeft = (float) (startX - AppHelper.dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressRight = progressLeft + maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressLeft = progressRight - minProgressDiff;
                    if (progressLeft < 0) {
                        progressLeft = 0;
                    }
                }
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.onLeftProgressChanged(progressLeft);
                }
                invalidate();
                return true;
            } else if (pressedRight) {
                endX = (int) (x - pressDx);
                if (endX < startX) {
                    endX = startX;
                } else if (endX > width + AppHelper.dp(16)) {
                    endX = width + AppHelper.dp(16);
                }
                progressRight = (float) (endX - AppHelper.dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressLeft = progressRight - maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressRight = progressLeft + minProgressDiff;
                    if (progressRight > 1.0f) {
                        progressRight = 1.0f;
                    }
                }
                if (videoTimelineViewListener != null) {
                    videoTimelineViewListener.onRightProgressChanged(progressRight);
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setMaxDuration(int videoLength) {
        this.videoLength = videoLength;
    }

    public void setVideoPath(String path) {

        destroy();
        mediaMetadataRetriever = new MediaMetadataRetriever();
        progressLeft = 0.0f;
        progressRight = 1.0f;
        try {
            mediaMetadataRetriever.setDataSource(path);
            String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoLength = 10;// Long.parseLong(duration);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        invalidate();
    }

    public void setVideoTimelineViewListener(VideoTimelineViewListener videoTimelineViewListener) {
        this.videoTimelineViewListener = videoTimelineViewListener;
    }

    @SuppressLint("StaticFieldLeak")
    private void reloadFrames(int frameNum) {
        if (mediaMetadataRetriever == null) {
            return;
        }
        if (frameNum == 0) {
            if (isRoundFrames) {
                frameHeight = frameWidth = AppHelper.dp(56);
                framesToLoad = (int) Math.ceil((getMeasuredWidth() - AppHelper.dp(16)) / (frameHeight / 2.0f));
            } else {
                frameHeight = AppHelper.dp(40);
                framesToLoad = (getMeasuredWidth() - AppHelper.dp(16)) / frameHeight;
                frameWidth = (int) Math.ceil((float) (getMeasuredWidth() - AppHelper.dp(16)) / (float) framesToLoad);
            }
            frameTimeOffset = videoLength / framesToLoad;
        }
        currentTask = new AsyncTask<Integer, Integer, Bitmap>() {
            private int frameNum = 0;

            @Override
            protected Bitmap doInBackground(Integer... objects) {
                frameNum = objects[0];
                Bitmap bitmap = null;
                if (isCancelled()) {
                    return null;
                }
                try {
                    bitmap = mediaMetadataRetriever.getFrameAtTime(frameTimeOffset * frameNum * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (isCancelled()) {
                        return null;
                    }
                    if (bitmap != null) {
                        Bitmap result = Bitmap.createBitmap(frameWidth, frameHeight, bitmap.getConfig());
                        Canvas canvas = new Canvas(result);
                        float scaleX = (float) frameWidth / (float) bitmap.getWidth();
                        float scaleY = (float) frameHeight / (float) bitmap.getHeight();
                        float scale = scaleX > scaleY ? scaleX : scaleY;
                        int w = (int) (bitmap.getWidth() * scale);
                        int h = (int) (bitmap.getHeight() * scale);
                        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        Rect destRect = new Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h);
                        canvas.drawBitmap(bitmap, srcRect, destRect, null);
                        bitmap.recycle();
                        bitmap = result;
                    }
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (!isCancelled()) {
                    frames.add(bitmap);
                    invalidate();
                    if (frameNum < framesToLoad) {
                        reloadFrames(frameNum + 1);
                    }
                }
            }
        };
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null);
    }

    public void destroy() {
        synchronized (sync) {
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                    mediaMetadataRetriever = null;
                }
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
        for (int a = 0; a < frames.size(); a++) {
            Bitmap bitmap = frames.get(a);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    public void clearFrames() {
        for (int a = 0; a < frames.size(); a++) {
            Bitmap bitmap = frames.get(a);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth() - AppHelper.dp(36);
        int startX = (int) (width * progressLeft) + AppHelper.dp(16);
        int endX = (int) (width * progressRight) + AppHelper.dp(16);

        canvas.save();
        canvas.clipRect(AppHelper.dp(16), 0, width + AppHelper.dp(20), getMeasuredHeight());
        if (frames.isEmpty() && currentTask == null) {
            reloadFrames(0);
        } else {
            int offset = 0;
            for (int a = 0; a < frames.size(); a++) {
                Bitmap bitmap = frames.get(a);
                if (bitmap != null) {
                    int x = AppHelper.dp(16) + offset * (isRoundFrames ? frameWidth / 2 : frameWidth);
                    int y = AppHelper.dp(2);
                    if (isRoundFrames) {
                        rect2.set(x, y, x + AppHelper.dp(28), y + AppHelper.dp(28));
                        canvas.drawBitmap(bitmap, rect1, rect2, null);
                    } else {
                        canvas.drawBitmap(bitmap, x, y, null);
                    }
                }
                offset++;
            }
        }

        int top = AppHelper.dp(2);

        canvas.drawRect(AppHelper.dp(16), top, startX, getMeasuredHeight() - top, paint2);
        canvas.drawRect(endX + AppHelper.dp(4), top, AppHelper.dp(16) + width + AppHelper.dp(4), getMeasuredHeight() - top, paint2);

        canvas.drawRect(startX, 0, startX + AppHelper.dp(2), getMeasuredHeight(), paint);
        canvas.drawRect(endX + AppHelper.dp(2), 0, endX + AppHelper.dp(4), getMeasuredHeight(), paint);
        canvas.drawRect(startX + AppHelper.dp(2), 0, endX + AppHelper.dp(4), top, paint);
        canvas.drawRect(startX + AppHelper.dp(2), getMeasuredHeight() - top, endX + AppHelper.dp(4), getMeasuredHeight(), paint);
        canvas.restore();

        canvas.drawCircle(startX, getMeasuredHeight() / 2, AppHelper.dp(7), paint);
        canvas.drawCircle(endX + AppHelper.dp(4), getMeasuredHeight() / 2, AppHelper.dp(7), paint);
    }
}