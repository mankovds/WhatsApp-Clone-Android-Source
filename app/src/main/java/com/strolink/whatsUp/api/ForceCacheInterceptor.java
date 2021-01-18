package com.strolink.whatsUp.api;

import com.strolink.whatsUp.helpers.AppHelper;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Abderrahim El imame on 2019-07-30.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class ForceCacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (!AppHelper.internetAvailable()) {
            builder.cacheControl(CacheControl.FORCE_CACHE);
        }

        return chain.proceed(builder.build());
    }
}

