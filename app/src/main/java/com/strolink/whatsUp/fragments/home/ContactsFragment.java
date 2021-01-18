package com.strolink.whatsUp.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.adapters.recyclerView.contacts.ContactsAdapter;
import com.strolink.whatsUp.app.AppConstants;

import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.users.ContactsPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsFragment extends Fragment implements LoadingData {

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;


    @BindView(R.id.swipeContacts)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ContactsAdapter mContactsAdapter;
    private ContactsPresenter mContactsPresenter;
    private boolean isRefreshing = false;

    private PreCachingLayoutManager mLinearLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
        initializerView();
        mContactsPresenter = new ContactsPresenter(this);
        mContactsPresenter.onCreate();

        return mView;
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {

        mLinearLayoutManager = new PreCachingLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mLinearLayoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(getActivity()));//fix preload image before appears
        mContactsAdapter = new ContactsAdapter(true);
        setHasOptionsMenu(true);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mContactsAdapter);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(10);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end

        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {

            if (!isRefreshing) {
                isRefreshing = true;
                mContactsPresenter.onRefresh();
            }
        });
    }


    /**
     * method to update contacts
     *
     * @param contactsModelList this is parameter for  updateContacts method
     */
    public void updateContacts(List<UsersModel> contactsModelList) {

        mContactsAdapter.setContacts(contactsModelList);

        if (contactsModelList.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);

        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
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

            case AppConstants.EVENT_BUS_SEARCH_QUERY_CONTACTS:

                // filter recycler view when query submitted
                Search(pusher.getData());
                break;
        }
    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        mContactsAdapter.setString(string);
            List<UsersModel> filteredModelList;
            filteredModelList = FilterList(string);
            if (filteredModelList.size() != 0) {

                    mContactsAdapter.animateTo(filteredModelList);
                    mLinearLayoutManager.scrollToPositionWithOffset(0, 0);

            }

    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<UsersModel> FilterList(String query) {
        return UsersController.getInstance().loadAllUsersQuery(PreferenceManager.getInstance().getID(getActivity()), query);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mContactsPresenter.onDestroy();

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
        isRefreshing = false;
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());
        isRefreshing = false;
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

}