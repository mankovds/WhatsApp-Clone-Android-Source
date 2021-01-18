package com.strolink.whatsUp.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.strolink.whatsUp.models.groups.MembersModelJson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class PreferenceManager {


    private SharedPreferences mSharedPreferences;
    private final String KEY_USER_PREF = "KEY_USER_PREFERENCES";

    private final String KEY_MEMBERS_SELECTED = "KEY_MEMBERS_SELECTED";
    private final String KEY_IS_WAITING_FOR_SMS = "KEY_IS_WAITING_FOR_SMS";
    private final String KEY_MOBILE_NUMBER = "KEY_MOBILE_NUMBER";
    private final String KEY_VERSION_APP = "KEY_VERSION_APP";
    private final String KEY_NEW_USER = "KEY_NEW_USER";
    private final String KEY_WALLPAPER_USER = "KEY_WALLPAPER_USER";
    private final String KEY_LANGUAGE = "KEY_LANGUAGE";
    private final String KEY_APP_IS_OUT_DATE = "KEY_APP_IS_OUT_DATE";
    private final String KEY_NEED_MORE_INFO = "KEY_NEED_MORE_INFO";
    private final String KEY_APP_KILLED = "KEY_APP_KILLED";


    private static volatile PreferenceManager Instance = null;


    public static PreferenceManager getInstance() {
        PreferenceManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (PreferenceManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PreferenceManager();
                }
            }
        }
        return localInstance;
    }


    /**
     * method to set Language
     *
     * @param lang     this is the first parameter for setLanguage  method
     * @param mContext this is the second parameter for setLanguage  method
     * @return return value
     */
    public void setLanguage(Context mContext, String lang) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LANGUAGE, lang);
        editor.apply();
    }

    /**
     * method to get Language
     *
     * @return return value
     */
    public String getLanguage(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_LANGUAGE, "");
    }

    /**
     * method to set wallpaper
     *
     * @param wallpaper this is the first parameter for setWallpaper  method
     * @param mContext  this is the second parameter for setWallpaper  method
     * @return return value
     */
    public void setWallpaper(Context mContext, String wallpaper) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_WALLPAPER_USER, wallpaper);
        editor.apply();
    }

    /**
     * method to get wallpaper
     *
     * @return return value
     */
    public String getWallpaper(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_WALLPAPER_USER, null);
    }

    /**
     * method to set token
     *
     * @param token    this is the first parameter for setToken  method
     * @param mContext this is the second parameter for setToken  method
     * @return return value
     */
    public void setToken(Context mContext, String token) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    /**
     * method to get token
     *
     * @return return value
     */
    public String getToken(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("token", null);
    }


    /**
     * method to setID
     *
     * @param ID this is the first parameter for setID  method
     * @return return value
     */
    public void setID(Context mContext, String ID) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("id", ID);
        editor.apply();
    }

    /**
     * method to getID
     *
     * @return return value
     */
    public String getID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("id", null);
    }





    /**
     * method to getPhone
     *
     * @return return value
     */
    public String getPhone(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("phone", null);
    }

    /**
     * method to setPhone
     *
     * @param Phone this is the first parameter for setID  method
     * @return return value
     */
    public void setPhone(Context mContext, String Phone) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("phone", Phone);
        editor.apply();
    }


    /**
     * method to set contacts size
     *
     * @param size this is the first parameter for setContactSize  method
     * @return return value
     */
    public void setContactSize(Context mContext, int size) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("size", size);
        editor.apply();
    }

    /**
     * method to get contacts size
     *
     * @return return value
     */
    public int getContactSize(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt("size", 0);

    }


    /**
     * method to save new members to group
     *
     * @param membersGroupModels this is the second parameter for saveMembers  method
     */
    private void saveMembers(Context mContext, List<MembersModelJson> membersGroupModels) {


        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        Gson gson = new Gson();
        String jsonMembers = gson.toJson(membersGroupModels);

        editor.putString(KEY_MEMBERS_SELECTED, jsonMembers);

        editor.apply();
    }

    /**
     * method to add member
     *
     * @param membersGroupModel this is the second parameter for addMember  method
     */
    public void addMember(Context mContext, MembersModelJson membersGroupModel) {
        List<MembersModelJson> membersGroupModelArrayList = getMembers(mContext);
        if (membersGroupModelArrayList == null)
            membersGroupModelArrayList = new ArrayList<MembersModelJson>();
        membersGroupModelArrayList.add(membersGroupModel);
        saveMembers(mContext, membersGroupModelArrayList);
    }

    /**
     * method to remove member
     *
     * @param membersGroupModel this is the second parameter for removeMember  method
     */
    public void removeMember(Context mContext, MembersModelJson membersGroupModel) {
        ArrayList<MembersModelJson> membersGroupModelArrayList = getMembers(mContext);
        if (membersGroupModelArrayList != null) {
            membersGroupModelArrayList.remove(membersGroupModel);
            saveMembers(mContext, membersGroupModelArrayList);
        }
    }

    /**
     * method to clear members
     */
    public void clearMembers(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_MEMBERS_SELECTED, null);
        editor.apply();
    }

    /**
     * method to get all members
     *
     * @return return value
     */
    public ArrayList<MembersModelJson> getMembers(Context mContext) {
        try {
            List<MembersModelJson> membersGroupModels;
            mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
            if (mSharedPreferences.contains(KEY_MEMBERS_SELECTED)) {
                String jsonMembers = mSharedPreferences.getString(KEY_MEMBERS_SELECTED, null);
                Gson gson = new Gson();
                MembersModelJson[] membersItems = gson.fromJson(jsonMembers, MembersModelJson[].class);
                membersGroupModels = Arrays.asList(membersItems);
                return new ArrayList<>(membersGroupModels);
            } else {
                return null;
            }


        } catch (Exception e) {
            AppHelper.LogCat("getMembers Exception " + e.getMessage());
            return null;
        }
    }


    /**
     * method to setUnitInterstitialAdID
     *
     * @param UnitId this is the first parameter for setUnitInterstitialAdID  method
     * @return return value
     */
    public void setUnitInterstitialAdID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("InterstitialUnitId", UnitId);
        editor.apply();
    }

    /**
     * method to getUnitInterstitialAdID
     *
     * @return return value
     */
    public String getUnitInterstitialAdID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("InterstitialUnitId", null);
    }

    /**
     * method to setShowInterstitialAds
     *
     * @param UnitId this is the first parameter for setShowInterstitialAds  method
     * @return return value
     */
    public void setShowInterstitialAds(Context mContext, Boolean UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ShowInterstitialAds", UnitId);
        editor.apply();
    }

    /**
     * method to ShowInterstitialrAds
     *
     * @return return value
     */
    public boolean ShowInterstitialrAds(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ShowInterstitialAds", false);
    }

    /**
     * method to setUnitBannerAdsID
     *
     * @param UnitId this is the first parameter for setUnitBannerAdsID  method
     * @return return value
     */
    public void setUnitBannerAdsID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("BannerUnitId", UnitId);
        editor.apply();
    }

    /**
     * method to getUnitBannerAdsID
     *
     * @return return value
     */
    public String getUnitBannerAdsID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("BannerUnitId", null);
    }

    public void setPublisherId(Context mContext, String PublisherId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("PublisherId", PublisherId);
        editor.apply();
    }

    /**
     * method to getPublisherId
     *
     * @return return value
     */
    public String getPublisherId(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("PublisherId", null);
    }


    /**
     * method to setShowBannerAds
     *
     * @param UnitId this is the first parameter for setShowBannerAds  method
     * @return return value
     */
    public void setShowBannerAds(Context mContext, Boolean UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ShowBannerAds", UnitId);
        editor.apply();
    }

    /**
     * method to ShowBannerAds
     *
     * @return return value
     */
    public boolean ShowBannerAds(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ShowBannerAds", false);
    }

    /**
     * method to setPrivacyLink
     *
     * @param PrivacyLink this is the first parameter for setPrivacyLink  method
     * @return return value
     */
    public void setPrivacyLink(Context mContext, String PrivacyLink) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("PrivacyLink", PrivacyLink);
        editor.apply();
    }

    /**
     * method to getPrivacyLink
     *
     * @return return value
     */
    public String getPrivacyLink(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("PrivacyLink", null);
    }

    /**
     * method to setGiphyKey
     *
     * @param GiphyKey this is the first parameter for setGiphyKey  method
     * @return return value
     */
    public void setGiphyKey(Context mContext, String GiphyKey) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("GiphyKey", GiphyKey);
        editor.apply();
    }

    /**
     * method to getGiphyKey
     *
     * @return return value
     */
    public String getGiphyKey(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("GiphyKey", null);
    }

    /**
     * method to setUnitVideoAdsID
     *
     * @param UnitId this is the first parameter for setUnitVideoAdsID  method
     * @return return value
     */
    public void setUnitVideoAdsID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("VideoUnitId", UnitId);
        editor.apply();
    }

    /**
     * method to getUnitVideoAdsID
     *
     * @return return value
     */
    public String getUnitVideoAdsID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("VideoUnitId", null);
    }

    /**
     * method to setAppVideoAdsID
     *
     * @param UnitId this is the first parameter for setAppVideoAdsID  method
     * @return return value
     */
    public void setAppVideoAdsID(Context mContext, String UnitId) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("VideoAppId", UnitId);
        editor.apply();
    }

    /**
     * method to getAppVideoAdsID
     *
     * @return return value
     */
    public String getAppVideoAdsID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString("VideoAppId", null);
    }


    /**
     * method to setShowVideoAds
     *
     * @param bo this is the first parameter for setShowVideoAds  method
     * @return return value
     */
    public void setShowVideoAds(Context mContext, Boolean bo) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("ShowVideoAds", bo);
        editor.apply();
    }

    /**
     * method to ShowVideoAds
     *
     * @return return value
     */
    public boolean ShowVideoAds(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ShowVideoAds", false);
    }


    /**
     * method to set var as the info aren't incomplete
     *
     * @param isNew this is parameter for setIsNewUser  method
     */
    public void setIsNeedInfo(Context mContext, Boolean isNew) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_NEED_MORE_INFO, isNew);
        editor.apply();
    }

    /**
     * method to check if user is provide more info
     *
     * @return return value
     */
    public boolean isNeedProvideInfo(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_NEED_MORE_INFO, false);
    }

    /**
     * method to set user waiting for SMS (code verification)
     *
     * @param isWaiting this is parameter for setIsWaitingForSms  method
     */
    public void setIsWaitingForSms(Context mContext, Boolean isWaiting) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.apply();
    }

    /**
     * method to check if user is waiting for SMS
     *
     * @return return value
     */
    public boolean isWaitingForSms(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    /**
     * method to set mobile phone
     *
     * @param mobileNumber this is parameter for setMobileNumber  method
     */
    public void setMobileNumber(Context mContext, String mobileNumber) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.apply();
    }

    /**
     * method to get mobile phone
     *
     * @return return value
     */
    public String getMobileNumber(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(KEY_MOBILE_NUMBER, null);
    }


    /**
     * method to set var as the user is new on the app
     *
     * @param isNew this is parameter for setIsNewUser  method
     */
    public void setIsNewUser(Context mContext, Boolean isNew) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_NEW_USER, isNew);
        editor.apply();
    }

    /**
     * method to check if user is new here the app
     *
     * @return return value
     */
    public boolean isNewUser(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_NEW_USER, false);
    }


    /**
     * method to set last backup
     *
     * @param version this is parameter for setLastBackup  method
     */
    public void setVersionApp(Context mContext, int version) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_VERSION_APP, version);
        editor.apply();
    }

    /**
     * method to get last backup
     *
     * @return return value
     */
    public int getVersionApp(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt(KEY_VERSION_APP, 0);
    }

    /**
     * method to set the app is out date
     *
     * @param isNew this is parameter for setIsOutDate  method
     */
    public void setIsOutDate(Context mContext, Boolean isNew) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_APP_IS_OUT_DATE, isNew);
        editor.apply();
    }

    /**
     * method to check if the app is out date
     *
     * @return return value
     */
    public boolean isOutDate(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_APP_IS_OUT_DATE, false);
    }

    public void setKeyAppKilled(Context mContext, boolean value) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_APP_KILLED, value);
        editor.apply();
    }

    public boolean getKeyAppKilled(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getBoolean(KEY_APP_KILLED, false);
    }

    /**
     * method to setStoriesPrivacy
     *
     * @param privacy this is the first parameter for setStoriesPrivacy  method
     * @return return value
     */
    public void setStoriesPrivacy(Context mContext, int privacy) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("stories_privacy", privacy);
        editor.apply();
    }

    /**
     * method to setStoriesPrivacy
     *
     * @return return value
     */
    public int getStoriesPrivacy(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        return mSharedPreferences.getInt("stories_privacy", 0);
    }

    /**
     * method to clear preference
     *
     * @return return value
     */
    public void clearPreferences(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(KEY_USER_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
