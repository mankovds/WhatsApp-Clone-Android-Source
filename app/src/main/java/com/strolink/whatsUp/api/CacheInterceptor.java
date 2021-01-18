package com.strolink.whatsUp.api;

import androidx.annotation.NonNull;

import com.strolink.whatsUp.helpers.AppHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by Abderrahim El imame on 2019-07-30.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class CacheInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        CacheControl cacheControl;
        if (AppHelper.internetAvailable()) {
            cacheControl = new CacheControl.Builder()
                    .maxAge(5, TimeUnit.SECONDS)
                    .build();
        } else {

            cacheControl = new CacheControl.Builder()
                    .maxAge(7, TimeUnit.DAYS)
                    .onlyIfCached()
                    .build();
        }

        return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", cacheControl.toString())
                .build();
    }
}