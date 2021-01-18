package com.strolink.whatsUp.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
;

import com.google.android.material.appbar.AppBarLayout;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Abderrahim El imame on 26/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AnimationsUtil {


    private static AnimatorSet mAnimationSet;

    // To reveal a previously invisible view using this effect:
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void show(final View view, long duration) {
        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 17;
        int cy = (view.getTop() + view.getBottom()) / 17;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy,
                0, finalRadius);
        anim.setDuration(duration);

        // make the view visible and start the animation
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    // To hide a previously visible view using this effect:
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void hide(Activity mActivity, final View view, long duration) {

        // get the center for the clipping circle
        int cx = (view.getLeft() + view.getRight()) / 17;
        int cy = (view.getTop() + view.getBottom()) / 17;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth();

        // create the animation (the final radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy,
                initialRadius, 0);
        anim.setDuration(duration);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
                mActivity.finish();
            }
        });

        // start the animation
        anim.start();
    }


    public static void AnimationLoader(Context context, View view, long duration, int id) {
        final Animation animTranslatePassword = android.view.animation.AnimationUtils.loadAnimation(context, id);
        animTranslatePassword.setDuration(duration);
        view.startAnimation(animTranslatePassword);
    }

    public static void expandToolbar(CoordinatorLayout rootLayout, Bitmap bmp, AppBarLayout appBar) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBar.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setTopAndBottomOffset(0);
        int height = AppHelper.pxToDp(350);
        behavior.onNestedPreScroll(rootLayout, appBar, null, 0, bmp.getHeight() - height, new int[2]);
        params.setBehavior(behavior);
        appBar.setLayoutParams(params);
    }

    public static void startAlphaAnimation(View v, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(1000);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public static void animateY(RecyclerView.ViewHolder holder, boolean goesDown) {

        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(holder.itemView, "translationY", goesDown == true ? 100 : -100, 0);
        animatorTranslateY.setDuration(300);
        animatorTranslateY.start();
    }

    public static void rotationY(final View viewToFlip) {
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(viewToFlip, "rotationY", 0f, 360f);
        rotationY.setDuration(2000);
        rotationY.start();

    }

    public static void slideStatus(View view) {
        ObjectAnimator objectanimator = ObjectAnimator.ofFloat(view, "x", -200);
        objectanimator.setDuration(1500);
        objectanimator.start();
        new Handler().postDelayed(() -> {
            ObjectAnimator objectanimator2 = ObjectAnimator.ofFloat(view, "x", 0);
            objectanimator2.setDuration(1500);
            objectanimator2.start();
        }, 1500);
    }


    public static void ShakeAnimation(Context mContext, View view) {
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        view.startAnimation(shake);
    }


    public static void setFlipAnimation(final AppCompatImageView view, final ObjectAnimator animator, final int firstImage,
                                        final int secondImage, final Context c) {
        int NO_ANIMATION = -1;
        if (secondImage == NO_ANIMATION) {
            view.setImageResource(firstImage);
            animator.end();
            ViewCompat.setHasTransientState(view, false);
        } else {
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setDuration(1300);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatMode(ValueAnimator.RESTART);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                final Drawable shape1 = c.getResources().getDrawable(firstImage);
                final Drawable shape2 = c.getResources().getDrawable(secondImage);
                Drawable currentDrawable = null;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float angle = (Float) animation.getAnimatedValue();
                    int quadrant = (int) (angle / 90) + 1;
                    if ((quadrant == 1 || quadrant == 4) && shape1 != currentDrawable) {
                        view.setImageDrawable(shape1);
                        currentDrawable = shape1;
                    } else if ((quadrant == 2 || quadrant == 3) && currentDrawable != shape2) {
                        view.setImageDrawable(shape2);
                        currentDrawable = shape2;
                    }
                }
            });
            animator.start();
            ViewCompat.setHasTransientState(view, true);
        }
    }


    public static void fadeInOut(View mView) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mView, "alpha", 1f, 0f);
        fadeOut.setDuration(500L);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mView, "alpha", 0f, 1f);
        fadeIn.setDuration(500L);

        mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mAnimationSet != null)
                    mAnimationSet.start();
            }
        });
        mAnimationSet.start();
    }

    public static void cancelAnimationSet(View mView) {
        if (mAnimationSet != null && mAnimationSet.isRunning()) {
            mAnimationSet.cancel();
            mAnimationSet = null;
            mView.clearAnimation();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mView.setStateListAnimator(null);
            }

        }
    }



    public static void setTransitionAnimation(Activity mActivity) {
    }

    public static void animate(Context context, View view, @AnimRes int anim, int visibility, Animation.AnimationListener animationListener) {
        if (view.getVisibility() != visibility) {
            Animation animation = AnimationUtils.loadAnimation(context, anim);
            animation.setAnimationListener(animationListener);

            view.startAnimation(animation);
            view.setVisibility(visibility);
        }
    }

    private static final int ANIMATION_DURATION = 300;

    public static void animateWindowInTranslate(@NonNull View contentView) {
        contentView.setVisibility(View.VISIBLE);
        Animation animation = new TranslateAnimation(0, 0, contentView.getHeight(), 0);
        animation.setDuration(ANIMATION_DURATION);

        contentView.startAnimation(animation);
    }

    public static void animateWindowOutTranslate(@NonNull View contentView) {
        Animation animation = new TranslateAnimation(0, 0, 0, contentView.getTop() + contentView.getHeight());
        animation.setDuration(ANIMATION_DURATION);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        contentView.startAnimation(animation);
    }


    public static void manageBlinkEffect(@NonNull View contentView) {
        ObjectAnimator anim = ObjectAnimator.ofInt(contentView, "backgroundColor", Color.YELLOW, Color.TRANSPARENT);
        anim.setDuration(300);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatCount(3);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.start();
    }
}
