package com.strolink.whatsUp.activities.media;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.others.TabsMediaAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;
import com.vanniktech.emoji.EmojiTextView;

import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Abderrahim El imame on 1/24/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class MediaActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.toolbar_title)
    EmojiTextView toolbarTitle;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHelper.isAndroid5()) {
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorBlack));
        }
        setContentView(R.layout.activity_media);
        ButterKnife.bind(this);
        initializerView();

        if (getIntent().hasExtra("Username")) {
            toolbarTitle.setText(getIntent().getStringExtra("Username"));
        }

    }

    private void initializerView() {
        viewPager.setAdapter(new TabsMediaAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewPager.setCurrentItem(0);
        tabLayout.getTabAt(0).setCustomView(R.layout.custom_tab_media);
        tabLayout.getTabAt(1).setCustomView(R.layout.custom_tab_documents);
        tabLayout.getTabAt(2).setCustomView(R.layout.custom_tab_links);
        ((TextView) findViewById(R.id.title_tabs_media)).setTextColor(AppHelper.getColor(this, R.color.colorSelectedTab));
        ((TextView) findViewById(R.id.title_tabs_documents)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelectedMedia));
        ((TextView) findViewById(R.id.title_tabs_links)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelectedTab));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        viewPager.setCurrentItem(0);
                        ((TextView) findViewById(R.id.title_tabs_media)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorSelectedTab));
                        break;
                    case 1:
                        viewPager.setCurrentItem(1);
                        ((TextView) findViewById(R.id.title_tabs_documents)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorSelectedTab));
                        break;
                    case 2:
                        viewPager.setCurrentItem(2);
                        ((TextView) findViewById(R.id.title_tabs_links)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorSelectedTab));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        ((TextView) findViewById(R.id.title_tabs_media)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorUnSelectedMedia));
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.title_tabs_documents)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorUnSelectedMedia));
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.title_tabs_links)).setTextColor(AppHelper.getColor(MediaActivity.this, R.color.colorUnSelectedTab));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.arrow_back)
    public void backPressed() {
        finish();

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
