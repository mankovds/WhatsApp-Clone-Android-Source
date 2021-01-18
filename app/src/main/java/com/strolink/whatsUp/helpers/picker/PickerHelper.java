package com.strolink.whatsUp.helpers.picker;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;

import com.strolink.whatsUp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class PickerHelper {

    public static int HEIGHT, WIDTH;

    public static void setupNavigationHidden(AppCompatActivity appCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = appCompatActivity.getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public static void getScreenSize(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        HEIGHT = displayMetrics.heightPixels;
        WIDTH = displayMetrics.widthPixels;
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String getDateDifference(Context context, Calendar calendar) {
        Date d = calendar.getTime();
        Calendar lastMonth = Calendar.getInstance();
        Calendar lastWeek = Calendar.getInstance();
        Calendar recent = Calendar.getInstance();
        lastMonth.add(Calendar.DAY_OF_MONTH, -(Calendar.DAY_OF_MONTH));
        lastWeek.add(Calendar.DAY_OF_MONTH, -7);
        recent.add(Calendar.DAY_OF_MONTH, -2);
        if (calendar.before(lastMonth)) {
            return new SimpleDateFormat("MMMM").format(d);
        } else if (calendar.after(lastMonth) && calendar.before(lastWeek)) {
            return context.getResources().getString(R.string.last_month);
        } else if (calendar.after(lastWeek) && calendar.before(recent)) {
            return context.getResources().getString(R.string.last_week);
        } else {
            return context.getResources().getString(R.string.recent);
        }
    }

    public static boolean isNull(View topChild) {
        return topChild == null;
    }

    public static Cursor getCursor(Context context) {
        return context.getContentResolver().query(Constants.queryUri, Constants.PROJECTION,
                Constants.selection, null, Constants.ORDERBY);
    }

    public static boolean isViewVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    public static ViewPropertyAnimator showScrollbar(View mScrollbar, Context context) {
        float transX = context.getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end);
        mScrollbar.setTranslationX(transX);
        mScrollbar.setVisibility(View.VISIBLE);
        return mScrollbar.animate().translationX(0f).alpha(1f)
                .setDuration(Constants.sScrollbarAnimDuration)
                .setListener(new AnimatorListenerAdapter() {
                    // adapter required for new alpha value to stick
                });
    }

    public static void cancelAnimation(ViewPropertyAnimator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    public static void manipulateVisibility(float slideOffset,
                                            RecyclerView instantRecyclerView, RecyclerView recyclerView,
                                            View status_bar_bg, View topbar, View clickme, View sendButton, boolean longSelection) {
        instantRecyclerView.setAlpha(1 - slideOffset);
        clickme.setAlpha(1 - slideOffset);
        if (longSelection) {
            sendButton.setAlpha(1 - slideOffset);
        }
        topbar.setAlpha(slideOffset);
        recyclerView.setAlpha(slideOffset);
        if ((1 - slideOffset) == 0 && instantRecyclerView.getVisibility() == View.VISIBLE) {
            instantRecyclerView.setVisibility(View.GONE);
            clickme.setVisibility(View.GONE);
        } else if (instantRecyclerView.getVisibility() == View.GONE && (1 - slideOffset) > 0) {
            instantRecyclerView.setVisibility(View.VISIBLE);
            clickme.setVisibility(View.VISIBLE);
            if (longSelection) {
                sendButton.clearAnimation();
                sendButton.setVisibility(View.VISIBLE);
            }
        }
        if ((slideOffset) > 0 && recyclerView.getVisibility() == View.INVISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            status_bar_bg.animate().translationY(0).setDuration(300).start();
            topbar.setVisibility(View.VISIBLE);
        } else if (recyclerView.getVisibility() == View.VISIBLE && (slideOffset) == 0) {
            recyclerView.setVisibility(View.INVISIBLE);
            topbar.setVisibility(View.GONE);
            status_bar_bg.animate().translationY(-(status_bar_bg.getHeight())).setDuration(300).start();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    public static void vibe(Context c, long l) {
        ((Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(l);
    }

    public static float getFingerSpacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (Exception e) {
            Log.e("exc", "->" + e.getMessage());
            return 0;
        }
    }


}
