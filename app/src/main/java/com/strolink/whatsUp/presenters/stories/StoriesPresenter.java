package com.strolink.whatsUp.presenters.stories;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.main.MainActivity;
import com.strolink.whatsUp.activities.stories.StoriesListActivity;
import com.strolink.whatsUp.activities.stories.StoriesSeenListActivity;
import com.strolink.whatsUp.api.APIHelper;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.fragments.home.StoriesFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.interfaces.Presenter;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.StoriesController;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.strolink.whatsUp.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;

/**
 * Created by Abderrahim El imame on 12/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesPresenter implements Presenter {

    private StoriesListActivity storiesListActivity;
    private StoriesSeenListActivity storiesSeenListActivity;
    private StoriesFragment storiesFragment;
    private MainActivity mainActivity;


    private CompositeDisposable mDisposable;
    private String storyId;

    public StoriesPresenter(StoriesSeenListActivity storiesSeenListActivity, String storyId) {
        this.storiesSeenListActivity = storiesSeenListActivity;

        mDisposable = new CompositeDisposable();
        this.storyId = storyId;
    }

    public StoriesPresenter(StoriesListActivity storiesListActivity) {
        this.storiesListActivity = storiesListActivity;

        mDisposable = new CompositeDisposable();
    }

    public StoriesPresenter(StoriesFragment storiesFragment) {
        this.storiesFragment = storiesFragment;

        mDisposable = new CompositeDisposable();
    }

    public StoriesPresenter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        mDisposable = new CompositeDisposable();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (storiesListActivity != null) {

            getStories();
        } else if (storiesFragment != null) {
            getAllStories();
        } else if (mainActivity != null) {

            getAllStories();
        } else {

            getSeenList(storyId);

        }
    }

    private void getAllStories() {
        if (mainActivity != null) {

            mDisposable.addAll(APIHelper.initialApiUsersContacts()
                            .getMineStories()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(storiesHeaderModel -> {
                                // mainActivity.onHideLoading();


                                mainActivity.UpdateStories(storiesHeaderModel);


                            }, throwable -> {
                                AppHelper.LogCat("throwable " + throwable.getMessage());
                                // mainActivity.onHideLoading();
                                //   mainActivity.onErrorLoading(throwable);
                            }),
                    APIHelper.initialApiUsersContacts()
                            .getAllStories()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(storiesModels -> {
                                // mainActivity.onHideLoading();
                                AppHelper.LogCat("storiesModels " + storiesModels.size());

                                mainActivity.UpdateStories(storiesModels);


                            }, throwable -> {
                                AppHelper.LogCat("throwable " + throwable.getMessage());
                                // mainActivity.onHideLoading();
                                //   mainActivity.onErrorLoading(throwable);
                            }));

        } else {

            mDisposable.add(APIHelper.initialApiUsersContacts()
                    .getAllStories()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(storiesModels -> {
               /* StoriesHeaderModel storiesHeaderModel = APIHelper.initialApiUsersContacts().getMineStories();
                storiesFragment.onHideLoading();

                    storiesFragment.UpdateStories(storiesModels, storiesHeaderModel);*/


                    }, throwable -> {
                        storiesFragment.onHideLoading();
                        storiesFragment.onErrorLoading(throwable);
                    }))
            ;
        }
    }

    private void getSeenList(String storyId) {

        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getSeenList(storyId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(seenList -> {
                    AppHelper.LogCat("seenList "+seenList.size());
                    AppHelper.LogCat("seenList "+seenList.toString());
                    storiesSeenListActivity.onHideLoading();
                    storiesSeenListActivity.showSeenList(seenList);
                }, throwable -> {
                    storiesSeenListActivity.onHideLoading();
                    storiesSeenListActivity.onErrorLoading(throwable);
                }))
        ;
    }

    private void getStories() {

        mDisposable.add(APIHelper.initialApiUsersContacts()
                .getStories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(storyModels -> {
                    storiesListActivity.onHideLoading();
                    storiesListActivity.showStories(storyModels);
                }, throwable -> {
                    storiesListActivity.onHideLoading();
                    storiesListActivity.onErrorLoading(throwable);
                }))
        ;
    }

    public void deleteStory(StoryModel storyModel, int currentPosition1) {
        String storyId = storyModel.get_id();
        storiesListActivity.storiesListAdapter.removeStoryItem(currentPosition1);
        if (storyModel.getStatus() == AppConstants.IS_WAITING) {


            StoryModel storyModel1 = StoriesController.getInstance().getStoryById(storyId);

            storyModel1.setDeleted(true);
            StoriesController.getInstance().updateStoryModel(storyModel);

            AppHelper.LogCat("story deleted successfully  ");

            List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(PreferenceManager.getInstance().getID(storiesListActivity));

            if (storyModels.size() == 0) {

                storiesListActivity.finish();
                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));

            } else {
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(storiesListActivity)));
            }
        } else {

            AppHelper.LogCat("delete story testing  ");
            mDisposable.add(APIHelper.initialApiUsersContacts().deleteStory(storyModel.get_id()).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {


                    StoryModel storyModel1 = StoriesController.getInstance().getStoryById(storyId);
                    storyModel1.setDeleted(true);
                    StoriesController.getInstance().updateStoryModel(storyModel1);


                    AppHelper.LogCat("story deleted successfully  ");

                    List<StoryModel> storyModels = StoriesController.getInstance().getAllStoryNotDeleted(PreferenceManager.getInstance().getID(storiesListActivity));

                    if (storyModels.size() == 0) {

                        storiesListActivity.finish();
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));

                    } else {
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(storiesListActivity)));
                    }
                } else {
                    AppHelper.LogCat("delete story failed  " + statusResponse.getMessage());
                    AppHelper.CustomToast(storiesListActivity, storiesListActivity.getString(R.string.oops_something));
                }


            }, throwable -> {
                AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                AppHelper.CustomToast(storiesListActivity, storiesListActivity.getString(R.string.oops_something));
            }));
        }

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {
        if (storiesFragment != null) {

            getAllStories();
        } else if (mainActivity != null) {

            getAllStories();
        }
    }

    @Override
    public void onStop() {

    }


}
