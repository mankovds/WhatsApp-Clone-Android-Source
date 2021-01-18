package com.strolink.whatsUp.adapters.recyclerView.stories;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.cache.ImageLoader;
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
public class PrivacyContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private List<UsersModel> usersModelList;
    private String SearchQuery;

    private SparseBooleanArray selectedItems;

    public boolean isSelectedAll = false;


    public PrivacyContactsAdapter() {
        this.selectedItems = new SparseBooleanArray();
        this.usersModelList = new ArrayList<>();

    }

    public List<UsersModel> getUsersModelList() {
        return usersModelList;
    }

    public void setUsersModelList(List<UsersModel> usersModelList) {
        this.usersModelList = usersModelList;
        notifyDataSetChanged();
    }

    public int indexFor(List<UsersModel> array, String id) {
        int position = -1;
        try {

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).get_id().equals(id)) {
                    position = i;
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            return -1;
        }

        return position;
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
        int arraySize = usersModelList.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final UsersModel model = usersModelList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final UsersModel model = newModels.get(i);
            if (!usersModelList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final UsersModel model = newModels.get(toPosition);
            final int fromPosition = usersModelList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private UsersModel removeItem(int position) {
        final UsersModel model = usersModelList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, UsersModel model) {
        usersModelList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final UsersModel model = usersModelList.remove(fromPosition);
        usersModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_privacy_contacts, parent, false);
        return new ContactsViewHolder(itemView);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof ContactsViewHolder) {
            ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            Activity context = (Activity) holder.itemView.getContext();

            UsersModel contactsModel = this.usersModelList.get(position);
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

            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);


            if (!isSelectedAll) {
                contactsViewHolder.privacy_check_box.setChecked(selectedItems.get(position, false));
            } else {

                if (contactsViewHolder.privacy_check_box.isChecked()) {
                    contactsViewHolder.privacy_check_box.setChecked(false);
                    isSelectedAll = false;
                } else {
                    toggleSelectionAll(position);
                    contactsViewHolder.privacy_check_box.setChecked(true);
                }

            }


        }


    }

    private void changeItemAtPosition(int position, UsersModel contactsModel) {
        usersModelList.set(position, contactsModel);
        notifyItemChanged(position);
    }


    public List<UsersModel> getUnSelectedItem() {
        ArrayList<UsersModel> usersModelList = new ArrayList<>();
        for (int x = 0; x < this.usersModelList.size(); x++) {
            if (!exist(x)) {
                usersModelList.add(this.usersModelList.get(x));
            }

        }
        return usersModelList;
    }

    public boolean exist(int pos) {
        return selectedItems.get(pos, false);
    }

    public void toggleSelection(int pos) {
        if (isSelectedAll)
            isSelectedAll = false;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);

        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }


    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public int getItemCount() {
        return usersModelList.size() > 0 ? usersModelList.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (usersModelList.size() > pos) {
                String name = UtilsPhone.getContactName(usersModelList.get(pos).getPhone());
                if (name != null) {
                    return Character.toString(name.charAt(0));
                } else {
                    return Character.toString(usersModelList.get(pos).getPhone().charAt(0));
                }

            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public UsersModel getItem(int position) {
        return usersModelList.get(position);
    }

    public void checkAll() {
        //if (!isSelectedAll) {

        clearSelectionsAll();
        isSelectedAll = true;
        notifyDataSetChanged();
        // }

    }

    public void clearSelectionsAll() {
        selectedItems.clear();
    }

    private void toggleSelectionAll(int pos) {
        if (!selectedItems.get(pos, false)) {
            selectedItems.put(pos, true);
        }


    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        Context context;
        @BindView(R.id.user_image)
        AppCompatImageView userImage;


        @BindView(R.id.username)
        EmojiTextView username;

        @BindView(R.id.privacy_check_box)
        AppCompatCheckBox privacy_check_box;


        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

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


        void setUsername(String phone) {
            username.setText(phone);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
    /*        privacy_check_box.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                clearSelections();

            });*/
        }

    }


}
