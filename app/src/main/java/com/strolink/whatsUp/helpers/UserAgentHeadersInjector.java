package com.strolink.whatsUp.helpers;

import com.danikula.videocache.headers.HeaderInjector;
import com.strolink.whatsUp.app.WhatsCloneApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abderrahim El imame on 12/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class UserAgentHeadersInjector implements HeaderInjector {

    @Override
    public Map<String, String> addHeaders(String url) {
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", PreferenceManager.getInstance().getToken(WhatsCloneApplication.getInstance()));
        return map;
    }
}