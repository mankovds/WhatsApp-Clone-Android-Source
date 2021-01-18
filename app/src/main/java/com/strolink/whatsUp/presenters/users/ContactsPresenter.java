package com.strolink.whatsUp.presenters.users;


import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.NewConversationContactsActivity;
import com.strolink.whatsUp.activities.stories.PrivacyContactsActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.fragments.home.ContactsFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.ForegroundRuning;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsPresenter implements Presenter {
    private NewConversationContactsActivity newConversationContactsActivity;
    private PrivacyContactsActivity privacyContactsActivity;
    private ContactsFragment contactsFragment;


    private CompositeDisposable mDisposable;

    public ContactsPresenter(PrivacyContactsActivity privacyContactsActivity) {
        this.privacyContactsActivity = privacyContactsActivity;

        mDisposable = new CompositeDisposable();
    }

    public ContactsPresenter(NewConversationContactsActivity newConversationContactsActivity) {
        this.newConversationContactsActivity = newConversationContactsActivity;

        mDisposable = new CompositeDisposable();
    }

    public ContactsPresenter(ContactsFragment contactsFragment) {

        this.contactsFragment = contactsFragment;

        mDisposable = new CompositeDisposable();
    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {

        if (newConversationContactsActivity != null) {
            getContacts();
        } else if (privacyContactsActivity != null) {

            getContacts();
        } else {
            getContactsFragment();
        }

    }

    public void getContactsFragment() {
        try {

            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getAllContacts()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(usersModels -> {


                        contactsFragment.updateContacts(usersModels);

                    }, throwable -> {
                        contactsFragment.onErrorLoading(throwable);
                    }))
            ;
            try {
                PreferenceManager.getInstance().setContactSize(contactsFragment.getActivity(), APIHelper.initialApiUsersContacts().getLinkedContactsSize());
            } catch (Exception e) {
                AppHelper.LogCat(" Exception size contact fragment " + e.getMessage());
            }
        } catch (Exception e) {
            AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
        }
        if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
            loadDataFromServer();
        }
    }

    public void getContacts() {
        if (newConversationContactsActivity != null) {
            newConversationContactsActivity.onShowLoading();
            try {

                mDisposable.add(APIHelper.initialApiUsersContacts().getAllContacts()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(contactsModels -> {
                            newConversationContactsActivity.onHideLoading();
                            newConversationContactsActivity.ShowContacts(contactsModels);
                        }, throwable -> {
                            newConversationContactsActivity.onHideLoading();
                            newConversationContactsActivity.onErrorLoading(throwable);
                        }))
                ;
                try {

                    PreferenceManager.getInstance().setContactSize(newConversationContactsActivity, APIHelper.initialApiUsersContacts().getLinkedContactsSize());
                    AppHelper.LogCat("   size contact fragment" + PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()));
                } catch (Exception e) {
                    AppHelper.LogCat(" Exception size contact fragment " + e.getMessage());
                }
            } catch (Exception e) {
                AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
            }
            if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
                loadDataFromServer();
            }


        } else if (privacyContactsActivity != null) {
            privacyContactsActivity.onShowLoading();
            try {

                mDisposable.add(APIHelper.initialApiUsersContacts()
                        .getLinkedContacts()
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(contactsModels -> {

                            privacyContactsActivity.onHideLoading();
                            privacyContactsActivity.ShowContacts(contactsModels);
                        }, throwable -> {
                            privacyContactsActivity.onHideLoading();
                            privacyContactsActivity.onErrorLoading(throwable);
                        }))
                ;
                try {
                    PreferenceManager.getInstance().setContactSize(privacyContactsActivity, APIHelper.initialApiUsersContacts().getLinkedContactsSize());

                } catch (Exception e) {
                    AppHelper.LogCat(" Exception size contact fragment");
                }
            } catch (Exception e) {
                AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
            }
            if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
                loadDataFromServer();
            }
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
        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }


    @SuppressLint("CheckResult")
    @Override
    public void onRefresh() {
        if (contactsFragment != null) {

            if (Permissions.hasAny(contactsFragment.getActivity(), Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {

                AppHelper.LogCat("Read contact data permission already granted.");
                contactsFragment.onShowLoading();
                mDisposable.addAll(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {


                    try {
                        List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                        subscriber.onNext(contactsModels);
                        subscriber.onComplete();
                    } catch (Exception throwable) {
                        subscriber.onError(throwable);
                    }
                }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
                    AppHelper.LogCat("  size contact fragment " + contacts.size());
                    APIHelper.initialApiUsersContacts().updateContacts(contacts)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(contactsModelList -> {



                                new Handler().postDelayed(() -> {
                                    try {
                                        mDisposable.addAll(APIHelper.initialApiUsersContacts().getAllContacts()
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(usersModels -> {
                                                    AppHelper.LogCat("usersModels " + usersModels.size());
                                                    contactsFragment.onHideLoading();

                                                    if (contactsFragment != null) {
                                                        if (PreferenceManager.getInstance().getStoriesPrivacy(contactsFragment.getActivity()) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS) {


                                                            List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
                                                            AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());

                                                            for (UsersModel usersModel : usersModels) {
                                                                if (!UsersController.getInstance().checkIfPrivacyUserExist(usersModel.get_id()) && usersModel.isActivate() && usersModel.isLinked()) {
                                                                    UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                                                                    usersPrivacyModel.setUp_id(usersModel.get_id());
                                                                    usersPrivacyModel.setExclude(false);
                                                                    usersPrivacyModel.setUsersModel(usersModel);
                                                                    UsersController.getInstance().insertUserPrivacy(usersPrivacyModel);
                                                                }
                                                            }
                                                            PreferenceManager.getInstance().setStoriesPrivacy(contactsFragment.getActivity(), AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS);


                                                        }
                                                        contactsFragment.updateContacts(usersModels);
                                                    }
                                                }, throwable -> {
                                                    contactsFragment.onErrorLoading(throwable);
                                                    contactsFragment.onHideLoading();

                                                }))
                                        ;
                                        try {

                                            PreferenceManager.getInstance().setContactSize(contactsFragment.getActivity(), APIHelper.initialApiUsersContacts().getLinkedContactsSize());

                                        } catch (Exception e) {
                                            AppHelper.LogCat(" Exception size contact fragment");
                                        }
                                    } catch (Exception e) {
                                        AppHelper.LogCat("getAllContacts Exception ContactsPresenter ");
                                    }
                                    AppHelper.CustomToast(WhatsCloneApplication.getInstance(), contactsFragment.getString(R.string.success_response_contacts));
                                    //  mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(contactsFragment)).subscribe(contactsModel -> AppHelper.LogCat("getContactInfo"), AppHelper::LogCat));

                                }, 2000);
                            }, throwable -> {
                                contactsFragment.onErrorLoading(throwable);

                                if (ForegroundRuning.get().isForeground()) {
                                    try {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(contactsFragment.getActivity());
                                        alert.setMessage(contactsFragment.getString(R.string.error_response_contacts));
                                        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                                        });
                                        alert.setCancelable(false);
                                        alert.show();
                                    } catch (Exception e) {
                                        AppHelper.LogCat("Exception " + e.getMessage());
                                        AppHelper.CustomToast(contactsFragment.getActivity(), contactsFragment.getString(R.string.error_response_contacts));
                                    }
                                }
                            }, () -> {
                                contactsFragment.onHideLoading();
                            });
                }, throwable -> {
                    AppHelper.LogCat(" " + throwable.getMessage());
                }))
                ;

            } else {
                Permissions.with(contactsFragment)
                        .request(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)
                        .ifNecessary()
                        .withRationaleDialog(contactsFragment.getString(R.string.app__requires_contacts_permission_in_order_to_attach_contact_information),
                                R.drawable.ic_contacts_white_24dp)
                        .onAnyResult(() -> {

                        })
                        .execute();
            }
        }


    }

    @Override
    public void onStop() {

    }

    private void loadDataFromServer() {
        //   getContactInfo();
        if (newConversationContactsActivity != null)
            newConversationContactsActivity.onShowLoading();
        else if (privacyContactsActivity != null) {
            privacyContactsActivity.onShowLoading();
        }
        mDisposable.add(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {
            try {
                List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                subscriber.onNext(contactsModels);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
            mDisposable.add(APIHelper.initialApiUsersContacts().updateContacts(contacts)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(contactsModelList -> {
                        //newConversationContactsActivity.ShowContacts(contactsModelList);

                        new Handler().postDelayed(() -> {
                            try {
                                if (contactsFragment != null) {
                                    mDisposable.addAll(APIHelper.initialApiUsersContacts()
                                            .getLinkedContacts()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(usersModels -> {


                                                if (contactsFragment != null) {
                                                    if (PreferenceManager.getInstance().getStoriesPrivacy(contactsFragment.getActivity()) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS) {

                                                        List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
                                                        AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());

                                                        for (UsersModel usersModel : usersModels) {
                                                            if (!UsersController.getInstance().checkIfPrivacyUserExist(usersModel.get_id()) && usersModel.isActivate() && usersModel.isLinked()) {
                                                                UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                                                                usersPrivacyModel.setUp_id(usersModel.get_id());
                                                                usersPrivacyModel.setExclude(false);
                                                                usersPrivacyModel.setUsersModel(usersModel);
                                                                UsersController.getInstance().insertUserPrivacy(usersPrivacyModel);
                                                            }
                                                        }
                                                        PreferenceManager.getInstance().setStoriesPrivacy(contactsFragment.getActivity(), AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS);


                                                    }
                                                    contactsFragment.onHideLoading();
                                                    contactsFragment.updateContacts(usersModels);

                                                } else if (privacyContactsActivity != null) {
                                                    privacyContactsActivity.onHideLoading();
                                                    privacyContactsActivity.ShowContacts(usersModels);
                                                }
                                            }, throwable -> {
                                                if (privacyContactsActivity != null) {

                                                    privacyContactsActivity.onErrorLoading(throwable);
                                                    privacyContactsActivity.onHideLoading();
                                                } else
                                                    contactsFragment.onErrorLoading(throwable);
                                            }));
                                } else {
                                    mDisposable.addAll(APIHelper.initialApiUsersContacts().getAllContacts()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(contactsModels -> {
                                                if (newConversationContactsActivity != null)
                                                    newConversationContactsActivity.ShowContacts(contactsModels);


                                            }, throwable -> {
                                                if (newConversationContactsActivity != null) {
                                                    newConversationContactsActivity.onErrorLoading(throwable);
                                                    newConversationContactsActivity.onHideLoading();
                                                }
                                            }));
                                }
                                try {
                                    if (newConversationContactsActivity != null)

                                        PreferenceManager.getInstance().setContactSize(newConversationContactsActivity, APIHelper.initialApiUsersContacts().getLinkedContactsSize());

                                    else if (privacyContactsActivity != null) {
                                        PreferenceManager.getInstance().setContactSize(privacyContactsActivity, APIHelper.initialApiUsersContacts().getLinkedContactsSize());

                                    } else
                                        PreferenceManager.getInstance().setContactSize(contactsFragment.getActivity(), APIHelper.initialApiUsersContacts().getLinkedContactsSize());

                                } catch (Exception e) {
                                    AppHelper.LogCat(" Exception size contact fragment");
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("getAllContacts Exception ContactsPresenter ");
                            }
                            if (newConversationContactsActivity != null)
                                AppHelper.CustomToast(newConversationContactsActivity, newConversationContactsActivity.getString(R.string.success_response_contacts));

                        }, 2000);

                    }, throwable -> {
                        if (newConversationContactsActivity != null)
                            newConversationContactsActivity.onErrorLoading(throwable);
                        else if (privacyContactsActivity != null) {
                            privacyContactsActivity.onErrorLoading(throwable);
                        } else
                            contactsFragment.onErrorLoading(throwable);
                    }, () -> {

                    }));
        }, throwable -> {
            AppHelper.LogCat(" " + throwable.getMessage());
        }));

    }


}