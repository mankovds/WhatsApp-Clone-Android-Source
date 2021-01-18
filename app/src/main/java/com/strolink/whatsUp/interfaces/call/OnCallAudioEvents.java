package com.strolink.whatsUp.interfaces.call;

/**
 * Created by Abderrahim El imame on 5/29/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public interface OnCallAudioEvents {

    void onCallHangUp(boolean clicked);

    void onMute();

    void onSpeaker();
}
