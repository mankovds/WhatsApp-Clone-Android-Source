package com.strolink.whatsUp.adapters.recyclerView.groups;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    protected Activity mActivity;
    private List<MembersModel> membersModelList;


    public GroupMembersAdapter(@NonNull Activity mActivity) {
        this.mActivity = mActivity;
        this.membersModelList = new ArrayList<>();

    }

    public void setMembers(List<MembersModel> membersModels) {
        this.membersModelList = membersModels;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_group_members, parent, false);
        return new ContactsViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final MembersModel membersGroupModel = this.membersModelList.get(position);
        try {


            UsersModel usersModel = getUser(membersGroupModel.getOwnerId());

            if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                contactsViewHolder.itemView.setEnabled(false);
            }


            try {
                if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    contactsViewHolder.setUsername(mActivity.getString(R.string.you));
                } else {
                    String name = usersModel.getDisplayed_name();
                    if (name != null) {
                        contactsViewHolder.setUsername(name);
                    } else {
                        contactsViewHolder.setUsername(usersModel.getPhone());
                    }

                }
            } catch (Exception e) {
                AppHelper.LogCat(" " + e.getMessage());
            }


            if (usersModel.getStatus().getBody() != null) {
                contactsViewHolder.setStatus(usersModel.getStatus().getBody());
            } else {
                contactsViewHolder.setStatus(usersModel.getPhone());
            }
            if (!membersGroupModel.isAdmin()) {
                contactsViewHolder.hideAdmin();
            } else {
                contactsViewHolder.showAdmin();
            }

            contactsViewHolder.setUserImage(usersModel.getImage(), usersModel.get_id(), null);

            contactsViewHolder.setOnLongClickListener(view -> {

                MembersModel currentUser = getUserInfo(PreferenceManager.getInstance().getID(mActivity));

                if (currentUser.isAdmin()) {
                    String TheName;
                    String name = usersModel.getDisplayed_name();
                    if (name != null) {
                        TheName = name;
                    } else {
                        TheName = usersModel.getPhone();
                    }
                    CharSequence options[];
                    if (membersGroupModel.isAdmin()) {
                        options = new CharSequence[]{mActivity.getString(R.string.message_group_option) + TheName + "", mActivity.getString(R.string.view_group_option) + TheName + "", mActivity.getString(R.string.make_group_option) + " " + TheName + " " + mActivity.getString(R.string.make_member_group_option), mActivity.getString(R.string.remove_group_option) + TheName + ""};
                    } else {
                        options = new CharSequence[]{mActivity.getString(R.string.message_group_option) + TheName + "", mActivity.getString(R.string.view_group_option) + TheName + "", mActivity.getString(R.string.make_group_option) + " " + TheName + " " + mActivity.getString(R.string.make_admin_group_option), mActivity.getString(R.string.remove_group_option) + TheName + ""};
                    }
                    if (!mActivity.isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setItems(options, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                                    messagingIntent.putExtra("recipientID", membersGroupModel.getOwnerId());
                                    messagingIntent.putExtra("isGroup", false);
                                    mActivity.startActivity(messagingIntent);
                                    mActivity.finish();
                                    break;
                                case 1:
                                    contactsViewHolder.viewContact(usersModel.getPhone());
                                    break;
                                case 2:
                                    if (membersGroupModel.isAdmin()) {
                                        contactsViewHolder.makeAdminMember(membersGroupModel.get_id(), membersGroupModel.getGroupId());
                                    } else {
                                        contactsViewHolder.makeMemberAdmin(membersGroupModel.get_id(), membersGroupModel.getGroupId());
                                    }
                                    break;
                                case 3:
                                    AlertDialog.Builder builderDelete = new AlertDialog.Builder(mActivity);
                                    builderDelete.setMessage(mActivity.getString(R.string.remove_from_group) + TheName + mActivity.getString(R.string.from_group))
                                            .setPositiveButton(mActivity.getString(R.string.ok), (dialog1, which1) -> {
                                                contactsViewHolder.RemoveMemberFromGroup(membersGroupModel.get_id(), membersGroupModel.getGroupId());
                                            }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();
                                    break;
                            }

                        });

                        builder.show();
                    }
                }


                // return true;


            });
        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
        }

    }

    private MembersModel getUserInfo(String userId) {
        return UsersController.getInstance().loadSingleMemberByOwnerId(userId);

    }

    private UsersModel getUser(String userId) {
        return UsersController.getInstance().getUserById(userId);

    }

    @Override
    public int getItemCount() {
        if (membersModelList != null)
            return membersModelList.size();
        else
            return 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (membersModelList.size() > pos) {

                String name = UtilsPhone.getContactName(membersModelList.get(pos).getOwner_phone());
                if (name != null) {
                    return Character.toString(name.charAt(0));
                } else {
                    return Character.toString(membersModelList.get(pos).getOwner_phone().charAt(0));
                }

            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        AppCompatImageView userImage;
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.status)
        EmojiTextView status;
        @BindView(R.id.admin)
        TextView admin;

        @BindView(R.id.member)
        TextView member;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

        }


        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId, String username) {
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
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {
                userImage.setImageDrawable(drawable);
            }
        }

        void hideAdmin() {
            admin.setVisibility(View.GONE);
            member.setVisibility(View.VISIBLE);
        }

        void showAdmin() {
            admin.setVisibility(View.VISIBLE);
            member.setVisibility(View.GONE);
        }

        void setUsername(String Username) {
            username.setText(Username);
        }

        void setStatus(String Status) {
            String statu = UtilsString.unescapeJava(Status);
            status.setText(statu);
        }


        void setOnLongClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }

        void viewContact(String phone) {
            long ContactID = UtilsPhone.getContactID(mActivity, phone);
            try {
                if (ContactID != 0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
                    mActivity.startActivity(intent);
                }
            } catch (Exception e) {
                AppHelper.LogCat("Error view contact  Exception" + e.getMessage());
            }
        }


        @SuppressLint("CheckResult")
        void makeMemberAdmin(String id, String groupID) {

            APIHelper.initializeApiGroups().makeMemberAdmin(groupID, id).subscribe(groupResponse -> {
                if (groupResponse.isSuccess()) {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);


                    MembersModel membersGroupModel = UsersController.getInstance().loadSingleMemberById(id);
                    membersGroupModel.setAdmin(true);
                    UsersController.getInstance().updateMember(membersGroupModel);
                    notifyItemChanged(getAdapterPosition());
                    MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.ADMIN_STATE);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));


                } else {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), mActivity.getString(R.string.failed_to_make_member_as_admin), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            });


        }

        @SuppressLint("CheckResult")
        void makeAdminMember(String id, String groupID) {

            APIHelper.initializeApiGroups().makeAdminMember(groupID, id).subscribe(groupResponse -> {
                if (groupResponse.isSuccess()) {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);


                    MembersModel membersGroupModel = UsersController.getInstance().loadSingleMemberById(id);
                    membersGroupModel.setAdmin(false);
                    UsersController.getInstance().updateMember(membersGroupModel);
                    notifyItemChanged(getAdapterPosition());
                    MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.MEMBER_STATE);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));


                } else {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                AppHelper.LogCat("" + throwable);
                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), mActivity.getString(R.string.failed_to_make_member_as_admin), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            });


        }

        @SuppressLint("CheckResult")
        void RemoveMemberFromGroup(String id, String groupID) {

            APIHelper.initializeApiGroups().removeMember(groupID, id).subscribe(groupResponse -> {
                if (groupResponse.isSuccess()) {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);


                    MembersModel membersGroupModel = UsersController.getInstance().loadSingleMemberById(id);
                    UsersController.getInstance().deleteMember(membersGroupModel);
                    notifyDataSetChanged();
                    MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.REMOVE_STATE);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));


                } else {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                AppHelper.LogCat("throwable " + throwable.getMessage());
                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.containerProfile), mActivity.getString(R.string.failed_to_remove_member), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            });


        }


    }


}
