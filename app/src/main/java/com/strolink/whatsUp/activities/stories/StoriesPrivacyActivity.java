package com.strolink.whatsUp.activities.stories;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Abderrahim El imame on 7/27/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesPrivacyActivity extends BaseActivity {

    private Unbinder unbinder;

    @BindView(R.id.my_contacts)
    RadioButton my_contacts;

    @BindView(R.id.my_contacts_except)
    RadioButton my_contacts_except;

    @BindView(R.id.my_contacts_with)
    RadioButton my_contacts_with;

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories_privacy);
        unbinder = ButterKnife.bind(this);
        setupToolbar();
        initializerView();

    }

    @SuppressLint("CheckResult")
    private void initializerView() {
        if (PreferenceManager.getInstance().getStoriesPrivacy(this) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS) {
            my_contacts.toggle();
        } else if (PreferenceManager.getInstance().getStoriesPrivacy(this) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS_EXCEPT) {
            my_contacts_except.toggle();
        } else if (PreferenceManager.getInstance().getStoriesPrivacy(this) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS_WITH) {
            my_contacts_with.toggle();
        }
        my_contacts_except.setOnClickListener(view -> {

            Intent intent = new Intent(this, PrivacyContactsActivity.class);
            intent.putExtra("exclude", true);
            startActivity(intent);
            AnimationsUtil.setTransitionAnimation(this);

        });
        my_contacts_with.setOnClickListener(view -> {

            Intent intent = new Intent(this, PrivacyContactsActivity.class);
            intent.putExtra("exclude", false);
            startActivity(intent);
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected

            if (my_contacts.getId() == checkedId) {
                AppHelper.LogCat("my_contacts click " + checkedId);


                APIHelper.initialApiUsersContacts().getLinkedContacts().subscribe(usersModels -> {
                    AppHelper.LogCat("usersPrivacyModels usersModels" + usersModels.size());

                    List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
                    AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());
                    if (usersPrivacyModels.size() != 0)
                        for (UsersPrivacyModel usersPrivacyModel : usersPrivacyModels)
                            UsersController.getInstance().deleteUserPrivacy(usersPrivacyModel);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (UsersModel usersModel : usersModels) {
                        if (!UsersController.getInstance().checkIfPrivacyUserExist(usersModel.get_id())) {
                            UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                            usersPrivacyModel.setUp_id(usersModel.get_id());
                            usersPrivacyModel.setExclude(false);
                            usersPrivacyModel.setUsersModel(usersModel);
                            UsersController.getInstance().insertUserPrivacy(usersPrivacyModel);
                        }
                    }
                    PreferenceManager.getInstance().setStoriesPrivacy(this, AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS);


                }, throwable -> {
                    AppHelper.CustomToast(this, getString(R.string.oops_something));
                });
            }/* else if (my_contacts_except.getId() == checkedId) {
                AppHelper.LogCat("my_contacts_except click " + checkedId);

                Intent intent = new Intent(this,PrivacyContactsActivity.class);
                intent.putExtra("exclude",true);
                startActivity(intent);
                AnimationsUtil.setTransitionAnimation(this);
            } else if (my_contacts_with.getId() == checkedId) {


                AppHelper.LogCat("my_contacts_with click " + checkedId);
                Intent intent = new Intent(this,PrivacyContactsActivity.class);
                intent.putExtra("exclude",false);
                startActivity(intent);
            }*/
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
