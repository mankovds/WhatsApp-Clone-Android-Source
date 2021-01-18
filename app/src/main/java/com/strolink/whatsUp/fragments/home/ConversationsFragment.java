package com.strolink.whatsUp.fragments.home;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.adapters.recyclerView.messages.ConversationsAdapter;
import com.strolink.whatsUp.app.AppConstants;

import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.messages.ConversationsPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_ACTION_MODE_FINISHED;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_UPDATED;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;
import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS;
import static com.strolink.whatsUp.app.AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW;

/**
 * Created by Abderrahim El imame  on 20/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsFragment extends Fragment implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback {

    @BindView(R.id.ConversationsList)
    RecyclerView ConversationList;
    @BindView(R.id.empty)
    LinearLayout emptyConversations;

    @BindView(R.id.swipeConversations)
    SwipeRefreshLayout mSwipeRefreshLayout;


    public ConversationsAdapter mConversationsAdapter;
    private ConversationsPresenter mConversationsPresenter;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private PreCachingLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, mView);
        mConversationsPresenter = new ConversationsPresenter(this);
        initializerView();
        mConversationsPresenter.onCreate();
        return mView;
    }



    /**
     * method to initialize the view
     */
    private void initializerView() {
        setHasOptionsMenu(true);
        layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(getActivity()));//fix preload image before appears
        mConversationsAdapter = new ConversationsAdapter(ConversationList, GlideApp.with(this));

        mConversationsAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        ConversationList.setLayoutManager(layoutManager);
        ConversationList.setAdapter(mConversationsAdapter);/*
        ConversationList.setItemAnimator(new DefaultItemAnimator());
        ConversationList.getItemAnimator().setChangeDuration(0);*/
        ((SimpleItemAnimator) ConversationList.getItemAnimator()).setSupportsChangeAnimations(false);
        //fix slow recyclerview start
        ConversationList.setHasFixedSize(true);
        ConversationList.setItemViewCacheSize(30);
        ConversationList.setDrawingCacheEnabled(true);
        ConversationList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        ConversationList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewBenOnGestureListener());
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {

            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(getContext())));
            mConversationsPresenter.onRefresh();
        });

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }


    /**
     * method to toggle the selection
     *
     * @param position
     */
    private void ToggleSelection(int position) {
        mConversationsAdapter.toggleSelection(position);
        String title = String.format(" " + getString(R.string.selected_items), mConversationsAdapter.getSelectedItemCount());
        actionMode.setTitle(title);


    }

    @Override
    public void onResume() {
        super.onResume();
        mConversationsPresenter.onResume();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.select_conversation_menu, menu);
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_STARTED));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


        switch (item.getItemId()) {
            // TODO: 11/7/18 nzid group actions
            case R.id.delete_conversations:

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage(R.string.alert_message_delete_conversation);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    int arraySize = mConversationsAdapter.getSelectedItems().size();

                    AppHelper.LogCat("start delete " + arraySize);

                    if (arraySize != 0) {
                        AppHelper.showDialog(getActivity(), getString(R.string.deleting_chat));
                        for (int x = 0; x < arraySize; x++) {
                            int currentPosition = mConversationsAdapter.getSelectedItems().get(x);
                            try {
                                ConversationModel conversationsModel = mConversationsAdapter.getItem(currentPosition);
                                mConversationsPresenter.deleteConversation(conversationsModel.get_id(), currentPosition);
                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                            }
                        }
                        AppHelper.LogCat("finish delete");
                        AppHelper.hideDialog();
                    } else {
                        AppHelper.CustomToast(getActivity(), "Delete conversation failed  ");
                    }
                    if (actionMode != null) {
                        mConversationsAdapter.clearSelections();
                        actionMode.finish();
                    }

                });


                builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builder.show();
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        mConversationsAdapter.clearSelections();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_DESTROYED));
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = ConversationList.getChildAdapterPosition(view);
            try {
                ConversationModel conversationModel = mConversationsAdapter.getItem(currentPosition);

                if (actionMode != null) {
                    if (!conversationModel.isIs_group())
                        ToggleSelection(currentPosition);
                    boolean hasCheckedItems = mConversationsAdapter.getSelectedItems().size() > 0;//Check if any items are already selected or not
                    if (!hasCheckedItems && actionMode != null) {
                        // there no selected items, finish the actionMode
                        actionMode.finish();
                    }

                }

            } catch (Exception ex) {
                AppHelper.LogCat(" onSingleTapConfirmed " + ex.getMessage());
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            try {

                View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = ConversationList.getChildAdapterPosition(view);
                if (actionMode != null) {
                    return;
                }
                ConversationModel conversationModel = mConversationsAdapter.getItem(currentPosition);

                if (!conversationModel.isIs_group()) {

                    actionMode = getActivity().startActionMode(ConversationsFragment.this);
                    if (actionMode != null) {
                        ToggleSelection(currentPosition);

                    }
                }
                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }


        }


    }

    /**
     * method to show conversation list
     *
     * @param conversationsModels this is parameter for  ShowConversation  method
     */
    public void UpdateConversation(List<ConversationModel> conversationsModels) {

        if (conversationsModels.size() != 0) {
            mConversationsAdapter.setConversations(conversationsModels);

            ConversationList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
        } else {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConversationsPresenter != null)
            mConversationsPresenter.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onShowLoading() {
        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public void onHideLoading() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable);
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * method to add a new message to list messages
     *
     * @param conversationId this is the parameter for addConversationEventMainThread
     */

    private void addConversationEventMainThread(String conversationId) {
        mConversationsAdapter.addConversationItem(conversationId);
        ConversationList.setVisibility(View.VISIBLE);
        emptyConversations.setVisibility(View.GONE);
        ConversationList.scrollToPosition(0);
    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        mConversationsAdapter.setString(string);

            List<ConversationModel> filteredModelList;
            filteredModelList = FilterList(string);
            AppHelper.LogCat("filteredModelList "+filteredModelList.size());
            if (filteredModelList.size() != 0) {

                    mConversationsAdapter.animateTo(filteredModelList);
                    layoutManager.scrollToPositionWithOffset(0, 0);

            }

    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<ConversationModel> FilterList(String query) {
        return MessagesController.getInstance().loadAllChatsQuery(query);


    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        String messageId = pusher.getMessageId();
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_SEARCH_QUERY_CHAT:

                // filter recycler view when query submitted
                Search(pusher.getData());
                break;
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW:
                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(() -> addConversationEventMainThread(pusher.getConversationId()), 500);
                });

                break;
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW:
                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(() -> mConversationsAdapter.updateConversationItem(pusher.getConversationId()), 500);
                });

                break;

            case EVENT_BUS_MESSAGE_IS_READ:
            case EVENT_UPDATE_CONVERSATION_OLD_ROW:
            case EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS:

                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(() -> mConversationsAdapter.updateStatusConversationItem(pusher.getConversationId()), 500);
                });

                break;
            case EVENT_BUS_DELETE_CONVERSATION_ITEM:
                mConversationsAdapter.DeleteConversationItem(pusher.getConversationId());
                showEmptyView();
                break;
            case EVENT_BUS_IMAGE_GROUP_UPDATED:


                    mConversationsAdapter.updateStatusConversationItem(MessagesController.getInstance().getChatIdByGroupId(pusher.getGroupID()));


                break;

            case EVENT_BUS_IMAGE_PROFILE_UPDATED:


                    mConversationsAdapter.updateStatusConversationItem(MessagesController.getInstance().getChatIdByUserId(pusher.getOwnerID()));


                break;
            case EVENT_BUS_ACTION_MODE_FINISHED:
                if (actionMode != null) {
                    mConversationsAdapter.clearSelections();
                    actionMode.finish();
                }
                break;


            case AppConstants.EVENT_BUS_MEMBER_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_TYPING, pusher.getSenderID(), pusher.getGroupID(), true);

                break;

            case AppConstants.EVENT_BUS_MEMBER_STOP_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, pusher.getSenderID(), pusher.getGroupID(), true);
                break;
            case AppConstants.EVENT_BUS_USER_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_TYPING, pusher.getSenderID(), null, false);

                break;

            case AppConstants.EVENT_BUS_USER_STOP_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, pusher.getSenderID(), null, false);
                break;

           /* case AppConstants.EVENT_BUS_UPDATE_USER_STATE:
                if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_ONLINE)) {
                    mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_CONNECTED, pusher.getSenderID(), null, false);
                } else if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_OFFLINE)) {
                    mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, pusher.getSenderID(), null, false);
                }
                break;*/

        }
    }


    private void showEmptyView() {
        if (mConversationsAdapter.getItemCount() == 0) {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }

}
