package com.strolink.whatsUp.adapters.recyclerView.picker;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.picker.PickerHelper;
import com.strolink.whatsUp.interfaces.picker.OnSelectionListener;
import com.strolink.whatsUp.models.MediaPicker;

import java.util.ArrayList;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class RecentMediaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<MediaPicker> list;
    private OnSelectionListener onSelectionListener;
    private RequestManager glide;
    private RequestOptions options;

    public RecentMediaAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();

        glide = Glide.with(context);
        options = new RequestOptions().override(256).transform(new CenterCrop()).transform(new FitCenter());
    }

    public void addOnSelectionListener(OnSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public RecentMediaAdapter addImage(MediaPicker image) {
        list.add(image);
        notifyDataSetChanged();
        return this;
    }

    public ArrayList<MediaPicker> getItemList() {
        return list;
    }

    public void addImageList(ArrayList<MediaPicker> images) {
        list.addAll(images);
        notifyDataSetChanged();
    }

    public void clearList() {
        list.clear();
    }

    public void select(boolean selection, int pos) {
        if (pos < 100) {
            list.get(pos).setSelected(selection);
            notifyItemChanged(pos);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MediaFilesAdapter.HEADER) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_recent_media_picker, parent, false);
            return new HolderNone(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_recent_media_picker, parent, false);
            return new RecentMediaHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MediaPicker mediaPicker = list.get(position);
        return (mediaPicker.getContentUrl().isEmpty()) ? MediaFilesAdapter.HEADER : MediaFilesAdapter.ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaPicker mediaPicker = list.get(position);
        if (holder instanceof RecentMediaHolder) {
            RecentMediaHolder recentMediaHolder = (RecentMediaHolder) holder;
            int margin = 2;
            float size = PickerHelper.convertDpToPixel(72, context) - 2;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) size, (int) size);
            layoutParams.setMargins(margin, margin, margin, margin);
            recentMediaHolder.itemView.setLayoutParams(layoutParams);
            int padding = (int) (size / 3.5);
            recentMediaHolder.selection.setPadding(padding, padding, padding, padding);
            recentMediaHolder.preview.setLayoutParams(layoutParams);
            if (mediaPicker.getType().equals("video")) {

                long interval = 5000 * 1000;
                RequestOptions options = new RequestOptions()
                        .frame(interval)
                        .override(360)
                        .transform(new CenterCrop())
                        .transform(new FitCenter());

                glide.load(mediaPicker.getContentUrl())
                        .apply(options)
                        .into(recentMediaHolder.preview);
                recentMediaHolder.play_btn.setVisibility(View.VISIBLE);
            } else {
                glide.load(mediaPicker.getContentUrl())
                        .apply(options)
                        .into(recentMediaHolder.preview);
                recentMediaHolder.play_btn.setVisibility(View.GONE);
            }
            recentMediaHolder.selection.setVisibility(mediaPicker.getSelected() ? View.VISIBLE : View.GONE);
        } else {
            HolderNone noneHolder = (HolderNone) holder;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
            noneHolder.itemView.setLayoutParams(layoutParams);
            noneHolder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecentMediaHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatImageView preview;
        AppCompatImageView selection;
        AppCompatImageView play_btn;

        RecentMediaHolder(View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.preview);
            selection = itemView.findViewById(R.id.selection);
            play_btn = itemView.findViewById(R.id.play_btn);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = this.getLayoutPosition();
            onSelectionListener.OnClick(list.get(id), view, id);
        }

        @Override
        public boolean onLongClick(View view) {
            int id = this.getLayoutPosition();
            onSelectionListener.OnLongClick(list.get(id), view, id);
            return true;
        }
    }

    public class HolderNone extends RecyclerView.ViewHolder {
        HolderNone(View itemView) {
            super(itemView);
        }
    }
}
