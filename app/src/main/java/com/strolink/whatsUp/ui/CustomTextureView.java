package com.strolink.whatsUp.ui;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.strolink.whatsUp.helpers.AppHelper;

import java.io.IOException;

/**
 * Created by Abderrahim El imame on 5/12/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CustomTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private MediaPlayer mMediaPlayer;
    private Uri mSource;
    private boolean isLooping = false;
    private OnPreparedListener mOnPreparedListener;
    private OnInfoListener mOnInfoListener;
    private OnErrorListener mOnErrorListener;
    private OnCompletionListener mOnCompletionListener;

    public CustomTextureView(Context context) {
        this(context, null, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    public void setSource(Uri source) {
        mSource = source;
    }


    public void initVideo() {
        setSurfaceTextureListener(this);

        if (this.getSurfaceTexture() != null) {
            Surface surface = new Surface(this.getSurfaceTexture());
            try {
                mMediaPlayer = new MediaPlayer();

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(getContext(), mSource);
                mMediaPlayer.setOnPreparedListener(mp -> {
                    mOnPreparedListener.onPrepared();
                });
                mMediaPlayer.setOnInfoListener((mp, what, extra) -> {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        mOnInfoListener.OnInfoListen();
                    }
                    return false;
                });

                mMediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
                    mOnErrorListener.onError();
                    return false;
                });


                mMediaPlayer.setOnCompletionListener(mediaPlayer -> mOnCompletionListener.OnCompletion());
                mMediaPlayer.setOnBufferingUpdateListener((mediaPlayer, i) -> {
                    //AppHelper.LogCat("onBufferingUpdate" + i);
                });
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setLooping(isLooping);
            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                AppHelper.LogCat("IOException " + e.getMessage());
                if (mOnErrorListener != null) mOnErrorListener.onError();
                e.printStackTrace();
            }
        }
    }

    public interface OnPreparedListener {
        void onPrepared();
    }

    public interface OnInfoListener {
        void OnInfoListen();
    }

    public interface OnErrorListener {
        void onError();
    }

    public interface OnCompletionListener {
        void OnCompletion();
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    protected void onDetachedFromWindow() {
        // release resources on detach
        release();
        super.onDetachedFromWindow();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();

        } else {
            Surface surface = new Surface(surfaceTexture);
            try {
                mMediaPlayer = new MediaPlayer();

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(getContext(), mSource);
                mMediaPlayer.setOnPreparedListener(mp -> {
                    mOnPreparedListener.onPrepared();
                });
                mMediaPlayer.setOnInfoListener((mp, what, extra) -> {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        mOnInfoListener.OnInfoListen();
                    }
                    return false;
                });

                mMediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
                    mOnErrorListener.onError();
                    return false;
                });


                mMediaPlayer.setOnCompletionListener(mediaPlayer -> mOnCompletionListener.OnCompletion());
                mMediaPlayer.setOnBufferingUpdateListener((mediaPlayer, i) -> {
                    //AppHelper.LogCat("onBufferingUpdate" + i);
                });
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setLooping(isLooping);
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.prepareAsync();
            } catch (IllegalArgumentException | SecurityException | IllegalStateException |
                    IOException e)

            {
                AppHelper.LogCat("IOException " + e.getMessage());
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //pre lollipop needs SurfaceTexture it owns before calling onDetachedFromWindow super
            surface.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void release() {
        if (getSurfaceTexture() != null) {
            AppHelper.LogCat("getSurfaceTexture nto");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //pre lollipop needs SurfaceTexture it owns before calling onDetachedFromWindow super
                getSurfaceTexture().release();
            }
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        setSurfaceTextureListener(null);
    }

    public void stopVideo() {

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    public void pauseVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void startVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void muteVideo() {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume(0f, 0f);
    }

    public void unMuteVideo() {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume(1f, 1f);
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null)
            return mMediaPlayer.isPlaying();
        else
            return false;
    }

    public int getDuration() {
        return (mMediaPlayer != null) ? mMediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return (mMediaPlayer != null) ? mMediaPlayer.getCurrentPosition() : 0;
    }
}