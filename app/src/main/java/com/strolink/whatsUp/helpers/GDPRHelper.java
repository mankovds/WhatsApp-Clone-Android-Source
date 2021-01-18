package com.strolink.whatsUp.helpers;

import android.Manifest;
import android.content.Context;
import androidx.annotation.RequiresPermission;
import android.util.Log;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;

import java.net.URL;

/**
 * Created by Abderrahim El imame on 6/2/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GDPRHelper {

    private static final String TAG = "GDPRHelper";
    private ConsentInformation consentInformation;
    private Context context;
    private String privacyUrl;
    private ConsentForm form;
    private String[] publisherIds;
    private static GDPRHelper instance;

    protected GDPRHelper(Context context) {
        this.context = context;
        this.consentInformation = ConsentInformation.getInstance(context);
    }

    public GDPRHelper() {
    }

    public GDPRHelper withContext(Context context) {
        instance = new GDPRHelper(context);
        return instance;
    }

    public GDPRHelper withPrivacyUrl(String privacyUrl) {
        this.privacyUrl = privacyUrl;
        if (instance == null)
            throw new NullPointerException("Please call withContext first");
        return instance;
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    private void initGDPR() {
//        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        if (publisherIds == null)
            throw new NullPointerException("publisherIds is null, please call withPublisherIds first");
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if (consentStatus == ConsentStatus.UNKNOWN)
                    if (consentInformation.isRequestLocationInEeaOrUnknown())
                        setupForm();
                    else consentInformation.setConsentStatus(consentStatus);
                else
                    consentInformation.setConsentStatus(consentStatus);
                Log.i(TAG, "onConsentInfoUpdated: " + consentStatus.name());
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                Log.e(TAG, "onFailedToUpdateConsentInfo: " + errorDescription);
            }
        });


    }

    private void setupForm() {
        if (privacyUrl == "")
            throw new NullPointerException("PrivacyUrl is null, Please call withPrivacyUrl first");
        URL Url = null;
        try {
            Url = new URL(privacyUrl);
        } catch (Exception e) {
            Log.e(TAG, "initGDPR: ", e);
        }
        form = new ConsentForm.Builder(context, Url)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        Log.i(TAG, "onConsentFormOpened: ");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.i(TAG, "onConsentFormClosed: " + consentStatus.name());
                        consentInformation.setConsentStatus(consentStatus);

                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.e(TAG, "onConsentFormError: " + errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();

        form.load();
    }

    private void showForm() {
        form.show();
    }

    public void check() {
        initGDPR();
    }

    public GDPRHelper withPublisherIds(String... publisherIds) {
        this.publisherIds = publisherIds;
        if (instance == null)
            throw new NullPointerException("Please call withContext first");
        return instance;
    }

    public GDPRHelper withTestMode(String testDevice) {
        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        consentInformation.addTestDevice(testDevice);
        if (instance == null)
            throw new NullPointerException("Please call withContext first");
        return instance;
    }

    public GDPRHelper withTestMode() {
        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        if (instance == null)
            throw new NullPointerException("Please call withContext first");
        return instance;
    }
}
