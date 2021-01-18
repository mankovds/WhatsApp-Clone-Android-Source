package com.strolink.whatsUp.activities.messages;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.messages.TransferMessageContactsAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.users.SelectContactsPresenter;
import com.strolink.whatsUp.ui.PreCachingLayoutManager;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class TransferMessageContactsActivity extends BaseActivity {

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)

    RecyclerViewFastScroller fastScroller;
    private List<UsersModel> mContactsModelList;
    private TransferMessageContactsAdapter mTransferMessageContactsAdapter;
    private SelectContactsPresenter mContactsPresenter;
    private ArrayList<String> messageCopied = new ArrayList<>();
    private ArrayList<String> filePathList = new ArrayList<>();
    private String filePath;
    private boolean forCall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            if (getIntent().hasExtra("messageCopied")) {
                messageCopied = getIntent().getExtras().getStringArrayList("messageCopied");
            }
            forCall = getIntent().getBooleanExtra("forCall", false);

            Intent intent = getIntent();
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    filePath = FilesManager.getPath(this, uri);
                }
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text != null) {
                    messageCopied.add(text);
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (uris != null) {
                    for (Uri uri : uris) {
                        filePathList.add(FilesManager.getPath(this, uri));
                    }
                }
            }

        }
        initializeView();
        mContactsPresenter = new SelectContactsPresenter(this);
        mContactsPresenter.onCreate();

    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_select_contacts));

        }
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        if (filePathList != null && filePathList.size() != 0) {
            mTransferMessageContactsAdapter = new TransferMessageContactsAdapter(this, mContactsModelList, filePathList, true);
        } else if (filePath != null) {
            mTransferMessageContactsAdapter = new TransferMessageContactsAdapter(this, mContactsModelList, filePath);
        } else if (messageCopied != null && messageCopied.size() != 0) {
            mTransferMessageContactsAdapter = new TransferMessageContactsAdapter(this, mContactsModelList, messageCopied);
        } else if (forCall) {
            mTransferMessageContactsAdapter = new TransferMessageContactsAdapter(this, mContactsModelList, true);
        }

        ContactsList.setLayoutManager(layoutManager);
        ContactsList.setAdapter(mTransferMessageContactsAdapter);
        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        // Set up SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_contacts).getActionView();
        searchView.setIconified(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(mQueryTextListener);
        searchView.setQueryHint(getString(R.string.search_hint));
        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            Search(s.trim());
            return true;
        }
    };

    /**
     * method to start searching
     *
     * @param string this is parameter for Search method
     */
    public void Search(String string) {
        mTransferMessageContactsAdapter.setString(string);
        List<UsersModel> filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mTransferMessageContactsAdapter.setContacts(filteredModelList);

        }
    }

    /**
     * method to filter the list of contacts
     *
     * @param query this is parameter for FilterList method
     * @return this is what method will return
     */
    private List<UsersModel> FilterList(String query) {
        return UsersController.getInstance().loadAllLinkedUsersQuery(PreferenceManager.getInstance().getID(this), query);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
     * method to show linked contacts
     *
     * @param contactsModels this is parameter for ShowContacts method
     */
    public void ShowContacts(List<UsersModel> contactsModels) {
        mContactsModelList = contactsModels;
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle("" + mContactsModelList.size() + getString(R.string.of) + PreferenceManager.getInstance().getContactSize(this));
        mTransferMessageContactsAdapter.setContacts(mContactsModelList);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
