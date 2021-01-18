package com.strolink.whatsUp.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.strolink.whatsUp.R;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Abderrahim El imame on 3/17/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class AspectRatioImageView extends AppCompatImageView {

    private int defaultWidthRatio   = 3;
    private int defaultHeightRatio  = 2;

    private int widthRatio;
    private int heightRatio;

    public AspectRatioImageView(Context context) {
        this(context, null);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView);
        try {
            widthRatio = a.getInteger(R.styleable.AspectRatioImageView_width_ratio, 0);
            heightRatio = a.getInteger(R.styleable.AspectRatioImageView_height_ratio, 0);

            if (widthRatio == 0 || heightRatio == 0) {
                widthRatio = defaultWidthRatio;
                heightRatio = defaultHeightRatio;
            }
        } finally {
            a.recycle();
        }
    }

    /**
     * Sets width ratio.
     *
     * @param widthRatio the width ratio
     */
    public void setWidthRatio(int widthRatio) {
        this.widthRatio = widthRatio;
    }

    /**
     * Sets height ratio.
     *
     * @param heightRatio the height ratio
     */
    public void setHeightRatio(int heightRatio) {
        this.heightRatio = heightRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(widthMeasureSpec) * heightRatio / widthRatio;
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }

}