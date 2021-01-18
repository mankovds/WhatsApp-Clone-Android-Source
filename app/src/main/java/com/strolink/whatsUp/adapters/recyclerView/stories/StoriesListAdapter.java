package com.strolink.whatsUp.adapters.recyclerView.stories;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.stories.StoriesDetailsActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.jobs.WorkJobsManager;
import com.strolink.whatsUp.jobs.files.PendingFilesTask;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.ui.RelativeTimeTextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 7/10/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<StoryModel> storyModelList;
    private RecyclerView storiesList;
    private AppCompatActivity mActivity;

    private SparseBooleanArray selectedItems;
    private boolean isActivated = false;

    public StoriesListAdapter(AppCompatActivity mActivity, RecyclerView storiesList) {
        this.mActivity = mActivity;
        this.storiesList = storiesList;
        this.selectedItems = new SparseBooleanArray();
    }

    public void setStoryModelList(List<StoryModel> storyModelList) {
        this.storyModelList = storyModelList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_my_story, parent, false);
        return new StoryViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        StoryModel item = getItem(position);
        StoryViewHolder storyViewHolder = (StoryViewHolder) holder;

        storyViewHolder.setStory_image(item.getFile(), item.getType());
        DateTime previousTs = UtilsTime.getCorrectDate(item.getDate());
        storyViewHolder.getStory_date().setReferenceTime(previousTs.getMillis());
        int seen_counter = StoriesController.getInstance().getAllSeenStoryCounter(item.get_id());
        storyViewHolder.getSeen_counter().setText(String.valueOf(seen_counter));

        if (!StoriesController.getInstance().checkIfStoryUploadExist(PreferenceManager.getInstance().getID(mActivity), item.get_id())) {
            storyViewHolder.getRetry_upload_story().setVisibility(View.GONE);
            storyViewHolder.getSeen_story_layout().setVisibility(View.VISIBLE);
        } else {

            storyViewHolder.getRetry_upload_story().setVisibility(View.VISIBLE);
            storyViewHolder.getSeen_story_layout().setVisibility(View.GONE);
        }

        holder.itemView.setActivated(selectedItems.get(position, false));

        if (holder.itemView.isActivated() && getSelectedItemCount() > 0) {

            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_enter);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    storyViewHolder.selectIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            storyViewHolder.selectIcon.startAnimation(animation);
        } else {

            if (getSelectedItemCount() > 0) {

                final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_exit);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        storyViewHolder.selectIcon.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                storyViewHolder.selectIcon.startAnimation(animation);
            } else {
                storyViewHolder.selectIcon.setVisibility(View.GONE);
            }

        }

    }


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


    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public StoryModel getItem(int position) {
        return storyModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        try {
            StoryModel storyModel = getItem(position);
            return storyModel.getId(); ///to avoid blink recyclerview item when notify the adapter
        } catch (Exception e) {
            return position;
        }

    }

    @Override
    public int getItemCount() {
        return  storyModelList != null ? storyModelList.size() : 0;
    }


    public void updateStoryItem(String storyId) {
        try {

            int arraySize = storyModelList.size();
            for (int i = 0; i < arraySize; i++) {
                StoryModel model = storyModelList.get(i);
                if (storyId.equals(model.getId())) {
                    StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
                    changeItemAtPosition(i, storyModel);
                    if (i != 0)
                        MoveItemToPosition(i, 1);
                    break;
                }

            }

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, StoryModel storyModel) {
        storyModelList.set(position, storyModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        StoryModel model = storyModelList.remove(fromPosition);
        storyModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        storiesList.scrollToPosition(fromPosition);
    }


    public void addStoryItem(String storyId) {
        try {

            StoryModel storyModel = StoriesController.getInstance().getStoryById(storyId);
            if (!isStoryExistInList(storyModel.get_id())) {
                addStoryItem(1, storyModel);
            } else {
                return;
            }


        } catch (Exception e) {
            AppHelper.LogCat("addStoryItem  Exception" + e);
        }
    }

    private void addStoryItem(int position, StoryModel storyModel) {
        try {
            this.storyModelList.add(position, storyModel);
            notifyItemInserted(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);

        }
    }

    private boolean isStoryExistInList(String storyId) {
        int arraySize = storyModelList.size();
        boolean exist = false;
        for (int i = 0; i < arraySize; i++) {
            StoryModel model = storyModelList.get(i);
            if (storyId.equals(model.getId())) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public void removeStoryItem(int position) {
        try {
            storyModelList.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    public class StoryViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.story_image)
        AppCompatImageView story_image;


        @BindView(R.id.seen_counter)
        AppCompatTextView seen_counter;


        @BindView(R.id.story_date)
        RelativeTimeTextView story_date;

        @BindView(R.id.retry_upload_story)
        LinearLayout retry_upload_story;

        @BindView(R.id.seen_story_layout)
        LinearLayout seen_story_layout;

        @BindView(R.id.select_icon)
        AppCompatImageView selectIcon;

        StoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                if (isActivated) return;
                StoryModel storyModel = storyModelList.get(getAdapterPosition());

                if (StoriesController.getInstance().checkIfStoryUploadExist(PreferenceManager.getInstance().getID(mActivity), storyModel.get_id())) {
                    // Create the task, set the listener, add to the task controller, and run
                    PendingFilesTask.initUploadListener(storyModel.get_id());

                } else {
                    if (StoriesController.getInstance().checkIfSingleStoryWaitingExist(storyModel.get_id())) {
                        WorkJobsManager.getInstance().sendSingleStoryToServerWorker(storyModel.getId());
                    } else {

                        AppHelper.LogCat("checkIfStoryWatingExist");
                        Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                        a.putExtra("position", 0);
                        a.putExtra("currentStoryPosition", getAdapterPosition());
                        a.putExtra("storyId", storyModel.getUserId());
                        itemView.getContext().startActivity(a);
                    }
                }

            });
            retry_upload_story.setOnClickListener(v -> {
                if (isActivated) return;

                StoryModel storyModel = storyModelList.get(getAdapterPosition());

                // Create the task, set the listener, add to the task controller, and run
                PendingFilesTask.initUploadListener(storyModel.get_id());

            });
        }


        public LinearLayout getRetry_upload_story() {
            return retry_upload_story;
        }

        public LinearLayout getSeen_story_layout() {
            return seen_story_layout;
        }

        public void setStory_image(String ImageUrl, String type) {

            Drawable drawable = AppHelper.getDrawable(mActivity, R.drawable.bg_circle_image_holder);

            BitmapImageViewTarget target = new BitmapImageViewTarget(story_image) {

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    story_image.setImageBitmap(resource);


                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    story_image.setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadStarted(@Nullable Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    story_image.setImageDrawable(placeholder);
                }
            };
            if (type.equals("image")) {

                if (ImageUrl.startsWith("/storage")) {

                    GlideApp.with(mActivity.getApplicationContext())
                            .asBitmap()
                            .load(ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                } else {
                    GlideApp.with(mActivity.getApplicationContext())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }
            } else {

                long interval = 5000 * 1000;
                RequestOptions options = new RequestOptions().frame(interval);

                if (ImageUrl.startsWith("/storage")) {

                    GlideApp.with(mActivity.getApplicationContext())
                            .asBitmap()
                            .load(ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .apply(RequestOptions.circleCropTransform())
                            .apply(options)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                } else {
                    GlideApp.with(mActivity.getApplicationContext())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .apply(RequestOptions.circleCropTransform())
                            .apply(options)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }
            }

        }

        public AppCompatImageView getStory_image() {
            return story_image;
        }

        public AppCompatTextView getSeen_counter() {
            return seen_counter;
        }

        public RelativeTimeTextView getStory_date() {
            return story_date;
        }
    }


}