package com.strolink.whatsUp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Build.VERSION;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;

/**
 * Created by Abderrahim El imame on 1/7/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CircularRevealView extends LinearLayout {
    int duration = 300;
    int offsetX;
    int offsetY;
    CustomAnimation customAnimation;
    private int colorCircle = -1;
    private Paint paint = new Paint(1);
    private Path path = new Path();
    private RectF rectF = new RectF();
    public float interpolatedTime;

    private Context context;


    class CustomAnimation extends Animation {
        boolean aBoolean;
        final CircularRevealView circularRevealView;

        CustomAnimation(CircularRevealView circularRevealView, boolean z) {
            this.circularRevealView = circularRevealView;
            this.aBoolean = z;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            CircularRevealView circularRevealView = this.circularRevealView;
            if (this.aBoolean) {
                interpolatedTime = 1.0f - interpolatedTime;
            }
            circularRevealView.interpolatedTime = interpolatedTime;
            this.circularRevealView.invalidate();
        }
    }

    public CircularRevealView(Context context) {
        super(context);
        this.context =context;
    }

    public CircularRevealView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context =context;
    }

    public CircularRevealView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.context =context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircularRevealView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.context =context;
    }

    public final void initialCircularRevealView() {
        if (VERSION.SDK_INT < 21) {
            clearAnimation();
            this.customAnimation = new CustomAnimation(this, true);
            this.customAnimation.setDuration((long) this.duration);
            startAnimation(this.customAnimation);
        } else {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.offsetX, this.offsetY, (float) Math.max(getWidth(), getHeight()), 0.0f);
            createCircularReveal.setDuration((long) this.duration);
            createCircularReveal.addListener(new AnimatorListenerAdapter() {

                @Override
                public final void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    setVisibility(INVISIBLE);
                }
            });
            createCircularReveal.start();
        }
    }

    public final void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public final void start(Animation animation) {
        clearAnimation();
        setBackgroundColor(AppHelper.getColor(context,android.R.color.transparent));
        animation.setDuration((long) this.duration);
        startAnimation(animation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (VERSION.SDK_INT < 21) {
            clearAnimation();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        Canvas canvas2 = canvas;
        super.onDraw(canvas2);
        float sqrt;
        if (VERSION.SDK_INT < 21 && this.colorCircle != -1) {
            sqrt = (float) (Math.sqrt((double) ((getWidth() * getWidth()) + (getHeight() * getHeight()))) * ((double) this.interpolatedTime));
            this.rectF.set(-sqrt, -sqrt, sqrt, sqrt);
            this.rectF.offset((float) this.offsetX, (float) this.offsetY);
            this.paint.setColor(this.colorCircle);
            this.paint.setStyle(Style.FILL);
            canvas2.drawArc(this.rectF, 0.0f, 360.0f, true, this.paint);
        } else if (VERSION.SDK_INT < 21 && VERSION.SDK_INT >= 18) {
            sqrt = (float) (Math.sqrt((double) ((getWidth() * getWidth()) + (getHeight() * getHeight()))) * ((double) this.interpolatedTime));
            this.rectF.set(-sqrt, -sqrt, sqrt, sqrt);
            this.rectF.offset((float) this.offsetX, (float) this.offsetY);
            this.paint.setColor(AppHelper.getColor(WhatsCloneApplication.getInstance(), R.color.colorAccent));
            this.paint.setStyle(Style.FILL);
            this.path.reset();
            this.path.addArc(this.rectF, 0.0f, 360.0f);
            canvas2.clipPath(this.path);
        }
    }

    public void setColor(int colorCircle) {
        this.colorCircle = colorCircle;
    }

    public void setDuration(int i) {
        this.duration = i;
    }
}
