package com.strolink.whatsUp.activities.media;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.media.GiphyAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.fragments.GiphyGifFragment;
import com.strolink.whatsUp.fragments.GiphyStickerFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.utils.ViewUtil;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class GiphyActivity extends BaseActivity implements GiphyActivityToolbar.OnLayoutChangedListener, GiphyActivityToolbar.OnFilterChangedListener,
        GiphyAdapter.OnItemClickListener {

    private static final String TAG = GiphyActivity.class.getSimpleName();



    private GiphyGifFragment gifFragment;
    private GiphyStickerFragment stickerFragment;


    private GiphyAdapter.GiphyViewHolder finishingImage;

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giphy);
        initializeToolbar();
        initializeResources();
    }

    private void initializeToolbar() {
        GiphyActivityToolbar toolbar = ViewUtil.findById(this, R.id.giphy_toolbar);
        toolbar.setOnFilterChangedListener(this);
        toolbar.setOnLayoutChangedListener(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeResources() {
        ViewPager viewPager = ViewUtil.findById(this, R.id.giphy_pager);
        TabLayout tabLayout = ViewUtil.findById(this, R.id.tab_layout);

        this.gifFragment = new GiphyGifFragment();
        this.stickerFragment = new GiphyStickerFragment();


        gifFragment.setClickListener(this);
        stickerFragment.setClickListener(this);

        viewPager.setAdapter(new GiphyFragmentPagerAdapter(this, getSupportFragmentManager(),
                gifFragment, stickerFragment));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onFilterChanged(String filter) {
        this.gifFragment.setSearchString(filter);
        this.stickerFragment.setSearchString(filter);
    }

    @Override
    public void onLayoutChanged(int type) {
        this.gifFragment.setLayoutManager(type);
        this.stickerFragment.setLayoutManager(type);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onClick(final GiphyAdapter.GiphyViewHolder viewHolder) {
        if (finishingImage != null) finishingImage.gifProgress.setVisibility(View.GONE);
        finishingImage = viewHolder;
        finishingImage.gifProgress.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, Uri>() {
            @Override
            protected Uri doInBackground(Void... params) {
                try {
                    AppHelper.LogCat("viewHolder.getFile()  " + viewHolder.getFile());
                    return Uri.fromFile(viewHolder.getFile());
                } catch (InterruptedException | ExecutionException e) {
                    AppHelper.LogCat(e);
                    return null;
                }
            }

            protected void onPostExecute(@Nullable Uri uri) {
                if (uri == null) {
                    Toast.makeText(GiphyActivity.this, R.string.GiphyActivity_error_while_retrieving_full_resolution_gif, Toast.LENGTH_LONG).show();
                } else if (viewHolder == finishingImage) {
                    setResult(RESULT_OK, new Intent().setData(uri));
                    finish();
                } else {
                    AppHelper.LogCat("Resolved Uri is no longer the selected element...");
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class GiphyFragmentPagerAdapter extends FragmentPagerAdapter {

        private final Context context;
        private final GiphyGifFragment gifFragment;
        private final GiphyStickerFragment stickerFragment;

        private GiphyFragmentPagerAdapter(@NonNull Context context,
                                          @NonNull FragmentManager fragmentManager,
                                          @NonNull GiphyGifFragment gifFragment,
                                          @NonNull GiphyStickerFragment stickerFragment) {
            super(fragmentManager);
            this.context = context.getApplicationContext();
            this.gifFragment = gifFragment;
            this.stickerFragment = stickerFragment;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return gifFragment;
            else return stickerFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return context.getString(R.string.GiphyFragmentPagerAdapter_gifs);
            else return context.getString(R.string.GiphyFragmentPagerAdapter_stickers);
        }
    }

}
