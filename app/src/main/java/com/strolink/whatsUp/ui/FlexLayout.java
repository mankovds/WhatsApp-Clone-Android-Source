package com.strolink.whatsUp.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.strolink.whatsUp.R;

/**
 * Created by Abderrahim El imame on 3/9/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class FlexLayout extends RelativeLayout {
    private TextView viewPartMain;
    private View viewPartSlave;

    private TypedArray typedArray;

    private int viewPartSlaveWidth;
    private int viewPartSlaveHeight;


    public FlexLayout(Context context) {
        super(context);
    }

    public FlexLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlexLayout, 0, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        try {
            viewPartMain = (TextView) this.findViewById(typedArray.getResourceId(R.styleable.FlexLayout_viewPartMain, -1));
            viewPartSlave = this.findViewById(typedArray.getResourceId(R.styleable.FlexLayout_viewPartSlave, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (viewPartMain == null || viewPartSlave == null || widthSize <= 0) {
            return;
        }

        int availableWidth = widthSize - getPaddingLeft() - getPaddingRight();
        int availableHeight = heightSize - getPaddingTop() - getPaddingBottom();

        LayoutParams viewPartMainLayoutParams = (LayoutParams) viewPartMain.getLayoutParams();
        int viewPartMainWidth = viewPartMain.getMeasuredWidth() + viewPartMainLayoutParams.leftMargin + viewPartMainLayoutParams.rightMargin;
        int viewPartMainHeight = viewPartMain.getMeasuredHeight() + viewPartMainLayoutParams.topMargin + viewPartMainLayoutParams.bottomMargin;

        LayoutParams viewPartSlaveLayoutParams = (LayoutParams) viewPartSlave.getLayoutParams();
        viewPartSlaveWidth = viewPartSlave.getMeasuredWidth() + viewPartSlaveLayoutParams.leftMargin + viewPartSlaveLayoutParams.rightMargin;
        viewPartSlaveHeight = viewPartSlave.getMeasuredHeight() + viewPartSlaveLayoutParams.topMargin + viewPartSlaveLayoutParams.bottomMargin;

        int viewPartMainLineCount = viewPartMain.getLineCount();
        float viewPartMainLastLineWitdh = viewPartMainLineCount > 0 ? viewPartMain.getLayout().getLineWidth(viewPartMainLineCount - 1) : 0;

        widthSize = getPaddingLeft() + getPaddingRight();
        heightSize = getPaddingTop() + getPaddingBottom();

        if (viewPartMainLineCount > 1 && !(viewPartMainLastLineWitdh + viewPartSlaveWidth >= viewPartMain.getMeasuredWidth())) {
            widthSize += viewPartMainWidth;
            heightSize += viewPartMainHeight;
        } else if (viewPartMainLineCount > 1 && (viewPartMainLastLineWitdh + viewPartSlaveWidth >= availableWidth)) {
            widthSize += viewPartMainWidth;
            heightSize += viewPartMainHeight + viewPartSlaveHeight;
        } else if (viewPartMainLineCount == 1 && (viewPartMainWidth + viewPartSlaveWidth >= availableWidth)) {
            widthSize += viewPartMain.getMeasuredWidth();
            heightSize += viewPartMainHeight + viewPartSlaveHeight;
        } else {
            widthSize += viewPartMainWidth + viewPartSlaveWidth;
            heightSize += viewPartMainHeight;
        }

        this.setMeasuredDimension(widthSize, heightSize);
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (viewPartMain == null || viewPartSlave == null) {
            return;
        }

        viewPartMain.layout(
                getPaddingLeft(),
                getPaddingTop(),
                viewPartMain.getWidth() + getPaddingLeft(),
                viewPartMain.getHeight() + getPaddingTop());

        viewPartSlave.layout(
                right - left - viewPartSlaveWidth - getPaddingRight(),
                bottom - top - getPaddingBottom() - viewPartSlaveHeight,
                right - left - getPaddingRight(),
                bottom - top - getPaddingBottom());
    }
}