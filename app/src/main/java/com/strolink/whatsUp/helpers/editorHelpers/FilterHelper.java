package com.strolink.whatsUp.helpers.editorHelpers;


import android.content.Context;

import com.strolink.whatsUp.models.ThumbnailFilter;
import com.zomato.photofilters.FilterPack;

import java.util.ArrayList;

public class FilterHelper {

    public FilterHelper() {
    }

    public ArrayList<ThumbnailFilter> getFilters(Context context) {
        ArrayList<ThumbnailFilter> thumbnailFilters = new ArrayList<>();
        thumbnailFilters.add(new ThumbnailFilter("Struck", FilterPack.getAweStruckVibeFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Clarendon", FilterPack.getClarendon(context)));
        thumbnailFilters.add(new ThumbnailFilter("OldMan", FilterPack.getOldManFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Mars", FilterPack.getMarsFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Rise", FilterPack.getRiseFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("April", FilterPack.getAprilFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Amazon", FilterPack.getAmazonFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Starlit", FilterPack.getStarLitFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Whisper", FilterPack.getNightWhisperFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Lime", FilterPack.getLimeStutterFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Haan", FilterPack.getHaanFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Bluemess", FilterPack.getBlueMessFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Adele", FilterPack.getAdeleFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Cruz", FilterPack.getCruzFilter(context)));
        thumbnailFilters.add(new ThumbnailFilter("Metropolis", FilterPack.getMetropolis(context)));
        thumbnailFilters.add(new ThumbnailFilter("Audrey", FilterPack.getAudreyFilter(context)));


        return thumbnailFilters;
    }
}
