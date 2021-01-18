package com.strolink.whatsUp.ui.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;

import java.util.ArrayList;

public class CustomPaintView extends View implements View.OnTouchListener {
    private Paint mPaint;
    private Bitmap mDrawBit;

    private Canvas mPaintCanvas = null;
    private Path drawPath;
    private ArrayList<Pair<Path, Integer>> pathColorsList = new ArrayList<>();
    private ArrayList<Pair<Path, Integer>> undonePaths = new ArrayList<>();

    private int mColor;
    private RectF bounds;

    public CustomPaintView(Context context) {
        super(context);
        init();
    }

    public CustomPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomPaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        AppHelper.LogCat("width = " + getMeasuredWidth() + "     height = " + getMeasuredHeight());
        if (mDrawBit == null) {
            generatorBit();
        }
    }

    private void generatorBit() {
        mDrawBit = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mPaintCanvas = new Canvas(mDrawBit);
    }

    private void init() {

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        mColor = Color.RED;
        bounds = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(15f);
        drawPath = new Path();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        this.mPaint.setColor(mColor);


    }

    public void setWidth(float width) {
        this.mPaint.setStrokeWidth(width);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawBit != null) {
            canvas.drawBitmap(mDrawBit, 0, 0, null);
        }

        for (Pair<Path, Integer> path_clr : pathColorsList) {
            mPaint.setColor(path_clr.second);
            canvas.drawPath(path_clr.first, mPaint);
        }
        canvas.drawPath(drawPath, mPaint);
    }


    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        drawPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mPaintCanvas.drawPath(drawPath, mPaint);
        // kill this so we don't double draw
        pathColorsList.add(Pair.create(drawPath, mColor));
        drawPath = new Path();

    }

    public void onClickUndo() {

        int size = pathColorsList.size();
        if (size > 0) {
            undonePaths.add(Pair.create(pathColorsList.get(size - 1).first, pathColorsList.get(size - 1).second));
            pathColorsList.remove(size - 1);
            invalidate();
            reset();
        } else {
            AppHelper.CustomToast(getContext(), getContext().getString(R.string.nothing_to_undo));
        }
    }

    public void onClickRedo() {
        int size = undonePaths.size();
        if (size > 0) {
            pathColorsList.add(Pair.create(undonePaths.get(size - 1).first, undonePaths.get(size - 1).second));
            undonePaths.remove(size - 1);
            invalidate();
            reset();
        } else {

        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
        }
    }

    public Bitmap getPaintBit() {
        return mDrawBit;
    }

    public void reset() {
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
        }
        generatorBit();
    }

    public void setBounds(RectF bitmapRect) {
        this.bounds = bitmapRect;


    }
}
