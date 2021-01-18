package com.strolink.whatsUp.fragments.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.media.ImageEditActivity;
import com.strolink.whatsUp.adapters.recyclerView.picker.FilterImageAdapter;
import com.strolink.whatsUp.animations.AnimationsUtil;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.helpers.editorHelpers.FilterHelper;
import com.strolink.whatsUp.helpers.editorHelpers.FilterTouchListener;
import com.strolink.whatsUp.helpers.editorHelpers.Matrix3;
import com.strolink.whatsUp.helpers.editorHelpers.filter.ApplyFilterTask;
import com.strolink.whatsUp.helpers.editorHelpers.filter.GetFiltersTask;
import com.strolink.whatsUp.helpers.editorHelpers.filter.ProcessingImage;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.models.ThumbnailFilter;
import com.strolink.whatsUp.ui.editor.PhotoEditorView;
import com.strolink.whatsUp.ui.editor.VerticalSlideColorPicker;
import com.strolink.whatsUp.ui.editor.ViewTouchListener;
import com.strolink.whatsUp.ui.editor.imagezoom.ImageViewTouch;
import com.strolink.whatsUp.ui.views.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PhotoEditorFragment extends Fragment
        implements View.OnClickListener, ViewTouchListener,
        FilterImageAdapter.FilterImageAdapterListener, InputGeneralPanel.Listener {

    @BindView(R.id.image_iv)
    ImageViewTouch mainImageView;

    @BindView(R.id.crop_btn)
    AppCompatImageView cropButton;

    @BindView(R.id.stickers_btn)
    AppCompatImageView stickerButton;

    @BindView(R.id.add_text_btn)
    AppCompatImageView addTextButton;

    @BindView(R.id.photo_editor_view)
    PhotoEditorView photoEditorView;

    @BindView(R.id.paint_btn)
    AppCompatImageView paintButton;

    @BindView(R.id.delete_view)
    AppCompatImageView deleteButton;

    @BindView(R.id.eraseButton)
    AppCompatImageView eraseButton;

    @BindView(R.id.color_picker_view)
    VerticalSlideColorPicker colorPickerView;

    @BindView(R.id.toolbar_layout)
    View toolbarLayout;

    @BindView(R.id.filter_list_rv)
    RecyclerView filterRecylerview;

    @BindView(R.id.filter_list_layout)
    View filterLayout;

    @BindView(R.id.filter_label)
    View filterLabel;


    @BindView(R.id.send_buttonn)
    AppCompatImageButton doneBtn;


    @BindView(R.id.fragment_photo_editor_layout)
    View fragment_photo_editor_layout;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EmojiEditText composeText;


    private EmojiPopup emojiPopup;
    private Unbinder unbinder;

    private Bitmap mainBitmap;
    private int filterLayoutHeight;
    private OnFragmentInteractionListener mListener;
    public static final int MODE_NONE = 0;
    public static final int MODE_PAINT = 1;
    public static final int MODE_ADD_TEXT = 2;
    public static final int MODE_STICKER = 3;

    protected int currentMode;
    private ThumbnailFilter selectedFilter;
    private Bitmap originalBitmap;


    public static PhotoEditorFragment newInstance(String imagePath) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.MediaConstants.EXTRA_IMAGE_PATH, imagePath);
        PhotoEditorFragment photoEditorFragment = new PhotoEditorFragment();
        photoEditorFragment.setArguments(bundle);
        return photoEditorFragment;
    }

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    public PhotoEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_editor, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        try {

            if (emojiPopup != null)
                if (emojiPopup.isShowing()) emojiPopup.dismiss();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    @Override
    public void onEmojiToggle() {
        if (emojiPopup != null)
            if (!emojiPopup.isShowing())
                emojiPopup.toggle();
            else
                emojiPopup.dismiss();
    }

    protected void setVisibility(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(
                    context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onCropClicked(Bitmap bitmap);

        void onDoneClicked(String imagePath, String message);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mainImageView.setImageBitmap(bitmap);
        mainImageView.post(() -> photoEditorView.setBounds(mainImageView.getBitmapRect()));
    }

    public void setImageWithRect(Bitmap bitmap) {
        mainBitmap = getScaledBitmap(bitmap);
        mainImageView.setImageBitmap(mainBitmap);
        mainImageView.post(() -> photoEditorView.setBounds(mainImageView.getBitmapRect()));

        new GetFiltersTask(data -> {
            FilterImageAdapter filterImageAdapter = (FilterImageAdapter) filterRecylerview.getAdapter();
            if (filterImageAdapter != null) {
                filterImageAdapter.setData(data);
                filterImageAdapter.notifyDataSetChanged();
            }
        }, mainBitmap, getActivity()).execute();
    }


    private Bitmap getScaledBitmap(Bitmap resource) {
        int currentBitmapWidth = resource.getWidth();
        int currentBitmapHeight = resource.getHeight();
        int ivWidth = mainImageView.getWidth();
        int newHeight = (int) Math.floor(
                (double) currentBitmapHeight * ((double) ivWidth / (double) currentBitmapWidth));
        return Bitmap.createScaledBitmap(resource, ivWidth, newHeight, true);
    }

    private Bitmap getCroppedBitmap(Bitmap srcBitmap, Rect rect) {
        // Crop the subset from the original Bitmap.
        return Bitmap.createBitmap(srcBitmap,
                rect.left,
                rect.top,
                (rect.right - rect.left),
                (rect.bottom - rect.top));
    }

    public void reset() {
        photoEditorView.reset();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initView(View view) {


        inputPanel.setListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(fragment_photo_editor_layout).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);


        if (getArguments() != null && getActivity() != null && getActivity().getIntent() != null) {
            final String imagePath = getArguments().getString(AppConstants.MediaConstants.EXTRA_IMAGE_PATH);

            GlideApp.with(this)
                    .asBitmap()
                    .load(imagePath)
                    .into(new BitmapImageViewTarget(mainImageView) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            int currentBitmapWidth = resource.getWidth();
                            int currentBitmapHeight = resource.getHeight();
                            int ivWidth = mainImageView.getWidth();
                            int newHeight = (int) Math.floor((double) currentBitmapHeight * ((double) ivWidth / (double) currentBitmapWidth));
                            originalBitmap = Bitmap.createScaledBitmap(resource, ivWidth, newHeight, true);
                            mainBitmap = originalBitmap;
                            setImageBitmap(mainBitmap);


                            new GetFiltersTask(data -> {
                                FilterImageAdapter filterImageAdapter = (FilterImageAdapter) filterRecylerview.getAdapter();
                                if (filterImageAdapter != null) {
                                    filterImageAdapter.setData(data);
                                    filterImageAdapter.notifyDataSetChanged();
                                }
                            }, mainBitmap, getActivity()).execute();
                        }
                    });
                 /*   .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            int currentBitmapWidth = resource.getWidth();
                            int currentBitmapHeight = resource.getHeight();
                            int ivWidth = mainImageView.getWidth();
                            int newHeight = (int) Math.floor((double) currentBitmapHeight * ((double) ivWidth / (double) currentBitmapWidth));
                            originalBitmap = Bitmap.createScaledBitmap(resource, ivWidth, newHeight, true);
                            mainBitmap = originalBitmap;
                            setImageBitmap(mainBitmap);


                            new GetFiltersTask(data -> {
                                FilterImageAdapter filterImageAdapter = (FilterImageAdapter) filterRecylerview.getAdapter();
                                if (filterImageAdapter != null) {
                                    filterImageAdapter.setData(data);
                                    filterImageAdapter.notifyDataSetChanged();
                                }
                            }, mainBitmap, getActivity()).execute();
                        }
                    });
*/


            photoEditorView.setImageView(mainImageView, deleteButton, this);
            eraseButton.setOnClickListener(this);
            cropButton.setOnClickListener(this);
            stickerButton.setOnClickListener(this);
            addTextButton.setOnClickListener(this);
            paintButton.setOnClickListener(this);
            doneBtn.setOnClickListener(this);
            view.findViewById(R.id.back_iv).setOnClickListener(this);

            colorPickerView.setOnColorChangeListener(selectedColor -> {
                if (currentMode == MODE_PAINT) {
                    paintButton.setBackground(
                            AppHelper.tintDrawable(getContext(), R.drawable.bg_circle_red_ind, selectedColor));
                    photoEditorView.setColor(selectedColor);
                } else if (currentMode == MODE_ADD_TEXT) {
                    addTextButton.setBackground(
                            AppHelper.tintDrawable(getContext(), R.drawable.bg_circle_red_ind, selectedColor));
                    photoEditorView.setTextColor(selectedColor);
                }
            });
            photoEditorView.setColor(colorPickerView.getDefaultColor());
            photoEditorView.setTextColor(colorPickerView.getDefaultColor());

            filterLayout.post(() -> {
                filterLayoutHeight = filterLayout.getHeight();
                filterLayout.setTranslationY(filterLayoutHeight);
                photoEditorView.setOnTouchListener(new FilterTouchListener(filterLayout, filterLayoutHeight, mainImageView, photoEditorView, filterLabel, doneBtn));
            });


            FilterHelper filterHelper = new FilterHelper();
            filterRecylerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            FilterImageAdapter filterImageAdapter = new FilterImageAdapter(filterHelper.getFilters(getActivity()), this);
            filterRecylerview.setAdapter(filterImageAdapter);

        }
    }

    protected void onModeChanged(int currentMode) {
        Log.i(ImageEditActivity.class.getSimpleName(), "CM: " + currentMode);
        onStickerMode(currentMode == MODE_STICKER);
        onAddTextMode(currentMode == MODE_ADD_TEXT);
        onPaintMode(currentMode == MODE_PAINT);

        if (currentMode == MODE_PAINT || currentMode == MODE_ADD_TEXT) {
            AnimationsUtil.animate(getContext(), colorPickerView, R.anim.slide_in_right, View.VISIBLE, null);
            AnimationsUtil.animate(getContext(), eraseButton, R.anim.slide_in_right, View.VISIBLE, null);
        } else {
            AnimationsUtil.animate(getContext(), colorPickerView, R.anim.slide_out_right, View.INVISIBLE, null);
            AnimationsUtil.animate(getContext(), eraseButton, R.anim.slide_out_right, View.INVISIBLE, null);
        }
    }

    @Override
    public void onClick(final View view) {
        int id = view.getId();
        if (id == R.id.crop_btn) {
            if (selectedFilter != null) {
                new ApplyFilterTask(data -> {
                    if (data != null) {
                        mListener.onCropClicked(getBitmapCache(data));
                        photoEditorView.hidePaintView();
                    }
                }, Bitmap.createBitmap(originalBitmap)).execute(selectedFilter);
            } else {
                mListener.onCropClicked(getBitmapCache(originalBitmap));
                photoEditorView.hidePaintView();
            }
        } else if (id == R.id.stickers_btn) {
            setMode(MODE_STICKER);
        } else if (id == R.id.add_text_btn) {
            setMode(MODE_ADD_TEXT);
        } else if (id == R.id.paint_btn) {
            setMode(MODE_PAINT);
        } else if (id == R.id.eraseButton) {
            photoEditorView.onClickUndo();
        } else if (id == R.id.back_iv) {
            getActivity().onBackPressed();
        } else if (id == R.id.send_buttonn) {
            if (selectedFilter != null) {
                new ApplyFilterTask(data -> {
                    if (data != null) {

                        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
                        File cacheFile = new File(FilesManager.getCacheDir(), fileName);
                        new ProcessingImage(getBitmapCache(data), cacheFile.getAbsolutePath(),
                                data1 -> {
                                    String message = UtilsString.escapeJava(composeText.getText().toString().trim());
                                    mListener.onDoneClicked(data1, message);
                                }).execute();
                    }
                }, Bitmap.createBitmap(mainBitmap)).execute(selectedFilter);
            } else {

                String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
                File cacheFile = new File(FilesManager.getCacheDir(), fileName);
                new ProcessingImage(getBitmapCache(mainBitmap), cacheFile.getAbsolutePath(),
                        data -> {
                            String message = UtilsString.escapeJava(composeText.getText().toString().trim());
                            mListener.onDoneClicked(data, message);
                        }).execute();
            }
        }

        if (currentMode != MODE_NONE) {
            filterLabel.setAlpha(0f);
            mainImageView.animate().scaleX(1f);
            photoEditorView.animate().scaleX(1f);
            mainImageView.animate().scaleY(1f);
            photoEditorView.animate().scaleY(1f);
            filterLayout.animate().translationY(filterLayoutHeight);
        } else {
            filterLabel.setAlpha(1f);
        }
    }

    private void onAddTextMode(boolean status) {
        if (status) {
            addTextButton.setBackground(
                    AppHelper.tintDrawable(getContext(), R.drawable.bg_circle_red_ind, photoEditorView.getColor()));
            photoEditorView.addText();
        } else {
            addTextButton.setBackground(null);
            photoEditorView.hideTextMode();
        }
    }

    private void onPaintMode(boolean status) {
        if (status) {
            paintButton.setBackground(AppHelper.tintDrawable(getContext(), R.drawable.bg_circle_red_ind, photoEditorView.getColor()));
            photoEditorView.showPaintView();
        } else {
            paintButton.setBackground(null);
            photoEditorView.hidePaintView();
        }
    }

    private void onStickerMode(boolean status) {
        if (status) {
            stickerButton.setBackground(
                    AppHelper.tintDrawable(getContext(), R.drawable.bg_circle_red_ind, photoEditorView.getColor()));
            if (getActivity() != null && getActivity().getIntent() != null) {
                photoEditorView.showStickers(AppConstants.STICKERS_EDITOR_FOLDER_NAME);
            }
        } else {
            stickerButton.setBackground(null);
            photoEditorView.hideStickers();
        }
    }

    @Override
    public void onStartViewChangeListener(final View view) {
        Log.i(ImageEditActivity.class.getSimpleName(), "onStartViewChangeListener" + "" + view.getId());
        toolbarLayout.setVisibility(View.GONE);
        AnimationsUtil.animate(getContext(), deleteButton, R.anim.fade_in_medium, View.VISIBLE, null);
    }

    @Override
    public void onStopViewChangeListener(View view) {
        Log.i(ImageEditActivity.class.getSimpleName(), "onStopViewChangeListener" + "" + view.getId());
        deleteButton.setVisibility(View.GONE);
        AnimationsUtil.animate(getContext(), toolbarLayout, R.anim.fade_in_medium, View.VISIBLE, null);
    }

    private Bitmap getBitmapCache(Bitmap bitmap) {
        Matrix touchMatrix = mainImageView.getImageViewMatrix();

        Bitmap resultBit = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);

        float[] data = new float[9];
        touchMatrix.getValues(data);
        Matrix3 cal = new Matrix3(data);
        Matrix3 inverseMatrix = cal.inverseMatrix();
        Matrix m = new Matrix();
        m.setValues(inverseMatrix.getValues());

        float[] f = new float[9];
        m.getValues(f);
        int dx = (int) f[Matrix.MTRANS_X];
        int dy = (int) f[Matrix.MTRANS_Y];
        float scale_x = f[Matrix.MSCALE_X];
        float scale_y = f[Matrix.MSCALE_Y];
        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale_x, scale_y);

        photoEditorView.setDrawingCacheEnabled(true);
        if (photoEditorView.getDrawingCache() != null) {
            canvas.drawBitmap(photoEditorView.getDrawingCache(), 0, 0, null);
        }

        if (photoEditorView.getPaintBit() != null) {
            canvas.drawBitmap(photoEditorView.getPaintBit(), 0, 0, null);
        }

        canvas.restore();
        return resultBit;
    }

    @Override
    public void ThumbnailFilter(ThumbnailFilter thumbnailItem) {
        selectedFilter = thumbnailItem;

        new ApplyFilterTask(data -> {
            if (data != null) {
                setImageBitmap(data);
            }
        }, Bitmap.createBitmap(mainBitmap)).execute(thumbnailItem);
    }

    protected void setMode(int mode) {
        if (currentMode != mode) {
            onModeChanged(mode);
        } else {
            mode = MODE_NONE;
            onModeChanged(mode);
        }
        this.currentMode = mode;
    }


}
