package com.strolink.whatsUp.adapters.recyclerView.calls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.call.CallDetailsActivity;
import com.strolink.whatsUp.activities.profile.ProfilePreviewActivity;
import com.strolink.whatsUp.activities.settings.PreferenceSettingsManager;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.call.CallManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.presenters.controllers.CallsController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.joda.time.DateTime;

import java.util.ArrayList;
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

public class CallsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<CallsModel> callsModelList;
    private RecyclerView callList;
    private String SearchQuery;


    public CallsAdapter(RecyclerView callList) {
        this.callList = callList;

        this.callsModelList = new ArrayList<>();
    }

    public CallsAdapter() {

        this.callsModelList = new ArrayList<>();
    }

    public void setCalls(List<CallsModel> callsModelList) {
        this.callsModelList = callsModelList;
        notifyDataSetChanged();
    }


    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<CallsModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<CallsModel> newModels) {
        int arraySize = callsModelList.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final CallsModel model = callsModelList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<CallsModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final CallsModel model = newModels.get(i);
            if (!callsModelList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<CallsModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final CallsModel model = newModels.get(toPosition);
            final int fromPosition = callsModelList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        try {
            CallsModel callsModel = getItem(position);
            return callsModel.getId(); ///to avoid blink recyclerview item when notify the adapter
        } catch (Exception e) {
            return position;
        }

    }

    private CallsModel removeItem(int position) {
        final CallsModel model = callsModelList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, CallsModel model) {
        callsModelList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final CallsModel model = callsModelList.remove(fromPosition);
        callsModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_calls, parent, false);
        return new CallsViewHolder(itemView);
    }

    private UsersModel getUserInfo(String userId) {
        return UsersController.getInstance().getUserById(userId);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CallsViewHolder callsViewHolder = (CallsViewHolder) holder;
        final CallsModel callsModel = callsModelList.get(position);
        //  try {

        Activity mActivity = (Activity) callsViewHolder.itemView.getContext();
        UsersModel contactsModel;
        if (callsModel.getFrom().equals(PreferenceManager.getInstance().getID(mActivity)))
            contactsModel = getUserInfo(callsModel.getTo());
        else
            contactsModel = getUserInfo(callsModel.getFrom());

        String Username = callsModel.getUsersModel().getDisplayed_name();
        if (Username == null) {
             Username = UtilsPhone.getContactName(callsModel.getUsersModel().getPhone());
        }
        SpannableString Message = SpannableString.valueOf(Username);
        if (SearchQuery != null) {
            int index = TextUtils.indexOf(Username.toLowerCase(), SearchQuery.toLowerCase());
            if (index >= 0) {
                Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            callsViewHolder.username.setText(Message, TextView.BufferType.SPANNABLE);
            callsViewHolder.username.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
        } else {
            callsViewHolder.username.setText(Username, TextView.BufferType.NORMAL);
            callsViewHolder.username.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
        }


        if (callsModel.isReceived()) {
            callsViewHolder.showIcon();
        } else {
            callsViewHolder.hideIcon();
        }
        if (callsModel.getType().equals(AppConstants.VIDEO_CALL)) {
            callsViewHolder.showVideoButton();
        } else if (callsModel.getType().equals(AppConstants.VOICE_CALL)) {
            callsViewHolder.hideVideoButton();

        }
        callsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), Username);

        if (callsModel.getDate() != null) {
            callsViewHolder.setCallDate(callsModel.getDate());
        }

        if (callsModel.getCounter() != 0 && callsModel.getCounter() > 1)
            callsViewHolder.setCallCounter(callsModel.getCounter());
        else
            callsViewHolder.counterCall.setVisibility(View.GONE);


        callsViewHolder.setOnClickListener(v -> {

            switch (v.getId()) {
                case R.id.CallVideoBtn:
                    if (callsModel.isReceived())
                        CallManager.callContact(mActivity, true, callsModel.getFrom());
                    else
                        CallManager.callContact(mActivity, true, callsModel.getTo());
                    break;
                case R.id.CallBtn:
                    if (callsModel.isReceived())
                        CallManager.callContact(mActivity, false, callsModel.getFrom());
                    else
                        CallManager.callContact(mActivity, false, callsModel.getTo());
                    break;
                case R.id.user_image:
                    if (AppHelper.isAndroid5()) {
                        if (contactsModel.isLinked() && contactsModel.isActivate()) {
                            Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                            mIntent.putExtra("userID", contactsModel.get_id());
                            mIntent.putExtra("isGroup", false);
                            mActivity.startActivity(mIntent);
                        }
                    } else {
                        if (contactsModel.isLinked() && contactsModel.isActivate()) {
                            Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                            mIntent.putExtra("userID", contactsModel.get_id());
                            mActivity.startActivity(mIntent);
                            mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                        }
                    }

                    break;
                default:
                    Intent mIntent = new Intent(mActivity, CallDetailsActivity.class);
                    mIntent.putExtra("userID", contactsModel.get_id());
                    mIntent.putExtra("callID", callsModel.getC_id());
                    mActivity.startActivity(mIntent);
                    AnimationsUtil.setTransitionAnimation(mActivity);
                    break;

            }
        });


     /*   } catch (Exception e) {
            AppHelper.LogCat("ex " + e.getMessage());
        }*/
    }


    @Override
    public int getItemCount() {
        if (callsModelList != null) return callsModelList.size();
        return 0;
    }


    public CallsModel getItem(int position) {
        return callsModelList.get(position);
    }


    public void addCallItem(String callId) {
        try {

            CallsModel callsModel = CallsController.getInstance().getCallById(callId);
            if (!isCallExistInList(callsModel.getC_id())) {
                addCallItem(0, callsModel);
            } else {
                return;
            }


        } catch (Exception e) {
            AppHelper.LogCat("addCallItem Exception" + e);
        }
    }

    private void addCallItem(int position, CallsModel callsModel) {
        try {
            this.callsModelList.add(position, callsModel);
            notifyItemInserted(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private boolean isCallExistInList(String callId) {
        int arraySize = callsModelList.size();
        boolean exist = false;
        for (int i = 0; i < arraySize; i++) {
            CallsModel model = callsModelList.get(i);
            if (callId.equals(model.getC_id())) {
                exist = true;
                break;
            }
        }
        return exist;
    }


    public void updateCallItem(String callId) {
        try {

            int arraySize = callsModelList.size();
            for (int i = 0; i < arraySize; i++) {
                CallsModel model = callsModelList.get(i);
                if (callId.equals(model.getC_id())) {
                    CallsModel callsModel = CallsController.getInstance().getCallById(callId);
                    changeItemAtPosition(i, callsModel);
                    if (i != 0)
                        MoveItemToPosition(i, 0);
                    break;
                }

            }

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, CallsModel callsModel) {
        callsModelList.set(position, callsModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        CallsModel model = callsModelList.remove(fromPosition);
        callsModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        callList.scrollToPosition(fromPosition);
    }

    public void removeCallItem(int position) {
        try {
            callsModelList.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    public void DeleteCallItem(String callID) {
        try {
            int arraySize = callsModelList.size();
            for (int i = 0; i < arraySize; i++) {
                CallsModel model = callsModelList.get(i);

                if (callID.equals(model.getC_id())) {
                    removeCallItem(i);
                    break;
                }

            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }


    public class CallsViewHolder extends RecyclerView.ViewHolder {

        Context mActivity;
        @BindView(R.id.user_image)
        AppCompatImageView userImage;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.CallVideoBtn)
        AppCompatImageView CallVideoBtn;
        @BindView(R.id.CallBtn)
        AppCompatImageView CallBtn;
        @BindView(R.id.icon_made)
        AppCompatImageView IconMade;
        @BindView(R.id.icon_received)
        AppCompatImageView IconReceived;
        @BindView(R.id.date_call)
        TextView CallDate;
        @BindView(R.id.counter_call)
        TextView counterCall;

        public CallsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mActivity = itemView.getContext();

        }


        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId, String name) {
            Drawable drawable = AppHelper.getDrawable(mActivity, R.drawable.holder_user);
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
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };

                GlideApp.with(mActivity.getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))

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


        void hideIcon() {
            IconMade.setVisibility(View.VISIBLE);
            IconReceived.setVisibility(View.GONE);
        }

        void showIcon() {
            IconMade.setVisibility(View.GONE);
            IconReceived.setVisibility(View.VISIBLE);
        }

        void showVideoButton() {
            CallVideoBtn.setVisibility(View.VISIBLE);
            CallBtn.setVisibility(View.GONE);
        }

        void hideVideoButton() {
            CallVideoBtn.setVisibility(View.GONE);
            CallBtn.setVisibility(View.VISIBLE);
        }

        @SuppressLint({"StaticFieldLeak", "CheckResult"})
        void setCallDate(String date) {

            DateTime messageDate = UtilsTime.getCorrectDate(date);
            CallDate.setText(UtilsTime.convertDateToStringFormat(mActivity, messageDate));
        }

        void setCallCounter(int counter) {
            counterCall.setVisibility(View.VISIBLE);
            counterCall.setText(String.format("(%d)", counter));
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
            CallVideoBtn.setOnClickListener(listener);
            CallBtn.setOnClickListener(listener);
        }


    }
}
