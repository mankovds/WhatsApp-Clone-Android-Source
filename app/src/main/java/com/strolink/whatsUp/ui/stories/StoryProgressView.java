package com.strolink.whatsUp.ui.stories;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.strolink.whatsUp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abderrahim El imame on 7/10/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoryProgressView extends LinearLayout {

    private static final int MAX_PROGRESS = 100;
    private static final int SPACE_BETWEEN_PROGRESS_BARS = 5;

    private final List<ProgressBar> progressBars = new ArrayList<>();
    private final List<ObjectAnimator> animators = new ArrayList<>();

    private int storiesCount = -1;
    private int current = 0;
    long playTime = 0;
    private StoryListener storyListener;

    boolean isReverse;
    boolean isComplete;

    public interface StoryListener {
        void onNext();

        void onPrev();

        void onComplete();
    }

    public interface StoryStateListener {
        void onPause();

        void onResume();

    }

    public StoryProgressView(Context context) {
        super(context);
    }

    public StoryProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StoryProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StoryProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void bindViews() {
        removeAllViews();

        for (int i = 0; i < storiesCount; i++) {
            final ProgressBar p = createProgressBar();
            p.setMax(MAX_PROGRESS);
            progressBars.add(p);
            addView(p);
            if ((i + 1) < storiesCount) {
                addView(createSpace());
            }
        }
    }

    private ProgressBar createProgressBar() {
        ProgressBar p = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        p.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        p.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.progress_bg));
        return p;

    }

    private View createSpace() {
        View v = new View(getContext());
        v.setLayoutParams(new LayoutParams(SPACE_BETWEEN_PROGRESS_BARS, LayoutParams.WRAP_CONTENT));
        return v;
    }


    public void setStoriesCount(int storiesCount) {
        this.storiesCount = storiesCount;
        bindViews();
    }


    public void setStoryListener(StoryListener storyListener) {
        this.storyListener = storyListener;
    }


    public void skip() {
        if (isComplete) return;
        ProgressBar p = progressBars.get(current);
        p.setProgress(p.getMax());
        animators.get(current).cancel();
    }


    // TODO: 7/18/18 fix bug with device below api 19
    public void pause() {
        if (isComplete) return;
        ProgressBar p = progressBars.get(current);
        p.setProgress(p.getProgress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animators.get(current).pause();
        } else {
            playTime = animators.get(current).getCurrentPlayTime();
            animators.get(current).cancel();
        }
    }

    // TODO: 7/18/18 fix bug with device below api 19
    public void resume() {
        if (isComplete) return;
        ProgressBar p = progressBars.get(current);
        p.setProgress(p.getProgress());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            animators.get(current).resume();
        } else {
            animators.get(current).setCurrentPlayTime(playTime);
            animators.get(current).start();
        }
    }


    public void reverse() {
        if (isComplete) return;
        ProgressBar p = progressBars.get(current);
        p.setProgress(0);
        isReverse = true;
        animators.get(current).cancel();
        if (0 <= (current - 1)) {
            p = progressBars.get(current - 1);
            p.setProgress(0);
            animators.get(--current).start();
        } else {
            animators.get(current).start();
        }
    }


    public void setStoryDuration(long duration) {
        animators.clear();
        for (int i = 0; i < progressBars.size(); i++) {
            animators.add(createAnimator(i, duration));
        }
    }


    public void setStoriesCountWithDurations(@NonNull long[] durations) {
        storiesCount = durations.length;
        bindViews();
        animators.clear();
        for (int i = 0; i < progressBars.size(); i++) {
            animators.add(createAnimator(i, durations[i]));
        }
    }


    public void playStories(int index) {
        animators.get(index).start();
    }

    /**
     * Need to call when Activity or Fragment destroy
     */
    public void destroy() {
        for (ObjectAnimator a : animators) {
            a.removeAllListeners();
            a.cancel();
        }
    }

    private ObjectAnimator createAnimator(final int index, long duration) {
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBars.get(index), "progress", MAX_PROGRESS);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(duration);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                current = index;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isReverse) {
                    isReverse = false;
                    if (storyListener != null) storyListener.onPrev();
                    return;
                }
                int next = current + 1;
                if (next <= (animators.size() - 1)) {
                    if (storyListener != null) storyListener.onNext();
                    animators.get(next).start();
                } else {
                    isComplete = true;
                    if (storyListener != null) storyListener.onComplete();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animation;

    }
}