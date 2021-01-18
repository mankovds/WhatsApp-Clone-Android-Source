package com.strolink.whatsUp.activities.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.NewConversationContactsActivity;
import com.strolink.whatsUp.activities.messages.TransferMessageContactsActivity;
import com.strolink.whatsUp.activities.settings.AboutActivity;
import com.strolink.whatsUp.activities.settings.SettingsActivity;
import com.strolink.whatsUp.activities.status.StatusActivity;
import com.strolink.whatsUp.activities.stories.StoriesListActivity;
import com.strolink.whatsUp.activities.stories.StoriesPrivacyActivity;
import com.strolink.whatsUp.activities.welcome.IntroActivity;
import com.strolink.whatsUp.adapters.others.HomeTabsAdapter;
import com.strolink.whatsUp.adapters.others.TextWatcherAdapter;
import com.strolink.whatsUp.adapters.others.WhatsCloneViewPager;
import com.strolink.whatsUp.adapters.recyclerView.stories.StoriesAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.ForegroundRuning;
import com.strolink.whatsUp.helpers.GDPRHelper;
import com.strolink.whatsUp.helpers.OutDateHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.RateHelper;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.interfaces.OnBackPressed;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.CallsController;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.stories.StoriesPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_SESSION_EXPIRED;

/**
 * Created by Abderrahim El imame on 01/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MainActivity extends BaseActivity {


    @BindView(R.id.adParentLyout)
    LinearLayout adParentLyout;


    @BindView(R.id.main_view)
    LinearLayout MainView;


    @BindView(R.id.floatingBtnMain)
    FloatingActionButton floatingBtnMain;


    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.appbar)
    View appbar;

    @BindView(R.id.container)
    WhatsCloneViewPager whatsCloneViewPager;


    InterstitialAd mInterstitialAd;
    private boolean adsAddeed = false;
    boolean actionModeStarted = false;
    private int START_INDEX = 1;
    private OnBackPressed onBackPressed;
    private ContactsChangeObserver observer;
//stories

    @BindView(R.id.storiesList)
    RecyclerView storiesList;

    private StoriesAdapter storiesAdapter;
    private StoriesPresenter storiesPresenter;


    @BindView(R.id.app_bar)
    Toolbar toolbar;
    //search
    @BindView(R.id.appBar_layout)
    AppBarLayout appBar;

    @BindView(R.id.layout_appbar_search)
    View searchAppBarLayout;

    @BindView(R.id.app_bar_search_view)
    View searchToolBar;

    @BindView(R.id.search_input)
    TextInputEditText searchEditText;

    @BindView(R.id.close_btn_search_view)
    AppCompatImageView closeBtn;

    @BindView(R.id.clear_btn_search_view)
    AppCompatImageView clearBtn;

    private float positionFromRight = 2;
    //search


    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + "closePickerActivity")) {
                if (whatsCloneViewPager.getCurrentItem() == 0)
                    whatsCloneViewPager.setCurrentItem(1);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setMainActivity(true);


        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("first_time")) {
                if (getIntent().getExtras().getBoolean("first_time")) {
                    if (PreferenceManager.getInstance().getToken(this) != null) {
                        WhatsCloneApplication.getInstance().preInitMqtt();
                        initializerApplication();
                    }

                }
            }
        }
        EventBus.getDefault().register(this);

        initializerView(savedInstanceState);
        initSearchBar();
        setupToolbar();
        initializePermissions();
        loadCounter();
        RateHelper.appLaunched(this);
        PreferenceManager.getInstance().setIsNeedInfo(this, false);


        if (PreferenceManager.getInstance().ShowInterstitialrAds(MainActivity.this)) {
            if (PreferenceManager.getInstance().getUnitInterstitialAdID(MainActivity.this) != null) {
                initializerAds();
                showMainAds();
            }
        }

        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("missed_call")) {
                if (getIntent().getExtras().getBoolean("missed_call")) {
                    whatsCloneViewPager.setCurrentItem(2);
                }
            }
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + "closePickerActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

        //listen for changes contacts
        observer = new ContactsChangeObserver(null, this);
        getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer);
        storiesPresenter = new StoriesPresenter(this);
        storiesPresenter.onCreate();
    }


    /**
     * Initialize searchBar.
     */
    private void initSearchBar() {

        if (searchToolBar != null) {
            searchAppBarLayout.setVisibility(View.GONE);

            searchEditText.addTextChangedListener(new TextWatcherAdapter() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    clearBtn.setVisibility(View.GONE);
                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Search(s.toString().trim());
                    clearBtn.setVisibility(View.VISIBLE);
                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void afterTextChanged(Editable s) {

                    if (s.length() == 0) {
                        clearBtn.setVisibility(View.GONE);

                    }
                }
            });
            clearBtn.setOnClickListener(view -> clearSearchView());
            closeBtn.setOnClickListener(view -> hideSearchBar(positionFromRight));
        }
    }

    /**
     * method to clear/reset the search view
     */
    public void clearSearchView() {
        if (searchEditText.getText() != null) {
            searchEditText.setText("");


        }
    }

    /**
     * method to start searching
     *
     * @param query this  is parameter for Search method
     */
    public void Search(String query) {
        if (whatsCloneViewPager.getCurrentItem() == 1) {
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SEARCH_QUERY_CHAT, query));
        } else if (whatsCloneViewPager.getCurrentItem() == 2) {
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SEARCH_QUERY_CALLS, query));
        } else if (whatsCloneViewPager.getCurrentItem() == 3) {
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SEARCH_QUERY_CONTACTS, query));
        }
    }

    public void initializerApplication() {


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {

            WorkJobsManager.getInstance().initializerApplicationService();
            WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
            WorkJobsManager.getInstance().sendUserMessagesToServer();
            WorkJobsManager.getInstance().sendUserStoriesToServer();
            WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
            WorkJobsManager.getInstance().sendDeletedStoryToServer();

        }

    }

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();


    }

    @SuppressLint("InlinedApi")
    private void initializePermissions() {
        Permissions.with(MainActivity.this)
                .request(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.app_needs_access_to_your_contacts_and_media_in_order_to_connect_with_friends), R.drawable.ic_contacts_white_24dp)
                .execute();
    }


    private void initializerAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(PreferenceManager.getInstance().getUnitInterstitialAdID(this));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                AppHelper.LaunchActivity(MainActivity.this, SettingsActivity.class);
            }
        });

        requestNewInterstitial();
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        if (mInterstitialAd != null)
            mInterstitialAd.loadAd(adRequest);
    }

    public void settings() {
        RateHelper.significantEvent(this);
        if (PreferenceManager.getInstance().ShowInterstitialrAds(this)) {
            try {
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    AppHelper.LaunchActivity(this, SettingsActivity.class);
                }
            } catch (Exception e) {
                AppHelper.LaunchActivity(this, SettingsActivity.class);
            }

        } else {
            AppHelper.LaunchActivity(this, SettingsActivity.class);
        }

    }

    public void about_app() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, AboutActivity.class);

    }

    public void status() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, StatusActivity.class);

    }


    public void stories() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, StoriesListActivity.class);

    }

    public void stories_privacy() {
        RateHelper.significantEvent(this);
        AppHelper.LaunchActivity(this, StoriesPrivacyActivity.class);

    }

    public void privacy_termes() {

        RateHelper.significantEvent(this);
        // Apply some fancy animation to show off
        CustomTabsIntent customTabsIntent = getDefaultCustomTabsIntentBuilder()
                .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
                .build();

        CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent);
        try {
            // This is where the magic happens...
            CustomTabsHelper.openCustomTab(MainActivity.this, customTabsIntent, Uri.parse(PreferenceManager.getInstance().getPrivacyLink(this)), new WebViewFallback());

        } catch (Exception e) {
            AppHelper.LogCat("Exception  " + e.getMessage());
        }

    }

    private CustomTabsIntent.Builder getDefaultCustomTabsIntentBuilder() {
        return new CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setToolbarColor(AppHelper.getColor(this, R.color.colorPrimary))
                .setShowTitle(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.search_conversations:
            case R.id.search_calls:
            case R.id.search_contacts:

                RateHelper.significantEvent(this);
                showSearchBar(positionFromRight);
                break;

            case R.id.clear_log_call:

                removeCallsLog();
                break;
            case R.id.settings:

                settings();
                break;
            case R.id.about_app:

                about_app();
                break;
            case R.id.privacy_termes:

                privacy_termes();
                break;
            case R.id.status:

                status();
                break;
            case R.id.my_stories:
                stories();
                break;
            case R.id.stories_privacy:

                stories_privacy();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCallsLog() {
        RateHelper.significantEvent(this);

        AppHelper.showDialog(this, getString(R.string.delete_call_dialog));


        List<CallsModel> callsModels = CallsController.getInstance().loadAllCalls();
        for (CallsModel callsModel : callsModels) {
            List<CallsInfoModel> callsInfoModel = CallsController.getInstance().loadAllCallsInfo(callsModel.getC_id());
            for (CallsInfoModel callsInfoModel1 : callsInfoModel)
                CallsController.getInstance().deleteCallInfo(callsInfoModel1);

            CallsController.getInstance().deleteCall(callsModel);

        }


        AppHelper.hideDialog();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CALL_ITEM, ""));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        switch (whatsCloneViewPager.getCurrentItem()) {

            case 1:
                getMenuInflater().inflate(R.menu.conversations_menu, menu);
                break;

            case 2:
                getMenuInflater().inflate(R.menu.calls_menu, menu);
                break;
            case 3:
                getMenuInflater().inflate(R.menu.contacts_menu, menu);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);

        }


    }


    /**
     * method to initialize the view
     *
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initializerView(Bundle savedInstanceState) {


        if (PreferenceManager.getInstance().isOutDate(this)) {
            OutDateHelper.appLaunched(this);
            OutDateHelper.significantEvent(this);
        }

        HomeTabsAdapter homeAdapter = new HomeTabsAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        whatsCloneViewPager.setAdapter(homeAdapter);
        whatsCloneViewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(whatsCloneViewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        whatsCloneViewPager.setCurrentItem(START_INDEX);
        whatsCloneViewPager.bindViews(appbar);
        whatsCloneViewPager.initTransformer(savedInstanceState, false);

        tabLayout.getTabAt(0).setCustomView(R.layout.custom_tab_camera);
        tabLayout.getTabAt(1).setCustomView(R.layout.custom_tab_messages);
        tabLayout.getTabAt(2).setCustomView(R.layout.custom_tab_calls);
        tabLayout.getTabAt(3).setCustomView(R.layout.custom_tab_contacts);
        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelected));
        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(this, R.color.colorUnSelected));
        setupTab();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (searchAppBarLayout.getVisibility() == View.VISIBLE)
                    hideSearchBar(positionFromRight);

                Drawable icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_chat_white_24dp);
                switch (tab.getPosition()) {

                    case 1:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_chat_white_24dp);
                        whatsCloneViewPager.setCurrentItem(1);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_messages)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;
                    case 2:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_call_white_24dp);
                        whatsCloneViewPager.setCurrentItem(2);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter));
                        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;

                    case 3:
                        icon = AppHelper.getVectorDrawable(MainActivity.this, R.drawable.ic_share_white_24dp);
                        whatsCloneViewPager.setCurrentItem(3);
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorWhite));
                        break;
                    default:
                        break;
                }


                if (tab.getPosition() != 1) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_FINISHED));
                }
                if (tab.getPosition() != 0) {
                    floatingBtnMain.setVisibility(View.VISIBLE);

                    floatingBtnMain.setImageDrawable(icon);
                    final Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_for_button_animtion_enter);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            floatingBtnMain.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    floatingBtnMain.startAnimation(animation);
                } else {
                    floatingBtnMain.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {

                    case 1:
                        findViewById(R.id.counterTabMessages).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_messages)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;
                    case 2:
                        findViewById(R.id.counterTabCalls).setBackground(AppHelper.getDrawable(MainActivity.this, R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_calls)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;

                    case 3:
                        findViewById(R.id.counterTabMessages).setBackground(getResources().getDrawable(R.drawable.bg_circle_tab_counter_unselected));
                        findViewById(R.id.counterTabCalls).setBackground(getResources().getDrawable(R.drawable.bg_circle_tab_counter_unselected));
                        ((TextView) findViewById(R.id.title_tabs_contacts)).setTextColor(AppHelper.getColor(MainActivity.this, R.color.colorUnSelected));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


            }
        });


        floatingBtnMain.setOnClickListener(view -> {
            Intent intent;
            switch (whatsCloneViewPager.getCurrentItem()) {

                case 1:

                    RateHelper.significantEvent(this);
                    AppHelper.LaunchActivity(this, NewConversationContactsActivity.class);

                    break;

                case 2:
                    RateHelper.significantEvent(this);
                    intent = new Intent(this, TransferMessageContactsActivity.class);
                    intent.putExtra("forCall", true);
                    startActivity(intent);
                    break;
                case 3:
                    RateHelper.significantEvent(this);

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invitation_from) + " " + PreferenceManager.getInstance().getPhone(this));
                    shareIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.INVITE_MESSAGE_SMS + String.format(getString(R.string.rate_helper_google_play_url), getPackageName()));
                    shareIntent.setType("text/*");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.shareItem)));
                    break;

            }
        });


        storiesAdapter = new StoriesAdapter(this, storiesList);
        PreCachingLayoutManager linearLayoutManager = new PreCachingLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        linearLayoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        storiesList.setLayoutManager(linearLayoutManager);

        storiesAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        storiesList.setAdapter(storiesAdapter);
        storiesList.setItemAnimator(new DefaultItemAnimator());
        storiesList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        storiesList.setHasFixedSize(true);
        storiesList.setItemViewCacheSize(10);
        storiesList.setDrawingCacheEnabled(true);
        storiesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end


    }

    /**
     * method to show stories list
     *
     * @param storiesHeaderModel this is parameter for  UpdateStories  method
     */
    public void UpdateStories(StoriesHeaderModel storiesHeaderModel) {
        if (storiesHeaderModel != null) {
            storiesList.setVisibility(View.VISIBLE);
            storiesAdapter.setStoriesHeaderModel(storiesHeaderModel);
        }

    }

    public void UpdateStories(List<StoriesModel> storiesModels) {


        if (storiesModels.size() != 0) {
            storiesList.setVisibility(View.VISIBLE);
            storiesAdapter.setStoriesModelList(storiesModels);

        }
    }

    private void setupTab() {
        View tab = ((LinearLayout) tabLayout.getChildAt(0)).getChildAt(0);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) (tab).getLayoutParams();
        params.weight = 0f;
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        tab.setLayoutParams(params);
    }

    private void showMainAds() {
        if (PreferenceManager.getInstance().ShowBannerAds(this)) {
            adParentLyout.setVisibility(View.VISIBLE);
            if (PreferenceManager.getInstance().getUnitBannerAdsID(this) != null) {
                if (!adsAddeed) {
                    AdView mAdView = new AdView(this);
                    mAdView.setAdSize(AdSize.BANNER);
                    mAdView.setAdUnitId(PreferenceManager.getInstance().getUnitBannerAdsID(this));
                    AdRequest.Builder builder = new AdRequest.Builder();


                    if (AppConstants.DEBUGGING_MODE) {
                        builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
                    }

                    AdRequest adRequest = builder.build();

                    if (mAdView.getAdSize() != null || mAdView.getAdUnitId() != null)
                        mAdView.loadAd(adRequest);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    adParentLyout.addView(mAdView, params);
                    adsAddeed = true;
                }
            }
        } else {
            adParentLyout.setVisibility(View.GONE);
        }
        if (PreferenceManager.getInstance().getPublisherId(this) != null) {
            GDPRHelper gdprHelper = new GDPRHelper().withContext(this);
            gdprHelper.withPrivacyUrl(PreferenceManager.getInstance().getPrivacyLink(this))
                    .withPublisherIds(PreferenceManager.getInstance().getPublisherId(this));
            if (AppConstants.DEBUGGING_MODE) {
                // gdprHelper.withTestMode("A75438FF5B33879362E7F428378F0102");
                gdprHelper.withTestMode();
            }
            gdprHelper.check();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainView.setVisibility(View.GONE);


    }


    @Override
    protected void onPause() {
        super.onPause();
        MainView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);


        if (mLocalBroadcastManager != null)
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);

        if (observer != null) {
            getApplicationContext().getContentResolver().unregisterContentObserver(observer);
        }


    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {

            case AppConstants.EVENT_BUS_DELETE_STORIES_ITEM:
            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW:
            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW:
            case AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_OLD_ROW:
            case AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_NEW_ROW:

                storiesPresenter.onRefresh();
                break;
            case EVENT_BUS_MESSAGE_COUNTER:
                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(this::loadCounter, 500);
                });
                break;
            case AppConstants.EVENT_BUS_NEW_CONTACT_ADDED:

                break;
            case AppConstants.EVENT_BUS_START_CONVERSATION:
                if (whatsCloneViewPager.getCurrentItem() == 2)
                    whatsCloneViewPager.setCurrentItem(1);
                break;
            case AppConstants.EVENT_BUS_ACTION_MODE_STARTED:
                actionModeStarted();
                break;
            case AppConstants.EVENT_BUS_ACTION_MODE_DESTROYED:
                actionModeDestroyed();
                break;

            case EVENT_BUS_SESSION_EXPIRED:
                if (ForegroundRuning.get().isForeground()) {
                    AppHelper.runOnUIThread(() -> {


                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setMessage(R.string.your_session_expired);
                        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                            PreferenceManager.getInstance().setToken(this, null);
                            PreferenceManager.getInstance().setID(this, null);
                            PreferenceManager.getInstance().setIsWaitingForSms(this, false);
                            PreferenceManager.getInstance().setMobileNumber(this, null);
                            PreferenceManager.getInstance().clearPreferences(this);
                            DbBackupRestore.deleteData(this);
                            NotificationsManager.getInstance().SetupBadger(this);
                            AppHelper.deleteCache(this);
                            Intent intent = new Intent(this, IntroActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
/*
                        try {
                            pahoMqttClient.disconnect(mqttAndroidClient);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }*/// TODO: 2019-12-02 khasni ndir unsubscribe and disconnect

                        });
                        alert.setCancelable(false);
                        alert.show();
                    });
                }
                break;

        }


    }


    /**
     * method to check if user connect in an other device
     */


    private void actionModeDestroyed() {
        if (actionModeStarted) {
            actionModeStarted = false;
            tabLayout.setBackground(AppHelper.getDrawable(this, R.drawable.toolbar_background));
            if (AppHelper.isAndroid5()) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
            }
        }
    }

    private void actionModeStarted() {
        if (!actionModeStarted) {
            actionModeStarted = true;
            tabLayout.setBackgroundColor(AppHelper.getColor(this, R.color.colorActionMode));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(AppHelper.getColor(this, R.color.colorActionMode));
            }
        }

    }


    /**
     * methdo to loadCircleImage number of unread messages
     */
    int messageCounter = 0;

    private void loadCounter() {


        try {

            List<ConversationModel> conversationsModel1 = MessagesController.getInstance().loadAllUnreadChats();


            if (conversationsModel1.size() != 0) {
                messageCounter = conversationsModel1.size();
            }

        } catch (Exception e) {
            AppHelper.LogCat("loadCounter main activity " + e.getMessage());
        }


        if (messageCounter == 0) {
            findViewById(R.id.counterTabMessages).setVisibility(View.GONE);
        } else {
            if (whatsCloneViewPager.getCurrentItem() == 0) {
                findViewById(R.id.counterTabMessages).setVisibility(View.VISIBLE);

                if (messageCounter > 99)
                    ((TextView) findViewById(R.id.counterTabMessages)).setText(getString(R.string.plus_99));
                else
                    ((TextView) findViewById(R.id.counterTabMessages)).setText(String.valueOf(messageCounter));
            }
        }

        NotificationsManager.getInstance().SetupBadger(this);
    }

    @Override
    public void onBackPressed() {

        if (whatsCloneViewPager != null && whatsCloneViewPager.getCurrentItem() != START_INDEX) {
            if (whatsCloneViewPager.getCurrentItem() == 0) {
                try {
                    onBackPressed.onBackPressedFragment();
                } catch (Exception e) {
                    AppHelper.LogCat("Exception " + e.getMessage());
                }

            }
            whatsCloneViewPager.setCurrentItem(START_INDEX);
        } else {

            if (searchAppBarLayout.getVisibility() == View.VISIBLE)
                hideSearchBar(positionFromRight);
            else
                super.onBackPressed();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        whatsCloneViewPager.saveState(outState);
    }

    /**
     * to show the searchAppBarLayout and hide the mainAppBar with animation.
     *
     * @param positionFromRight
     */
    private void showSearchBar(float positionFromRight) {

        if (AppHelper.isAndroid5()) {

            // start x-index for circular animation
            int cx = toolbar.getWidth() - (int) (getResources().getDimension(R.dimen.dp48) * (0.5f + positionFromRight));
            // start y-index for circular animation
            int cy = (toolbar.getTop() + toolbar.getBottom()) / 2;

            // calculate max radius
            int dx = Math.max(cx, toolbar.getWidth() - cx);
            int dy = Math.max(cy, toolbar.getHeight() - cy);
            float finalRadius = (float) Math.hypot(dx, dy);


            // Circular animation declaration begin
            final Animator animator;
            animator = ViewAnimationUtils.createCircularReveal(searchAppBarLayout, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(400).addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    AppHelper.LogCat("onAnimationStart ");
                    searchAppBarLayout.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationEnd(Animator animation) {


                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animator.start();
        } else {

            Animation animation = new TranslateAnimation(0, 0, searchAppBarLayout.getHeight(), 0);
            animation.setDuration(400);

            searchAppBarLayout.startAnimation(animation);
        }

        // Circular animation declaration end

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(appBar, "translationY", -tabLayout.getHeight()),
                ObjectAnimator.ofFloat(whatsCloneViewPager, "translationY", -tabLayout.getHeight())
                , ObjectAnimator.ofFloat(appBar, "alpha", 0)
        );
        set.setDuration(400).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                appBar.setVisibility(View.GONE);
                searchEditText.requestFocus();
                AppHelper.showKeyBoard(searchEditText);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();

    }


    /**
     * to hide the searchAppBarLayout and show the mainAppBar with animation.
     *
     * @param positionFromRight
     */
    private void hideSearchBar(float positionFromRight) {

        if (AppHelper.isAndroid5()) {

            // start x-index for circular animation
            int cx = toolbar.getWidth() - (int) (getResources().getDimension(R.dimen.dp48) * (0.5f + positionFromRight));
            // start  y-index for circular animation
            int cy = (toolbar.getTop() + toolbar.getBottom()) / 2;

            // calculate max radius
            int dx = Math.max(cx, toolbar.getWidth() - cx);
            int dy = Math.max(cy, toolbar.getHeight() - cy);
            float finalRadius = (float) Math.hypot(dx, dy);
            // Circular animation declaration begin
            Animator animator;
            animator = ViewAnimationUtils.createCircularReveal(searchAppBarLayout, cx, cy, finalRadius, 0);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(400);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    clearSearchView();
                    searchAppBarLayout.setVisibility(View.GONE);
                    AppHelper.hideKeyBoard(searchEditText);

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            animator.start();
        } else {


            Animation animation = new TranslateAnimation(0, 0, 0, searchAppBarLayout.getTop() + searchAppBarLayout.getHeight());
            animation.setDuration(400);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    searchAppBarLayout.setVisibility(View.GONE);
                    AppHelper.hideKeyBoard(searchEditText);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            searchAppBarLayout.startAnimation(animation);
        }
        // Circular animation declaration end

        appBar.setVisibility(View.VISIBLE);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(appBar, "translationY", 0),
                ObjectAnimator.ofFloat(appBar, "alpha", 1),
                ObjectAnimator.ofFloat(whatsCloneViewPager, "translationY", 0)
        );
        set.setDuration(400).start();

    }


    public class ContactsChangeObserver extends ContentObserver {

        private static final String TAG = "ContactsChangeObserver";
        Context context;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public ContactsChangeObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            AppHelper.LogCat("ContentObserver is called for contacts change " + selfChange);
            if (selfChange)
                WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
        }

    }
}