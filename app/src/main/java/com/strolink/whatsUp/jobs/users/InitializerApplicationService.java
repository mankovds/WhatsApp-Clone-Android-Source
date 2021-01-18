package com.strolink.whatsUp.jobs.users;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.models.users.Pusher;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class InitializerApplicationService extends Worker {

    public static final String TAG = InitializerApplicationService.class.getSimpleName();
    private int mPendingMessages = 0;
    private CountDownLatch latch;
    private CompositeDisposable mDisposable;

    public InitializerApplicationService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            mDisposable = new CompositeDisposable();
            runMethods(WhatsCloneApplication.getInstance());
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Result.success();

        } else {
            return Result.success();
        }


    }


    @Override
    public void onStopped() {
        super.onStopped();

        if (mDisposable != null)
            mDisposable.dispose();
        boolean needsReschedule = (mPendingMessages > 0);
        AppHelper.LogCat("Job stopped. Needs reschedule: " + needsReschedule);
        if (!needsReschedule) {
            WorkJobsManager.getInstance().cancelJob(TAG);
            mPendingMessages = 0;
        }
    }

    // returns whether an attempt was made to send every message at least once
    private boolean isComplete() {
        return mPendingMessages == 0;
    }

    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending messages to send.
     */
    private void checkCompletion() {
        if (!isComplete()) {
            return;
        }

        //  if any sending is not successful, reschedule job for remaining files

        AppHelper.LogCat("Job finished. : " + mPendingMessages);
        if(mPendingMessages == 0)
        WorkJobsManager.getInstance().cancelJob(TAG);


    }


    private void runMethods(Context mContext) {
        mPendingMessages = 4;
        latch = new CountDownLatch(4);

        checkIfUserSession();

    }

    /**
     * method to send notification if i'm new  to the app
     */

    private void notifyOtherUser(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {


            if (PreferenceManager.getInstance().isNewUser(context)) {


                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("senderId", PreferenceManager.getInstance().getID(context));
                    jsonObject.put("phone", PreferenceManager.getInstance().getMobileNumber(context));
                    try {
                        WhatsCloneApplication.getInstance().getMqttClientManager().publishUserHasJoined(AppConstants.MqttConstants.PUBLISH_WHATSCLONE_GENERAL, jsonObject);
                        PreferenceManager.getInstance().setIsNewUser(context, false);
                    } catch (MqttException e) {
                        e.printStackTrace();

                        mPendingMessages--;
                        checkCompletion();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                mPendingMessages--;
                checkCompletion();
            }
        });
    }


    private void getContactInfo(Context context) {

        mPendingMessages--;
        checkCompletion();

        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(PreferenceManager.getInstance().getID(context), mDisposable)
                .subscribe(usersModel -> {


                    mPendingMessages--;
                    checkCompletion();

                }, throwable -> {
                    checkCompletion();
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));
    }


    public void getAppSettings(Context context) {
        mDisposable.add(APIHelper.initialApiUsersContacts().getAppSettings().subscribe(settingsResponse -> {

            AppHelper.LogCat("settingsResponse " + settingsResponse.toString());
            PreferenceManager.getInstance().setPublisherId(context, settingsResponse.getPublisherId());
            PreferenceManager.getInstance().setUnitBannerAdsID(context, settingsResponse.getUnitBannerID());


            PreferenceManager.getInstance().setShowBannerAds(context, settingsResponse.isAdsBannerStatus());
            PreferenceManager.getInstance().setShowVideoAds(context, settingsResponse.isAdsVideoStatus());
            PreferenceManager.getInstance().setShowInterstitialAds(context, settingsResponse.isAdsInterstitialStatus());

            PreferenceManager.getInstance().setUnitVideoAdsID(context, settingsResponse.getUnitVideoID());
            PreferenceManager.getInstance().setAppVideoAdsID(context, settingsResponse.getAppID());

            PreferenceManager.getInstance().setUnitInterstitialAdID(context, settingsResponse.getUnitInterstitialID());

            PreferenceManager.getInstance().setGiphyKey(context, settingsResponse.getGiphyKey());
            PreferenceManager.getInstance().setPrivacyLink(context, settingsResponse.getPrivacyLink());


            int currentAppVersion;
            if (PreferenceManager.getInstance().getVersionApp(WhatsCloneApplication.getInstance()) != 0) {
                currentAppVersion = PreferenceManager.getInstance().getVersionApp(WhatsCloneApplication.getInstance());
            } else {
                currentAppVersion = AppHelper.getAppVersionCode(WhatsCloneApplication.getInstance());
            }
            if (currentAppVersion != 0 && currentAppVersion < settingsResponse.getAppVersion()) {
                PreferenceManager.getInstance().setVersionApp(context, currentAppVersion);
                PreferenceManager.getInstance().setIsOutDate(context, true);
            } else {
                PreferenceManager.getInstance().setIsOutDate(context, false);
            }

            mPendingMessages--;
            checkCompletion();
            latch.countDown();
        }, throwable -> {
            checkCompletion();
            AppHelper.LogCat("Error get settings info Welcome " + throwable.getMessage());
        }));
    }

    private void checkIfUserSession() {
        mDisposable.add(APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
            if (!networkModel.isConnected()) {
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SESSION_EXPIRED));

            }else {

                notifyOtherUser(getApplicationContext());
                getContactInfo(getApplicationContext());
                getAppSettings(getApplicationContext());
            }
            mPendingMessages--;
            checkCompletion();
        }, throwable -> {
            checkCompletion();
            AppHelper.LogCat("checkIfUserSession  " + throwable.getMessage());
        }))
        ;
    }
}