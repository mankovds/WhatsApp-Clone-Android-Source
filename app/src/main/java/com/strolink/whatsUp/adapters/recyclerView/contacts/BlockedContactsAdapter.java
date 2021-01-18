package com.strolink.whatsUp.adapters.recyclerView.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class BlockedContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private final Activity mActivity;
    private List<UsersBlockModel> mContactsModel;

    private String userId;

    public void setContacts(List<UsersBlockModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    public BlockedContactsAdapter(@NonNull Activity mActivity ) {
        this.mActivity = mActivity;
        mContactsModel = new ArrayList<>();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContactsViewHolder) {
            final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            final UsersBlockModel usersBlockModel = this.mContactsModel.get(position);
            try {
                String username = usersBlockModel.getUsersModel().getDisplayed_name();


                contactsViewHolder.setUsername(username);


                if (usersBlockModel.getUsersModel().getStatus() != null) {
                    contactsViewHolder.setStatus(usersBlockModel.getUsersModel().getStatus().getBody());
                } else {
                    contactsViewHolder.setStatus(usersBlockModel.getUsersModel().getPhone());
                }

                if (usersBlockModel.getUsersModel().isLinked()) {
                    contactsViewHolder.hideInviteButton();
                } else {
                    contactsViewHolder.showInviteButton();
                }
                contactsViewHolder.setUserImage(usersBlockModel.getUsersModel().getImage(), usersBlockModel.getUsersModel().get_id(), username);

            } catch (Exception e) {
                AppHelper.LogCat("" + e.getMessage());
            }

        }

    }


    @Override
    public int getItemCount() {
        return mContactsModel.size() > 0 ? mContactsModel.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (mContactsModel.size() > pos) {

                return Character.toString(mContactsModel.get(pos).getUsersModel().getDisplayed_name().charAt(0));

            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }


    private void removeItem(int position) {
        mContactsModel.remove(position);
        notifyItemRemoved(position);

    }


    private int indexFor(List<UsersBlockModel> array, String id) {
        int position = -1;
        try {

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getB_id().equals(id)) {
                    position = i;
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            return -1;
        }

        return position;
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        AppCompatImageView userImage;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.status)
        EmojiTextView status;
        @BindView(R.id.invite)
        TextView invite;

        @SuppressLint("CheckResult")
        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

            itemView.setOnClickListener(view -> {
                UsersBlockModel usersBlockModel = mContactsModel.get(getAdapterPosition());
                userId = usersBlockModel.getUsersModel().get_id();
                //delete popup

                AlertDialog.Builder builderUnblock = new AlertDialog.Builder(mActivity);
                builderUnblock.setMessage(R.string.unblock_user_make_sure);
                builderUnblock.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    APIHelper.initialApiUsersContacts().unbBlock(userId).subscribe(blockResponse -> {
                        if (blockResponse.isSuccess()) {


                            UsersBlockModel usersBlockModel2 = UsersController.getInstance().getUserBlockedById(userId);
                            UsersController.getInstance().deleteUserBlocked(usersBlockModel2);

                            if (mContactsModel.size() == 1)
                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REFRESH_BLOCKED_LIST));
                            else
                                removeItem(getAdapterPosition());


                        } else {

                            AppHelper.CustomToast(mActivity, blockResponse.getMessage());
                        }
                    }, throwable -> {
                        AppHelper.LogCat("throwable " + throwable.getMessage());
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
                    });


                });

                builderUnblock.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builderUnblock.show();
            });
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


        void hideInviteButton() {
            invite.setVisibility(View.GONE);
        }

        void showInviteButton() {
            invite.setVisibility(View.VISIBLE);
        }

        void setUsername(String phone) {
            username.setText(phone);
        }

        void setStatus(String Status) {
            String user = UtilsString.unescapeJava(Status);
            status.setText(user);
        }


    }
}
