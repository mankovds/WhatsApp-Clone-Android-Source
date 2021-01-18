package com.strolink.whatsUp.activities.stories;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.stories.StoriesListAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.stories.StoriesPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 7/10/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesListActivity extends BaseActivity implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback {

    @BindView(R.id.storiesList)
    RecyclerView storiesList;

    @BindView(R.id.app_bar)
    Toolbar toolbar;


    public StoriesListAdapter storiesListAdapter;
    private StoriesPresenter storiesPresenter;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private boolean actionModeStarted = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories_list);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initializerView();

        storiesPresenter = new StoriesPresenter(this);
        storiesPresenter.onCreate();


    }


    /**
     * method to initialize the view
     */
    private void initializerView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.my_story));

        }
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        storiesListAdapter = new StoriesListAdapter(this, storiesList);
        storiesList.setLayoutManager(layoutManager);

        storiesListAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        storiesList.setAdapter(storiesListAdapter);

        //fix slow recyclerview start
        storiesList.setHasFixedSize(true);
        storiesList.setItemViewCacheSize(30);
        storiesList.setDrawingCacheEnabled(true);
        storiesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        storiesList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());

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
        storiesListAdapter.toggleSelection(position);
        String title = String.format(" " + getString(R.string.selected_items), storiesListAdapter.getSelectedItemCount());
        actionMode.setTitle(title);


    }

    public void showStories(List<StoryModel> storyModels) {
        storiesList.setVisibility(View.VISIBLE);
        storiesListAdapter.setStoryModelList(storyModels);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }


    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (storiesPresenter != null)
            storiesPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
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

            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW:
                new Handler().postDelayed(() -> {
                    storiesListAdapter.notifyDataSetChanged();
                }, 500);
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void actionModeDestroyed() {
        if (actionModeStarted) {
            actionModeStarted = false;
            if (AppHelper.isAndroid5()) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
            }
        }
    }

    private void actionModeStarted() {
        if (!actionModeStarted) {
            actionModeStarted = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(AppHelper.getColor(this, R.color.colorActionMode));
            }
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.select_story_menu, menu);
        actionModeStarted();
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
            case R.id.delete_stories:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);


                builder.setMessage(R.string.are_you_sure);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    int arraySize = storiesListAdapter.getSelectedItems().size();

                    AppHelper.LogCat("start delete " + arraySize);

                    if (arraySize != 0) {
                        AppHelper.showDialog(this, getString(R.string.deleting_chat));
                        for (int x = 0; x < arraySize; x++) {
                            int currentPosition = storiesListAdapter.getSelectedItems().get(x);
                            try {
                                StoryModel storyModel = storiesListAdapter.getItem(currentPosition);
                                storiesPresenter.deleteStory(storyModel, currentPosition);
                            } catch (Exception e) {
                                AppHelper.LogCat(e);
                            }
                        }
                        AppHelper.LogCat("finish delete");
                        AppHelper.hideDialog();
                    } else {
                        AppHelper.CustomToast(this, "Delete conversation failed  ");
                    }
                    if (actionMode != null) {
                        storiesListAdapter.clearSelections();
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
        storiesListAdapter.clearSelections();
        actionModeDestroyed();
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = storiesList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = storiesList.getChildAdapterPosition(view);
            try {
                StoryModel storyModel = storiesListAdapter.getItem(currentPosition);

                if (actionMode != null) {
                    ToggleSelection(currentPosition);
                    boolean hasCheckedItems = storiesListAdapter.getSelectedItems().size() > 0;//Check if any items are already selected or not
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

                View view = storiesList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = storiesList.getChildAdapterPosition(view);
                if (actionMode != null) {
                    return;
                }

                actionMode = startActionMode(StoriesListActivity.this);
                if (actionMode != null) {
                    ToggleSelection(currentPosition);

                }

                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }


        }


    }


}
