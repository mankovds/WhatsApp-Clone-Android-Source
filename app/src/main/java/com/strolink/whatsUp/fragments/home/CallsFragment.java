package com.strolink.whatsUp.fragments.home;

import android.os.Bundle;
import android.os.Handler;
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
import com.strolink.whatsUp.adapters.recyclerView.calls.CallsAdapter;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.calls.CallsPresenter;
import com.strolink.whatsUp.presenters.controllers.CallsController;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallsFragment extends Fragment implements LoadingData {

    @BindView(R.id.CallsList)
    RecyclerView CallsList;
    @BindView(R.id.empty)
    LinearLayout emptyConversations;

    @BindView(R.id.swipeCalls)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private CallsAdapter mCallsAdapter;
    private CallsPresenter mCallsPresenter;
    private PreCachingLayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_calls, container, false);
        ButterKnife.bind(this, mView);
        mCallsPresenter = new CallsPresenter(this);
        initializerView();
        mCallsPresenter.onCreate();

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
        mCallsAdapter = new CallsAdapter(CallsList);
        CallsList.setLayoutManager(layoutManager);

        mCallsAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        CallsList.setAdapter(mCallsAdapter);
        CallsList.setItemAnimator(new DefaultItemAnimator());
        CallsList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        CallsList.setHasFixedSize(true);
        CallsList.setItemViewCacheSize(10);
        CallsList.setDrawingCacheEnabled(true);
        CallsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> mCallsPresenter.onRefresh());

    }


    /**
     * method to show calls list
     *
     * @param callsModelList this is parameter for  UpdateCalls  method
     */
    public void UpdateCalls(List<CallsModel> callsModelList) {

        if (callsModelList.size() != 0) {
            CallsList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
            mCallsAdapter.setCalls(callsModelList);
        } else {
            CallsList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        mCallsAdapter.setString(string);
        List<CallsModel> filteredModelList;
        filteredModelList = FilterList(string);
        AppHelper.LogCat("filteredModelList " + filteredModelList.size());
        if (filteredModelList.size() != 0) {
            mCallsAdapter.animateTo(filteredModelList);
            layoutManager.scrollToPositionWithOffset(0, 0);

        }

    }


    /**
     * method to filter the list
     *
     * @param query this is parameter for FilterList method
     * @return this what method will return
     */
    private List<CallsModel> FilterList(String query) {
        return CallsController.getInstance().loadAllCallQuery(query);


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

            case AppConstants.EVENT_BUS_SEARCH_QUERY_CALLS:
                Search(pusher.getData());
                break;
            case AppConstants.EVENT_BUS_CALL_NEW_ROW:
                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(() -> addCallEventMainThread(pusher.getCallId()), 500);
                });

                break;
            case AppConstants.EVENT_UPDATE_CALL_OLD_ROW:
                AppHelper.runOnUIThread(() -> {
                    new Handler().postDelayed(() -> mCallsAdapter.updateCallItem(pusher.getCallId()), 500);
                });

                break;
            case AppConstants.EVENT_BUS_DELETE_CALL_ITEM:
                if (pusher.getCallId() != null && !pusher.getCallId().equals(""))
                    mCallsAdapter.DeleteCallItem(pusher.getCallId());
                else
                    mCallsPresenter.onRefresh();
                break;
        }
    }

    /**
     * method to add a new call to list calls
     *
     * @param callId this is the parameter for addCallEventMainThread
     */

    private void addCallEventMainThread(String callId) {
        mCallsAdapter.addCallItem(callId);
        CallsList.setVisibility(View.VISIBLE);
        emptyConversations.setVisibility(View.GONE);
        CallsList.scrollToPosition(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCallsPresenter != null)
            mCallsPresenter.onDestroy();
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

}
