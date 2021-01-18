package com.strolink.whatsUp.activities.welcome;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.main.MainActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;

import butterknife.BindView;
import butterknife.ButterKnife;

;

/**
 * Created by Abderrahim El imame on 1/20/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class IntroActivity extends BaseActivity implements InstallReferrerStateListener {

    @BindView(R.id.intro_view_pager)
    ViewPager viewPager;
    @BindView(R.id.icon_image1)
    AppCompatImageView topImage1;
    @BindView(R.id.icon_image2)
    AppCompatImageView topImage2;
    @BindView(R.id.bottom_pages)
    ViewGroup bottomPages;

    @BindView(R.id.get_started_button)
    AppCompatTextView startMessagingButton;


    private int lastPage = 0;
    private boolean justCreated = false;

    private InstallReferrerClient mReferrerClient;
    private int[] icons;
    private int[] titles;
    private int[] messages;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + "closeSplashActivity")) {
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        //  AnimationsUtil.setSlideInAnimation(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorBlackOpacity));
        }

        mReferrerClient = InstallReferrerClient.newBuilder(this).build();
        mReferrerClient.startConnection(this);
        if (PreferenceManager.getInstance().getToken(this) != null) {
            NotificationsManager.getInstance().SetupBadger(this);
            if (PreferenceManager.getInstance().isNeedProvideInfo(this)) {
                Intent intent = new Intent(this, CompleteRegistrationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        } else {

            setContentView(R.layout.activity_intro_layout);
            ButterKnife.bind(this);

            if (PreferenceManager.getInstance().isWaitingForSms(this)) {
                launchWelcomeActivity();
            }
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(getPackageName() + "closeSplashActivity");
            mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
            icons = new int[]{
                    R.drawable.intro1,
                    R.drawable.intro2,
                    R.drawable.intro3,
                    R.drawable.intro4,
                    R.drawable.intro5,
            };
            titles = new int[]{
                    R.string.app_name,
                    R.string.Page2Title,
                    R.string.Page3Title,
                    R.string.Page4Title,
                    R.string.Page5Title,
            };
            messages = new int[]{
                    R.string.Page1Message,
                    R.string.Page2Message,
                    R.string.Page3Message,
                    R.string.Page4Message,
                    R.string.Page5Message,
            };

            topImage2.setVisibility(View.GONE);
            viewPager.setAdapter(new IntroAdapter());
            viewPager.setPageMargin(0);
            viewPager.setOffscreenPageLimit(1);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int i) {

                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if (i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING) {

                        try {
                            if (lastPage != viewPager.getCurrentItem()) {
                                lastPage = viewPager.getCurrentItem();

                                final AppCompatImageView fadeoutImage;
                                final AppCompatImageView fadeinImage;
                                if (topImage1.getVisibility() == View.VISIBLE) {
                                    fadeoutImage = topImage1;
                                    fadeinImage = topImage2;

                                } else {
                                    fadeoutImage = topImage2;
                                    fadeinImage = topImage1;
                                }

                                fadeinImage.bringToFront();
                                fadeinImage.setImageDrawable(AppHelper.getDrawable(IntroActivity.this, icons[lastPage]));
                                fadeinImage.clearAnimation();
                                fadeoutImage.clearAnimation();


                                Animation outAnimation = AnimationUtils.loadAnimation(IntroActivity.this, R.anim.icon_anim_fade_out);
                                outAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        fadeoutImage.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });

                                Animation inAnimation = AnimationUtils.loadAnimation(IntroActivity.this, R.anim.icon_anim_fade_in);
                                inAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        fadeinImage.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });


                                fadeoutImage.startAnimation(outAnimation);
                                fadeinImage.startAnimation(inAnimation);
                            }
                        } catch (Exception e) {
                            AppHelper.LogCat("Exception " + e);
                        }

                    }
                }
            });

            justCreated = true;
        }

    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {
        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                // Connection established

                try {
                    getReferralUser();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                // API not available on the current Play Store app
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                // Connection could not be established
                break;
            case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR:
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                break;
        }
    }

    private void getReferralUser() throws RemoteException {
        ReferrerDetails response = mReferrerClient.getInstallReferrer();


        String referrerData = response.getInstallReferrer();
        long referrerClickTime = response.getReferrerClickTimestampSeconds();
        long appInstallTime = response.getInstallBeginTimestampSeconds();
        boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
        Log.e("TAG", "Install referrer:" + response.getInstallReferrer());


    }

    @Override
    public void onInstallReferrerServiceDisconnected() {

        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
    }

    public void launchWelcomeActivity() {
        Intent mainIntent = new Intent(this, WelcomeActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        AnimationsUtil.setTransitionAnimation(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLocalBroadcastManager != null)
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (justCreated) {
            viewPager.setCurrentItem(0);
            lastPage = 0;
            justCreated = false;
        }
    }

    public class IntroAdapter extends PagerAdapter {

        @BindView(R.id.header_text)
        AppCompatTextView headerTextView;

        @BindView(R.id.message_text)
        AppCompatTextView messageTextView;


        @Override
        public int getCount() {
            return 5;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = View.inflate(container.getContext(), R.layout.fragment_intro_view_layout, null);
            ButterKnife.bind(this, view);
            container.addView(view, 0);

            headerTextView.setText(getString(titles[position]));
            messageTextView.setText(Html.fromHtml(getApplicationContext().getString(messages[position])));


            startMessagingButton.setOnClickListener(view1 -> {
                launchWelcomeActivityWithPermissions();
            });

            return view;
        }

        public void launchWelcomeActivityWithPermissions() {

            Permissions.with(IntroActivity.this)
                    .request(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                    .ifNecessary()
                    .withRationaleDialog(getString(R.string.app__requires_contacts_permission_in_order_to_attach_contact_information), R.drawable.ic_contacts_white_24dp)
                    .withPermanentDenialDialog(getString(R.string.app__requires_contacts_permission_in_order_to_attach_contact_information))
                    .onAllGranted(() -> {

                        Intent mainIntent = new Intent(IntroActivity.this, WelcomeActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                        AnimationsUtil.setTransitionAnimation(IntroActivity.this);
                    })
                    .onAnyDenied(() -> {
                        Toast.makeText(IntroActivity.this, getString(R.string.contact_permission_required), Toast.LENGTH_LONG).show();

                    })
                    .execute();

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.setPrimaryItem(container, position, object);
            /*if (viewPager.getCurrentItem() != 4) {
                startMessagingButton.setVisibility(View.GONE);
            } else {
                startMessagingButton.setVisibility(View.VISIBLE);

            }

*/
            startMessagingButton.setVisibility(View.VISIBLE);
            int count = bottomPages.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = bottomPages.getChildAt(a);
                if (a == position) {
                    child.setBackground(AppHelper.getDrawable(IntroActivity.this, R.drawable.bg_indicator_selected));
                } else {
                    child.setBackground(AppHelper.getDrawable(IntroActivity.this, R.drawable.bg_indicator));
                }
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

    }
}