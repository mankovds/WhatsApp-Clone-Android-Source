package com.strolink.whatsUp.adapters.recyclerView.contacts;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.activities.profile.ProfilePreviewActivity;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.animations.RevealAnimation;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.cache.ImageLoader;
import com.strolink.whatsUp.helpers.RateHelper;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.ui.RecyclerViewFastScroller;
import com.vanniktech.emoji.EmojiTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private List<UsersModel> contactList;

    private String SearchQuery;

    private boolean isFragment;

    public ContactsAdapter(boolean isFragment) {

        this.contactList = new ArrayList<>();
        this.isFragment = isFragment;
    }


    public void setContacts(List<UsersModel> contactsModelList) {
        this.contactList = contactsModelList;

        notifyDataSetChanged();
    }

    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<UsersModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<UsersModel> newModels) {
        int arraySize = contactList.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final UsersModel model = contactList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final UsersModel model = newModels.get(i);
            if (!contactList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final UsersModel model = newModels.get(toPosition);
            final int fromPosition = contactList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private UsersModel removeItem(int position) {
        final UsersModel model = contactList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, UsersModel model) {
        contactList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final UsersModel model = contactList.remove(fromPosition);
        contactList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_contacts, parent, false);
        return new ContactsViewHolder(itemView);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof ContactsViewHolder) {
            ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            Activity context = (Activity) holder.itemView.getContext();

            try {
                UsersModel contactsModel = this.contactList.get(position);
                String username;

                String name = UtilsPhone.getContactName(contactsModel.getPhone());
                if (name != null) {
                    username = name;
                } else {
                    username = contactsModel.getPhone();
                }


                contactsViewHolder.setUsername(username);


                SpannableString recipientUsername = SpannableString.valueOf(username);
                if (SearchQuery == null) {
                    contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
                } else {
                    int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(context, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
                }
                if (contactsModel.getStatus() != null) {
                    String status = UtilsString.unescapeJava(contactsModel.getStatus().getBody());
                    contactsViewHolder.setStatus(status);

                } else {
                    contactsViewHolder.setStatus(contactsModel.getPhone());
                }

                if (contactsModel.isLinked() && contactsModel.isActivate()) {
                    contactsViewHolder.hideInviteButton();
                } else {
                    contactsViewHolder.showInviteButton();
                }

                contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);


                contactsViewHolder.setOnClickListener(view -> {
                    if (view.getId() == R.id.user_image) {
                        RateHelper.significantEvent(context);
                        //  if (!contactsModel.isValid()) return;
                        if (AppHelper.isAndroid5()) {

                            //calculates the center of the View v you are passing
                            int revealX = (int) (contactsViewHolder.userImage.getX() + contactsViewHolder.userImage.getWidth() / 2);
                            int revealY = (int) (contactsViewHolder.userImage.getY() + contactsViewHolder.userImage.getHeight() / 2);
                            if (contactsModel.isLinked()) {

                                Intent mIntent = new Intent(context, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", contactsModel.get_id());
                                mIntent.putExtra("isGroup", false);
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_X, revealX);
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y, revealY);
                                context.startActivity(mIntent);
                            }
                        } else {
                            if (contactsModel.isLinked()) {
                                Intent mIntent = new Intent(context, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", contactsModel.get_id());
                                mIntent.putExtra("isGroup", false);
                                context.startActivity(mIntent);
                                context.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                            }
                        }

                    } else {
                        RateHelper.significantEvent(context);
                        // if (!contactsModel.isValid()) return;
                        if (contactsModel.isLinked() && contactsModel.isActivate()) {
                            Intent messagingIntent = new Intent(context, MessagesActivity.class);
                            //  messagingIntent.putExtra("conversationID", "");
                            messagingIntent.putExtra("recipientID", contactsModel.get_id());
                            messagingIntent.putExtra("isGroup", false);
                            context.startActivity(messagingIntent);
                            if (!isFragment)
                                context.finish();
                            AnimationsUtil.setTransitionAnimation(context);
                        } else {
                            String number = contactsModel.getPhone();
                            contactsViewHolder.setShareApp(context.getString(R.string.invitation_from) + " " + number);
                        }
                    }

                });
            } catch (Exception e) {
                AppHelper.LogCat("Contacts adapters Exception " + e.getMessage());
            }

        }


    }

    private void changeItemAtPosition(int position, UsersModel contactsModel) {
        contactList.set(position, contactsModel);
        notifyItemChanged(position);
    }


    @Override
    public int getItemCount() {
        return contactList.size() > 0 ? contactList.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (contactList.size() > pos) {
                return Character.toString(contactList.get(pos).getDisplayed_name().charAt(0));

            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public UsersModel getItem(int position) {
        return contactList.get(position);
    }


    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        Context context;
        @BindView(R.id.user_image)
        AppCompatImageView userImage;


        @BindView(R.id.username)
        EmojiTextView username;
        @BindView(R.id.status)
        EmojiTextView status;
        @BindView(R.id.invite)
        TextView invite;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);
            context = itemView.getContext();

        }


        void setShareApp(String subject) {

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.INVITE_MESSAGE_SMS + String.format(context.getString(R.string.rate_helper_google_play_url), context.getPackageName()));
            shareIntent.setType("text/*");
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.shareItem)));
        }


        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId, String name) {

            Drawable drawable = AppHelper.getDrawable(context, R.drawable.holder_user);
            if (ImageUrl != null) {
                BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        userImage.setImageBitmap(resource);


                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(ImageUrl));
                        } catch (IOException ex) {
                            // AppHelper.LogCat(e.getMessage());
                        }
                        if (bitmap != null) {
                            ImageLoader.SetBitmapImage(bitmap, userImage);
                        } else {
                            userImage.setImageDrawable(errorDrawable);
                        }
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        userImage.setImageDrawable(placeholder);
                    }
                };
                if (ImageUrl.startsWith("content:")) {

                    GlideApp.with(context.getApplicationContext())
                            .asBitmap()
                            .load(ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                } else {

                    GlideApp.with(context.getApplicationContext())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }
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
            status.setText(Status);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }


}
