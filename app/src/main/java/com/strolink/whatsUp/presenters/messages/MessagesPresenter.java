package com.strolink.whatsUp.presenters.messages;


import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesPresenter implements Presenter {
    private final MessagesActivity messagesActivity;


    private CompositeDisposable compositeDisposable;


    public MessagesPresenter(MessagesActivity messagesActivity) {
        this.messagesActivity = messagesActivity;


    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(messagesActivity))
            EventBus.getDefault().register(messagesActivity);


        AppHelper.LogCat("oncreate increment ");
        compositeDisposable = new CompositeDisposable();

        getContactSenderLocal();
        if (messagesActivity.isGroup()) {
            getGroupLocal();
        } else {


            getContactRecipientLocal();
        }

        getData();


    }


    public void getData() {

        if (messagesActivity.isGroup()) {
            loadLocalGroupData();
        } else {
            loadLocalData();
        }
    }

    private void getGroupLocal() {


        compositeDisposable.add(APIHelper.initializeApiGroups()
                .getGroupInfo(messagesActivity.getGroupID(), compositeDisposable)
                .subscribe(groupModel -> {
                    AppHelper.LogCat("groupModel " + groupModel.toString());

                    List<MembersModel> membersModels = UsersController.getInstance().loadAllGroupMembers(messagesActivity.getGroupID());
                    AppHelper.runOnUIThread(() -> {
                        messagesActivity.updateGroupInfo(groupModel, membersModels);
                    });

                }, throwable -> {
                    AppHelper.LogCat("groupModel " + throwable.getMessage());

                }));

    }


    public void getContactRecipientLocal() {

        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(messagesActivity.getRecipientId(), compositeDisposable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString());
                    messagesActivity.updateContactRecipient(usersModel);

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));


    }


    private void getContactSenderLocal() {

        compositeDisposable.add(APIHelper.initialApiUsersContacts()
                .getUserInfo(PreferenceManager.getInstance().getID(messagesActivity), compositeDisposable)
                .subscribe(usersModel -> {
                    AppHelper.LogCat("usersModel " + usersModel.toString());

                }, throwable -> {
                    AppHelper.LogCat("usersModel " + throwable.getMessage());

                }));

    }


    private void loadLocalGroupData() {
        if (NotificationsManager.getInstance().getManager())
            NotificationsManager.getInstance().cancelNotification(messagesActivity.getGroupID());

        compositeDisposable.add(APIHelper.initializeMessagesService()
                .getGroupChat(messagesActivity.getGroupID())
                //  .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageModels -> {
                    AppHelper.LogCat("messageModels " + messageModels.toString());
                    AppHelper.LogCat("messageModels " + messageModels.size());
                    messagesActivity.onHideLoading();
                    messagesActivity.ShowMessages(messageModels);

                }, throwable -> {
                    AppHelper.LogCat("throwable " + throwable.getMessage());

                    messagesActivity.onErrorLoading(throwable);
                    messagesActivity.onHideLoading();

                }));


    }

    private void loadLocalData() {
        if (NotificationsManager.getInstance().getManager())
            NotificationsManager.getInstance().cancelNotification(messagesActivity.getRecipientId());


        compositeDisposable.add(APIHelper.initializeMessagesService()
                .getUserChat(messagesActivity.getConversationID())
                //  .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messageModels -> {

                    AppHelper.LogCat("messageModels " + messageModels.size());
                    messagesActivity.onHideLoading();
                    messagesActivity.ShowMessages(messageModels);

                }, throwable -> {
                    AppHelper.LogCat("throwable " + throwable.getMessage());
                    throwable.printStackTrace();

                    messagesActivity.onErrorLoading(throwable);
                    messagesActivity.onHideLoading();

                }));

    }

    @Override
    public void onPause() {
        try {
            WhatsCloneApplication.getInstance().getMqttClientManager().publishUserStatus(AppConstants.MqttConstants.PUBLISH_USER_STATUS, true, true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        if (compositeDisposable != null && !compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(messagesActivity);

        if (compositeDisposable != null && !compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {
        getData();
    }

    @Override
    public void onStop() {

    }


    public void deleteConversation(String conversationID) {


        List<MessageModel> messagesModelWaiting = MessagesController.getInstance().loadMessagesByChatId(conversationID);
        List<MessageModel> messagesModelAll = MessagesController.getInstance().loadMessagesByChatId(conversationID, AppConstants.IS_WAITING);

        if (messagesModelWaiting.size() == messagesModelAll.size()) {


            List<MessageModel> messagesModel1 = MessagesController.getInstance().loadMessagesByChatId(conversationID);
            for (MessageModel messageModel : messagesModel1)
                MessagesController.getInstance().deleteMessage(messageModel);


            AppHelper.LogCat("Message Deleted  successfully  MessagesPopupActivity");

            List<MessageModel> messagesModel2 = MessagesController.getInstance().loadMessagesByChatId(conversationID);
            if (messagesModel2.size() == 0) {

                ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);
                MessagesController.getInstance().deleteChat(conversationsModel1);

                AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity");

                EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                NotificationsManager.getInstance().SetupBadger(messagesActivity);
                messagesActivity.finish();


            } else {
                MessageModel messagesModel = MessagesController.getInstance().getLastMessageById(conversationID);


                ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);

                conversationsModel1.setLatest_message_id(messagesModel.get_id());
                conversationsModel1.setLatest_message(messagesModel.getMessage());
                conversationsModel1.setFile_type(messagesModel.getFile_type());
                conversationsModel1.setLatest_message_latitude(messagesModel.getLatitude());
                conversationsModel1.setLatest_message_state(messagesModel.getState());
                conversationsModel1.setLatest_message_created(messagesModel.getCreated());
                conversationsModel1.setLatest_message_status(messagesModel.getStatus());
                conversationsModel1.setLatest_message_sender_id(messagesModel.getSenderId());
                conversationsModel1.setLatest_message_sender_phone(messagesModel.getSender_phone());

                String name = UtilsPhone.getContactName(messagesModel.getSender_phone());
                conversationsModel1.setLatest_message_sender__displayed_name(name);
                MessagesController.getInstance().updateChat(conversationsModel1);

                AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity ");
                EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                NotificationsManager.getInstance().SetupBadger(messagesActivity);
                messagesActivity.finish();

            }


        } else {
            compositeDisposable.add(APIHelper.initialApiUsersContacts().deleteConversation(conversationID).subscribe(statusResponse -> {


                List<MessageModel> messagesModel1 = MessagesController.getInstance().loadMessagesByChatId(conversationID);
                for (MessageModel messageModel : messagesModel1)
                    MessagesController.getInstance().deleteMessage(messageModel);

                List<MessageModel> messagesModel2 = MessagesController.getInstance().loadMessagesByChatId(conversationID);
                if (messagesModel2.size() == 0) {

                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);
                    MessagesController.getInstance().deleteChat(conversationsModel1);

                    AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity");

                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                    NotificationsManager.getInstance().SetupBadger(messagesActivity);
                    messagesActivity.finish();


                } else {
                    MessageModel messagesModel = MessagesController.getInstance().getLastMessageById(conversationID);


                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);

                    conversationsModel1.setLatest_message_id(messagesModel.get_id());
                    conversationsModel1.setLatest_message(messagesModel.getMessage());
                    conversationsModel1.setFile_type(messagesModel.getFile_type());
                    conversationsModel1.setLatest_message_latitude(messagesModel.getLatitude());
                    conversationsModel1.setLatest_message_state(messagesModel.getState());
                    conversationsModel1.setLatest_message_created(messagesModel.getCreated());
                    conversationsModel1.setLatest_message_status(messagesModel.getStatus());
                    conversationsModel1.setLatest_message_sender_id(messagesModel.getSenderId());
                    conversationsModel1.setLatest_message_sender_phone(messagesModel.getSender_phone());

                    String name = UtilsPhone.getContactName(messagesModel.getSender_phone());
                    conversationsModel1.setLatest_message_sender__displayed_name(name);
                    MessagesController.getInstance().updateChat(conversationsModel1);

                    AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity ");
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    NotificationsManager.getInstance().SetupBadger(messagesActivity);
                    messagesActivity.finish();
                }
                ;
            }, throwable -> {
                AppHelper.LogCat("Delete message failed MessagesPopupActivity" + throwable.getMessage());
            }));
        }

    }

    public void deleteMessage(MessageModel messagesModel, int currentPosition1) {

        String messageId = messagesModel.get_id();
        if (messagesModel.getState().equals(AppConstants.NORMAL_STATE)) {
            messagesActivity.mMessagesAdapter.removeMessageItem(currentPosition1);
            if (messagesModel.getStatus() == AppConstants.IS_WAITING) {


                MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                MessagesController.getInstance().deleteMessage(messagesModel1);

                AppHelper.LogCat("Message deleted successfully MessagesActivity ");

                List<MessageModel> messagesModel2 = MessagesController.getInstance().loadMessagesByChatId(messagesActivity.getConversationID());

                if (messagesModel2.size() == 0) {


                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(messagesActivity.getConversationID());
                    MessagesController.getInstance().deleteChat(conversationsModel1);

                    AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                    messagesActivity.finish();
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, messagesActivity.getConversationID()));
                    NotificationsManager.getInstance().SetupBadger(messagesActivity);

                } else {

                    MessageModel messagesModelLast = MessagesController.getInstance().getLastMessageById(messagesActivity.getConversationID());

                    ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(messagesActivity.getConversationID());

                    conversationsModel1.setLatest_message_id(messagesModelLast.get_id());
                    conversationsModel1.setLatest_message(messagesModelLast.getMessage());
                    conversationsModel1.setFile_type(messagesModelLast.getFile_type());
                    conversationsModel1.setLatest_message_latitude(messagesModelLast.getLatitude());
                    conversationsModel1.setLatest_message_state(messagesModelLast.getState());
                    conversationsModel1.setLatest_message_created(messagesModelLast.getCreated());
                    conversationsModel1.setLatest_message_status(messagesModelLast.getStatus());
                    conversationsModel1.setLatest_message_sender_id(messagesModelLast.getSenderId());
                    conversationsModel1.setLatest_message_sender_phone(messagesModelLast.getSender_phone());

                    String name = UtilsPhone.getContactName(messagesModelLast.getSender_phone());
                    conversationsModel1.setLatest_message_sender__displayed_name(name);

                    MessagesController.getInstance().updateChat(conversationsModel1);
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, messagesActivity.getConversationID()));
                    NotificationsManager.getInstance().SetupBadger(messagesActivity);
                }

            } else {
                compositeDisposable.add(APIHelper.initialApiUsersContacts().deleteMessage(messageId).subscribe(statusResponse -> {
                    if (statusResponse.isSuccess()) {
                        MessageModel messagesModel1 = MessagesController.getInstance().getMessageById(messageId);
                        MessagesController.getInstance().deleteMessage(messagesModel1);

                        AppHelper.LogCat("Message deleted successfully MessagesActivity ");

                        List<MessageModel> messagesModel2 = MessagesController.getInstance().loadMessagesByChatId(messagesActivity.getConversationID());

                        if (messagesModel2.size() == 0) {


                            ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(messagesActivity.getConversationID());
                            MessagesController.getInstance().deleteChat(conversationsModel1);

                            AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                            messagesActivity.finish();
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, messagesActivity.getConversationID()));
                            NotificationsManager.getInstance().SetupBadger(messagesActivity);

                        } else {
                            MessageModel messagesModelLast = MessagesController.getInstance().getLastMessageById(messagesActivity.getConversationID());


                            ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(messagesActivity.getConversationID());

                            conversationsModel1.setLatest_message_id(messagesModelLast.get_id());
                            conversationsModel1.setLatest_message(messagesModelLast.getMessage());
                            conversationsModel1.setFile_type(messagesModelLast.getFile_type());
                            conversationsModel1.setLatest_message_latitude(messagesModelLast.getLatitude());
                            conversationsModel1.setLatest_message_state(messagesModelLast.getState());
                            conversationsModel1.setLatest_message_created(messagesModelLast.getCreated());
                            conversationsModel1.setLatest_message_status(messagesModelLast.getStatus());
                            conversationsModel1.setLatest_message_sender_id(messagesModelLast.getSenderId());
                            conversationsModel1.setLatest_message_sender_phone(messagesModelLast.getSender_phone());

                            String name = UtilsPhone.getContactName(messagesModelLast.getSender_phone());
                            conversationsModel1.setLatest_message_sender__displayed_name(name);

                            MessagesController.getInstance().updateChat(conversationsModel1);

                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, messagesActivity.getConversationID()));
                            NotificationsManager.getInstance().SetupBadger(messagesActivity);
                        }

                    } else {
                        AppHelper.CustomToast(messagesActivity, messagesActivity.getString(R.string.oops_something));
                    }
                }, throwable -> {
                    AppHelper.LogCat("delete message failed  MessagesActivity" + throwable.getMessage());
                }));
            }
        }
    }
}

