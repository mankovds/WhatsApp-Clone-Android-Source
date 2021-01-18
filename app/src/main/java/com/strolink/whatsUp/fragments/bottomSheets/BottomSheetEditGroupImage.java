package com.strolink.whatsUp.fragments.bottomSheets;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.images.PickerBuilder;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.permissions.Permissions;
import com.strolink.whatsUp.presenters.groups.EditGroupPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class BottomSheetEditGroupImage extends BottomSheetDialogFragment {

    private View mView;
    @BindView(R.id.cameraBtn)
    FrameLayout cameraBtn;
    @BindView(R.id.galleryBtn)
    FrameLayout galleryBtn;
    private EditGroupPresenter mEditGroupPresenter;

    @Override
    public void onStart() {
        super.onStart();


    }

    private void setGalleryBtn() {

        dismiss();
        new PickerBuilder((AppCompatActivity) getActivity(), PickerBuilder.SELECT_FROM_GALLERY)
                .setOnImageReceivedListener(imageUri -> {
                    Intent data = new Intent();
                    data.setData(imageUri);
                    ///   AppHelper.LogCat("new image SELECT_FROM_GALLERY" + imageUri);
                    mEditGroupPresenter.onActivityResult(AppConstants.SELECT_PROFILE_PICTURE, RESULT_OK, data);

                })
                .setImageName(getActivity().getString(R.string.app_name))
                .setImageFolderName(getActivity().getString(R.string.app_name))
                .setCropScreenColor(R.color.colorPrimary)
                .withTimeStamp(false)
                .setOnPermissionRefusedListener(() -> {
                    //PermissionHandler.requestPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
                })
                .start();


    }

    protected void sendToExternalApp() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Uri mProcessingPhotoUri;
        mProcessingPhotoUri = FilesManager.getImageFile(getActivity());
        if (mProcessingPhotoUri != null)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mProcessingPhotoUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getActivity().startActivityForResult(cameraIntent, AppConstants.SELECT_PROFILE_CAMERA);
    }


    private void setCameraBtn() {
        dismiss();
        new PickerBuilder((AppCompatActivity) getActivity(), PickerBuilder.SELECT_FROM_CAMERA)
                .setOnImageReceivedListener(imageUri -> {
                    AppHelper.LogCat("new image SELECT_FROM_CAMERA " + imageUri);
                    Intent data = new Intent();
                    data.setData(imageUri);
                    mEditGroupPresenter.onActivityResult(AppConstants.SELECT_PROFILE_CAMERA, RESULT_OK, data);

                })
                .setImageName(getActivity().getString(R.string.app_name))
                .setImageFolderName(getActivity().getString(R.string.app_name))
                .setCropScreenColor(R.color.colorPrimary)
                .withTimeStamp(false)
                .setOnPermissionRefusedListener(() -> {
                    //PermissionHandler.requestPermission(getActivity(), Manifest.permission.CAMERA);
                })
                .start();

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.content_bottom_sheet, container, false);
        ButterKnife.bind(this, mView);
        mEditGroupPresenter = new EditGroupPresenter(this);
        galleryBtn.setOnClickListener(v -> {

            Permissions.with(getActivity())
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .ifNecessary()
                    .withRationaleDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                            R.drawable.ic_gallery_white_24dp)
                    .withPermanentDenialDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information))
                    .onAllGranted(this::setGalleryBtn)
                    .onAnyDenied(() -> {
                        Toast.makeText(getActivity(), getString(R.string.all_permission_required), Toast.LENGTH_LONG).show();

                    })
                    .execute();

        });

        cameraBtn.setOnClickListener(v -> {

            Permissions.with(getActivity())
                    .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .ifNecessary()
                    .withRationaleDialog(getString(R.string.to_capture_photos_and_video_allow_appp_access_to_the_camera), R.drawable.ic_photo_camera_white_24dp)
                    .withPermanentDenialDialog(getString(R.string.appp_needs_the_camera_permission_to_take_photos_or_video))
                    .onAllGranted(this::setCameraBtn)
                    .onAnyDenied(() -> Toast.makeText(getActivity(), R.string.appp_needs_camera_permissions_to_take_photos_or_video, Toast.LENGTH_LONG).show())
                    .execute();
        });

        return mView;
    }

    @Override
    public void onViewCreated(View contentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(contentView, savedInstanceState);
        initView();
    }

    public void initView() {

    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.content_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        int height = ((View) contentView.getParent()).getHeight() / 2;
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).addBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setPeekHeight(300);
            ((BottomSheetBehavior) behavior).setHideable(true);
        }

    }


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {

            switch (newState) {
                case BottomSheetBehavior.STATE_DRAGGING:
                    AppHelper.LogCat("state Dragging");
                    break;

                case BottomSheetBehavior.STATE_SETTLING:
                    AppHelper.LogCat("state Settling");
                    break;

                case BottomSheetBehavior.STATE_COLLAPSED:
                    AppHelper.LogCat("state Collapsed");

                    break;

                case BottomSheetBehavior.STATE_HIDDEN:
                    dismiss();
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    AppHelper.LogCat("state expended");

                    break;
            }


        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            AppHelper.LogCat("onSlide");
            bottomSheet.setNestedScrollingEnabled(false);
        }
    };

}