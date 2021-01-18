package com.strolink.whatsUp.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.content.Loader;


import com.strolink.whatsUp.helpers.giph.model.GiphyImage;
import com.strolink.whatsUp.helpers.giph.net.GiphyGifLoader;

import java.util.List;

public class GiphyGifFragment extends GiphyFragment {

    @NonNull
    @Override
    public Loader<List<GiphyImage>> onCreateLoader(int id, Bundle args) {
        return new GiphyGifLoader(getActivity(), searchString);
    }

}
