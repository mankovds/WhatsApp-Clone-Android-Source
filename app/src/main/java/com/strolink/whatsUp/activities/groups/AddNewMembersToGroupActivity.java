package com.strolink.whatsUp.activities.groups;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.adapters.recyclerView.groups.AddNewMembersToGroupAdapter;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.presenters.groups.AddNewMembersToGroupPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupActivity extends BaseActivity {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.ParentLayoutAddNewMembers)
    LinearLayout ParentLayoutAddContact;
    @BindView(R.id.app_bar)
    Toolbar toolbar;


    private AddNewMembersToGroupPresenter mAddMembersToGroupPresenter;
    private String groupID;
    private AddNewMembersToGroupAdapter mAddMembersToGroupListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_members_to_group);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("groupID")) {
            groupID = getIntent().getExtras().getString("groupID", "");
        }

        initializeView();
        setupToolbar();
        mAddMembersToGroupPresenter = new AddNewMembersToGroupPresenter(this);
        mAddMembersToGroupPresenter.onCreate();
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddNewMembersToGroupAdapter(this, groupID);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // this is the default; this call is actually only necessary with custom ItemAnimators
        ContactsList.setItemAnimator(new DefaultItemAnimator());


    }

    /**
     * method to show contacts
     *
     * @param contactsModelList this  parameter of ShowContacts method
     */
    public void ShowContacts(List<UsersModel> contactsModelList) {
        mAddMembersToGroupListAdapter.setContacts(contactsModelList);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();

    }


    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_new_members_to_group);
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
        searchView.setQueryHint("Search ...");
        return true;
    }

    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mAddMembersToGroupListAdapter.setString(s);
            Search(s);

            return true;
        }
    };


    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {


        List<UsersModel> filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {


            mAddMembersToGroupListAdapter.animateTo(filteredModelList);
            ContactsList.scrollToPosition(0);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
