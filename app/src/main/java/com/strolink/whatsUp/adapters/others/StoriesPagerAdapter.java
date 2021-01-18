package com.strolink.whatsUp.adapters.others;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.strolink.whatsUp.fragments.stories.StoryFragment;

/**
 * Created by Abderrahim El imame on 7/18/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public final class StoriesPagerAdapter extends FragmentStatePagerAdapter {

    private int size;
    private int currentStoryPosition;
    private String storyId;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public StoriesPagerAdapter(FragmentManager fragmentManager, int size, String storyId) {
        super(fragmentManager);
        this.size = size;
        this.storyId = storyId;
    }

    public StoriesPagerAdapter(FragmentManager fragmentManager, int size, String storyId, int currentStoryPosition) {
        super(fragmentManager);
        this.size = size;
        this.storyId = storyId;
        this.currentStoryPosition = currentStoryPosition;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        final Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putInt("currentStoryPosition", currentStoryPosition);
        bundle.putString("storyId", storyId);
        StoryFragment fragment = new StoryFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return size;
    }


    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }


}
