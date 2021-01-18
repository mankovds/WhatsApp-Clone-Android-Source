package com.strolink.whatsUp.presenters.groups;

import com.strolink.whatsUp.activities.groups.AddNewMembersToGroupActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.Presenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 26/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupPresenter implements Presenter {
    private AddNewMembersToGroupActivity view;

    private CompositeDisposable mDisposable;

    public AddNewMembersToGroupPresenter(AddNewMembersToGroupActivity addMembersToGroupActivity) {
        this.view = addMembersToGroupActivity;


    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getLinkedContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModels -> {
                    AppHelper.LogCat("usersModels " + usersModels);
                    view.ShowContacts(usersModels);
                }, throwable -> AppHelper.LogCat("AddNewMembersToGroupPresenter " + throwable.getMessage())));
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (mDisposable != null) mDisposable.dispose();
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