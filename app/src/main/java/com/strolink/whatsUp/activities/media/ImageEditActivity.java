package com.strolink.whatsUp.activities.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.BaseActivity;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.fragments.editor.CropFragment;
import com.strolink.whatsUp.fragments.editor.PhotoEditorFragment;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.backup.DbBackupRestore;
import com.strolink.whatsUp.helpers.PreferenceManager;
import com.strolink.whatsUp.helpers.phone.UtilsPhone;
import com.strolink.whatsUp.jobs.files.PendingFilesTask;
import com.strolink.whatsUp.models.users.Pusher;
import com.strolink.whatsUp.presenters.controllers.StoriesController;
import com.strolink.whatsUp.presenters.controllers.UsersController;

import org.greenrobot.eventbus.EventBus;


public class ImageEditActivity extends BaseActivity implements PhotoEditorFragment.OnFragmentInteractionListener, CropFragment.OnFragmentInteractionListener {
    private Rect cropRect;
    private boolean forStory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_edit);

        String imagePath = getIntent().getStringExtra(AppConstants.MediaConstants.EXTRA_IMAGE_PATH);
        forStory = getIntent().getBooleanExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, false);
        if (imagePath != null) {

            addFragment(this, R.id.fragment_container,
                    PhotoEditorFragment.newInstance(imagePath));
        }
    }

    @Override
    public void onCropClicked(Bitmap bitmap) {
        addFragment(this, R.id.fragment_container, CropFragment.newInstance(bitmap, cropRect));
    }

    private String getStoryId(String userId) {
        try {
            StoriesHeaderModel storiesHeaderModel = StoriesController.getInstance().getStoriesHeader(userId);
            return storiesHeaderModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("Get storyId id Exception  " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onDoneClicked(String imagePath, String message) {
        if (imagePath == null) {
            AppHelper.CustomToast(getApplicationContext(), getString(R.string.oops_something));
        } else {
            AppHelper.LogCat("imagePath " + imagePath);
            AppHelper.LogCat("message " + message);
            if (forStory) {

                try {
                    String storyId = getStoryId(PreferenceManager.getInstance().getID(this));
                    if (storyId == null) {

                        String lastID = DbBackupRestore.getStoryLastId();


                        UsersModel storyOwner = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(this));


                        StoryModel storyModel = new StoryModel();
                        storyModel.set_id(lastID);
                        storyModel.setUserId(PreferenceManager.getInstance().getID(this));
                        storyModel.setDate(AppHelper.getCurrentTime());
                        storyModel.setDownloaded(true);
                        storyModel.setUploaded(false);
                        storyModel.setDeleted(false);
                        storyModel.setStatus(AppConstants.IS_WAITING);
                        storyModel.setFile(imagePath);
                        storyModel.setBody(message);

                        storyModel.setType("image");
                        storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));

                        StoriesController.getInstance().insertStoryModel(storyModel);
                        StoriesHeaderModel storiesHeaderModel = new StoriesHeaderModel();
                        storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));


                        String name = UtilsPhone.getContactName(storyOwner.getPhone());
                        if (name != null) {
                            storiesHeaderModel.setUsername(name);
                        } else {
                            storiesHeaderModel.setUsername(storyOwner.getPhone());
                        }

                        storiesHeaderModel.setUserImage(storyOwner.getImage());
                        storiesHeaderModel.setDownloaded(true);

                        StoriesController.getInstance().insertStoriesHeaderModel(storiesHeaderModel);


                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW, PreferenceManager.getInstance().getID(this)));

                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initUploadListener(lastID);

                    } else {
                        String lastID = DbBackupRestore.getStoryLastId();

                        try {


                            UsersModel storyOwner = UsersController.getInstance().getUserById(PreferenceManager.getInstance().getID(this));


                            StoryModel storyModel = new StoryModel();
                            storyModel.set_id(lastID);
                            storyModel.setUserId(storyId);
                            storyModel.setDate(AppHelper.getCurrentTime());
                            storyModel.setDownloaded(true);
                            storyModel.setUploaded(false);
                            storyModel.setDeleted(false);
                            storyModel.setStatus(AppConstants.IS_WAITING);
                            storyModel.setFile(imagePath);
                            storyModel.setBody(message);
                            storyModel.setType("image");
                            storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));

                            StoriesController.getInstance().insertStoryModel(storyModel);

                            StoriesHeaderModel storiesHeaderModel = StoriesController.getInstance().getStoriesHeader(storyId);

                            storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));
                            storiesHeaderModel.setUsername(storyOwner.getUsername());
                            storiesHeaderModel.setUserImage(storyOwner.getImage());
                            storiesHeaderModel.setDownloaded(true);


                            StoriesController.getInstance().updateStoriesHeaderModel(storiesHeaderModel);


                        } catch (Exception e) {
                            AppHelper.LogCat("Exception  last id  " + e.getMessage());
                        }
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(this)));


                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initUploadListener(lastID);
                    }

                } finally {

                    finish();
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
                }


            } else {

                Intent intent = new Intent();
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITED_PATH, imagePath);
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE, message);
                setResult(Activity.RESULT_OK, intent);

                finish();
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
            }
        }

    }

    @Override
    public void onImageCropped(Bitmap bitmap, Rect cropRect) {
        this.cropRect = cropRect;
        PhotoEditorFragment photoEditorFragment = (PhotoEditorFragment) getFragmentByTag(this, PhotoEditorFragment.class.getSimpleName());
        if (photoEditorFragment != null) {
            photoEditorFragment.setImageWithRect(bitmap);
            photoEditorFragment.reset();
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));

        }
    }

    @Override
    public void onCancelCrop() {
        removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
    }

    @Override
    public void onBackPressed() {
        if (getFragmentByTag(this, CropFragment.class.getSimpleName()) != null) {
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
        } else {
            super.onBackPressed();
        }
    }


    public void addFragment(AppCompatActivity activity, int contentId, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        transaction.add(contentId, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }

    public void removeFragment(AppCompatActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }


    public Fragment getFragmentByTag(AppCompatActivity appCompatActivity, String tag) {
        return appCompatActivity.getSupportFragmentManager().findFragmentByTag(tag);
    }
}
