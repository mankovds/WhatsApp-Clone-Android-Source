package com.strolink.whatsUp.adapters.recyclerView.messages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.messages.MessagesActivity;
import com.strolink.whatsUp.activities.profile.ProfilePreviewActivity;
import com.strolink.whatsUp.animations.RevealAnimation;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.glide.GlideRequests;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.models.UpdateConversationRow;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.MessagesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.vanniktech.emoji.EmojiTextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
@SuppressLint("StaticFieldLeak")
public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ConversationModel> conversationModelList;


    private GlideRequests glideRequests;
    private String SearchQuery;
    private RecyclerView conversationList;
    public ConversationViewHolder conversationViewHolder;

    private SparseBooleanArray selectedItems;

    private boolean isActivated = false;

    public ConversationsAdapter(RecyclerView conversationList, GlideRequests glideRequests) {
        this.conversationModelList = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();
        this.conversationList = conversationList;
        this.glideRequests = glideRequests;
    }

    public ConversationsAdapter(GlideRequests glideRequests) {
        this.conversationModelList = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();
        this.glideRequests = glideRequests;
    }


    public void setConversations(List<ConversationModel> conversationsModelList) {
        this.conversationModelList = conversationsModelList;
        notifyDataSetChanged();
    }


    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<ConversationModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ConversationModel> newModels) {
        int arraySize = conversationModelList.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final ConversationModel model = conversationModelList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ConversationModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final ConversationModel model = newModels.get(i);
            if (!conversationModelList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ConversationModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final ConversationModel model = newModels.get(toPosition);
            final int fromPosition = indexFor(conversationModelList, model.getId());
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private ConversationModel removeItem(int position) {
        final ConversationModel model = conversationModelList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, ConversationModel model) {
        conversationModelList.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final ConversationModel model = conversationModelList.remove(fromPosition);
        conversationModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    //Methods for search end
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            AppHelper.LogCat("position " + position);
            if (payloads.get(0) instanceof UpdateConversationRow && holder instanceof ConversationViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((UpdateConversationRow) payloads.get(0)).getMember_name());
                ((ConversationViewHolder) holder).updateUserStatus(((UpdateConversationRow) payloads.get(0)).getStatus(), ((UpdateConversationRow) payloads.get(0)).isGroup(), ((UpdateConversationRow) payloads.get(0)).getMember_name());
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new ConversationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        try {
            conversationViewHolder = (ConversationViewHolder) holder;

            Activity mActivity = (Activity) conversationViewHolder.itemView.getContext();
            ConversationModel conversationsModel = getItem(position);

            if (conversationsModel.isIs_group()) {

                if (conversationsModel.getGroup_name() != null) {
                    String groupName = UtilsString.unescapeJava(conversationsModel.getGroup_name());
                    // conversationViewHolder.setUsername(groupName);
                    SpannableString recipientUsername = SpannableString.valueOf(groupName);
                    String last_message = UtilsString.unescapeJava(conversationsModel.getLatest_message());
                    // conversationViewHolder.setUsername(groupName);
                    SpannableString last_message_span = SpannableString.valueOf(conversationsModel.getLatest_message());
                    if (SearchQuery == null) {
                        conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
                        conversationViewHolder.lastMessage.setText(last_message_span, TextView.BufferType.NORMAL);
                    } else {
                        int index = TextUtils.indexOf(groupName.toLowerCase(), SearchQuery.toLowerCase());
                        if (index >= 0) {
                            recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);

                        int index_last_message = TextUtils.indexOf(last_message.toLowerCase(), SearchQuery.toLowerCase());
                        if (index_last_message >= 0) {
                            last_message_span.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index_last_message, index_last_message + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            last_message_span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index_last_message, index_last_message + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        conversationViewHolder.lastMessage.setText(last_message_span, TextView.BufferType.SPANNABLE);
                    }
                }

                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));

                if (conversationsModel.getFile_type() != null && !conversationsModel.getFile_type().equals("null")) {
                    switch (conversationsModel.getFile_type()) {
                        case AppConstants.MESSAGES_IMAGE:

                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            if (conversationsModel.getLatest_message_latitude() != null && !conversationsModel.getLatest_message_latitude().equals("null")) {
                                conversationViewHolder.setTypeFile("location");
                            } else {
                                conversationViewHolder.setTypeFile("image");
                            }


                            break;
                        case AppConstants.MESSAGES_GIF:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("gif");
                            break;
                        case AppConstants.MESSAGES_VIDEO:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("video");
                            break;
                        case AppConstants.MESSAGES_AUDIO:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("audio");
                            break;
                        case AppConstants.MESSAGES_DOCUMENT:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("document");
                            break;
                    }
                } else {

                    conversationViewHolder.FileContent.setVisibility(View.GONE);
                    conversationViewHolder.lastMessage.setVisibility(View.VISIBLE);

                    switch (conversationsModel.getLatest_message_state()) {
                        case AppConstants.CREATE_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_created_this_group));


                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_created_this_group));

                            }


                            break;
                        case AppConstants.LEFT_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_left));
                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_left));


                            }

                            break;

                        case AppConstants.REMOVE_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_removed_this_group));
                            } else {

                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_removed_this_group));


                            }

                            break;

                        case AppConstants.ADD_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_added_this_group));
                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_added_this_group));


                            }

                            break;

                        case AppConstants.ADMIN_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_make_admin_this_group));
                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_make_admin_this_group));
                            }

                            break;

                        case AppConstants.MEMBER_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_make_member_this_group));
                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_make_member_this_group));
                            }

                            break;

                        case AppConstants.EDITED_STATE:
                            if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                                conversationViewHolder.setLastMessage(mActivity.getString(R.string.you_edited_this_group));
                            } else {
                                conversationViewHolder.setLastMessage("" + conversationsModel.getLatest_message_sender__displayed_name() + " " + mActivity.getString(R.string.he_edited_this_group));

                            }

                            break;
                        default:

                            conversationViewHolder.FileContent.setVisibility(View.GONE);
                            conversationViewHolder.setLastMessage(conversationsModel.getLatest_message());
                            break;
                    }

                }


                if (conversationsModel.getLatest_message_created() != null) {
                    conversationViewHolder.setMessageDate(conversationsModel.getLatest_message_created());
                }


                conversationViewHolder.setGroupImage(conversationsModel.getGroup_image(), conversationsModel.getGroup_id(), conversationsModel.getGroup_name());


                if (conversationsModel.getLatest_message_state().equals(AppConstants.NORMAL_STATE)) {
                    if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        conversationViewHolder.showSent(conversationsModel.getLatest_message_status());
                    } else {
                        conversationViewHolder.hideSent();
                    }
                }else {
                    conversationViewHolder.hideSent();
                }
                if (conversationsModel.getUnread_message_counter() != 0) {

                    conversationViewHolder.ChangeStatusUnread();
                    conversationViewHolder.showCounter();

                    if (conversationsModel.getUnread_message_counter() > 99)
                        conversationViewHolder.setCounter(mActivity.getString(R.string.plus_99));
                    else
                        conversationViewHolder.setCounter(String.valueOf(conversationsModel.getUnread_message_counter()));

                } else {
                    conversationViewHolder.ChangeStatusRead();
                    conversationViewHolder.hideCounter();

                }


            } else {
                String username = conversationsModel.getOwner_displayed_name();


                conversationViewHolder.setUserImage(conversationsModel.getOwner_image(), conversationsModel.getOwner_id(), username);
                SpannableString recipientUsername = SpannableString.valueOf(username);


                String last_message = UtilsString.unescapeJava(conversationsModel.getLatest_message());
                SpannableString last_message_span = SpannableString.valueOf(last_message);

                if (SearchQuery == null) {
                    conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
                    conversationViewHolder.lastMessage.setText(last_message_span, TextView.BufferType.NORMAL);
                } else {
                    int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    conversationViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);


                    int index_last_message = TextUtils.indexOf(last_message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index_last_message >= 0) {
                        last_message_span.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index_last_message, index_last_message + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        last_message_span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index_last_message, index_last_message + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    conversationViewHolder.lastMessage.setText(last_message_span, TextView.BufferType.SPANNABLE);
                }

                conversationViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
                if (conversationsModel.getFile_type() != null && !conversationsModel.getFile_type().equals("null")) {
                    switch (conversationsModel.getFile_type()) {
                        case AppConstants.MESSAGES_IMAGE:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            if (conversationsModel.getLatest_message_latitude() != null && !conversationsModel.getLatest_message_latitude().equals("null")) {
                                conversationViewHolder.setTypeFile("location");
                            } else {
                                conversationViewHolder.setTypeFile("image");
                            }
                            break;
                        case AppConstants.MESSAGES_GIF:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("gif");
                            break;
                        case AppConstants.MESSAGES_VIDEO:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("video");
                            break;
                        case AppConstants.MESSAGES_AUDIO:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("audio");
                            break;
                        case AppConstants.MESSAGES_DOCUMENT:
                            conversationViewHolder.lastMessage.setVisibility(View.GONE);
                            conversationViewHolder.setTypeFile("document");
                            break;
                    }
                } else {

                    conversationViewHolder.FileContent.setVisibility(View.GONE);
                    conversationViewHolder.lastMessage.setVisibility(View.VISIBLE);
                    conversationViewHolder.setLastMessage(conversationsModel.getLatest_message());
                }

                if (conversationsModel.getLatest_message_created() != null) {
                    conversationViewHolder.setMessageDate(conversationsModel.getLatest_message_created());
                } else {
                    conversationViewHolder.setMessageDate(conversationsModel.getCreated());
                }


                if (conversationsModel.getLatest_message_sender_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    conversationViewHolder.showSent(conversationsModel.getLatest_message_status());
                } else {
                    conversationViewHolder.hideSent();
                }


                if (conversationsModel.getUnread_message_counter() != 0) {
                    conversationViewHolder.ChangeStatusUnread();
                    conversationViewHolder.showCounter();
                    if (conversationsModel.getUnread_message_counter() > 99)
                        conversationViewHolder.setCounter(mActivity.getString(R.string.plus_99));
                    else
                        conversationViewHolder.setCounter(String.valueOf(conversationsModel.getUnread_message_counter()));

                } else {
                    conversationViewHolder.ChangeStatusRead();
                    conversationViewHolder.hideCounter();

                }


            }

            conversationViewHolder.setOnClickListener(view -> {
                if (!isActivated) {
                    if (conversationsModel.isIs_group()) {
                        if (view.getId() == R.id.user_image) {
                            if (AppHelper.isAndroid5()) {

                                //calculates the center of the View v you are passing
                                int revealX = (int) (conversationViewHolder.userImage.getX() + conversationViewHolder.userImage.getWidth() / 2);
                                int revealY = (int) (conversationViewHolder.userImage.getY() + conversationViewHolder.userImage.getHeight() / 2);
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("conversationID", conversationsModel.get_id());
                                mIntent.putExtra("groupID", conversationsModel.getGroup_id());
                                mIntent.putExtra("isGroup", true);
                                //  mIntent.putExtra("userId", conversationsModel.getOwner().getId());
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_X, revealX);
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y, revealY);
                                // mActivity.startActivity(mIntent);

                                //just start the activity as an shared transition, but set the options bundle to null
                                ActivityCompat.startActivity(mActivity, mIntent, null);
                            } else {
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("conversationID", conversationsModel.get_id());
                                mIntent.putExtra("groupID", conversationsModel.getGroup_id());
                                mIntent.putExtra("isGroup", true);
                                // mIntent.putExtra("userId", conversationsModel.getOwner().getId());
                                mActivity.startActivity(mIntent);
                                mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                            }
                        } else {

                            Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                            messagingIntent.putExtra("conversationID", conversationsModel.get_id());
                            messagingIntent.putExtra("groupID", conversationsModel.getGroup_id());
                            messagingIntent.putExtra("isGroup", true);/*
                        messagingIntent.putExtra("recipientID", conversationsModel.getOwner().getId());*/
                            mActivity.startActivity(messagingIntent);

                        }
                        // }

                    } else {
                        if (view.getId() == R.id.user_image) {

                            if (AppHelper.isAndroid5()) {

                                //calculates the center of the View v you are passing
                                int revealX = (int) (conversationViewHolder.userImage.getX() + conversationViewHolder.userImage.getWidth() / 2);
                                int revealY = (int) (conversationViewHolder.userImage.getY() + conversationViewHolder.userImage.getHeight() / 2);
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", conversationsModel.getOwner_id());
                                mIntent.putExtra("isGroup", false);
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_X, revealX);
                                mIntent.putExtra(RevealAnimation.EXTRA_CIRCULAR_REVEAL_Y, revealY);
                                //mActivity.startActivity(mIntent);

                                //just start the activity as an shared transition, but set the options bundle to null
                                ActivityCompat.startActivity(mActivity, mIntent, null);
                            } else {
                                Intent mIntent = new Intent(mActivity, ProfilePreviewActivity.class);
                                mIntent.putExtra("userID", conversationsModel.getOwner_id());
                                mIntent.putExtra("isGroup", false);
                                mActivity.startActivity(mIntent);
                                mActivity.overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
                            }
                        } else {

                            Intent messagingIntent = new Intent(mActivity, MessagesActivity.class);
                            messagingIntent.putExtra("conversationID", conversationsModel.get_id());
                            messagingIntent.putExtra("recipientID", conversationsModel.getOwner_id());
                            messagingIntent.putExtra("isGroup", false);
                            mActivity.startActivity(messagingIntent);


                        }
                    }
                }


            });

            holder.itemView.setActivated(selectedItems.get(position, false));

            if (holder.itemView.isActivated() && getSelectedItemCount() > 0) {

                final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_enter);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        conversationViewHolder.selectIcon.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                conversationViewHolder.selectIcon.startAnimation(animation);
            } else {

                if (getSelectedItemCount() > 0) {

                    final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_exit);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            conversationViewHolder.selectIcon.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    conversationViewHolder.selectIcon.startAnimation(animation);
                } else {
                    conversationViewHolder.selectIcon.setVisibility(View.GONE);
                }

            }


        } catch (Exception e) {
            AppHelper.LogCat("Exception adapter conversation " + e.getMessage());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        try {
            ConversationModel conversationsModel = getItem(position);
            return conversationsModel.getId(); ///to avoid blink recyclerview item when notify the adapter
        } catch (Exception e) {
            return position;
        }

    }


    @Override
    public int getItemCount() {
        if (conversationModelList != null)
            return conversationModelList.size();
        else
            return 0;
    }


