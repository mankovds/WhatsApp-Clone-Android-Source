package com.strolink.whatsUp.presenters.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.strolink.whatsUp.activities.call.CallAlertActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.database.AppExecutors;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.CustomNullException;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.call.CallingApi;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.jobs.mqtt.MqttClientManager;
import com.strolink.whatsUp.models.calls.CallSaverModel;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class CallsController {

    private static volatile CallsController Instance = null;
    private boolean isSaved = false;

    public CallsController() {
    }

    public static CallsController getInstance() {

        CallsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (CallsController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new CallsController();
                }
            }
        }
        return localInstance;

    }


    public CallsInfoModel getCallInfoByFromToId(String fromId, String toId) {
        try {
            return Observable.create((ObservableOnSubscribe<CallsInfoModel>) subscriber -> {
                try {
                    CallsInfoModel callsInfoModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadSingleCallInfoByFromToId(fromId, toId);
                    if (callsInfoModel != null)
                        subscriber.onNext(callsInfoModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return null;
        }
    }

    public CallsInfoModel getCallInfoByCallId(String id) {
        try {
            return Observable.create((ObservableOnSubscribe<CallsInfoModel>) subscriber -> {
                try {
                    CallsInfoModel callsInfoModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadSingleCallInfoByCallId(id);
                    if (callsInfoModel != null)
                        subscriber.onNext(callsInfoModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return null;
        }
    }

    public CallsInfoModel getCallInfoById(String id) {
        try {
            return Observable.create((ObservableOnSubscribe<CallsInfoModel>) subscriber -> {
                try {
                    CallsInfoModel callsInfoModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadSingleCallInfoById(id);
                    if (callsInfoModel != null)
                        subscriber.onNext(callsInfoModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return null;
        }
    }

    public CallsModel getCallById(String callId) {

        try {
            return Observable.create((ObservableOnSubscribe<CallsModel>) subscriber -> {
                try {
                    CallsModel callsModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadSingleCallById(callId);
                    if (callsModel != null)
                        subscriber.onNext(callsModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return null;
        }
    }

    public List<CallsModel> loadAllCallQuery(String query) {

        try {
            return Observable.create((ObservableOnSubscribe<List<CallsModel>>) subscriber -> {
                try {
                    List<CallsModel> callsModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadAllCallQuery(query);
                    if (callsModel != null)
                        subscriber.onNext(callsModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return new ArrayList<>();
        }

    }

    public List<CallsModel> loadAllCalls() {

        try {
            return Observable.create((ObservableOnSubscribe<List<CallsModel>>) subscriber -> {
                try {
                    List<CallsModel> callsModel = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadAllUserCalls();
                    if (callsModel != null)
                        subscriber.onNext(callsModel);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CallsInfoModel> loadAllCallsInfo(String callId) {

        try {
            return Observable.create((ObservableOnSubscribe<List<CallsInfoModel>>) subscriber -> {
                try {
                    List<CallsInfoModel> callsInfoModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadAllUserCallsInfo(callId);
                    if (callsInfoModels != null)
                        subscriber.onNext(callsInfoModels);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return new ArrayList<>();
        }


    }

    public List<CallsInfoModel> loadAllCallInfoByCallId(String callId) {

        try {
            return Observable.create((ObservableOnSubscribe<List<CallsInfoModel>>) subscriber -> {
                try {
                    List<CallsInfoModel> callsInfoModels = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().loadAllCallInfoByCallId(callId);
                    if (callsInfoModels != null)
                        subscriber.onNext(callsInfoModels);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));

                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat(" Exception " + e.getMessage());
            return new ArrayList<>();
        }

    }


    public boolean checkIfSingleCallExist(String callId) {
        try {

            return Observable.create((ObservableOnSubscribe<Boolean>) subscriber -> {
                try {

                    subscriber.onNext(AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().callInfoExistence(callId) != 0);
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            return false;
        }
    }


    public void deleteCall(CallsModel callsModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().delete(callsModel);
        });
    }

    public void insertCall(CallsModel callsModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().insert(callsModel);
        });
    }

    public void updateCall(CallsModel callsModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().update(callsModel);
        });
    }

    public void insertCallInfo(CallsInfoModel callsInfoModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().insert(callsInfoModel);
        });
    }

    public void updateCallInfo(CallsInfoModel callsInfoModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().update(callsInfoModel);
        });
    }

    public void deleteCallInfo(CallsInfoModel callsInfoModel) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsInfoDao().delete(callsInfoModel);
        });
    }


    public void updateSeenCallStatus(String callId) {

        CallsInfoModel callsInfoModel1 = getCallInfoById(callId);
        if (callsInfoModel1 != null) {
            callsInfoModel1.setStatus(AppConstants.IS_SEEN);
            updateCallInfo(callsInfoModel1);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callId));
        }

    }


    public void sendUserCallToServer(Context context, CallsInfoModel callsInfoModel, String isUpdate) {


        AppHelper.LogCat("Job emit call to. callId : " + callsInfoModel.getCallId());
        //  CallsInfoModel callsInfoModel = getCallInfoById(callId);
        if (callsInfoModel.getStatus() != AppConstants.IS_SEEN) {


            CallSaverModel callSaverModel = new CallSaverModel();
            callSaverModel.setFromId(callsInfoModel.getFrom());
            callSaverModel.setToId(callsInfoModel.getTo());
            callSaverModel.setDate(callsInfoModel.getDate());
            callSaverModel.setDuration(callsInfoModel.getDuration());
            callSaverModel.setIsVideo(callsInfoModel.getType());
            if (isUpdate.equals("true")) {
                callSaverModel.setCallId(callsInfoModel.getFrom());
            }
            callSaverModel.setUpdate(isUpdate);

            APIHelper.initialApiUsersContacts().saveNewCall(callSaverModel).subscribe(response -> {

                if (response.isSuccess()) {
                    if (isUpdate.equals("false"))
                        CallsController.getInstance().makeCallAsSent(context, callsInfoModel.getCallId(), response.getCallId());
                } else {
                    AppHelper.LogCat("response " + response.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat("throwable " + throwable.getMessage());
            });

        } else {

            AppHelper.LogCat(" failed ");
        }


    }


    /**
     * method to make call as sent
     */
    private void makeCallAsSent(Context context, String infoId, String callId) {
        AppHelper.LogCat("heheheh ");


        try {

            CallsInfoModel callsInfoModel1 = getCallInfoByCallId(infoId);
            callsInfoModel1.setStatus(AppConstants.IS_SENT);
            callsInfoModel1.setCi_id(callId);
            updateCallInfo(callsInfoModel1);


            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callsInfoModel1.getTo()));
            JSONObject updateMessage = new JSONObject();


            UsersModel owner = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));


            try {


                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", owner.getUsername());
                jsonObject.put("phone", owner.getPhone());

                // updateMessage.put("callId", callsInfoModel.getTo());
                updateMessage.put("callId", callId);
                updateMessage.put("call_from", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                updateMessage.put("callType", callsInfoModel1.getType());
                updateMessage.put("status", "init_call");
                updateMessage.put("date", callsInfoModel1.getDate());
                updateMessage.put("owner", jsonObject);
                updateMessage.put("recipientId", callsInfoModel1.getTo());
                updateMessage.put("missed", false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            //emit by mqtt to other user

            try {
                CallingApi.startCall(context, callsInfoModel1.getType(), false, callsInfoModel1.getTo(), callId);
                WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(callsInfoModel1.getTo(), updateMessage,"call");
            } catch (MqttException | JSONException e) {
                e.printStackTrace();

                WhatsCloneApplication.getInstance().startActivity(new Intent(WhatsCloneApplication.getInstance(), CallAlertActivity.class));
            }


        } catch (Exception e) {
            AppHelper.LogCat(" Is sent calls  Error" + e.getMessage());
        }


    }


    public static String randomString() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        sb.append("PnPLabs3Embed");
        return sb.toString();
    }

    /**
     * method to update status as seen by sender (if recipient have been seen the call)  in realm database
     */
    public void updateSeenStatus(MqttClientManager mqttClientManager, MqttAndroidClient mqttAndroidClient, String callId, String recipientId) {
        //   ArrayList<String> usersList = new ArrayList<String>();


        if (checkIfSingleCallExist(callId)) {
            AppHelper.LogCat("Seen callId " + callId);


            CallsInfoModel callsInfoModel = getCallInfoById(callId);

            if (callsInfoModel != null) {
                callsInfoModel.setStatus(AppConstants.IS_SEEN);

                updateCallInfo(callsInfoModel);
                AppHelper.LogCat("Seen successfully");


                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("callId", callId);
                    updateMessage.put("recipientId", recipientId);
                    try {
                        WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED, updateMessage,"call");
                    } catch (MqttException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                }


            } else {
                AppHelper.LogCat("Seen failed ");
            }


            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callId));


        } else {
            JSONObject updateMessage = new JSONObject();
            try {
                updateMessage.put("callId", callId);
                try {


                    WhatsCloneApplication.getInstance().getMqttClientManager().publishCall(AppConstants.MqttConstants.UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage,"call");
                } catch (MqttException | JSONException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                // e.printStackTrace();
            }


        }

    }

    /*for update message status*/

    public void saveCallToLocalDB(Context context, String recipientId, String type) {
        boolean isVideoCall;
        isVideoCall = type.equals(AppConstants.VIDEO_CALL);
        DateTime current = new DateTime();
        String callTime = String.valueOf(current);


        String historyCallId = getHistoryCallId(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()), recipientId, isVideoCall);

        if (historyCallId == null) {

            UsersModel contactsModel1;

            contactsModel1 = UsersController.getInstance().getUserById(recipientId);
            String lastID = DbBackupRestore.getCallLastId();
            CallsModel callsModel = new CallsModel();
            callsModel.setC_id(lastID);
            callsModel.setType(type);

            callsModel.setUsersModel(contactsModel1);
            callsModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
            callsModel.setTo(contactsModel1.get_id());
            callsModel.setDuration("00:00");
            callsModel.setCounter(1);
            callsModel.setDate(callTime);
            callsModel.setReceived(false);


            String lastInfoID = DbBackupRestore.getCallInfoLastId();

            CallsInfoModel callsInfoModel = new CallsInfoModel();
            callsInfoModel.setCi_id(lastInfoID);
            callsInfoModel.setType(type);
            callsInfoModel.setStatus(AppConstants.IS_WAITING);
            callsInfoModel.setUsersModel(contactsModel1);


            callsInfoModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
            callsInfoModel.setCallId(lastID);
            callsInfoModel.setTo(contactsModel1.get_id());
            callsInfoModel.setDuration("00:00");
            callsInfoModel.setDate(callTime);
            callsInfoModel.setReceived(false);

            insertCallInfo(callsInfoModel);
            insertCall(callsModel);

            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, lastID));
            sendUserCallToServer(context, callsInfoModel, "false");

        } else {

            UsersModel contactsModel1;
            contactsModel1 = UsersController.getInstance().getUserById(recipientId);

            int callCounter;
            CallsModel callsModel = getCallById(historyCallId);

            callCounter = callsModel.getCounter();
            callCounter++;
            callsModel.setDate(callTime);
            callsModel.setCounter(callCounter);


            String lastInfoID = DbBackupRestore.getCallInfoLastId();

            CallsInfoModel callsInfoModel = new CallsInfoModel();
            callsInfoModel.setCi_id(lastInfoID);
            callsInfoModel.setType(type);
            callsInfoModel.setStatus(AppConstants.IS_WAITING);
            callsInfoModel.setUsersModel(contactsModel1);

            callsInfoModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
            callsInfoModel.setTo(contactsModel1.get_id());
            callsInfoModel.setCallId(callsModel.getC_id());
            callsInfoModel.setDuration("00:00");
            callsInfoModel.setDate(callTime);
            callsInfoModel.setReceived(false);


            insertCallInfo(callsInfoModel);
            updateCall(callsModel);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
            sendUserCallToServer(context, callsInfoModel, "false");

        }


    }

    @SuppressLint("CheckResult")
    public void saveToDataBase(String caller_id, String callId, boolean isVideoCall, DateTime date, String phone, String username) {

        try {


            String callTime = String.valueOf(date);

            String historyCallId = getHistoryCallIdReceived(caller_id, PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()), isVideoCall);

            if (historyCallId == null) {

                UsersModel usersModelSender = UsersController.getInstance().getUserById(caller_id);
                UsersModel usersModelSenderFinal;
                if (usersModelSender == null) {
                    UsersModel usersModelSenderNew = new UsersModel();
                    usersModelSenderNew.set_id(caller_id);
                    usersModelSenderNew.setPhone(phone);
                    if (!username.equals("null"))
                        usersModelSenderNew.setUsername(username);
                    usersModelSenderNew.setDisplayed_name(username);
                    usersModelSenderNew.setActivate(true);
                    usersModelSenderNew.setLinked(true);
                    usersModelSenderFinal = usersModelSenderNew;
                    UsersController.getInstance().insertUser(usersModelSenderNew);
                } else {
                    usersModelSenderFinal = usersModelSender;
                }
                String lastID = DbBackupRestore.getCallLastId();
                CallsModel callsModel = new CallsModel();
                callsModel.setC_id(lastID);
                if (isVideoCall)
                    callsModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsModel.setType(AppConstants.VOICE_CALL);
                callsModel.setUsersModel(usersModelSenderFinal);
                callsModel.setCounter(1);
                callsModel.setFrom(usersModelSenderFinal.get_id());
                callsModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                callsModel.setDuration("00:00");
                callsModel.setDate(callTime);
                callsModel.setReceived(true);

                CallsInfoModel callsInfoModel = new CallsInfoModel();

                callsInfoModel.setCi_id(callId);
                if (isVideoCall)
                    callsInfoModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsInfoModel.setType(AppConstants.VOICE_CALL);
                callsInfoModel.setUsersModel(usersModelSenderFinal);

                callsInfoModel.setStatus(AppConstants.IS_WAITING);

                callsInfoModel.setCallId(lastID);
                callsInfoModel.setFrom(usersModelSenderFinal.get_id());
                callsInfoModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                callsInfoModel.setDuration("00:00");
                callsInfoModel.setDate(callTime);
                callsInfoModel.setReceived(true);


                insertCallInfo(callsInfoModel);
                insertCall(callsModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, callId));

            } else {

                UsersModel usersModelSender = UsersController.getInstance().getUserById(caller_id);
                UsersModel usersModelSenderFinal;
                if (usersModelSender == null) {
                    UsersModel usersModelSenderNew = new UsersModel();
                    usersModelSenderNew.set_id(caller_id);
                    usersModelSenderNew.setPhone(phone);
                    if (!username.equals("null"))
                        usersModelSenderNew.setUsername(username);
                    usersModelSenderNew.setDisplayed_name(username);
                    usersModelSenderNew.setActivate(true);
                    usersModelSenderNew.setLinked(true);
                    usersModelSenderFinal = usersModelSenderNew;
                    UsersController.getInstance().insertUser(usersModelSenderNew);
                } else {
                    usersModelSenderFinal = usersModelSender;
                }
                int callCounter;
                CallsModel callsModel = getCallById(historyCallId);

                callCounter = callsModel.getCounter();
                callCounter++;
                callsModel.setDate(callTime);
                callsModel.setCounter(callCounter);
                callsModel.setDuration("00:00");
                CallsInfoModel callsInfoModel = new CallsInfoModel();


                callsInfoModel.setCi_id(callId);
                if (isVideoCall)
                    callsInfoModel.setType(AppConstants.VIDEO_CALL);
                else
                    callsInfoModel.setType(AppConstants.VOICE_CALL);
                callsInfoModel.setUsersModel(usersModelSenderFinal);

                callsInfoModel.setStatus(AppConstants.IS_WAITING);

                callsInfoModel.setCallId(callsModel.getC_id());
                callsInfoModel.setFrom(usersModelSenderFinal.get_id());
                callsInfoModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                callsInfoModel.setDuration("00:00");
                callsInfoModel.setDate(callTime);
                callsInfoModel.setReceived(true);

                insertCallInfo(callsInfoModel);
                updateCall(callsModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));

            }


        } finally {
            WorkJobsManager.getInstance().sendSeenCallToServer(callId);
        }


    }


    public String getHistoryCallIdReceived(String fromId, String toId, boolean isVideoCall) {
        String type;

        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;


        try {
            return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                try {


                    String id = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadSingleCallId(toId, fromId, type, 1);
                    if (id != null)
                        subscriber.onNext(id);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception MainService" + e.getMessage());
            return null;
        }
    }

    public String getHistoryCallId(String fromId, String toId, boolean isVideoCall) {
        String type;

        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;

        try {
            return Observable.create((ObservableOnSubscribe<String>) subscriber -> {
                try {
                    String id = AppDatabase.getInstance(WhatsCloneApplication.getInstance()).callsDao().loadSingleCallId(toId, fromId, type, 0);
                    if (id != null)
                        subscriber.onNext(id);
                    else
                        //  throw new CustomNullException("");
                        subscriber.onError(new CustomNullException("The value is Null"));
                    subscriber.onComplete();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }).subscribeOn(Schedulers.computation()).firstElement().blockingGet();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception MainService" + e.getMessage());
            return null;
        }
    }


}