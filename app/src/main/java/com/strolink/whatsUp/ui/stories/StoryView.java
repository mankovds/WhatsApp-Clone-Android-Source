package com.strolink.whatsUp.ui.stories;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;

import java.util.List;

/**
 * Created by Abderrahim El imame on 7/23/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoryView extends View {

    public static final int STORY_IMAGE_HEIGHT_WIDTH = 50;
    public static final int STORY_INDICATOR_WIDTH_IN_DP = 4;
    public static final int SPACE_BETWEEN_IMAGE_AND_INDICATOR = 4;
    public static final int START_ANGLE = 270;
    public static int ANGEL_OF_GAP = 16;
    public static final String PENDING_INDICATOR_COLOR = "#009988";
    public static final String VISITED_INDICATOR_COLOR = "#E6E6E6";
    private int mStoryImageHeightWidthInPx;
    private int mStoryIndicatorWidthInPx;
    private int mSpaceBetweenImageAndIndicator;
    private int mPendingIndicatorColor;
    private int mVisitedIndicatorColor;
    private int mViewWidth;
    private int mViewHeight;
    private Resources resources;

    private Paint mIndicatorPaint;
    private List<StoryModel> storiesModels;
    private int indicatorSweepAngle;
    private Bitmap mIndicatorImageBitmap;
    private Rect mIndicatorImageRect;
    private RectF mRectF;
    private Context mContext;

    public StoryView(Context context) {
        super(context);
        init(context);
        setDefaults();
    }

    public StoryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StoryView, 0, 0);
        try {

            mStoryImageHeightWidthInPx = getPxFromDp(STORY_IMAGE_HEIGHT_WIDTH);
            mStoryIndicatorWidthInPx = getPxFromDp((int) ta.getDimension(R.styleable.StoryView_storyItemIndicatorWidth, STORY_INDICATOR_WIDTH_IN_DP));
            mSpaceBetweenImageAndIndicator = getPxFromDp((int) ta.getDimension(R.styleable.StoryView_spaceBetweenImageAndIndicator, SPACE_BETWEEN_IMAGE_AND_INDICATOR));
            mPendingIndicatorColor = ta.getColor(R.styleable.StoryView_pendingIndicatorColor, Color.parseColor(PENDING_INDICATOR_COLOR));
            mVisitedIndicatorColor = ta.getColor(R.styleable.StoryView_visitedIndicatorColor, Color.parseColor(VISITED_INDICATOR_COLOR));
        } finally {
            ta.recycle();
        }
        prepareValues();
    }

    private void init(Context context) {
        this.mContext = context;
        resources = context.getResources();
        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void setDefaults() {
        mStoryImageHeightWidthInPx = getPxFromDp(STORY_IMAGE_HEIGHT_WIDTH);
        mStoryIndicatorWidthInPx = getPxFromDp(STORY_INDICATOR_WIDTH_IN_DP);
        mSpaceBetweenImageAndIndicator = getPxFromDp(SPACE_BETWEEN_IMAGE_AND_INDICATOR);
        mPendingIndicatorColor = Color.parseColor(PENDING_INDICATOR_COLOR);
        mVisitedIndicatorColor = Color.parseColor(VISITED_INDICATOR_COLOR);
        prepareValues();
    }

    public void setPendingIndicatorColor(int color) {
        mPendingIndicatorColor = color;
    }

    public void setStoryImageHeightWidth(int heightWidthInPx) {
        mStoryImageHeightWidthInPx = getPxFromDp(heightWidthInPx);
    }

    public void setVisitedIndicatorColor(int color) {

        mVisitedIndicatorColor = color;
    }

    private void prepareValues() {
        mViewHeight = mStoryImageHeightWidthInPx;
        mViewWidth = mStoryImageHeightWidthInPx;
        int mIndicatoryOffset = mStoryIndicatorWidthInPx / 2;
        int mIndicatorImageOffset = mStoryIndicatorWidthInPx + mSpaceBetweenImageAndIndicator;
        mIndicatorImageRect = new Rect(mIndicatorImageOffset, mIndicatorImageOffset, mViewWidth - mIndicatorImageOffset, mViewHeight - mIndicatorImageOffset);
        mRectF = new RectF(mIndicatoryOffset, mIndicatoryOffset, mViewWidth - mIndicatoryOffset, mViewHeight - mIndicatoryOffset);
    }

    public void setStoriesModels(List<StoryModel> storiesModels) {
        this.storiesModels = storiesModels;
        calculateSweepAngle(storiesModels.size());
        invalidate();
        loadFirstImageBitmap(storiesModels.get(storiesModels.size() - 1).getFile(), storiesModels.get(storiesModels.size() - 1).getType());


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mIndicatorPaint.setColor(mPendingIndicatorColor);
        mIndicatorPaint.setStrokeWidth(mStoryIndicatorWidthInPx);
        int startAngle = START_ANGLE + ANGEL_OF_GAP / 2;
        if (storiesModels == null) return;
        for (int i = 0; i < storiesModels.size(); i++) {
            mIndicatorPaint.setColor(getIndicatorColor(i));
            canvas.drawArc(mRectF, startAngle, indicatorSweepAngle - ANGEL_OF_GAP / 2, false, mIndicatorPaint);
            startAngle += indicatorSweepAngle + ANGEL_OF_GAP / 2;
        }
        if (mIndicatorImageBitmap != null) {
            canvas.drawBitmap(mIndicatorImageBitmap, null, mIndicatorImageRect, null);
        }
    }

    private int getIndicatorColor(int index) {
        if (!storiesModels.get(index).isUploaded()) {
            return AppHelper.getColor(getContext(), R.color.colorRedDark);
        } else {
            return storiesModels.get(index).isDownloaded() ? mVisitedIndicatorColor : mPendingIndicatorColor;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getPaddingRight() + getPaddingLeft() + mViewWidth;
        int height = getPaddingTop() + getPaddingBottom() + mViewHeight;
        int w = resolveSizeAndState(width, widthMeasureSpec, 0);
        int h = resolveSizeAndState(height, heightMeasureSpec, 0);
        setMeasuredDimension(w, h);
    }

    private void loadFirstImageBitmap(String file, String fileType) {

        if (fileType.equals("video")) {

            long interval = 5000 * 1000;
            RequestOptions options = new RequestOptions().frame(interval);


            if (file.startsWith("/storage")) {
                GlideApp.with(mContext)
                        .asBitmap()
                        .load(file)
                        .signature(new ObjectKey(file))
                        .dontAnimate()
                        .apply(RequestOptions.circleCropTransform())
                        .apply(options)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                mIndicatorImageBitmap = resource;
                                invalidate();
                            }
                        });
            } else {
                GlideApp.with(mContext)
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + file))
                        .signature(new ObjectKey(file))
                        .dontAnimate()
                        .apply(RequestOptions.circleCropTransform())
                        .apply(options)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                mIndicatorImageBitmap = resource;
                                invalidate();
                            }
                        });
            }

        } else {

            if (file.startsWith("/storage")) {
                GlideApp.with(mContext)
                        .asBitmap()
                        .apply(RequestOptions.circleCropTransform())
                        .load(file)
                        .signature(new ObjectKey(file))
                        .dontAnimate()
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                mIndicatorImageBitmap = resource;
                                invalidate();
                            }
                        });
            } else {
                GlideApp.with(mContext)
                        .asBitmap()
                        .apply(RequestOptions.circleCropTransform())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + file))
                        .signature(new ObjectKey(file))
                        .dontAnimate()
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                mIndicatorImageBitmap = resource;
                                invalidate();
                            }
                        });
            }


        }

    }

    private void calculateSweepAngle(int itemCounts) {
        if (itemCounts == 1) {
            ANGEL_OF_GAP = 0;
        }
        this.indicatorSweepAngle = (360 / itemCounts) - ANGEL_OF_GAP / 2;
    }

    private int getPxFromDp(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.getDisplayMetrics());
    }
}