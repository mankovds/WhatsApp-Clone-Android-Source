package com.strolink.whatsUp.activities.groups;

import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.groups.AddMembersToGroupAdapter;
import com.strolink.whatsUp.adapters.recyclerView.groups.AddMembersToGroupSelectorAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.models.groups.MembersModelJson;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.groups.AddMembersToGroupPresenter;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupActivity extends BaseActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.ParentLayoutAddContact)
    RelativeLayout ParentLayoutAddContact;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.ContactsListHeader)
    RecyclerView ContactsListHeader;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;


    private AddMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private AddMembersToGroupSelectorAdapter mAddMembersToGroupSelectorAdapter;
    private GestureDetectorCompat gestureDetector;
    private AddMembersToGroupPresenter mAddMembersToGroupPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);
        ButterKnife.bind(this);

        initializeView();
        setupToolbar();
        EventBus.getDefault().register(this);
        PreferenceManager.getInstance().clearMembers(this);
        mAddMembersToGroupPresenter = new AddMembersToGroupPresenter(this);
        mAddMembersToGroupPresenter.onCreate();
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddMembersToGroupAdapter(this);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        floatingActionButton.setOnClickListener(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ContactsListHeader.setLayoutManager(linearLayoutManager);
        mAddMembersToGroupSelectorAdapter = new AddMembersToGroupSelectorAdapter(this);
        ContactsListHeader.setAdapter(mAddMembersToGroupSelectorAdapter);
    }

    /**
     * method to show contacts
     *
     * @param contactsModels this  parameter of ShowContacts method
     */
    public void ShowContacts(List<UsersModel> contactsModels) {
        mAddMembersToGroupListAdapter.setContacts(contactsModels);

    }


    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);
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
     * @param position this is parameter of ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mAddMembersToGroupListAdapter.toggleSelection(position);
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);

    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.container_list_item) {

                int position = ContactsList.getChildAdapterPosition(v);
                ToggleSelection(position);


            } else if (v.getId() == R.id.fab) {
                if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                    if (mAddMembersToGroupListAdapter.getSelectedItemCount() > AppConstants.MEMBER_GROUP_LIMIT) {
                        AppHelper.Snackbar(this, ParentLayoutAddContact, String.format(getString(R.string.you_ve_reached_the_limit), AppConstants.MEMBER_GROUP_LIMIT), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                    } else {
                        int arraySize = mAddMembersToGroupListAdapter.getSelectedItems().size();
                        for (int x = 0; x < arraySize; x++) {
                            int position = mAddMembersToGroupListAdapter.getSelectedItems().get(x);
                            MembersModelJson membersGroupModel = new MembersModelJson();
                            membersGroupModel.setUserId(mAddMembersToGroupListAdapter.getContacts().get(position).get_id());
                            membersGroupModel.setAdmin(false);
                            membersGroupModel.setLeft(false);
                            membersGroupModel.setDeleted(false);
                            PreferenceManager.getInstance().addMember(this, membersGroupModel);
                        }
                        AppHelper.LaunchActivity(this, CreateGroupActivity.class);
                        finish();
                    }
                } else {
                    AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.select_one_at_least), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }


            }
        } catch (Exception e) {
            AppHelper.LogCat(" Touch Exception AddMembersToGroupActivity " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ContactsList.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

    }

    /**
     * method to scroll to the bottom of recyclerView
     */
    private void scrollToBottom() {
        ContactsListHeader.scrollToPosition(mAddMembersToGroupSelectorAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                mAddMembersToGroupListAdapter.clearSelections();
            }
            PreferenceManager.getInstance().clearMembers(this);
            finish();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();


        AnimationsUtil.setTransitionAnimation(this);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_REMOVE_CREATE_MEMBER:
                mAddMembersToGroupSelectorAdapter.remove(pusher.getContactsModel());
                if (mAddMembersToGroupSelectorAdapter.getContacts().size() == 0) {
                    ContactsListHeader.setVisibility(View.GONE);
                }
                break;
            case AppConstants.EVENT_BUS_ADD_CREATE_MEMBER:
                ContactsListHeader.setVisibility(View.VISIBLE);
                mAddMembersToGroupSelectorAdapter.add(pusher.getContactsModel());
                scrollToBottom();
                break;
            case AppConstants.EVENT_BUS_DELETE_CREATE_MEMBER:
                int position = mAddMembersToGroupListAdapter.getItemPosition(pusher.getContactsModel());
                ToggleSelection(position);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
            mAddMembersToGroupListAdapter.clearSelections();
        }
        PreferenceManager.getInstance().clearMembers(this);

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }


}
