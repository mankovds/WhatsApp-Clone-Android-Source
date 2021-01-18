package com.strolink.whatsUp.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.welcome.IntroActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 3/13/17.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class PreferenceLanguageActivity extends AppCompatActivity {


    @BindView(R.id.indicator_english)
    TextView indicatorEnglish;
    @BindView(R.id.indicator_french)
    TextView indicatorFrench;
    @BindView(R.id.indicator_german)
    TextView indicatorGerman;

    @BindView(R.id.indicator_italian)
    TextView indicatorItalian;

    @BindView(R.id.indicator_romaneste)
    TextView indicatorRomaneste;
    @BindView(R.id.indicator_srbski)
    TextView indicatorSrbski;
    @BindView(R.id.indicator_bosnisch)
    TextView indicatorBosnisch;
    @BindView(R.id.indicator_turski)
    TextView indicatorTurski;
    @BindView(R.id.indicator_tschechisch)
    TextView indicatorTschechisch;
    @BindView(R.id.indicator_grichisch)
    TextView indicatorGrichisch;
    @BindView(R.id.indicator_spanisch)
    TextView indicatorSpanisch;
    @BindView(R.id.indicator_kroatisch)
    TextView indicatorKroatisch;
    @BindView(R.id.indicator_indonesisch)
    TextView indicatorIndonesisch;
    @BindView(R.id.indicator_mazedonisch)
    TextView indicatorMazedonisch;
    @BindView(R.id.indicator_rusisch)
    TextView indicatorRusisch;
    @BindView(R.id.indicator_slovenisch)
    TextView indicatorSlovenisch;

    @BindView(R.id.english_btn)
    LinearLayout EnglishBtn;
    @BindView(R.id.french_btn)
    LinearLayout FrenchBtn;
    @BindView(R.id.german_btn)
    LinearLayout GermanBtn;
    @BindView(R.id.italian_btn)
    LinearLayout ItalianBtn;
    @BindView(R.id.romaneste_btn)
    LinearLayout RomanesteBtn;
    @BindView(R.id.srbski_btn)
    LinearLayout SrbskiBtn;
    @BindView(R.id.bosnisch_btn)
    LinearLayout BosnischBtn;
    @BindView(R.id.turski_btn)
    LinearLayout TurskiBtn;
    @BindView(R.id.tschechisch_btn)
    LinearLayout TschechischBtn;
    @BindView(R.id.grichisch_btn)
    LinearLayout GrichischBtn;
    @BindView(R.id.spanisch_btn)
    LinearLayout SpanischBtn;
    @BindView(R.id.kroatisch_btn)
    LinearLayout KroatischBtn;
    @BindView(R.id.indonesisch_btn)
    LinearLayout IndonesischBtn;
    @BindView(R.id.mazedonisch_btn)
    LinearLayout MazedonischBtn;
    @BindView(R.id.rusisch_btn)
    LinearLayout RusischBtn;
    @BindView(R.id.slovenisch_btn)
    LinearLayout SlovenischBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);
        setupToolbar();
        EnglishBtn.setOnClickListener(view -> {
            if (indicatorEnglish.getVisibility() == View.GONE) {
                ChangeLanguage("en", "US");
            }
        });
        FrenchBtn.setOnClickListener(view -> {
            if (indicatorFrench.getVisibility() == View.GONE) {
                ChangeLanguage("fr", null);
            }
        });
        ////////////
        GermanBtn.setOnClickListener(view -> {
            if (indicatorGerman.getVisibility() == View.GONE) {
                ChangeLanguage("de", null);
            }
        });
        ItalianBtn.setOnClickListener(view -> {
            if (indicatorItalian.getVisibility() == View.GONE) {
                ChangeLanguage("it", null);
            }
        });
        RomanesteBtn.setOnClickListener(view -> {
            if (indicatorRomaneste.getVisibility() == View.GONE) {
                ChangeLanguage("ro", null);
            }
        });
        SrbskiBtn.setOnClickListener(view -> {
            if (indicatorSrbski.getVisibility() == View.GONE) {
                ChangeLanguage("sr", null);
            }
        });
        BosnischBtn.setOnClickListener(view -> {
            if (indicatorBosnisch.getVisibility() == View.GONE) {
                ChangeLanguage("bs", null);
            }
        });
        TurskiBtn.setOnClickListener(view -> {
            if (indicatorSrbski.getVisibility() == View.GONE) {
                ChangeLanguage("tr", null);
            }
        });
        TschechischBtn.setOnClickListener(view -> {
            if (indicatorTschechisch.getVisibility() == View.GONE) {
                ChangeLanguage("cs", null);
            }
        });
        GrichischBtn.setOnClickListener(view -> {
            if (indicatorGrichisch.getVisibility() == View.GONE) {
                ChangeLanguage("el", null);
            }
        });
        SpanischBtn.setOnClickListener(view -> {
            if (indicatorSpanisch.getVisibility() == View.GONE) {
                ChangeLanguage("es", null);
            }
        });
        KroatischBtn.setOnClickListener(view -> {
            if (indicatorKroatisch.getVisibility() == View.GONE) {
                ChangeLanguage("hr", null);
            }
        });
        IndonesischBtn.setOnClickListener(view -> {
            if (indicatorIndonesisch.getVisibility() == View.GONE) {
                ChangeLanguage("id", null);
            }
        });
        MazedonischBtn.setOnClickListener(view -> {
            if (indicatorMazedonisch.getVisibility() == View.GONE) {
                ChangeLanguage("mk", null);
            }
        });
        RusischBtn.setOnClickListener(view -> {
            if (indicatorRusisch.getVisibility() == View.GONE) {
                ChangeLanguage("ru", null);
            }
        });
        SlovenischBtn.setOnClickListener(view -> {
            if (indicatorSlovenisch.getVisibility() == View.GONE) {
                ChangeLanguage("sl", null);
            }
        });

        loadLocale();
    }


    /**
     * method to setup toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_language);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppHelper.LogCat("onConfigurationChanged " + newConfig.locale);
    }


    public void ChangeLanguage(String lang, String country) {
        if (lang.equalsIgnoreCase(""))
            return;
        saveLocale(lang);
        if (country == null)
            setDefaultLocale(this, new Locale(lang));
        else
            setDefaultLocale(this, new Locale(lang, country));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.you_need_to_restart_the_application);
        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent mainIntent = new Intent(this, IntroActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
        alert.setCancelable(false);
        alert.show();
    }

    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public void saveLocale(String lang) {
        PreferenceManager.getInstance().setLanguage(this, lang);
    }

    public void loadLocale() {
        String language = PreferenceManager.getInstance().getLanguage(this);
        AppHelper.LogCat("language " + language + " getDefault " + Locale.getDefault());
        if (language.startsWith("fr")) {

            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.VISIBLE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("en")) {

            indicatorEnglish.setVisibility(View.VISIBLE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("de")) {

            indicatorGerman.setVisibility(View.VISIBLE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("it")) {
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.VISIBLE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("sr")) {

            indicatorSrbski.setVisibility(View.VISIBLE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("ro")) {

            indicatorRomaneste.setVisibility(View.VISIBLE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("bs")) {

            indicatorBosnisch.setVisibility(View.VISIBLE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("cs")) {

            indicatorTschechisch.setVisibility(View.VISIBLE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("tr")) {

            indicatorTurski.setVisibility(View.VISIBLE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("el")) {

            indicatorGrichisch.setVisibility(View.VISIBLE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("es")) {

            indicatorSpanisch.setVisibility(View.VISIBLE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("hr")) {

            indicatorKroatisch.setVisibility(View.VISIBLE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("id")) {

            indicatorIndonesisch.setVisibility(View.VISIBLE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("mk")) {

            indicatorMazedonisch.setVisibility(View.VISIBLE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("ru")) {

            indicatorRusisch.setVisibility(View.VISIBLE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorSlovenisch.setVisibility(View.GONE);
        } else if (language.startsWith("sl")) {

            indicatorSlovenisch.setVisibility(View.VISIBLE);
            indicatorRusisch.setVisibility(View.GONE);
            indicatorMazedonisch.setVisibility(View.GONE);
            indicatorIndonesisch.setVisibility(View.GONE);
            indicatorKroatisch.setVisibility(View.GONE);
            indicatorSpanisch.setVisibility(View.GONE);
            indicatorGrichisch.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
            indicatorBosnisch.setVisibility(View.GONE);
            indicatorTurski.setVisibility(View.GONE);
            indicatorRomaneste.setVisibility(View.GONE);
            indicatorFrench.setVisibility(View.GONE);
            indicatorEnglish.setVisibility(View.GONE);
            indicatorSrbski.setVisibility(View.GONE);
            indicatorGerman.setVisibility(View.GONE);
            indicatorItalian.setVisibility(View.GONE);
            indicatorTschechisch.setVisibility(View.GONE);
        }


    }
}