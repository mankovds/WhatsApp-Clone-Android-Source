package com.strolink.whatsUp.presenters.controllers;

import android.annotation.SuppressLint;

import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CustomNullException;
import com.strolink.whatsUp.models.UploadInfo;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;
import com.strolink.whatsUp.models.users.status.StatusModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class UsersController {

    private static volatile UsersController Instance = null;


    public UsersController() {
    }

    public static UsersController getInstance() {

        UsersController localInstance = Instance;
        if (localInstance == null) {
            synchronized (UsersController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UsersController();
                }
            }
        }
        return localInstance;

    }


    public List<UsersModel> loadAllUsers(String userId) {

        try {
            return Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {
                try {
                    List<UsersModel> usersModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadAllLinkedUsers(userId);
                    if (usersModel != null)
                        subscriber.onNext(usersModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public List<UsersModel> loadAllUsersQuery(String userId, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {
                try {
                    List<UsersModel> usersModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadAllUsersQuery(userId, query);
                    if (usersModel != null)
                        subscriber.onNext(usersModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public List<UsersModel> loadAllLinkedUsersQuery(String userId, String query) {
        try {
            return Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {
                try {
                    List<UsersModel> usersModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadAllLinkedUsersQuery(userId, query);
                    if (usersModel != null)
                        subscriber.onNext(usersModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public UsersBlockModel getUserBlockedById(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<UsersBlockModel>) subscriber -> {
                try {
                    UsersBlockModel usersBlockModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().loadUserBlockedById(userId);
                    if (usersBlockModel != null)
                        subscriber.onNext(usersBlockModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }
    }

    public void insertUserBlocked(UsersBlockModel usersBlockModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().insert(usersBlockModel));

    }

    public void updateUserBlocked(UsersBlockModel usersBlockModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().update(usersBlockModel));
    }

    public void deleteUserBlocked(UsersBlockModel usersBlockModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().delete(usersBlockModel));
    }


    public int userBlockedExistence(String userId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersBlockedDao().userExistence(userId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();


    }

    public UsersModel getUserById(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<UsersModel>) subscriber -> {
                try {
                    UsersModel usersModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().loadSingleUserById(userId);
                    if (usersModel != null)
                        subscriber.onNext(usersModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }
    }


    public void insertUser(UsersModel usersModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().insert(usersModel);
            } catch (Exception e) {
                AppHelper.LogCat("Exception" + e.getMessage());
            }
        });
    }

    public void updateUser(UsersModel usersModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().update(usersModel);
            } catch (Exception e) {
                AppHelper.LogCat("Exception" + e.getMessage());
            }
        });
    }


    public UploadInfo getSingleFileById(String uploadId) {
        try {
            return Observable.create((ObservableOnSubscribe<UploadInfo>) subscriber -> {
                try {
                    UploadInfo uploadInfo = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().loadSingleFileById(uploadId);
                    if (uploadInfo != null)
                        subscriber.onNext(uploadInfo);
                    else
                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }
    }

    public List<UploadInfo> getAllFilesById() {
        try {
            return Observable.create((ObservableOnSubscribe<List<UploadInfo>>) subscriber -> {
                try {
                    List<UploadInfo> uploadInfo = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().loadAllFiles();
                    if (uploadInfo != null)
                        subscriber.onNext(uploadInfo);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void insertFile(UploadInfo uploadInfo) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().insert(uploadInfo));
    }

    public void updateFile(UploadInfo uploadInfo) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().update(uploadInfo));
    }

    public void deleteFile(UploadInfo uploadInfo) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().delete(uploadInfo));
    }

    public int containsFile(String uploadId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).upDownDao().fileExistence(uploadId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }


    public GroupModel getGroupById(String groupId) {
        try {
            return Observable.create((ObservableOnSubscribe<GroupModel>) subscriber -> {
                try {
                    GroupModel groupModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().loadSingleGroupById(groupId);
                    if (groupModel != null)
                        subscriber.onNext(groupModel);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public int groupExistence(String groupId) {
        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().groupExistence(groupId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public int userExistence(String userId) {
        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).userDao().userExistence(userId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public void deleteGroup(GroupModel groupModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().delete(groupModel));
    }

    public void updateGroup(GroupModel groupModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().update(groupModel));
    }

    public void insertGroup(GroupModel groupModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).groupDao().insert(groupModel));
    }

    public List<MembersModel> loadAllGroupMembers(String groupId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<MembersModel>>) subscriber -> {
                try {
                    List<MembersModel> membersModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().loadAllGroupMembers(groupId);
                    if (membersModels != null)
                        subscriber.onNext(membersModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public int groupMemberCount(String groupId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().groupMemberCount(groupId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public MembersModel loadSingleMemberById(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<MembersModel>) subscriber -> {
                try {
                    MembersModel membersModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().loadSingleMemberById(userId);
                    if (membersModels != null)
                        subscriber.onNext(membersModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public MembersModel loadSingleMemberByOwnerId(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<MembersModel>) subscriber -> {
                try {
                    MembersModel membersModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().loadSingleMemberByOwnerId(userId);
                    if (membersModels != null)
                        subscriber.onNext(membersModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public MembersModel loadSingleMemberByOwnerIdAndGroupId(String groupId, String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<MembersModel>) subscriber -> {
                try {
                    MembersModel membersModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().loadSingleMemberByOwnerIdAndGroupId(groupId, userId);
                    if (membersModels != null)
                        subscriber.onNext(membersModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public void deleteMember(MembersModel membersModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().delete(membersModel));
    }

    public void updateMember(MembersModel membersModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().update(membersModel));
    }

    public void insertMember(MembersModel membersModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().insert(membersModel));
    }

    public int memberExistence(String memberId, String groupId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().memberExistence(memberId, groupId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public int userIsMemberExistence(String userId, String groupId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().userIsMemberExistence(userId, groupId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

    public int memberIsLeft(String userId, String groupId) {

        return Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).membersDao().memberIsLeft(userId, groupId));

                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }


    public Single<List<StatusModel>> getAllUserStatusById(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<Single<List<StatusModel>>>) subscriber -> {
                try {
                    Single<List<StatusModel>> statusModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().loadAllStatus(userId);
                    if (statusModels != null)
                        subscriber.onNext(statusModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return Single.just(new ArrayList<>());
        }

    }

    public List<StatusModel> getAllUserStatusByUserId(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<List<StatusModel>>) subscriber -> {
                try {
                    List<StatusModel> statusModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().loadAllUserStatusByUserId(userId);
                    if (statusModels != null)
                        subscriber.onNext(statusModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }

    }

    public Single<StatusModel> getCurrentUserStatusById(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<Single<StatusModel>>) subscriber -> {
                try {
                    Single<StatusModel> statusModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().loadCurrentStatus(userId);
                    if (statusModels != null)
                        subscriber.onNext(statusModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }


    }

    public StatusModel getUserStatusByUserId(String userId) {
        try {
            return Observable.create((ObservableOnSubscribe<StatusModel>) subscriber -> {
                try {
                    StatusModel statusModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().loadUserStatusByUserId(userId);
                    if (statusModels != null)
                        subscriber.onNext(statusModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public StatusModel getUserStatusById(String statusId) {
        try {
            return Observable.create((ObservableOnSubscribe<StatusModel>) subscriber -> {
                try {
                    StatusModel statusModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().loadUserStatusById(statusId);
                    if (statusModels != null)
                        subscriber.onNext(statusModels);
                    else

                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return null;
        }

    }

    public void insertStatus(StatusModel statusModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().insert(statusModel));

    }

    public void deleteAllOldStatus(List<StatusModel> statusModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().deleteAllOldStatus(statusModel));
    }

    public void insertAllStatus(List<StatusModel> statusModel) {

        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().insertAll(statusModel));
    }

    public void updateStatus(StatusModel statusModel) {

        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().update(statusModel));
    }

    public void deleteStatus(StatusModel statusModel) {

        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).statusDao().delete(statusModel));
    }

    public void updateUserPrivacy(UsersPrivacyModel usersPrivacyModel) {

        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersPrivacyDao().update(usersPrivacyModel));
    }

    public void insertUserPrivacy(UsersPrivacyModel usersPrivacyModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersPrivacyDao().insert(usersPrivacyModel));
    }

    public void deleteUserPrivacy(UsersPrivacyModel usersPrivacyModel) {
        AppExecutors.getInstance().diskIO().execute(() -> AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersPrivacyDao().delete(usersPrivacyModel));

    }

    public List<UsersPrivacyModel> getAllUsersPrivacy() {

        try {
            return Observable.create((ObservableOnSubscribe<List<UsersPrivacyModel>>) subscriber -> {
                try {
                    List<UsersPrivacyModel> usersPrivacyModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersPrivacyDao().loadAllUsersPrivacy();
                    if (usersPrivacyModels != null)
                        subscriber.onNext(usersPrivacyModels);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Boolean checkIfPrivacyUserExist(String userId) {

        return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
            try {

                subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).usersPrivacyDao().userPrivacyExistence(userId) != 0);
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

    }

}