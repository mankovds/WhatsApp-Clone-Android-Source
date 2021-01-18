package com.strolink.whatsUp.adapters.recyclerView.stories;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.stories.StoriesDetailsActivity;
import com.strolink.whatsUp.activities.stories.StoriesListActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;
import com.strolink.whatsUp.ui.stories.StoryView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 6/29/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private List<StoriesModel> storiesModelList;
    private RecyclerView storiesList;
    private AppCompatActivity mActivity;

    private StoriesHeaderModel storiesHeaderModel;

    public StoriesAdapter(AppCompatActivity mActivity, RecyclerView storiesList) {
        this.mActivity = mActivity;
        this.storiesList = storiesList;

    }

    public void setStoriesModelList(List<StoriesModel> storiesModelList) {
        this.storiesModelList = storiesModelList;
        notifyDataSetChanged();
    }

    public void setStoriesHeaderModel(StoriesHeaderModel storiesHeaderModel) {
        this.storiesHeaderModel = storiesHeaderModel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_header, parent, false);
            return new HeaderStoryViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stories, parent, false);
            return new StoryViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof StoryViewHolder) {
            StoriesModel item = getItem(position);
            StoryViewHolder storyViewHolder = (StoryViewHolder) holder;

            List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(item.get_id());
            if (storyModels.size() != 0)
                storyViewHolder.setImageStory(storyModels);

        } else if (holder instanceof HeaderStoryViewHolder) {

            HeaderStoryViewHolder headerStoryViewHolder = (HeaderStoryViewHolder) holder;
            if (StoriesController.getInstance().checkIfSingleStoryMineExist(PreferenceManager.getInstance().getID(mActivity))) {
                List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(PreferenceManager.getInstance().getID(mActivity));
                headerStoryViewHolder.setImageStory(storyModels);


                if (StoriesController.getInstance().checkIfStoryUploadExist(PreferenceManager.getInstance().getID(mActivity))) {
                    headerStoryViewHolder.story_status.setVisibility(View.VISIBLE);
                } else {
                    headerStoryViewHolder.story_status.setVisibility(View.GONE);
                }

            } else {
                if (getItemCount() != 0)
                    headerStoryViewHolder.setImageStory();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }


    public StoriesModel getItem(int position) {
        return storiesModelList.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        try {
            StoriesModel storiesModel = getItem(position);
            return storiesModel.getId(); ///to avoid blink recyclerview item when notify the adapter
        } catch (Exception e) {
            return position;
        }

    }

    @Override
    public int getItemCount() {
        if (storiesModelList != null)
            return storiesModelList.size() + 1;
        else
            return 1;
    }


    public void updateStoryItem(String storyId) {
        try {

            int arraySize = storiesModelList.size();
            for (int i = 0; i < arraySize; i++) {
                StoriesModel model = storiesModelList.get(i);
                if (storyId.equals(model.getId())) {
                    StoriesModel storiesModel = StoriesController.getInstance().getStoriesModel(storyId);
                    changeItemAtPosition(i, storiesModel);
                    if (i != 0)
                        MoveItemToPosition(i, 1);
                    break;
                }

            }

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, StoriesModel storiesModel) {
        storiesModelList.set(position, storiesModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        StoriesModel model = storiesModelList.remove(fromPosition);
        storiesModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        storiesList.scrollToPosition(fromPosition);
    }


    public void updateStoryMineItem(String storyId) {

        try {
            StoriesHeaderModel storiesHeaderModel = StoriesController.getInstance().getStoriesHeader(storyId);
            setStoriesHeaderModel(storiesHeaderModel);
        } catch (Exception e) {
            AppHelper.LogCat("updateStoryMineItem  Exception" + e);
        }
    }

    public void addStoryItem(String storyId) {

        try {

            StoriesModel storiesModel = StoriesController.getInstance().getStoriesModel(storyId);
            if (!isStoryExistInList(storiesModel.get_id())) {
                addStoryItem(1, storiesModel);
            }

        } catch (Exception e) {
            AppHelper.LogCat("addStoryItem  Exception" + e);
        }
    }

    private void addStoryItem(int position, StoriesModel storiesModel) {
        try {
            this.storiesModelList.add(position, storiesModel);
            notifyItemInserted(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);

        }
    }

    private boolean isStoryExistInList(String storyId) {
        int arraySize = storiesModelList.size();
        boolean exist = false;
        for (int i = 0; i < arraySize; i++) {
            StoriesModel model = storiesModelList.get(i);
            if (storyId.equals(model.getId())) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public void deleteStoryMineItem() {


        try {

            removeStoryItem(0);

        } catch (Exception e) {
            AppHelper.LogCat(e);
        }


    }

    public void removeStoryItem(int position) {
        try {
            storiesModelList.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }


    public class StoryViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.user_image)
        StoryView storyView;


        StoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {

                StoriesModel storiesModel = storiesModelList.get(getAdapterPosition() - 1);
                Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                a.putExtra("position", getAdapterPosition() - 1);
                a.putExtra("storyId", storiesModel.get_id());
                itemView.getContext().startActivity(a);

            });
        }


        public void setImageStory(List<StoryModel> storyModels) {
            storyView.setStoriesModels(storyModels);
        }

    }

    public class HeaderStoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_image)
        StoryView storyView;


        @BindView(R.id.empty_story_layout)
        FrameLayout empty_story_layout;

        @BindView(R.id.user_image_profile)
        AppCompatImageView user_image_profile;


        @BindView(R.id.story_status)
        AppCompatImageView story_status;

        Context context;

        public HeaderStoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
            itemView.setOnClickListener(v -> {

                if (StoriesController.getInstance().checkIfSingleStoryMineExist(PreferenceManager.getInstance().getID(mActivity))) {

                    if (StoriesController.getInstance().checkIfStoryUploadExist(PreferenceManager.getInstance().getID(mActivity)) || StoriesController.getInstance().checkIfSingleStoryWaitingExist(PreferenceManager.getInstance().getID(mActivity))) {

                        Intent a = new Intent(itemView.getContext(), StoriesListActivity.class);
                        itemView.getContext().startActivity(a);
                    } else {
                        Intent a = new Intent(mActivity, StoriesDetailsActivity.class);
                        a.putExtra("position", getAdapterPosition());
                        a.putExtra("storyId", storiesHeaderModel.get_id());
                        itemView.getContext().startActivity(a);
                    }
                } else {
                    FilesManager.capturePhoto(mActivity, AppConstants.PICK_CAMERA_GALLERY_STORY, true);
                }

            });

        }


        void setImageStory() {
            storyView.setVisibility(View.GONE);
            empty_story_layout.setVisibility(View.VISIBLE);


            UsersModel usersModel = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(context));


            if (usersModel != null && usersModel.getImage() != null) {
                DrawableImageViewTarget target = new DrawableImageViewTarget(user_image_profile) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        user_image_profile.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        user_image_profile.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        user_image_profile.setImageDrawable(placeholder);
                    }
                };

                GlideApp.with(mActivity.getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + usersModel.get_id() + "/" + usersModel.getImage()))
                        .signature(new ObjectKey(usersModel.getImage()))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.holder_user)
                        .error(R.drawable.holder_user)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {
                user_image_profile.setBackgroundDrawable(AppHelper.getDrawable(mActivity, R.drawable.holder_user));
            }

        }

        void setImageStory(List<StoryModel> storyModels) {
            if (storyModels.size() == 0) {
                storyView.setVisibility(View.GONE);
                empty_story_layout.setVisibility(View.VISIBLE);

                try {
                    UsersModel usersModel = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(context));

                    if (usersModel.getImage() != null) {
                        DrawableImageViewTarget target = new DrawableImageViewTarget(user_image_profile) {

                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                super.onResourceReady(resource, transition);
                                user_image_profile.setImageDrawable(resource);
                            }


                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                user_image_profile.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                user_image_profile.setImageDrawable(placeholder);
                            }
                        };

                        GlideApp.with(mActivity.getApplicationContext())
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + usersModel.get_id() + "/" + usersModel.getImage()))
                                .signature(new ObjectKey(usersModel.getImage()))
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.holder_user)
                                .error(R.drawable.holder_user)
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .into(target);
                    } else {
                        user_image_profile.setBackgroundDrawable(AppHelper.getDrawable(mActivity, R.drawable.holder_user));
                    }

                } catch (Exception e) {
                    AppHelper.LogCat("Exception " + e.getMessage());
                }
            } else {

                storyView.setVisibility(View.VISIBLE);
                empty_story_layout.setVisibility(View.GONE);
                if (storyModels.size() != 0)
                    storyView.setStoriesModels(storyModels);
            }

        }
    }


}