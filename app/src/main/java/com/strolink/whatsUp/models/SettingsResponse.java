package com.strolink.whatsUp.models;

/**
 * Created by Abderrahim El imame on 03/05/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class SettingsResponse {
    private boolean adsVideoStatus;
    private boolean adsBannerStatus;
    private boolean adsInterstitialStatus;
    private String unitBannerID;
    private String unitVideoID;
    private String unitInterstitialID;
    private String giphyKey;
    private String privacyLink;
    private String appID;
    private String publisherId;
    private int appVersion;



    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getGiphyKey() {
        return giphyKey;
    }

    public void setGiphyKey(String giphyKey) {
        this.giphyKey = giphyKey;
    }

    public String getPrivacyLink() {
        return privacyLink;
    }

    public void setPrivacyLink(String privacyLink) {
        this.privacyLink = privacyLink;
    }

    public boolean isAdsVideoStatus() {
        return adsVideoStatus;
    }

    public void setAdsVideoStatus(boolean adsVideoStatus) {
        this.adsVideoStatus = adsVideoStatus;
    }

    public boolean isAdsBannerStatus() {
        return adsBannerStatus;
    }

    public void setAdsBannerStatus(boolean adsBannerStatus) {
        this.adsBannerStatus = adsBannerStatus;
    }

    public boolean isAdsInterstitialStatus() {
        return adsInterstitialStatus;
    }

    public void setAdsInterstitialStatus(boolean adsInterstitialStatus) {
        this.adsInterstitialStatus = adsInterstitialStatus;
    }

    public String getUnitBannerID() {
        return unitBannerID;
    }

    public void setUnitBannerID(String unitBannerID) {
        this.unitBannerID = unitBannerID;
    }

    public String getUnitVideoID() {
        return unitVideoID;
    }

    public void setUnitVideoID(String unitVideoID) {
        this.unitVideoID = unitVideoID;
    }

    public String getUnitInterstitialID() {
        return unitInterstitialID;
    }

    public void setUnitInterstitialID(String unitInterstitialID) {
        this.unitInterstitialID = unitInterstitialID;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public int getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(int app_version) {
        this.appVersion = app_version;
    }


}
