package com.strolink.whatsUp.interfaces.video;

/**
 * Created by Abderrahim El imame on 12/14/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public interface VideoTimelineViewListener {

    void onLeftProgressChanged(float progress);

    void onRightProgressChanged(float progress);

    void didStartDragging();

    void didStopDragging();
}
