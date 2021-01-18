package com.strolink.whatsUp.helpers;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.PhoneNumberFormattingTextWatcher;

/**
 * Created by Abderrahim El imame on 11/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {


    @SuppressWarnings("unused")
    public PhoneNumberWatcher() {
        super();
    }

    //TODO solve it! support for android kitkat
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PhoneNumberWatcher(String countryCode) {
        super(countryCode);
    }
}