package com.strolink.whatsUp.interfaces.picker;

import android.app.Activity;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public interface FastScrollStateChangeListener {
    /**
     * Called when fast scrolling begins
     */
    void onFastScrollStart(Activity fastScroller);

    /**
     * Called when fast scrolling ends
     */
    void onFastScrollStop(Activity fastScroller);
}
