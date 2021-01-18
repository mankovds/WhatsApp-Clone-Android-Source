package com.strolink.whatsUp.adapters.others;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.strolink.whatsUp.helpers.AppHelper;

import java.util.ArrayList;

/**
 * Created by Abderrahim El imame on 2/11/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class WhatsCloneViewPager extends ViewPager {


    public WhatsCloneViewPager(@NonNull Context context) {
        super(context);
        init();
    }

    public WhatsCloneViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Touch event variables
    private ArrayList<View> mTouchable = new ArrayList<>();
    private View mTouchOverrideView = null;

    // Reveal transformation variables
    private int mRevealPosition = -1;
    private View appBar = null;

    private View mTab = null;

    private OnOffsetChangedListener offsetChangedListener = null;

    // State variables holding current states of full screen and translucence
    private int mState = ViewPager.SCROLL_STATE_IDLE;
    private int mCurrent = -1;
    private boolean shouldTransform = false;
    private boolean isStateSaved = false;
    // Init the reveal transformer here
    private RevealTransformer mTransformer;

    void init() {

        mTransformer = new RevealTransformer();
        // bind the reveal transformer to the ViewPager
        addOnPageChangeListener(mTransformer);

        // Intercept touch events and deliver to other view layers beneath it when in reveal state

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                // deliver touch events only when in IDLE state and on reveal position
                if (getCurrentItem() == mRevealPosition && mState == ViewPager.SCROLL_STATE_IDLE) {

                    // Offset the vertical position of the touch event
                    // due to a displacement of the ViewPager caused by the AppBar collapsing.
                    event.offsetLocation(0f, (float) appBar.getTop());

                    // check if any view has a hold on touch events
                    if (mTouchOverrideView != null) {
                        mTouchOverrideView.dispatchTouchEvent(event);
                    } else {
                        // Keep sending the touch event layer by layer until a view consumes it
                        for (View touchable : mTouchable) {
                            if (touchable.dispatchTouchEvent(event)) break;
                        }
                    }
                }

                return false;
            }
        });


    }


    /**
     * Sets the position of the revealing item fragment.
     */
    void setRevealPosition(int position) {
        mRevealPosition = position;
    }


    /**
     * Used to set views that can receive touch events behind the view pager
     *
     * @param view       The view to receive the touch event
     * @param layerIndex Touch events are delivered in ascending order from index 0 till a view
     *                   completely consumes the event
     */

    void addTouchableViewLayer(View view, int layerIndex) {
        int index = resolveIndex(layerIndex);
        if (!mTouchable.contains(view)) {
            mTouchable.add(index, view);
        } else if (mTouchable.indexOf(view) != index) {
            mTouchable.remove(view);
            index = resolveIndex(index);
            mTouchable.add(index, view);
        }
    }

    boolean removeTouchableViewLayer(View view) {
        return mTouchable.remove(view);
    }

    void removeTouchableViewLayerIndex(int index) {
        if (index >= 0 && index < mTouchable.size()) mTouchable.remove(index);
    }


    public void setOnOffsetChangedListener(OnOffsetChangedListener listener) {
        offsetChangedListener = listener;
    }

    private int resolveIndex(int index) {
        if (mTouchable.size() == 0 || index < 0) return 0;
        if (index > mTouchable.size()) return mTouchable.size();
        return index;
    }


    // Individual methods for binding views that would be transformed during state changes
    public void bindViews(View appBar) {
        this.appBar = appBar;
        getTouchables().remove(mTab);
        addTouchableViewLayer(appBar, 1);
        mTab = appBar;
        setRevealPosition(0);
    }


    /**
     * This method should not be called before any call to [ViewPager.setCurrentItem] is made during
     * layout initialization.
     * A call to [ViewPager.setCurrentItem] after calling this method during app Initialization,
     * introduces a subtle transformation error.
     *
     * @param inState   . Pass the savedInstanceState bundle from [Activity.onCreate] to this method
     * @param firstInit . Set to false if the index of the  tab, [mRevealPosition], = 0
     *                  and you do not want to initialise layout at that position. It disables initial transformations.
     *                  Default value is set to false. Set true if initial transformation is required
     */

    public void initTransformer(Bundle inState, boolean firstInit) {
        int current;
        if (inState != null)
            current = inState.getInt("RevealTransformer.mCurrent", -2);
        else
            current = 0;

        isStateSaved = current != -2;

        if (mCurrent != -1 && inState == null) current = mCurrent;

        if ((inState == null && current == mRevealPosition && firstInit) ||
                (inState != null && current == mRevealPosition)) {
            mCurrent = current;
            mTransformer.swipe();
        }

    }


    /**
     * Method should be called in the [Activity.onSaveInstanceState] method to save the current state
     * of the UI
     */

    public void saveState(Bundle outState) {
        outState.putInt("RevealTransformer.mCurrent", mCurrent);
    }


    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        // check if user saved view state in activity or not then initialise transformer
        if (!isStateSaved) initTransformer(null, true);
    }


    /**
     * The page transform class that handles transformation of the appbar, swipeForeground,
     * action button and state of translucence and full screen.
     **/

    class RevealTransformer implements OnPageChangeListener {

        RevealTransformer() {

        }


        // Initialises first transformation for the swipe view
        void swipe() {
            new Thread(() -> {
                // get the width of the screen
                int w = getWidth();

                // get an instance of the bottom value of the appBar
                int h = appBar.getBottom();

                // Loop till view is drawn or transformation is not necessary
                while ((h == 0 && appBar != null && appBar.getVisibility() == VISIBLE) ||
                        (w == 0 && getVisibility() == VISIBLE)) h = (appBar.getBottom());

                int finalH = h;
                AppHelper.runOnUIThread(() -> {
                    // Translate the appBar up as the swipeReveal slides into view
                    appBar.setTranslationY(-finalH - finalH / 8f);
                });

            }).start();


            mCurrent = getCurrentItem();
        }


        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (offsetChangedListener != null)
                offsetChangedListener.onOffsetChanged(positionOffset);
            // going right offset changes from zero to one

            int w = getWidth();

            // get an instance of the bottom value of the appBar
            int h = (appBar.getBottom());

            if (position == mRevealPosition && shouldTransform) {

                // Translate the appBar up as the swipeReveal slides into view
                float y = (positionOffset * h) - h;
                if (y == -(float) h) y = -h - h / 8f;
                appBar.setTranslationY(y);
                //   setTranslationY(y);
//                AppHelper.LogCat("positionOffset "+positionOffset);
                //    setAlpha(positionOffset);
               /* if (positionOffset == 0) {
                    appBar.setVisibility(GONE);
                }*/

/*
                // Set translucence based on the position of the swipeReveal
                if (positionOffset < 1) setTranslucentNav();
                else exitTranslucentNav();

                // Set full screen based on the position of swipeReveal
                if (positionOffset > 0) exitFullScreen();
                else setFullScreen();*/
            } else if (position == mRevealPosition - 1 && shouldTransform) {
                // going right offset changes from zero to one
                // Translate the appBar up as the swipeReveal slides into view
                appBar.setTranslationY(-(positionOffset * h));

                /*
                // Set translucence based on the position of the swipeReveal
                if (positionOffset > 0) setTranslucentNav();
                else exitTranslucentNav();
                // Set full screen based on the position of swipeReveal
                if (positionOffset < 1) exitFullScreen();
                else setFullScreen();*/
            } else {
                if (appBar.getTranslationY() != 0f /*|| swipeForeground.getTranslationX() == 0f*/) {
                    //   if (appBar.getTranslationY() != 0f) exitTranslucentNav();
                    // Reset translation of the appBar
                    appBar.setTranslationY(0f);


                }
            }
        }

        @Override
        public void onPageSelected(int position) {

            // check if page scroll state is settling from/to the swipeReveal position
            shouldTransform = (mCurrent == mRevealPosition || position == mRevealPosition);


            mCurrent = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING || state == ViewPager.SCROLL_STATE_IDLE) {
                shouldTransform = true;
            }

            mState = state;
        }
    }


    /**
     * Interface for listening to when the swiping view becomes visible and when it looses visibility
     */

    public interface OnOffsetChangedListener {
        void onOffsetChanged(float var2);
    }
}
