package com.strolink.whatsUp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Abderrahim El imame on 04/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class RecyclerViewFastScroller extends LinearLayout {
    private static final int BUBBLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;
    private static final int DISPLAY_LOWEST_VALUE = 20;

    private int height;
    private boolean isInitialized = false;
    private ObjectAnimator currentAnimator = null;

    private TextView bubble;
    private View handle;
    private RecyclerView recyclerView;

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int position);
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            updateBubbleAndHandlePosition();
        }
    };

    public RecyclerViewFastScroller(Context context) {
        this(context, null);
    }

    public RecyclerViewFastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewFastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        this.height = height;
        updateBubbleAndHandlePosition();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) {
                    return false;
                }
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }
                if (bubble != null && bubble.getVisibility() == INVISIBLE) {
                    showBubble();
                }
                handle.setSelected(true);
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                setBubbleAndHandlePosition(y);
                setRecyclerViewPosition(y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handle.setSelected(false);
                hideBubble();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(onScrollListener);
            recyclerView = null;
        }
    }

    /**
     * Initialization of FastScroller
     */
    protected void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        setOrientation(HORIZONTAL);
        setClipChildren(false);
    }

    /**
     * Set RecyclerView to display FastScroller
     *
     * @param recyclerView 対象のRecyclerView
     */
    public void setRecyclerView(@NonNull RecyclerView recyclerView) {
        if (this.recyclerView != recyclerView) {
            if (this.recyclerView != null) {
                this.recyclerView.removeOnScrollListener(onScrollListener);
            }
            this.recyclerView = recyclerView;
            recyclerView.addOnScrollListener(onScrollListener);
        }
    }

    /**
     * Set the layout of FastScroller
     *
     * @param layoutResId FastScrollerのLayoutId
     * @param bubbleResId BubbleのLayoutId
     * @param handleResId HandleのLayoutId
     */
    public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutResId, this, true);
        bubble = (TextView) findViewById(bubbleResId);
        if (bubble != null) {
            bubble.setVisibility(INVISIBLE);
        }
        handle = findViewById(handleResId);
    }

    /**
     * Change the display position of RecyclerView
     *
     * @param
     */
    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (handle.getY() == 0) {
                proportion = 0f;
            } else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
                proportion = 1f;
            } else {
                proportion = y / (float) height;
            }
            int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);
            String bubbleText = ((BubbleTextGetter) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
            if (bubble != null) {
                bubble.setText(bubbleText);
            }
        }
    }

    /**
     * Hide FastScroller if the number of elements is less than a certain number
     *
     * @param size The number of elements
     */
    public void checkHideFastScroller(int size) {
        if (size < DISPLAY_LOWEST_VALUE) {
            this.setVisibility(GONE);
        } else {
            this.setVisibility(VISIBLE);
        }
    }

    /**
     * Update the position of Bubble and Handle
     */
    private void updateBubbleAndHandlePosition() {
        if (bubble == null || handle.isSelected()) {
            return;
        }
        int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
        int verticalScrollRange = recyclerView.computeVerticalScrollRange();
        float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
        setBubbleAndHandlePosition(height * proportion);
    }

    /**
     * Set the position of Bubble and Handle
     *
     * @param
     */
    private void setBubbleAndHandlePosition(float y) {
        int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2)));
        if (bubble != null) {
            int bubbleHeight = bubble.getHeight();
            bubble.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
        }
    }

    /**
     * Display Bubble
     */
    private void showBubble() {
        if (bubble == null) {
            return;
        }
        bubble.setVisibility(VISIBLE);
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
        currentAnimator.start();
    }

    /**
     * Hide Bubble
     */
    private void hideBubble() {
        if (bubble == null) {
            return;
        }
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                bubble.setVisibility(INVISIBLE);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                bubble.setVisibility(INVISIBLE);
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    /**
     * Handle position adjustment
     *
     * @ param min min 値
     * @ param max maximum 値
     * @ param adjust Value for position adjustment
     */
    private int getValueInRange(int min, int max, int adjust) {
        int minimum = Math.max(min, adjust);
        return Math.min(minimum, max);
    }
}