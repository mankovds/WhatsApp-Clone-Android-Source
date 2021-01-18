package com.strolink.whatsUp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by Abderrahim El imame on 12/18/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CircleVideo extends GLSurfaceView {
    public CircleVideo(Context context) {
        super(context);
    }

    public CircleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Path clippingPath;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            int radius = Math.min(w, h) / 2;
            clippingPath = new Path();
            clippingPath.addCircle(w / 2, h / 2, radius, Path.Direction.CW);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = canvas.save();
        canvas.clipPath(clippingPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }
}
