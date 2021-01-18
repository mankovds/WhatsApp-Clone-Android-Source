package com.strolink.whatsUp.helpers.giph.net;


import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.giph.model.GiphyImage;
import com.strolink.whatsUp.helpers.giph.util.AsyncLoader;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.OkHttpClient;

public abstract class GiphyLoader extends AsyncLoader<List<GiphyImage>> {

    private static final String TAG = GiphyLoader.class.getName();
    private CountDownLatch latch;
    public static int PAGE_SIZE = 100;
    private   List<GiphyImage> results;
    @Nullable
    private String searchString;

    private final OkHttpClient client;

    protected GiphyLoader(@NonNull Context context, @Nullable String searchString) {
        super(context);
        this.searchString = searchString;
        this.client = new OkHttpClient.Builder().proxySelector(new GiphyProxySelector()).build();
    }

    @Override
    public List<GiphyImage> loadInBackground() {
        return loadPage(0);
    }



    @SuppressLint("CheckResult")
    public @NonNull
    List<GiphyImage> loadPage(int offset) {
        latch = new CountDownLatch(1);
        try {
            if (TextUtils.isEmpty(searchString)) {

                APIHelper.getGiphy(offset).subscribe(giphyResponse -> {
                    results = giphyResponse.getData();
                    latch.countDown();
                }, throwable -> {
                    AppHelper.LogCat("throwable " + throwable.getMessage());
                    latch.countDown();
                });
            } else {
                APIHelper.getGiphy(offset, searchString).subscribe(giphyResponse -> {
                    results = giphyResponse.getData();
                    latch.countDown();
                }, throwable -> {
                    AppHelper.LogCat("throwable " + throwable.getMessage());
                    latch.countDown();
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AppHelper.LogCat("results " + results.size());
            return results;


        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            return new LinkedList<>();
        }
    }
}
