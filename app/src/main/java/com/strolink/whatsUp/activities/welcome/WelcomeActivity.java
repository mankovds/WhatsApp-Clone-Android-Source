package com.strolink.whatsUp.activities.welcome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.CountryActivity;
import com.strolink.whatsUp.adapters.others.TextWatcherAdapter;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CountriesFetcher;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.models.CountriesModel;
import com.strolink.whatsUp.models.auth.LoginModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.services.SMSVerificationService;
import com.strolink.whatsUp.ui.CustomProgressView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 09/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.numberPhone)
    AppCompatEditText phoneNumberWrapper;

    @BindView(R.id.inputOtpWrapper)
    TextInputEditText inputOtpWrapper;

    @BindView(R.id.btn_request_sms)
    AppCompatTextView btnNext;


    @BindView(R.id.progress_bar_load)
    CustomProgressView progressBarLoad;


    @BindView(R.id.btn_change_number)
    AppCompatImageView changeNumberBtn;

    @BindView(R.id.btn_verify_otp)
    AppCompatImageView btnVerifyOtp;

    @BindView(R.id.viewPagerVertical)
    ViewPager viewPager;

    @BindView(R.id.TimeCount)
    TextView textViewShowTime;

    @BindView(R.id.Resend)
    TextView ResendBtn;

    @BindView(R.id.country_code)
    AppCompatTextView countryCode;

    @BindView(R.id.short_description_phone)
    AppCompatTextView shortDescriptionPhone;

    @BindView(R.id.country_name)
    AppCompatTextView countryName;

    @BindView(R.id.current_mobile_number)
    TextView currentMobileNumber;

    @BindView(R.id.numberPhone_layout_sv)
    NestedScrollView numberPhoneLayoutSv;

    @BindView(R.id.layout_verification_sv)
    NestedScrollView layoutVerificationSv;

    private CountDownTimer countDownTimer;
    private long totalTimeCountInMilliseconds;

    @BindView(R.id.registrationTerms)
    TextView registrationTerms;

    private CompositeDisposable mDisposable;


    private PhoneNumberUtil mPhoneUtil = PhoneNumberUtil.getInstance();
    private CountriesModel mSelectedCountry;
    private CountriesFetcher.CountryList mCountries;
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getPackageName() + "closeWelcomeActivity")) {
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mDisposable = new CompositeDisposable();
        initializePermissions();
        initializerView();

    }


    @SuppressLint("InlinedApi")
    private void initializePermissions() {
        Permissions.with(WelcomeActivity.this)
                .request(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.app_needs_access_to_your_contacts_and_media_in_order_to_connect_with_friends), R.drawable.ic_contacts_white_24dp, R.drawable.ic_folder_white_24dp)
                .execute();
    }


    /**
     * method to initialize the view
     */
    private void initializerView() {
        /**
         * Checking if user already connected
         */


        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(getPackageName() + "closeWelcomeActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);


        btnNext.setText(getString(R.string.next));
        btnNext.setEnabled(true);
        btnNext.setVisibility(View.VISIBLE);
        layoutVerificationSv.setVisibility(View.VISIBLE);
        numberPhoneLayoutSv.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

        hideKeyboard();

        mCountries = CountriesFetcher.getCountries(this);

        int defaultIdx = mCountries.indexOfIso(AppConstants.DEFAULT_COUNTRY_CODE);
        mSelectedCountry = mCountries.get(defaultIdx);
        countryCode.setText(mSelectedCountry.getDial_code());
        countryName.setText(mSelectedCountry.getName());
        shortDescriptionPhone.setText(String.format("%s %s %s", getString(R.string.click_on), mSelectedCountry.getDial_code(), getString(R.string.to_choose_your_country_n_and_enter_your_phone_number)));
        setHint();

        btnNext.setOnClickListener(this);

        countryCode.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);
        ResendBtn.setOnClickListener(this);
        changeNumberBtn.setOnClickListener(this);
        ViewPagerAdapter adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        inputOtpWrapper.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    verificationOfCode();
                }
            }
        });

        /**
         * Checking if the device is waiting for sms
         * showing the user Code screen
         */
        if (PreferenceManager.getInstance().isWaitingForSms(this)) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(WelcomeActivity.this);
            localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeSplashActivity"));
            viewPager.setCurrentItem(1);
            setTimer();
            resumeTimer();
        }


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (viewPager.getCurrentItem() == 0) {
                    btnNext.setText(getString(R.string.next));
                    btnNext.setEnabled(true);
                    progressBarLoad.setVisibility(View.GONE);
                    setOnKeyboardDone();
                }else{
                    setOnKeyboardCodeDone();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });





    }


    /**
     * method to validate user information
     */
    private void validateInformation() {
        hideKeyboard();
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        if (phoneNumber != null) {
            String phoneNumberFinal = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            if (isValid()) {
                String internationalFormat = phoneNumberFinal.replace("-", "");
                String finalResult = internationalFormat.replace(" ", "");
                PreferenceManager.getInstance().setMobileNumber(this, finalResult);
                requestForSMS(finalResult, mSelectedCountry.getName());
            } else {
                phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
            }
        } else {
            phoneNumberWrapper.setError(getString(R.string.enter_a_val_number));
        }
    }

    /**
     * method to resend a request for SMS
     *
     * @param mobile this is parameter of ResendRequestForSMS method
     */
    private void ResendRequestForSMS(String mobile) {
        mDisposable.add(APIHelper.initializeAuthService().resend(mobile).subscribe(joinModelResponse -> {
            if (joinModelResponse.isSuccess()) {
                ResendBtn.setVisibility(View.GONE);
                textViewShowTime.setVisibility(View.VISIBLE);
                setTimer();
                startTimer();
                PreferenceManager.getInstance().setIsWaitingForSms(WelcomeActivity.this, true);
                viewPager.setCurrentItem(1);
                currentMobileNumber.setText(PreferenceManager.getInstance().getMobileNumber(WelcomeActivity.this));
            } else {
                AppHelper.CustomToast(WelcomeActivity.this, joinModelResponse.getMessage());
            }
        }, throwable -> {
            AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.unexpected_reponse_from_server));
        }))
        ;

    }

    /**
     * method to send an SMS request to provider
     *
     * @param mobile  this the first parameter of  requestForSMS method
     * @param country this the second parameter of requestForSMS  method
     */


    private void requestForSMS(String mobile, String country) {
        LoginModel loginModel = new LoginModel();


        registrationTerms.setText(getString(R.string.signup_terms));
        progressBarLoad.setVisibility(View.VISIBLE);
        progressBarLoad.setColor(AppHelper.getColor(this, R.color.colorAccent));
        AppHelper.showDialog(this, getString(R.string.set_back_and_keep_calm_you_will_receive_an_sms_of_verification), true);
        btnNext.setEnabled(false);


        loginModel.setCountry(country);
        loginModel.setPhone(mobile);




        mDisposable.add(APIHelper.initializeAuthService().join(loginModel).subscribe(joinModelResponse -> {
            AppHelper.LogCat("joinModelResponse " + joinModelResponse.toString());
            AppHelper.hideDialog();


            if (joinModelResponse.isSuccess()) {

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(WelcomeActivity.this);


                localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closeSplashActivity"));
                setTimer();
                startTimer();
                PreferenceManager.getInstance().setIsWaitingForSms(WelcomeActivity.this, true);
                viewPager.setCurrentItem(1);
                currentMobileNumber.setText(PreferenceManager.getInstance().getMobileNumber(WelcomeActivity.this));


            } else {

                btnNext.setText(getString(R.string.next));
                btnNext.setEnabled(true);
                progressBarLoad.setVisibility(View.GONE);

            }

        }, throwable -> {


            AppHelper.LogCat("joinModelResponse throwable " + throwable.getMessage());
            AppHelper.hideDialog();


            btnNext.setText(getString(R.string.next));
            btnNext.setEnabled(true);
            progressBarLoad.setVisibility(View.GONE);
            AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.unexpected_reponse_from_server));
            hideKeyboard();


        }));

    }


    /**
     * method to verify the code received by user then activating the user
     */
    private void verificationOfCode() {
        hideKeyboard();
        String code = inputOtpWrapper.getText().toString().trim();
        if (!code.isEmpty()) {
            Intent otpIntent = new Intent(getApplicationContext(), SMSVerificationService.class);
            otpIntent.putExtra("code", code);
            otpIntent.putExtra("register", true);
            startService(otpIntent);
        } else {
            AppHelper.CustomToast(WelcomeActivity.this, getString(R.string.please_enter_your_ver_code));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_sms:

                Permissions.with(WelcomeActivity.this)
                        .request(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .ifNecessary()
                        .withRationaleDialog(getString(R.string.app_needs_access_to_your_contacts_and_media_in_order_to_connect_with_friends), R.drawable.ic_contacts_white_24dp, R.drawable.ic_folder_white_24dp)

                        .withPermanentDenialDialog(getString(R.string.app__requires_contacts_permission_in_order_to_attach_contact_information))
                        .onAllGranted(this::validateInformation)
                        .onAnyDenied(() -> {
                            Toast.makeText(WelcomeActivity.this, getString(R.string.all_permission_required), Toast.LENGTH_LONG).show();

                        })
                        .execute();
                break;
            case R.id.country_code:
                Intent mIntent = new Intent(this, CountryActivity.class);
                startActivityForResult(mIntent, AppConstants.SELECT_COUNTRY);
                break;

            case R.id.btn_verify_otp:
                verificationOfCode();
                break;

            case R.id.btn_change_number:
                viewPager.setCurrentItem(0);
                stopTimer();
                PreferenceManager.getInstance().setID(this, null);
                PreferenceManager.getInstance().setToken(this, null);
                PreferenceManager.getInstance().setMobileNumber(this, null);
                PreferenceManager.getInstance().setIsWaitingForSms(this, false);
                break;

            case R.id.Resend:
                viewPager.setCurrentItem(1);
                ResendRequestForSMS(PreferenceManager.getInstance().getMobileNumber(this));
                break;
        }
    }


    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((View) object);
        }

        @NonNull
        public Object instantiateItem(@NonNull View collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.numberPhone_layout;
                    break;
                case 1:
                    resId = R.id.layout_verification;
                    break;
            }
            return findViewById(resId);
        }
    }

    private void setTimer() {
        int time = 1;
        totalTimeCountInMilliseconds = 60 * time * 1000;

    }

    private void startTimer() {
        countDownTimer = new WhatsCloneCounter(totalTimeCountInMilliseconds, 500).start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void resumeTimer() {
        textViewShowTime.setVisibility(View.VISIBLE);
        countDownTimer = new WhatsCloneCounter(totalTimeCountInMilliseconds, 500).start();
    }


    public class WhatsCloneCounter extends CountDownTimer {

        WhatsCloneCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long leftTimeInMilliseconds) {
            long seconds = leftTimeInMilliseconds / 1000;
            textViewShowTime.setText(String.format(Locale.getDefault(), "%02d", seconds / 60) + ":" + String.format(Locale.getDefault(), "%02d", seconds % 60));
        }

        @Override
        public void onFinish() {
            textViewShowTime.setVisibility(View.GONE);
            ResendBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocalBroadcastManager != null)
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        if (mDisposable != null) mDisposable.dispose();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.SELECT_COUNTRY) {
                phoneNumberWrapper.setEnabled(true);
                numberPhoneLayoutSv.pageScroll(View.FOCUS_DOWN);
                String codeIso = data.getStringExtra("countryIso");
                String countryName = data.getStringExtra("countryCode");
                int defaultIdx = mCountries.indexOfIso(codeIso);
                mSelectedCountry = mCountries.get(defaultIdx);
                this.countryCode.setText(mSelectedCountry.getDial_code());
                this.countryName.setText(mSelectedCountry.getName());
                shortDescriptionPhone.setText(String.format("%s %s %s", getString(R.string.click_on), mSelectedCountry.getDial_code(), getString(R.string.to_choose_your_country_n_and_enter_your_phone_number)));
                setHint();

            }
        } else {
            AppHelper.LogCat("resultCode " + resultCode);
            AppHelper.LogCat("requestCode " + requestCode);
            if (requestCode == AppConstants.APP_REQUEST_CODE) {
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(phoneNumberWrapper.getWindowToken(), 0);
    }


    /**
     * Set hint number for country
     */
    private void setHint() {

        if (phoneNumberWrapper != null && mSelectedCountry != null && mSelectedCountry.getCode() != null) {
            Phonenumber.PhoneNumber phoneNumber = mPhoneUtil.getExampleNumberForType(mSelectedCountry.getCode(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            if (phoneNumber != null) {
                String internationalNumber = mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                String finalPhone = internationalNumber.substring(mSelectedCountry.getDial_code().length());
                phoneNumberWrapper.setHint(finalPhone);
                int numberLength = internationalNumber.length();
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(numberLength);
                phoneNumberWrapper.setFilters(fArray);

            }
        }

    }


    /**
     * Get PhoneNumber object
     *
     * @return PhoneNumber | null on error
     */
    @SuppressWarnings("unused")
    public Phonenumber.PhoneNumber getPhoneNumber() {
        try {
            String iso = null;
            if (mSelectedCountry != null) {
                iso = mSelectedCountry.getCode();
            }
            String phone = countryCode.getText().toString().concat(phoneNumberWrapper.getText().toString());
            return mPhoneUtil.parse(phone, iso);
        } catch (NumberParseException ignored) {
            return null;
        }
    }


    /**
     * Check if number is valid
     *
     * @return boolean
     */
    @SuppressWarnings("unused")
    public boolean isValid() {
        Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
        return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber);
    }

    public void setOnKeyboardDone() {
        phoneNumberWrapper.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
            }
            return false;
        });
    }

    public void setOnKeyboardCodeDone() {
        inputOtpWrapper.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verificationOfCode();
            }
            return false;
        });
    }

    private void handleChallengeReceived(@Nullable String challenge) {
        if (challenge != null && challenge.length() == 6) {

            inputOtpWrapper.setText(challenge);
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
            case AppConstants.EVENT_BUS_SMS_CODE:
                handleChallengeReceived(pusher.getData());
                break;
        }
    }

}
