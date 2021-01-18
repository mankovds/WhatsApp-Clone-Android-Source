package com.strolink.whatsUp.presenters.users;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.popups.StatusDelete;
import com.strolink.whatsUp.activities.status.EditStatusActivity;
import com.strolink.whatsUp.activities.status.StatusActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;

import com.strolink.whatsUp.models.users.status.StatusModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusPresenter implements Presenter {

    private StatusActivity view;
    private EditStatusActivity editStatusActivity;
    private StatusDelete viewDelete;


    private CompositeDisposable mDisposable;

    public StatusPresenter(StatusActivity statusActivity) {
        this.view = statusActivity;


        mDisposable = new CompositeDisposable();
    }

    public StatusPresenter(StatusDelete statusDelete) {
        this.viewDelete = statusDelete;

        mDisposable = new CompositeDisposable();

    }

    public StatusPresenter(EditStatusActivity editStatusActivity) {
        this.editStatusActivity = editStatusActivity;
        mDisposable = new CompositeDisposable();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(view)) EventBus.getDefault().register(view);

        getStatusFromServer();
        getCurrentStatus();

    }


    private void getStatusFromServer() {
        try {
            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getUserStatus(PreferenceManager.getInstance().getID(view), mDisposable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(statusModels -> {
                        AppHelper.LogCat("statusModels " + statusModels.toString());
                        view.ShowStatus(statusModels);

                    }, view::onErrorLoading));
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }
    }

    public void getCurrentStatus() {
        try {
            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getCurrentStatusFromLocal()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::ShowCurrentStatus, throwable -> AppHelper.LogCat(" " + throwable.getMessage())))
            ;
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
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
        if (view != null) {
            EventBus.getDefault().unregister(view);
        }
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

    public void DeleteAllStatus() {
        AppHelper.showDialog(view, view.getString(R.string.delete_all_status_dialog));
        mDisposable.add(APIHelper.initialApiUsersContacts().deleteAllStatus().subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();


                    List<StatusModel> statusModel = UsersController.getInstance().getAllUserStatusByUserId(PreferenceManager.getInstance().getID(view));
                    for (StatusModel statusModel1 : statusModel)
                        if (!statusModel1.isIs_default())
                            UsersController.getInstance().deleteStatus(statusModel1);

                AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                view.startActivity(view.getIntent());
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            }
        }, throwable -> {
            AppHelper.LogCat("Delete Status Error StatusPresenter ");
            AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);


            AppHelper.hideDialog();
        }))
        ;
    }


    public void DeleteStatus(String statusID) {

        AppHelper.showDialog(viewDelete, viewDelete.getString(R.string.deleting));
        mDisposable.add(APIHelper.initialApiUsersContacts().deleteStatus(statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_STATUS, statusID));
                viewDelete.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.LogCat("delete  status " + statusResponse.getMessage());
                AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("delete  status " + throwable.getMessage());
            AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
        }))
        ;
    }

    public void UpdateCurrentStatus(String status, String statusID, String currentStatusId) {
        AppHelper.showDialog(view, view.getString(R.string.updating_status));
        mDisposable.add(APIHelper.initialApiUsersContacts().updateStatus(statusID, currentStatusId).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_STATUS, status));


            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
            AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

        }))
        ;

    }


    public void EditCurrentStatus(String status, String statusID) {
        mDisposable.add(APIHelper.initialApiUsersContacts().editStatus(status, statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity, editStatusActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_STATUS, status));
                editStatusActivity.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity, editStatusActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
            AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), editStatusActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

        }))
        ;

    }

    public void onEventPush(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_DELETE_STATUS:
                String id = pusher.getStatusID();



                    try {
                        StatusModel statusModel = UsersController.getInstance().getUserStatusById(id);
                        UsersController.getInstance().deleteStatus(statusModel);


                    } catch (Exception e) {
                        AppHelper.LogCat(e.getMessage());
                        AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);


                    }

                view.deleteStatus(id);
                AppHelper.Snackbar(view, view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.your_status_updated_successfully), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

                getStatusFromServer();
                getCurrentStatus();
                break;
            case AppConstants.EVENT_BUS_UPDATE_STATUS:
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS));
                if (pusher.getData() != null)
                    view.ShowCurrentStatus(pusher.getData());
                getStatusFromServer();
                getCurrentStatus();
                break;
        }
    }
}