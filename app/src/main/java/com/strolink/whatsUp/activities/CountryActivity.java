package com.strolink.whatsUp.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.adapters.others.TextWatcherAdapter;
import com.strolink.whatsUp.adapters.recyclerView.CountriesAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.models.CountriesModel;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 10/30/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CountryActivity extends BaseActivity {

    private CountriesAdapter mCountriesAdapter;

    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    AppCompatImageView clearBtn;
    @BindView(R.id.close_btn_search_view)
    AppCompatImageView closeBtn;

    @BindView(R.id.CounrtriesList)
    RecyclerView CountriesList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counrty);
        ButterKnife.bind(this);
        initializerView();

    }



    void initializerView() {

        initializerSearchView(searchInput, clearBtn);
        clearBtn.setOnClickListener(v -> clearSearchView());
        closeBtn.setOnClickListener(v -> closeSearchView());
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        CountriesList.setLayoutManager(mLinearLayoutManager);
        mCountriesAdapter = new CountriesAdapter(this);
        CountriesList.setAdapter(mCountriesAdapter);
        Gson gson = new Gson();
        final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
        }.getType());
        mCountriesAdapter.setCountries(list);

        // set recycler view to fastScroller
        fastScroller.setRecyclerView(CountriesList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }


    /**
     * method to clear/reset search view content
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            Gson gson = new Gson();
            final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(this), new TypeToken<List<CountriesModel>>() {
            }.getType());
            mCountriesAdapter.setCountries(list);
        }

    }

    /**
     * method to initial the search view
     */
    public void initializerSearchView(TextInputEditText searchInput, AppCompatImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCountriesAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    Gson gson = new Gson();
                    final List<CountriesModel> list = gson.fromJson(AppHelper.loadJSONFromAsset(CountryActivity.this), new TypeToken<List<CountriesModel>>() {
                    }.getType());
                    mCountriesAdapter.setCountries(list);
                }
            }
        });

    }

    /**
     * method to start searching
     *
     * @param string this is parameter of Search method
     */
    public void Search(String string) {

        final List<CountriesModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mCountriesAdapter.animateTo(filteredModelList);
            CountriesList.scrollToPosition(0);
        }
    }

    /**
     * method to filter the list
     *
     * @param query this is parameter of FilterList method
     * @return this for what method return
     */
    private List<CountriesModel> FilterList(String query) {
        query = query.toLowerCase();
        List<CountriesModel> countriesModelList = mCountriesAdapter.getCountries();
        final List<CountriesModel> filteredModelList = new ArrayList<>();
        for (CountriesModel countriesModel : countriesModelList) {
            final String name = countriesModel.getName().toLowerCase();
            if (name.contains(query)) {
                filteredModelList.add(countriesModel);
            }
        }
        return filteredModelList;
    }

    /**
     * method to close the search view
     */
    public void closeSearchView() {
        finish();

    }
    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
