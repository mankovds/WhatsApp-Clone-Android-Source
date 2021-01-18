package com.strolink.whatsUp.activities.stories;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.others.TextWatcherAdapter;
import com.strolink.whatsUp.adapters.recyclerView.stories.PrivacyContactsAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.interfaces.LoadingData;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.users.ContactsPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class PrivacyContactsActivity extends BaseActivity implements LoadingData, RecyclerView.OnItemTouchListener {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;
    private PrivacyContactsAdapter privacyContactsAdapter;
    private ContactsPresenter mContactsPresenter;

    @BindView(R.id.toolbar_progress_bar)
    ProgressBar toolbarProgressBar;

    @BindView(R.id.close_btn_search_view)
    AppCompatImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    AppCompatImageView clearBtn;
    @BindView(R.id.app_bar_search_view)
    View searchView;


    @BindView(R.id.main_view)
    LinearLayout MainView;
    private GestureDetectorCompat gestureDetector;

    private boolean exclude = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_contacts);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("exclude")) {
            exclude = getIntent().getBooleanExtra("exclude", false);
        }

        searchInput.setFocusable(true);
        initializerSearchView(searchInput, clearBtn);
        initializerView();

        mContactsPresenter = new ContactsPresenter(this);
        mContactsPresenter.onCreate();


    }


    /**
     * method to initialize the view
     */
    private void initializerView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (exclude) {
                getSupportActionBar().setTitle(getString(R.string.title_hide_stories_from));
                getSupportActionBar().setSubtitle(getString(R.string.title_sub_hide_stories_from));
            } else {
                getSupportActionBar().setTitle(getString(R.string.title_share_stories_with));
                getSupportActionBar().setSubtitle(getString(R.string.title_sub_share_stories_with));
            }

        }
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        privacyContactsAdapter = new PrivacyContactsAdapter();
        ContactsList.setLayoutManager(layoutManager);
        ContactsList.setAdapter(privacyContactsAdapter);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(30);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        ContactsList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

        closeBtn.setOnClickListener(v -> closeSearchView());
        clearBtn.setOnClickListener(v -> clearSearchView());

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
     * method to initialize the search view
     */
    public void initializerSearchView(TextInputEditText searchInput, AppCompatImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                privacyContactsAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    mContactsPresenter.getContacts();
                }
            }
        });

    }


    @SuppressWarnings("unused")
    @OnClick(R.id.floatingBtnSave)
    public void floatingBtnSave() {
        if (exclude) {
            AppHelper.LogCat("getUnSelectedItem " + privacyContactsAdapter.getUnSelectedItem().size());


            List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
            AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());
            if (usersPrivacyModels.size() != 0)
                for (UsersPrivacyModel usersPrivacyModel : usersPrivacyModels)
                    UsersController.getInstance().deleteUserPrivacy(usersPrivacyModel);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int arraySize = privacyContactsAdapter.getUnSelectedItem().size();
            for (int x = 0; x < arraySize; x++) {

                try {
                    UsersModel usersModel = privacyContactsAdapter.getItem(x);
                    UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                    usersPrivacyModel.setUp_id(usersModel.get_id());
                    usersPrivacyModel.setExclude(true);
                    usersPrivacyModel.setUsersModel(usersModel);
                    UsersController.getInstance().insertUserPrivacy(usersPrivacyModel);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }


            PreferenceManager.getInstance().setStoriesPrivacy(this, AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS_EXCEPT);
            finish();

        } else {
            AppHelper.LogCat("getSelectedItem " + privacyContactsAdapter.getSelectedItemCount());


            List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
            AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());
            if (usersPrivacyModels.size() != 0)
                for (UsersPrivacyModel usersPrivacyModel : usersPrivacyModels)
                    UsersController.getInstance().deleteUserPrivacy(usersPrivacyModel);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int arraySize = privacyContactsAdapter.getSelectedItems().size();
            for (int x = 0; x < arraySize; x++) {
                int currentPosition = privacyContactsAdapter.getSelectedItems().get(x);
                try {
                    UsersModel usersModel = privacyContactsAdapter.getItem(currentPosition);
                    UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                    usersPrivacyModel.setUp_id(usersModel.get_id());
                    usersPrivacyModel.setExclude(false);
                    usersPrivacyModel.setUsersModel(usersModel);
                    UsersController.getInstance().insertUserPrivacy(usersPrivacyModel);

                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }


            PreferenceManager.getInstance().setStoriesPrivacy(this, AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS_WITH);
            finish();

        }

        AnimationsUtil.setTransitionAnimation(this);
    }


    /**
     * method to toggle the selection
     *
     * @param position
     */
    private void ToggleSelection(int position) {
        privacyContactsAdapter.toggleSelection(position);
        String title;
        if (exclude) {
            title = String.format(" " + getString(R.string.excluded_items), privacyContactsAdapter.getSelectedItemCount());
        } else {
            title = String.format(" " + getString(R.string.selected_items), privacyContactsAdapter.getSelectedItemCount());
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(title);


    }

    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            AppHelper.LogCat(" onSingleTapConfirmed ");
            View view = ContactsList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = ContactsList.getChildAdapterPosition(view);
            try {
                UsersModel usersModel = privacyContactsAdapter.getItem(currentPosition);

                ToggleSelection(currentPosition);
                if (privacyContactsAdapter.getSelectedItemCount() == 0)
                    if (exclude) {
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setSubtitle(getString(R.string.title_sub_hide_stories_from));
                    } else {
                        if (getSupportActionBar() != null)
                            getSupportActionBar().setSubtitle(getString(R.string.title_sub_share_stories_with));
                    }
            } catch (Exception ex) {
                AppHelper.LogCat(" onSingleTapConfirmed " + ex.getMessage());
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_contacts:
                launcherSearchView();
                break;
            case R.id.select_all:
                privacyContactsAdapter.checkAll();

                String title;
                if (exclude) {

                    if (privacyContactsAdapter.isSelectedAll) {
                        // title = String.format(" " + getString(R.string.excluded_items), privacyContactsAdapter.getUnSelectedItem().size());
                        title = null;
                    } else {
                        title = getString(R.string.title_sub_hide_stories_from);
                    }

                } else {
                    if (privacyContactsAdapter.isSelectedAll) {
                        //  title = String.format(" " + getString(R.string.selected_items), privacyContactsAdapter.getSelectedItemCount());
                        title = null;
                    } else {
                        title = getString(R.string.title_sub_share_stories_with);

                    }

                }

                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle(title);


                break;
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

    /**
     * method to show contacts list
     *
     * @param usersModels this is parameter for ShowContacts  method
     */
    public void ShowContacts(List<UsersModel> usersModels) {
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REFRESH_CONTACTS));

           if (usersModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);

            privacyContactsAdapter.setUsersModelList(usersModels);
            List<UsersPrivacyModel> usersPrivacyModels = UsersController.getInstance().getAllUsersPrivacy();
            AppHelper.LogCat("usersPrivacyModels initializer "+usersPrivacyModels.size());
            for (UsersPrivacyModel usersPrivacyModel : usersPrivacyModels) {
                int currentPosition = privacyContactsAdapter.indexFor(privacyContactsAdapter.getUsersModelList(), usersPrivacyModel.getUsersModel().get_id());
                ToggleSelection(currentPosition);

            }
        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainView.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MainView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onShowLoading() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        toolbarProgressBar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onHideLoading() {
        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());

        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_privacy_menu, menu);
        return true;
    }

    /**
     * method to clear/reset the search view
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            mContactsPresenter.getContacts();
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
        }
    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        List<UsersModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            privacyContactsAdapter.animateTo(filteredModelList);
            ContactsList.scrollToPosition(0);
        } else {
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * method to filter the list of contacts
     *
     * @param query this parameter for FilterList  method
     * @return this for what method will return
     */
    private List<UsersModel> FilterList(String query) {

        return UsersController.getInstance().loadAllLinkedUsersQuery(PreferenceManager.getInstance().getID(this), query);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContactsPresenter != null)
            mContactsPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


}
