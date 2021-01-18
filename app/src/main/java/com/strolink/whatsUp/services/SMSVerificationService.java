package com.strolink.whatsUp.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;


import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.main.MainActivity;
import com.strolink.whatsUp.activities.welcome.CompleteRegistrationActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.models.users.Pusher;

import org.greenrobot.eventbus.EventBus;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by Abderrahim El imame on 23/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SMSVerificationService extends IntentService {


    public SMSVerificationService() {
        super(SMSVerificationService.class.getSimpleName());
    }

    // private CompositeDisposable mDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        //  mDisposable = new CompositeDisposable();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String code = intent.getStringExtra("code");
            boolean registration = intent.getBooleanExtra("register", true);
            verifyUser(code, registration);
        }
    }

    @SuppressLint("CheckResult")
    private void verifyUser(String code, boolean registration) {
        if (registration) {
            APIHelper.initializeAuthService().verifyUser(code,PreferenceManager.getInstance().getMobileNumber(this)).subscribe(joinModelResponse -> {
                if (joinModelResponse.isSuccess()) {
                    PreferenceManager.getInstance().setIsNewUser(SMSVerificationService.this, true);
                    PreferenceManager.getInstance().setID(SMSVerificationService.this, joinModelResponse.getUserID());
                    PreferenceManager.getInstance().setToken(SMSVerificationService.this, joinModelResponse.getToken());
                    PreferenceManager.getInstance().setIsWaitingForSms(SMSVerificationService.this, false);
                      if (joinModelResponse.isHasProfile()) {
                        PreferenceManager.getInstance().setIsNeedInfo(SMSVerificationService.this, false);
                        Intent intent = new Intent(SMSVerificationService.this, MainActivity.class);

                        intent.putExtra("first_time", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        PreferenceManager.getInstance().setIsNeedInfo(SMSVerificationService.this, true);
                        Intent intent = new Intent(SMSVerificationService.this, CompleteRegistrationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeWelcomeActivity"));
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeSplashActivity"));

                } else {
                    AppHelper.CustomToast(SMSVerificationService.this, joinModelResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat("SMS verification failure  SMSVerificationService" + throwable.getMessage());
            })
            ;
        } else {

            APIHelper.initialApiUsersContacts().deleteAccountConfirmation(code,PreferenceManager.getInstance().getMobileNumber(this)).subscribe(statusResponse -> {

                if (statusResponse.isSuccess()) {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(SMSVerificationService.this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeDeleteAccountActivity"));
                } else {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ERROR));
                    AppHelper.CustomToast(SMSVerificationService.this, statusResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.hideDialog();
                AppHelper.CustomToast(SMSVerificationService.this, WhatsCloneApplication.getInstance().getString(R.string.oops_something));
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ERROR));
                AppHelper.LogCat("SMS verification failure  SMSVerificationService" + throwable.getMessage());
            });

        }
    }
}
