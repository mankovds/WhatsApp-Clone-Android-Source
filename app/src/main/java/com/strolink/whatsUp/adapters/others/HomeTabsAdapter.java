package com.strolink.whatsUp.adapters.others;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.strolink.whatsUp.fragments.home.CallsFragment;
import com.strolink.whatsUp.fragments.home.CameraFragment;
import com.strolink.whatsUp.fragments.home.ContactsFragment;
import com.strolink.whatsUp.fragments.home.ConversationsFragment;

/**
 * Created by Abderrahim El imame on 27/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class HomeTabsAdapter extends FragmentStatePagerAdapter {


    public HomeTabsAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return CameraFragment.newInstance();
            case 1:
                return new ConversationsFragment();
            case 2:
                return new CallsFragment();
            case 3:
                return new ContactsFragment();
            default:
                return new ConversationsFragment();
        }

    }

    @Override
    public int getCount() {
        return 4;
    }
}