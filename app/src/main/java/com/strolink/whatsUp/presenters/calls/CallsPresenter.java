package com.strolink.whatsUp.presenters.calls;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.call.CallDetailsActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.fragments.home.CallsFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.CallsController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallsPresenter implements Presenter {

    private CallsFragment callsFragment;
    private CallDetailsActivity callDetailsActivity;


    private String userID;
    private String callID;
    private CompositeDisposable mDisposable;


    public CallsPresenter(CallsFragment callsFragment) {
        this.callsFragment = callsFragment;


    }

    public CallsPresenter(CallDetailsActivity callDetailsActivity) {
        this.callDetailsActivity = callDetailsActivity;

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        if (callDetailsActivity != null) {
            callID = callDetailsActivity.getIntent().getStringExtra("callID");
            userID = callDetailsActivity.getIntent().getStringExtra("userID");

        } else {
            if (!EventBus.getDefault().isRegistered(callsFragment))
                EventBus.getDefault().register(callsFragment);
        }


        if (callDetailsActivity != null) {


            getCallerDetailsInfo(userID);
            getCallDetails(callID);
            getCallsDetailsList(callID);
        } else {

            getCallsList(false);
        }
    }

    private void getCallerDetailsInfo(String userID) {

        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(userID, mDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString()
                    );
                    callDetailsActivity.showUserInfo(usersModel);

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));

    }

    private void getCallDetails(String callID) {
        mDisposable.add(
                APIHelper.initialApiUsersContacts().getCallDetails(callID)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(callsModel -> {
                    callDetailsActivity.showCallInfo(callsModel);
                }, AppHelper::LogCat));
    }

    private void getCallsDetailsList(String callID) {

        try {
            mDisposable.add(
                    APIHelper.initialApiUsersContacts().getAllCallsDetails(callID)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(callsInfoModels -> {
                        callDetailsActivity.UpdateCallsDetailsList(callsInfoModels);
                    }, AppHelper::LogCat));

        } catch (Exception e) {
            AppHelper.LogCat("calls presenter " + e.getMessage());
        }
    }

    private void getCallsList(boolean refresh) {

        if (refresh)
            callsFragment.onShowLoading();
        try {
            mDisposable.add(
                    APIHelper.initialApiUsersContacts().getAllCalls()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(callsModels -> {
                        callsFragment.UpdateCalls(callsModels);
                        callsFragment.onHideLoading();
                    }, callsFragment::onErrorLoading));

        } catch (Exception e) {
            AppHelper.LogCat("calls presenter " + e.getMessage());
        }
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        if (callsFragment != null)
            EventBus.getDefault().unregister(callsFragment);

        if (mDisposable != null)
            mDisposable.dispose();
    }

    public void removeCall() {

        AppHelper.showDialog(callDetailsActivity, callDetailsActivity.getString(R.string.delete_call_dialog));

        CallsModel callsModel = CallsController.getInstance().getCallById(callID);
        List<CallsInfoModel> callsInfoModel = CallsController.getInstance().loadAllCallInfoByCallId(callsModel.getC_id());
        for (CallsInfoModel callsInfoModel1 : callsInfoModel)
            CallsController.getInstance().deleteCallInfo(callsInfoModel1);
        CallsController.getInstance().deleteCall(callsModel);

        AppHelper.hideDialog();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_CALL_ITEM, callID));
        callDetailsActivity.finish();


    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {
        getCallsList(true);
    }

    @Override
    public void onStop() {

    }
}
