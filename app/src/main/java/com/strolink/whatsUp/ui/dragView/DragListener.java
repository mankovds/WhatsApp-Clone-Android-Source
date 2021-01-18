package com.strolink.whatsUp.ui.dragView;

/**
 * Created by Abderrahim El imame on 7/11/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

/**
 * Enables to listen drag events.
 */
public interface DragListener {

    /**
     * Invoked when the view has just started to be dragged.
     */
    void onStartDraggingView();
    /**
     * Invoked when the view has  dragging.
     */
    void onDraggingView(float offset);

    /**
     * Invoked when the view has being dragged out of the screen
     * and just before calling activity.finish().
     */
    void onViewClosed();
}