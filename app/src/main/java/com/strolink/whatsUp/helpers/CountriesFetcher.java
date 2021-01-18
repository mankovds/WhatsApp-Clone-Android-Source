package com.strolink.whatsUp.helpers;

import android.content.Context;

import com.strolink.whatsUp.models.CountriesModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Abderrahim El imame on 10/30/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CountriesFetcher {


    private static CountryList mCountries;

    /**
     * method to loadJSONFromAsset json files from asset directory
     *
     * @param mContext this is  parameter for loadJSONFromAsset  method
     * @return return value
     */
    public static String loadJSONFromAsset(Context mContext) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("country_phones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Import CountryList from RAW resource
     *
     * @param context Context
     * @return CountryList
     */
    public static CountryList getCountries(Context context) {
        if (mCountries != null) {
            return mCountries;
        }
        mCountries = new CountryList();
        try {
            JSONArray countries = new JSONArray(loadJSONFromAsset(context));
            for (int i = 0; i < countries.length(); i++) {
                try {
                    JSONObject country = (JSONObject) countries.get(i);
                    mCountries.add(new CountriesModel(country.getString("name"), country.getString("code"), country.getString("dial_code")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mCountries;
    }


    public static class CountryList extends ArrayList<CountriesModel> {
        /**
         * Fetch item index on the list by iso
         *
         * @param iso Country's iso2
         * @return index of the item in the list
         */
        public int indexOfIso(String iso) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).getCode().toUpperCase().equals(iso.toUpperCase())) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Fetch item index on the list by dial coder
         *
         * @param dialCode Country's dial code prefix
         * @return index of the item in the list
         */
        @SuppressWarnings("unused")
        public int indexOfDialCode(String dialCode) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).getDial_code().equals(dialCode)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
