package com.strolink.whatsUp.adapters.recyclerView.messages;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.call.CallManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class TransferMessageContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    protected final Activity mActivity;
    private List<UsersModel> mContactsModel;
    private String SearchQuery;
    private ArrayList<String> filePathList;
    private ArrayList<String> messageCopied;
    private String filePath;
    private boolean forCall = false;

    public TransferMessageContactsAdapter(@NonNull Activity mActivity, List<UsersModel> mContactsModel, ArrayList<String> messageCopied) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
        this.messageCopied = messageCopied;
        this.filePathList = messageCopied;
    }

    public TransferMessageContactsAdapter(@NonNull Activity mActivity, List<UsersModel> mContactsModel, boolean forCall) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
        this.forCall = forCall;
    }

    public TransferMessageContactsAdapter(@NonNull Activity mActivity, List<UsersModel> mContactsModel, ArrayList<String> filePathList, boolean forFiles) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
        this.filePathList = filePathList;

    }

    public TransferMessageContactsAdapter(@NonNull Activity mActivity, List<UsersModel> mContactsModel, String filePath) {
        this.mActivity = mActivity;
        this.mContactsModel = mContactsModel;
        this.filePath = filePath;
    }


    public void setContacts(List<UsersModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
    }

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    //Methods for search end

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final UsersModel contactsModel = this.mContactsModel.get(position);
        try {
            contactsViewHolder.setUsername(contactsModel.getDisplayed_name(), contactsModel.getPhone());

            String username = contactsModel.getDisplayed_name();


            SpannableString recipientUsername = SpannableString.valueOf(username);
            if (SearchQuery == null) {
                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
            } else {
                int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
            }
            if (contactsModel.getStatus() != null) {
                String status = UtilsString.unescapeJava(contactsModel.getStatus().getBody());
                if (status.length() > 18)
                    contactsViewHolder.setStatus(status.substring(0, 18) + "... " + "");

                else
                    contactsViewHolder.setStatus(status);

            } else {
                contactsViewHolder.setStatus(contactsModel.getPhone());
            }

            if (contactsModel.isLinked()) {
                contactsViewHolder.hideInviteButton();
            } else {
                contactsViewHolder.showInviteButton();
            }

            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);

            if (forCall) {
                contactsViewHolder.showVideoButton();
            } else {
                contactsViewHolder.hideVideoButton();
            }
            contactsViewHolder.setOnClickListener(view -> {
                if (view.getId() == R.id.CallVideoBtn) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(mActivity.getString(R.string.call_select_video))
                            .setPositiveButton(mActivity.getString(R.string.Yes), (dialog, which) -> {
                                CallManager.callContact(mActivity, true, contactsModel.get_id());
                            }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();
                } else if (view.getId() == R.id.CallBtn) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(mActivity.getString(R.string.call_select_voice))
                            .setPositiveButton(mActivity.getString(R.string.Yes), (dialog, which) -> {
                                CallManager.callContact(mActivity, false, contactsModel.get_id());
                            }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();
                } else {

                    if (messageCopied != null && messageCopied.size() != 0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage(mActivity.getString(R.string.transfer_message_to) + " " + username)
                                .setPositiveButton(mActivity.getString(R.string.Yes), (dialog, which) -> {
                                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                                    // messagingIntent.putExtra("conversationID", "");
                                    messagingIntent.putExtra("recipientID", contactsModel.get_id());
                                    messagingIntent.putExtra("isGroup", false);
                                    messagingIntent.putExtra("messageCopied", messageCopied);
                                    mActivity.startActivity(messagingIntent);
                                    mActivity.finish();
                                }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();

                    } else if (filePathList != null && filePathList.size() != 0) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage(mActivity.getString(R.string.transfer_message_to) + " " + username)
                                .setPositiveButton(mActivity.getString(R.string.Yes), (dialog, which) -> {
                                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                                    //    messagingIntent.putExtra("conversationID", "");
                                    messagingIntent.putExtra("recipientID", contactsModel.get_id());
                                    messagingIntent.putExtra("isGroup", false);
                                    messagingIntent.putExtra("filePathList", filePathList);
                                    mActivity.startActivity(messagingIntent);
                                    mActivity.finish();
                                }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();

                    } else if (filePath != null) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage(mActivity.getString(R.string.transfer_message_to) + " " + username)
                                .setPositiveButton(mActivity.getString(R.string.Yes), (dialog, which) -> {
                                    Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                                    //  messagingIntent.putExtra("conversationID", "");
                                    messagingIntent.putExtra("recipientID", contactsModel.get_id());
                                    messagingIntent.putExtra("isGroup", false);
                                    messagingIntent.putExtra("filePath", filePath);
                                    mActivity.startActivity(messagingIntent);
                                    mActivity.finish();
                                }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();

                    }
                }
            });

        } catch (Exception e) {
            AppHelper.LogCat("contacts adapters " + e.getMessage());
        }

    }


    @Override
    public int getItemCount() {
        if (mContactsModel != null) return mContactsModel.size();
        return 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (mContactsModel.size() > pos) {

                return Character.toString(mContactsModel.get(pos).getDisplayed_name().charAt(0));


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
        @BindView(R.id.invite)
        TextView invite;

        @BindView(R.id.CallVideoBtn)
        AppCompatImageView CallVideoBtn;

        @BindView(R.id.CallBtn)
        AppCompatImageView CallBtn;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

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

        void showVideoButton() {
            CallVideoBtn.setVisibility(View.VISIBLE);
            CallBtn.setVisibility(View.VISIBLE);
        }

        void hideVideoButton() {
            CallVideoBtn.setVisibility(View.GONE);
            CallBtn.setVisibility(View.GONE);
        }

        void setUsername(String Username, String phone) {
            if (Username != null) {
                username.setText(Username);
            } else {
                String name = UtilsPhone.getContactName(phone);
                if (name != null) {
                    username.setText(name);
                } else {
                    username.setText(phone);
                }

            }

        }

        void setStatus(String Status) {
            status.setText(Status);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
            CallVideoBtn.setOnClickListener(listener);
            CallBtn.setOnClickListener(listener);
        }


    }


}
