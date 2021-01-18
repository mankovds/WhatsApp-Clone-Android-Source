package com.strolink.whatsUp.presenters.users;


import com.strolink.whatsUp.activities.profile.ProfilePreviewActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.Presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewPresenter implements Presenter {
    private ProfilePreviewActivity profilePreviewActivity;
    private CompositeDisposable compositeDisposable;
    private String userID;
    private String groupID;

    public ProfilePreviewPresenter(ProfilePreviewActivity profilePreviewActivity) {
        this.profilePreviewActivity = profilePreviewActivity;


    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {

        compositeDisposable = new CompositeDisposable();
        if (profilePreviewActivity != null) {


            if (profilePreviewActivity.getIntent().hasExtra("userID")) {
                userID = profilePreviewActivity.getIntent().getExtras().getString("userID");
                getContactLocal();

            }

            if (profilePreviewActivity.getIntent().hasExtra("groupID")) {
                groupID = profilePreviewActivity.getIntent().getExtras().getString("groupID");

                getGroupLocal();

            }
        }
    }

    private void getGroupLocal() {


        compositeDisposable.add(APIHelper.initializeApiGroups()
                .getGroupInfo(groupID, compositeDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groupModel -> {
                    AppHelper.LogCat("groupModel " + groupModel.toString());
                    profilePreviewActivity.ShowGroup(groupModel);
                }, throwable -> {
                    AppHelper.LogCat("groupModel " + throwable.getMessage());

                }));
    }


    private void getContactLocal() {

        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(userID, compositeDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString());
                    profilePreviewActivity.ShowContact(usersModel);

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {

        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }

}