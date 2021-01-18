package com.strolink.whatsUp.jobs.users;

import android.Manifest;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.models.users.contacts.UsersModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SyncingContactsWithServerWorker extends ListenableWorker {

    public static final String TAG = SyncingContactsWithServerWorker.class.getSimpleName();
    private CompositeDisposable mDisposable;


    private SettableFuture<Result> mFuture;

    public SyncingContactsWithServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        mFuture = SettableFuture.create();
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        mDisposable = new CompositeDisposable();
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {

            if (Permissions.hasAny(getApplicationContext(), Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {

                mDisposable.addAll(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {

                    try {
                        List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                        subscriber.onNext(contactsModels);
                        subscriber.onComplete();
                    } catch (Exception throwable) {
                        subscriber.onError(throwable);
                    }
                }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
                    AppHelper.LogCat("completeJob: " + "jobFinished");
                    AppHelper.LogCat("  size contact ScheduledJobService " + contacts.size());
                    //Tell the framework that the job has completed and  needs to be reschedule
                    mDisposable.add(APIHelper.initialApiUsersContacts()
                            .updateContacts(contacts)
                            .subscribe(contactsModelList -> {
                                //Tell the framework that the job has completed and doesnot needs to be reschedule

                                mFuture.set(Result.success());
                            }, throwable -> {

                                //Tell the framework that the job has completed and  needs to be reschedule
                                mFuture.set(Result.retry());
                            }));

                }, throwable -> {
                    AppHelper.LogCat("completeJob: " + "jobFinished");
                    //Tell the framework that the job has completed and  needs to be reschedule

                    mFuture.set(Result.retry());
                    AppHelper.LogCat(" " + throwable.getMessage());
                }));
            }


        } else {
            mFuture.set(Result.failure());
        }
        return mFuture;
    }

    @Override
    public void onStopped() {
        super.onStopped();

        if (mDisposable != null)
            mDisposable.dispose();
    }

}