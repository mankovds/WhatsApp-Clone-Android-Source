package com.strolink.whatsUp.helpers.Files.cache;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 10/30/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class MemoryCache {
    public LruCache memCache;

    public MemoryCache() {
        initCache();
    }


    private void initCache() {
        int cacheSize = Math.min(15, ((ActivityManager) WhatsCloneApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 7) * 1024 * 1024;

        memCache = new LruCache(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldBitmap, Bitmap newBitmap) {

            }
        };
    }


    public boolean isExist(String id) {
        try {
            return isInCache(id);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Bitmap get(String id) {
        try {
            if (!isInCache(id))
                return null;
            return imageFromKey(id);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @SuppressLint("CheckResult")
    public void put(String id, Bitmap bitmap) {
        Observable.create((ObservableOnSubscribe<String>) subscriber -> {
            if (isInCache(id)) return;//zdtha
            try {
                addImage(id, bitmap);
                subscriber.onNext("The cache file is saved :");
                subscriber.onComplete();
            } catch (Throwable th) {
                th.printStackTrace();
                subscriber.onError(th);
            }
        }).subscribeOn(Schedulers.computation()).subscribe(string -> {

        },throwable -> {
            AppHelper.LogCat(" throwable " + throwable.getMessage());
        });
    }

    public void clear() {
        try {
            memCache.evictAll();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }


    private Bitmap imageFromKey(String key) {
        if (key == null) {
            return null;
        }
        return memCache.get(key);
    }

    private boolean isInCache(String key) {
        return memCache.get(key) != null;
    }

    public void removeImage(String key) {
        memCache.remove(key);
    }

    private void addImage(String key, Bitmap bitmap) {
        memCache.put(key, bitmap);

    }
}