/*    @Override
    public void onViewRecycled(@NonNull ConversationViewHolder holder) {
        super.onViewRecycled(holder);
        glideRequests.clear(holder.userImage);

    }*/

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {

            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
            if (!isActivated)
                isActivated = true;

        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        if (isActivated)
            isActivated = false;
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public boolean groupExist() {
        boolean exist = false;
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            if (conversationModelList.get(i).isIs_group()) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public ConversationModel getItem(int position) {
        return conversationModelList.get(position);
    }


    public void addConversationItem(String conversationId) {


        ConversationModel conversationsModel = MessagesController.getInstance().getChatById(conversationId);


        if (conversationsModel != null)
            if (!isConversationExistInList(conversationsModel)) {
                addConversationItem(0, conversationsModel);
            }

    }

    private boolean isConversationExistInList(ConversationModel conversationModel) {
        return indexFor(conversationModelList, conversationModel.getId()) != -1;
    }

    private void addConversationItem(int position, ConversationModel conversationsModel) {
        // if (position != 0) {
        try {
            this.conversationModelList.add(conversationsModel);
            notifyItemInserted(0);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        // }
    }

    public void removeConversationItem(int position) {
        //if (position != 0) {
        try {
            conversationModelList.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
        //  }
    }

    public void DeleteConversationItem(String ConversationID) {


        ConversationModel conversationsModel = MessagesController.getInstance().getChatById(ConversationID);
        int position = indexFor(conversationModelList, conversationsModel.getId());


        if (position == -1) return;
        removeConversationItem(position);


    }

    public void updateStatusConversationItem(String ConversationID) {


        ConversationModel conversationsModel = MessagesController.getInstance().getChatById(ConversationID);
        if (conversationsModel == null) return;

        int position = indexFor(conversationModelList, conversationsModel.getId());
        if (position == -1) return;
        changeItemAtPosition(position, conversationsModel);

    }


    private int indexFor(List<ConversationModel> array, long id) {
        int position = -1;
        try {

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getId() == id) {
                    position = i;
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
            return -1;
        }

        return position;
    }


    public void updateConversationItem(String ConversationID) {


        ConversationModel conversationsModel = MessagesController.getInstance().getChatById(ConversationID);
        if (conversationsModel == null) return;
        int position = indexFor(conversationModelList, conversationsModel.getId());


        if (position == -1) return;
        changeItemAtPosition(position, conversationsModel);
        if (position != 0)
            MoveItemToPosition(position, 0);


    }

    private void changeItemAtPosition(int position, ConversationModel conversationsModel) {
        conversationModelList.set(position, conversationsModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        ConversationModel model = conversationModelList.remove(fromPosition);
        conversationModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        conversationList.smoothScrollToPosition(fromPosition);
    }


    public void updateUserStatus(int statusUser, String recipientId, String groupId, boolean is_group) {


        if (is_group) {


            ConversationModel conversationsModel = MessagesController.getInstance().getChatByGroupId(groupId);
            if (conversationsModel == null) return;
            int position = indexFor(conversationModelList, conversationsModel.getId());
            if (position == -1) return;
            ConversationModel model = getItem(position);
            if (groupId.equals(model.getGroup_id())) {


                UsersModel contactsModel = UsersController.getInstance().getUserById(recipientId);

                UpdateConversationRow updateConversationRow = new UpdateConversationRow();
                updateConversationRow.setStatus(statusUser);
                updateConversationRow.setGroup(true);
                updateConversationRow.setMember_name(contactsModel.getDisplayed_name());


                conversationList.post(() -> notifyItemChanged(position, updateConversationRow));


            }
        } else {

            ConversationModel conversationsModel = MessagesController.getInstance().getChatByUserId(recipientId);
            if (conversationsModel == null) return;
            int position = indexFor(conversationModelList, conversationsModel.getId());
            if (position == -1) return;
            ConversationModel model = getItem(position);
            if (recipientId.equals(model.getOwner_id())) {

                UpdateConversationRow updateConversationRow = new UpdateConversationRow();
                updateConversationRow.setStatus(statusUser);
                updateConversationRow.setGroup(false);
                updateConversationRow.setMember_name(null);

                conversationList.post(() -> notifyItemChanged(position, updateConversationRow));

            }
        }


    }


    class ConversationViewHolder extends RecyclerView.ViewHolder {

        Context context;
        @BindView(R.id.user_image)
        AppCompatImageView userImage;


        @BindView(R.id.username)
        EmojiTextView username;

        @BindView(R.id.last_message)
        EmojiTextView lastMessage;

        @BindView(R.id.counter)
        AppCompatTextView counter;

        @BindView(R.id.date_message)
        AppCompatTextView messageDate;

        @BindView(R.id.status_messages)
        AppCompatImageView status_messages;

        @BindView(R.id.online_icon)
        AppCompatImageView online_icon;

        @BindView(R.id.offline_icon)
        AppCompatImageView offline_icon;

        @BindView(R.id.file_types_text)
        AppCompatTextView FileContent;

        @BindView(R.id.status_user)
        AppCompatTextView statusUser;

        @BindView(R.id.create_group_pro_bar)
        ProgressBar progressBarGroup;

        @BindView(R.id.conversation_row)
        LinearLayout ConversationRow;


        @BindView(R.id.select_icon)
        AppCompatImageView selectIcon;

        ConversationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            username.setSelected(true);
            context = itemView.getContext();


        }

        private void showStatus() {
            statusUser.setVisibility(View.VISIBLE);
            messageDate.setVisibility(View.GONE);
        }

        private void hideStatus() {
            statusUser.setVisibility(View.GONE);
            messageDate.setVisibility(View.VISIBLE);
        }

        void updateUserStatus(int statusUserTyping, boolean is_group, String memberName) {

            if (is_group) {
                offline_icon.setVisibility(View.GONE);
                online_icon.setVisibility(View.GONE);
                if (statusUserTyping == AppConstants.STATUS_USER_TYPING) {
                    showStatus();
                    if (memberName.length() <= 5)
                        statusUser.setText(String.format("%s %s", memberName, context.getString(R.string.isTyping)));
                    else
                        statusUser.setText(String.format("%s %s", memberName.substring(0, 5), context.getString(R.string.isTyping)));
                    statusUser.setTextColor(AppHelper.getColor(context, R.color.colorAccent));
                } else {
                    hideStatus();
                    statusUser.setText("");
                }
            } else {
                AppHelper.LogCat("statusUserTyping " + statusUserTyping);

                switch (statusUserTyping) {
                    case AppConstants.STATUS_USER_TYPING:
                        showStatus();
                        statusUser.setText(context.getString(R.string.isTyping));
                        statusUser.setTextColor(AppHelper.getColor(context, R.color.colorAccent));
                        break;
                    case AppConstants.STATUS_USER_STOP_TYPING:
                        hideStatus();
                        statusUser.setText("");
                        break;
                    case AppConstants.STATUS_USER_CONNECTED:
                        offline_icon.setVisibility(View.GONE);
                        online_icon.setVisibility(View.VISIBLE);
                        break;
                    case AppConstants.STATUS_USER_DISCONNECTED:
                        online_icon.setVisibility(View.GONE);
                        offline_icon.setVisibility(View.VISIBLE);
                        break;
                    default:
                        offline_icon.setVisibility(View.GONE);
                        online_icon.setVisibility(View.GONE);
                        hideStatus();
                        statusUser.setText("");
                        break;

                }

            }
        }

        void getProgressBarGroup() {
            progressBarGroup.setVisibility(View.VISIBLE);
            ConversationRow.setEnabled(false);
        }

        void setProgressBarGroup() {
            progressBarGroup.setVisibility(View.GONE);
            ConversationRow.setEnabled(true);
        }

        @SuppressLint("SetTextI18n")
        void setTypeFile(String type) {
            FileContent.setVisibility(View.VISIBLE);
            FileContent.setVisibility(View.VISIBLE);
            Drawable img;
            FileContent.setCompoundDrawablePadding(2);
            switch (type) {
                case "image":
                    img = AppHelper.getDrawable(context, R.drawable.ic_photo_camera_gra_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

                    FileContent.setText(R.string.conversation_row_image);
                    break;
                case "location":

                    img = AppHelper.getDrawable(context, R.drawable.ic_location_gray_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

                    FileContent.setText(R.string.conversation_row_location);
                    break;
                case "gif":

                    img = AppHelper.getDrawable(context, R.drawable.ic_gif_gray_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    FileContent.setText(R.string.conversation_row_gif);
                    break;
                case "video":
                    img = AppHelper.getDrawable(context, R.drawable.ic_videocam_gray_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    FileContent.setText(R.string.conversation_row_video);
                    break;
                case "audio":
                    img = AppHelper.getDrawable(context, R.drawable.ic_headset_gray_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    FileContent.setText(R.string.conversation_row_audio);
                    break;
                case "document":
                    img = AppHelper.getDrawable(context, R.drawable.ic_document_file_gray_24dp);
                    FileContent.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    FileContent.setText(R.string.conversation_row_document);
                    break;
            }

        }


        void setGroupImage(String ImageUrl, String groupId, String name) {


            Drawable drawable = AppHelper.getDrawable(context, R.drawable.holder_group);

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
                        userImage.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                glideRequests
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .dontAnimate()
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
                        userImage.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };
                glideRequests
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .dontAnimate()
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

        void setUsername(String user) {
            username.setText(user);

        }

        void setLastMessage(String LastMessage) {
            lastMessage.setVisibility(View.VISIBLE);
            lastMessage.setTextColor(AppHelper.getColor(context, R.color.colorGray2));
            String last = UtilsString.unescapeJava(LastMessage);
            if (last.length() > 18)
                lastMessage.setText(String.format("%s... ", last.substring(0, 18)));
            else
                lastMessage.setText(last);

        }

        @SuppressLint("CheckResult")
        void setMessageDate(String MessageDate) {
            DateTime messageDat = UtilsTime.getCorrectDate(MessageDate);
            messageDate.setText(UtilsTime.convertDateToStringFormat(context, messageDat));
        }

        void hideSent() {
            status_messages.setVisibility(View.GONE);
        }

        void showSent(int status) {

            status_messages.setVisibility(View.VISIBLE);
            switch (status) {
                case AppConstants.IS_WAITING:
                    status_messages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                    break;
                case AppConstants.IS_SENT:
                    status_messages.setImageResource(R.drawable.ic_done_gray_24dp);
                    break;
                case AppConstants.IS_DELIVERED:
                    status_messages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                    break;
                case AppConstants.IS_SEEN:
                    status_messages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                    break;

            }

        }

        void setCounter(String Counter) {
            counter.setText(Counter.toUpperCase());
        }

        void hideCounter() {
            counter.setVisibility(View.GONE);
        }


        void showCounter() {
            counter.setVisibility(View.VISIBLE);
        }

        void ChangeStatusUnread() {/*
            messageDate.setTypeface(R.font.roboto, Typeface.BOLD);
            username.setTypeface(null, Typeface.BOLD);
            lastMessage.setTypeface(null, Typeface.BOLD);*/
            messageDate.setTextColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
            lastMessage.setTextColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
        }

        void ChangeStatusRead() {/*
            messageDate.setTypeface(context.getResources().getFont(R.font.roboto), Typeface.NORMAL);
            username.setTypeface(context.getResources().getFont(R.font.roboto), Typeface.BOLD);
            lastMessage.setTypeface(null, Typeface.NORMAL);*/
            messageDate.setTextColor(ContextCompat.getColor(context, R.color.colorGray2));
            lastMessage.setTextColor(ContextCompat.getColor(context, R.color.colorGray2));
            username.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        }

        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
        }

    }


}
