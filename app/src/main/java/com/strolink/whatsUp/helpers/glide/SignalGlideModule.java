package com.strolink.whatsUp.helpers.glide;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.giph.model.GiphyPaddedUrl;

import java.io.InputStream;

@GlideModule
public class SignalGlideModule extends AppGlideModule {

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setLogLevel(Log.ERROR);
        // set disk cache size & external vs. internal
        int cacheSize100MegaBytes = 104857600;

        builder.setDiskCache(new DiskLruCacheFactory(FilesManager.getDataCachedPathString(context), cacheSize100MegaBytes));//change location of cache even user clear cache of  the app
        //  builder.setDiskCache(new NoopDiskCacheFactory());
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.append(GiphyPaddedUrl.class, InputStream.class, new GiphyPaddedUrlLoader.Factory());
        //registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
    }

    public static class NoopDiskCacheFactory implements DiskCache.Factory {
        @Override
        public DiskCache build() {
            return new DiskCacheAdapter();
        }
    }
}
