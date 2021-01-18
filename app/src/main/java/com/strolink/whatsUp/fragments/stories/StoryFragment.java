package com.strolink.whatsUp.fragments.stories;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.adapters.recyclerView.stories.StoryShowAdapter;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.UtilsTime;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideUrlHeaders;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.ui.RelativeTimeTextView;
import com.strolink.whatsUp.ui.dragView.DragListener;
import com.strolink.whatsUp.ui.dragView.DragToClose;
import com.strolink.whatsUp.ui.stories.CustomViewPager;
import com.strolink.whatsUp.ui.stories.StoryProgressView;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.ene.toro.PlayerSelector;
import im.ene.toro.widget.Container;

/**
 * Created by Abderrahim El imame on 7/18/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoryFragment extends DialogFragment implements StoryProgressView.StoryListener {


    private String storyId = null;


    @BindView(R.id.storyProgressView)
    StoryProgressView storyProgressView;


    @BindView(R.id.user_image)
    AppCompatImageView user_image;


    @BindView(R.id.username)
    EmojiTextView username;


    @BindView(R.id.story_date)
    RelativeTimeTextView story_date;


    @BindView(R.id.top_layout)
    LinearLayout top_layout;

    private CustomViewPager customViewPager;
    private int counter = 0;
    int position = 0;
    List<StoryModel> storiesModels = new ArrayList<>();


    public List<StoriesModel> storyModels;

    Animation slideUpAnimation, slideDownAnimation;
    public StoriesHeaderModel storiesHeaderModels;

    public StoryFragment() {
        // Required empty constructor
    }

    @BindView(R.id.story_container)
    Container storyContainer;

    private DragToClose dragToClose;

    private PlayerSelector selector = PlayerSelector.DEFAULT; // visible to user by default.
    private final Handler handler = new Handler(); // post a delay due to the visibility change

    private LinearLayoutManager layoutManager;
    private StoryShowAdapter mStoryShowAdapter;
    private StoryProgressView.StoryStateListener storyStateListener;
    private View view;

    public void setStoryStateListener(StoryProgressView.StoryStateListener storyStateListener) {
        this.storyStateListener = storyStateListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        position = getArguments().getInt("position");
        storyId = getArguments().getString("storyId");
        view = inflater.inflate(R.layout.fragment_stories_swipe, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        customViewPager = getActivity().findViewById(R.id.viewpager);
        dragToClose = getActivity().findViewById(R.id.drag_to_close);
        initView();
        return view;
    }


    private void initView() {


        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        storyContainer.setLayoutManager(layoutManager);

        playStories();
        storyContainer.setPlayerSelector(null);
        handler.postDelayed(() -> {
            if (storyContainer != null) storyContainer.setPlayerSelector(selector);
        }, 500);

        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {
                AppHelper.LogCat("onStartDraggingView()");

            }

            @Override
            public void onDraggingView(float offset) {
                storyProgressView.setAlpha(offset);
                user_image.setAlpha(offset);
                username.setAlpha(offset);
                story_date.setAlpha(offset);
                storyContainer.setAlpha(offset);
            }

            @Override
            public void onViewClosed() {
                AppHelper.LogCat("onViewClosed()");
            }
        });

    }

    @OnClick(R.id.backBtn)
    public void back() {
        getActivity().finish();
    }

    public void playStories() {

        storyModels = StoriesController.getInstance().getAllStoriesModelList();


        if (storyId.equals(PreferenceManager.getInstance().getID(getActivity()))) {
            storiesHeaderModels = StoriesController.getInstance().getStoriesHeader(storyId);


            storiesModels = StoriesController.getInstance().getAllStoryNotDeleted(storiesHeaderModels.get_id());

        } else {
            storyId = storyModels.get(position).get_id();

            storiesModels = StoriesController.getInstance().getAllStoryNotDeleted(storyModels.get(position).get_id());

        }

        mStoryShowAdapter = new StoryShowAdapter(storiesModels, storyProgressView, this);
        storyContainer.setAdapter(mStoryShowAdapter);
        storyProgressView.setStoriesCount(storiesModels.size());

        // or
        // statusView.setStoriesCountWithDurations(statusResourcesDuration);

        storyProgressView.setStoryListener(this);
        if (storyId.equals(PreferenceManager.getInstance().getID(getActivity()))) {

            if (getArguments().get("currentStoryPosition") != null) {
                counter = getArguments().getInt("currentStoryPosition");
                storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));

                storyProgressView.playStories(counter);
                storyContainer.scrollToPosition(counter);
            } else {
                if (StoriesController.getInstance().checkIfStoryDownloadExist(storyId)) {

                    int storiesModelsSize = StoriesController.getInstance().getStoryDownloadSize(storyId);

                    if (storiesModelsSize < storiesModels.size()) {
                        counter = storiesModelsSize;
                        storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                        storyProgressView.playStories(counter);
                        storyContainer.scrollToPosition(counter);
                    } else {
                        storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                        storyProgressView.playStories(0);
                    }
                } else {
                    storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                    storyProgressView.playStories(0);
                }
            }


            List<StoryModel> storiesList = StoriesController.getInstance().getAllStoryNotDeleted(storiesHeaderModels.get_id());
            setUserInfo(storiesHeaderModels.getUserImage(), storiesList.get(counter).getDate(), getString(R.string.my_story));
        } else {
            if (StoriesController.getInstance().checkIfStoryDownloadExist(storyId)) {

                int storiesModelsSize = StoriesController.getInstance().getStoryDownloadSize(storyId);
                if (storiesModelsSize < storiesModels.size()) {
                    counter = storiesModelsSize;
                    storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                    storyProgressView.playStories(storiesModelsSize);

                    storyContainer.scrollToPosition(counter);
                } else {
                    storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                    storyProgressView.playStories(0);
                }
            } else {
                storyProgressView.setStoryDuration(Long.parseLong(storiesModels.get(counter).getDuration()));
                storyProgressView.playStories(0);
            }

            List<StoryModel> storyModelsList = StoriesController.getInstance().getAllStoryNotDeleted(storyModels.get(position).get_id());

            setUserInfo(storyModels.get(position).getUserImage(), storyModelsList.get(counter).getDate(), storyModels.get(position).getUsername());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.PICK_REPLY_STORY) {
                storyProgressView.resume();
                if (storyStateListener != null) storyStateListener.onResume();
            }
        }
    }


    private void setUserInfo(String ImageUrl, String date, String name) {
        try {
            if (ImageUrl != null) {

                DrawableImageViewTarget target = new DrawableImageViewTarget(user_image) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        user_image.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        user_image.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        user_image.setImageDrawable(placeholder);
                    }
                };

                GlideApp.with(WhatsCloneApplication.getInstance())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + storyId + "/" + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .dontAnimate()
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.holder_user)
                        .error(R.drawable.holder_user)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {

                DrawableImageViewTarget target = new DrawableImageViewTarget(user_image) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        user_image.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        user_image.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        user_image.setImageDrawable(placeholder);
                    }
                };

                GlideApp.with(WhatsCloneApplication.getInstance())
                        .load(R.drawable.holder_user)
                        .dontAnimate()
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.holder_user)
                        .error(R.drawable.holder_user)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            }
            username.setText(name);
            DateTime previousTs = UtilsTime.getCorrectDate(date);
            story_date.setReferenceTime(previousTs.getMillis());


        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }
    }


    @Override
    public void onNext() {
        AppHelper.LogCat("onNext counter " + counter);
        storyProgressView.pause();
        ++counter;
        storyContainer.scrollToPosition(counter);
        if (storyId.equals(PreferenceManager.getInstance().getID(getActivity()))) {

            List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(storiesHeaderModels.get_id());

            setUserInfo(storiesHeaderModels.getUserImage(), storyModels.get(counter).getDate(), getString(R.string.my_story));
        } else {
            setUserInfo(storyModels.get(position).getUserImage(), storiesModels.get(counter).getDate(), storyModels.get(position).getUsername());
        }


    }

    @Override
    public void onPrev() {
        AppHelper.LogCat("onPrev counter " + counter);

        if (storyId.equals(PreferenceManager.getInstance().getID(getActivity()))) {

            if (counter <= 0) {
                storyProgressView.pause();
                --counter;
                if (customViewPager.getCurrentItem() > 0) {
                    customViewPager.setCurrentItem(customViewPager.getCurrentItem() - 1, true);
                } else {
                    back();
                }
            } else {
                storyProgressView.pause();
                --counter;
                storyContainer.scrollToPosition(counter);

                List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(storiesHeaderModels.get_id());
                setUserInfo(storiesHeaderModels.getUserImage(), storyModels.get(counter).getDate(), getString(R.string.my_story));
            }
        } else {
            if (counter <= 0) {
                storyProgressView.pause();
                --counter;
                if (customViewPager.getCurrentItem() > 0) {
                    customViewPager.setCurrentItem(customViewPager.getCurrentItem() - 1, true);
                } else {
                    back();
                }
            } else {
                if (storyProgressView != null) {
                    storyProgressView.pause();
                    --counter;
                    storyContainer.scrollToPosition(counter);
                    setUserInfo(storyModels.get(position).getUserImage(), storiesModels.get(counter).getDate(), storyModels.get(position).getUsername());

                }
            }
        }
    }

    @Override
    public void onComplete() {
        AppHelper.LogCat("onComplete " + counter);


        if (storyId.equals(PreferenceManager.getInstance().getID(getActivity()))) {
            back();
        } else {
            if (customViewPager.getCurrentItem() < storyModels.size() - 1) {
                storyProgressView.destroy();
                customViewPager.setCurrentItem(customViewPager.getCurrentItem() + 1, true);
            } else {
                back();
            }
        }
    }


    @Override
    public void onDestroy() {
        // Very important !
        if (storyProgressView != null)
            storyProgressView.destroy();
        super.onDestroy();
        //  AppHelper.LogCat("onDestroy " + position);
        EventBus.getDefault().unregister(this);
    }

    /**
     * method of EventBus
     *
     * @param object this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(String object) {
        if (object.equals("hideTopPanel")) {
            fadeOutAnimation(top_layout, 100);
            fadeOutAnimation(storyProgressView, 100);

        } else if (object.equals("showTopPanel")) {
            fadeInAnimation(top_layout, 100);
            fadeInAnimation(storyProgressView, 100);
        }
    }

    public static void fadeOutAnimation(final View view, long animationDuration) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(animationDuration);
        fadeOut.setDuration(animationDuration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);

    }

    public static void fadeInAnimation(final View view, long animationDuration) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(animationDuration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }

    @Override
    public void onPause() {
        super.onPause();
        //     AppHelper.LogCat("onPause " + position);
    }

    @Override
    public void onResume() {
        super.onResume();
        //   AppHelper.LogCat("onResume " + position);
       /* if (open_reply.getVisibility() == View.GONE) {
            open_reply.setVisibility(View.VISIBLE);
            storyProgressView.resume();
            if (storyStateListener != null) storyStateListener.onResume();
        }
*/
    }


    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        layoutManager = null;
        mStoryShowAdapter = null;
        selector = null;
        if (storyProgressView != null)
            storyProgressView.destroy();
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            selector = PlayerSelector.DEFAULT;
        } else {
            selector = PlayerSelector.NONE;
        }
        // Using TabLayout has a downside: once we click to a tab to change page, there will be no animation,
        // which will cause our setup doesn't work well. We need a delay to make things work.
        if (storyContainer != null) storyContainer.setPlayerSelector(selector);

    }

}
