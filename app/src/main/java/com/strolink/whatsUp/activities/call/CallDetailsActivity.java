package com.strolink.whatsUp.activities.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.adapters.recyclerView.calls.CallsDetailsAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.call.CallManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.presenters.calls.CallsPresenter;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 12/18/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallDetailsActivity extends BaseActivity {

    @BindView(R.id.CallsDetailsList)
    RecyclerView CallsList;

    @BindView(R.id.user_image)
    AppCompatImageView userImage;

    @BindView(R.id.username)
    TextView username;

    @BindView(R.id.CallVideoBtn)
    AppCompatImageView CallVideoBtn;

    @BindView(R.id.CallBtn)
    AppCompatImageView CallBtn;

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private CallsDetailsAdapter mCallsAdapter;
    private CallsPresenter mCallsPresenter;

    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_details);
        ButterKnife.bind(this);
        initializerView();
        setupToolbar();

        mCallsPresenter = new CallsPresenter(this);
        userID = getIntent().getStringExtra("userID");
        mCallsPresenter.onCreate();
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mCallsAdapter = new CallsDetailsAdapter(this);
        CallsList.setLayoutManager(mLinearLayoutManager);
        CallsList.setAdapter(mCallsAdapter);
        CallsList.setItemAnimator(new DefaultItemAnimator());
        CallsList.getItemAnimator().setChangeDuration(0);
        CallBtn.setOnClickListener(v -> CallManager.callContact(this, false, userID));
        CallVideoBtn.setOnClickListener(v -> CallManager.callContact(this, true, userID));
    }

    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_calls);
    }

    public void UpdateCallsDetailsList(List<CallsInfoModel> callsModelList) {
        if (callsModelList.size() != 0) {
            mCallsAdapter.setCalls(callsModelList);
        }
    }


    public void refreshMenu() {
        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        refreshMenu();

        if (MessagesController.getInstance().checkIfUserBlockedExist(userID)) {
            getMenuInflater().inflate(R.menu.calls_info_menu_unblock, menu);

        } else {
            getMenuInflater().inflate(R.menu.calls_info_menu, menu);

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @SuppressLint("CheckResult")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == R.id.new_message) {
            Intent messagingIntent = new Intent(this, MessagesActivity.class);
            // messagingIntent.putExtra("conversationID", "");
            messagingIntent.putExtra("recipientID", userID);
            messagingIntent.putExtra("isGroup", false);
            startActivity(messagingIntent);
            AnimationsUtil.setTransitionAnimation(this);
            finish();
        } else if (item.getItemId() == R.id.remove_from_log) {
            mCallsPresenter.removeCall();

        } else if (item.getItemId() == R.id.block_user) {

            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setMessage(R.string.block_user_make_sure);
            builder2.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {

                APIHelper.initialApiUsersContacts().block(userID).subscribe(blockResponse -> {
                    if (blockResponse.isSuccess()) {

                        UsersModel usersModel = UsersController.getInstance().getUserById(userID);
                        UsersBlockModel usersBlockModel = new UsersBlockModel();
                        usersBlockModel.setB_id(DbBackupRestore.getBlockUserLastId());
                        usersBlockModel.setUsersModel(usersModel);
                        UsersController.getInstance().insertUserBlocked(usersBlockModel);


                        refreshMenu();
                        AppHelper.CustomToast(this, blockResponse.getMessage());

                    } else {
                        AppHelper.CustomToast(this, blockResponse.getMessage());
                    }
                }, throwable -> {
                    AppHelper.CustomToast(this, getString(R.string.oops_something));
                });


            });

            builder2.setNegativeButton(R.string.No, (dialog, whichButton) -> {

            });

            builder2.show();
        } else if (item.getItemId() == R.id.unblock_user) {

            AlertDialog.Builder builderUnblock = new AlertDialog.Builder(this);
            builderUnblock.setMessage(R.string.unblock_user_make_sure);
            builderUnblock.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {

                APIHelper.initialApiUsersContacts().unbBlock(userID).subscribe(blockResponse -> {
                    if (blockResponse.isSuccess()) {
                        UsersBlockModel usersBlockModel2 = UsersController.getInstance().getUserBlockedById(userID);
                        UsersController.getInstance().deleteUserBlocked(usersBlockModel2);


                        refreshMenu();
                        AppHelper.CustomToast(this, blockResponse.getMessage());

                    } else {
                        AppHelper.CustomToast(this, blockResponse.getMessage());
                    }
                }, throwable -> {
                    AppHelper.CustomToast(this, getString(R.string.oops_something));
                });


            });

            builderUnblock.setNegativeButton(R.string.No, (dialog, whichButton) -> {

            });

            builderUnblock.show();


        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallsPresenter.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @SuppressLint("StaticFieldLeak")
    public void showUserInfo(UsersModel contactsModel) {
        String finalName;
        if (contactsModel.getUsername() != null) {
            finalName = contactsModel.getUsername();
        } else {
            String name = contactsModel.getDisplayed_name();
            if (name != null) {
                finalName = name;
            } else {
                finalName = contactsModel.getPhone();
            }
        }
        username.setText(finalName);
        Drawable drawable = AppHelper.getDrawable(this, R.drawable.holder_user);

        String ImageUrl = contactsModel.getImage();
        String userId = contactsModel.get_id();

        if (ImageUrl != null) {
            DrawableImageViewTarget target = new DrawableImageViewTarget(userImage) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    userImage.setImageDrawable(resource);
                }


                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    userImage.setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    userImage.setImageDrawable(placeholder);
                }
            };
            GlideApp.with(CallDetailsActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + ImageUrl))

                    .signature(new ObjectKey(ImageUrl))
                    .centerCrop().apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(target);
        } else {
            userImage.setImageDrawable(drawable);
        }

    }

    public void showCallInfo(CallsModel callsModel) {
        if (callsModel.getType().equals(AppConstants.VIDEO_CALL)) {
            showVideoButton();
        } else if (callsModel.getType().equals(AppConstants.VOICE_CALL)) {
            hideVideoButton();
        }
    }


    void showVideoButton() {
        CallVideoBtn.setVisibility(View.VISIBLE);
        CallBtn.setVisibility(View.GONE);
    }

    void hideVideoButton() {
        CallVideoBtn.setVisibility(View.GONE);
        CallBtn.setVisibility(View.VISIBLE);
    }

}
