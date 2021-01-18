package com.strolink.whatsUp.presenters.messages;

import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.fragments.home.ConversationsFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.notifications.NotificationsManager;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsPresenter implements Presenter {
    private final ConversationsFragment conversationsFragmentView;

    private CompositeDisposable mDisposable;
    private int currentPage = 1;

    public ConversationsPresenter(ConversationsFragment conversationsFragment) {
        this.conversationsFragmentView = conversationsFragment;


    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(conversationsFragmentView))
            EventBus.getDefault().register(conversationsFragmentView);

        mDisposable = new CompositeDisposable();
        loadData(false);


    }


    private void loadData(boolean isRefresh) {
        if (isRefresh)
            conversationsFragmentView.onShowLoading();
        //getConversationFromLocal(isRefresh);
        getConversationFromServer();
    }


    private void getConversationFromServer() {


        mDisposable.add(APIHelper.initializeConversationsService()
                .getConversations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conversationsModels -> {


                    conversationsFragmentView.onHideLoading();
                    AppHelper.LogCat("conversationsModels " + conversationsModels.toString());

                    conversationsFragmentView.UpdateConversation(conversationsModels);

                }, throwable -> {
                    AppHelper.LogCat("throwable " + throwable.getMessage());

                    conversationsFragmentView.onErrorLoading(throwable);
                    conversationsFragmentView.onHideLoading();

                }));


    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
        //  mViewModel.refreshConversations();

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(conversationsFragmentView);
        if (mDisposable != null) {
            mDisposable.dispose();
        }

    }


    @Override
    public void onLoadMore() {
/*
        if (loadLastItem() != 0) {
            setCurrentPage(loadLastItem());
        }*/

        //loadData(false);
    }

    @Override
    public void onRefresh() {
        setCurrentPage(1);
        loadData(true);

    }

    @Override
    public void onStop() {

    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void deleteConversation(String conversationID, int currentPosition) {
        List<MessageModel> messagesModelWaiting = MessagesController.getInstance().loadMessagesByChatId(conversationID);


        List<MessageModel> messagesModelAll = MessagesController.getInstance().loadMessagesByChatId(conversationID, AppConstants.IS_WAITING);

        conversationsFragmentView.mConversationsAdapter.removeConversationItem(currentPosition);
        if (messagesModelWaiting.size() == messagesModelAll.size()) {


            List<MessageModel> messagesModel1 = MessagesController.getInstance().loadMessagesByChatId(conversationID);

            for (MessageModel messageModel : messagesModel1)
                MessagesController.getInstance().deleteMessage(messageModel);


            ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);
            MessagesController.getInstance().deleteChat(conversationsModel1);

            AppHelper.LogCat("Conversation deleted successfully ConversationsFragment");
            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
            NotificationsManager.getInstance().SetupBadger(conversationsFragmentView.getActivity());


        } else {
            mDisposable.add(APIHelper.initialApiUsersContacts().deleteConversation(conversationID).subscribe(statusResponse -> {


                List<MessageModel> messagesModel1 = MessagesController.getInstance().loadMessagesByChatId(conversationID);
                for (MessageModel messageModel : messagesModel1)
                    MessagesController.getInstance().deleteMessage(messageModel);

                ConversationModel conversationsModel1 = MessagesController.getInstance().getChatById(conversationID);
                MessagesController.getInstance().deleteChat(conversationsModel1);


                AppHelper.LogCat("Conversation deleted successfully ConversationsFragment");
                EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                NotificationsManager.getInstance().SetupBadger(conversationsFragmentView.getActivity());
            }, throwable -> {
                AppHelper.LogCat("Delete message failed ConversationsFragment" + throwable.getMessage());
            }));
        }

    }
}
